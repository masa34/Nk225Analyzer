package com.masa34.nk225analyzer.UI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Nk225PagerAdapter extends FragmentStatePagerAdapter {

    private final String TAG = "Nk225PagerAdapter";

    private List<Nk225Entity> nk225Entities = new ArrayList<>();

    public Nk225PagerAdapter(FragmentManager fm) {
        super(fm);
        Log.d(TAG, "Nk225FragmentPagerAdapter");
    }

    public void setNk225Entitiy(List<Nk225Entity> entities)
    {
        nk225Entities = entities;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG, "getItem");

        if (nk225Entities.isEmpty()) {
            return  new EmptyFragment();
        }

        Nk225Fragment fragment = new Nk225Fragment();
        Nk225Entity data = nk225Entities.get(position);
        Bundle args = new Bundle();
        args.putSerializable("NK225", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount");

        if (nk225Entities.isEmpty()) {
            return 1;
        }

        return nk225Entities.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d(TAG, "getPageTitle");

        if (nk225Entities.isEmpty()) {
            return "NODATA";
        }

        Nk225Entity data = nk225Entities.get(position);
        return new SimpleDateFormat("yyyy/MM/dd").format(data.getDate());
    }

    @Override
    public final int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
