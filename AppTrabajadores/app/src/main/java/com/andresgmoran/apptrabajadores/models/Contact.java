package com.andresgmoran.apptrabajadores.models;

import java.util.ArrayList;
import java.util.Objects;

public class Contact {
    private final String name;
    private final String phone;
    private final String email;
    private final ArrayList<Resident> residents;

    public Contact(String name, String phone, String email, ArrayList<Resident> residents) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.residents = residents;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Resident> getResidents() {
        return residents;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(name, contact.name) && Objects.equals(phone, contact.phone) && Objects.equals(email, contact.email) && Objects.equals(residents, contact.residents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone, email, residents);
    }
}
