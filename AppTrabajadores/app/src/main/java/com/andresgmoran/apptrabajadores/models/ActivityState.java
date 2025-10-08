package com.andresgmoran.apptrabajadores.models;

import com.andresgmoran.apptrabajadores.models.gameStats.Difficulty;

public enum ActivityState {
    ABIERTO,
    CERRADO,
    EN_CURSO,
    FINALIZADA;

    public static ActivityState fromString(String value) {
        switch (value.toLowerCase()) {
            case "abierto":
                return ABIERTO;
            case "cerrado":
                return CERRADO;
            case "en_curso":
                return EN_CURSO;
            case "finalizada":
                return FINALIZADA;
            default:
                throw new IllegalArgumentException("Valor de estado no válido: " + value);
        }
    }
}
