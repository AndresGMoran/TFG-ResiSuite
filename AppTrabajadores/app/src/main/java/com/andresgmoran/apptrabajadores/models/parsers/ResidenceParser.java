package com.andresgmoran.apptrabajadores.models.parsers;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.Residence;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ResidenceParser {

    public static Residence parseResidence(String jsonText) throws ParserException {
        try {
            JSONObject obj = new JSONObject(jsonText);

            Long id = obj.getLong("id");
            String nombre = obj.getString("nombre");
            String email = obj.getString("email");

            List<Long> usuarios = new ArrayList<>();
            if (obj.has("usuarios") && !obj.isNull("usuarios")) {
                JSONArray usuariosArray = obj.getJSONArray("usuarios");
                for (int i = 0; i < usuariosArray.length(); i++) {
                    usuarios.add(usuariosArray.getLong(i));
                }
            }

            List<Long> residentes = new ArrayList<>();
            if (obj.has("residentes") && !obj.isNull("residentes")) {
                JSONArray residentesArray = obj.getJSONArray("residentes");
                for (int i = 0; i < residentesArray.length(); i++) {
                    residentes.add(residentesArray.getLong(i));
                }
            }

            return new Residence(id, nombre, email, usuarios, residentes);

        } catch (JSONException e) {
            throw new ParserException("Error al parsear residencia: " + e.getMessage(), e);
        }
    }

    public static List<Residence> parseResidences(String jsonText) throws ParserException {
        try {
            JSONArray array = new JSONArray(jsonText);
            List<Residence> residenceList = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                Long id = obj.getLong("id");
                String nombre = obj.getString("nombre");
                String email = obj.getString("email");

                List<Long> usuarios = new ArrayList<>();
                if (obj.has("usuarios") && !obj.isNull("usuarios")) {
                    JSONArray usuariosArray = obj.getJSONArray("usuarios");
                    for (int j = 0; j < usuariosArray.length(); j++) {
                        usuarios.add(usuariosArray.getLong(j));
                    }
                }

                List<Long> residentes = new ArrayList<>();
                if (obj.has("residentes") && !obj.isNull("residentes")) {
                    JSONArray residentesArray = obj.getJSONArray("residentes");
                    for (int j = 0; j < residentesArray.length(); j++) {
                        residentes.add(residentesArray.getLong(j));
                    }
                }

                residenceList.add(new Residence(id, nombre, email, usuarios, residentes));
            }

            return residenceList;

        } catch (JSONException e) {
            throw new ParserException("Error al parsear lista de residencias: " + e.getMessage(), e);
        }
    }

}
