package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;

import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Stock.Nk225Entity;

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

        double score = calcScore();
        if (score >= 75.0) {
            if (score >= 87.5) {
                evaluationHholder.setEvaluation("天井");
                evaluationHholder.setEvaluationColor(Color.parseColor("#FF0000"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_top);
            } else {
                evaluationHholder.setEvaluation("割高");
                evaluationHholder.setEvaluationColor(Color.parseColor("#FF8000"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_high);
            }
        } else if (score <= 25.0) {
            if (score <= 12.5) {
                evaluationHholder.setEvaluation("底");
                evaluationHholder.setEvaluationColor(Color.parseColor("#0000FF"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_bottom);
            } else {
                evaluationHholder.setEvaluation("割安");
                evaluationHholder.setEvaluationColor(Color.parseColor("#0080FF"));
                evaluationHholder.setEvaluationBackground(R.drawable.style_low);
            }
        } else {
            evaluationHholder.setEvaluation("中立");
            evaluationHholder.setEvaluationColor(Color.parseColor("#000000"));
            evaluationHholder.setEvaluationBackground(R.drawable.style_normal);
        }

        String updated = "";
        if (!entity.getMarketClosing()) {
            try {
                // ※騰落レシオを計算するための情報が取得できなくなったため暫定対応とする
                //updated = new SimpleDateFormat("Hmm更新").format(entity.getDate());
            } catch (NumberFormatException e) {
            }
        }
        evaluationHholder.setUpdated(updated);
    }

    private double calcBollingerBandScore(Nk225Entity entity) {
        double hBand = entity.getMovingAverage25() + 2.2 * entity.getStandardDeviation();
        double lBand = entity.getMovingAverage25() - 2.2 * entity.getStandardDeviation();
        return (entity.getValue() - lBand) / (hBand - lBand) * 100.0;
    }

    private double calcEstrangementRateScore(Nk225Entity entity) {
        double hRate = 3.5;
        double lRate = -3.5;
        double estrangementRate = (entity.getValue() - entity.getMovingAverage25()) / entity.getMovingAverage25() * 100.0;
        return (estrangementRate - lRate) / (hRate - lRate) * 100.0;
    }

    private double calcLosersRatioScore(Nk225Entity entity) {
        double hRatio = 146.0;
        double lRatio = 54.0;
        double losersRatio = entity.getLosersRatio();
        return (losersRatio - lRatio) / (hRatio - lRatio) * 100.0;
    }

    private double calcPsychologicalScore(Nk225Entity entity) {
        return entity.getPsychological();
    }

    private double calcRsiScore(Nk225Entity entity) {
        return entity.getRsi();
    }

    private double calcRciScore(Nk225Entity entity) {
        double hRci = 100.0;
        double lRci = -100.0;
        double rci = entity.getRci();
        return (rci - lRci) / (hRci - lRci) * 100.0;
    }

    private double calcScore() {
        List<Double> scores = new ArrayList<>();
        scores.add(calcBollingerBandScore(entity));
        scores.add(calcEstrangementRateScore(entity));
        scores.add(calcPsychologicalScore(entity));
        scores.add(calcRsiScore(entity));
        scores.add(calcRciScore(entity));
        //scores.add(calcLosersRatioScore(entity));

        double totalScore = 0.0;
        for (Double score : scores) {
            totalScore += score;
        }

        return totalScore / scores.size();
    }
}
