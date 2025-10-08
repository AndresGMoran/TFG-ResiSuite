package com.andresgmoran.apptrabajadores.ui.fragments.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;

import java.util.ArrayList;
import java.util.List;

public class ParticipantSelectionDialogFragment extends DialogFragment {
    public interface OnParticipantSelectedListener {
        void onParticipantSelected(Activity activity, List<ActivityResident> participants , Resident selectedResident);
    }

    private OnParticipantSelectedListener listener;
    private Activity activityId;
    private List<ActivityResident> participants;
    private List<Resident> residentList;

    public ParticipantSelectionDialogFragment(Activity activityId, List<ActivityResident> participants) {
        this.participants = participants;
        this.activityId = activityId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<Resident> filteredResidents = new ArrayList<>();

        for (Resident resident : residentList) {
            boolean alreadyParticipant = false;
            for (ActivityResident activityResident : participants) {
                if (activityResident.getIdResident().equals(resident.getId())) {
                    alreadyParticipant = true;
                    break;
                }
            }
            if (!alreadyParticipant) {
                filteredResidents.add(resident);
            }
        }

        String[] names = new String[filteredResidents.size()];
        for (int i = 0; i < filteredResidents.size(); i++) {
            Resident r = filteredResidents.get(i);
            names[i] = r.getName() + " " + r.getSurnames();
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle("Selecciona un residente")
                .setItems(names, (dialog, which) -> {
                    if (listener != null && which >= 0 && which < filteredResidents.size()) {
                        listener.onParticipantSelected(activityId, participants, filteredResidents.get(which));
                    }
                })
                .create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        residentList = AppDataRepository.getInstance().getResidents();
        if (context instanceof OnParticipantSelectedListener) {
            listener = (OnParticipantSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnParticipantSelectedListener");
        }
    }
}
