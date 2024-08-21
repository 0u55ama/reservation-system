package com.SchedularApp.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface AdminService {

    public List<Map<String, String>> getAllBookedSlotsWithCustomerDetails();
    public boolean createTable(String tableName, Map<LocalDate, List<LocalTime>> dateTimeSlots);
    public boolean addDatesAndTimesToTable(String tableName, Map<LocalDate, List<LocalTime>> dateTimeSlots);
    public boolean deleteDates(String tableName, List<String> dates);
    public boolean deleteTimes(String tableName, String date, List<String> times);
    public boolean deleteTable(String tableName);
}
