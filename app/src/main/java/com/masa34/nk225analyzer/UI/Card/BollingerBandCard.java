package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;
import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class BollingerBandCard extends Nk225CardBase {

     public BollingerBandCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM2;
    }

    public void bindViewHolder(ViewHolder holder) {
        double hBand = entity.getMovingAverage25() + 2.0 * entity.getStandardDeviation();
        double lBand = entity.getMovingAverage25() - 2.0 * entity.getStandardDeviation();

        ViewHolder2 holder2 = (ViewHolder2)holder;
        holder2.setTitle("ボリンジャーバンド");
        holder2.setSubTitle1("+2σ");
        holder2.setValue1(String.format("%.2f", hBand));
        if (entity.getValue() >= hBand) {
            holder2.setValueColor1(Color.RED);
        }
        holder2.setSubTitle2("-2σ");
        holder2.setValue2(String.format("%.2f", lBand));
        if (entity.getValue() <= lBand) {
            holder2.setValueColor2(Color.BLUE);
        }
    }
}
