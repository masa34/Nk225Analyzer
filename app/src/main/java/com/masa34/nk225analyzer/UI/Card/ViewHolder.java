package com.masa34.nk225analyzer.UI.Card;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.masa34.nk225analyzer.R;

public class ViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = "ViewHolder";

    protected TextView title;

    public ViewHolder(View v) {
        super(v);
        Log.d(TAG, "ViewHolder");
        title = (TextView) v.findViewById(R.id.title);
    }

    public void setTitle(String title) {
        Log.d(TAG, "setTitle");
        this.title.setText(title);
    }
}
