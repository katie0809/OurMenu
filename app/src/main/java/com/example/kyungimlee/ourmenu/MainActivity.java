package com.example.kyungimlee.ourmenu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

//import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String CLOUD_TRANSLATION_API_KEY = "AIzaSyDON76sNwTcC2AuXU2L_y31z7BtHYP74Ko";
    private static String FILE_NAME = "";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final int SAVED_MENU_REQUEST = 4;

    private TextView mImageDetails;
    private ImageView flag;
    private Button camera_btn;
    private Button gallary_btn;
    private Button saved_btn;
    private Button setting_btn;
    boolean FLAG;

    // 코드상에서 사용된 변수
    HashMap supported_lang = new HashMap();
    ArrayList<String> items;
    String target_txt = null, choice = null;

    //Handlers
    Handler handler = null;
    Handler menu_handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera_btn = (Button) findViewById(R.id.camera_btn);
        gallary_btn = (Button) findViewById(R.id.gallary_btn);
        saved_btn = (Button) findViewById(R.id.saved_btn);
        setting_btn = (Button) findViewById(R.id.setting_btn);
        flag = (ImageView) findViewById(R.id.imageView2);
        FLAG = true;

        if(FLAG){
            //startActivity(new Intent(this, LogoActivity.class));
            FLAG = false;
        }

        //Set file name
        FILE_NAME = getDate() +"_menu.jpg";

        //Set flags
        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();
        if(countryCodeValue.compareTo("do") == 0)
            countryCodeValue = "dom";
        String resName = "@drawable/" + countryCodeValue;
        if(countryCodeValue != null){
            Resources resources = this.getResources();
            final int resourceId = resources.getIdentifier(countryCodeValue, "drawable", this.getPackageName());
            flag.setImageResource(resourceId);

        }
        camera_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCamera();
            }
        });
        gallary_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGalleryChooser();
            }
        });
        saved_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSavedMenuGallery();
            }
        });
        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetting();
            }
        });

        //check if an user already made fixed language choice
        File fixed = new File(getExternalPath()+"/OurMenu/setting/languageFixed.txt");
        if(!fixed.exists()){
            //Show loading page
            startActivity(new Intent(this, LoadingActivity.class));
        }

    }

    private void startSetting() {
        startActivity(new Intent(this, SettingActivity.class));
    }

    private void isLanguageChosen(){
        //check if an user made language choice
        File langFile = new File(getExternalPath()+"/OurMenu/setting/languageChoice.txt");
        if(langFile.exists()){
            byte[] buffer = readFile(langFile);
            choice = new String(buffer);
            //Toast.makeText(MainActivity.this, choice, Toast.LENGTH_SHORT).show();
        }else{
            //if user didn's select the language
            //Show message
            //Toast.makeText(MainActivity.this, "No lanugage chose. Use korean as a default language", Toast.LENGTH_SHORT).show();
            choice = "Korean";
        }
        return;
    }

    //get current date, time
    public static String getDate(){
        SimpleDateFormat dateFormat = new  SimpleDateFormat("yyMMdd_HHmm", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        return strDate;
    }

    /**
     * 파일 읽어 오기
     * @param file
     */
    private byte[] readFile(File file){
        int readcount=0;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                readcount = (int)file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                for(int i=0 ; i<file.length();i++){
                    Log.d(TAG, ""+buffer[i]);
                }
                fis.close();
                return buffer;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }

    private void startSavedMenuGallery() {
        //start saved menu gallery
        Intent intent = new Intent(this, GallaryActivity.class);
        startActivity(intent);

    }


    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    //After finishing gallary chooser of camera, get the photo uri and show it using cropping activity
    //First, deliver the uri to method showImage()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            showImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            showImage(photoUri);
        } else if (requestCode == SAVED_MENU_REQUEST && requestCode == RESULT_OK && data != null){
            //Start menuboard activity
            Intent i = new Intent(this, MenuBoardActivity.class);

            String struri = data.getData().toString();

            i.putExtra("imageUri", struri);
            i.putExtra("header", 1);

            startActivity(i);
        }
    }

    //Using delivered uri, start Cropping activity
    public void showImage(Uri uri){
        Intent crop = new Intent(this, CroppingActivity.class);
        String struri = uri.toString();

        crop.putExtra("imageUri", struri);
        startActivity(crop);
    }

    public String getExternalPath(){
        String sdPath = "";
        String ext = Environment.getExternalStorageState();
        if(ext.equals(Environment.MEDIA_MOUNTED)){
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }else{
            sdPath = getFilesDir() + "";
        }

        return sdPath;
    }

}

