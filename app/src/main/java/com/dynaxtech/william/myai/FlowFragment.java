package com.dynaxtech.william.myai;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by william on 9/29/2017.
 */

public class FlowFragment extends Fragment {
    View myView;
    MainActivity mainActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView =inflater.inflate(R.layout.flow_layout, container, false );
        return myView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity)getActivity();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private Drawable wifiBtnOffBackground;
    public Drawable getWifiOffBackgound()
    {
        return wifiBtnOffBackground;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Button wifiBtn = (Button)myView.findViewById(R.id.wifi);
        wifiBtnOffBackground = wifiBtn.getBackground();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mainActivity.finishDownloading();
    }


/*
    public void setNetworkFragment( NetworkFragment fragment )
    {
        mNetworkFragment = fragment;
    }

    public NetworkFragment getNetworkFragment()
    {
        return mNetworkFragment;
    }
    */

    /*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
    public void draw()
    {
        SurfaceView surfaceView = (SurfaceView)myView.findViewById(R.id.wifi_surface);
        SurfaceHolder hd = surfaceView.getHolder();
        Canvas c = hd.lockCanvas();
        Paint goodPaint = new Paint();
        goodPaint.setColor(Color.BLACK);
        goodPaint.setStyle(Paint.Style.STROKE);
        goodPaint.setStrokeWidth(10);
        c.drawLine(319, 73, 500, 200, goodPaint);
        c.drawLine(500, 73, 600, 200, goodPaint);
        hd.unlockCanvasAndPost(c);
    }
*/
}
