package com.andresgmoran.apptrabajadores.ui.fragments.residence;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOnClickOnBackButtonListener;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Residence;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;
import com.andresgmoran.apptrabajadores.ui.graphics.CustomBarChartView;
import com.andresgmoran.apptrabajadores.ui.graphics.CustomPieChartView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResidenceDetailFragment extends Fragment {

    private TextView residenceNameTextView;
    private TextView totalResidentsTextView;
    private TextView residentsTakenOutTextView;
    private TextView allGamesTextView;
    private TextView gamesLastWeekTextView;
    private TextView statsEmptyTextView;

    private CardView pieChartCardView;
    private CustomPieChartView pieChart;
    private LinearLayout barChartsContainer;

    private Residence residence;
    private List<Resident> residents;
    private List<Resident> residentsTakenOut;
    private List<GameStat> gameStats;
    private List<Game> games;

    public interface IOnRefreshResidenceDetailListener {
        void onRefreshResidenceDetail(Runnable refresh);
    }

    private IOnRefreshResidenceDetailListener refreshListener;
    private IOnClickOnBackButtonListener backButtonListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_residence_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#0062FF"));

        setupSwipeRefresh(view);
        setupBackButton(view);

        residenceNameTextView = view.findViewById(R.id.residence_detail_name);
        totalResidentsTextView = view.findViewById(R.id.tv_residents_residence_detail);
        residentsTakenOutTextView = view.findViewById(R.id.tv_residents_taken_down_residence_detail);
        allGamesTextView = view.findViewById(R.id.tv_total_games_residence_detail);
        gamesLastWeekTextView = view.findViewById(R.id.tv_games_last_week_residence_detail);
        updateUI();

        statsEmptyTextView = view.findViewById(R.id.tv_stats_empty_residence_detail);
        pieChartCardView = view.findViewById(R.id.pieCard_residence_detail);
        pieChart = view.findViewById(R.id.pieChart_residence_detail);
        barChartsContainer = view.findViewById(R.id.barChartsContainer_residence_detail);

        if(gameStats.isEmpty()){
            pieChartCardView.setVisibility(View.GONE);
            barChartsContainer.setVisibility(View.GONE);
            statsEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            statsEmptyTextView.setVisibility(View.GONE);
            pieChartCardView.setVisibility(View.VISIBLE);
            barChartsContainer.setVisibility(View.VISIBLE);
            pieChart(view);
            generateBarCharts(view);
        }
    }

    private void updateData() {
        residence = AppDataRepository.getInstance().getResidence();
        residents = AppDataRepository.getInstance().getResidents();
        residentsTakenOut = AppDataRepository.getInstance().getResidentsTakenOut();
        gameStats = AppDataRepository.getInstance().getGameStats();
        games = AppDataRepository.getInstance().getGames();

        updateUI();
        pieChart(getView());
        generateBarCharts(getView());
    }

    private void updateUI() {
        if (residence != null) {
            residenceNameTextView.setText(residence.getName());
            totalResidentsTextView.setText(String.valueOf(residence.getResidents().size()));
            residentsTakenOutTextView.setText(String.valueOf(residentsTakenOut.size()));
            allGamesTextView.setText(String.valueOf(gameStats.size()));

            int gamesLastWeekCount = 0;
            for (GameStat gameStat : gameStats) {
                if (gameStat.getDateTime() != null &&
                    gameStat.getDateTime().toLocalDate().isAfter(java.time.LocalDate.now().minusDays(7))) {
                    gamesLastWeekCount++;
                }
            }
            gamesLastWeekTextView.setText(String.valueOf(gamesLastWeekCount));
        }
    }

    private void setupSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_residence_detail);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshListener.onRefreshResidenceDetail(this::updateData);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupBackButton(View view) {
        ImageButton backButton;
        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v ->
                backButtonListener.onClickOnBackButton());
    }

    private void pieChart(View view){
        TextView[] gameNames = new TextView[]{
                view.findViewById(R.id.pie_game_1_name_residence_detail),
                view.findViewById(R.id.pie_game_2_name_residence_detail),
                view.findViewById(R.id.pie_game_3_name_residence_detail),
                view.findViewById(R.id.pie_game_4_name_residence_detail),
                view.findViewById(R.id.pie_game_5_name_residence_detail)
        };

        // Mapa juegoId → número de partidas jugadas
        Map<Long, Integer> gameCountMap = new HashMap<>();
        List<Long> residentIds = residence.getResidents();

        for (GameStat stat : gameStats) {
            if (residentIds.contains(stat.getResidentId())) {
                Long gameId = stat.getGameId();
                gameCountMap.put(gameId, gameCountMap.getOrDefault(gameId, 0) + 1);
            }
        }

        int totalPartidas = gameCountMap.values().stream().mapToInt(Integer::intValue).sum();
        if (totalPartidas == 0) {
            pieChart.setVisibility(View.GONE);
            view.findViewById(R.id.tv_stats_empty_residence_detail).setVisibility(View.VISIBLE);
            for (TextView tv : gameNames) tv.setVisibility(View.GONE);
            return;
        }

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

        // Ocultar los TextView sobrantes
        for (int i = index; i < gameNames.length; i++) {
            gameNames[i].setVisibility(View.GONE);
        }

        pieChart.setVisibility(View.VISIBLE);
        pieChart.setData(pieValues, pieColors);
    }

    private void generateBarCharts(View view) {
        barChartsContainer.removeAllViews();

        // Mapa de juego → [día de la semana → cantidad de partidas]
        Map<Long, Map<Integer, Integer>> juegosPorDias = new HashMap<>();
        List<Long> residentIds = residence.getResidents();

        for (GameStat stat : gameStats) {
            if (residentIds.contains(stat.getResidentId())) {
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

            float[] datos = new float[7];
            Map<Integer, Integer> actividadDias = entry.getValue();
            for (int i = 1; i <= 7; i++) {
                datos[i - 1] = actividadDias.getOrDefault(i, 0);
            }

            // Layout vertical por gráfico
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
            cardView.setRadius(32f);
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

            if (currentRow != null) {
                currentRow.addView(cardView);
            }

            index++;
        }
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        residence = AppDataRepository.getInstance().getResidence();
        residents = AppDataRepository.getInstance().getResidents();
        residentsTakenOut = AppDataRepository.getInstance().getResidentsTakenOut();
        gameStats = AppDataRepository.getInstance().getGameStats();
        games = AppDataRepository.getInstance().getGames();

        refreshListener = (IOnRefreshResidenceDetailListener) context;
        backButtonListener = (IOnClickOnBackButtonListener) context;
    }
}
