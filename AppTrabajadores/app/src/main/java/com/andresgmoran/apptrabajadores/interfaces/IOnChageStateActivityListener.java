package com.andresgmoran.apptrabajadores.interfaces;

import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityState;

public interface IOnChageStateActivityListener {
    void onChangeStateActivity(Activity activity, ActivityState state, Runnable refresh);
}
