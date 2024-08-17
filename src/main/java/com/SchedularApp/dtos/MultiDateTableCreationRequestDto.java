package com.SchedularApp.dtos;

import java.util.List;
import java.util.Map;

public class MultiDateTableCreationRequestDto {
    private String tableName;
    private Map<String, List<String>> dateTimeSlots; // Key: Date, Value: List of Times

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, List<String>> getDateTimeSlots() {
        return dateTimeSlots;
    }

    public void setDateTimeSlots(Map<String, List<String>> dateTimeSlots) {
        this.dateTimeSlots = dateTimeSlots;
    }
}
