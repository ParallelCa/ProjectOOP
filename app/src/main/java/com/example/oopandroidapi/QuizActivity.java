package com.example.oopandroidapi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextView infoTextView;
    private TextView quizQuestionText;
    private final ArrayList<String> questions = new ArrayList<>();
    private final ArrayList<Boolean> answers = new ArrayList<>();
    private final ArrayList<Boolean> userAnswers = new ArrayList<>();
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        infoTextView = findViewById(R.id.info_text_view);
        quizQuestionText = findViewById(R.id.quiz_question_text);
        Button trueButton = findViewById(R.id.true_button);
        Button falseButton = findViewById(R.id.false_button);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupQuizQuestions();
        displayNextQuestion();

        trueButton.setOnClickListener(v -> recordAnswer(true));
        falseButton.setOnClickListener(v -> recordAnswer(false));

        SearchCitiesData();
        bottomNavigationView.setSelectedItemId(R.id.navigation_quiz);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_search) {
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            } else if (itemId == R.id.navigation_comparison) {

                startActivity(new Intent(this, ComparisonActivity.class));
                return true;

            } else return itemId == R.id.navigation_quiz;
        });
    }

    private void setupQuizQuestions() {
        SharedPreferences prefs = getSharedPreferences("MunicipalityData", MODE_PRIVATE);
        String[] cities = {"Tampere", "Kuopio", "Jyväskylä", "Turku", "Oulu", "Espoo", "Vantaa", "Lahti", "Pori", "Helsinki"};
        Random random = new Random();

        for (String city : cities) {
            // Retrieve data
            int population = prefs.getInt(city + "_population", 0);
            String employmentRate = prefs.getString(city + "_employmentRate", "0%");
            String selfSufficiency = prefs.getString(city + "_selfSufficiency", "0%");

            // Randomly select the type of data to ask about
            int questionType = random.nextInt(3); // 0 for population, 1 for employment rate, 2 for self-sufficiency
            String question;
            boolean correctAnswer;

            switch (questionType) {
                case 0: // Population
                    int popQuestionValue = population + (random.nextBoolean() ? random.nextInt(10000) : -random.nextInt(10000));
                    question = "Is the population of " + city + " over " + popQuestionValue + "?";
                    correctAnswer = population > popQuestionValue;
                    break;
                case 1: // Employment rate
                    double empRate = Double.parseDouble(employmentRate.replace("%", ""));
                    double empRateQuestionValue = empRate + (random.nextBoolean() ? random.nextDouble() * 20 : -random.nextDouble() * 20);
                    @SuppressLint("DefaultLocale") String formattedEmpRateQuestionValue = String.format("%.2f", empRateQuestionValue);
                    question = "Is the employment rate of " + city + " over " +  formattedEmpRateQuestionValue + "%?";
                    correctAnswer = empRate > empRateQuestionValue;
                    break;
                case 2: // Self-sufficiency
                    double selfSuff = Double.parseDouble(selfSufficiency.replace("%", ""));
                    double selfSuffQuestionValue = selfSuff + (random.nextBoolean() ? random.nextDouble() * 20 : -random.nextDouble() * 20);
                    @SuppressLint("DefaultLocale") String formattedSelfSuffQuestionValue = String.format("%.2f", selfSuffQuestionValue);
                    question = "Is the self-sufficiency of " + city + " over " + formattedSelfSuffQuestionValue + "%?";
                    correctAnswer = selfSuff > selfSuffQuestionValue;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + questionType);
            }

            questions.add(question);
            answers.add(correctAnswer);
        }
    }


    private void displayNextQuestion() {
        if (currentQuestionIndex < questions.size()) {
            quizQuestionText.setText(questions.get(currentQuestionIndex));
        } else {
            showResults();
        }
    }

    private void recordAnswer(boolean userAnswer) {
        userAnswers.add(userAnswer);
        currentQuestionIndex++;
        displayNextQuestion();
    }

    private void showResults() {
        StringBuilder resultBuilder = new StringBuilder();
        int correctAnswers = 0;

        for (int i = 0; i < questions.size(); i++) {
            resultBuilder.append("Q").append(i + 1).append(": ").append(questions.get(i))
                    .append("\nCorrect answer: ").append(answers.get(i) ? "True" : "False")
                    .append("\nYour answer: ").append(userAnswers.get(i) ? "True" : "False");

            if (answers.get(i).equals(userAnswers.get(i))) {
                correctAnswers++;
                resultBuilder.append(" - Correct\n\n");
            } else {
                resultBuilder.append(" - Wrong\n\n");
            }
        }
        resultBuilder.append("You answered ").append(correctAnswers).append(" out of ")
                .append(questions.size()).append(" questions correctly.");

        infoTextView.setText(resultBuilder.toString());}

    private void SearchCitiesData() {
        String[] cities = {"Tampere", "Kuopio", "Jyväskylä", "Turku", "Oulu", "Espoo", "Vantaa", "Lahti", "Pori", "Helsinki"};
        for (String city : cities) {
            SearchMunicipalityInformation(city);
        }
    }

    private void SearchMunicipalityInformation(String municipalityName) {
        executorService.execute(() -> {
            MunicipalityDataRetriever municipalityDataRetriever = new MunicipalityDataRetriever();
            MunicipalityDataRetriever.getMunicipalityCodesMap();
            ArrayList<PopulationData> municipalityDataArrayList = municipalityDataRetriever.getPopulationData(this, municipalityName);

            PopulationData populationData = municipalityDataArrayList.get(0);
            WorkData workData = municipalityDataRetriever.getWorkplaceAndEmploymentData(this, municipalityName);

            if (workData != null) {
                // Save data right after fetching it
                saveMunicipalityData(municipalityName, populationData, workData);


            }
        });
    }
    private void saveMunicipalityData(String cityName, PopulationData populationData, WorkData workData) {
        SharedPreferences sharedPreferences = getSharedPreferences("MunicipalityData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(cityName + "_population", populationData.getPopulation());
        editor.putInt(cityName + "_year", populationData.getYear());
        editor.putString(cityName + "_employmentRate", workData.getEmploymentRate().toString());
        editor.putString(cityName + "_selfSufficiency", workData.getSelfSufficiency().toString());
        editor.apply();
    }

}


