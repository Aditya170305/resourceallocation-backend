package com.services.resourceallocation.service;

import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.services.resourceallocation.dto.TimetableUploadResponseDTO;
import com.services.resourceallocation.model.Resource;
import com.services.resourceallocation.model.TimetableEntry;
import com.services.resourceallocation.model.TimetableEntry.LectureSlot;
import com.services.resourceallocation.repository.ResourceRepository;
import com.services.resourceallocation.repository.TimetableEntryRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Parses the timetable Excel file and saves entries to `timetable_entries`.
 *
 * Excel format expected:
 * ┌──────────────────────────────────────┐
 * │ Resource - Computer Lab - 1          │  ← Resource header row (col A)
 * │        │ Subject │ Faculty │ Start  │ End │
 * │        │ DBMS    │ Dr. X   │ 09:00  │ 10:00 │
 * │        │ OS      │ Dr. Y   │ 10:00  │ 11:00 │
 * │  ...up to 6 data rows per resource   │
 * │ Resource - Computer Lab - 2          │  ← Next resource header
 * │        │ Subject │ Faculty │ ...     │
 * └──────────────────────────────────────┘
 *
 * Rules:
 *  - Row with content only in col A → resource header
 *  - Next row is column header (Subject / Faculty / StartTime / EndTime) — skip
 *  - Following rows (up to 6) are data rows
 *  - Each data row = one lecture slot (1st Lecture, 2nd Lecture, …)
 *  - Old entries for each resource are deleted before inserting new ones
 */
@Service
public class TimetableUploadService {

    private static final int MAX_LECTURES = 6;

    @Autowired private ResourceRepository      resourceRepository;
    @Autowired private TimetableEntryRepository timetableEntryRepository;

    @Transactional
    public TimetableUploadResponseDTO processUpload(MultipartFile file,
                                                    String department,
                                                    Integer hodId) throws Exception {

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please upload a valid Excel file.");
        }
        String filename = file.getOriginalFilename() != null
            ? file.getOriginalFilename().toLowerCase() : "";
        if (!filename.endsWith(".xlsx") && !filename.endsWith(".xls")) {
            throw new IllegalArgumentException("Only .xlsx and .xls files are supported.");
        }

        // Parse Excel
        List<ResourceBlock> blocks = parseExcel(file.getInputStream(), filename.endsWith(".xls"));

        if (blocks.isEmpty()) {
            throw new IllegalArgumentException(
                "No resource blocks found in the file. " +
                "Make sure column A contains 'Resource - <name>' headers.");
        }

        // Process each block
        List<String> warnings    = new ArrayList<>();
        List<String> insertedFor = new ArrayList<>();
        int totalRows    = 0;
        int rowsInserted = 0;
        int rowsSkipped  = 0;

