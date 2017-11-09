package com.example.kyungimlee.ourmenu;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.services.translate.model.*;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequest;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.TranslateScopes;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1AnnotateImageRequest;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1EntityAnnotation;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Feature;
import com.google.api.services.vision.v1.model.GoogleCloudVisionV1Image;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String CLOUD_TRANSLATION_API_KEY = "AIzaSyDON76sNwTcC2AuXU2L_y31z7BtHYP74Ko";
    private static final String FILE_NAME = "";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final int WRITE_PERMISSIONS_REQUEST = 4;

    private TextView mImageDetails;
    private ImageView mMainImage;
    private Button camera_btn;
    private Button gallary_btn;
    private Spinner select_language;

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
        select_language = (Spinner) findViewById(R.id.lang_list);

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

        // 드롭다운 메뉴의 기본값은 Select Language
        items = new ArrayList<String>();
        items.add("Select Language");

        // 구글에서 처리할 수 있는 언어 리스트 받는 스레드 처리
        // 해시맵에 <언어명,언어코드>로 저장
        // 드롭다운 메뉴에 띄울 배열리스트에 언어명 전부 저장
        menu_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // 메시지를 받아온다
                Bundle bun = msg.getData();
                String language_name = bun.getString("name");
                String language_code = bun.getString("code");

                // 해시맵에 제공되는 언어명과 언어 코드 쌍을 모두 집어넣고
                // ArrayList에 언어명을 모두 넣는다. 이 ArrayList는 spinner의 드롭다운 메뉴를
                // 만드는 데 사용됨
                supported_lang.put(language_name, language_code);
                items.add(language_name);
            }
        };

        // 언어명 저장된 배열리스트와 GUI 컴포넌트 spinner 연결해준다
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_language.setAdapter(adapter);

        // 사용자가 번역 원하는 언어 택한경우
        select_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                // 선택된 메뉴아이템을 Toast메시지로 띄운다
                Toast.makeText(MainActivity.this,"Selected Language : "+select_language.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
                choice = (String)select_language.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }


    private void callTranslation() throws IOException{
        //Do the work in an async task
        new AsyncTask<Object, Void, List<TranslationsResource> >() {
            @Override
            protected List<TranslationsResource> doInBackground(Object... objects) {


                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                //Translate translate = builder.build();
                Translate translate;
                translate = new Translate.Builder(httpTransport, jsonFactory, null)
                        .setGoogleClientRequestInitializer(new TranslateRequestInitializer(CLOUD_TRANSLATION_API_KEY))
                        .setApplicationName("Intent").build();

                try {
                    List<String> list = new ArrayList<>();
                    list.add("hello world");
                    Translate.Languages.List req = translate.languages().list();
                    LanguagesListResponse resp = req.execute();
                    List<LanguagesResource> lang = resp.getLanguages();
                    System.out.print(lang);
                    for(LanguagesResource l : lang){
                        System.out.print(l.getName());
                        System.out.print(l.getLanguage()+"\n");
                    }
                    Translate.Translations.List request = translate.translations().list(list, "ko");
                    TranslationsListResponse tlr = request.execute();
                    List<TranslationsResource> res = tlr.getTranslations();
                    for(TranslationsResource r : res){
                        System.out.print(r.getDetectedSourceLanguage());
                        System.out.print(r.getTranslatedText());
                    }
                    return res;

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected void onPostExecute(List<TranslationsResource> result){
                System.out.print("please\n");
            }
        }.execute();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            showImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            showImage(photoUri);
        }
    }

    public void showImage(Uri uri){
        Intent i = new Intent(this, MenuBoardActivity.class);

        Intent crop = new Intent(this, CroppingActivity.class);

        String struri = uri.toString();
        i.putExtra("imageUri", struri);

        if(select_language.getSelectedItem()==null)
            choice = "Korean";

        i.putExtra("langChoice", choice);

        crop.putExtra("imageUri", struri);
        startActivity(crop);
    }

}

