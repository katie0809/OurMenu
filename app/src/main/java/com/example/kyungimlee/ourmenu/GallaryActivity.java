package com.example.kyungimlee.ourmenu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class GallaryActivity extends AppCompatActivity {

    /** Called when the activity is first created. */
    protected GridView objGridView;
    protected String path = getExternalPath() + "OurMenu/res/pictures/"; //파일 위치
    protected File f = new File(path);
    protected int i = 0;
    protected List<String> image_path = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallary);
        for(File file : f.listFiles()){
            image_path.add(file.getName());
        }

        objGridView = (GridView)findViewById(R.id.GridView01);
        ImageAdapter imageAdapter = new ImageAdapter(this, image_path, path);
        objGridView.setAdapter(imageAdapter);


        objGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Start menuboard activity
                //String str = String.valueOf(position);
                //Toast.makeText(GallaryActivity.this, str, Toast.LENGTH_SHORT).show();
                startMenuBoard(position);
            }
        });
    }

    private void startMenuBoard(int position) {

        String _path = path + image_path.get(position % image_path.size());
        //Bitmap bitmap = BitmapFactory.decodeFile(_path);
        Toast.makeText(GallaryActivity.this, _path, Toast.LENGTH_SHORT).show();
        File bitmap = new File(_path);
        Uri uri = Uri.fromFile(bitmap);

        Intent i = new Intent(this, MenuBoardActivity.class);
        i.putExtra("imageUri", uri.toString());
        i.putExtra("header", 1);

        startActivity(i);

    }


    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private File[] files = new File[50];
        private List<String> aImages_path;
        private String SD_Path;

        public ImageAdapter(Context objC, List<String> path, String spath) {
            mContext = objC;
            aImages_path = path;
            SD_Path = spath;
        }
        public int getCount() {

            return aImages_path.size();
        }
        public Object getItem(int position){

            return aImages_path.get(position%aImages_path.size());
        }
        public long getItemId(int position){
            return position;
        }

        public View getView(int position, View converterView, ViewGroup parent){
            ImageView objImgView;

            if(converterView == null){
                objImgView = new ImageView(mContext);
                objImgView.setLayoutParams(new GridView.LayoutParams(300, 300));
                objImgView.setAdjustViewBounds(false);
                objImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                objImgView.setPadding(8, 8, 8, 8);
            }else{
                objImgView = (ImageView)converterView;
            }

            String path = SD_Path + aImages_path.get(position % aImages_path.size());
            Bitmap bitmap = BitmapFactory.decodeFile(path);

            objImgView.setImageBitmap(bitmap);
            return objImgView;
        }
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
                fis.close();
                return buffer;
            } catch (Exception e) {
                Toast.makeText(GallaryActivity.this,"Authorization Problem Occurred",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        return null;
    }

}
