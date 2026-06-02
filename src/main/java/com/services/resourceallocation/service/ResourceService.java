package com.services.resourceallocation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.services.resourceallocation.dto.FacultyScheduleDTO;
import com.services.resourceallocation.dto.FacultyScheduleDTO.SlotInfo;
import com.services.resourceallocation.dto.ResourceDTO;
import com.services.resourceallocation.dto.TimetableEntryDTO;
import com.services.resourceallocation.model.Resource;
import com.services.resourceallocation.model.TimetableEntry;
import com.services.resourceallocation.repository.ResourceRepository;
import com.services.resourceallocation.repository.TimetableEntryRepository;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private TimetableEntryRepository timetableEntryRepository;

    private static final String[] SLOT_LABELS = {
        "1st Lecture", "2nd Lecture", "3rd Lecture",
        "4th Lecture", "5th Lecture", "6th Lecture"
    };

    private static final Map<String, Integer> SLOT_INDEX = Map.of(
        "1st Lecture", 0, "2nd Lecture", 1, "3rd Lecture", 2,
        "4th Lecture", 3, "5th Lecture", 4, "6th Lecture", 5
    );

    // ── Entity → DTO ──────────────────────────────────────────────
    private ResourceDTO toResourceDTO(Resource r) {
        return new ResourceDTO(
            r.getResourceId(), r.getName(), r.getType().name(),
            r.getDepartment(), r.getCapacity(), r.getLocation(), r.getAmenities()
        );
    }

    private TimetableEntryDTO toTimetableDTO(TimetableEntry t) {
        TimetableEntryDTO dto = new TimetableEntryDTO();
        dto.setId(t.getId());
        dto.setResourceId(t.getResource().getResourceId());
        dto.setResourceName(t.getResource().getName());
        dto.setFacultyName(t.getFacultyName());
        dto.setStartTime(t.getStartTime().toString());
        dto.setEndTime(t.getEndTime().toString());
        dto.setClassType(t.getClassType());
        dto.setLectureSlot(t.getLectureSlot().getDbValue());
        dto.setLectureIndex(SLOT_INDEX.getOrDefault(t.getLectureSlot().getDbValue(), 0));
        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    //  GET ALL RESOURCES
    // ─────────────────────────────────────────────────────────────
    public List<ResourceDTO> getAllResources() {
        return resourceRepository.findAll()
            .stream().map(this::toResourceDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  GET RESOURCES BY DEPARTMENT
    //  Returns dept-specific + General (shared halls etc.)
    // ─────────────────────────────────────────────────────────────
    public List<ResourceDTO> getResourcesByDepartment(String department) {
        List<Resource> deptResources    = resourceRepository.findByDepartment(department);
        List<Resource> generalResources = resourceRepository.findByDepartment("General");

        List<Resource> combined = new ArrayList<>(deptResources);
        for (Resource r : generalResources) {
            if (combined.stream().noneMatch(c -> c.getResourceId().equals(r.getResourceId()))) {
                combined.add(r);
            }
        }
        return combined.stream().map(this::toResourceDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  GET BY CATEGORY
    // ─────────────────────────────────────────────────────────────
    public List<ResourceDTO> getResourcesByCategory(String category) {
        Resource.ResourceType type = mapCategoryToType(category);
        return resourceRepository.findByType(type)
            .stream().map(this::toResourceDTO).collect(Collectors.toList());
    }

    public List<ResourceDTO> getResourcesByCategoryAndDepartment(String category,
                                                                  String department) {
        Resource.ResourceType type = mapCategoryToType(category);

        if (department == null || department.isBlank()) {
            return resourceRepository.findByType(type)
                .stream().map(this::toResourceDTO).collect(Collectors.toList());
        }

        List<Resource> deptResources    = resourceRepository.findByTypeAndDepartment(type, department);
        List<Resource> generalResources = resourceRepository.findByTypeAndDepartment(type, "General");

        List<Resource> combined = new ArrayList<>(deptResources);
        for (Resource r : generalResources) {
            if (combined.stream().noneMatch(c -> c.getResourceId().equals(r.getResourceId()))) {
                combined.add(r);
            }
        }
        return combined.stream().map(this::toResourceDTO).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  TIMETABLE
    // ─────────────────────────────────────────────────────────────
    public List<TimetableEntryDTO> getTimetableByResourceId(Integer resourceId) {
        return timetableEntryRepository.findByResourceIdEager(resourceId)
            .stream().map(this::toTimetableDTO).collect(Collectors.toList());
    }

    public List<TimetableEntryDTO> getTimetableByResourceName(String resourceName) {
        return resourceRepository.findByName(resourceName)
            .map(r -> getTimetableByResourceId(r.getResourceId()))
            .orElse(List.of());
    }

    // ─────────────────────────────────────────────────────────────
    //  FACULTY SCHEDULE (dashboard)
    // ─────────────────────────────────────────────────────────────
    public List<FacultyScheduleDTO> getFacultySchedule(String facultyName) {
        List<TimetableEntry> entries =
            timetableEntryRepository.findByFacultyName(facultyName);
        if (entries.isEmpty()) return List.of();

        Map<Integer, List<TimetableEntry>> byResource = entries.stream()
            .collect(Collectors.groupingBy(t -> t.getResource().getResourceId()));

        List<FacultyScheduleDTO> result = new ArrayList<>();
        for (Map.Entry<Integer, List<TimetableEntry>> e : byResource.entrySet()) {
            List<TimetableEntry> resourceEntries = e.getValue();
            TimetableEntry first = resourceEntries.get(0);

            Map<String, TimetableEntry> slotMap = resourceEntries.stream()
                .collect(Collectors.toMap(
                    t -> t.getLectureSlot().getDbValue(),
                    t -> t, (a, b) -> a));

            List<SlotInfo> slots = new ArrayList<>();
            for (String label : SLOT_LABELS) {
                TimetableEntry match = slotMap.get(label);
                if (match != null) {
                    slots.add(new SlotInfo(true,
                        match.getStartTime().toString(),
                        match.getEndTime().toString(),
                        match.getClassType(), label));
                } else {
                    slots.add(new SlotInfo(false, null, null, null, label));
                }
            }

            FacultyScheduleDTO row = new FacultyScheduleDTO();
            row.setResourceId(first.getResource().getResourceId());
            row.setResourceName(first.getResource().getName());
            row.setResourceType(first.getResource().getType().name());
            row.setSlots(slots);
            result.add(row);
        }

        result.sort((a, b) -> a.getResourceName().compareTo(b.getResourceName()));
        return result;
    }

    // ─────────────────────────────────────────────────────────────
    //  ADD RESOURCE
    // ─────────────────────────────────────────────────────────────
    public ResourceDTO saveResource(Resource resource) {
        return toResourceDTO(resourceRepository.save(resource));
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE RESOURCE
    //  Deletes timetable entries first (FK constraint), then resource
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void deleteResource(Integer resourceId) {
        // 1. Remove timetable entries for this resource
        timetableEntryRepository.deleteByResourceId(resourceId);

        // 2. Delete the resource itself
        resourceRepository.deleteById(resourceId);
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE UTIL
    // ─────────────────────────────────────────────────────────────
    private Resource.ResourceType mapCategoryToType(String category) {
        return switch (category.toLowerCase()) {
            case "labs"       -> Resource.ResourceType.Lab;
            case "halls"      -> Resource.ResourceType.Hall;
            case "classrooms" -> Resource.ResourceType.Classroom;
            default           -> Resource.ResourceType.Lab;
        };
    }

    public List<String> getAllDepartments() {

        return resourceRepository.findAll()
                .stream()
                .map(Resource::getDepartment)
                .filter(dep -> dep != null)
                .map(String::trim)
                .filter(dep -> !dep.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    public Long getTotalResourceCount() {

        return resourceRepository.count();
    }

   public Long getResourceCountByDepartment(
        String department
) {

    Long deptCount =
            resourceRepository.countByDepartment(department);

    Long generalCount =
            resourceRepository.countByDepartment("General");

    return deptCount + generalCount;
    }
}