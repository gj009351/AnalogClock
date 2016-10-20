package com.duke.analogclock;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private String[] mTabNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        List<Fragment> list = new ArrayList<>();
        mTabNames = getResources().getStringArray(R.array.tab_name);

        ClockFragment clockFragment1 = new ClockFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putBoolean(ClockFragment.KEY_SECONDS, true);
        clockFragment1.setArguments(bundle1);
        list.add(clockFragment1);

        ClockFragment clockFragment2 = new ClockFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(ClockFragment.KEY_SECONDS, false);
        clockFragment2.setArguments(bundle2);
        list.add(clockFragment2);

        ClockPagerAdapter adapter = new ClockPagerAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(0);
        setTitle(mTabNames[0]);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setTitle(mTabNames[position]);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
