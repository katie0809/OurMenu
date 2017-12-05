package com.example.kyungimlee.ourmenu;

import android.Manifest;
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
        import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
        import java.io.FileInputStream;
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

public class SettingActivity extends AppCompatActivity {

    private Spinner select_language;
    private Button start_btn;
    private CheckBox check_btn;

    ArrayList<String> items;
    String choice = null;
    HashMap supported_lang = new HashMap();

    public static final int WRITE_PERMISSIONS_REQUEST = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        select_language = (Spinner) findViewById(R.id.lang_list2);
        start_btn = (Button) findViewById(R.id.confirm_btn);
        check_btn = (CheckBox) findViewById(R.id.checkBox2);

        // 드롭다운 메뉴의 기본값은 Select Language
        items = new ArrayList<String>();
        items.add("Select Language");

        readLangFile();

        // 언어명 저장된 배열리스트와 GUI 컴포넌트 spinner 연결해준다
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items
        );

        Map<String, String> ordered_map = new TreeMap<>();
        ordered_map.putAll(supported_lang);
        Iterator<String> keys = ordered_map.keySet().iterator();
        while(keys.hasNext()){
            String key = keys.next();
            items.add(key);
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        select_language.setAdapter(adapter);
        getChoice();

        // 사용자가 선택했던 언어를 default로
        int tmp = 0, i = 1;
        Iterator<String> it = ordered_map.keySet().iterator();
        while(it.hasNext()){
            String item = items.get(i);
            Object obj = supported_lang.get(item);
            if(obj == null)
                continue;
            String langName = obj.toString();
            if(langName.compareTo(choice) == 0){
                tmp = i;
                break;
            }
            i++;
        }
        select_language.setSelection(tmp);

        File file = new File(getExternalPath()+"/OurMenu/setting/languageFixed.txt");
        if(isFileExist(file)){
            check_btn.setChecked(true);
        }

        //set button listener
        start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startApp();
                Toast.makeText(SettingActivity.this,"Saved",Toast.LENGTH_SHORT).show();

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
                supported_lang.put(words.get(0), words.get(1));
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


    public void getChoice(){
        //check if an user made language choice
        File langFile = new File(getExternalPath()+"/OurMenu/setting/languageChoice.txt");
        if(langFile.exists()){
            byte[] buffer = readFile(langFile);
            choice = new String(buffer);
        }else{
            //if user didn's select the language
            choice = "ko";
        }
    }

    public void startApp(){
        //Intent i = new Intent(this, MainActivity.class);
        File setting = null, langfile = null;
        setting = makeDirectory(getExternalPath()+"/OurMenu/setting/");

        //Ask for External Storage Writing Permission
        if(PermissionUtils.requestPermission(this, WRITE_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            //Make file languageChoice.txt on OurMenu/setting
            langfile = makeFile(setting, getExternalPath()+"/OurMenu/setting/languageChoice.txt");

            //write user language choice
            if(choice != null) {
                writeFile(langfile, supported_lang.get(select_language.getSelectedItem().toString()).toString().getBytes());
            }else{
                //if user doesn't select the language
                return;
            }
        }else{
            //Permission denied or no permission
            return;
        }

        //if checkbox is checked
        if(check_btn.isChecked()){
            //make file languageFixed.txt
            makeFile(setting, getExternalPath()+"/OurMenu/setting/languageFixed.txt");
        }
        else{
            //delete file
            File file = new File(getExternalPath()+"/OurMenu/setting/languageFixed.txt");
            deleteFile(file);
        }

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

    /**
     * 파일 존재 여부 확인 하기
     * @param file
     * @return
     */
    private boolean isFileExist(File file){
        boolean result;
        if(file!=null&&file.exists()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }
    /**
     * (dir/file) 삭제 하기
     * @param file
     */
    private boolean deleteFile(File file){
        boolean result;
        if(file!=null&&file.exists()){
            file.delete();
            result = true;
        }else{
            result = false;
        }
        return result;
    }


}
