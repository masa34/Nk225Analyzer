package com.masa34.nk225analyzer.UI.Card;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.masa34.nk225analyzer.R;

public class ViewHolder2 extends ViewHolder {
    private final String TAG = "ViewHolder2";

    private TextView subTitle1;
    private TextView value1;
    private TextView subTitle2;
    private TextView value2;

    public ViewHolder2(View v) {
        super(v);
        Log.d(TAG, "ViewHolder2");
        subTitle1 = (TextView) v.findViewById(R.id.subTitle1);
        value1 = (TextView) v.findViewById(R.id.value1);
        subTitle2 = (TextView) v.findViewById(R.id.subTitle2);
        value2 = (TextView) v.findViewById(R.id.value2);
    }

    public void setSubTitle1(String subTitle) {
        Log.d(TAG, "setSubTitle1");
        this.subTitle1.setText(subTitle);
    }

    public void setValue1(String value) {
        Log.d(TAG, "setValue1");
        this.value1.setText(value);
    }

    public void setValueColor1(int color) {
        Log.d(TAG, "setValueColor1");
        this.value1.setTextColor(color);
    }

    public void setSubTitle2(String subTitle) {
        Log.d(TAG, "setSubTitle2");
        this.subTitle2.setText(subTitle);
    }

    public void setValue2(String value) {
        Log.d(TAG, "setValue2");
        this.value2.setText(value);
    }

    public void setValueColor2(int color) {
        Log.d(TAG, "setValueColor2");
        this.value2.setTextColor(color);
    }
}
