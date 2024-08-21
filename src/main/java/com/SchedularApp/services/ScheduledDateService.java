package com.SchedularApp.services;

import java.util.Map;

public interface ScheduledDateService {
    public Map<String, Map<String, String>> getAllDatesAndTimesForTable(String tableName);
    public Map<String, String> getTimesForTableOnDate(String tableName, String date);
    public Map<String, Map<String, Map<String, String>>> getAvailableTimeSlots();
}
