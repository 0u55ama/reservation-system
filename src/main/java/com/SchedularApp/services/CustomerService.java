package com.SchedularApp.services;

import com.SchedularApp.dtos.BookingRequestDto;

public interface CustomerService {
    public boolean bookTimeSlot(BookingRequestDto bookingRequestDto) throws InterruptedException;
}
