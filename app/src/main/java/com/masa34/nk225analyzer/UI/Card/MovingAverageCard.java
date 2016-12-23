package com.masa34.nk225analyzer.UI.Card;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class MovingAverageCard extends Nk225CardBase {

    public MovingAverageCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM2;
    }

    public void bindViewHolder(ViewHolder holder) {
        ViewHolder2 holder2 = (ViewHolder2)holder;
        holder2.setTitle("移動平均線");
        holder2.setSubTitle1("5日");
        holder2.setValue1(String.format("%.2f", entity.getMovingAverage5()));
        holder2.setSubTitle2("25日");
        holder2.setValue2(String.format("%.2f", entity.getMovingAverage25()));
    }
}
