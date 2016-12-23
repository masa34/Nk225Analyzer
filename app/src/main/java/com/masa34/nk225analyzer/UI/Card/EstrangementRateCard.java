package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;
import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class EstrangementRateCard extends Nk225CardBase {

    public EstrangementRateCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM1;
    }

    public void bindViewHolder(ViewHolder holder) {
        double estrangementRate = (entity.getValue() - entity.getMovingAverage25()) / entity.getMovingAverage25() * 100.0;

        ViewHolder1 holder1 = (ViewHolder1)holder;
        holder1.setTitle("乖離率");
        holder1.setValue(String.format("%+.2f", estrangementRate));
        if (estrangementRate >= 3.5) {
            holder1.setValueColor(Color.RED);
        } else if (estrangementRate <= -3.5) {
            holder1.setValueColor(Color.BLUE);
        }
    }
}
