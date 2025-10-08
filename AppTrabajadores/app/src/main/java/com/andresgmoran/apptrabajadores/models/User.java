package com.andresgmoran.apptrabajadores.models;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private final Long id;
    private final String name;
    private final String surnames;
    private final String email;
    private final boolean enabled;
    private final Long residenceId;
    private final String accountImage;
    private final boolean takenOut;

    public User(Long id, String name, String surnames, String email, boolean enabled, Long residenceId, String accountImage, boolean takenOut) {
        this.id = id;
        this.name = name;
        this.surnames = surnames;
        this.email = email;
        this.enabled = enabled;
        this.residenceId = residenceId;
        this.accountImage = accountImage;
        this.takenOut = takenOut;


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

    public String getEmail() {
        return email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Long getResidenceId() {
        return residenceId;
    }

    public String getAccountImage() {
        return accountImage;
    }

    public boolean isTakenOut() {
        return takenOut;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return enabled == user.enabled && takenOut == user.takenOut && Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(surnames, user.surnames) && Objects.equals(email, user.email) && Objects.equals(residenceId, user.residenceId) && Objects.equals(accountImage, user.accountImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surnames, email, enabled, residenceId, accountImage, takenOut);
    }
}
