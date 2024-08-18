package com.SchedularApp;
import com.SchedularApp.Repositories.TableRepository;
import com.SchedularApp.Repositories.TimeSlotRepository;
import com.SchedularApp.Entities.TableEntity;
import com.SchedularApp.Entities.TimeSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    @Autowired
//    private ScheduledDateRepository scheduledDateRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        if (scheduledDateRepository.count() == 0) {
//            scheduledDateRepository.save(new ScheduledDate(LocalDate.of(2024, 1, 1)));
//            scheduledDateRepository.save(new ScheduledDate(LocalDate.of(2024, 1, 2)));
//            scheduledDateRepository.save(new ScheduledDate(LocalDate.of(2024, 1, 3)));
//            scheduledDateRepository.save(new ScheduledDate(LocalDate.of(2024, 1, 4)));
//            scheduledDateRepository.save(new ScheduledDate(LocalDate.of(2024, 1, 5)));
//            scheduledDateRepository.save(new ScheduledDate(LocalDate.of(2024, 1, 6)));
//            scheduledDateRepository.save(new ScheduledDate(LocalDate.of(2024, 1, 7)));
//        }
//    }
//}
//@Component
//public class DataInitializer implements CommandLineRunner {
//
//    @Autowired
//    private TimeSlotRepository timeSlotRepository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        if (timeSlotRepository.count() == 0) {
//            // Initialize dates with specific time slots
//            LocalDate startDate = LocalDate.of(2024, 1, 1);
//            LocalTime[] times = {
//                    LocalTime.of(8, 0),
//                    LocalTime.of(9, 0),
//                    LocalTime.of(10, 0),
//            };
//
//            for (LocalTime time : times) {  // For each time slot
//                timeSlotRepository.save(new TimeSlot(startDate, time));
//            }
//        }
//    }
//}
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TableRepository tableEntityRepository;

    @Override
    public void run(String... args) throws Exception {
        if (timeSlotRepository.count() == 0) {
            // Initialize tables
            TableEntity tableA = new TableEntity("table_A");
            TableEntity tableB = new TableEntity("table_B");

            boolean availability = true;

            tableEntityRepository.save(tableA);
            tableEntityRepository.save(tableB);

            // Initialize dates with specific time slots
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalTime[] times = {
                    LocalTime.of(8, 0),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0),
            };

            // Save time slots for table_A
            for (LocalTime time : times) {
                timeSlotRepository.save(new TimeSlot(startDate, time, tableA,null, availability));
            }

            // Save time slots for table_B
            for (LocalTime time : times) {
                LocalDate date = startDate.plusDays(1);
                timeSlotRepository.save(new TimeSlot(date, time, tableB,null, availability));
            }

            // Save time slots for table_B
            for (LocalTime time : times) {
                LocalDate date = startDate.plusDays(2);
                timeSlotRepository.save(new TimeSlot(date, time, tableB, null, availability));
            }
        }
    }
}