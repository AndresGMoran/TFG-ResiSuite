package com.andresgmoran.apptrabajadores.ui.fragments.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnGameListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnGameStatsListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnResidentListener;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.User;
import com.andresgmoran.apptrabajadores.models.adapters.GamesAdapter;
import com.andresgmoran.apptrabajadores.models.adapters.LastGamesAdapter;
import com.andresgmoran.apptrabajadores.models.adapters.ResidentsAdapter;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private List<Resident> residents;
    private List<GameStat> gameStats;
    private List<Game> games;
    private User user;
    private Bitmap userImageBitmap;

    private TextView userNameTextView;
    private ImageView userImage;
    private RecyclerView recyclerViewLastGames;
    private RecyclerView recyclerViewGames;
    private RecyclerView recyclerViewResidents;
    private TextView emptyLatestGamesText;
    private TextView emptyGamesText;
    private TextView emptyResidentsText;

    private GamesAdapter gamesAdapter;
    private Map<Long, Double> weeklyPercentages;

    private ResidentsAdapter residentsAdapter;
    private LastGamesAdapter lastGamesAdapter;

    public interface IOnRefreshHomeListener {
        void onRefreshHome(Runnable refresh);
    }
    private IOnRefreshHomeListener refreshListener;

    public interface IOnClickOnAddParticipantListener {
        void onClickOnAddParticipant();
    }
    private IOnClickOnAddParticipantListener addParticipantListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#CCCCCC"));

        setupSwipeRefresh(view);
        setupUserInfo(view);
        setupLastGamesList(view);
        setupGamesList(view);
        setupResidentsList(view);
        setupFilterButtons(view);
        setupFab(view);
    }

    public void refreshData() {
        residents = AppDataRepository.getInstance().getResidents();
        gameStats = AppDataRepository.getInstance().getGameStats();
        games = AppDataRepository.getInstance().getGames();
        user = AppDataRepository.getInstance().getActualUser();
        userImageBitmap = AppDataRepository.getInstance().getActualUserImage();

        userNameTextView.setText(user.getName() + " " + user.getSurnames());
        userImage.setImageBitmap(userImageBitmap);

        lastGamesAdapter.updateData(gameStats, games, residents);
        gamesAdapter.updateData(games);
        residentsAdapter.updateData(residents);
    }

    private void setupSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_home);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshListener.onRefreshHome( () -> {
                refreshData();
            });
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupUserInfo(View view) {
        userNameTextView = view.findViewById(R.id.home_user_name);
        userImage = view.findViewById(R.id.user_image_home);
        userNameTextView.setText(user.getName() + " " + user.getSurnames());
        userImage.setImageBitmap(userImageBitmap);
    }

    private void setupLastGamesList(View view) {
        emptyLatestGamesText = view.findViewById(R.id.tv_lastgames_empty_home);
        recyclerViewLastGames = view.findViewById(R.id.latestGames_recycleView_home);

        lastGamesAdapter = new LastGamesAdapter(gameStats, games, residents, (IOClickOnGameStatsListener) requireActivity(), this::refreshData);
        recyclerViewLastGames.setAdapter(lastGamesAdapter);
        recyclerViewLastGames.setHasFixedSize(true);
        recyclerViewLastGames.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        if (gameStats.isEmpty()) {
            recyclerViewLastGames.setVisibility(View.GONE);
            emptyLatestGamesText.setVisibility(View.VISIBLE);
        } else {
            recyclerViewLastGames.setVisibility(View.VISIBLE);
            emptyLatestGamesText.setVisibility(View.GONE);
        }
    }

    private void setupGamesList(View view) {
        emptyGamesText = view.findViewById(R.id.tv_games_empty_home);
        recyclerViewGames = view.findViewById(R.id.games_recycleView_home);

        weeklyPercentages = calculateWeeklyGamePercentages();

        gamesAdapter = new GamesAdapter(games, (IOClickOnGameListener) requireActivity(), weeklyPercentages);
        recyclerViewGames.setAdapter(gamesAdapter);
        recyclerViewGames.setHasFixedSize(true);
        recyclerViewGames.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (games.isEmpty()) {
            recyclerViewGames.setVisibility(View.GONE);
            emptyGamesText.setVisibility(View.VISIBLE);
        } else {
            recyclerViewGames.setVisibility(View.VISIBLE);
            emptyGamesText.setVisibility(View.GONE);
        }
    }

    private Map<Long, Double> calculateWeeklyGamePercentages() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        Map<Long, Long> countMap = new HashMap<>();

        for (Game game : games) {
            long count = gameStats.stream()
                    .filter(stat -> stat.getGameId() == game.getId())
                    .filter(stat -> stat.getDateTime().isAfter(oneWeekAgo))
                    .count();
            countMap.put(game.getId(), count);
        }

        long total = countMap.values().stream().mapToLong(Long::longValue).sum();
        Map<Long, Double> percentageMap = new HashMap<>();

        for (Map.Entry<Long, Long> entry : countMap.entrySet()) {
            double percentage = total > 0 ? (entry.getValue() * 100.0) / total : 0;
            percentageMap.put(entry.getKey(), percentage);
        }

        return percentageMap;
    }

    private void setupResidentsList(View view) {
        emptyResidentsText = view.findViewById(R.id.tv_residents_empty_home);
        recyclerViewResidents = view.findViewById(R.id.residents_recycleView_home);

        residentsAdapter = new ResidentsAdapter(residents, (IOClickOnResidentListener) requireActivity(), this::refreshData );
        recyclerViewResidents.setAdapter(residentsAdapter);
        recyclerViewResidents.setHasFixedSize(true);
        recyclerViewResidents.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (residents.isEmpty()) {
            recyclerViewResidents.setVisibility(View.GONE);
            emptyResidentsText.setVisibility(View.VISIBLE);
        } else {
            recyclerViewResidents.setVisibility(View.VISIBLE);
            emptyResidentsText.setVisibility(View.GONE);
        }

        filterResidentsList("default");
    }

    private void setupFilterButtons(View view) {
        View filterGamesButton = view.findViewById(R.id.filter_games_list_button);
        View filterResidentsButton = view.findViewById(R.id.filter_residents_list_button);
        View filterLastGamesButton = view.findViewById(R.id.filter_lastgames_list_button);

        filterGamesButton.setOnClickListener(v -> showGamesFilterDialog());
        filterResidentsButton.setOnClickListener(v -> showResidentsFilterDialog());
        filterLastGamesButton.setOnClickListener(v -> showLastGamesFilterDialog());
    }

    private void setupFab(View view) {
        MaterialButton fab = view.findViewById(R.id.add_resident_button);

        fab.setOnClickListener(v ->
                addParticipantListener.onClickOnAddParticipant()
        );

        NestedScrollView scrollView = view.findViewById(R.id.nested_scroll_view_home);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            private int lastScrollY = 0;
            private boolean isButtonVisible = true;

            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > lastScrollY + 10 && isButtonVisible) {
                    fab.animate().translationY(fab.getHeight() + 50).alpha(0.0f).setDuration(200).withEndAction(() -> fab.setVisibility(View.GONE));
                    isButtonVisible = false;
                } else if (scrollY < lastScrollY - 10 && !isButtonVisible) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setAlpha(0f);
                    fab.setTranslationY(fab.getHeight() + 50);
                    fab.animate().translationY(0).alpha(1.0f).setDuration(200);
                    isButtonVisible = true;
                }
                lastScrollY = scrollY;
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        residents = AppDataRepository.getInstance().getResidents();
        gameStats = AppDataRepository.getInstance().getGameStats();
        games = AppDataRepository.getInstance().getGames();
        user = AppDataRepository.getInstance().getActualUser();
        userImageBitmap = AppDataRepository.getInstance().getActualUserImage();

        refreshListener = (IOnRefreshHomeListener) context;
        addParticipantListener = (IOnClickOnAddParticipantListener) context;
    }

    private void showGamesFilterDialog() {
        String[] options = {getString(R.string.game_name_a_z_filter_text), getString(R.string.game_name_z_a_filter_text), getString(R.string.most_played_filter_text)};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_games_title)
                .setItems(options, (dialog, which) -> filterGamesList(options[which]))
                .show();
    }

    private void showResidentsFilterDialog() {
        String[] options = {getString(R.string.resident_name_a_z_filter_text), getString(R.string.resident_name_z_a_filter_text), getString(R.string.date_of_birth_filter_text)};

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_residents_title)
                .setItems(options, (dialog, which) -> filterResidentsList(options[which]))
                .show();
    }

    private void showLastGamesFilterDialog() {
        String[] options = {
                getString(R.string.resident_name_a_z_filter_text),
                getString(R.string.resident_name_z_a_filter_text),
                getString(R.string.game_name_a_z_filter_text),
                getString(R.string.game_name_z_a_filter_text),
                getString(R.string.first_to_last_filter_text),
                getString(R.string.last_to_first_filter_text),
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString( R.string.filter_last_games_title))
                .setItems(options, (dialog, which) -> filterLastGamesList(options[which]))
                .show();
    }

    private void filterGamesList(String option) {
        List<Game> filteredList = new ArrayList<>(games);

        if (option.equals(getString(R.string.game_name_a_z_filter_text))) {
            filteredList.sort(Comparator.comparing(Game::getName));
        } else if (option.equals(getString(R.string.game_name_z_a_filter_text))) {
            filteredList.sort((g1, g2) -> g2.getName().compareTo(g1.getName()));
        } else if (option.equals(getString(R.string.most_played_filter_text))) {
            filteredList.sort((g1, g2) -> {
                Double p1 = weeklyPercentages.getOrDefault(g1.getId(), 0.0);
                Double p2 = weeklyPercentages.getOrDefault(g2.getId(), 0.0);
                return Double.compare(p2, p1);
            });
        }

        gamesAdapter.updateData(filteredList);
        recyclerViewGames.scrollToPosition(0);
    }

    private void filterResidentsList(String option) {
        List<Resident> filteredList = new ArrayList<>(residents);

        if (option.equals(getString(R.string.resident_name_a_z_filter_text))) {
            filteredList.sort(Comparator.comparing(Resident::getName));
        } else if (option.equals(getString(R.string.resident_name_z_a_filter_text))) {
            filteredList.sort((r1, r2) -> r2.getName().compareTo(r1.getName()));
        } else if (option.equals(getString(R.string.date_of_birth_filter_text))) {
            filteredList.sort(Comparator.comparing(Resident::getBirthDate));
        } else {
            filteredList.sort((r1, r2) -> Long.compare(r2.getId(), r1.getId()));
        }

        residentsAdapter.updateData(filteredList);
        recyclerViewResidents.scrollToPosition(0);
    }

    private void filterLastGamesList(String option) {
        List<GameStat> filteredList = new ArrayList<>(gameStats);

        if (option.equals(getString(R.string.resident_name_a_z_filter_text))) {
            filteredList.sort(Comparator.comparing(stat -> getResidentNameById(stat.getResidentId())));
        } else if (option.equals(getString(R.string.resident_name_z_a_filter_text))) {
            filteredList.sort((s1, s2) -> getResidentNameById(s2.getResidentId()).compareTo(getResidentNameById(s1.getResidentId())));
        } else if (option.equals(getString(R.string.game_name_a_z_filter_text))) {
            filteredList.sort(Comparator.comparing(stat -> getGameNameById(stat.getGameId())));
        } else if (option.equals(getString(R.string.game_name_z_a_filter_text))) {
            filteredList.sort((s1, s2) -> getGameNameById(s2.getGameId()).compareTo(getGameNameById(s1.getGameId())));
        } else if (option.equals(getString(R.string.first_to_last_filter_text))) {
            filteredList.sort(Comparator.comparing(GameStat::getDateTime));
        } else if (option.equals(getString(R.string.last_to_first_filter_text))) {
            filteredList.sort((s1, s2) -> s2.getDateTime().compareTo(s1.getDateTime()));
        } else {
            filteredList.sort((s1, s2) -> s2.getDateTime().compareTo(s1.getDateTime()));
        }

        lastGamesAdapter.updateData(filteredList, games, residents);
        recyclerViewLastGames.scrollToPosition(0);
    }

    private String getResidentNameById(Long id) {
        return residents.stream()
                .filter(r -> r.getId().equals(id))
                .map(Resident::getName)
                .findFirst()
                .orElse("");
    }

    private String getGameNameById(Long id) {
        return games.stream()
                .filter(g -> g.getId().equals(id))
                .map(Game::getName)
                .findFirst()
                .orElse("");
    }
}