package com.example.kyungimlee.ourmenu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
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
    private int count;
    Boolean ACTIVATE;
    private boolean[] thumbnailsselection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallary);
        ACTIVATE = false;

        for(File file : f.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.toString().endsWith(".jpg");
            }
        })){
            image_path.add(file.getName());
        }

        objGridView = (GridView)findViewById(R.id.GridView01);
        ImageAdapter imageAdapter = new ImageAdapter(this, image_path, path);
        objGridView.setAdapter(imageAdapter);
        this.thumbnailsselection = new boolean[50];

    }

    private void deleteContents() {

        for(int i = 0; i<50; i++){
            String _path = path + image_path.get(i % image_path.size());
            if(thumbnailsselection[i]){
                //image is checked to delete

            }
        }

        ACTIVATE = false;
        invalidateOptionsMenu();
        objGridView.invalidateViews();
    }

    private void startMenuBoard(int position) {

        String _path = path + image_path.get(position % image_path.size());
        //Bitmap bitmap = BitmapFactory.decodeFile(_path);
        Toast.makeText(GallaryActivity.this, _path, Toast.LENGTH_SHORT).show();
        File bitmap = new File(_path);
        Uri uri = Uri.fromFile(bitmap);

        Intent i = new Intent(this, MenuBoardActivity.class);
        i.putExtra("imagePath", _path);
        i.putExtra("imageUri", uri.toString());
        i.putExtra("header", 1);

        startActivity(i);

    }


    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private File[] files = new File[50];
        private List<String> aImages_path;
        private String SD_Path;
        ////////
        private LayoutInflater mInflater;

        public ImageAdapter(Context objC, List<String> path, String spath) {
            mContext = objC;
            aImages_path = path;
            SD_Path = spath;
            /////////
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

        public View getView(final int position, View converterView, ViewGroup parent){
            ImageView objImgView;
            ViewHolder holder;

            if(converterView == null){
                holder = new ViewHolder();
                converterView = mInflater.inflate(R.layout.gallary_itm, null);
                holder.imageview = (ImageView) converterView.findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) converterView.findViewById(R.id.itemCheckBox);
                converterView.setTag(holder);
                holder.imageview.setLayoutParams(new ConstraintLayout.LayoutParams(300, 300));
                holder.imageview.setAdjustViewBounds(false);
                holder.imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.imageview.setPadding(8, 8, 8, 8);
                //////
                //objImgView = new ImageView(mContext);
                //objImgView.setLayoutParams(new GridView.LayoutParams(300, 300));
                //objImgView.setAdjustViewBounds(false);
                //objImgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                //objImgView.setPadding(8, 8, 8, 8);
            }else{
                holder = (ViewHolder) converterView.getTag();
                //objImgView = (ImageView)converterView;
            }

            String path = SD_Path + aImages_path.get(position % aImages_path.size());
            Bitmap bitmap = BitmapFactory.decodeFile(path);

            holder.checkbox.setId(position);
            if(!ACTIVATE)
                holder.checkbox.setVisibility(View.INVISIBLE);
            else
                holder.checkbox.setVisibility(View.VISIBLE);

            holder.checkbox.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    if (thumbnailsselection[id]){
                        cb.setChecked(false);
                        thumbnailsselection[id] = false;
                    } else {
                        cb.setChecked(true);
                        thumbnailsselection[id] = true;
                    }
                }
            });

            holder.imageview.setId(position);
            holder.imageview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if(!ACTIVATE)
                        startMenuBoard(position);
                }
            });

            holder.imageview.setImageBitmap(bitmap);
            holder.checkbox.setChecked(thumbnailsselection[position]);

            //objImgView.setImageBitmap(bitmap);
            return converterView;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!ACTIVATE)
            getMenuInflater().inflate(R.menu.activity_gallary, menu);
        else
            getMenuInflater().inflate(R.menu.activity_gallary2, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete){
            ACTIVATE = true;
            objGridView.invalidateViews();
            invalidateOptionsMenu();
        }
        if (item.getItemId() == R.id.action_cancel){
            ACTIVATE = false;
            thumbnailsselection = new boolean[50];
            objGridView.invalidateViews();
            invalidateOptionsMenu();
        }
        if (item.getItemId() == R.id.action_ok){
            //delete contents
            deleteContents();
        }
        return super.onOptionsItemSelected(item);
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

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        int id;
    }
}
