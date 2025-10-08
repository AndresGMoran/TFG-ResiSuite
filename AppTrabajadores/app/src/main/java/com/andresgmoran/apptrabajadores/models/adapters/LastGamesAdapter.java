package com.andresgmoran.apptrabajadores.models.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnGameStatsListener;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LastGamesAdapter extends RecyclerView.Adapter<LastGamesAdapter.LastGamesViewHolder> {

    private List<GameStat> gameStats;
    private List<Game> games;
    private List<Resident> residents;
    private final IOClickOnGameStatsListener listener;
    private Runnable runnable;

    // Constructor para varios residentes
    public LastGamesAdapter(List<GameStat> gameStats, List<Game> games, List<Resident> residents, IOClickOnGameStatsListener listener, Runnable refresh) {
        this.gameStats = gameStats;
        this.games = games;
        this.residents = residents;
        this.listener = listener;
        this.runnable = refresh;

        this.gameStats.sort(Comparator.comparing(GameStat::getDateTime).reversed());
    }

    // Constructor para un solo un residente
    public LastGamesAdapter(List<GameStat> gameStats, List<Game> games, Resident resident, IOClickOnGameStatsListener listener, Runnable refresh) {
        this(gameStats, games, List.of(resident), listener, refresh);
    }

    // Constructor para un solo juego
    public LastGamesAdapter(List<GameStat> gameStats, Game game, List<Resident> residents, IOClickOnGameStatsListener listener, Runnable refresh) {
        this(gameStats, List.of(game), residents, listener, refresh);
    }

    public void updateData(List<GameStat> newGameStats, List<Game> newGames, List<Resident> newResidents) {
        this.gameStats = newGameStats;
        this.games = newGames;
        this.residents = newResidents;
        notifyDataSetChanged();
    }
    public void updateData(List<GameStat> newGameStats, List<Game> newGames, Resident newResident) {
        this.gameStats = newGameStats;
        this.games = newGames;
        this.residents = List.of(newResident);
        notifyDataSetChanged();
    }
    public void updateData(List<GameStat> newGameStats, Game newGame, List<Resident> newResidents) {
        this.gameStats = newGameStats;
        this.games = List.of(newGame);
        this.residents = newResidents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LastGamesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_latest_game, parent, false);
        return new LastGamesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LastGamesViewHolder holder, int position) {
        GameStat gameStat = gameStats.get(position);

        Resident gameStatResident = null;
        for (Resident resident : residents) {
            if (resident.getId().equals(gameStat.getResidentId())) {
                gameStatResident = resident;
                break;
            }
        }

        Game gameStatGame = null;
        for (Game game : games) {
            if (game.getId().equals(gameStat.getGameId())) {
                gameStatGame = game;
                break;
            }
        }

        holder.bindObservation(gameStat, gameStatResident, gameStatGame);


        Resident finalGameStatResident = gameStatResident;
        Game finalGameStatGame = gameStatGame;

        holder.optionsButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    if (finalGameStatResident != null && finalGameStatGame != null)
                        listener.onDeleteGameStat(gameStat, finalGameStatGame, () -> {
                            runnable.run();
                        });
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        holder.itemView.setOnClickListener(v -> {
            if (finalGameStatResident != null && finalGameStatGame != null)
                listener.onClickOnLatestGame(gameStat, finalGameStatResident, finalGameStatGame);
        });
    }

    @Override
    public int getItemCount() {
        return gameStats.size();
    }

    public class LastGamesViewHolder extends RecyclerView.ViewHolder {

        private final TextView residentName;
        private final TextView gameName;
        private final TextView date;
        private final TextView time;
        private final ImageButton optionsButton;

        public LastGamesViewHolder(View view) {
            super(view);
            residentName = view.findViewById(R.id.name_text_latestGameItem);
            gameName = view.findViewById(R.id.gameName_text_latestGameItem);
            date = view.findViewById(R.id.date_text_latestGameItem);
            time = view.findViewById(R.id.time_text_latestGameItem);
            optionsButton = view.findViewById(R.id.more_options_latestGameItem);
        }

        public void bindObservation(GameStat gameStat, Resident gameStatResident, Game gameStatGame) {

            if (gameStat == null) {
                residentName.setText("Residente no encontrado");
                gameName.setText("Juego desconocido");
                date.setText("-");
                time.setText("-");
                return;
            }

            if (gameStatResident == null) {
                residentName.setText("Residente no encontrado");
            } else {
                residentName.setText(gameStatResident.getName() + " " + gameStatResident.getSurnames());
            }

            if (gameStatGame == null) {
                gameName.setText("Juego desconocido");
            } else {
                String name = gameStatGame.getName();
                String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);
                gameName.setText(formattedName);
            }

            Locale currentLocale = itemView.getContext().getResources().getConfiguration().getLocales().get(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", currentLocale);
            String fechaFormateada = gameStat.getDateTime().format(formatter);
            fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);
            date.setText(fechaFormateada);

            DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm", currentLocale);
            String horaFormateada = gameStat.getDateTime().format(formatterHora).toLowerCase();
            time.setText(horaFormateada);
        }
    }
}

