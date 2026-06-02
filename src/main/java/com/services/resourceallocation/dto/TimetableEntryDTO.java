package com.services.resourceallocation.dto;

public class TimetableEntryDTO {

    private Integer id;
    private Integer resourceId;
    private String  resourceName;
    private String  facultyName;
    private String  startTime;     // "HH:mm"
    private String  endTime;       // "HH:mm"
    private String  classType;
    private String  lectureSlot;   // "1st Lecture" … "6th Lecture"
    private int     lectureIndex;  // 0-based for React (0=Lecture-1 … 5=Lecture-6)

    public TimetableEntryDTO() {}

    // ── Getters & Setters ──────────────────────────

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getResourceId() { return resourceId; }
    public void setResourceId(Integer resourceId) { this.resourceId = resourceId; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public String getLectureSlot() { return lectureSlot; }
    public void setLectureSlot(String lectureSlot) { this.lectureSlot = lectureSlot; }

    public int getLectureIndex() { return lectureIndex; }
    public void setLectureIndex(int lectureIndex) { this.lectureIndex = lectureIndex; }
}