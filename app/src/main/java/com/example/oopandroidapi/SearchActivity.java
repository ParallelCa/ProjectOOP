package com.example.oopandroidapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText editMunicipalityName;
    private RecyclerView recyclerViewHistory;
    private HistoryAdapter adapter;
    private List<String> searchHistory = new ArrayList<>();
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editMunicipalityName = findViewById(R.id.editMunicipalityName);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        adapter = new HistoryAdapter(this, searchHistory);
        recyclerViewHistory.setAdapter(adapter);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));

        loadHistory();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_search) {

                    return true;
                } else if (itemId == R.id.navigation_comparison) {

                    startActivity(new Intent(this, ComparisonActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_quiz) {

                    startActivity(new Intent(this, QuizActivity.class));
                    return true;
                }
                return false;
            });

    }

    private void loadHistory() {
        SharedPreferences prefs = getSharedPreferences("SearchHistory", MODE_PRIVATE);
        String savedHistory = prefs.getString("history", "");
        if (!savedHistory.isEmpty()) {
            searchHistory.addAll(Arrays.asList(savedHistory.split(",")));
            adapter.notifyDataSetChanged();
        }
    }

    public void onSearchButtonClick(View view) {
        String municipalityName = editMunicipalityName.getText().toString();
        updateHistory(municipalityName);

        Intent intent = new Intent(this, BasicInformationActivity.class);
        intent.putExtra("municipality_name", municipalityName);
        startActivity(intent);

    }

    private void updateHistory(String newSearch) {
        if (searchHistory.contains(newSearch)) {
            searchHistory.remove(newSearch);
        }
        searchHistory.add(0, newSearch);
        if (searchHistory.size() > 5) {
            searchHistory.remove(searchHistory.size() - 1);
        }

        SharedPreferences.Editor editor = getSharedPreferences("SearchHistory", MODE_PRIVATE).edit();
        String savedHistory = String.join(",", searchHistory);
        editor.putString("history", savedHistory);
        editor.apply();

        adapter.notifyDataSetChanged();
    }


}
