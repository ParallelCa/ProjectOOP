package com.example.oopandroidapi;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PopulationDetailsActivity extends AppCompatActivity {


    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.population_details);

        if (PopulationDataHolder.getInstance().hasData()) {

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            ArrayList<PopulationData> populationData = PopulationDataHolder.getInstance().getPopulationData();
            PopulationAdapter adapter = new PopulationAdapter(populationData);
            recyclerView.setAdapter(adapter);
        } else {

            TextView textView = findViewById(R.id.emptyView);
            textView.setVisibility(View.VISIBLE);
            textView.setText("No data available");
    } if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.chart, new PopulationFragment())
                    .commit();
    }}}
