package com.andresgmoran.apptrabajadores.models.adapters;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnActivityListener;
import com.andresgmoran.apptrabajadores.interfaces.IOnChageStateActivityListener;
import com.andresgmoran.apptrabajadores.models.Activity;
import com.andresgmoran.apptrabajadores.models.ActivityResident;
import com.andresgmoran.apptrabajadores.models.ActivityState;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivitiesViewHolder> {

    private final Context context;
    private List<Activity> activities;
    private List<ActivityResident> participants;
    private IOClickOnActivityListener listener;
    private IOnChageStateActivityListener changeStateListener;
    private Runnable refresh;


    public ActivitiesAdapter(Context context, List<Activity> activities, List<ActivityResident> participants, IOClickOnActivityListener listener, IOnChageStateActivityListener changeStateListener, Runnable refresh) {
        this.context = context;
        this.activities = activities;
        this.participants = participants;
        this.listener = listener;
        this.changeStateListener = changeStateListener;
        this.refresh = refresh;
    }
    @NonNull
    @Override
    public ActivitiesAdapter.ActivitiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from( parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivitiesAdapter.ActivitiesViewHolder(view);
    }

    public void updateData(List<Activity> newActivities, List<ActivityResident> newParticipants) {
        this.activities = newActivities;
        this.participants = newParticipants;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ActivitiesViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bindActivity(activity);
        List<ActivityResident> activityResidents = new ArrayList<>();
        for (ActivityResident activityResident : participants) {
            if (activityResident.getActivityId() == activity.getId()) {
                activityResidents.add(activityResident);
            }
        }

        if (activity.getState() != ActivityState.CERRADO){
            holder.activityStateButton.setOnClickListener( v -> {
                new AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.close_activity_title))
                        .setMessage(context.getString(R.string.close_activity_message))
                        .setPositiveButton(context.getString(R.string.accept_text), (dialog, which) -> {
                            changeStateListener.onChangeStateActivity(activity, ActivityState.CERRADO, refresh);
                        })
                        .setNegativeButton(context.getString(R.string.cancel_text), (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            });
        }

        holder.optionsButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    if (activities != null)
                        listener.onDeleteActivitie(activity, refresh);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickOnActivity(activity, activityResidents);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public class ActivitiesViewHolder extends RecyclerView.ViewHolder {

        private final TextView activityName;
        private final TextView activityDate;
        private final TextView activityTime;
        private final ImageButton optionsButton;
        private final ImageButton activityStateButton;


        public ActivitiesViewHolder(View view) {
            super(view);
            activityName = itemView.findViewById(R.id.name_salida);
            activityDate = itemView.findViewById(R.id.date_text_activityItem);
            activityTime = itemView.findViewById(R.id.time_text_activityItem);
            optionsButton = itemView.findViewById(R.id.more_options_activity_item);
            activityStateButton = itemView.findViewById(R.id.activity_status_button);

        }

        public void bindActivity(Activity activity) {
            activityName.setText(activity.getName());

            Locale currentLocale = itemView.getContext().getResources().getConfiguration().getLocales().get(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", currentLocale);
            String fechaFormateada = activity.getDate().format(formatter);
            fechaFormateada = fechaFormateada.substring(0, 1).toUpperCase() + fechaFormateada.substring(1);
            activityDate.setText(fechaFormateada);

            DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm", currentLocale);
            String horaFormateada = activity.getDate().format(formatterHora).toLowerCase();
            activityTime.setText(horaFormateada);

            if (activity.getState() == ActivityState.ABIERTO) {
                activityStateButton.setImageResource(R.drawable.open_activity_status);
            } else {
                activityStateButton.setImageResource(R.drawable.closed_activity_status);
            }
        }

    }
}
