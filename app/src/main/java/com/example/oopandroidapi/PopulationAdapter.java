package com.example.oopandroidapi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class PopulationAdapter extends RecyclerView.Adapter<PopulationAdapter.ViewHolder> {

    private final ArrayList<PopulationData> dataList;

    public PopulationAdapter(ArrayList<PopulationData> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.population_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PopulationData data = dataList.get(position);
        String displayText = data.getYear() + ": " + data.getPopulation();
        holder.txtYearPopulation.setText(displayText);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtYearPopulation;

        public ViewHolder(View itemView) {
            super(itemView);
            txtYearPopulation = itemView.findViewById(R.id.txtYearPopulation);
        }
    }
}
