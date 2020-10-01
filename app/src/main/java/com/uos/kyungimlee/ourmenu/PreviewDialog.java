package com.uos.kyungimlee.ourmenu;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
//import android.support.design.widget.TextInputEditText;
//import android.text.TextUtils;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import static android.content.ContentValues.TAG;


/**
 * Created by kyungimlee on 2018. 2. 18..
 */

public class PreviewDialog extends Dialog{

    private MyDialogListener dialogListener;


    private static final int LAYOUT = R.layout.dialog_preview;
    ViewPager previewImgViewpager;
    TextView tv;
    CheckBox chk;
    String external_path;
    private LinearLayout mPageMark;			//현재 몇 페이지
    private int mPrevPosition;				//이전에 선택되었던 포지션 값

    private final int[] previewTxts = new int[] {
            R.string.preview_msg1,
            R.string.preview_msg2,
            R.string.preview_msg3,
            R.string.preview_msg4,
            R.string.preview_msg5
    };

    public PreviewDialog(Context context, String path){
        super(context);
        external_path = path;
    }

    public void setDialogListener(MyDialogListener dialogListener){
        this.dialogListener = dialogListener;
    }

    @Override
    public void dismiss() {

        //if checkbox is checked
        if(chk.isChecked()){
            //make file languageFixed.txt
            File setting = null;
            setting = makeDirectory(external_path+"/OurMenu/setting/");
            makeFile(setting, external_path+"/OurMenu/setting/tutorialNo.txt");
        }

        super.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        previewImgViewpager = (ViewPager) findViewById(R.id.preview_img);
        mPageMark = (LinearLayout) findViewById(R.id.page_mark);
        chk = (CheckBox) findViewById(R.id.preview_chk);
        //tv = (TextView)findViewById(R.id.previewTxt);

        final PreviewAdapter previewImgAdapter = new PreviewAdapter(getContext(), getLayoutInflater());

        previewImgViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                previewImgViewpager.setCurrentItem(position, true);
                mPageMark.getChildAt(mPrevPosition).setBackgroundResource(R.drawable.page_not);	//이전 페이지에 해당하는 페이지 표시 이미지 변경
                mPageMark.getChildAt(position).setBackgroundResource(R.drawable.page_select);		//현재 페이지에 해당하는 페이지 표시 이미지 변경
                mPrevPosition = position;
                //tv.setText(previewTxts[position]);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
        initPageMark();	//현재 페이지 표시하는 뷰 초기화

        previewImgViewpager.setAdapter(previewImgAdapter);

    }
    //상단의 현재 페이지 표시하는 뷰 초기화
    private void initPageMark(){


        for(int i=0; i< 5 ; i++)
        {
            ImageView iv = new ImageView(getContext());	//페이지 표시 이미지 뷰 생성
            iv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            //첫 페이지 표시 이미지 이면 선택된 이미지로
            if(i==0)
                iv.setBackgroundResource(R.drawable.page_select);
            else	//나머지는 선택안된 이미지로
                iv.setBackgroundResource(R.drawable.page_not);

            //LinearLayout에 추가
            mPageMark.addView(iv);
        }
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

}
