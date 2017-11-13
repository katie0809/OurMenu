package com.example.kyungimlee.ourmenu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by JB. Ahn 2017.11.12
 */
public class ResultActivity extends AppCompatActivity {

    /**
     * txtView          : 검색된 음식이름
     * food_ingre       : 검색된 음식의 재료
     * food_img_view    : 검색된 음식의 사진
     * con_handler      : 검색으로 인한 UI 변경
     */
    TextView txtView;
    TextView food_ingre;
    ImageView food_img_view;
    Handler con_handler;
    Handler cse_handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        txtView = (TextView) findViewById(R.id.textView);
        food_ingre = (TextView) findViewById(R.id.food_ingre);
        food_img_view = (ImageView) findViewById(R.id.food_img);

        Intent myIntent = new Intent(this.getIntent());
        String inputText = myIntent.getStringExtra("inputText");
        String selectedLang = myIntent.getStringExtra("selectedLang");

        // 검색될 음식의 이름을 세팅하고
        // 음식 이름에 빈칸이 있으면 빈칸을 '+' 로 변경한다
        txtView.setText(inputText);
        final String food_txt = inputText.replaceAll(" ", "+");
        final String apiURLforImg = "https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/search?number=10&offset=0&query=" + food_txt;

        System.out.println("Initiating...");
        System.out.println("apiURL : " + apiURLforImg);

        Thread getFoodInfoThread = new Thread(){
            @Override
            public void run() {
                try {
                    JSONObject resultJson = getFoodInfo(apiURLforImg);
                    JSONArray food_arr = resultJson.getJSONArray("results");
                    Bundle foodInfoBundle = new Bundle();

                    boolean noData = true;
                    ArrayList<String> foodImgUriList = new ArrayList<String>();
                    String baseUri = new String();
                    String food_id;
                    ArrayList<String> food_ingre = new ArrayList<String>();

                    if (food_arr == null)
                        noData = true;
                    else {
                        noData = false;
                        food_id = food_arr.getJSONObject(0).get("id").toString();

                        String getIngreURL = "https://spoonacular-recipe-food-nutrition-v1.p.mashape.com/recipes/" +
                                food_id + "/information?includeNutrition=false";
                        food_ingre = getFoodIngredients(getIngreURL);
                        for (int i = 0; i < food_arr.length(); i++) {
                            JSONObject temp_food = food_arr.getJSONObject(i);
                            String temp_food_uri = temp_food.get("imageUrls").toString();

                            temp_food_uri = temp_food_uri.replace("]", "");
                            temp_food_uri = temp_food_uri.replace("[", "");
                            temp_food_uri = temp_food_uri.replace("\"", "");

                            List<String> temp_uri_list = Arrays.asList(temp_food_uri.split(","));

                            foodImgUriList.add(temp_uri_list.get(0));
                        }
                        baseUri = resultJson.get("baseUri").toString();

                        String full_uri = baseUri + foodImgUriList.get(0);
                        System.out.println("full_uri : " + full_uri);

                        URL food_img_url = new URL(full_uri);
                        URLConnection food_img_con = food_img_url.openConnection();
                        food_img_con.connect();
                        BufferedInputStream food_img_stream = new BufferedInputStream(food_img_con.getInputStream());
                        Bitmap food_img_bitmap = BitmapFactory.decodeStream(food_img_stream);
                        food_img_stream.close();

                        // foodImgConfig = food_img_bitmap.getConfig();
                        // final int bitmap_len = food_img_bitmap.getByteCount();
                        // ByteBuffer byteDst = ByteBuffer.allocate(bitmap_len);
                        // food_img_bitmap.copyPixelsToBuffer(byteDst);
                        // byte[] foodImgByteArr = byteDst.array();

                        byte[] foodImgByteArr = bitmapToByteArray(food_img_bitmap);

                        foodInfoBundle.putByteArray("foodImgByteArr", foodImgByteArr);
                        foodInfoBundle.putStringArrayList("foodIngre", food_ingre);
                    }

                    foodInfoBundle.putBoolean("noData", noData);
                    Message foodInfoMsg = con_handler.obtainMessage();
                    foodInfoMsg.setData(foodInfoBundle);
                    con_handler.sendMessage(foodInfoMsg);

                } catch(Exception e){
                    System.out.println("Error Occurred");
                    e.printStackTrace();
                }
            };
        };

