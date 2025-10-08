package com.andresgmoran.apptrabajadores.models.parsers;

import com.andresgmoran.apptrabajadores.exceptions.ParserException;
import com.andresgmoran.apptrabajadores.models.Game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameParser {

    public static List<Game> parseGames(String jsonText) throws ParserException{
        List<Game> games = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonText);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonGame = jsonArray.getJSONObject(i);

                Long id = jsonGame.getLong("id");
                String name = jsonGame.getString("nombre");

                Game game = new Game(id, name);
                games.add(game);
            }

        } catch (JSONException | IllegalArgumentException e) {
            throw new ParserException("Error al parsear juegos", e);
        }

        return games;
    }
}
