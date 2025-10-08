package com.andresgmoran.apptrabajadores.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class ActivityResident implements Serializable {
    private final Long id;
    private final Long activityId;
    private final Long idResident;
    private final boolean assistance;
    private final boolean humanHelp;
    private final boolean materialHelp;
    private String preOpinion;
    private String postOpinion;

    public ActivityResident(Long id, Long activityId, Long idResident,boolean assistance, boolean humanHelp, boolean materialHelp, String preOpinion, String postOpinion) {
        this.id = id;
        this.activityId = activityId;
        this.idResident = idResident;
        this.assistance = assistance;
        this.humanHelp = humanHelp;
        this.materialHelp = materialHelp;
        this.preOpinion = preOpinion;
        this.postOpinion = postOpinion;
    }
    public Long getId() {
        return id;
    }

    public Long getActivityId() {
        return activityId;
    }
    public Long getIdResident() {
        return idResident;
    }
    public boolean isAssistance() {
        return assistance;
    }

    public boolean isHumanHelp() {
        return humanHelp;
    }
    public boolean isMaterialHelp() {
        return materialHelp;
    }
    public String getPreOpinion() {
        return preOpinion;
    }
    public void setPreOpinion(String preOpinion) {
        this.preOpinion = preOpinion;
    }
    public String getPostOpinion() {
        return postOpinion;
    }
    public void setPostOpinion(String postOpinion) {
        this.postOpinion = postOpinion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ActivityResident that = (ActivityResident) o;
        return humanHelp == that.humanHelp && materialHelp == that.materialHelp && Objects.equals(id, that.id) && Objects.equals(activityId, that.activityId) && Objects.equals(idResident, that.idResident) && Objects.equals(preOpinion, that.preOpinion) && Objects.equals(postOpinion, that.postOpinion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, activityId, idResident, humanHelp, materialHelp, preOpinion, postOpinion);
    }
}
