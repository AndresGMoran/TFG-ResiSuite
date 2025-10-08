package com.andresgmoran.apptrabajadores.ui.fragments.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnAddParticipantListener;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnParticipantListener;
import com.andresgmoran.apptrabajadores.interfaces.IOnChageStateActivityListener;
import com.andresgmoran.apptrabajadores.interfaces.IOnClickOnBackButtonListener;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.ActivityState;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.adapters.ParticipantsAdapter;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;
import com.andresgmoran.apptrabajadores.ui.MainActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ActivityDetailFragment extends Fragment {

    private View rootView;

    private Activity activity;
    private List<ActivityResident> participants;
    private List<Resident> residents = AppDataRepository.getInstance().getResidents();
    private ParticipantsAdapter adapter;
    private RecyclerView recyclerView;

    private IOClickOnAddParticipantListener addParticipantListener;
    private IOnChageStateActivityListener changeStateActivityListener;
    private IOnClickOnBackButtonListener backButtonListener;

    public interface OnRefreshActivityDetailListener {
        void onRefreshActivityDetail(Runnable refresh);
    }
    private OnRefreshActivityDetailListener refreshActivityDetailListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.rootView = view;

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#0062FF"));

        setupSwipeRefresh(view);
        setupBackButton(view);
        setupActivityInfo(view);
        setupParticipantsStats(view);
        setupAssistanceStats(view);
        setupRecyclerView(view);
        setupFab(view);
    }

    public void updateData() {
        if (getContext() == null || rootView == null) return;

        List<Activity> activities = AppDataRepository.getInstance().getActivities();
        for (Activity a : activities) {
            if (a.getId() == activity.getId()) {
                this.activity = a;
                break;
            }
        }
        this.participants.clear();
        List<ActivityResident> allParticipants = AppDataRepository.getInstance().getActivityResidents();
        for (ActivityResident p : allParticipants) {
            if (p.getActivityId() == activity.getId()) {
                this.participants.add(p);
            }
        }

        residents = AppDataRepository.getInstance().getResidents();
        setupSwipeRefresh(rootView);
        setupActivityInfo(rootView);
        setupParticipantsStats(rootView);
        setupAssistanceStats(rootView);
        adapter.updateData(activity, participants, residents);
    }

    private void setupSwipeRefresh(View view) {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_activity_detail);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshActivityDetailListener.onRefreshActivityDetail(this::updateData);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupBackButton(View view) {
        ImageButton backButton;
        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> backButtonListener.onClickOnBackButton());
    }

    private void setupActivityInfo(View view) {
        TextView activityName = view.findViewById(R.id.activitie_detail_name);
        TextView description = view.findViewById(R.id.tv_description_activity_detail);
        TextView activityDate = view.findViewById(R.id.tv_date_activity_detail);
        TextView activityExitTime = view.findViewById(R.id.tv_exit_time_activity_detail);

        activityName.setText(activity.getName());
        description.setText(activity.getDescription());
        activityDate.setText(activity.getDate().toLocalDate().toString());
        activityExitTime.setText(activity.getDate().toLocalTime().toString());

        MaterialButton startEndButton = view.findViewById(R.id.btn_start_end_activity);
        ActivityState state = activity.getState();
        if (state == ActivityState.ABIERTO){
            startEndButton.setVisibility(View.GONE);
        }else {
        switch (state) {
            case CERRADO:
                configureButton(startEndButton, getContext().getString(R.string.start_activity_text), "#59FF00", () ->
                        changeStateActivityListener.onChangeStateActivity(activity, ActivityState.EN_CURSO, this::updateData));
                break;
            case EN_CURSO:
                configureButton(startEndButton, getContext().getString(R.string.finish_activity_text), "#FF0000" , () ->
                        changeStateActivityListener.onChangeStateActivity(activity, ActivityState.FINALIZADA, this::updateData));
                break;
            case FINALIZADA:
                startEndButton.setText(getContext().getString(R.string.activity_finished_text));
                startEndButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#324F5E")));
                startEndButton.setEnabled(false);
                break;
        }
        }
    }

    private void configureButton(MaterialButton button, String text, String hexColor, Runnable onClick) {
        button.setText(text);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(hexColor)));
        button.setOnClickListener(v -> onClick.run());
    }

    private void setupParticipantsStats(View view) {
        List<ActivityResident> activityParticipants = new ArrayList<>();
        for (ActivityResident p : participants) {
            if (p.getActivityId() == activity.getId()) {
                activityParticipants.add(p);
            }
        }

        TextView totalParticipants = view.findViewById(R.id.tv_total_participants_activity_detail);
        TextView confirmedParticipants = view.findViewById(R.id.tv_confirmed_residents);
        TextView notGoingParticipants = view.findViewById(R.id.tv_unconfirmed_residents);

        int goingCount = 0, notGoingCount = 0;
        for (ActivityResident p : activityParticipants) {
            if (p.isAssistance()) goingCount++;
            else notGoingCount++;
        }

        totalParticipants.setText(String.valueOf(activityParticipants.size()));
        confirmedParticipants.setText(String.valueOf(goingCount));
        notGoingParticipants.setText(String.valueOf(notGoingCount));
    }

    private void setupAssistanceStats(View view) {
        TextView humanAssistance = view.findViewById(R.id.tv_required_human_attendance);
        TextView materialAssistance = view.findViewById(R.id.tv_required_material_attendance);

        int humanCount = 0, materialCount = 0;
        for (ActivityResident p : participants) {
            if (p.getActivityId() == activity.getId()) {
                if (p.isHumanHelp())
                    humanCount++;
                if (p.isMaterialHelp())
                    materialCount++;
            }
        }

        humanAssistance.setText(String.valueOf(humanCount));
        materialAssistance.setText(String.valueOf(materialCount));
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.rv_participants);
        adapter = new ParticipantsAdapter(requireContext(), activity, participants, residents, (IOClickOnParticipantListener) requireActivity(), this::updateData);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
    }

    private void setupFab(View view) {
        MaterialButton fab = view.findViewById(R.id.add_participant_button);
        if (activity.getState() != ActivityState.ABIERTO) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(v ->
                    addParticipantListener.onClickOnAddParticipant(activity, participants)
            );

            NestedScrollView scrollView = view.findViewById(R.id.nested_scroll_game_detail);
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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (getArguments() != null) {
            activity = (Activity) getArguments().getSerializable("activity");
            participants = (List<ActivityResident>) getArguments().getSerializable("participants");
        }

        refreshActivityDetailListener = (OnRefreshActivityDetailListener) context;

        addParticipantListener = (IOClickOnAddParticipantListener) context;
        changeStateActivityListener = (IOnChageStateActivityListener) context;
        backButtonListener = (IOnClickOnBackButtonListener) context;
    }
}
