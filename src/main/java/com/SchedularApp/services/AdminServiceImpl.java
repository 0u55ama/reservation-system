package com.SchedularApp.services;

import com.SchedularApp.Entities.TableEntity;
import com.SchedularApp.Entities.TimeSlot;
import com.SchedularApp.Repositories.TableRepository;
import com.SchedularApp.Repositories.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService{

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public List<Map<String, String>> getAllBookedSlotsWithCustomerDetails() {
        return timeSlotRepository.findAll().stream()
                .filter(slot -> !slot.isAvailable()) // Filter only booked slots
                .map(slot -> Map.of(
                        "tableName", slot.getTable().getName(),
                        "date", slot.getDate().toString(),
                        "time", slot.getTime().toString(),
                        "firstname", slot.getCustomer().getFirstname(),
                        "lastname", slot.getCustomer().getLastname(),
                        "phonenumber", slot.getCustomer().getPhonenumber()
                ))
                .collect(Collectors.toList());
    }

    public boolean createTable(String tableName, Map<LocalDate, List<LocalTime>> dateTimeSlots) {
        if (tableRepository.findByName(tableName).isPresent()) {
            return false; // Table already exists
        }

        // Create and save the new table
        TableEntity newTable = new TableEntity(tableName);
        tableRepository.save(newTable);

        // Process the dateTimeSlots map
        if (dateTimeSlots != null) {
            dateTimeSlots.forEach((date, times) -> {
                if (times.isEmpty()) {
                    // Create a TimeSlot for the date without specific times
                    timeSlotRepository.save(new TimeSlot(date, null, newTable, null, true));
                } else {
                    // Create TimeSlots for each time
                    times.forEach(time -> timeSlotRepository.save(new TimeSlot(date, time, newTable, null, true)));
                }
            });
        }

        return true;
    }

    public boolean addDatesAndTimesToTable(String tableName, Map<LocalDate, List<LocalTime>> dateTimeSlots) {
        Optional<TableEntity> existingTable = tableRepository.findByName(tableName);
        if (!existingTable.isPresent()) {
            return false; // Table does not exist
        }

        TableEntity table = existingTable.get();

        // Process the dateTimeSlots map
        dateTimeSlots.forEach((date, times) -> {
            if (times.isEmpty()) {
                // Only add a new date if it doesn't exist
                if (!timeSlotRepository.findByTableAndDateAndTime(table, date, null).isPresent()) {
                    timeSlotRepository.save(new TimeSlot(date, null, table, null,true));
                }
            } else {
                // Check if the date exists with null time slots and remove them
                timeSlotRepository.findByTableAndDate(table, date).stream()
                        .filter(slot -> slot.getTime() == null)
                        .forEach(slot -> timeSlotRepository.delete(slot));

                // Add times to the existing date if they don't exist
                times.stream()
                        .filter(time -> !timeSlotRepository.findByTableAndDateAndTime(table, date, time).isPresent())
                        .forEach(time -> timeSlotRepository.save(new TimeSlot(date, time, table, null, true)));
            }
        });

        return true;
    }

    public boolean deleteDates(String tableName, List<String> dates) {
        if (tableName == null || dates == null) {
            return false;
        }

        Optional<TableEntity> tableOpt = tableRepository.findByName(tableName);
        if (tableOpt.isPresent()) {
            TableEntity table = tableOpt.get();
            dates.stream()
                    .map(LocalDate::parse)
                    .forEach(date -> {
                        List<TimeSlot> timeSlots = timeSlotRepository.findByTableAndDate(table, date);
                        timeSlotRepository.deleteAll(timeSlots);
                    });
            return true;
        }
        return false;
    }

    public boolean deleteTimes(String tableName, String date, List<String> times) {
        if (tableName == null || date == null || times == null) {
            return false;
        }

        Optional<TableEntity> tableOpt = tableRepository.findByName(tableName);
        if (tableOpt.isPresent()) {
            TableEntity table = tableOpt.get();
            LocalDate parsedDate = LocalDate.parse(date);
            times.stream()
                    .map(LocalTime::parse)
                    .forEach(time -> {
                        Optional<TimeSlot> timeSlotOpt = timeSlotRepository.findByTableAndDateAndTime(table, parsedDate, time);
                        timeSlotOpt.ifPresent(timeSlotRepository::delete);
                    });
            return true;
        }
        return false;
    }

    public boolean deleteTable(String tableName) {
        if (tableName == null) {
            return false;
        }

        Optional<TableEntity> tableOpt = tableRepository.findByName(tableName);
        if (tableOpt.isPresent()) {
            TableEntity table = tableOpt.get();
            List<TimeSlot> timeSlots = timeSlotRepository.findByTable(table);
            if (!timeSlots.isEmpty()) {
                timeSlotRepository.deleteAll(timeSlots);
            }
            tableRepository.delete(table);
            return true;
        }
        return false;
    }


}
