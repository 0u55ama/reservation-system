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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private KafkaTemplate<String, Object> kafkaTemplate;

//    public boolean  (BookingRequestDto bookingRequestDto) {
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
//                kafkaProducerServiceImpl.sendMessage("booking-confirmation-email", messageData);
//                kafkaProducerServiceImpl.sendMessage("booking-confirmation-whatsapp", messageData);
//
//                return true; // Time slot has been booked
//            } else {
//                return false; // Time slot is not available
//            }
//        } else {
//            throw new RuntimeException("Table not found");
//        }
//    }
//
//}

    @Transactional // Ensure this method is transactional
    public boolean bookTimeSlot(BookingRequestDto bookingRequestDto) {
        try {
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
                    customerRepository.save(customer);

                    // Update the time slot
                    TimeSlot timeSlot = existingSlot.get();
                    timeSlot.setAvailable(false);
                    timeSlot.setCustomer(customer);
                    timeSlotRepository.save(timeSlot);

                    // Start a Kafka transaction
                    kafkaTemplate.executeInTransaction(kafkaOperations -> {
                        Map<String, String> messageData = new HashMap<>();
                        messageData.put("tableName", table.getName());
                        messageData.put("date", bookingRequestDto.getDate());
                        messageData.put("time", bookingRequestDto.getTime());
                        messageData.put("firstname", customer.getFirstname());
                        messageData.put("lastname", customer.getLastname());
                        messageData.put("phonenumber", customer.getPhonenumber());

                        kafkaOperations.send("booking-confirmation-email", messageData);
                        kafkaOperations.send("booking-confirmation-whatsapp", messageData);
                        return true;
                    });

                    return true; // Time slot has been booked
                } else {
                    throw new RuntimeException("Time slot is not available");
                }
            } else {
                throw new RuntimeException("Table not found");
            }
        } catch (Exception e) {
            // Rollback the transaction if anything fails
            throw new RuntimeException("Booking failed, transaction rolled back", e);
        }
    }
}