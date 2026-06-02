package com.services.resourceallocation.dto;

import java.util.List;

/**
 * Summary returned to HOD after timetable upload.
 */
public class TimetableUploadResponseDTO {

    private int    totalRowsRead;       // total data rows in Excel
    private int    rowsInserted;        // rows successfully inserted
    private int    rowsSkipped;         // rows skipped (resource not found, etc.)
    private int    resourcesUpdated;    // distinct resources updated
    private List<String> warnings;      // resource names not found in DB
    private List<String> insertedFor;   // resource names successfully updated
    private String message;

    // ── Getters & Setters ──────────────────────────────────────

    public int     getTotalRowsRead()              { return totalRowsRead; }
    public void    setTotalRowsRead(int v)         { this.totalRowsRead = v; }

    public int     getRowsInserted()               { return rowsInserted; }
    public void    setRowsInserted(int v)          { this.rowsInserted = v; }

    public int     getRowsSkipped()                { return rowsSkipped; }
    public void    setRowsSkipped(int v)           { this.rowsSkipped = v; }

    public int     getResourcesUpdated()           { return resourcesUpdated; }
    public void    setResourcesUpdated(int v)      { this.resourcesUpdated = v; }

    public List<String> getWarnings()              { return warnings; }
    public void    setWarnings(List<String> v)     { this.warnings = v; }

    public List<String> getInsertedFor()           { return insertedFor; }
    public void    setInsertedFor(List<String> v)  { this.insertedFor = v; }

    public String  getMessage()                    { return message; }
    public void    setMessage(String v)            { this.message = v; }
}