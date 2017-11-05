package com.dynaxtech.william.myai;

import android.app.Fragment;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DownloadCallback {

    private static String SAVE_KEY_ACTIVE_ID = "activeComponents";
    private static String SAVE_KEY_ACTIVE_STATE = "activeStates";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // create a headless fragment to handle network
        // This accociates this activity withe the fragment and set the callback of the fragment to this activity.
        // See callback in the fragment and DownloadTask
        android.app.FragmentManager fragmentManager = getFragmentManager();
        Fragment x = fragmentManager.findFragmentById(R.id.content_frame);
        if( x instanceof FlowFragment )
            flowFragment = (FlowFragment) x;
        x = fragmentManager.findFragmentByTag(NetworkFragment.TAG);
        if( x instanceof NetworkFragment )
            networkFragment = (NetworkFragment)x;

        /*ArrayList<Integer> intList = savedInstanceState==null ? null : savedInstanceState.getIntegerArrayList("activeComponents");
        activeComponents = intList==null ? new HashSet() : new HashSet(Arrays.asList(intList.toArray()));*/
        activeComponents = new HashMap<Integer, Boolean>();
        requestingViewId = 0;

        if( savedInstanceState != null ) {
            int[] comps = savedInstanceState.getIntArray(SAVE_KEY_ACTIVE_ID);
            boolean[] states = savedInstanceState.getBooleanArray(SAVE_KEY_ACTIVE_STATE);
            if (comps != null && states != null && comps.length == states.length) {
                for (int k = 0; k < comps.length; ++k)
                    activeComponents.put(comps[k], states[k]);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    AnimationDrawable wifiAnimation;
    FlowFragment flowFragment;
    NetworkFragment networkFragment;

    float p;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//        p = 0f;

        android.app.FragmentManager fragmentManager = getFragmentManager();

        switch (id) {
            case R.id.nav_flow_layout:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, (flowFragment=new FlowFragment()) )
                        .commit();
                networkFragment = NetworkFragment.getInstance(fragmentManager, "https://www.google.com");
                break;
            case R.id.nav_phone_layout:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, new PhoneFragment())
                        .commit();
                break;
            case R.id.nav_gps_layout:
                break;
            case R.id.nav_send:
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, new GpsFragment())
                        .commit();
                break;
            default:
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // https://stackoverflow.com/questions/3616676/how-to-draw-a-line-in-android
    // https://medium.com/@ali.muzaffar/understanding-vectordrawable-pathdata-commands-in-android-d56a6054610e
    // https://alvinalexander.com/java/jwarehouse/android-examples/platforms/android-5/samples/ApiDemos/src/com/example/android/apis/graphics/PathEffects.java.shtml
    // https://stackoverflow.com/questions/16528572/draw-dash-line-on-a-canvas
    // moving dotted Line android
    // shape drawable Line example


    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {

    }

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
//    private NetworkFragment mNetworkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;
    private int requestingViewId;
    private int requestedNetworkType;

    @Override
    public void updateFromDownload(Object result) {
        String content;
        if( result == null ) {
//            onoff = false;
            Log.d("MainActivity","updateFromDownload: no result" );
            return;
        }
        if( result instanceof Exception) {
            Exception excep = (Exception)result;
//            onoff = false;
            Log.d("MainActivity","updateFromDownload: Exception: " + excep.getMessage());
            return;
        }
        if( result instanceof String ) {
            content = (String)result;
//            onoff = true;
            Log.d("MainActivity", "updateFromDownload: Content: " + content );
            return;
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(getBaseContext().CONNECTIVITY_SERVICE);
        try {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.getType() == requestedNetworkType ) {
                    // connected to wifi
                    p = 1;
                    return activeNetwork;
                }
/*                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    p = 1;
                    return activeNetwork;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    p = 2;
                    return activeNetwork;
                }*/
                return null;
            } else {
                // not connected to the internet
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void showComponent( int viewId, boolean status )
    {
        FlowView surfaceView = (FlowView)flowFragment.getView().findViewById(R.id.wifi_surface);
        Button requestingView = (Button) flowFragment.getView().findViewById( viewId );
        if( surfaceView != null ) {
            if( requestingView != null ) {
                if( status )
                    requestingView.setBackgroundResource(R.drawable.ninepatchon );
                else
                    requestingView.setBackground(flowFragment.getWifiOffBackgound());
                View meBtn = (View)surfaceView.getRootView().findViewById(R.id.me);
                View parentView = (View)requestingView.getParent();
                int parentId = ( parentView==null ? 0 : parentView.getId() );
                if( parentId != 0 && meBtn != null ) {
                    switch (parentId) {
                        case R.id.InputTop:
                            Point s = new Point((requestingView.getLeft() + requestingView.getRight()) / 2, requestingView.getBottom());
                            Point e = new Point((meBtn.getLeft()+meBtn.getRight())/2, meBtn.getTop());
                            surfaceView.addLine( String.valueOf(requestingView.getId()), s.x, s.y, e.x, e.y );
                            break;
                        case R.id.InputLeft:
                            s = new Point(requestingView.getRight(), (requestingView.getTop() + requestingView.getBottom()) / 2);
                            e = new Point(meBtn.getLeft(), (meBtn.getTop()+meBtn.getBottom())/2);
                            surfaceView.addLine( String.valueOf(requestingView.getId()), s.x, s.y, e.x, e.y );
                            break;
                        case R.id.OutputBottom:
                            s = new Point((requestingView.getLeft() + requestingView.getRight()) / 2, requestingView.getTop());
                            e = new Point((meBtn.getLeft() + meBtn.getRight()) / 2, meBtn.getBottom());
                            surfaceView.addLine( String.valueOf(requestingView.getId()), s.x, s.y, e.x, e.y );
                            break;
                        case R.id.OutputRight:
                            s = new Point(requestingView.getLeft(), (requestingView.getTop() + requestingView.getBottom()) / 2);
                            e = new Point(meBtn.getLeft(), (meBtn.getTop() + meBtn.getBottom()) / 2);
                            surfaceView.addLine( String.valueOf(requestingView.getId()), s.x, s.y, e.x, e.y );
                            break;
                    }
                    surfaceView.setLineStatus( String.valueOf(requestingView.getId()), status );
                }

            }
            surfaceView.stop();
        }
    }

    @Override
    public void finishDownloading() {
        if( wifiAnimation != null )
            wifiAnimation.stop();
        mDownloading = false;
        if( requestingViewId > 0 )
            activeComponents.put(requestingViewId, networkFragment != null && networkFragment.isConnected() );
        showComponent( requestingViewId, networkFragment != null && networkFragment.isConnected() );
        requestingViewId = 0;
    }

    // https://developer.android.com/guide/topics/graphics/drawable-animation.html

    private void startDownload() {
        if (!mDownloading && networkFragment != null) {
            // Execute the async download.
            networkFragment.startDownload();
            mDownloading = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if( flowFragment != null ) {
            for (Map.Entry<Integer, Boolean> entry : activeComponents.entrySet()) {
                showComponent( entry.getKey(), entry.getValue() );
            }
        }
//            finishDownloading();
    }

    private Map<Integer, Boolean> activeComponents;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if( activeComponents.size() > 0 ) {
            int[] comps = new int[activeComponents.size()];
            boolean[] states = new boolean[activeComponents.size()];
            int k = 0;
            for (Map.Entry<Integer, Boolean> entry : activeComponents.entrySet()) {
                comps[k] = entry.getKey();
                states[k] = entry.getValue();
                ++k;
            }
            outState.putIntArray(SAVE_KEY_ACTIVE_ID, comps);
            outState.putBooleanArray(SAVE_KEY_ACTIVE_STATE, states);
        }
/*
        Integer [] acList = new Integer[activeComponents.size()];
        activeComponents.keySet().toArray(acList);
        outState.putIntegerArrayList("activeComponents", new ArrayList(Arrays.asList(acList)));
        boolean [] stateList = new boolean[acList.length];
        outState.putBooleanArray("activeStates", stateList);
*/
    }

    public void onClickWifi(View view)
    {
        if( !(view instanceof Button ) ) return;
        requestingViewId = view.getId();
        switch (requestingViewId)
        {
            case R.id.wifi:
                requestedNetworkType = ConnectivityManager.TYPE_WIFI;
                view.setBackgroundResource(R.drawable.blink);
/*                wifiAnimation = (AnimationDrawable) view.getBackground();
                wifiAnimation.getDuration(3000);
                wifiAnimation.start();*/
                startDownload();
                break;
            case R.id.mobiledata:
                requestedNetworkType = ConnectivityManager.TYPE_MOBILE;
                view.setBackgroundResource(R.drawable.blink);
                startDownload();
            default:
        }

        /*
        surfaceView.setPhase(p);
        p += 2f;
        if( p>10f )
            p = 0.0f;
*/
      /*  SurfaceHolder hd = surfView.getHolder();
        Canvas c = hd.lockCanvas();
        Paint goodPaint = new Paint();
        goodPaint.setColor(Color.BLACK);
        goodPaint.setStrokeWidth(3);
        c.drawLine(319, 73, 500, 200, goodPaint);
        hd.unlockCanvasAndPost(c);*/

    }
}
