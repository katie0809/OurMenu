package com.example.kyungimlee.ourmenu;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by kyungimlee on 2017. 12. 6..
 */

class Spoonacular {

    private final String base_url_for_id = "https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/search?number=10&offset=0&query=";
    private final String base_url_for_recipe = "https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/";
    private final String spoon_key = "sAcepFcMKomshyynyECAhHF6Y8kTp1jIcotjsnlWrissS3KMRQ";
    private ArrayList<String> foodImgUrlList = new ArrayList<String>();
    private ArrayList<String> foodIngredients = new ArrayList<String>();
    private boolean isThereFoodData;

    public void getFoodInfoBySpoon(String food_txt) {
        try {
            JSONObject resultJson = getFoodIdBySpoon(food_txt);
            JSONArray foodIdArray = resultJson.getJSONArray("results");

            if(foodIdArray.length() == 0) {
                setThereFoodData(false);
                return;
            }

            setThereFoodData(true);
            String food_id = foodIdArray.getJSONObject(0).get("id").toString();
            String urlForIngre = base_url_for_recipe + food_id + "/information?includeNutrition=false";
            ArrayList<String> foodIngre = getFoodIngreBySpoon(urlForIngre);
            String foodImgBaseUrl = resultJson.get("baseUri").toString();

            ArrayList<String> temp_url_arrayList = new ArrayList<String>();
            for (int i = 0; i < foodIdArray.length(); i++) {
                JSONObject temp_food = foodIdArray.getJSONObject(i);
                String temp_food_url = temp_food.get("imageUrls").toString();

                temp_food_url = temp_food_url.replace("]", "");
                temp_food_url = temp_food_url.replace("[", "");
                temp_food_url = temp_food_url.replace("\"", "");

                temp_url_arrayList.add(foodImgBaseUrl + temp_food_url);
                /*
                List<String> temp_url_list = Arrays.asList(temp_food_url.split(","));

                for(int j = 0; j < temp_url_list.size(); j++) {
                    String full_str = foodImgBaseUrl + temp_url_list.get(i);
                    temp_url_list.set(i, full_str);
                }

                ArrayList<String> temp_url_arrayList = new ArrayList<String>(temp_url_list);
                setFoodImgUrlList(temp_url_arrayList);
                */
            }
            setFoodImgUrlList(temp_url_arrayList);
            setFoodIngredients(foodIngre);
        } catch (Exception e) {
            System.out.println("Spoonacular API Error Occurred");
            e.printStackTrace();
        }
    }

    private ArrayList<String> getFoodIngreBySpoon(String apiURL) {
        ArrayList<String> ret = new ArrayList<String>();
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Mashape-Key", spoon_key);
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                System.out.println("Spoonacular Connection Succeed");
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {  // 에러 발생
                System.out.println("Spoonacular Connection Fail");
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
            JSONArray ingreJsonArray = jsonObj.getJSONArray("extendedIngredients");

            for(int i = 0; i < ingreJsonArray.length(); i++) {
                JSONObject tempJO = ingreJsonArray.getJSONObject(i);
                ret.add(tempJO.get("name").toString());
            }
        } catch (Exception e) {
            System.out.println("Spoonacular Error Occurred");
            e.printStackTrace();
        }
        return ret;
    }

    private JSONObject getFoodIdBySpoon(String food_txt) {
        String fullUrlStr = base_url_for_id + food_txt;
        JSONObject jsonObj = null;
        try {
            URL url = new URL(fullUrlStr);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Mashape-Key", spoon_key);
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                System.out.println("Spoonacular Connection Succeed");
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {  // 에러 발생
                System.out.println("Spoonacular Connection Fail");
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), "UTF-8"));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            String result = response.toString();
            jsonObj = new JSONObject(result);
        } catch (Exception e) {
            System.out.println("Spoonacular API Error Occurred");
            e.printStackTrace();
        }

        return jsonObj;
    }

    public boolean isThereFoodData() {
        return isThereFoodData;
    }

    public void setThereFoodData(boolean thereFoodData) {
        isThereFoodData = thereFoodData;
    }

    public ArrayList<String> getFoodImgUrlList() {
        return foodImgUrlList;
    }

    public void setFoodImgUrlList(ArrayList<String> foodImgUrlList) {
        this.foodImgUrlList = foodImgUrlList;
    }

    public ArrayList<String> getFoodIngredients() {
        return foodIngredients;
    }

    public void setFoodIngredients(ArrayList<String> foodIngredients) {
        this.foodIngredients = foodIngredients;
    }
}
