package com.masa34.nk225analyzer.UI.Card;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.masa34.nk225analyzer.R;

public class ViewHolder1 extends ViewHolder {
    private final String TAG = "ViewHolder1";

    private TextView value1;

    public ViewHolder1(View v) {
        super(v);
        Log.d(TAG, "ViewHolder1");
        value1 = (TextView) v.findViewById(R.id.subTitle1);
    }

    public void setValue(String value) {
        Log.d(TAG, "setValue");
        this.value1.setText(value);
    }

    public void setValueColor(int color) {
        Log.d(TAG, "setValueColor");
        this.value1.setTextColor(color);
    }
}
