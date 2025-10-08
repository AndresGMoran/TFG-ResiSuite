package com.andresgmoran.apptrabajadores.models;

import java.util.List;
import java.util.Objects;

public class Residence {
    private final Long id;
    private final String name;
    private final String email;
    private final List<Long> users;
    private final List<Long> residents;

    public Residence(Long id, String name, String email, List<Long> users, List<Long> residents) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.users = users;
        this.residents = residents;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Long> getUsers() {
        return users;
    }

    public List<Long> getResidents() {
        return residents;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Residence residence = (Residence) o;
        return Objects.equals(id, residence.id) && Objects.equals(name, residence.name) && Objects.equals(email, residence.email) && Objects.equals(users, residence.users) && Objects.equals(residents, residence.residents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, users, residents);
    }
}
