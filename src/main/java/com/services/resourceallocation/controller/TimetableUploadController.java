package com.services.resourceallocation.controller;

import com.services.resourceallocation.dto.TimetableUploadResponseDTO;
import com.services.resourceallocation.service.TimetableUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "*")
public class TimetableUploadController {

    @Autowired
    private TimetableUploadService timetableUploadService;

    /**
     * POST /api/timetable/upload?department=Computer+Science+and+Engineering&hodId=4
     *
     * Accepts the Excel file uploaded by HOD.
     * Parses resource blocks, maps resource names to resource_id,
     * assigns lecture slots (1st Lecture … 6th Lecture) sequentially,
     * deletes old entries for those resources, inserts new rows.
     *
     * Returns a summary of what was inserted / skipped.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadTimetable(
            @RequestParam("file")       MultipartFile file,
            @RequestParam("department") String        department,
            @RequestParam("hodId")      Integer       hodId) {
        try {
            TimetableUploadResponseDTO result =
                timetableUploadService.processUpload(file, department, hodId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Upload failed: " + e.getMessage());
        }
    }
}