        for (ResourceBlock block : blocks) {
            totalRows += block.rows.size();

            // Match resource name to DB
            Optional<Resource> resourceOpt = findResource(block.resourceName);
            if (resourceOpt.isEmpty()) {
                warnings.add("Resource not found in DB: '" + block.resourceName +
                             "' — " + block.rows.size() + " rows skipped.");
                rowsSkipped += block.rows.size();
                continue;
            }

            Resource resource = resourceOpt.get();

            // Delete old timetable entries for this resource
            Set<String> processedDays = new HashSet<>();

            for (DataRow row : block.rows) {

                if (!processedDays.contains(row.day)) {

                    timetableEntryRepository
                        .deleteByResource_ResourceIdAndDayName(
                            resource.getResourceId(),
                            row.day
                        );

                    processedDays.add(row.day);
                }

            }

            // Insert new entries (up to MAX_LECTURES slots)
            Map<String, Integer> daySlotMap = new HashMap<>();
            for (DataRow row : block.rows) {
                int currentSlot = daySlotMap.getOrDefault(row.day, 0);

                if (currentSlot >= MAX_LECTURES) {
                    warnings.add(
                        "Resource '" + block.resourceName +
                        "' for day '" + row.day +
                        "' has more than 6 lectures. Extra rows skipped."
                    );
                    rowsSkipped++;
                    continue;
                }
                if (row.subject.isBlank() || row.startTime == null || row.endTime == null) {
                    warnings.add("Skipped incomplete row for '" + block.resourceName + "'.");
                    rowsSkipped++;
                    continue;
                }

                TimetableEntry entry = new TimetableEntry();
                entry.setResource(resource);
                entry.setFacultyName(row.faculty.isBlank() ? "TBD" : row.faculty);
                entry.setClassType(row.subject);
                entry.setStartTime(row.startTime);
                entry.setEndTime(row.endTime);
                // entry.setLectureSlot(LectureSlot.fromIndex(slotIdx));
                // int currentSlot = daySlotMap.getOrDefault(row.day, 0);

                entry.setLectureSlot(
                    LectureSlot.fromIndex(currentSlot)
                );

                daySlotMap.put(row.day, currentSlot + 1);
                entry.setDayName(row.day);

                timetableEntryRepository.save(entry);
                rowsInserted++;
            }

           insertedFor.add(
                resource.getName() + " (" + block.rows.size() + " slots)"
            );
        }

        // Build response
        TimetableUploadResponseDTO resp = new TimetableUploadResponseDTO();
        resp.setTotalRowsRead(totalRows);
        resp.setRowsInserted(rowsInserted);
        resp.setRowsSkipped(rowsSkipped);
        resp.setResourcesUpdated(insertedFor.size());
        resp.setWarnings(warnings);
        resp.setInsertedFor(insertedFor);
        resp.setMessage(
            rowsInserted + " lecture slots saved across " +
            insertedFor.size() + " resource(s)." +
            (rowsSkipped > 0 ? " " + rowsSkipped + " rows skipped." : "")
        );
        return resp;
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE: Parse Excel file → list of ResourceBlocks
    // ─────────────────────────────────────────────────────────────

