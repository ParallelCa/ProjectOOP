package com.example.oopandroidapi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final List<String> historyList;
    private final LayoutInflater inflater;
    private final Context context;

    public HistoryAdapter(Context context, List<String> historyList) {
        this.inflater = LayoutInflater.from(context);
        this.historyList = historyList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String historyItem = historyList.get(position);
        holder.historyText.setText(historyItem);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BasicInformationActivity.class);
            intent.putExtra("municipality_name", historyItem);
            context.startActivity(intent);
        });
        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                historyList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                saveHistory();
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView historyText;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            historyText = itemView.findViewById(R.id.textViewHistoryItem);
            deleteButton = itemView.findViewById(R.id.buttonDeleteHistory);
        }
    }

    private void saveHistory() {
        SharedPreferences.Editor editor = context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE).edit();
        String savedHistory = String.join(",", historyList);
        editor.putString("history", savedHistory);
        editor.apply();
    }
}
