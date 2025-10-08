package com.andresgmoran.apptrabajadores.interfaces;

import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.Resident;

import java.util.List;

public interface IOClickOnAddParticipantListener {
    void onClickOnAddParticipant(Activity activity, List<ActivityResident> participants);
}
