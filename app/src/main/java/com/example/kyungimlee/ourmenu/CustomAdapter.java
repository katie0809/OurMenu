package com.example.kyungimlee.ourmenu;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import java.util.ArrayList;

/**
 * Created by kyungimlee on 2017. 12. 6..
 */

class CustomAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> img_urls = new ArrayList<String>();
    private static final int MAX_IMAGES = 10;
    private int imageViewWidth = 324;
    private int imageViewHeight = 236;

    public CustomAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    public CustomAdapter(Context context) {
        this.context = context;
    }

    public CustomAdapter(LayoutInflater inflater) {
        // TODO Auto-generated constructor stub
        //전달 받은 LayoutInflater를 멤버변수로 전달
        this.inflater = inflater;
    }

    public void addImgUrlsToAdapter(String url_str) {
        if(img_urls.size() < MAX_IMAGES - 1)
            img_urls.add(url_str);
    }

    //PagerAdapter가 가지고 잇는 View의 개수를 리턴
    //보통 보여줘야하는 이미지 배열 데이터의 길이를 리턴
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        // return MAX_IMAGES; //이미지 개수 리턴(그림이 10개라서 10을 리턴)
        return img_urls.size();
    }

    //ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
    //쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
    //첫번째 파라미터 : ViewPager
    //두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        View view = null;

        //새로운 View 객체를 Layoutinflater를 이용해서 생성
        //만들어질 View의 설계는 res폴더>>layout폴더>>viewpater_childview.xml 레이아웃 파일 사용
        view = inflater.inflate(R.layout.viewpager_childview, null);

        //만들어진 View안에 있는 ImageView 객체 참조
        //위에서 inflated 되어 만들어진 view로부터 findViewById()를 해야 하는 것에 주의.
        ImageView img = (ImageView)view.findViewById(R.id.img_viewpager_childimage);

        /*
        Glide.with(context)
                .load(img_urls.get(position))
                .into(img);
                */

        RequestOptions glideOptions = new RequestOptions()
                .override(imageViewWidth, imageViewHeight)
                .placeholder(R.drawable.img_loading)
                .error(R.drawable.image_loading_error);

        Glide.with(context)
                .load(img_urls.get(position))
                .apply(glideOptions)
                .into(img);

        //ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
        //현재 position에 해당하는 이미지를 setting
        // img.setImageResource(R.drawable.gametitle_01+position);

        //ViewPager에 만들어 낸 View 추가
        container.addView(view);

        //Image가 세팅된 View를 리턴
        return view;
    }

    //화면에 보이지 않은 View는파괴를 해서 메모리를 관리함.
    //첫번째 파라미터 : ViewPager
    //두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
    //세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub
        //ViewPager에서 보이지 않는 View는 제거
        //세번째 파라미터가 View 객체 이지만 데이터 타입이 Object여서 형변환 실시
        container.removeView((View)object);
    }

    //instantiateItem() 메소드에서 리턴된 Ojbect가 View가  맞는지 확인하는 메소드
    @Override
    public boolean isViewFromObject(View v, Object obj) {
        // TODO Auto-generated method stub
        return v == obj;
    }

    public void addImgUrl(String url_str) {
        img_urls.add(url_str);
    }
}
