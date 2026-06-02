package com.services.resourceallocation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.services.resourceallocation.dto.FacultyScheduleDTO;
import com.services.resourceallocation.dto.ResourceDTO;
import com.services.resourceallocation.dto.TimetableEntryDTO;
import com.services.resourceallocation.model.Resource;
import com.services.resourceallocation.model.TimetableEntry;
import com.services.resourceallocation.repository.TimetableEntryRepository;
import com.services.resourceallocation.service.ResourceService;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    // GET /api/resources — all resources
    @GetMapping
    public ResponseEntity<List<ResourceDTO>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }

    // GET /api/resources/category?name=Labs&department=Computer+Science
    @GetMapping("/category")
    public ResponseEntity<List<ResourceDTO>> getByCategory(
            @RequestParam("name") String categoryName,
            @RequestParam(value = "department", required = false) String department) {

        if (department != null && !department.isBlank()) {
            return ResponseEntity.ok(
                resourceService.getResourcesByCategoryAndDepartment(categoryName, department));
        }
        return ResponseEntity.ok(resourceService.getResourcesByCategory(categoryName));
    }

    // GET /api/resources/timetable?resourceName=Computer+Lab+1
    @GetMapping("/timetable")
    public ResponseEntity<List<TimetableEntryDTO>> getTimetableByName(
            @RequestParam("resourceName") String resourceName) {
        return ResponseEntity.ok(resourceService.getTimetableByResourceName(resourceName));
    }

    // ── NEW ────────────────────────────────────────────────────────
    // GET /api/resources/faculty-schedule?facultyName=Dr.+Emily+Davis
    //
    // Returns only the resources where this faculty has lectures,
    // each with exactly 6 slot columns (hasClass true/false + times).
    // Used by the Faculty Dashboard "Today's Class Schedule" table.
    @GetMapping("/faculty-schedule")
    public ResponseEntity<List<FacultyScheduleDTO>> getFacultySchedule(
            @RequestParam("facultyName") String facultyName) {
        return ResponseEntity.ok(resourceService.getFacultySchedule(facultyName));
    }

    //    HOD adds a new resource
    @PostMapping("/add")
    public ResponseEntity<?> addResource(@RequestBody Resource resource) {
        try {
            ResourceDTO saved = resourceService.saveResource(resource);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add resource: " + e.getMessage());
        }
    }
 
    // ── DELETE /api/resources/{id}
    //    HOD deletes a resource (also removes timetable entries)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable("id") Integer resourceId) {
        try {
            resourceService.deleteResource(resourceId);
            return ResponseEntity.ok("Resource deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete: " + e.getMessage());
        }
    }

    @Autowired
    private TimetableEntryRepository timetableEntryRepository;

    // ── GET TIMETABLE BY RESOURCE + DAY ─────────────────
    @GetMapping("/{resourceId}/timetable")
    public ResponseEntity<List<TimetableEntry>> getTimetable(
            @PathVariable Integer resourceId,
            @RequestParam String day
    ) {

        List<TimetableEntry> entries =
                timetableEntryRepository
                        .findByResource_ResourceIdAndDayName(
                                resourceId,
                                day
                        );

        return ResponseEntity.ok(entries);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>>
    getDepartments() {

        return ResponseEntity.ok(
            resourceService.getAllDepartments()
        );
    }

    @GetMapping("/count")
public ResponseEntity<Long> getResourceCount(
        @RequestParam(required = false) String department
    ) {

        if (
            department == null ||
            department.equalsIgnoreCase("All Departments")
        ) {

            return ResponseEntity.ok(
                resourceService.getTotalResourceCount()
            );
        }

        return ResponseEntity.ok(
            resourceService.getResourceCountByDepartment(
                department
            )
        );
    }
}