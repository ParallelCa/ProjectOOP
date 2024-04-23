package com.example.oopandroidapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class BasicInformationActivity extends AppCompatActivity {
    private TextView txtPopulation;
    private TextView txtWeather;
    private TextView txtWork;
    private ImageView imageWeatherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_information);

        txtPopulation = findViewById(R.id.txtPopulation);
        txtWeather = findViewById(R.id.txtWeather);
        txtWork = findViewById(R.id.txtWork);
        imageWeatherIcon = findViewById(R.id.imageWeatherIcon);
        Button buttonShowNewActivity = findViewById(R.id.button2);
        String municipalityName = getIntent().getStringExtra("municipality_name");
        SearchMunicipalityInformation(municipalityName);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_container, new MapFragment())
                    .commit();
        }
        buttonShowNewActivity.setOnClickListener(view -> {
            Intent intent = new Intent(BasicInformationActivity.this, PopulationDetailsActivity.class);
            intent.putExtra("municipality_name", municipalityName);
            startActivity(intent);
        });

    }

    @SuppressLint("SetTextI18n")
    private void SearchMunicipalityInformation(String municipalityName) {
        Context context = this;
        MunicipalityDataRetriever municipalityDataRetriever = new MunicipalityDataRetriever();
        WeatherDataRetriever weatherDataRetriever = new WeatherDataRetriever();

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            MunicipalityDataRetriever.getMunicipalityCodesMap();
            ArrayList<PopulationData> municipalityDataArrayList = municipalityDataRetriever.getPopulationData(context, municipalityName);
            PopulationDataHolder.getInstance().setPopulationData(municipalityDataArrayList);
            if (municipalityDataArrayList == null) {
                return;
            }

            WorkData workData = municipalityDataRetriever.getWorkplaceAndEmploymentData(context, municipalityName);
            WeatherData weatherData = weatherDataRetriever.getData(municipalityName);
            MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_container);
            assert mapFragment != null;
            mapFragment.updateMapLocation(Double.parseDouble(weatherData.getLatitude()), Double.parseDouble(weatherData.getLongitude()), "Marker in " + weatherData.getName());
            runOnUiThread(() -> {
                if (!municipalityDataArrayList.isEmpty()) {
                    PopulationData latestPopulationData = municipalityDataArrayList.get(municipalityDataArrayList.size() - 1);
                    String latestDataString = "Population: " + latestPopulationData.getPopulation();
                    txtPopulation.setText(latestDataString);
                }

                String weatherDataAsString = weatherData.getName() + "\n" +
                        "Weather now: " + weatherData.getMain() + "(" + weatherData.getDescription() + ")\n" +
                        "Temperature: " + weatherData.getTemperature() + "\n" +
                        "Wind speed: " + weatherData.getWindSpeed();
                txtWeather.setText(weatherDataAsString);
                String iconCode = weatherData.getIcon();
                String imageUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                Picasso.get().load(imageUrl).into(imageWeatherIcon);
                if (workData != null) {
                    String workDataAsString = "Self-sufficiency: " + workData.getSelfSufficiency().toString() +
                            "\nEmployment Rate: " + workData.getEmploymentRate().toString();
                    txtWork.setText(workDataAsString);
                } else {
                    txtWork.setText("Workplace and employment data not available");
                }
            });
        });
    }
}
