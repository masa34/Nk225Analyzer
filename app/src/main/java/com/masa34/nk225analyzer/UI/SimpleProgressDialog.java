package com.masa34.nk225analyzer.UI;

import android.app.Dialog;
import android.content.Context;

import com.masa34.nk225analyzer.R;

public class SimpleProgressDialog extends Dialog {

    public SimpleProgressDialog(Context context) {
        super(context, R.style.Theme_SimpleProgressDialog);

        setContentView(R.layout.simple_progress_dialog);
        setCancelable(false);
    }
}
