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
        ViewHolder2 holder2 = (ViewHolder2)holder;
        holder2.setTitle("日経平均");
        holder2.setSubTitle1("終値");
        holder2.setValue1(String.format("%.2f", entity.getValue()));
        holder2.setSubTitle2("前日比");
        holder2.setValue2(String.format("%+.2f", entity.getChange()));
    }
}
