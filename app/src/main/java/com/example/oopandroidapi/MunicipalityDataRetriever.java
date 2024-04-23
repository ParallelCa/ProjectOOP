package com.example.oopandroidapi;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MunicipalityDataRetriever {

    static ObjectMapper objectMapper = new ObjectMapper();

    static HashMap<String, String> municipalityNamesToCodesMap = null;

    /**
     * Get municipality codes, we need to do this only once
     *
     */
    public static HashMap<String, String> getMunicipalityCodesMap() {
        if (municipalityNamesToCodesMap == null) {
            JsonNode areas = readAreaDataFromTheAPIURL(objectMapper);
            municipalityNamesToCodesMap = createMunicipalityNamesToCodesMap(areas);
        }
        return municipalityNamesToCodesMap;
    }

    public WorkData getWorkplaceAndEmploymentData(Context context, String municipalityName) {
        String code = municipalityNamesToCodesMap.get(municipalityName);
        WorkData workData = new WorkData();

        try {

            JsonNode jsonQuerySelfSufficiency = objectMapper.readTree(context.getResources().openRawResource(R.raw.workplaceselfsufficiencyquery));
            ((ObjectNode) jsonQuerySelfSufficiency.findValue("query").get(1).get("selection")).putArray("values").add(code);
            HttpURLConnection conSelfSufficiency = connectToAPIAndSendPostRequest(objectMapper, jsonQuerySelfSufficiency, new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/en/StatFin/tyokay/statfin_tyokay_pxt_125s.px"));
            StringBuilder responseSelfSufficiency = readApiResponse(conSelfSufficiency);
            JsonNode workAndEmploymentDataSelfSufficiency = objectMapper.readTree(responseSelfSufficiency.toString());
            JsonNode valueSelfSufficiency = workAndEmploymentDataSelfSufficiency.get("value");
            workData.setSelfSufficiency(new BigDecimal(valueSelfSufficiency.get(0).toString()).setScale(2));


            JsonNode jsonQueryEmploymentRate = objectMapper.readTree(context.getResources().openRawResource(R.raw.employmentratequery));
            ((ObjectNode) jsonQueryEmploymentRate.findValue("query").get(0).get("selection")).putArray("values").add(code);
            HttpURLConnection conEmploymentRate = connectToAPIAndSendPostRequest(objectMapper, jsonQueryEmploymentRate, new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/en/StatFin/tyokay/statfin_tyokay_pxt_115x.px"));
            StringBuilder responseEmploymentRate = readApiResponse(conEmploymentRate);
            JsonNode workAndEmploymentDataEmploymentRate = objectMapper.readTree(responseEmploymentRate.toString());
            JsonNode valueEmploymentRate = workAndEmploymentDataEmploymentRate.get("value");
            workData.setEmploymentRate(new BigDecimal(valueEmploymentRate.get(0).toString()).setScale(2));

            return workData;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private StringBuilder readApiResponse(HttpURLConnection con) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            Log.d("API Response", response.toString());
            return response;
        }
    }

    public ArrayList<PopulationData> getPopulationData(Context context, String municipalityName) {
        //System.out.println(municipalityNamesToCodesMap);

        String code = municipalityNamesToCodesMap.get(municipalityName);


        try {

            JsonNode jsonQuery = objectMapper.readTree(context.getResources().openRawResource(R.raw.populationquery));

            ((ObjectNode) jsonQuery.findValue("query").get(0).get("selection")).putArray("values").add(code);


            HttpURLConnection con = connectToAPIAndSendPostRequest(objectMapper, jsonQuery, new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/en/StatFin/synt/statfin_synt_pxt_12dy.px"));


            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JsonNode municipalityData = objectMapper.readTree(response.toString());

                ArrayList<String> years = new ArrayList<>();
                JsonNode populations = null;

                for (JsonNode node : municipalityData.get("dimension").get("Vuosi")
                        .get("category").get("label")) {
                    years.add(node.asText());
                }

                populations = municipalityData.get("value");

                ArrayList<PopulationData> populationData = new ArrayList<>();


                for (int i = 0; i < populations.size(); i++) {
                    int population = populations.get(i).asInt();
                    populationData.add(new PopulationData(Integer.parseInt(years.get(i)), population));
                }


                return populationData;

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return null;
    }


    private static HttpURLConnection connectToAPIAndSendPostRequest(ObjectMapper objectMapper, JsonNode jsonQuery, URL url)
            throws MalformedURLException, IOException, ProtocolException, JsonProcessingException {

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = objectMapper.writeValueAsBytes(jsonQuery);
            os.write(input, 0, input.length);
        }
        return con;
    }



    private static HashMap<String, String> createMunicipalityNamesToCodesMap(JsonNode areas) {
        JsonNode codes = null;
        JsonNode names = null;

        // Here we find the element "variables", and inside it we have the element "text", that has value "Area".
        // Within the same element, we have the keys "values" which contains the municipality codes (e.g. KU123) as a list
        // and "valueTexts" which contains the municipality names (e.g. Lahti) as a list
        for (JsonNode node : areas.findValue("variables")) {
            if (node.findValue("text").asText().equals("Area")) {
                codes = node.findValue("values");
                names = node.findValue("valueTexts");
            }
        }

        // Let's store the municipality names as keys, and municipality codes as values in a HashMap

        HashMap<String, String> municipalityNamesToCodesMap = new HashMap<>();

        // Here we can assume that the size of names and codes are equal, at there are as many municipality codes
        // as there are municipality names
        for (int i = 0; i < Objects.requireNonNull(names).size(); i++) {
            String name = names.get(i).asText();
            String code = codes.get(i).asText();
            municipalityNamesToCodesMap.put(name, code);

        }
        return municipalityNamesToCodesMap;
    }


    /**
     * Here we read the all the JSON from the URL to a JsonNode
     * <p>
     * How to improve this: instead of fetching the same data all over again when restarting the app, we could store
     * the areas JSON to a file and read it from there. Then we would only need to fetch it once, if the file does
     * not yet exist.
     *
     * @return JsonNode with municipality data
     */
    private static JsonNode readAreaDataFromTheAPIURL(ObjectMapper objectMapper) {
        JsonNode areas = null;
        try {
            areas = objectMapper.readTree(new URL("https://pxdata.stat.fi:443/PxWeb/api/v1/en/StatFin/synt/statfin_synt_pxt_12dy.px"));


        } catch (IOException e) {
            e.printStackTrace();
        }
        return areas;
    }


}