        Thread CSE_Thread = new Thread() {
            @Override
            public void run() {
                Bundle foodInfoBundle = new Bundle();
                GoogleCSE googleCSE = new GoogleCSE();
                Bitmap foodImgBitmap = googleCSE.getFoodImageCSE(food_txt);
                boolean noData = true;

                if(foodImgBitmap == null) {
                    noData = true;
                }
                else {
                    noData = false;

                    byte[] foodImgByteArr = bitmapToByteArray(foodImgBitmap);
                    foodInfoBundle.putByteArray("foodImgByteArr", foodImgByteArr);
                }
                foodInfoBundle.putBoolean("noData", noData);
                Message foodInfoMsg = cse_handler.obtainMessage();
                foodInfoMsg.setData(foodInfoBundle);
                cse_handler.sendMessage(foodInfoMsg);
            };
        };

        System.out.println("food name : " + food_txt);
        System.out.println("selected Lang ; " + selectedLang);
        if(selectedLang.compareTo("English") == 0)
            getFoodInfoThread.start();
        else
            CSE_Thread.start();

        con_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle foodInfoBundle = msg.getData();
                if(foodInfoBundle.getBoolean("noData") == true) {
                    food_ingre.setText("No Food Data");
                }
                else if(foodInfoBundle.getBoolean("noData") == false) {
                    try {
                        byte[] foodImgByteArr = foodInfoBundle.getByteArray("foodImgByteArr");
                        ArrayList<String> foodIngre = foodInfoBundle.getStringArrayList("foodIngre");
                        String foodIngreString = "";

                        for(int i = 0; i < foodIngre.size(); i++) {
                            if(i != foodIngre.size() - 1)
                                foodIngreString += foodIngre.get(i) + ", ";
                            else
                                foodIngreString += foodIngre.get(i);
                        }
                        // Bitmap food_img_bitmap = BitmapFactory.decodeByteArray(foodImgByteArr, 0, foodImgByteArr.length);
                        // int width = foodInfoBundle.getInt("imgWidth");
                        // int height = foodInfoBundle.getInt("imgHeight");
                        // Bitmap food_img_bitmap = Bitmap.createBitmap(width, height, foodImgConfig);
                        // ByteBuffer buffers = ByteBuffer.wrap()

                        Bitmap food_img_bitmap = byteArrayToBitmap(foodImgByteArr);

                        // System.out.println("byte len : " + foodImgByteArr.length);
                        // System.out.println("bitmap height : " + food_img_bitmap.getHeight() + ", bitmap width : " + food_img_bitmap.getWidth());
                        food_img_view.setImageBitmap(food_img_bitmap);
                        food_ingre.setText(foodIngreString);
                    } catch (Exception e) {
                        System.out.println("Error Occurred");
                        e.printStackTrace();
                    }
                }
            }
        };

        cse_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle foodInfoBundle = msg.getData();
                if(foodInfoBundle.getBoolean("noData") == true) {
                    food_ingre.setText("No Food Data");
                }
                else if(foodInfoBundle.getBoolean("noData") == false) {
                    try {
                        byte[] foodImgByteArr = foodInfoBundle.getByteArray("foodImgByteArr");
                        Bitmap food_img_bitmap = byteArrayToBitmap(foodImgByteArr);
                        food_img_view.setImageBitmap(food_img_bitmap);
                        food_ingre.setText("Searched by Google Custom Search Engine");
                    } catch (Exception e) {
                        System.out.println("Error Occurred");
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public Bitmap byteArrayToBitmap(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        return bitmap;
    }

    public JSONObject getFoodInfo(String apiURL) {
        JSONObject jsonObj = null;
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Mashape-Key", "sAcepFcMKomshyynyECAhHF6Y8kTp1jIcotjsnlWrissS3KMRQ");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                System.out.println("Connection Succeed");
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {  // 에러 발생
                System.out.println("Connection Fail");
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
            System.out.println("Error Occurred");
            e.printStackTrace();
        }

        return jsonObj;
    }

    public ArrayList<String> getFoodIngredients(String apiURL) {
        ArrayList<String> ret = new ArrayList<String>();
        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Mashape-Key", "sAcepFcMKomshyynyECAhHF6Y8kTp1jIcotjsnlWrissS3KMRQ");
            con.setRequestProperty("Accept", "application/json");
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) { // 정상 호출
                System.out.println("Connection Succeed");
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            } else {  // 에러 발생
                System.out.println("Connection Fail");
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
            System.out.println("Error Occurred");
            e.printStackTrace();
        }
        return ret;
    }
}
