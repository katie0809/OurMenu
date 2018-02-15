package com.uos.kyungimlee.ourmenu;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.translate.model.TranslationsResource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FoodInfoActivity_v1 extends AppCompatActivity {

    TextView foodNameTextView;
    TextView nutrientsTextView;
    TextView foodContextView;
    TextView allergenContextView;

    TextView nutrientsTitleTextView;
    TextView flavorsTitleTextView;
    TextView ingreTitleTextView;
    TextView allergenTitleTextView;
    TextView poweredByTitleTextView;
    TextView dataFromTitleTextView;
    TextView hasFlavorTextView;
    TextView fatsecretAttrTextView;
    TextView yummlyAttrRecipeTextView;
    TextView yummlyAttrRecipeUrlTextView;
    TextView yummlyAttrSrcTextView;
    TextView yummlyAttrSrcUrlTextView;

    ViewPager foodImgViewPager;

    TextView bitterTextView;
    TextView meatyTextView;
    TextView piquantTextView;
    TextView saltyTextView;
    TextView sourTextView;
    TextView sweetTextView;

    ProgressBar bitterPB;
    ProgressBar meatyPB;
    ProgressBar piquantPB;
    ProgressBar saltyPB;
    ProgressBar sourPB;
    ProgressBar sweetPB;

    Handler spoon_handler;
    Handler cse_handler;
    Handler fatsecret_handler;
    Handler yummly_handler;

    String food_txt_noSpace = new String();
    String food_txt_withSpace = new String();
    String originInputText = new String();
    String selectedLang = new String();
    ArrayList<String> foodImgUrlList = new ArrayList<String>();
    ArrayList<String> spoonsFoodIngreList = new ArrayList<String>();
    ArrayList<String> yummlyFoodIngreList = new ArrayList<String>();

    AllergenIngreList myAllergenList;

    String fatsecretHomepageURL = "http://platform.fatsecret.com";
    String yummlyAttrRecipeURL;
    String yummlyAttrText;
    String yummlyAttrSrcName;
    String yummlyAttrSrcURL;

    FoodInfoContents myFoodInfoContents;
    boolean menu_activated = false;
    private static final String API_KEY = "AIzaSyDON76sNwTcC2AuXU2L_y31z7BtHYP74Ko";
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_info_v1);

        Intent myIntent = new Intent(this.getIntent());
        String inputText = myIntent.getStringExtra("inputText");
        originInputText = myIntent.getStringExtra("originText");

        food_txt_noSpace = inputText.replaceAll(" ", "+");
        food_txt_withSpace = inputText;
        selectedLang = myIntent.getStringExtra("selectedLang");

        allergenContextView = (TextView) findViewById(R.id.allergen_textView);
        allergenTitleTextView = (TextView) findViewById(R.id.allergenTitleTextView);
        nutrientsTitleTextView = (TextView) findViewById(R.id.nutrientsTitleTextView);
        flavorsTitleTextView = (TextView) findViewById(R.id.flavorsTitleTextView);
        ingreTitleTextView = (TextView) findViewById(R.id.ingreTitleTextView);
        poweredByTitleTextView = (TextView) findViewById(R.id.poweredByTitleTextView);
        dataFromTitleTextView = (TextView) findViewById(R.id.dataFromTitleTextView);

        hasFlavorTextView = (TextView) findViewById(R.id.hasFlavorTextView);
        foodNameTextView = (TextView) findViewById(R.id.food_name_textView);
        nutrientsTextView = (TextView) findViewById(R.id.nutrients_textView);
        foodContextView = (TextView) findViewById(R.id.ingredients_textView);
        foodImgViewPager = (ViewPager) findViewById(R.id.food_img_viewPager);

        fatsecretAttrTextView = (TextView) findViewById(R.id.fatsecretUrlTextView);
        yummlyAttrRecipeTextView = (TextView) findViewById(R.id.yummlyRecipeTextView);
        yummlyAttrRecipeUrlTextView = (TextView) findViewById(R.id.yummlyRecipeUrlTextView);
        yummlyAttrSrcTextView = (TextView) findViewById(R.id.yummlySrcTextView);
        yummlyAttrSrcUrlTextView = (TextView) findViewById(R.id.yummlySrcUrlTextView);

        bitterTextView = (TextView) findViewById(R.id.bitter_textView);
        meatyTextView = (TextView) findViewById(R.id.meaty_textView);
        piquantTextView = (TextView) findViewById(R.id.piquant_textView);
        saltyTextView = (TextView) findViewById(R.id.salty_textView);
        sourTextView = (TextView) findViewById(R.id.sour_textView);
        sweetTextView = (TextView) findViewById(R.id.sweet_textView);

        bitterPB = (ProgressBar) findViewById(R.id.bitter_progressBar);
        meatyPB = (ProgressBar) findViewById(R.id.meaty_progressBar);
        piquantPB = (ProgressBar) findViewById(R.id.piquant_progressBar);
        saltyPB = (ProgressBar) findViewById(R.id.salty_progressBar);
        sourPB = (ProgressBar) findViewById(R.id.sour_progressBar);
        sweetPB = (ProgressBar) findViewById(R.id.sweet_progressBar);

        myAllergenList = new AllergenIngreList();

        Spanned fatsecretLink = Html.fromHtml("<a href=\"http://platform.fatsecret.com\">Powered by FatSecret</a>");
        fatsecretAttrTextView.setMovementMethod(LinkMovementMethod.getInstance());
        fatsecretAttrTextView.setText(fatsecretLink);

        myFoodInfoContents = new FoodInfoContents();
        myFoodInfoContents.setOriginFoodName(originInputText);

        CustomAdapter foodImgAdapter = new CustomAdapter(this, getLayoutInflater());

        System.out.println("food name : " + food_txt_noSpace);
        System.out.println("selected Lang ; " + selectedLang);

        fatsecret_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle foodInfoBundle = msg.getData();
                Boolean isThereFoodData = foodInfoBundle.getBoolean("isThereFoodData");

                if(isThereFoodData) {
                    String foodDescription = foodInfoBundle.getString("foodDescription");
                    nutrientsTextView.setText(foodDescription);
                }
                else {
                    nutrientsTextView.setText("No Nutrient Information");
                }
            }
        };

        Thread Fatsercret_Thread = new Thread() {
            @Override
            public void run() {
                FatSecretSearch myFatsecretSearch = new FatSecretSearch();
                FatSecretGet myFatsecretGet = new FatSecretGet();
                int page = 0;
                boolean isThereFoodData = false;

                Bundle foodInfoBundle = new Bundle();

                try {
                    JSONObject foodSearchRet = myFatsecretSearch.searchFood(food_txt_withSpace, page);
                    JSONArray searchedFoodArr = foodSearchRet.getJSONArray("food");

                    if (searchedFoodArr.length() > 0) {
                        isThereFoodData = true;
                        JSONObject temp_food = searchedFoodArr.getJSONObject(0);
                        String foodDescription = temp_food.getString("food_description");

                        foodInfoBundle.putString("foodDescription", foodDescription);
                        myFoodInfoContents.setOriginNutrientsContext(foodDescription);
                        // Long temp_food_id = temp_food.getLong("food_id");
                        // JSONObject foodGetRet = myFatsecretGet.getFood(temp_food_id);
                    }
                    else
                        myFoodInfoContents.setOriginNutrientsContext("There are not nutrients information");

                } catch (Exception e) {
                    System.out.println("Fatsecret Thread Error Occurred");
                    e.printStackTrace();
                }

                foodInfoBundle.putBoolean("isThereFoodData", isThereFoodData);
                Message foodInfoMsg = fatsecret_handler.obtainMessage();
                foodInfoMsg.setData(foodInfoBundle);
                fatsecret_handler.sendMessage(foodInfoMsg);
            }
        };

        yummly_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle foodInfoBundle = msg.getData();
                Boolean isThereFoodData = foodInfoBundle.getBoolean("isThereFoodData");

                if(isThereFoodData) {
                    double bitter = foodInfoBundle.getDouble("bitter");
                    double meaty = foodInfoBundle.getDouble("meaty");
                    double piquant = foodInfoBundle.getDouble("piquant");
                    double salty = foodInfoBundle.getDouble("salty");
                    double sour = foodInfoBundle.getDouble("sour");
                    double sweet = foodInfoBundle.getDouble("sweet");

                    if(foodInfoBundle.getBoolean("isThereFlavors")) {
                        bitterPB.setProgress((int) (bitter * 100));
                        meatyPB.setProgress((int) (meaty * 100));
                        piquantPB.setProgress((int) (piquant * 100));
                        saltyPB.setProgress((int) (salty * 100));
                        sourPB.setProgress((int) (sour * 100));
                        sweetPB.setProgress((int) (sweet * 100));
                    }
                    else {
                        hasFlavorTextView.setText("There are not Flavor Info");
                    }

                    String attrUrlStr = "<a href=\"" + foodInfoBundle.getString("attrUrl") + "\">" + foodInfoBundle.getString("attrUrl") + "</a>";
                    String attrSrcUrlStr = "<a href=\"" + foodInfoBundle.getString("attrSrcUrl") + "\">" + foodInfoBundle.getString("attrSrcUrl") + "</a>";
                    Spanned attrUrl = Html.fromHtml(attrUrlStr);
                    Spanned attrSrcUrl = Html.fromHtml(attrSrcUrlStr);

                    yummlyAttrRecipeUrlTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    yummlyAttrRecipeUrlTextView.setText(attrUrl);

                    yummlyAttrSrcUrlTextView.setMovementMethod(LinkMovementMethod.getInstance());
                    yummlyAttrSrcUrlTextView.setText(attrSrcUrl);

                    yummlyAttrRecipeTextView.setText(foodInfoBundle.getString("attrText"));
                    yummlyAttrSrcTextView.setText(foodInfoBundle.getString("attrSrcUrl"));
                }
                else {
                    bitterPB.setProgress(0);
                    meatyPB.setProgress(0);
                    piquantPB.setProgress(0);
                    saltyPB.setProgress(0);
                    sourPB.setProgress(0);
                    sweetPB.setProgress(0);

                    yummlyAttrRecipeUrlTextView.setText("");
                    yummlyAttrSrcUrlTextView.setText("");
                    yummlyAttrRecipeTextView.setText("");
                    yummlyAttrSrcTextView.setText("");
                }
            }
        };

        Thread Yummly_Thread = new Thread() {
            @Override
            public void run() {
                Yummly myYummly = new Yummly();
                Boolean isThereFoodData;

                myYummly.getFoodInfoByYummly(food_txt_withSpace);

                Bundle foodInfoBundle = new Bundle();

                if(myYummly.isThereYummlyData()) {
                    isThereFoodData = true;
                    String yummlyImage = myYummly.getFoodImgUrl();
                    foodImgUrlList.add(yummlyImage);

                    if(myYummly.isThereFlavors()) {
                        foodInfoBundle.putDouble("bitter", myYummly.getBitter());
                        foodInfoBundle.putDouble("meaty", myYummly.getMeaty());
                        foodInfoBundle.putDouble("piquant", myYummly.getPiquant());
                        foodInfoBundle.putDouble("salty", myYummly.getSalty());
                        foodInfoBundle.putDouble("sour", myYummly.getSour());
                        foodInfoBundle.putDouble("sweet", myYummly.getSweet());
                    }

                    foodInfoBundle.putBoolean("isThereFlavors", myYummly.isThereFlavors());
                    foodInfoBundle.putString("attrUrl", myYummly.getAttr_url());
                    foodInfoBundle.putString("attrText", myYummly.getAttr_text());
                    foodInfoBundle.putString("attrSrcName", myYummly.getAttr_srcName());
                    foodInfoBundle.putString("attrSrcUrl", myYummly.getAttr_srcUrl());

                    yummlyFoodIngreList.addAll(myYummly.getFoodIngredients());
                }
                else
                    isThereFoodData = false;

                foodInfoBundle.putBoolean("isThereFoodData", isThereFoodData);
                Message foodInfoMsg = yummly_handler.obtainMessage();
                foodInfoMsg.setData(foodInfoBundle);
                yummly_handler.sendMessage(foodInfoMsg);
            }
        };

        spoon_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle foodInfoBundle = msg.getData();
                ArrayList<String> spoons_imgUrlList = foodInfoBundle.getStringArrayList("imgUrlList");
                ArrayList<String> spoons_foodContextList = foodInfoBundle.getStringArrayList("contextList");

                foodImgUrlList.addAll(spoons_imgUrlList);
                spoonsFoodIngreList.addAll(spoons_foodContextList);
            }
        };

        Thread Spoonacular_Thread = new Thread() {
            @Override
            public void run() {
                Spoonacular spoons = new Spoonacular();
                spoons.getFoodInfoBySpoon(food_txt_noSpace);

                if(spoons.isThereFoodData()) {
                    ArrayList<String> spoons_imgUrlList = spoons.getFoodImgUrlList();
                    ArrayList<String> spoons_foodContextList = spoons.getFoodIngredients();

                    foodImgUrlList.addAll(spoons_imgUrlList);
                    spoonsFoodIngreList.addAll(spoons_foodContextList);
                }
                /*
                Bundle foodInfoBundle = new Bundle();

                foodInfoBundle.putStringArrayList("imgUrlList", spoons_imgUrlList);
                foodInfoBundle.putStringArrayList("contextList", spoons_foodContextList);
                Message foodInfoMsg = spoon_handler.obtainMessage();
                foodInfoMsg.setData(foodInfoBundle);
                spoon_handler.sendMessage(foodInfoMsg);
                */
            };
        };

        cse_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle foodInfoBundle = msg.getData();
                ArrayList<String> cse_imgUrlList = foodInfoBundle.getStringArrayList("imgUrlList");

                foodImgUrlList.addAll(cse_imgUrlList);
            }
        };

        Thread CSE_Thread = new Thread() {
            public void run() {
                GoogleCSE googleCSE = new GoogleCSE();
                ArrayList<String> cse_imgUrlList = googleCSE.getFoodImgUrlByCSE(originInputText);

                foodImgUrlList.addAll(cse_imgUrlList);
                /*
                Bundle foodInfoBundle = new Bundle();

                foodInfoBundle.putStringArrayList("imgUrlList", cse_imgUrlList);
                Message foodInfoMsg = cse_handler.obtainMessage();
                foodInfoMsg.setData(foodInfoBundle);
                cse_handler.sendMessage(foodInfoMsg);
                */
            };
        };

        Fatsercret_Thread.start();
        Yummly_Thread.start();
        // Spoonacular_Thread.start();
        CSE_Thread.start();

        try {
            Fatsercret_Thread.join();
            Yummly_Thread.join();
            // Spoonacular_Thread.join();
            CSE_Thread.join();
        } catch (Exception e) {
            System.out.println("Intent Activity Error Occurred");
            e.printStackTrace();
        }


        long seed = System.nanoTime();
        Collections.shuffle(foodImgUrlList, new Random(seed));

        for(int i = 0; i < foodImgUrlList.size(); i++) {

            if(i == 10)
                break;

            System.out.println(i + "th img Url : " + foodImgUrlList.get(i));
            foodImgAdapter.addImgUrl(foodImgUrlList.get(i));
        }

        foodNameTextView.setText(originInputText);
        foodImgViewPager.setAdapter(foodImgAdapter);

        String foodIngreString = "";

        if(yummlyFoodIngreList.size() > 0) {
            for (int i = 0; i < yummlyFoodIngreList.size(); i++) {
                if (i != yummlyFoodIngreList.size() - 1)
                    foodIngreString += yummlyFoodIngreList.get(i) + ", ";
                else
                    foodIngreString += yummlyFoodIngreList.get(i);
            }
        }
        else if(spoonsFoodIngreList.size() > 0) {
            for (int i = 0; i < spoonsFoodIngreList.size(); i++) {
                if (i != spoonsFoodIngreList.size() - 1)
                    foodIngreString += spoonsFoodIngreList.get(i) + ", ";
                else
                    foodIngreString += spoonsFoodIngreList.get(i);
            }
        }
        else
            foodIngreString = "There are not ingredients information";

        if(foodIngreString.compareTo("There are not ingredients information") != 0)
            myFoodInfoContents.setOriginAllergenContext(myAllergenList.isThereAllergenIngre(foodIngreString));
        else
            myFoodInfoContents.setOriginAllergenContext("There is not allergen");

        myFoodInfoContents.setOriginIngreContext(foodIngreString);
        allergenContextView.setText(myFoodInfoContents.getOriginAllergenContext());
        // System.out.println("food ingre str : " + foodIngreString);
        foodContextView.setText(foodIngreString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!menu_activated)
            getMenuInflater().inflate(R.menu.result_activity_menu, menu);
        else
            getMenuInflater().inflate(R.menu.result_activity_menu2, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_translate) {
            menu_activated = true;

            if(myFoodInfoContents.isTranslated() == false) {
                /*
                번역 작동
                 */
                List<String> target = new ArrayList<>();
                target.add(myFoodInfoContents.getOriginNurientsTitle());
                target.add(myFoodInfoContents.getOriginFlavorsTitle());
                target.add(myFoodInfoContents.getOriginIngreTitle());
                target.add(myFoodInfoContents.getOriginAllergenTitle());
                target.add(myFoodInfoContents.getOriginBitter());
                target.add(myFoodInfoContents.getOriginMeaty());
                target.add(myFoodInfoContents.getOriginPiquant());
                target.add(myFoodInfoContents.getOriginSalty());
                target.add(myFoodInfoContents.getOriginSour());
                target.add(myFoodInfoContents.getOriginSweet());
                target.add(myFoodInfoContents.getOriginFoodName());
                target.add(myFoodInfoContents.getOriginNutrientsContext());
                target.add(myFoodInfoContents.getOriginIngreContext());
                target.add(myFoodInfoContents.getOriginAllergenContext());

                try {
                    callCloudTranslation(target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            foodNameTextView.setText(myFoodInfoContents.getTransFoodName());

            nutrientsTitleTextView.setText(myFoodInfoContents.getTransNurientsTitle());
            nutrientsTextView.setText(myFoodInfoContents.getTransNutrientsContext());

            flavorsTitleTextView.setText(myFoodInfoContents.getTransFlavorsTitle());
            bitterTextView.setText(myFoodInfoContents.getTransBitter());
            meatyTextView.setText(myFoodInfoContents.getTransMeaty());
            piquantTextView.setText(myFoodInfoContents.getTransPiquant());
            saltyTextView.setText(myFoodInfoContents.getTransSalty());
            sourTextView.setText(myFoodInfoContents.getTransSour());
            sweetTextView.setText(myFoodInfoContents.getTransSweet());

            ingreTitleTextView.setText(myFoodInfoContents.getTransIngreTitle());
            foodContextView.setText(myFoodInfoContents.getTransIngreContext());

            allergenTitleTextView.setText(myFoodInfoContents.getTransAllergenTitle());
            allergenContextView.setText(myFoodInfoContents.getTransAllergenContext());

            poweredByTitleTextView.setText(myFoodInfoContents.getOriginPoweredByTitle());
            dataFromTitleTextView.setText(myFoodInfoContents.getOriginDataFromTitle());

            invalidateOptionsMenu();
        }
        else if(item.getItemId() == R.id.action_back_translate) {
            menu_activated = false;

            foodNameTextView.setText(myFoodInfoContents.getOriginFoodName());

            nutrientsTitleTextView.setText(myFoodInfoContents.getOriginNurientsTitle());
            nutrientsTextView.setText(myFoodInfoContents.getOriginNutrientsContext());

            flavorsTitleTextView.setText(myFoodInfoContents.getOriginFlavorsTitle());
            bitterTextView.setText(myFoodInfoContents.getOriginBitter());
            meatyTextView.setText(myFoodInfoContents.getOriginMeaty());
            piquantTextView.setText(myFoodInfoContents.getOriginPiquant());
            saltyTextView.setText(myFoodInfoContents.getOriginSalty());
            sourTextView.setText(myFoodInfoContents.getOriginSour());
            sweetTextView.setText(myFoodInfoContents.getOriginSweet());

            ingreTitleTextView.setText(myFoodInfoContents.getOriginIngreTitle());
            foodContextView.setText(myFoodInfoContents.getOriginIngreContext());

            allergenTitleTextView.setText(myFoodInfoContents.getOriginAllergenTitle());
            allergenContextView.setText(myFoodInfoContents.getOriginAllergenContext());

            poweredByTitleTextView.setText(myFoodInfoContents.getOriginPoweredByTitle());
            dataFromTitleTextView.setText(myFoodInfoContents.getOriginDataFromTitle());

            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    public void callCloudTranslation(final List<String> target) throws IOException {
        new AsyncTask<Object, Void, List<TranslationsResource>>(){
            @Override
            protected void onPreExecute() {
                dialog= new ProgressDialog(FoodInfoActivity_v1.this); //ProgressDialog 객체 생성
                dialog.setTitle("Progress");                   //ProgressDialog 제목
                dialog.setMessage("Loading.....");             //ProgressDialog 메세지
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); //막대형태의 ProgressDialog 스타일 설정
                dialog.setCanceledOnTouchOutside(false); //ProgressDialog가 진행되는 동안 dialog의 바깥쪽을 눌러 종료하는 것을 금지

                dialog.show(); //ProgressDialog 보여주기
                super.onPreExecute();
            }
            @Override
            protected List<TranslationsResource> doInBackground(Object... objects) {
                String result = "";

                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                //Access Google Translation
                Translate translate;
                translate = new Translate.Builder(httpTransport, jsonFactory, null)
                        .setGoogleClientRequestInitializer(new TranslateRequestInitializer(API_KEY))
                        .setApplicationName("OurMenu").build();

                List<TranslationsResource> translated_txt = new ArrayList<>();

                //Do translation for each paragraph
                try {
                    Translate.Translations.List translationreq = translate.translations().list(target, selectedLang);
                    TranslationsListResponse translationsResponse = translationreq.execute();

                    //save translated text in an array 'translated_txt'
                    translated_txt = translationsResponse.getTranslations();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return translated_txt;
            }

            @Override
            protected void onPostExecute(List<TranslationsResource> res) {

                String str = "";

                myFoodInfoContents.setTransNurientsTitle(res.get(0).getTranslatedText());
                myFoodInfoContents.setTransFlavorsTitle(res.get(1).getTranslatedText());
                myFoodInfoContents.setTransIngreTitle(res.get(2).getTranslatedText());
                myFoodInfoContents.setTransAllergenTitle(res.get(3).getTranslatedText());
                myFoodInfoContents.setTransBitter(res.get(4).getTranslatedText());
                myFoodInfoContents.setTransMeaty(res.get(5).getTranslatedText());
                myFoodInfoContents.setTransPiquant(res.get(6).getTranslatedText());
                myFoodInfoContents.setTransSalty(res.get(7).getTranslatedText());
                myFoodInfoContents.setTransSour(res.get(8).getTranslatedText());
                myFoodInfoContents.setTransSweet(res.get(9).getTranslatedText());
                myFoodInfoContents.setTransFoodName(res.get(10).getTranslatedText());
                myFoodInfoContents.setTransNutrientsContext(res.get(11).getTranslatedText());
                myFoodInfoContents.setTransIngreContext(res.get(12).getTranslatedText());
                myFoodInfoContents.setTransAllergenContext(res.get(13).getTranslatedText());


                foodNameTextView.setText(myFoodInfoContents.getTransFoodName());

                nutrientsTitleTextView.setText(myFoodInfoContents.getTransNurientsTitle());
                nutrientsTextView.setText(myFoodInfoContents.getTransNutrientsContext());

                flavorsTitleTextView.setText(myFoodInfoContents.getTransFlavorsTitle());
                bitterTextView.setText(myFoodInfoContents.getTransBitter());
                meatyTextView.setText(myFoodInfoContents.getTransMeaty());
                piquantTextView.setText(myFoodInfoContents.getTransPiquant());
                saltyTextView.setText(myFoodInfoContents.getTransSalty());
                sourTextView.setText(myFoodInfoContents.getTransSour());
                sweetTextView.setText(myFoodInfoContents.getTransSweet());

                ingreTitleTextView.setText(myFoodInfoContents.getTransIngreTitle());
                foodContextView.setText(myFoodInfoContents.getTransIngreContext());

                allergenTitleTextView.setText(myFoodInfoContents.getTransAllergenTitle());
                allergenContextView.setText(myFoodInfoContents.getTransAllergenContext());

                poweredByTitleTextView.setText(myFoodInfoContents.getOriginPoweredByTitle());
                dataFromTitleTextView.setText(myFoodInfoContents.getOriginDataFromTitle());


                dialog.dismiss();

                myFoodInfoContents.setTranslated(true);
            }
        }.execute();
    }




    public void mOnClick(View v) {
        int position;

        switch (v.getId()) {
            case R.id.img_previous_btn://이전버튼 클릭
                position = foodImgViewPager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴

                //현재 위치(position)에서 -1 을 해서 이전 position으로 변경
                //이전 Item으로 현재의 아이템 변경 설정(가장 처음이면 더이상 이동하지 않음)
                //첫번째 파라미터: 설정할 현재 위치
                //두번째 파라미터: 변경할 때 부드럽게 이동하는가? false면 팍팍 바뀜
                foodImgViewPager.setCurrentItem(position - 1, true);
                break;
            case R.id.img_next_btn://다음버튼 클릭
                position = foodImgViewPager.getCurrentItem();//현재 보여지는 아이템의 위치를 리턴

                //현재 위치(position)에서 +1 을 해서 다음 position으로 변경
                //다음 Item으로 현재의 아이템 변경 설정(가장 마지막이면 더이상 이동하지 않음)
                //첫번째 파라미터: 설정할 현재 위치
                //두번째 파라미터: 변경할 때 부드럽게 이동하는가? false면 팍팍 바뀜
                foodImgViewPager.setCurrentItem(position + 1, true);
                break;
        }
    }

}
