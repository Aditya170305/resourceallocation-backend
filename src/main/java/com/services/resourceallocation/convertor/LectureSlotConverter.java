package com.services.resourceallocation.convertor;

import com.services.resourceallocation.model.TimetableEntry.LectureSlot;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LectureSlotConverter implements AttributeConverter<LectureSlot, String> {

    @Override
    public String convertToDatabaseColumn(LectureSlot slot) {
        if (slot == null) return null;
        return slot.getDbValue();   // LECTURE_1 → "1st Lecture"
    }

    @Override
    public LectureSlot convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        for (LectureSlot s : LectureSlot.values()) {
            if (s.getDbValue().equalsIgnoreCase(dbValue)) return s;
        }
        throw new IllegalArgumentException("Unknown lecture_slot: " + dbValue);
    }
}