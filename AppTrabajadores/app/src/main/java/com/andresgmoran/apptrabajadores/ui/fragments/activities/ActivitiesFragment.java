package com.andresgmoran.apptrabajadores.ui.fragments.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnActivityListener;
import com.andresgmoran.apptrabajadores.interfaces.IOnChageStateActivityListener;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.ActivityState;
import com.andresgmoran.apptrabajadores.models.adapters.ActivitiesAdapter;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActivitiesFragment extends Fragment {

    private ActivitiesAdapter activitiesAdapter;
    private RecyclerView activitiesRecyclerView;

    public interface IOnActivities {
        void onRefreshActivities(Runnable refresh);
        void onClickOnAddActivity();
    }
    private IOnActivities listener;

    private List<Activity> activities = AppDataRepository.getInstance().getActivities();
    private List<ActivityResident> participants = AppDataRepository.getInstance().getActivityResidents();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate( R.layout.fragment_activities_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#CCCCCC"));

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout_activities_list);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            listener.onRefreshActivities( () -> {
                reloadData();
            });
            swipeRefreshLayout.setRefreshing(false);
        });

        activitiesAdapter = new ActivitiesAdapter(requireContext(), activities, participants, (IOClickOnActivityListener) requireActivity(), (IOnChageStateActivityListener) requireActivity(), this::reloadData);
        activitiesRecyclerView = view.findViewById(R.id.activities_recycleView);
        activitiesRecyclerView.setAdapter(activitiesAdapter);
        activitiesRecyclerView.setHasFixedSize(true);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        setupFab(view);
        filterButton(view);
    }
    public void reloadData() {
        activities = AppDataRepository.getInstance().getActivities();
        participants = AppDataRepository.getInstance().getActivityResidents();
        activitiesAdapter.updateData(activities, participants);
    }

    private void setupFab(View view) {
        MaterialButton fab = view.findViewById(R.id.add_activity_button);

        fab.setOnClickListener(v ->
                listener.onClickOnAddActivity()
        );


        NestedScrollView scrollView = view.findViewById(R.id.nested_scroll_view_activities);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            private int lastScrollY = 0;
            private boolean isButtonVisible = true;

            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int dy = scrollY - oldScrollY;

                if (dy > 20 && isButtonVisible) {
                    fab.animate().translationY(fab.getHeight() + 100).alpha(0f).setDuration(200).withEndAction(() -> fab.setVisibility(View.INVISIBLE));
                    isButtonVisible = false;
                } else if (dy < -20 && !isButtonVisible) {
                    fab.setVisibility(View.VISIBLE);
                    fab.setAlpha(0f);
                    fab.setTranslationY(fab.getHeight() + 100);
                    fab.animate().translationY(0).alpha(1f).setDuration(200);
                    isButtonVisible = true;
                }
            }
        });
        fab.setVisibility(View.VISIBLE);
        fab.setAlpha(1f);
        fab.setTranslationY(0);

    }

    private void filterButton(View view){
        ImageButton filterButton = view.findViewById(R.id.filter_activities_list_image_button);
        filterButton.setOnClickListener( v -> {
            String[] options = {getString(R.string.status_open_filter_text),
                    getString(R.string.status_closed_filter_text),
                    getString(R.string.status_on_going_filter_text),
                    getString(R.string.status_finished_filter_text),
                    getString(R.string.activity_date_filter_text)};

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.filter_activities_title)
                    .setItems(options, (dialog, which) -> filterActivities(options[which]))
                    .show();
        });
    }

    private void filterActivities(String option) {
        List<Activity> filteredList = new ArrayList<>(AppDataRepository.getInstance().getActivities());

        if (option.equals(getString(R.string.status_open_filter_text))) {
            filteredList.removeIf(activity -> activity.getState() != ActivityState.ABIERTO);
        } else if (option.equals(getString(R.string.status_closed_filter_text))) {
            filteredList.removeIf(activity -> activity.getState() != ActivityState.CERRADO);
        } else if (option.equals(getString(R.string.status_on_going_filter_text))) {
            filteredList.removeIf(activity -> activity.getState() != ActivityState.EN_CURSO);
        } else if (option.equals(getString(R.string.status_finished_filter_text))) {
            filteredList.removeIf(activity -> activity.getState() != ActivityState.FINALIZADA);
        } else if (option.equals(getString(R.string.activity_date_filter_text))) {
            filteredList.sort(Comparator.comparing(Activity::getDate));
        }

        activitiesAdapter.updateData(filteredList, participants);
        activitiesRecyclerView.scrollToPosition(0);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (IOnActivities) context;
    }
}
