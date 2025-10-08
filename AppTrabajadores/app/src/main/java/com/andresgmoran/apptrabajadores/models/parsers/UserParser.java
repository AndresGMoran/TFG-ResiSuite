package com.andresgmoran.apptrabajadores.models.parsers;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserParser {

    public static List<User> parseUsers(String jsonText) throws ParserException {
        List<User> usuarios = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(jsonText);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                Long id = obj.getLong("id");
                String name = obj.getString("nombre");
                String surnames = obj.getString("apellido");
                String email = obj.getString("email");
                boolean enabled = obj.getBoolean("enabled");
                Long residentId = obj.has("idResidencia") && !obj.isNull("idResidencia") ? obj.getLong("idResidencia") : null;
                String accountImage = obj.getString("fotoPerfil");
                boolean takenOut = obj.getBoolean("baja");

                usuarios.add(new User(id, name, surnames, email, enabled, residentId, accountImage, takenOut));
            }
        } catch (JSONException | IllegalArgumentException e) {
            throw new ParserException("Error al parsear usuarios", e);
        }

        return usuarios;
    }

    public static User parseUser(String jsonText) throws ParserException {
        try {
            JSONObject obj = new JSONObject(jsonText);

            Long id = obj.getLong("id");
            String name = obj.getString("nombre");
            String surnames = obj.getString("apellido");
            String email = obj.getString("email");
            boolean enabled = obj.getBoolean("enabled");
            Long residentId = obj.has("residentId") && !obj.isNull("residentId") ? obj.getLong("residentId") : null;
            String accountImage = obj.getString("fotoPerfil");
            boolean takenOut = obj.getBoolean("baja");

            return new User(id, name, surnames, email, enabled, residentId, accountImage, takenOut);
        } catch (JSONException | IllegalArgumentException e) {
            throw new ParserException("Error al parsear el usuario", e);
        }
    }


}