    private List<ResourceBlock> parseExcel(InputStream is, boolean isXls) throws Exception {

        Workbook workbook = isXls ? new HSSFWorkbook(is) : new XSSFWorkbook(is);
        Sheet    sheet    = workbook.getSheetAt(0);

        List<ResourceBlock> blocks = new ArrayList<>();

        String currentDay = ""; 

        ResourceBlock current = null;
        boolean skipNextRow   = false;   // true when next row is the column header row

         List<String> validDays = List.of(
                    "Monday",
                    "Tuesday",
                    "Wednesday",
                    "Thursday",
                    "Friday",
                    "Saturday",
                    "Sunday"
                );

        for (Row row : sheet) {
            if (row == null) continue;

            String colA = getCellString(row.getCell(0));
            String colB = getCellString(row.getCell(1));
            String colC = getCellString(row.getCell(2));
            String colD = getCellString(row.getCell(3));
            String colE = getCellString(row.getCell(4));

                if (
                    validDays.contains(colA.trim())
                    && colB.isBlank()
                ) {
                    currentDay = colA.trim();

                    // IMPORTANT:
                    // Do NOT skip next row here.
                    // Next row is actually "Resource - ..."
                    
                    continue;
                }

            // ── Detect resource header: col A has "Resource -" and rest are blank
            if (!colA.isBlank() && colA.toLowerCase().startsWith("resource")) { 
                // Extract resource name: "Resource - Computer Lab - 1" → "Computer Lab 1"
                String resourceName = extractResourceName(colA);
                current    = new ResourceBlock(resourceName);
                skipNextRow = true;   // next row is "Subject | Faculty | StartTime | EndTime"
                blocks.add(current);
                continue;
            }

            // ── Skip header row (Subject / Faculty / StartTime / EndTime)
            if (skipNextRow) {
                skipNextRow = false;
                continue;
            }

            // ── Data row: col B = Subject, col C = Faculty, col D = Start, col E = End
            if (current != null && !colB.isBlank()) {
                LocalTime start = parseTime(row.getCell(3));
                LocalTime end   = parseTime(row.getCell(4));
                current.rows.add(
                    new DataRow(
                        currentDay,
                        colB,
                        colC,
                        start,
                        end
                    )
                );
            }
        }

        workbook.close();
        return blocks;
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE: Extract resource name from header cell
    //  "Resource - Computer Lab - 1"   → "Computer Lab 1"
    //  "Resource - Computer Lab -2 "   → "Computer Lab 2"
    //  "Resource - Seminar Hall"        → "Seminar Hall"
    // ─────────────────────────────────────────────────────────────

    private String extractResourceName(String raw) {
        // Remove "Resource -" prefix
        String cleaned = raw.replaceFirst("(?i)^Resource\\s*-\\s*", "").trim();
        // Replace " - " with " " so "Computer Lab - 1" → "Computer Lab 1"
        cleaned = cleaned.replaceAll("\\s*-\\s*", " ").trim();
        // Normalise multiple spaces
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE: Fuzzy match resource name → DB Resource
    //  Tries exact match first, then case-insensitive, then contains
    // ─────────────────────────────────────────────────────────────

    private Optional<Resource> findResource(String parsedName) {
        List<Resource> all = resourceRepository.findAll();

        // 1. Exact match
        for (Resource r : all)
            if (r.getName().equalsIgnoreCase(parsedName)) return Optional.of(r);

        // 2. Normalise both (remove spaces/hyphens/numbers) and compare
        String norm = normalise(parsedName);
        for (Resource r : all)
            if (normalise(r.getName()).equals(norm)) return Optional.of(r);

        // 3. Contains match (e.g. "Computer Lab 1" contains "Lab 1")
        for (Resource r : all)
            if (normalise(r.getName()).contains(norm) ||
                norm.contains(normalise(r.getName()))) return Optional.of(r);

        return Optional.empty();
    }

    private String normalise(String s) {
        return s.toLowerCase()
                .replaceAll("[^a-z0-9]", "")   // remove all non-alphanumeric
                .trim();
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE: Cell value helpers
    // ─────────────────────────────────────────────────────────────

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // time cell: format as HH:mm
                    Date d = cell.getDateCellValue();
                    yield String.format("%02d:%02d",
                        d.getHours(), d.getMinutes());
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCachedFormulaResultType() == CellType.STRING
                ? cell.getRichStringCellValue().getString()
                : String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    /**
     * Parse a cell as LocalTime.
     * Handles:  datetime.time object stored as numeric, or string "10:00 AM"
     */
    private LocalTime parseTime(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date d = cell.getDateCellValue();
                return LocalTime.of(d.getHours(), d.getMinutes());
            }
            // Try string parse
            String s = getCellString(cell).trim().toUpperCase();
            if (s.isBlank()) return null;
            boolean pm = s.contains("PM");
            s = s.replace("AM","").replace("PM","").trim();
            String[] parts = s.split(":");
            int h = Integer.parseInt(parts[0].trim());
            int m = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            if (pm && h != 12) h += 12;
            if (!pm && h == 12) h = 0;
            return LocalTime.of(h, m);
        } catch (Exception e) {
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Inner data classes
    // ─────────────────────────────────────────────────────────────

    private static class ResourceBlock {
        String        resourceName;
        List<DataRow> rows = new ArrayList<>();
        ResourceBlock(String name) { this.resourceName = name; }
    }

    private static class DataRow {

    String day;
    String subject;
    String faculty;
    LocalTime startTime;
    LocalTime endTime;

    DataRow(
        String day,
        String subject,
        String faculty,
        LocalTime start,
        LocalTime end
    ) {
        this.day = day;
        this.subject = subject;
        this.faculty = faculty;
        this.startTime = start;
        this.endTime = end;
    }
}
}