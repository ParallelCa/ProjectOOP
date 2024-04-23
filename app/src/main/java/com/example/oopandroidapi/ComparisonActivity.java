package com.example.oopandroidapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComparisonActivity extends AppCompatActivity {
    private EditText editCity1, editCity2;
    private TextView txtCity1Info, txtCity2Info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        editCity1 = findViewById(R.id.editCity1);
        editCity2 = findViewById(R.id.editCity2);
        txtCity1Info = findViewById(R.id.txtCity1Info);
        txtCity2Info = findViewById(R.id.txtCity2Info);
        Button buttonCompare = findViewById(R.id.buttonCompare);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        buttonCompare.setOnClickListener(view -> {
            String city1 = editCity1.getText().toString();
            String city2 = editCity2.getText().toString();
            compareCities(city1, city2);
        });

        bottomNavigationView.setSelectedItemId(R.id.navigation_comparison);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.navigation_comparison) {

                return true;
            } else if (itemId == R.id.navigation_quiz) {

                startActivity(new Intent(this, QuizActivity.class));
                return true;
            }
            return false;
        });

    }

    private void compareCities(String city1, String city2) {
        SearchMunicipalityInformation(city1, txtCity1Info);
        SearchMunicipalityInformation(city2, txtCity2Info);
    }

    @SuppressLint("SetTextI18n")
    private void SearchMunicipalityInformation(String municipalityName, TextView infoTextView) {
        Context context = this;
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {

            MunicipalityDataRetriever municipalityDataRetriever = new MunicipalityDataRetriever();
            MunicipalityDataRetriever.getMunicipalityCodesMap();
            WeatherDataRetriever weatherDataRetriever = new WeatherDataRetriever();

            ArrayList<PopulationData> municipalityDataArrayList = municipalityDataRetriever.getPopulationData(context, municipalityName);
            if (municipalityDataArrayList == null) {
                runOnUiThread(() -> infoTextView.setText("No data available for " + municipalityName));
                return;
            }

            WorkData workData = municipalityDataRetriever.getWorkplaceAndEmploymentData(context, municipalityName);
            WeatherData weatherData = weatherDataRetriever.getData(municipalityName);

            runOnUiThread(() -> {
                PopulationData latestPopulationData = municipalityDataArrayList.get(municipalityDataArrayList.size() - 1);
                String displayText = municipalityName + "\n" +
                        "Population: " + latestPopulationData.getPopulation() + "\n" +
                        "Weather now: " + weatherData.getMain() + " (" + weatherData.getDescription() + ")" +
                        ", Temp: " + weatherData.getTemperature() + "°C\n" +
                        "Wind: " + weatherData.getWindSpeed() + " m/s\n";

                if (workData != null) {
                    displayText += "Self-sufficiency: " + workData.getSelfSufficiency().toString() +
                            ", Employment Rate: " + workData.getEmploymentRate().toString();
                } else {
                    displayText += "Workplace and employment data not available";
                }

                infoTextView.setText(displayText);
            });
        });
    }
}
