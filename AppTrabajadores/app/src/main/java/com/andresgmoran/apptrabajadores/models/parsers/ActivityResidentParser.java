package com.andresgmoran.apptrabajadores.models.parsers;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.ActivityResident;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivityResidentParser {

    public static List<ActivityResident> parseActivityResidents(String jsonText) throws ParserException{
        List<ActivityResident> activityResidents = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonText);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonResident = jsonArray.getJSONObject(i);

                Long id = jsonResident.getLong("id");
                Long activityId = jsonResident.getLong("idEvento");
                Long residentId = jsonResident.getLong("idResidente");
                boolean humanHelp = jsonResident.getBoolean("recursosHumanos");
                boolean materialHelp = jsonResident.getBoolean("recursosMateriales");
                boolean assistance = jsonResident.getBoolean("asistenciaPermitida");
                String preOpinion = jsonResident.optString("preOpinion", "");
                String postOpinion = jsonResident.optString("postOpinion", "");

                ActivityResident activityResident = new ActivityResident(id, activityId, residentId, assistance, humanHelp, materialHelp, preOpinion, postOpinion);

                activityResidents.add(activityResident);
            }

        } catch (JSONException | IllegalArgumentException e) {
            throw new ParserException("Error al parsear residentes de actividades", e);
        }

        return activityResidents;
    }
}
