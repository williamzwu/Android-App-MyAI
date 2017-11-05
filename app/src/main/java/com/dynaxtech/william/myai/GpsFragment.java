package com.dynaxtech.william.myai;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by william on 9/29/2017.
 */

public class GpsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View myView =inflater.inflate(R.layout.gps_layout, container, false );
        return myView;
    }
}
