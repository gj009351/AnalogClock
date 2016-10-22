package com.duke.analogclocksamples;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by andy on 16-8-5.
 */
public class ClockPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mList;

    public ClockPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public ClockPagerAdapter(FragmentManager fm, List<Fragment> mList) {
        super(fm);
        this.mList = mList;
    }

    @Override
    public Fragment getItem(int position) {
        if(mList != null){
            if(position < mList.size()){
                return mList.get(position);
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        if(mList != null){
            return mList.size();
        }
        return 0;
    }
}
