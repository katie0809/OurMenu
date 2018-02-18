package com.uos.kyungimlee.ourmenu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.support.design.widget.TextInputEditText;
//import android.text.TextUtils;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by kyungimlee on 2018. 2. 18..
 */

public class PreviewDialog extends Dialog{

    private MyDialogListener dialogListener;


    private static final int LAYOUT = R.layout.dialog_preview;
    ViewPager previewImgViewpager;

    public PreviewDialog(Context context){
        super(context);
    }

    public void setDialogListener(MyDialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        previewImgViewpager = (ViewPager) findViewById(R.id.preview_img);

        final PreviewAdapter previewImgAdapter = new PreviewAdapter(getContext(), getLayoutInflater());
        previewImgViewpager.setAdapter(previewImgAdapter);

        previewImgViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                previewImgViewpager.setCurrentItem(position, true);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });


    }


}
