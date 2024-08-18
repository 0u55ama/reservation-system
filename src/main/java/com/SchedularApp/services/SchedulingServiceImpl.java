package com.SchedularApp.services;

import com.SchedularApp.Entities.Customer;
import com.SchedularApp.Repositories.CustomerRepository;
import com.SchedularApp.Repositories.TableRepository;
import com.SchedularApp.Entities.TableEntity;
import com.SchedularApp.Entities.TimeSlot;
import com.SchedularApp.Repositories.TimeSlotRepository;
import com.SchedularApp.dtos.BookingRequestDto;
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

    @Autowired
    private CustomerRepository customerRepository;

//    public boolean saveTimeSlot(String tableName, LocalDate date, LocalTime time) {
//        Optional<TableEntity> tableOpt = tableRepository.findByName(tableName);
//        if (tableOpt.isPresent()) {
//            TableEntity table = tableOpt.get();
//            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table, date, time);
//            if (existingSlot.isPresent()) {
//                return false; // Time slot is already taken for the given date
//            } else {
//                timeSlotRepository.save(new TimeSlot(date, time, table, true));
//                return true; // Time slot is available and has been saved
//            }
//        } else {
//            throw new RuntimeException("Table not found");
//        }
//    }

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

//    public boolean bookTimeSlot(String tableName, LocalDate date, LocalTime time) {
//        Optional<TableEntity> tableOpt = tableRepository.findByName(tableName);
//        if (tableOpt.isPresent()) {
//            TableEntity table = tableOpt.get();
//            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table, date, time);
//            if (existingSlot.isPresent() && existingSlot.get().isAvailable()) {
//                existingSlot.get().setAvailable(false);
//                timeSlotRepository.save(existingSlot.get());
//                return true; // Time slot has been booked
//            } else {
//                return false; // Time slot is not available
//            }
//        } else {
//            throw new RuntimeException("Table not found");
//        }
//    }

    public boolean bookTimeSlot(BookingRequestDto bookingRequestDto) {
        Optional<TableEntity> tableOpt = tableRepository.findByName(bookingRequestDto.getTableName());
        if (tableOpt.isPresent()) {
            TableEntity table = tableOpt.get();
            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table, LocalDate.parse(bookingRequestDto.getDate()), LocalTime.parse(bookingRequestDto.getTime()));
            if (existingSlot.isPresent() && existingSlot.get().isAvailable()) {
                // Create and save the customer
                Customer customer = new Customer();
                customer.setFirstname(bookingRequestDto.getFirstname());
                customer.setLastname(bookingRequestDto.getLastname());
                customer.setPhonenumber(bookingRequestDto.getPhonenumber());
                // Additional customer details can be set here

                customerRepository.save(customer);

                // Update the time slot
                TimeSlot timeSlot = existingSlot.get();
                timeSlot.setAvailable(false);
                timeSlot.setCustomer(customer);
                timeSlotRepository.save(timeSlot);

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

//    {
//        "tableName": "table_C",
//            "dateTimeSlots": {
//        "2024-01-01": ["10:00"],
//        "2024-01-02": []
//    }
//    }

//    {
//        "tableName": "table_C",
//    }

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

//    {
//        "tableName": "table_C",
//            "dateTimeSlots": {
//        "2024-01-01": ["10:00"],
//        "2024-01-02": []
//    }
//    }


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