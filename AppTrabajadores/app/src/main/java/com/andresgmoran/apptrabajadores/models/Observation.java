package com.andresgmoran.apptrabajadores.models;

import java.util.Objects;

public class Observation {
    private final User observationOwner;
    private final String observationText;

    public Observation(User observationOwner, String observationText) {
        this.observationOwner = observationOwner;
        this.observationText = observationText;
    }

    public User getObservationOwner() {
        return observationOwner;
    }

    public String getObservationText() {
        return observationText;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Observation that = (Observation) o;
        return Objects.equals(observationOwner, that.observationOwner) && Objects.equals(observationText, that.observationText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(observationOwner, observationText);
    }
}
