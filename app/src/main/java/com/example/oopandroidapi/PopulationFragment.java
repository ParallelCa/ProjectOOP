package com.example.oopandroidapi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class PopulationFragment extends Fragment {

    private LineChart chart;

    public PopulationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_population_chart, container, false);
        chart = view.findViewById(R.id.chart);
        setupChart();
        return view;
    }

    private void setupChart() {

        ArrayList<PopulationData> populationDataArrayList = PopulationDataHolder.getInstance().getPopulationData();
        List<Entry> entries = new ArrayList<>();

        for (PopulationData data : populationDataArrayList) {
            entries.add(new Entry(data.getYear(), data.getPopulation()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Population Over Time");
        dataSet.setColor(getResources().getColor(R.color.colorPrimary)); // Assuming you have a color defined
        dataSet.setValueTextColor(getResources().getColor(R.color.colorAccent));

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh the chart
    }
}
