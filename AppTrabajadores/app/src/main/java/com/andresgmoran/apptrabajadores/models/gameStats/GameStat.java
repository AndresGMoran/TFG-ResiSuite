package com.andresgmoran.apptrabajadores.models.gameStats;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class GameStat implements Serializable {
    private final Long id;
    private final Long residentId;
    private final Long gameId;
    private final Long userId;
    private final Integer num;
    private final Double duration;
    private final Difficulty difficulty;
    private final LocalDateTime dateTime;
    private final String observation;

    public GameStat(Long id, Long residentId, Long gameId, Long userId, Integer num, Double duration, Difficulty difficulty, LocalDateTime dateTime, String observation) {
        this.id = id;
        this.residentId = residentId;
        this.gameId = gameId;
        this.userId = userId;
        this.num = num;
        this.duration = duration;
        this.difficulty = difficulty;
        this.dateTime = dateTime;
        this.observation = observation;
    }

    public Long getId() {
        return id;
    }

    public Long getResidentId() {
        return residentId;
    }

    public Long getGameId() {
        return gameId;
    }

    public Long getUserId() {
        return userId;
    }

    public Integer getNum() {
        return num;
    }

    public Double getDuration() {
        return duration;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getObservation() {
        return observation;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GameStat gameStat = (GameStat) o;
        return Objects.equals(id, gameStat.id) && Objects.equals(residentId, gameStat.residentId) && Objects.equals(gameId, gameStat.gameId) && Objects.equals(userId, gameStat.userId) && Objects.equals(num, gameStat.num) && Objects.equals(duration, gameStat.duration) && difficulty == gameStat.difficulty && Objects.equals(dateTime, gameStat.dateTime) && Objects.equals(observation, gameStat.observation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, residentId, gameId, userId, num, duration, difficulty, dateTime, observation);
    }
}
