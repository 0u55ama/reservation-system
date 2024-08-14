package com.SchedularApp.Repositories;

import com.SchedularApp.Entities.TableEntity;
import com.SchedularApp.Entities.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    Optional<TimeSlot> findByTableAndDateAndTime(TableEntity table, LocalDate date, LocalTime time);

    List<TimeSlot> findByTable(TableEntity table);

    List<TimeSlot> findByTableAndDate(TableEntity tableEntity, LocalDate date);
}
