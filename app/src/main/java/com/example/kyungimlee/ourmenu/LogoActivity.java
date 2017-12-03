package com.example.kyungimlee.ourmenu;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        Handler hd = new Handler();
        hd.postDelayed(new Runnable() {
            @Override public void run() {
                finish();
            }
            }, 3000);
    }
}
