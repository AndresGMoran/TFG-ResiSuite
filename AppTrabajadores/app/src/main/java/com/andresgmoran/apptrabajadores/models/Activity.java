package com.andresgmoran.apptrabajadores.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Activity implements Serializable {
    private final Long id;
    private final String name;
    private final String description;
    private final LocalDateTime date;
    private final ActivityState state;
    private final List<Long> residentIds;
    private final Long residenceId;

    public Activity(Long id, String name, String description, LocalDateTime date, ActivityState state, List<Long> residentIds, Long residenceId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.state = state;
        this.residentIds = residentIds;
        this.residenceId = residenceId;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public ActivityState getState() {
        return state;
    }
    public List<Long> getResidentIds() {
        return residentIds;
    }
    public Long getResidenceId() {
        return residenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Activity activity = (Activity) o;
        return Objects.equals(id, activity.id) && Objects.equals(name, activity.name) && Objects.equals(description, activity.description) && Objects.equals(date, activity.date) && state == activity.state && Objects.equals(residentIds, activity.residentIds) && Objects.equals(residenceId, activity.residenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, date, state, residentIds, residenceId);
    }
}
