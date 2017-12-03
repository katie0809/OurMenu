package com.example.kyungimlee.ourmenu;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

//import android.widget.Toast;

public class LoadingActivity extends AppCompatActivity {

    private Spinner select_language;
    private Button start_btn;
    private CheckBox check_btn;

    ArrayList<String> items;
    String choice = null;
    HashMap supported_lang = new HashMap();

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final int WRITE_PERMISSIONS_REQUEST = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        select_language = (Spinner) findViewById(R.id.lang_list2);
        start_btn = (Button) findViewById(R.id.start_btn);
        check_btn = (CheckBox) findViewById(R.id.checkBox2);

        // 드롭다운 메뉴의 기본값은 Select Language
        items = new ArrayList<String>();
        items.add("Select Language");

        // 언어명 저장된 배열리스트와 GUI 컴포넌트 spinner 연결해준다
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items
        );
        items.add("한국어");
        items.add("English");
        items.add("中文");
        items.add("日本語");
        items.add("española");

        supported_lang.put("한국어", "ko");
        supported_lang.put("English", "en");
        supported_lang.put("中文", "zh");
        supported_lang.put("日本語", "ja");
        supported_lang.put("española", "es");

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_language.setAdapter(adapter);

        // 사용자가 번역 원하는 언어 택한경우
        select_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                // 선택된 메뉴아이템을 Toast메시지로 띄운다
                //Toast.makeText(LoadingActivity.this,"Selected Language : "+select_language.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //set button listener
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((String)select_language.getSelectedItem().toString() != "Select Language")
                    choice = supported_lang.get(select_language.getSelectedItem().toString()).toString();
                startApp();
            }
        });

    }
    public void startApp(){
        //Intent i = new Intent(this, MainActivity.class);
        File setting = null, langfile = null;

        //i.putExtra("langChoice", choice);

        //Ask for External Storage Writing Permission
        if(PermissionUtils.requestPermission(this, WRITE_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            //Save image
            //Make folder OurMenu, OurMenu/res/annotations, OurMenu/res/pictures, OurMenu/setting
            makeDirectory(getExternalPath()+"/OurMenu/");
            setting = makeDirectory(getExternalPath()+"/OurMenu/setting/");
            makeDirectory(getExternalPath()+"/OurMenu/res/");
            makeDirectory(getExternalPath()+"/OurMenu/res/pictures/");
            makeDirectory(getExternalPath()+"/OurMenu/res/annotations/");

            //Make file languageChoice.txt on OurMenu/setting
            langfile = makeFile(setting, getExternalPath()+"/OurMenu/setting/languageChoice.txt");

            //write user language choice
            if(choice != null) {
                writeFile(langfile, choice.getBytes());
            }else{
                //if user doesn't select the language
                //Toast메시지를 띄운다
                //Toast.makeText(LoadingActivity.this,"Please select the language",Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            //Permission denied or no permission
            //Toast메시지를 띄운다
            //Toast.makeText(LoadingActivity.this,"Authorization Problem Occurred",Toast.LENGTH_SHORT).show();
            return;
        }

        //if checkbox is checked
        if(check_btn.isChecked()){
            //make file languageFixed.txt
            makeFile(setting, getExternalPath()+"/OurMenu/setting/languageFixed.txt");
        }

        finish();
    }

    /**
     * 파일에 내용 쓰기
     * @param file
     * @param file_content
     * @return
     */
    private boolean writeFile(File file , byte[] file_content){
        boolean result;
        FileOutputStream fos;
        if(file!=null&&file.exists()&&file_content!=null){
            try {
                fos = new FileOutputStream(file);
                try {
                    fos.write(file_content);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }


    /**
     * 디렉토리 생성
     * @return dir
     */
    private File makeDirectory(String dir_path){
        File dir = new File(dir_path);
        if (!dir.exists())
        {
            dir.mkdirs();
            Log.i( TAG , "!dir.exists" );
        }else{
            Log.i( TAG , "dir.exists" );
        }

        return dir;
    }

    /**
     * 파일 생성
     * @param dir
     * @return file
     */
    private File makeFile(File dir , String file_path){
        File file = null;
        boolean isSuccess = false;
        if(dir.isDirectory()){
            file = new File(file_path);
            if(file!=null&&!file.exists()){
                Log.i( TAG , "!file.exists" );
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    Log.i(TAG, "파일생성 여부 = " + isSuccess);
                }
            }else{
                Log.i( TAG , "file.exists" );
            }
        }
        return file;
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
