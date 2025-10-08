package com.andresgmoran.apptrabajadores.ui.fragments.activities;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOnClickOnBackButtonListener;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;

import java.util.List;

public class ParticipantDetailFragment extends Fragment {

    private ImageButton backButton;

    private TextView participantNameTextView;
    private ImageView participantImageView;
    private TextView participantActivityTextView;
    private TextView needMaterialHelpTextView;
    private TextView needHumanHelpTextView;
    private TextView preOpinionTextView;
    private TextView postOpinionTextView;

    private ActivityResident participant;
    private List<Resident> residents;
    private List<Activity> activities;

    public interface IOnRefreshParticipantDetailListener {
        void onRefreshParticipantDetail(Runnable refresh);
    }

    private IOnRefreshParticipantDetailListener refreshParticipantDetailListener;
    private IOnClickOnBackButtonListener backButtonListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_participant_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#0062FF"));

        View participantCardView = view.findViewById(R.id.participant_card_view);

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_participant_detail);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshParticipantDetailListener.onRefreshParticipantDetail(this::updateData);
            swipeRefreshLayout.setRefreshing(false);
        });

        backButton = participantCardView.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> backButtonListener.onClickOnBackButton());


        participantNameTextView = participantCardView.findViewById( R.id.banner_name_game);
        participantImageView = participantCardView.findViewById(R.id.image_item_person_banner);
        participantActivityTextView = view.findViewById(R.id.tv_activity_participant);
        needMaterialHelpTextView = view.findViewById(R.id.tv_need_material_help_participant_detail);
        needHumanHelpTextView = view.findViewById(R.id.tv_need_human_help_participant_detail);
        preOpinionTextView = view.findViewById(R.id.tv_pre_opinion_participant_detail);
        postOpinionTextView = view.findViewById(R.id.tv_post_opinion_participant_detail);

        for (Resident resident : residents) {
            if (resident.getId().equals(participant.getIdResident())) {
                participantNameTextView.setText(resident.getName() + " " + resident.getSurnames());
                //participantImageView.setImageBitmap(resident.getImage());
                break;
            }
        }

        for (Activity activity : activities) {
            if (activity.getId().equals(participant.getActivityId())) {
                participantActivityTextView.setText(activity.getName());
                break;
            }
        }

        if (participant.isMaterialHelp())
            needMaterialHelpTextView.setText("Si");
        else
            needMaterialHelpTextView.setText("No");

        if (participant.isHumanHelp())
            needHumanHelpTextView.setText("Si");
        else
            needHumanHelpTextView.setText("No");

        if (participant.getPreOpinion().isEmpty())
            preOpinionTextView.setText("No hay opinión previa");
        else
            preOpinionTextView.setText(participant.getPreOpinion());

        if (participant.getPostOpinion().isEmpty())
            postOpinionTextView.setText("No hay opinión posterior");
        else
            postOpinionTextView.setText(participant.getPostOpinion());

    }

    private void updateData(){
        residents = AppDataRepository.getInstance().getResidents();
        activities = AppDataRepository.getInstance().getActivities();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getArguments() != null) {
            participant = (ActivityResident) getArguments().getSerializable("participant");
        }
        residents = AppDataRepository.getInstance().getResidents();
        activities = AppDataRepository.getInstance().getActivities();

        refreshParticipantDetailListener = (IOnRefreshParticipantDetailListener) context;
        backButtonListener = (IOnClickOnBackButtonListener) context;
    }
}
