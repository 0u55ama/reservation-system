package com.SchedularApp.Entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private LocalTime time;
    private boolean available;  // Changed to boolean

    @ManyToOne
    @JoinColumn(name = "table_id")
    private TableEntity table;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Constructors, Getters, Setters
    public TimeSlot() {}

    public TimeSlot(LocalDate date, LocalTime time, TableEntity table, Customer customer, boolean available) {
        this.date = date;
        this.time = time;
        this.table = table;
        this.available = available;
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public TableEntity getTable() {
        return table;
    }

    public void setTable(TableEntity table) {
        this.table = table;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}