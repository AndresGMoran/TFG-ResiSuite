package com.andresgmoran.apptrabajadores.interfaces;

import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;

import java.util.List;

public interface IOClickOnActivityListener {
    void onClickOnActivity(Activity activity , List<ActivityResident> participants);
    void onDeleteActivitie(Activity activity, Runnable runnable);
}
