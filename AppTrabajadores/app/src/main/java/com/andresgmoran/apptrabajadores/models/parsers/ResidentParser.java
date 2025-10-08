package com.andresgmoran.apptrabajadores.models.parsers;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.Resident;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResidentParser {

    public static List<Resident> parseResidents(String jsonText) throws ParserException {
        List<Resident> residents = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(jsonText);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                Long id = obj.getLong("id");
                String name = obj.getString("nombre");
                String surnames = obj.getString("apellido");

                // Convertir string a LocalDate (formato esperado: "2023-05-10")
                String birthDateStr = obj.getString("fechaNacimiento");
                LocalDate birthDate = LocalDate.parse(birthDateStr); // Usa formato ISO (yyyy-MM-dd)

                String identityCard = obj.getString("documentoIdentidad");

                String family1 = obj.optString("familiar1", null);
                String family2 = obj.optString("familiar2", null);

                Long residenceId = obj.getLong("idResidencia");

                boolean isTakenDown = obj.optBoolean("baja", false);

                residents.add(new Resident(id, name, surnames, birthDate, identityCard, family1, family2, residenceId, isTakenDown));
            }
        } catch (JSONException | IllegalArgumentException e) {
            throw new ParserException("Error al parsear residentes", e);
        }

        return residents;
    }
}
