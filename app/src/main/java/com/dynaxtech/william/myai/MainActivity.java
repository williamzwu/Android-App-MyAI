package com.dynaxtech.william.myai;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
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
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DownloadCallback, LocationListener {

    private static String SAVE_KEY_ACTIVE_ID = "activeComponents";
    private static String SAVE_KEY_ACTIVE_STATE = "activeStates";
    private static String LOCATION_FILE_NAME = "location_list";
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

        AppIntent intent = new AppIntent( this, Intent.ACTION_SEND, "text/plain");
        if( intent.hasNext() ) {
            AppWriteFile locFile = new AppWriteFile(this.getApplicationContext(), LOCATION_FILE_NAME, "txt");
            if (locFile.canWrite()) {
                while (intent.hasNext()) {
                    String t = intent.next();
                    locFile.add(t);
                }
                intent.done();
                locFile.done();
            }
        }
        AppReadFile locFile = new AppReadFile(this.getApplicationContext(), LOCATION_FILE_NAME, "txt");
        if( locFile.hasNext() )
            locTarget = locFile.next();
        locFile.done();
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
    private String locTarget;

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

    LocationManager locationManager;
    static int distanceNear = 5000;
    static int distanceVeryNear = 2000;
    static int distanceApproaching = 30;
    static int distanceArrival = 10;
    static int [] distance = { distanceNear, distanceVeryNear, distanceApproaching, distanceArrival };
    static int [] background = { R.color.colorFar, R.color.colorNear, R.color.colorVeryNear, R.color.colorArrival };
    static int [] foreground = { R.color.colorPrimary, R.color.colorReverse, R.color.colorReverse, R.color.colorReverse };
    static int [] minTime = { 60*1000, 10*1000, 2*1000, 500, 500 };
    static int [] minDist = { 1000, 50, 20, 10 };
    static int locStatus;

    private void addLocation(String lati, String longti, String name )
    {
        AppReadFile locFile = new AppReadFile(this.getApplicationContext(), LOCATION_FILE_NAME, "txt");
        AppWriteFile newLocFile = new AppWriteFile(this.getApplicationContext(), LOCATION_FILE_NAME, "txt");
        int added = 0;
        while( locFile.hasNext() )
        {
            String old = locFile.next();
            String gps[] = old.split(",");
            if( gps.length>2 && name.equalsIgnoreCase(gps[2]) )
            {
                // find the same name, replace
                if( newLocFile.canWrite() )
                    newLocFile.add( lati+","+longti+","+name);
                added++;
            } else
                if( newLocFile.canWrite() )
                    newLocFile.add( old );
        }
        locFile.done();
        if( added==0 )
            if( newLocFile.canWrite() )
                newLocFile.add( lati+","+longti+","+name);
        newLocFile.done();
    }

    private String getLocation( String n )
    {
        AppReadFile locFile = new AppReadFile(this.getApplicationContext(), LOCATION_FILE_NAME, "txt");
        boolean isdigit = n.matches("^[0-9]+.*");
        if( isdigit ) {
            // by number of lines
            int k = Integer.parseInt(n);
            while( locFile.hasNext()) {
                k--;
                String x = locFile.next();
                if( k==0 ) {
                    locFile.done();
                    return x;
                }
            }
        } else {
            while( locFile.hasNext()) {
                String loc = locFile.next();
                String gps[] = loc.split(",");
                if( gps != null && gps.length>2 && gps[2].equalsIgnoreCase(n) ) {
                    locFile.done();
                    return loc;
                }
            }
        }
        // can't find, return the first one
        locFile.done();
        locFile.again();
        if( locFile.hasNext() ) {
            String x = locFile.next();
            locFile.done();
            return x;
        }
        // I don't have anything
        return "40.773104,-74.318118,Swan";
    }

    private String locationTarget(EditText loctext)
    {
        String fromUI = loctext.getText().toString();
        String [] gps = fromUI.split(",");
//        return getLocation(gps[0]);
/*
        switch (gps.length)
        {
            case 4: // save the location specified by the first 3 text
                double lati = Double.parseDouble(gps[0]);
                double longti = Double.parseDouble(gps[1]);
                if( lati != 0 && longti != 0 && gps[2].matches("^[a-zA-Z]+.*")) // validated
                    addLocation( gps[0], gps[1], gps[2] );
                return gps[0]+","+gps[1]+","+gps[2];
            case 1: // line in the file
                return getLocation(gps[0]);
            case 2: // just use the location
                return fromUI;
            case 3: // just use the location with a name
                return fromUI;
            default: // no function, same as 1st in the file
                return getLocation("1");
        }
*/
        if (gps.length==1)
            return getLocation(gps[0]);
        else if (gps.length==2)
            return fromUI;
        else if (gps.length==3)
            return fromUI;
        else if (gps.length==4)
        {
            double lati = Double.parseDouble(gps[0]);
            double longti = Double.parseDouble(gps[1]);
            if( lati != 0 && longti != 0 && gps[2].matches("^[a-zA-Z]+.*")) // validated
                addLocation( gps[0], gps[1], gps[2] );
            return gps[0]+","+gps[1]+","+gps[2];
        } else
            return getLocation("1");
    }

    private String currentTarget;

    private String humanReadable( double x, double precision )
    {
        return String.valueOf(Math.round(x/precision)*precision);
    }

    private double distance( double latitude, double longitude, double targetLatitude, double targetLongitude )
    {
        // https://www.movable-type.co.uk/scripts/latlong.html
        double r = 6371e3;
        double phi1 = Math.toRadians(targetLatitude);
        double phi2 = Math.toRadians(latitude);
/*
        double lamda1 = Math.toRadians(targetLongitude);
        double lamda2 = Math.toRadians(longitude);
*/
        double deltaPhi = Math.toRadians(latitude-targetLatitude);
        double deltaLamda = Math.toRadians(longitude-targetLongitude);

        double a = Math.sin(deltaPhi/2) * Math.sin(deltaPhi/2) +
                Math.cos(phi1) * Math.cos(phi2) *
                        Math.sin(deltaLamda/2) * Math.sin(deltaLamda/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return r * c;
    }

    //    @Override
    public void onLocationChanged(Location loc) {
/*
        editLocation.setText("");
        pb.setVisibility(View.INVISIBLE);
        Toast.makeText(
                getBaseContext(),
                "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                        + loc.getLongitude(), Toast.LENGTH_SHORT).show();
        String longitude = "Longitude: " + loc.getLongitude();
        Log.v(TAG, longitude);
        String latitude = "Latitude: " + loc.getLatitude();
        Log.v(TAG, latitude);
*/
        double precision = 0.00001;
        FlowView surfaceView = (FlowView)flowFragment.getView().findViewById(R.id.wifi_surface);
//        Button gpsView = (Button)flowFragment.getView().findViewById(requestingViewId);
        EditText me1 = (EditText)surfaceView.getRootView().findViewById(R.id.me1);
        TextView me2 = (TextView)surfaceView.getRootView().findViewById(R.id.me2);
        if( locStatus<0 ) // get current location and set it in me1
        {
            String x = humanReadable(loc.getLatitude(), precision) +", "+humanReadable(loc.getLongitude(), precision);
            me1.setText(x);
            try {
                locationManager.removeUpdates(this);
                me2.setText("Got current location");
            } catch (SecurityException se) {
                me2.setText("No GPS");
            }
            return;
        }
        String target =  currentTarget==null ? (currentTarget=me1.getText().toString()) : currentTarget;
        String [] gps = target.split(",");
        if( gps==null || gps.length <2 ) return;
        double targetLatitude = Double.parseDouble(gps[0]);   // 40.7725612;
        double targetLongitude = Double.parseDouble(gps[1]);  // -74.2678638;
        double d = distance(loc.getLatitude(), loc.getLongitude(), targetLatitude, targetLongitude );
        Button choice = (Button)surfaceView.getRootView().findViewById(R.id.choice);
        boolean selected = choice.getBackground()==getResources().getDrawable(R.drawable.ninepatchon);
        String rpt = selected ? humanReadable(loc.getLatitude(), precision)+", "+humanReadable(loc.getLongitude(), precision) + " : "+humanReadable(d, 0.01) + "m to"
                                + (gps.length>2?" "+gps[2]+",":" ")+ String.valueOf(targetLatitude)+", "+String.valueOf(targetLongitude)
                        : humanReadable(d, 0.01) + (gps.length>2?"m to "+gps[2]:"m");

        try {
            if( locStatus == distance.length-1 ) {
                return;
            }
            me2.setText(rpt);
            while( locStatus < distance.length-1 && d < distance[locStatus] ) {
                locStatus++;
                if( locStatus < distance.length ) {
                    me2.setTextColor(getResources().getColor(foreground[locStatus]));
                    me2.setText(rpt);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime[locStatus], minDist[locStatus], this);
                    me2.setBackgroundResource(background[locStatus]);
                }
                if (locStatus >= distance.length-1) {
                    me2.setText("Arrived:"+rpt);
                    currentTarget = null;
                    locationManager.removeUpdates(this);
                }
            }
        } catch (SecurityException se) {
            me2.setText("No GPS");
        }


        // https://stackoverflow.com/questions/10893913/what-parameters-to-choose-in-gps-tracing-for-requestlocationupdates
        // https://stackoverflow.com/questions/6302175/requestlocationupdates-parameter-android

        /*------- To get city name from coordinates -------- */
/*        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                System.out.println(addresses.get(0).getLocality());
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                + cityName;
        editLocation.setText(s);*/
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

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
                break;
            case R.id.gps:
                requestedNetworkType = ConnectivityManager.TYPE_MOBILE;
                locationManager = (LocationManager)getSystemService(getBaseContext().LOCATION_SERVICE);
//                locationManager.
                TextView me2 = (TextView)view.getRootView().findViewById(R.id.me2);
                EditText me1 = (EditText)view.getRootView().findViewById(R.id.me1);
                String v = me1.getText().toString();
                if( v.equalsIgnoreCase("set"))
                {
                    // https://developers.google.com/maps/documentation/urls/android-intents
                    try {
                        me2.setBackgroundResource(background[0]);
                        me2.setTextColor(getResources().getColor(foreground[0]));
                        locStatus = -1;
                        currentTarget = null;
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
                        me2.setText("Getting current location ...");
                    } catch (SecurityException se) {
                        me2.setText("No GPS");
                    }
                    return;
                }
                if( v.equalsIgnoreCase("stop"))
                {
                    try {
                        locationManager.removeUpdates(this);
                        currentTarget = null;
                        me1.setText("");
                        me2.setText(getApplicationContext().getFilesDir().getAbsolutePath());
                    } catch (SecurityException se) {
                        me2.setText("No GPS");
                    }
                    return;
                }
                if( v.equalsIgnoreCase("emailwechat"))
                {
                    try {
                        ShareOut( LOCATION_FILE_NAME+".txt", "FILE", "SEND_MULTIPLE_WECHAT");
                    } catch (SecurityException se) {
                    }
                    return;
                }
                if( v.equalsIgnoreCase("email"))
                {
                    try {
                        ShareOut( LOCATION_FILE_NAME+".txt", "FILE", "SEND_MULTIPLE");
                    } catch (SecurityException se) {
                    }
                    return;
                }
                if( v.equalsIgnoreCase("send"))
                {
                    try {
                        ShareOut( LOCATION_FILE_NAME+".txt", "FILE", "SEND");
                    } catch (SecurityException se) {
                    }
                    return;
                }
                String target = locationTarget(me1);
                me1.setText(target);
                try {
                    locStatus = 0;
                    currentTarget = target;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime[locStatus], minDist[locStatus], this);
                    view.setBackgroundResource(R.drawable.blink);
                    me2.setBackgroundResource(R.drawable.blink);
                    me2.setTextColor(getResources().getColor(foreground[0]));
                    me2.setText("Monitoring ...");
                } catch (SecurityException se) {
                    me2.setText("No GPS");
                }
                // startDownload();                    TextView me2 = (TextView)view.getRootView().findViewById(R.id.me2);
                break;
            case R.id.choice:
                if( view.getBackground()==getResources().getDrawable(R.drawable.ninepatchon) )
                    view.setBackgroundResource(R.drawable.ninepatchoff);
                else
                    view.setBackgroundResource(R.drawable.ninepatchon);
                break;
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


    protected void ShareOut( String content, String inType, String outType ) {
        String text = content;
        if( inType.equalsIgnoreCase("FILE") ) {
            AppReadFile locFile = new AppReadFile(this.getApplicationContext(), content);
            text = locFile.all();
        }
        if (outType.equalsIgnoreCase("SEND_MULTIPLE")) { // plain email
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("text/plain");
//            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.putExtra(Intent.EXTRA_EMAIL, "me@myfamily.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "family tree");
            intent.putExtra(Intent.EXTRA_TEXT, text.toString());
//            startActivity(Intent.createChooser(intent, "Send Email"));
            startActivity(Intent.createChooser(intent, "Send Email"));
        }

        if (outType.equalsIgnoreCase("SEND_MULTIPLE_WECHAT")) {  // wechat way
            Parcelable.Creator<String> c = Parcel.STRING_CREATOR;
            Parcel p = Parcel.obtain();
            p.writeString(text.toString());
            p.setDataPosition(0);
            String s = p.readString();
            p.setDataPosition(0);
            Parcelable.Creator<AppParcelable> creator = AppParcelable.CREATOR;
            AppParcelable x = creator.createFromParcel(p);
            ArrayList<AppParcelable> textList = new ArrayList<>();
            textList.add(x); // Add your image URIs here

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, textList);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Share Email"));
        }

        if( outType.equalsIgnoreCase("SEND") )
        {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }
}
