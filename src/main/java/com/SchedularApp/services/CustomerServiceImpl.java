package com.SchedularApp.services;

import com.SchedularApp.Entities.Customer;
import com.SchedularApp.Entities.TableEntity;
import com.SchedularApp.Entities.TimeSlot;
import com.SchedularApp.Repositories.CustomerRepository;
import com.SchedularApp.Repositories.TableRepository;
import com.SchedularApp.Repositories.TimeSlotRepository;
import com.SchedularApp.dtos.BookingRequestDto;
import com.SchedularApp.kafka.KafkaProducerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private KafkaProducerServiceImpl kafkaProducerServiceImpl;

    @Autowired
    private KafkaTemplate<String, Map<String, String>> kafkaTemplate;  // Transactional KafkaTemplate



//        @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class, timeout = 60)
//    public boolean bookTimeSlot(BookingRequestDto bookingRequestDto) {
//        Optional<TableEntity> tableOpt = tableRepository.findByName(bookingRequestDto.getTableName());
//        if (tableOpt.isPresent()) {
//            TableEntity table = tableOpt.get();
//            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table, LocalDate.parse(bookingRequestDto.getDate()), LocalTime.parse(bookingRequestDto.getTime()));
//            if (existingSlot.isPresent() && existingSlot.get().isAvailable()) {
//                // Create and save the customer
//                Customer customer = new Customer();
//                customer.setFirstname(bookingRequestDto.getFirstname());
//                customer.setLastname(bookingRequestDto.getLastname());
//                customer.setPhonenumber(bookingRequestDto.getPhonenumber());
//                customer.setEmail(bookingRequestDto.getEmail());
//                customerRepository.save(customer);
//
//                // Update the time slot
//                TimeSlot timeSlot = existingSlot.get();
//                timeSlot.setAvailable(false);
//                timeSlot.setCustomer(customer);
//                timeSlotRepository.save(timeSlot);
//
//                // Send booking details to Kafka topics
//                Map<String, String> messageData = new HashMap<>();
//                messageData.put("tableName", table.getName());
//                messageData.put("date", bookingRequestDto.getDate());
//                messageData.put("time", bookingRequestDto.getTime());
//                messageData.put("firstname", customer.getFirstname());
//                messageData.put("lastname", customer.getLastname());
//                messageData.put("phonenumber", customer.getPhonenumber());
//                messageData.put("email", customer.getEmail());
//
//                try {
//                    kafkaProducerServiceImpl.sendMessage("booking-confirmation-email", messageData);
//                    kafkaProducerServiceImpl.sendMessage("booking-confirmation-whatsapp", messageData);
//                } catch (KafkaException e) {
//                    throw new RuntimeException("Notification service failed, rolling back transaction", e);
//                }
//
//                return true; // Time slot has been booked
//            } else {
//                return false; // Time slot is not available
//            }
//        } else {
//            throw new RuntimeException("Table not found");
//        }
//    }
//}
//
//    @Transactional(transactionManager = "transactionManager", rollbackFor = IllegalArgumentException.class, timeout = 60)
//    public boolean bookTimeSlot(BookingRequestDto bookingRequestDto) {
//        Optional<TableEntity> tableOpt = tableRepository.findByName(bookingRequestDto.getTableName());
//        if (tableOpt.isPresent()) {
//            TableEntity table = tableOpt.get();
//            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table,
//                    LocalDate.parse(bookingRequestDto.getDate()), LocalTime.parse(bookingRequestDto.getTime()));
//
//            if (existingSlot.isPresent() && existingSlot.get().isAvailable()) {
//                // Prepare booking data
//                Map<String, String> messageData = new HashMap<>();
//                messageData.put("tableName", table.getName());
//                messageData.put("date", bookingRequestDto.getDate());
//                messageData.put("time", bookingRequestDto.getTime());
//                messageData.put("firstname", bookingRequestDto.getFirstname());
//                messageData.put("lastname", bookingRequestDto.getLastname());
//                messageData.put("phonenumber", bookingRequestDto.getPhonenumber());
//                messageData.put("email", bookingRequestDto.getEmail());  // Add email here
//
//                try {
//                    // Start Kafka transaction to send notifications
//                    kafkaTemplate.executeInTransaction(kafkaOperations -> {
//                        // Send email notification
//                        kafkaOperations.send("booking-confirmation-email", messageData);
//                        // Send WhatsApp notification
//                        kafkaOperations.send("booking-confirmation-whatsapp", messageData);
//                        return true;
//                    });
//
//                    // Only save the timeslot if the notification was successful
//                    Customer customer = new Customer();
//                    customer.setFirstname(bookingRequestDto.getFirstname());
//                    customer.setLastname(bookingRequestDto.getLastname());
//                    customer.setPhonenumber(bookingRequestDto.getPhonenumber());
//                    customer.setEmail(bookingRequestDto.getEmail());
//                    customerRepository.save(customer);
//
//                    TimeSlot timeSlot = existingSlot.get();
//                    timeSlot.setAvailable(false);  // Mark slot as unavailable
//                    timeSlot.setCustomer(customer);
//                    timeSlotRepository.save(timeSlot);
//
//                    return true;  // Time slot booking is successful
//                } catch (KafkaException e) {
//                    // If notification fails, the transaction will roll back
//                    throw new RuntimeException("Failed to send notifications, rolling back the transaction.", e);
//                }
//            } else {
//                return false;  // Time slot is not available
//            }
//        } else {
//            throw new RuntimeException("Table not found");
//        }
//    }
//}

    private final CountDownLatch notificationLatch = new CountDownLatch(1);
    private boolean notificationSuccess = false;

    @Transactional(transactionManager = "transactionManager", rollbackFor = IllegalArgumentException.class, timeout = 60)
    public boolean bookTimeSlot(BookingRequestDto bookingRequestDto) throws InterruptedException {
        Optional<TableEntity> tableOpt = tableRepository.findByName(bookingRequestDto.getTableName());
        if (tableOpt.isPresent()) {
            TableEntity table = tableOpt.get();
            Optional<TimeSlot> existingSlot = timeSlotRepository.findByTableAndDateAndTime(table, LocalDate.parse(bookingRequestDto.getDate()), LocalTime.parse(bookingRequestDto.getTime()));

            if (existingSlot.isPresent() && existingSlot.get().isAvailable()) {
                // Prepare booking data
                Map<String, String> messageData = new HashMap<>();
                messageData.put("tableName", table.getName());
                messageData.put("date", bookingRequestDto.getDate());
                messageData.put("time", bookingRequestDto.getTime());
                messageData.put("firstname", bookingRequestDto.getFirstname());
                messageData.put("lastname", bookingRequestDto.getLastname());
                messageData.put("phonenumber", bookingRequestDto.getPhonenumber());
                messageData.put("email", bookingRequestDto.getEmail());  // Add email here

                try {
                    // Start Kafka transaction to send notifications
                    kafkaTemplate.executeInTransaction(kafkaOperations -> {
                        kafkaOperations.send("booking-confirmation-email", messageData);
                        kafkaOperations.send("booking-confirmation-whatsapp", messageData);
                        return true;
                    });

                    // Wait for the notification service to confirm success
                    boolean receivedResponse = notificationLatch.await(1, TimeUnit.MINUTES);  // Wait for 1 minute
                    if (!receivedResponse || !notificationSuccess) {
                        throw new RuntimeException("Notification failed, rolling back the transaction.");
                    }

                    // Save the timeslot only if notification was successful
                    Customer customer = new Customer();
                    customer.setFirstname(bookingRequestDto.getFirstname());
                    customer.setLastname(bookingRequestDto.getLastname());
                    customer.setPhonenumber(bookingRequestDto.getPhonenumber());
                    customer.setEmail(bookingRequestDto.getEmail());
                    customerRepository.save(customer);

                    TimeSlot timeSlot = existingSlot.get();
                    timeSlot.setAvailable(false);  // Mark slot as unavailable
                    timeSlot.setCustomer(customer);
                    timeSlotRepository.save(timeSlot);

                    return true;  // Time slot booking is successful
                } catch (KafkaException e) {
                    // If notification fails, the transaction will roll back
                    throw new RuntimeException("Failed to send notifications, rolling back the transaction.", e);
                }
            } else {
                return false;  // Time slot is not available
            }
        } else {
            throw new RuntimeException("Table not found");
        }
    }

    @KafkaListener(topics = "notification-response", groupId = "notification-group")
    public void handleNotificationResponse(Map<String, String> response) {
        if ("success".equals(response.get("status"))) {
            notificationSuccess = true;
        } else {
            notificationSuccess = false;
        }
        notificationLatch.countDown();  // Release the latch
    }
}