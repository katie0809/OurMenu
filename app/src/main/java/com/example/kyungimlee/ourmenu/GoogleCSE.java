package com.example.kyungimlee.ourmenu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by jungbae on 2017-11-12.
 */

public class GoogleCSE {

    final String baseUri = "https://www.googleapis.com/customsearch/v1?";
    final String CSE_id = "011555106316765250977:p02puvrjndo";
    final String CSE_key = "AIzaSyDamhMfHEKkmPTkAc3mq4FNpqBbr-OtFT4";

    public Bitmap getFoodImageCSE(String foodName){
        Bitmap bitmap = null;
        ArrayList<String> foodImgUriList = new ArrayList<String>();

        try {
            String encode_foodName = URLEncoder.encode(foodName, "UTF-8");
            String full_uri = baseUri + "key=" + CSE_key + "&q=" + encode_foodName + "&cx=" + CSE_id + "&alt=json";
            URL url = new URL(full_uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Mashape-Key", "sAcepFcMKomshyynyECAhHF6Y8kTp1jIcotjsnlWrissS3KMRQ");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                System.out.println("Connection Succeed // CSE");
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {  // 에러 발생
                System.out.println("Connection Fail // CSE");
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
            JSONArray retFoodArray = jsonObj.getJSONArray("items");

            for(int i = 0; i < retFoodArray.length(); i++) {
                JSONObject tempItem = retFoodArray.getJSONObject(i);
                JSONObject pagemap = tempItem.getJSONObject("pagemap");
                JSONArray cse_image = pagemap.getJSONArray("cse_image");

                for(int j = 0; j < cse_image.length(); j++) {
                    JSONObject tempCSE_image = cse_image.getJSONObject(j);
                    foodImgUriList.add(tempCSE_image.get("src").toString());
                }
            }

            URL food_img_url = new URL(foodImgUriList.get(0));
            URLConnection food_img_con = food_img_url.openConnection();
            food_img_con.connect();
            BufferedInputStream food_img_stream = new BufferedInputStream(food_img_con.getInputStream());
            bitmap = BitmapFactory.decodeStream(food_img_stream);
            food_img_stream.close();

        } catch (Exception e) {
            System.out.println("Error Occurred");
            e.printStackTrace();
        }

        return bitmap;
    }
}
