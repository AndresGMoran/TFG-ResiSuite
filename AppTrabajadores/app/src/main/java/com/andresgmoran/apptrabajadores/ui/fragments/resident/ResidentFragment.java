package com.andresgmoran.apptrabajadores.ui.fragments.resident;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.andresgmoran.apptrabajadores.ui.graphics.CustomBarChartView;
import com.andresgmoran.apptrabajadores.ui.graphics.CustomPieChartView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResidentFragment extends Fragment {
    private Resident resident;
    private List<Game> games = AppDataRepository.getInstance().getGames();
    private List<GameStat> gameStats = AppDataRepository.getInstance().getGameStats();

    private ImageButton backButton;
    private TextView residentName;
    private CardView pieCard;
    private TextView tvLastGamesEmpty;
    private TextView tvStatsEmpty;

    private RecyclerView recyclerViewLastGames;
    private LastGamesAdapter lastGamesAdapter;
    private List<GameStat> filteredStats;

    public interface IOnRefreshResidentListener {
        void onRefreshGameStats(Runnable runnable);
    }

    private IOnRefreshResidentListener refreshListener;
    private IOnClickOnBackButtonListener backButtonListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resident, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#0062FF"));

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_resident);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshListener.onRefreshGameStats(this::updateData);
            swipeRefreshLayout.setRefreshing(false);
        });

        pieCard = view.findViewById(R.id.pieCard);
        tvLastGamesEmpty = view.findViewById(R.id.tv_lastgames_empty_resident);
        tvStatsEmpty = view.findViewById(R.id.tv_stats_empty_resident);
        recyclerViewLastGames = view.findViewById(R.id.latestGames_recycleView_resident);

        View banner = view.findViewById(R.id.resident_banner);
        backButton = banner.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> backButtonListener.onClickOnBackButton());

        residentName = banner.findViewById(R.id.banner_name_game);
        residentName.setText(resident.getName() + " " + resident.getSurnames());

        filteredStats = new ArrayList<>();
        for (GameStat stat : gameStats) {
            if (Objects.equals(stat.getResidentId(), resident.getId())) {
                filteredStats.add(stat);
            }
        }

        lastGamesAdapter = new LastGamesAdapter(filteredStats, games, resident, (IOClickOnGameStatsListener) requireActivity(), () -> {
            updateData();
        });
        recyclerViewLastGames.setAdapter(lastGamesAdapter);
        recyclerViewLastGames.setHasFixedSize(true);
        recyclerViewLastGames.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        ImageButton filterButton = view.findViewById(R.id.filter_lastgames_list_button);
        filterButton.setOnClickListener(v -> showLastGamesFilterDialog());

        updateVisibility(view);
    }

    private void updateData() {
        gameStats = AppDataRepository.getInstance().getGameStats();
        filteredStats.clear();
        for (GameStat stat : gameStats) {
            if (Objects.equals(stat.getResidentId(), resident.getId())) {
                filteredStats.add(stat);
            }
        }
        lastGamesAdapter.updateData(filteredStats, games, resident);
        updateVisibility(getView());
    }

    private void updateVisibility(View view) {
        if (filteredStats.isEmpty()) {
            pieCard.setVisibility(View.GONE);
            recyclerViewLastGames.setVisibility(View.GONE);
            tvLastGamesEmpty.setVisibility(View.VISIBLE);
            tvStatsEmpty.setVisibility(View.VISIBLE);
        } else {
            pieCard.setVisibility(View.VISIBLE);
            recyclerViewLastGames.setVisibility(View.VISIBLE);
            tvLastGamesEmpty.setVisibility(View.GONE);
            tvStatsEmpty.setVisibility(View.GONE);
            pieChart(view);
            generateBarCharts(view);
        }
    }

    private void showLastGamesFilterDialog() {
        String[] options = {
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

    private void filterLastGamesList(String option) {
        List<GameStat> filteredList = new ArrayList<>(gameStats);

        if (option.equals(getString(R.string.game_name_a_z_filter_text))) {
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

        lastGamesAdapter.updateData(filteredList, games, resident);
        recyclerViewLastGames.scrollToPosition(0);
    }

    private String getGameNameById(Long id) {
        return games.stream()
                .filter(g -> g.getId().equals(id))
                .map(Game::getName)
                .findFirst()
                .orElse("");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        if (args != null) {
            resident = (Resident) args.getSerializable("resident");
        }
        refreshListener = (IOnRefreshResidentListener) context;
        backButtonListener = (IOnClickOnBackButtonListener) context;
    }

    private void pieChart(View view){
        CustomPieChartView pieChart = view.findViewById(R.id.pieChart);

        TextView[] gameNames = new TextView[]{
                view.findViewById(R.id.pie_game_1_name),
                view.findViewById(R.id.pie_game_2_name),
                view.findViewById(R.id.pie_game_3_name),
                view.findViewById(R.id.pie_game_4_name),
                view.findViewById(R.id.pie_game_5_name)
        };

        Map<Long, Integer> gameCountMap = new HashMap<>();
        for (GameStat stat : gameStats) {
            if (Objects.equals(stat.getResidentId(), resident.getId())) {
                Long gameId = stat.getGameId();
                gameCountMap.put(gameId, gameCountMap.getOrDefault(gameId, 0) + 1);
            }
        }

        int totalPartidas = gameCountMap.values().stream().mapToInt(Integer::intValue).sum();
        float[] pieValues = new float[gameCountMap.size()];
        int[] pieColors = new int[gameCountMap.size()];
        int[] colorPool = { Color.YELLOW, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN };

        int index = 0;
        for (Map.Entry<Long, Integer> entry : gameCountMap.entrySet()) {
            float porcentaje = (entry.getValue() * 100f) / totalPartidas;
            pieValues[index] = porcentaje;
            pieColors[index] = colorPool[index % colorPool.length];


            String nombreJuego = "";
            for (Game g : games) {
                if (g.getId().equals(entry.getKey())) {
                    String name = g.getName();
                    nombreJuego = name.substring(0, 1).toUpperCase() + name.substring(1);
                    break;
                }
            }

            if (index < gameNames.length) {
                String texto = nombreJuego + " - " + String.format("%.1f", porcentaje) + "%";
                gameNames[index].setText(texto);
                gameNames[index].setVisibility(View.VISIBLE);
                gameNames[index].setBackgroundColor(colorPool[index % colorPool.length]);
            }

            index++;
        }

        // Ocultar los TextView sobrantes si hay menos de 5 juegos
        for (int i = index; i < gameNames.length; i++) {
            gameNames[i].setVisibility(View.GONE);
        }

        pieChart.setData(pieValues, pieColors);
    }

    private void generateBarCharts(View view) {
        LinearLayout barChartsContainer = view.findViewById(R.id.barChartsContainer);
        barChartsContainer.removeAllViews();

        Map<Long, Map<Integer, Integer>> juegosPorDias = new HashMap<>();

        for (GameStat stat : gameStats) {
            if (Objects.equals(stat.getResidentId(), resident.getId())) {
                Long gameId = stat.getGameId();
                int dia = stat.getDateTime().getDayOfWeek().getValue(); // 1 = lunes, ..., 7 = domingo

                juegosPorDias
                        .computeIfAbsent(gameId, k -> new HashMap<>())
                        .put(dia, juegosPorDias.get(gameId).getOrDefault(dia, 0) + 1);
            }
        }

        int index = 0;
        LinearLayout currentRow = null;

        for (Map.Entry<Long, Map<Integer, Integer>> entry : juegosPorDias.entrySet()) {
            if (index >= 5) break;

            if (index % 2 == 0) {
                currentRow = new LinearLayout(requireContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                rowParams.setMargins(0, 0, 0, 32);
                currentRow.setLayoutParams(rowParams);
                barChartsContainer.addView(currentRow);
            }

            String nombreJuego = "";
            for (Game g : games) {
                if (g.getId().equals(entry.getKey())) {
                    String name = g.getName();
                    nombreJuego = name.substring(0, 1).toUpperCase() + name.substring(1);
                    break;
                }
            }

            // Preparar datos por días de la semana
            float[] datos = new float[7];
            Map<Integer, Integer> actividadDias = entry.getValue();
            for (int i = 1; i <= 7; i++) {
                datos[i - 1] = actividadDias.getOrDefault(i, 0);
            }

            // Crear layout vertical del gráfico
            LinearLayout chartLayout = new LinearLayout(requireContext());
            chartLayout.setOrientation(LinearLayout.VERTICAL);
            chartLayout.setPadding(16, 16, 16, 16);

            TextView title = new TextView(requireContext());
            title.setText(nombreJuego);
            title.setTextColor(Color.BLACK);
            title.setTextSize(14);
            chartLayout.addView(title);

            TextView subtitle = new TextView(requireContext());
            subtitle.setText("Actividad semanal");
            subtitle.setTextColor(Color.GRAY);
            subtitle.setTextSize(12);
            chartLayout.addView(subtitle);

            CustomBarChartView chart = new CustomBarChartView(requireContext(), null);
            chart.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    500
            ));
            chart.setData(datos);
            chartLayout.addView(chart);

            CardView cardView = new CardView(requireContext());
            cardView.setCardElevation(6f);
            cardView.setRadius(32f); // 16dp
            cardView.setUseCompatPadding(true);
            cardView.setCardBackgroundColor(Color.WHITE);
            cardView.addView(chartLayout);

            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            );
            colParams.setMargins(8, 0, 8, 0);
            cardView.setLayoutParams(colParams);

            currentRow.addView(cardView);
            index++;
        }
    }

}
