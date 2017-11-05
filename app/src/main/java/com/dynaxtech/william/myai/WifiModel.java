package com.dynaxtech.william.myai;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

/**
 * Created by william on 10/22/2017.
 */

public class WifiModel {
    public boolean onoff;
    public Drawable offDrawable;
    public Button view;
    public NetworkFragment mNetworkFragment;
    public boolean mDownloading;

    public void onClick( View v)
    {
        if( view == null )
            view = (Button)v.findViewById(R.id.wifi);;
        if( offDrawable == null )
            offDrawable = view.getBackground();
        view.setBackgroundResource(R.drawable.blink);
        Point s = new Point((view.getLeft()+view.getRight())/2, view.getBottom());
        FlowView surfaceView = (FlowView)view.getRootView().findViewById(R.id.wifi_surface);
        Button meBtn = (Button)view.getRootView().findViewById(R.id.me);
        Point e = new Point((meBtn.getLeft()+meBtn.getRight())/2, meBtn.getTop());
        surfaceView.addLine( "wifi", s.x, s.y, e.x, e.y );
        surfaceView.start();
                /*
                wifiAnimation = (AnimationDrawable) view.getBackground();
                wifiAnimation.getDuration(3000);
                wifiAnimation.start();
                */
//        startDownload();

    }

    private void startDownload() {
        if (!mDownloading && mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }
}
