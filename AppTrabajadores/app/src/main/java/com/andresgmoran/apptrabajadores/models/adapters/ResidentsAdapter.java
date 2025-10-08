package com.andresgmoran.apptrabajadores.models.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnResidentListener;
import com.andresgmoran.apptrabajadores.models.Resident;

import java.util.Collections;
import java.util.List;

public class ResidentsAdapter extends RecyclerView.Adapter<ResidentsAdapter.ResidentsViewHolder> {
    private List<Resident> residents;
    private final IOClickOnResidentListener listener;
    private Runnable refresh;

    public ResidentsAdapter(List<Resident> residents, IOClickOnResidentListener listener, Runnable refresh) {
        this.residents = residents;
        this.listener = listener;
        this.refresh = refresh;
    }

    public void updateData(List<Resident> newResidents) {
        this.residents = newResidents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResidentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from( parent.getContext()).inflate(R.layout.item_list_container, parent, false);
        return new ResidentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResidentsViewHolder holder, int position) {
        Resident resident = residents.get(position);
        holder.bindResident(resident);

        holder.optionsButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_options, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_delete) {
                    listener.onTakeOutResident(resident, () -> {
                        refresh.run();
                    });
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickOnResident(resident);
            }
        });
    }

    public int getItemCount() {
        return residents != null ? residents.size() : 0;
    }

    public class ResidentsViewHolder extends RecyclerView.ViewHolder {

        private final TextView residentName;
        private final ImageButton optionsButton;
        private final TextView birthDate;


        public ResidentsViewHolder(View view) {
            super(view);
            residentName = view.findViewById(R.id.name_list_item);
            optionsButton = view.findViewById(R.id.more_options_item_list);
            birthDate = view.findViewById(R.id.resident_info_item);
        }

        public void bindResident(Resident resident) {
            String fullName = resident.getName() + " " + resident.getSurnames();
            residentName.setText(fullName);
            birthDate.setText(resident.getBirthDate().toString());
        }
    }
}
