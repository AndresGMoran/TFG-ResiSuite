package com.andresgmoran.apptrabajadores.ui.fragments.game;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnGameStatsListener;
import com.andresgmoran.apptrabajadores.interfaces.IOnClickOnBackButtonListener;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.adapters.LastGamesAdapter;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;
import com.andresgmoran.apptrabajadores.ui.MainActivity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GameFragment extends Fragment {
    private List<GameStat> gameStats = AppDataRepository.getInstance().getGameStats();
    private List<Resident> residents = AppDataRepository.getInstance().getResidents();
    private Game game;

    public interface IOnRefreshGameListener {
        void onRefreshGameStats(Runnable refresh);
    }
    private IOnRefreshGameListener refreshGameListener;

    private IOnClickOnBackButtonListener backButtonListener;

    private TextView gameNameTextView;
    private ImageView gameImageView;
    private TextView numberOfGamesLastWeekTextView;
    private TextView totalGamesPlayedTextView;
    private TextView topPlayerTextView;
    private ImageButton backButton;

    private LastGamesAdapter lastGamesAdapter;
    private RecyclerView recyclerView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate( R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#0062FF"));

        gameNameTextView = view.findViewById(R.id.banner_name_game);
        gameImageView = view.findViewById(R.id.game_image);
        numberOfGamesLastWeekTextView = view.findViewById(R.id.tv_number_of_games_last_week);
        totalGamesPlayedTextView = view.findViewById(R.id.tv_total_games_played);
        topPlayerTextView = view.findViewById(R.id.tv_top_jugador);

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_game);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshGameListener.onRefreshGameStats(this::refreshData);
            swipeRefreshLayout.setRefreshing(false);
        });

        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            backButtonListener.onClickOnBackButton();
        });

        List<GameStat> allGameStats = new ArrayList<>();
        for (GameStat gs : gameStats){
            if (gs.getGameId() == game.getId()){
                allGameStats.add(gs);
            }
        }

        String name = game.getName();
        String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);
        gameNameTextView.setText(formattedName);

        String resourceName = "logo_" + game.getName().toLowerCase().replace(" ", "_");
        int imageResId = getContext().getResources().getIdentifier(resourceName, "drawable", getContext().getPackageName());
        if (imageResId != 0)
            gameImageView.setImageResource(imageResId);

        if (allGameStats.isEmpty()) {
            numberOfGamesLastWeekTextView.setText("-");
            totalGamesPlayedTextView.setText("-");
            topPlayerTextView.setText("-");
            view.findViewById(R.id.rv_game_stats).setVisibility( View.GONE );
            view.findViewById(R.id.tv_lastgames_empty_game).setVisibility( View.VISIBLE );
            return;
        }

        int numberOfGamesLastWeek = getGamesLastWeek(allGameStats);
        numberOfGamesLastWeekTextView.setText(String.valueOf(numberOfGamesLastWeek));
        totalGamesPlayedTextView.setText(String.valueOf(allGameStats.size()));
        Resident topResident = getTopResident(allGameStats, residents);
        topPlayerTextView.setText(topResident.getName() + " " + topResident.getSurnames());

        lastGamesAdapter = new LastGamesAdapter(allGameStats, game, residents, (IOClickOnGameStatsListener) requireActivity(), () -> {
            refreshData();
        });
        recyclerView = view.findViewById(R.id.rv_game_stats);
        recyclerView.setAdapter( lastGamesAdapter );
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    private void refreshData() {
        gameStats = AppDataRepository.getInstance().getGameStats();
        residents = AppDataRepository.getInstance().getResidents();

        List<GameStat> allGameStats = new ArrayList<>();
        for (GameStat gs : gameStats) {
            if (gs.getGameId() == game.getId()) {
                allGameStats.add(gs);
            }
        }

        if (allGameStats.isEmpty()) {
            numberOfGamesLastWeekTextView.setText("-");
            totalGamesPlayedTextView.setText("-");
            topPlayerTextView.setText("-");
            return;
        }

        lastGamesAdapter.updateData(allGameStats, game, residents);


        int numberOfGamesLastWeek = getGamesLastWeek(allGameStats);
        numberOfGamesLastWeekTextView.setText(String.valueOf(numberOfGamesLastWeek));
        totalGamesPlayedTextView.setText(String.valueOf(allGameStats.size()));
        Resident topResident = getTopResident(allGameStats, residents);
        topPlayerTextView.setText(topResident.getName());
    }

    private int getGamesLastWeek(List<GameStat> allGameStats) {
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusDays(7);

        for (GameStat gs : allGameStats) {
            if (gs.getDateTime().isAfter(oneWeekAgo)) {
                count++;
            }
        }

        return count;
    }


    private Resident getTopResident(List<GameStat> allGameStats, List<Resident> residents) {
        Resident topResident = null;
        int maxGames = -1;

        for (Resident resident : residents) {
            int gamesCount = 0;

            for (GameStat gs : allGameStats) {
                if (gs.getResidentId() == resident.getId()) {
                    gamesCount++;
                }
            }

            if (gamesCount > maxGames) {
                maxGames = gamesCount;
                topResident = resident;
            }
        }

        return topResident;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(getArguments() != null) {
            game = (Game) getArguments().getSerializable("game");

        }
        refreshGameListener = (IOnRefreshGameListener) context;
        backButtonListener = (IOnClickOnBackButtonListener) context;
    }
}

