package com.andresgmoran.apptrabajadores.models.parsers;


import android.util.Log;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.gameStats.Difficulty;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GameStatParser {

    public static List<GameStat> parseStats(String jsonText) throws ParserException {
        List<GameStat> stats = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(jsonText);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                Long id = obj.getLong("id");
                Long residentId = obj.getLong("idResidente");
                Long gameId = obj.getLong("idJuego");
                Long userId = obj.getLong("idUsuario");
                Integer num = obj.getInt("num");
                Double duration = obj.getDouble("duracion");
                Difficulty difficulty = null;
                if (obj.has("dificultad") && !obj.isNull("dificultad")) {
                    String dificultadStr = obj.getString("dificultad").trim();
                    if (!dificultadStr.isEmpty()) {
                        difficulty = Difficulty.fromString(dificultadStr);
                    }
                }
                LocalDateTime dateTime = LocalDateTime.parse(
                        obj.getString("fecha"),
                        DateTimeFormatter.ISO_DATE_TIME // ejemplo: "2025-04-18T22:31:00"
                );
                String observation = obj.getString("observacion");

                stats.add(new GameStat(id, residentId, gameId, userId, num, duration, difficulty, dateTime, observation));
            }

        } catch (JSONException | IllegalArgumentException e) {
            throw new ParserException("Error al parsear partidas", e);
        }

        return stats;
    }
}
