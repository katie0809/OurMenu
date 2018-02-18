package com.uos.kyungimlee.ourmenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by kyungimlee on 2018. 2. 18..
 */
public class PreviewAdapter extends PagerAdapter {

    Context context;
    ImageView img;
    private LayoutInflater inflater;

    private final int[] drawableImgs = new int[] {
            R.drawable.ad,
            R.drawable.ae,
            R.drawable.af,
            R.drawable.ag,
            R.drawable.ai
    };

    PreviewAdapter(Context context, LayoutInflater inflater) {
        this.context = context;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return drawableImgs.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null;
        view = inflater.inflate(R.layout.viewpager_childview, null);
        img = (ImageView)view.findViewById(R.id.img_viewpager_childimage);
        Bitmap drawImg = BitmapFactory.decodeResource(context.getResources(), drawableImgs[position]);

        img.setImageBitmap(drawImg);

        if(img.getParent()!=null)
            ((ViewGroup)img.getParent()).removeView(img); // <- fix
        container.addView(img);
        return img;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
