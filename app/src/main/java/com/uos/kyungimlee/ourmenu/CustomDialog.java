package com.uos.kyungimlee.ourmenu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.support.design.widget.TextInputEditText;
//import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by charlie on 2017. 8. 18..
 */


public class CustomDialog extends Dialog implements View.OnClickListener{

    private MyDialogListener dialogListener;

    private static final int LAYOUT = R.layout.dialog_custom;
    TextView translatedTxt;
    TextView originTxt;
    Button next_btn;
    private Context context;

    private String original_txt;
    private String result_txt;
    private String en_result_txt;
    private String detected_lang;

    public CustomDialog(Context context, String origin, String result, String en_result, String detected){
        super(context);
        this.context = context;
        this.original_txt = origin;
        this.result_txt = result;
        this.en_result_txt = en_result;
        this.detected_lang = detected;
    }

    public void setDialogListener(MyDialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        translatedTxt = (TextView) findViewById(R.id.result_txt);
        originTxt = (TextView) findViewById(R.id.origin_txt);
        next_btn = (Button) findViewById(R.id.next_btn);

        next_btn.setOnClickListener(this);

        translatedTxt.setText(result_txt);
        originTxt.setText(original_txt);

    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(getContext(), FoodInfoActivity_v1.class);
        // i.putExtra("selectedLang", "Others");
        i.putExtra("selectedLang", detected_lang);
        i.putExtra("originText", original_txt);
        i.putExtra("inputText", en_result_txt);
        getContext().startActivity(i);
    }

}
