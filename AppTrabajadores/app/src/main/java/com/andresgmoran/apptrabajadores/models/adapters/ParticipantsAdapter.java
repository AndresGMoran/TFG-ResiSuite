package com.andresgmoran.apptrabajadores.models.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnParticipantListener;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.ActivityState;
import com.andresgmoran.apptrabajadores.models.Resident;

import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.ParticipantsViewHolder> {

    private final Context context;
    private Activity activity;
    private List<Resident> residents;
    private List<ActivityResident> activityResidents;
    private IOClickOnParticipantListener listener;
    private Runnable refresh;


    public ParticipantsAdapter(Context context, Activity activity, List<ActivityResident> activityResidents, List<Resident> residents  , IOClickOnParticipantListener listener, Runnable refresh) {
        this.context = context;
        this.activity = activity;
        this.residents = residents;
        this.activityResidents = activityResidents;
        this.listener = listener;
        this.refresh = refresh;
    }
    @NonNull
    @Override
    public ParticipantsAdapter.ParticipantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from( parent.getContext()).inflate(R.layout.item_resident_activity, parent, false);
        return new ParticipantsAdapter.ParticipantsViewHolder(view);
    }

    public void updateData(Activity activity, List<ActivityResident> activityResidents, List<Resident> residents) {
        this.activityResidents = activityResidents;
        this.residents = residents;
        this.activity = activity;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantsViewHolder holder, int position) {
        ActivityResident activityResident = activityResidents.get(position);
        holder.bindActivity(activityResident);
        if (activity.getState().equals(ActivityState.ABIERTO)){
            holder.optionsButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.action_delete) {
                        listener.onDeleteParticipant(activityResident, () -> {
                            refresh.run();
                        });
                        return true;
                    }
                    return false;
                });

                popupMenu.show();
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickOnParticipant(activityResident);
            }
        });

        holder.assitanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity.getState() == ActivityState.ABIERTO){
                    if (activityResident.isAssistance()){
                        listener.onClickOnAssistance(activityResident, false, refresh);
                    } else {
                        listener.onClickOnAssistance(activityResident, true, refresh);
                    }
                }
            }
        });

        holder.opinionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity.getState() == ActivityState.ABIERTO && activityResident.getPreOpinion().isEmpty()) {
                    // Dejar al usuario añadir un comentario y cambiar boolean de preOpinion a true
                    listener.onClickOnOpinion(activityResident, true);
                } else if (activity.getState() == ActivityState.FINALIZADA && activityResident.getPostOpinion().isEmpty()){
                    // Dejar al usuario añadir un comentario y cambiar boolean de postOpinion a true
                    listener.onClickOnOpinion(activityResident, false);
                }
            }
        });

        holder.materialHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity.getState() == ActivityState.ABIERTO){
                    if (activityResident.isMaterialHelp()){
                        listener.onClickOnMaterialHelp(activityResident, false, refresh);
                    }
                    if (!activityResident.isMaterialHelp()) {
                        listener.onClickOnMaterialHelp(activityResident, true, refresh);
                    }
                }
            }
        });

        holder.humanHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity.getState() == ActivityState.ABIERTO){
                    if (activityResident.isHumanHelp()){
                        listener.onClickOnHumanHelp(activityResident, false, refresh);
                    }
                    if (!activityResident.isHumanHelp()) {
                        listener.onClickOnHumanHelp(activityResident, true, refresh);
                    }
                }
            }
        });

        holder.assitanceButton.setBackgroundResource(R.drawable.activity_button_unpressed_background);
        holder.materialHelp.setBackgroundResource(R.drawable.activity_button_unpressed_background);
        holder.humanHelp.setBackgroundResource(R.drawable.activity_button_unpressed_background);
        holder.opinionButton.setBackgroundResource(R.drawable.activity_button_unpressed_background);

        if (activityResident.isAssistance()){
            holder.assitanceButton.setBackgroundResource(R.drawable.activity_button_pressed_background);
        }

        if (activityResident.isMaterialHelp()){
            holder.materialHelp.setBackgroundResource(R.drawable.activity_button_pressed_background);
        }
        Log.e( "TAG", "MaterialHelp: " + activityResident.isMaterialHelp());
        if (activityResident.isHumanHelp()){
            holder.humanHelp.setBackgroundResource(R.drawable.activity_button_pressed_background);
        }
        Log.e( "TAG", "HumanHelp: " + activityResident.isHumanHelp());
        Log.e( "TAG", "State: " + activity.getState());
        Log.e( "TAG", "PreOpinion: " + activityResident.getPreOpinion());
        Log.e( "TAG", " State: " + activityResident.isAssistance());
        if (activity.getState() == ActivityState.ABIERTO && !activityResident.getPreOpinion().isEmpty()){
            holder.opinionButton.setBackgroundResource(R.drawable.activity_button_pressed_background);
        }
        Log.e( "TAG", "Opinion: " + activityResident.getPostOpinion() + " State: " + activityResident.isAssistance());
        if (activity.getState() == ActivityState.FINALIZADA && !activityResident.getPostOpinion().isEmpty()){
            holder.opinionButton.setBackgroundResource(R.drawable.activity_button_pressed_background);
        }
    }

    @Override
    public int getItemCount() {
        return activityResidents.size();
    }

    public class ParticipantsViewHolder extends RecyclerView.ViewHolder {

        private final TextView name;
        private final ImageButton assitanceButton;
        private final ImageButton opinionButton;
        private final ImageButton materialHelp;
        private final ImageButton humanHelp;
        private final ImageButton optionsButton;


        public ParticipantsViewHolder(View view) {
            super(view);
            name = itemView.findViewById(R.id.tv_name_participant_item);
            assitanceButton = itemView.findViewById(R.id.participant_asistencia_button);
            opinionButton = itemView.findViewById(R.id.participant_opinion_button);
            materialHelp = itemView.findViewById(R.id.participant_material_help_button);
            humanHelp = itemView.findViewById(R.id.participant_human_help_button);
            optionsButton = itemView.findViewById(R.id.more_options_item_resident_activity);
        }

        public void bindActivity(ActivityResident activityResident) {
            for( Resident resident : residents){
                if(resident.getId() == activityResident.getIdResident()){
                    name.setText(resident.getName() + " " + resident.getSurnames());
                    break;
                }
            }
        }

    }
}
