package com.SchedularApp.services;

import com.SchedularApp.Entities.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduledDateService {
    public List<TimeSlot> getWeekDates();
    public boolean saveDate(LocalDate date, LocalTime time);
}
