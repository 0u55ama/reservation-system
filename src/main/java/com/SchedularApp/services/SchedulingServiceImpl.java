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
import java.util.stream.Collectors;

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
        // Fetch all available time slots in one query
        List<TimeSlot> slots = timeSlotRepository.findAll()
                .stream()
                .filter(TimeSlot::isAvailable)
                .toList();

        return slots.stream()
                .collect(Collectors.groupingBy(
                        slot -> slot.getTable().getName(), // Group by table name
                        Collectors.groupingBy(
                                slot -> slot.getDate().toString(), // Group by date
                                Collectors.toMap(
                                        slot -> "time_" + slot.getTime().toString(), // Key: "time_HH:mm"
                                        slot -> slot.getTime().toString(), // Value: HH:mm
                                        (existing, replacement) -> existing, // Merge function in case of conflict
                                        LinkedHashMap::new // Use LinkedHashMap to maintain insertion order
                                )
                        )
                ));
    }
    public boolean createMultiDateTableWithTimeSlots(String tableName, Map<LocalDate, List<LocalTime>> dateTimeSlots) {
        Optional<TableEntity> existingTable = tableRepository.findByName(tableName);
        if (existingTable.isPresent()) {
            return false; // Table already exists
        }

        // Create and save the new table
        TableEntity newTable = new TableEntity(tableName);
        tableRepository.save(newTable);

        // Create and save the time slots for each date
        for (Map.Entry<LocalDate, List<LocalTime>> entry : dateTimeSlots.entrySet()) {
            LocalDate date = entry.getKey();
            List<LocalTime> times = entry.getValue();
            for (LocalTime time : times) {
                TimeSlot timeSlot = new TimeSlot(date, time, newTable, true);
                timeSlotRepository.save(timeSlot);
            }
        }

        return true;
    }
}