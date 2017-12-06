package com.example.kyungimlee.ourmenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kyungimlee on 2017. 12. 6..
 */

class Yummly {

    private final String API_ID = "932af2e3";
    private final String API_KEY = "94cedc52515210d5649aae1621230364";
    private final String API_BASE_URL = "https://api.yummly.com/v1/api/";
    private String foodImgUrl = new String();
    private ArrayList<String> foodIngredients = new ArrayList<String>();
    private String attr_url;
    private String attr_text;
    private String attr_srcName;
    private String attr_srcUrl;
    private boolean isThereYummlyData;
    private boolean isThereFlavors = false;
    private double bitter;
    private double meaty;
    private double piquant;
    private double salty;
    private double sour;
    private double sweet;

    public void getFoodInfoByYummly(String food_text) {
        String food_id = getFoodId(food_text);

        if(food_id.compareTo("noFood") == 0) {
            setThereYummlyData(false);
            return;
        }
        else {
            try {
                setThereYummlyData(true);
                String encodedFoodId = URLEncoder.encode(food_id, "UTF-8");
                String reqestFullUrl = API_BASE_URL + "recipe/" + encodedFoodId;

                URL url = new URL(reqestFullUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Yummly-App-ID", API_ID);
                con.setRequestProperty("X-Yummly-App-Key", API_KEY);

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    System.out.println("Yummly Connection Succeed");
                    br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                } else {  // 에러 발생
                    System.out.println("Yummly Connection Fail");
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
                }

                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                String result = response.toString();
                JSONObject jsonObj = new JSONObject(result);

                JSONArray imagesJsonArray = jsonObj.getJSONArray("images");
                JSONObject imagesJsonObj = imagesJsonArray.getJSONObject(0);
                foodImgUrl = imagesJsonObj.getString("hostedLargeUrl");

                String ingredientsStr = jsonObj.get("ingredientLines").toString();
                ingredientsStr = ingredientsStr.replace("[", "");
                ingredientsStr = ingredientsStr.replace("]", "");
                ingredientsStr = ingredientsStr.replace("\"", "");
                ingredientsStr = ingredientsStr.replace("\\", "");
                List<String> tempList = Arrays.asList(ingredientsStr.split(","));
                ArrayList<String> ingredientsList = new ArrayList<String>(tempList);
                foodIngredients.addAll(ingredientsList);

                JSONObject flavors = jsonObj.getJSONObject("flavors");

                if(flavors.length() > 0) {
                    setThereFlavors(true);
                    setBitter(flavors.getDouble("Bitter"));
                    setMeaty(flavors.getDouble("Meaty"));
                    setPiquant(flavors.getDouble("Piquant"));
                    setSalty(flavors.getDouble("Salty"));
                    setSour(flavors.getDouble("Sour"));
                    setSweet(flavors.getDouble("Sweet"));
                }

                JSONObject attrJson = jsonObj.getJSONObject("attribution");
                attr_text = attrJson.getString("text");
                attr_url = attrJson.getString("url");

                JSONObject attrSrcJson = jsonObj.getJSONObject("source");
                attr_srcName = attrSrcJson.getString("sourceDisplayName");
                attr_srcUrl = attrSrcJson.getString("sourceRecipeUrl");
            } catch (Exception e) {
                System.out.println("Yummly Error Occurred");
                e.printStackTrace();
            }
        }
    }

    private String getFoodId(String food_text) {
        String ret = new String();
        try {
            String encodedFoodText = URLEncoder.encode(food_text, "UTF-8");
            String reqestFullUrl = API_BASE_URL + "recipes?q=" + encodedFoodText;

            URL url = new URL(reqestFullUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Yummly-App-ID", API_ID);
            con.setRequestProperty("X-Yummly-App-Key", API_KEY);

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                System.out.println("Yummly Connection Succeed");
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {  // 에러 발생
                System.out.println("Yummly Connection Fail");
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
            }

            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            String result = response.toString();
            JSONObject jsonObj = new JSONObject(result);

            JSONArray returnedFoodArray = jsonObj.getJSONArray("matches");

            if(returnedFoodArray.length() > 0) {
                JSONObject first_food = returnedFoodArray.getJSONObject(0);
                for(int i = 1; i < returnedFoodArray.length(); i++) {
                    if(first_food.getString("flavors").compareTo("null") == 0)
                        first_food = returnedFoodArray.getJSONObject(i);
                    else
                        break;
                }
                ret = first_food.getString("id");
            }
            else {
                ret = "noFood";
            }
        } catch(Exception e) {
            System.out.println("Yummly Error Occurred");
            e.printStackTrace();
        }

        return ret;
    }


    public boolean isThereFlavors() {
        return isThereFlavors;
    }

    public void setThereFlavors(boolean thereFlavors) {
        isThereFlavors = thereFlavors;
    }

    public String getAttr_url() {
        return attr_url;
    }

    public void setAttr_url(String attr_url) {
        this.attr_url = attr_url;
    }

    public String getAttr_text() {
        return attr_text;
    }

    public void setAttr_text(String attr_text) {
        this.attr_text = attr_text;
    }

    public String getAttr_srcName() {
        return attr_srcName;
    }

    public void setAttr_srcName(String attr_srcName) {
        this.attr_srcName = attr_srcName;
    }

    public String getAttr_srcUrl() {
        return attr_srcUrl;
    }

    public void setAttr_srcUrl(String attr_srcUrl) {
        this.attr_srcUrl = attr_srcUrl;
    }

    public boolean isThereYummlyData() {
        return isThereYummlyData;
    }

    public void setThereYummlyData(boolean thereYummlyData) {
        isThereYummlyData = thereYummlyData;
    }

    public String getFoodImgUrl() {
        return foodImgUrl;
    }

    public void setFoodImgUrl(String foodImgUrl) {
        this.foodImgUrl = foodImgUrl;
    }

    public ArrayList<String> getFoodIngredients() {
        return foodIngredients;
    }

    public void setFoodIngredients(ArrayList<String> foodIngredients) {
        this.foodIngredients = foodIngredients;
    }

    public double getBitter() {
        return bitter;
    }

    public void setBitter(double bitter) {
        this.bitter = bitter;
    }

    public double getMeaty() {
        return meaty;
    }

    public void setMeaty(double meaty) {
        this.meaty = meaty;
    }

    public double getPiquant() {
        return piquant;
    }

    public void setPiquant(double piquant) {
        this.piquant = piquant;
    }

    public double getSalty() {
        return salty;
    }

    public void setSalty(double salty) {
        this.salty = salty;
    }

    public double getSour() {
        return sour;
    }

    public void setSour(double sour) {
        this.sour = sour;
    }

    public double getSweet() {
        return sweet;
    }

    public void setSweet(double sweet) {
        this.sweet = sweet;
    }
}
