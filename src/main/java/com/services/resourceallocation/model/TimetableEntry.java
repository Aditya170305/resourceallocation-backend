package com.services.resourceallocation.model;

import com.services.resourceallocation.convertor.LectureSlotConverter;
import jakarta.persistence.*;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "timetable_enteries")   // ← FIXED: was "timetable_enteries"
public class TimetableEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    @JsonIgnore
    private Resource resource;

    @Column(name = "faculty_name", nullable = false, length = 100)
    private String facultyName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "class_type", length = 100)
    private String classType;

    @Convert(converter = LectureSlotConverter.class)
    @Column(name = "lecture_slot", nullable = false)
    private LectureSlot lectureSlot;

    @Column(name = "day_name", nullable = false, length = 20)
    private String dayName;

    public enum LectureSlot {
        LECTURE_1("1st Lecture"),
        LECTURE_2("2nd Lecture"),
        LECTURE_3("3rd Lecture"),
        LECTURE_4("4th Lecture"),
        LECTURE_5("5th Lecture"),
        LECTURE_6("6th Lecture");

        private final String dbValue;
        LectureSlot(String dbValue) { this.dbValue = dbValue; }
        public String getDbValue() { return dbValue; }

        // ── ADDED: needed by TimetableUploadService ──────────
        public static LectureSlot fromIndex(int idx) {
            LectureSlot[] vals = values();
            if (idx < 0 || idx >= vals.length)
                throw new IllegalArgumentException(
                    "Lecture index out of range: " + idx);
            return vals[idx];
        }
    }

    // ── Getters & Setters (unchanged) ─────────────────────────

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }

    public LectureSlot getLectureSlot() { return lectureSlot; }
    public void setLectureSlot(LectureSlot lectureSlot) { this.lectureSlot = lectureSlot; }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public Integer getResourceId() {
        return resource != null ? resource.getResourceId() : null;
    }

    public String getResourceName() {
        return resource != null ? resource.getName() : null;
    }
}