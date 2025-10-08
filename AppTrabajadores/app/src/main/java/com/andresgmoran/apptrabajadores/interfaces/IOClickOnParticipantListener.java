package com.andresgmoran.apptrabajadores.interfaces;

import com.andresgmoran.apptrabajadores.models.ActivityResident;

public interface IOClickOnParticipantListener {
    void onClickOnParticipant(ActivityResident participant);
    void onClickOnAssistance(ActivityResident participant, boolean isTrue, Runnable refresh);
    void onClickOnOpinion(ActivityResident participant, boolean isPreOpinion);
    void onClickOnMaterialHelp(ActivityResident participant, boolean isTrue, Runnable refresh);
    void onClickOnHumanHelp(ActivityResident participant, boolean isTrue, Runnable refresh);
    void onDeleteParticipant(ActivityResident participant, Runnable refresh);

}
