package com.andresgmoran.apptrabajadores.interfaces;

import com.andresgmoran.apptrabajadores.models.Resident;

public interface IOClickOnResidentListener {
    void onClickOnResident(Resident resident);
    void onTakeOutResident(Resident resident, Runnable refresh);
}
