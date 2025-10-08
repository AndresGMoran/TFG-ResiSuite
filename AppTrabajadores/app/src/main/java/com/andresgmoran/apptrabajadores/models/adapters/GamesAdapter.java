package com.andresgmoran.apptrabajadores.models.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.interfaces.IOClickOnGameListener;
import com.andresgmoran.apptrabajadores.models.Game;

import java.util.List;
import java.util.Map;

public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.GamesViewHolder> {
    private List<Game> games;
    private final IOClickOnGameListener listener;
    private final Map<Long, Double> weeklyPercentages;

    public GamesAdapter(List<Game> games, IOClickOnGameListener listener, Map<Long, Double> weeklyPercentages) {
        this.games = games;
        this.listener = listener;
        this.weeklyPercentages = weeklyPercentages;
    }

    public void updateData(List<Game> newGames) {
        this.games = newGames;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GamesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_container, parent, false);
        return new GamesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GamesViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bindGame(game);

        holder.itemView.setOnClickListener(v -> listener.onClickOnGame(game));
    }

    public int getItemCount() {
        return games != null ? games.size() : 0;
    }

    public class GamesViewHolder extends RecyclerView.ViewHolder {
        private final TextView gameName;
        private final ImageView gameImage;
        private final TextView percentageView;
        private final ImageButton optionsButton;

        public GamesViewHolder(View view) {
            super(view);
            gameName = view.findViewById(R.id.name_list_item);
            gameImage = view.findViewById(R.id.item_list_image);
            percentageView = view.findViewById(R.id.resident_info_item);
            optionsButton = view.findViewById(R.id.more_options_item_list);
        }

        public void bindGame(Game game) {
            optionsButton.setVisibility(View.GONE);
            String name = game.getName();
            String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);
            gameName.setText(formattedName);

            String resourceName = "logo_" + game.getName().toLowerCase().replace(" ", "_");
            int imageResId = itemView.getContext().getResources().getIdentifier(resourceName, "drawable", itemView.getContext().getPackageName());
            if (imageResId != 0)
                gameImage.setImageResource(imageResId);

            double percentage = weeklyPercentages.getOrDefault(game.getId(), 0.0);
            percentageView.setText(Math.round(percentage) + "%");
        }
    }
}
