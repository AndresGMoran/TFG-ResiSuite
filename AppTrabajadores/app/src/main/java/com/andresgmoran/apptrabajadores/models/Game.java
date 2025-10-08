package com.andresgmoran.apptrabajadores.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Game implements Serializable {
    private final Long id;
    private final String name;

    public Game(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id) && Objects.equals(name, game.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}