package com.andresgmoran.apptrabajadores.models.gameStats;

public enum Difficulty {
    DIFICULTAD1,
    DIFICULTAD2,
    DIFICULTAD3;

    public static Difficulty fromString(String value) {
        switch (value.toLowerCase()) {
            case "dificultad1":
                return DIFICULTAD1;
            case "dificultad2":
                return DIFICULTAD2;
            case "dificultad3":
                return DIFICULTAD3;
            default:
                throw new IllegalArgumentException("Valor de dificultad no válido: " + value);
        }
    }
}
