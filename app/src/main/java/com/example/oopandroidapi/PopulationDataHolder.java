package com.example.oopandroidapi;

import java.util.ArrayList;

public class PopulationDataHolder {
    private static PopulationDataHolder instance;
    private ArrayList<PopulationData> populationData;

    private PopulationDataHolder() {}

    public static PopulationDataHolder getInstance() {
        if (instance == null) {
            instance = new PopulationDataHolder();
        }
        return instance;
    }

    public ArrayList<PopulationData> getPopulationData() {
        return populationData;
    }

    public void setPopulationData(ArrayList<PopulationData> populationData) {
        this.populationData = populationData;
    }
    public boolean hasData() {
        return populationData != null && !populationData.isEmpty();
    }

}
