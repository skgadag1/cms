package com.tbscg.doosan.magnolia.mediatracker.data.model;

import java.time.LocalDate;

public class DomainFilters extends MediatrackerFilters {

    private String name;

    private LocalDate startFrom;
    private LocalDate startTo;

    private LocalDate endFrom;
    private LocalDate endTo;

    private LocalDate createdFrom;
    private LocalDate createdTo;

    private LocalDate modifiedFrom;
    private LocalDate modifiedTo;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(LocalDate startFrom) {
        this.startFrom = startFrom;
    }

    public LocalDate getStartTo() {
        return startTo;
    }

    public void setStartTo(LocalDate startTo) {
        this.startTo = startTo;
    }

    public LocalDate getEndFrom() {
        return endFrom;
    }

    public void setEndFrom(LocalDate endFrom) {
        this.endFrom = endFrom;
    }

    public LocalDate getEndTo() {
        return endTo;
    }

    public void setEndTo(LocalDate endTo) {
        this.endTo = endTo;
    }

    public LocalDate getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(LocalDate createdFrom) {
        this.createdFrom = createdFrom;
    }

    public LocalDate getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(LocalDate createdTo) {
        this.createdTo = createdTo;
    }

    public LocalDate getModifiedFrom() {
        return modifiedFrom;
    }

    public void setModifiedFrom(LocalDate modifiedFrom) {
        this.modifiedFrom = modifiedFrom;
    }

    public LocalDate getModifiedTo() {
        return modifiedTo;
    }

    public void setModifiedTo(LocalDate modifiedTo) {
        this.modifiedTo = modifiedTo;
    }
}
