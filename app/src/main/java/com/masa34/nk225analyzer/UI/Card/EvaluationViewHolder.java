package com.masa34.nk225analyzer.UI.Card;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.masa34.nk225analyzer.R;

public class EvaluationViewHolder extends ViewHolder {

    private final String TAG = "EvaluationViewHolder";

    private TextView evaluation;
    private TextView updated;

    public EvaluationViewHolder(View v) {
        super(v);
        Log.d(TAG, "EvaluationViewHolder");
        evaluation = (TextView)v.findViewById(R.id.evaluation);
        updated = (TextView)v.findViewById(R.id.updated);
    }

    public void setEvaluation(String evaluation) {
        Log.d(TAG, "setEvaluation");
        this.evaluation.setText(evaluation);
    }

    public void setEvaluationColor(int color) {
        Log.d(TAG, "setEvaluationColor");
        this.evaluation.setTextColor(color);
    }

    public void setUpdated(String updated) {
        Log.d(TAG, "setUpdated");
        this.updated.setText(updated);
    }

    public void setEvaluationBackground(int id) {
        Log.d(TAG, "setEvaluationBackground");
        this.evaluation.setBackgroundResource(id);
    }
}
