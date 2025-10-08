package com.andresgmoran.apptrabajadores.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

public class Resident implements Serializable {
    private final Long id;
    private final String name;
    private final String surnames;
    private final LocalDate birthDate;
    private final String identityCard;
    private final String family1;
    private final String family2;
    private final Long residenceId;
    private final boolean isTakenDown;

    public Resident(Long id, String name, String surnames, LocalDate birthDate, String identityCard, String family1, String family2, Long residenceId, boolean isTakenDown) {
        this.id = id;
        this.name = name;
        this.surnames = surnames;
        this.birthDate = birthDate;
        this.identityCard = identityCard;
        this.family1 = family1;
        this.family2 = family2;
        this.residenceId = residenceId;
        this.isTakenDown = isTakenDown;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurnames() {
        return surnames;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Resident resident = (Resident) o;
        return Objects.equals(id, resident.id) && Objects.equals(name, resident.name) && Objects.equals(surnames, resident.surnames) && Objects.equals(birthDate, resident.birthDate) && Objects.equals(residenceId, resident.residenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surnames, birthDate, residenceId);
    }
}
