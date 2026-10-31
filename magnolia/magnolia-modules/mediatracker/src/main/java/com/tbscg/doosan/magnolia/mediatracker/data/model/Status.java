package com.tbscg.doosan.magnolia.mediatracker.data.model;

public enum Status {
    APPROVED("Published"),
    DRAFT("Draft"),
    ARCHIVED("Archived");

    private String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Status findIgnoreCase(String statusName) {
        for (Status s : Status.values()) {
            if (s.name().equalsIgnoreCase(statusName)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getLabel();
    }

}
