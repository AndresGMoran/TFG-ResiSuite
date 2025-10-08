package com.andresgmoran.apptrabajadores.models.parsers;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.ActivityState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ActivityParser {

    public static List<Activity> parseActivities(String jsonText) throws ParserException {
        List<Activity> activities = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(jsonText);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                Long id = obj.getLong("id");
                String name = obj.getString("nombre");
                String description = obj.getString("descripcion");

                // Asegúrate de que la fecha tenga formato "yyyy-MM-dd"
                String fechaStr = obj.getString("fechaInicio").trim();
                LocalDateTime date = LocalDateTime.parse(fechaStr);

                // Validar estado
                ActivityState state = null;
                if (obj.has("estado") && !obj.isNull("estado")) {
                    String estadoStr = obj.getString("estado").trim();
                    if (!estadoStr.isEmpty()) {
                        state = ActivityState.fromString(estadoStr);
                    }
                }

                Long residenceId = obj.getLong("idResidencia");

                JSONArray jsonResidentsArray = obj.getJSONArray("participantes");
                List<Long> residentIds = new ArrayList<>();

                for (int j = 0; j < jsonResidentsArray.length(); j++) {
                    residentIds.add(jsonResidentsArray.getLong(j));
                }

                activities.add(new Activity(id, name, description, date, state, residentIds, residenceId));
            }

        } catch (JSONException | IllegalArgumentException e) {
            throw new ParserException("Error al parsear actividades: " + e.getMessage(), e);
        }

        return activities;
    }
}
