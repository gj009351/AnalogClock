package com.duke.analogclocksamples;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duke.analogclock.AnalogClock;
import com.duke.analogclock.AnalogClockCustomDrawable;

/**
 * Created by andy on 16-8-5.
 */
public class ClockFragment extends Fragment {

    public static final String KEY_SECONDS = "has_second";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = initView(inflater);
        return view;
    }

    private View initView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.analog_clock_container, null);
        AnalogClock analogClock = (AnalogClock) view.findViewById(R.id.analog_clock);
        AnalogClockCustomDrawable clockWithDrawable = (AnalogClockCustomDrawable) view.findViewById(R.id.analog_clock_drawable);
        Bundle bundle = getArguments();
        if(bundle != null){
            boolean hasSecond = bundle.getBoolean(KEY_SECONDS);
            analogClock.enableSeconds(hasSecond);
            clockWithDrawable.enableSeconds(hasSecond);
        }
        return view;
    }
}
