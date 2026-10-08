package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;

import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;
import com.masa34.nk225analyzer.Util.StockUtils;

import java.util.ArrayList;
import java.util.List;

public class ComprehensiveEvaluationCard extends Nk225CardBase {

    public ComprehensiveEvaluationCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_EVALUATION;
    }

    public void bindViewHolder(ViewHolder holder) {
        EvaluationViewHolder evaluationHholder = (EvaluationViewHolder)holder;
        evaluationHholder.setTitle("総合評価");

        var result = StockUtils.getStockEvaluation((entity));

        switch (result.status) {
            case TOP:
                evaluationHholder.setEvaluation("天井");
                evaluationHholder.setEvaluationColor(Color.parseColor("#FF0000"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_top);
                break;
            case EXPENSIVE:
                evaluationHholder.setEvaluation("割高");
                evaluationHholder.setEvaluationColor(Color.parseColor("#FF8000"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_high);
                break;
            case NEUTRAL:
                evaluationHholder.setEvaluation("中立");
                evaluationHholder.setEvaluationColor(Color.parseColor("#000000"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_normal);
                break;
            case CHEAP:
                evaluationHholder.setEvaluation("割安");
                evaluationHholder.setEvaluationColor(Color.parseColor("#0080FF"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_low);
                break;
            case BOTTOM:
                evaluationHholder.setEvaluation("底");
                evaluationHholder.setEvaluationColor(Color.parseColor("#0000FF"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_bottom);
                break;
        }
    }
}
