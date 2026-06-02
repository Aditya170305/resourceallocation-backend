package com.services.resourceallocation.dto;

import java.util.List;

/**
 * One row in the faculty dashboard schedule table.
 * Represents a single resource with 6 lecture slot columns.
 */
public class FacultyScheduleDTO {

    private Integer    resourceId;
    private String     resourceName;
    private String     resourceType;

    /**
     * Always exactly 6 items (index 0 = 1st Lecture … index 5 = 6th Lecture).
     * If the faculty has no class in a slot, hasClass = false.
     */
    private List<SlotInfo> slots;

    /* ── Inner class ── */
    public static class SlotInfo {
        private boolean hasClass;
        private String  startTime;   // "HH:mm" or null
        private String  endTime;     // "HH:mm" or null
        private String  classType;   // subject / class type or null
        private String  lectureSlot; // "1st Lecture" … "6th Lecture"

        public SlotInfo() {}

        public SlotInfo(boolean hasClass, String startTime,
                        String endTime, String classType, String lectureSlot) {
            this.hasClass   = hasClass;
            this.startTime  = startTime;
            this.endTime    = endTime;
            this.classType  = classType;
            this.lectureSlot = lectureSlot;
        }

        public boolean isHasClass()              { return hasClass; }
        public void    setHasClass(boolean v)    { this.hasClass = v; }

        public String  getStartTime()            { return startTime; }
        public void    setStartTime(String v)    { this.startTime = v; }

        public String  getEndTime()              { return endTime; }
        public void    setEndTime(String v)      { this.endTime = v; }

        public String  getClassType()            { return classType; }
        public void    setClassType(String v)    { this.classType = v; }

        public String  getLectureSlot()          { return lectureSlot; }
        public void    setLectureSlot(String v)  { this.lectureSlot = v; }
    }

    /* ── Getters & Setters ── */

    public Integer         getResourceId()             { return resourceId; }
    public void            setResourceId(Integer v)    { this.resourceId = v; }

    public String          getResourceName()           { return resourceName; }
    public void            setResourceName(String v)   { this.resourceName = v; }

    public String          getResourceType()           { return resourceType; }
    public void            setResourceType(String v)   { this.resourceType = v; }

    public List<SlotInfo>  getSlots()                  { return slots; }
    public void            setSlots(List<SlotInfo> v)  { this.slots = v; }
}