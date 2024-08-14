package com.SchedularApp.Entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class TableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL)
    private List<TimeSlot> timeSlots;

    // Constructors, Getters, Setters
    public TableEntity() {}

    public TableEntity(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }
}