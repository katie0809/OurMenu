package com.uos.kyungimlee.ourmenu;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
