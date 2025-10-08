package com.andresgmoran.apptrabajadores.interfaces;

import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.gameStats.GameStat;

public interface IOClickOnGameStatsListener {
    void onClickOnLatestGame(GameStat gameStat, Resident gameStatResident, Game gameStatGame);
    void onDeleteGameStat(GameStat gameStat, Game gameStatGame, Runnable runnable);
}
