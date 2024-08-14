package com.SchedularApp.services;

import com.SchedularApp.Repositories.TableRepository;
import com.SchedularApp.Entities.TableEntity;
import com.SchedularApp.Entities.TimeSlot;
import com.SchedularApp.Repositories.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class SchedulingServiceImpl {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public boolean saveTimeSlot(String tableName, LocalDate date, LocalTime time) {
        Optional<TableEntity> tableOpt = tableRepository.findByName(tableName);
        if (tableOpt.isPresent()) {
            TableEntity table = tableOpt.get();
            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table, date, time);
            if (existingSlot.isPresent()) {
                return false; // Time slot is already taken for the given date
            } else {
                timeSlotRepository.save(new TimeSlot(date, time, table, true));
                return true; // Time slot is available and has been saved
            }
        } else {
            throw new RuntimeException("Table not found");
        }
    }

    public boolean bookTimeSlot(String tableName, LocalDate date, LocalTime time) {
        Optional<TableEntity> tableOpt = tableRepository.findByName(tableName);
        if (tableOpt.isPresent()) {
            TableEntity table = tableOpt.get();
            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table, date, time);
            if (existingSlot.isPresent() && existingSlot.get().isAvailable()) {
                existingSlot.get().setAvailable(false);
                timeSlotRepository.save(existingSlot.get());
                return true; // Time slot has been booked
            } else {
                return false; // Time slot is not available
            }
        } else {
            throw new RuntimeException("Table not found");
        }
    }

    public Map<String, Map<String, Map<String, String>>> getAvailableTimeSlots() {
        List<TableEntity> tables = tableRepository.findAll();
        Map<String, Map<String, Map<String, String>>> response = new LinkedHashMap<>();

        for (TableEntity table : tables) {
            Map<String, Map<String, String>> tableData = new LinkedHashMap<>();
            List<TimeSlot> slots = timeSlotRepository.findByTable(table);

            for (TimeSlot slot : slots) {
                if (slot.isAvailable()) {
                    String dateKey = slot.getDate().toString();
                    tableData.putIfAbsent(dateKey, new LinkedHashMap<>());
                    String timeKey = "time_" + slot.getTime().toString();
                    tableData.get(dateKey).put(timeKey, slot.getTime().toString());
                }
            }

            response.put(table.getName(), tableData);
        }

        return response;
    }
}