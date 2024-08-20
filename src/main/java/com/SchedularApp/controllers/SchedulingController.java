package com.SchedularApp.controllers;

import com.SchedularApp.dtos.BookingRequestDto;
import com.SchedularApp.dtos.DeleteRequestDto;
import com.SchedularApp.dtos.MultiDateTableCreationRequestDto;
import com.SchedularApp.dtos.TimeSlotDto;
import com.SchedularApp.services.SchedulingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class SchedulingController {

    @Autowired
    private SchedulingServiceImpl schedulingService;


//    @PostMapping("/add")
//    public ResponseEntity<String> addTimeSlot(@RequestBody TimeSlotDto timeSlotRequestDto) {
//        boolean isAdded = schedulingService.saveTimeSlot(
//                timeSlotRequestDto.getTableName(),
//                LocalDate.parse(timeSlotRequestDto.getDate()),
//                LocalTime.parse(timeSlotRequestDto.getTime())
//        );
//        if (isAdded) {
//            return ResponseEntity.ok("Time slot added successfully.");
//        } else {
//            return ResponseEntity.badRequest().body("Time slot already exists.");
//        }
//    }

//    @PostMapping("/schedule")
//    public ResponseEntity<String> bookTimeSlot(@RequestBody TimeSlotDto timeSlotRequestDto) {
//        boolean isBooked = schedulingService.bookTimeSlot(
//                timeSlotRequestDto.getTableName(),
//                LocalDate.parse(timeSlotRequestDto.getDate()),
//                LocalTime.parse(timeSlotRequestDto.getTime())
//        );
//        if (isBooked) {
//            return ResponseEntity.ok("Time slot booked successfully.");
//        } else {
//            return ResponseEntity.badRequest().body("Time slot is not available at the moment.");
//        }
//    }

    @PostMapping("/schedule")
    public ResponseEntity<String> bookTimeSlot(@RequestBody BookingRequestDto bookingRequestDto) {
        boolean isBooked = schedulingService.bookTimeSlot(bookingRequestDto);
        if (isBooked) {
            return ResponseEntity.ok("Time slot booked successfully.");
        } else {
            return ResponseEntity.badRequest().body("Time slot is not available at the moment.");
        }
    }
//    {
//        "tableName": "table_A",
//            "date": "2024-01-01",
//            "time": "08:00",
//            "firstname": "John",
//            "lastname": "Doe",
//            "phonenumber": "123-456-7890"
//    "email" : email@emal.com
//    }


    @GetMapping("/booked-slots")
    public ResponseEntity<List<Map<String, String>>> getAllBookedSlotsWithCustomerDetails() {
        List<Map<String, String>> bookedSlots = schedulingService.getAllBookedSlotsWithCustomerDetails();
        return ResponseEntity.ok(bookedSlots);
    }

    @GetMapping("/get-all-dates-times")
    public ResponseEntity<Map<String, Map<String, String>>> getAllDatesAndTimesForTable(@RequestBody TimeSlotDto requestDto) {
        Map<String, Map<String, String>> datesAndTimes = schedulingService.getAllDatesAndTimesForTable(requestDto.getTableName());
        return ResponseEntity.ok(datesAndTimes);
    }
    @GetMapping("/get-times-for-date")
    public ResponseEntity<Map<String, String>> getTimesForTableOnDate(@RequestBody TimeSlotDto requestDto) {
        Map<String, String> times = schedulingService.getTimesForTableOnDate(
                requestDto.getTableName(),
                requestDto.getDate()
        );
        return ResponseEntity.ok(times);
    }

    @PostMapping("/create-table")
    public ResponseEntity<String> createTable(@RequestBody MultiDateTableCreationRequestDto requestDto) {
        boolean isCreated = schedulingService.createTable(
                requestDto.getTableName(),
                requestDto.getDateTimeSlots() != null ?
                        requestDto.getDateTimeSlots().entrySet().stream().collect(
                                Collectors.toMap(
                                        entry -> LocalDate.parse(entry.getKey()),
                                        entry -> entry.getValue() != null ?
                                                entry.getValue().stream().map(LocalTime::parse).collect(Collectors.toList())
                                                : null
                                )
                        ) : null
        );
        if (isCreated) {
            return ResponseEntity.ok("Table created successfully.");
        } else {
            return ResponseEntity.badRequest().body("Table creation failed. Table may already exist.");
        }
    }

// {
//     "tableName": "table_C",
// }

    @PostMapping("/add-dates-times")
    public ResponseEntity<String> addDatesAndTimesToTable(@RequestBody MultiDateTableCreationRequestDto requestDto) {
        boolean isUpdated = schedulingService.addDatesAndTimesToTable(
                requestDto.getTableName(),
                requestDto.getDateTimeSlots().entrySet().stream().collect(
                        Collectors.toMap(
                                entry -> LocalDate.parse(entry.getKey()),
                                entry -> entry.getValue() != null ?
                                        entry.getValue().stream().map(LocalTime::parse).collect(Collectors.toList())
                                        : null
                        )
                )
        );
        if (isUpdated) {
            return ResponseEntity.ok("Dates and times added successfully.");
        } else {
            return ResponseEntity.badRequest().body("Adding dates or times failed. The t able may not exist, or the dates/times may already be added.");
        }
    }

// {
//     "tableName": "table_C",
//     "dateTimeSlots": {
//         "2024-01-01": ["10:00"],
//         "2024-01-02": []
//     }
// }



    @GetMapping("/slots")
    public ResponseEntity<Map<String, Map<String, Map<String, String>>>> getAllTimeSlots() {
        Map<String, Map<String, Map<String, String>>> slots = schedulingService.getAvailableTimeSlots();
        return ResponseEntity.ok(slots);
    }
    @DeleteMapping("/delete-dates")
    public ResponseEntity<String> deleteDates(@RequestBody DeleteRequestDto requestDto) {
        boolean isDeleted = schedulingService.deleteDates(requestDto.getTableName(), requestDto.getDates());
        if (isDeleted) {
            return ResponseEntity.ok("Dates and all associated time slots deleted successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete dates.");
        }
    }
//    {
//        "tableName": "table_C",
//            "dates": [
//        "2024-01-02",
//        "2024-01-01"
//    ]
//    }

    @DeleteMapping("/delete-times")
    public ResponseEntity<String> deleteTimes(@RequestBody DeleteRequestDto requestDto) {
        boolean isDeleted = schedulingService.deleteTimes(requestDto.getTableName(), requestDto.getDates().get(0), requestDto.getTimes());
        if (isDeleted) {
            return ResponseEntity.ok("Time slots deleted successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete time slots.");
        }
    }
//    {
//        "tableName": "table_C",
//            "dates": [
//        "2024-01-01"
//    ],
//        "times": [
//        "10:00"
//    ]
//    }

    @DeleteMapping("/delete-table")
    public ResponseEntity<String> deleteTable(@RequestBody DeleteRequestDto requestDto) {
        boolean isDeleted = schedulingService.deleteTable(requestDto.getTableName());
        if (isDeleted) {
            return ResponseEntity.ok("Table and all associated dates and time slots deleted successfully.");
        } else {
            return ResponseEntity.badRequest().body("Failed to delete table.");
        }
    }

//    {
//        "tableName": "table_C",
//    }


}