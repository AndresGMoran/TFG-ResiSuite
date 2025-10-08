package com.andresgmoran.apptrabajadores.ui.fragments.gameDetail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOnClickOnBackButtonListener;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.User;
import com.andresgmoran.apptrabajadores.models.gameStats.Difficulty;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;
import com.andresgmoran.apptrabajadores.ui.MainActivity;

import java.util.List;

public class GameDetailFragment extends Fragment {

    public interface IOnAddObservationListener {
        void onAddObservation(String observation, long gameId, long gameStatId, Runnable refresh);
    }
    public interface IOnRefreshGameStatsListener {
        void onRefreshGameStats(Runnable runnable);
    }
    private IOnRefreshGameStatsListener refreshGameStatsListener;
    private IOnAddObservationListener addObservationListener;
    private IOnClickOnBackButtonListener backButtonListener;

    private GameStat gameStat;
    private Resident gameStatResident;
    private Game gameStatGame;
    private User actualUser = AppDataRepository.getInstance().getActualUser();
    private Bitmap actualUserImage = AppDataRepository.getInstance().getActualUserImage();
    private List<User> users = AppDataRepository.getInstance().getUsers();

    private TextView residentNameTextView;
    private TextView gameNameTextView;
    private TextView durationTextView;
    private TextView levelTextView;
    private TextView failsTitle;
    private TextView failsTextView;
    private ImageButton backButton;
    private TextView observationText;
    private EditText observationEditText;
    private Button addObservationButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate( R.layout.fragment_game_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#0062FF"));

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_game_detail);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshGameStatsListener.onRefreshGameStats(this::updateData);
            swipeRefreshLayout.setRefreshing(false);
        });

        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            backButtonListener.onClickOnBackButton();
        });

        residentNameTextView = view.findViewById(R.id.banner_name_resident);
        gameNameTextView = view.findViewById(R.id.game_stats_detail_game_name);
        durationTextView = view.findViewById(R.id.tv_duration);
        levelTextView = view.findViewById(R.id.tv_level);
        failsTitle = view.findViewById(R.id.label_fails);
        failsTextView = view.findViewById(R.id.tv_fails);

        residentNameTextView.setText(gameStatResident.getName() + " " + gameStatResident.getSurnames());
        String name = gameStatGame.getName();
        String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);
        gameNameTextView.setText(formattedName);
        if (gameStat.getDifficulty().equals(Difficulty.DIFICULTAD1))
            levelTextView.setText(R.string.level1_text);
        else if (gameStat.getDifficulty().equals(Difficulty.DIFICULTAD2))
            levelTextView.setText(R.string.level2_text);
        else if (gameStat.getDifficulty().equals(Difficulty.DIFICULTAD3))
            levelTextView.setText(R.string.level3_text);
        durationTextView.setText(Math.round(gameStat.getDuration()) + " seg");

        if (gameStatGame.getName().equalsIgnoreCase("bingo auditivo") || gameStatGame.getName().equalsIgnoreCase("flecha y reacciona")) {
            failsTitle.setText(getContext().getString(R.string.result_text));
            if (gameStat.getNum() == 0) {
                failsTextView.setText(getContext().getString(R.string.lost_text));
            } else {
                failsTextView.setText(getContext().getString(R.string.won_text));
            }
        } else if (gameStatGame.getName().equalsIgnoreCase("gimnasio")){
            failsTitle.setVisibility( View.GONE);
            failsTextView.setVisibility( View.GONE);
        } else {
            failsTitle.setText(getContext().getString(R.string.fails_text));
            failsTextView.setText(String.valueOf(gameStat.getNum()));
        }


        View observation = view.findViewById(R.id.observation);
        ImageView observationUserImage = observation.findViewById(R.id.user_image_observation);
        observationUserImage.setImageBitmap(actualUserImage);
        TextView observationUserName = observation.findViewById(R.id.observation_user_name);
        observationText = observation.findViewById(R.id.observation_text);

        observationEditText = observation.findViewById(R.id.observation_input);
        addObservationButton = observation.findViewById(R.id.add_observation_button);

        for (User user : users) {
            if (user.getId() == gameStat.getUserId()) {
                observationUserName.setText(actualUser.getName() + " " + actualUser.getSurnames());
                break;
            }
        }
        if (gameStat.getObservation().isEmpty()) {
            if (gameStat.getUserId() == actualUser.getId()) {
                observationEditText.setVisibility(View.VISIBLE);
                addObservationButton.setVisibility(View.VISIBLE);

                addObservationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addObservationListener.onAddObservation(observationEditText.getText().toString(), gameStatGame.getId(), gameStat.getId(),GameDetailFragment.this::updateData);
                    }
                });
            } else {
                observationText.setText(getContext().getString(R.string.no_observation_text));
            }
        } else {
            observationText.setVisibility(View.VISIBLE);
            observationText.setText(gameStat.getObservation());
        }

    }

    private void updateData() {
        List<GameStat> gameStats = AppDataRepository.getInstance().getGameStats();
        for (GameStat stat : gameStats) {
            if (stat.getId().equals(gameStat.getId())) {
                gameStat = stat;
                break;
            }
        }

        List<Resident> residents = AppDataRepository.getInstance().getResidents();
        for (Resident resident : residents) {
            if (resident.getId().equals(gameStat.getResidentId())) {
                gameStatResident = resident;
                break;
            }
        }
        List<Game> games = AppDataRepository.getInstance().getGames();
        for (Game game : games) {
            if (game.getId().equals(gameStat.getGameId())) {
                gameStatGame = game;
                break;
            }
        }

        if (!gameStat.getObservation().isEmpty()){
            observationEditText.setVisibility( View.GONE);
            addObservationButton.setVisibility( View.GONE);
            observationText.setVisibility(View.VISIBLE);
            observationText.setText(gameStat.getObservation());
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        refreshGameStatsListener = (IOnRefreshGameStatsListener) context;
        addObservationListener = (IOnAddObservationListener) context;
        backButtonListener = (IOnClickOnBackButtonListener) context;

        if(getArguments() != null) {
            gameStat = (GameStat) getArguments().getSerializable("gameStat");
            gameStatResident = (Resident) getArguments().getSerializable("gameStatResident");
            gameStatGame = (Game) getArguments().getSerializable("gameStatGame");
        }
    }
}
