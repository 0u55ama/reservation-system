package com.SchedularApp.services;

import com.SchedularApp.Repositories.CustomerRepository;
import com.SchedularApp.Repositories.TableRepository;
import com.SchedularApp.Entities.TableEntity;
import com.SchedularApp.Entities.TimeSlot;
import com.SchedularApp.Repositories.TimeSlotRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchedulingServiceImpl implements ScheduledDateService{

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;


    public Map<String, Map<String, String>> getAllDatesAndTimesForTable(String tableName) {
        Optional<TableEntity> table = tableRepository.findByName(tableName);
        if (table.isEmpty()) {
            return Collections.emptyMap(); // Table does not exist
        }

        return timeSlotRepository.findByTable(table.get()).stream()
                .filter(slot -> slot.getTime() != null) // Filter out slots with null times
                .collect(Collectors.groupingBy(
                        slot -> slot.getDate().toString(), // Group by date
                        LinkedHashMap::new, // Maintain insertion order
                        Collectors.toMap(
                                slot -> "time_" + slot.getTime().toString(), // Key: "time_HH:mm"
                                slot -> slot.getTime().toString(), // Value: HH:mm
                                (existing, replacement) -> existing, // Merge function in case of conflict
                                LinkedHashMap::new // Maintain insertion order within each date
                        )
                ));
    }

    public Map<String, String> getTimesForTableOnDate(String tableName, String date) {
        Optional<TableEntity> table = tableRepository.findByName(tableName);
        if (table.isEmpty()) {
            return Collections.emptyMap(); // Table does not exist
        }

        LocalDate parsedDate = LocalDate.parse(date);
        return timeSlotRepository.findByTableAndDate(table.get(), parsedDate).stream()
                .filter(slot -> slot.getTime() != null) // Filter out slots with null times
                .collect(Collectors.toMap(
                        slot -> "time_" + slot.getTime().toString(), // Key: "time_HH:mm"
                        slot -> slot.getTime().toString(), // Value: HH:mm
                        (existing, replacement) -> existing, // Merge function in case of conflict
                        LinkedHashMap::new // Maintain insertion order
                ));
    }


    public Map<String, Map<String, Map<String, String>>> getAvailableTimeSlots() {
        // Fetch all available time slots in one query
        List<TimeSlot> slots = timeSlotRepository.findAll()
                .stream()
                .filter(TimeSlot::isAvailable)
                .filter(slot -> slot.getTime() != null) // Filter out slots with null times
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


}