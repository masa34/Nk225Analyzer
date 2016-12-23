package com.masa34.nk225analyzer.UI.Card;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class Nk225Card extends Nk225CardBase {

    public Nk225Card(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM2;
    }

    public void bindViewHolder(ViewHolder holder) {
        double score = calcScore();
        String eval = "中立";
        if (score >= 87.5) {
            eval = "天井";
        } else if (score >= 75.0) {
            eval = "高値圏";
        } else if (score <= 12.5) {
            eval = "底";
        } else if (score <= 25.0) {
            eval = "安値圏";
        }

        ViewHolder2 holder2 = (ViewHolder2)holder;
        holder2.setTitle("日経平均");
        holder2.setSubTitle1("終値");
        holder2.setValue1(String.format("%.2f", entity.getValue()));
        holder2.setSubTitle2("前日比");
        holder2.setValue2(String.format("%+.2f", entity.getChange()));

        /*
        ViewHolderNk225 holderNk225 = (ViewHolderNk225)holder;
        holderNk225.setEval(String.format("%s(%d)", eval, (int)score));
        holderNk225.setTitle("日経平均");
        holderNk225.setSubTitle1("終値");
        holderNk225.setValue1(String.format("%.2f", entity.getValue()));
        holderNk225.setSubTitle2("前日比");
        holderNk225.setValue2(String.format("%+.2f", entity.getChange()));
        */
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
        double bollingerBandScore = calcBollingerBandScore(entity);
        double estrangementRateScore = calcEstrangementRateScore(entity);
        double losersRatioScore = calcLosersRatioScore(entity);
        double psychologicalScore = calcPsychologicalScore(entity);
        double rsiScore = calcRsiScore(entity);
        double rciScore = calcRciScore(entity);

        return (bollingerBandScore + estrangementRateScore + losersRatioScore + psychologicalScore + rsiScore  + rciScore) / 6;
    }
}
