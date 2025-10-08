package com.andresgmoran.apptrabajadores.ui.fragments.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.models.ActivityResident;

public class OpinionFragment extends Fragment {

    private ActivityResident participant;
    private boolean isPreOpinion;

    public interface OnAddOpinionListener {
        void onAddOpinion(ActivityResident participant, boolean isPreOpinion, String opinion);
    }

    private OnAddOpinionListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_opinion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText opinionEditText = view.findViewById(R.id.opinion_edit_text);
        Button addOpinionButton = view.findViewById(R.id.add_opinion_button);

        addOpinionButton.setOnClickListener(v -> {
            String opinion = opinionEditText.getText().toString().trim();

            if (TextUtils.isEmpty(opinion)) {
                opinionEditText.setError("Campo obligatorio");
                return;
            }

            requireActivity().getSupportFragmentManager().popBackStack();
            listener.onAddOpinion(participant, isPreOpinion, opinion);
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnAddOpinionListener) {
            listener = (OnAddOpinionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnAddOpinionListener");
        }

        if (getArguments() != null) {
            participant = (ActivityResident) getArguments().getSerializable("participant");
            isPreOpinion = getArguments().getBoolean("isPreOpinion");
        }
    }
}
