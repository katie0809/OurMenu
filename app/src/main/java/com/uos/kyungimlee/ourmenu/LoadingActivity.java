package com.uos.kyungimlee.ourmenu;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

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

    String langList = "Afrikaans\taf\n" +
            "Albanian\tsq\n" +
            "Amharic\tam\n" +
            "Arabic\tar\n" +
            "Armenian\thy\n" +
            "Azeerbaijani\taz\n" +
            "Basque\teu\n" +
            "Belarusian\tbe\n" +
            "Bengali\tbn\n" +
            "Bosnian\tbs\n" +
            "Bulgarian\tbg\n" +
            "Catalan\tca\n" +
            "Cebuano\tceb\n" +
            "Chinese (Simplified)\tzh-CN\n" +
            "Chinese (Traditional)\tzh-TW\n" +
            "Corsican\tco\n" +
            "Croatian\thr\n" +
            "Czech\tcs\n" +
            "Danish\tda\n" +
            "Dutch\tnl\n" +
            "English\ten\n" +
            "Esperanto\teo\n" +
            "Estonian\tet\n" +
            "Finnish\tfi\n" +
            "French\tfr\n" +
            "Frisian\tfy\n" +
            "Galician\tgl\n" +
            "Georgian\tka\n" +
            "German\tde\n" +
            "Greek\tel\n" +
            "Gujarati\tgu\n" +
            "Haitian Creole\tht\n" +
            "Hausa\tha\n" +
            "Hawaiian\thaw\n" +
            "Hebrew\tiw\n" +
            "Hindi\thi\n" +
            "Hmong\thmn\n" +
            "Hungarian\thu\n" +
            "Icelandic\tis\n" +
            "Igbo\tig\n" +
            "Indonesian\tid\n" +
            "Irish\tga\n" +
            "Italian\tit\n" +
            "Japanese\tja\n" +
            "Javanese\tjw\n" +
            "Kannada\tkn\n" +
            "Kazakh\tkk\n" +
            "Khmer\tkm\n" +
            "Korean\tko\n" +
            "Kurdish\tku\n" +
            "Kyrgyz\tky\n" +
            "Lao\tlo\n" +
            "Latin\tla\n" +
            "Latvian\tlv\n" +
            "Lithuanian\tlt\n" +
            "Luxembourgish\tlb\n" +
            "Macedonian\tmk\n" +
            "Malagasy\tmg\n" +
            "Malay\tms\n" +
            "Malayalam\tml\n" +
            "Maltese\tmt\n" +
            "Maori\tmi\n" +
            "Marathi\tmr\n" +
            "Mongolian\tmn\n" +
            "Myanmar (Burmese)\tmy\n" +
            "Nepali\tne\n" +
            "Norwegian\tno\n" +
            "Nyanja (Chichewa)\tny\n" +
            "Pashto\tps\n" +
            "Persian\tfa\n" +
            "Polish\tpl\n" +
            "Portuguese (Portugal, Brazil)\tpt\n" +
            "Punjabi\tpa\n" +
            "Romanian\tro\n" +
            "Russian\tru\n" +
            "Samoan\tsm\n" +
            "Scots Gaelic\tgd\n" +
            "Serbian\tsr\n" +
            "Sesotho\tst\n" +
            "Shona\tsn\n" +
            "Sindhi\tsd\n" +
            "Sinhala (Sinhalese)\tsi\n" +
            "Slovak\tsk\n" +
            "Slovenian\tsl\n" +
            "Somali\tso\n" +
            "Spanish\tes\n" +
            "Sundanese\tsu\n" +
            "Swahili\tsw\n" +
            "Swedish\tsv\n" +
            "Tagalog (Filipino)\ttl\n" +
            "Tajik\ttg\n" +
            "Tamil\tta\n" +
            "Telugu\tte\n" +
            "Thai\tth\n" +
            "Turkish\ttr\n" +
            "Ukrainian\tuk\n" +
            "Urdu\tur\n" +
            "Uzbek\tuz\n" +
            "Vietnamese\tvi\n" +
            "Welsh\tcy\n" +
            "Xhosa\txh\n" +
            "Yiddish\tyi\n" +
            "Yoruba\tyo\n" +
            "Zulu\tzu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        PermissionUtils.requestPermission(
                LoadingActivity.this,
                WRITE_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

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

        String tmp = new String();
        tmp = langList;
        StringTokenizer stt = new StringTokenizer(tmp, "\n");
        while(stt.hasMoreTokens()){
            StringTokenizer st = new StringTokenizer(stt.nextToken(), "\t");
            String nation = "", code = "";
            nation = st.nextToken();
            code = st.nextToken();
            supported_lang.put(nation, code);
        }

        Map<String, String> ordered_map = new TreeMap<>();
        ordered_map.putAll(supported_lang);
        Iterator<String> keys = ordered_map.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            items.add(key);
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_language.setAdapter(adapter);

        /*
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
        */

        //set button listener
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(select_language.getSelectedItem().toString().compareTo("Select Language") != 0 ){
                    choice = supported_lang.get(select_language.getSelectedItem().toString()).toString();
                    if(PermissionUtils.requestPermission(
                            LoadingActivity.this,
                            WRITE_PERMISSIONS_REQUEST,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        startApp();
                        finish();
                    }
                }
                //startApp();
            }
        });

    }

    private void readLangFile() {

        try {
            String data_path = getExternalPath() + "OurMenu/setting/languageList.txt";
            File f = new File(data_path);
            FileReader fr = null;
            BufferedReader br = null;

            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String read = "";
            while((read = br.readLine()) != null){
                StringTokenizer stt = new StringTokenizer(read, "\t");
                List<String> words = new ArrayList<>();
                while(stt.hasMoreTokens()){
                    String word = "";
                    word = stt.nextToken();
                    words.add(word);
                }
                //supported_lang.put(words.get(0), words.get(1));
                read = "";
            }
            fr.close();
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeDefaultDir() {

            //Save image
            //Make folder OurMenu, OurMenu/res/annotations, OurMenu/res/pictures, OurMenu/setting
            makeDirectory(getExternalPath()+"/OurMenu/");
            makeDirectory(getExternalPath()+"/OurMenu/setting/");
            makeDirectory(getExternalPath()+"/OurMenu/res/");
            makeDirectory(getExternalPath()+"/OurMenu/res/pictures/");
            makeDirectory(getExternalPath()+"/OurMenu/res/annotations/");

    }

    private void makeLangFile() {

        File setting = null, langfile = null;

        if(PermissionUtils.requestPermission(this, WRITE_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            setting = makeDirectory(getExternalPath()+"OurMenu/setting/");
            langfile = makeFile(setting, getExternalPath()+"OurMenu/setting/languageList.txt");

            if(langfile.length() <= 0){

                writeFile(langfile, langList.getBytes());
            }
        }
    }

    public void startApp(){


        File setting = null, langfile = null;

        makeDefaultDir();
        makeLangFile();
        //readLangFile();

        //Make file languageChoice.txt on OurMenu/setting
        setting = makeDirectory(getExternalPath()+"/OurMenu/setting/");
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