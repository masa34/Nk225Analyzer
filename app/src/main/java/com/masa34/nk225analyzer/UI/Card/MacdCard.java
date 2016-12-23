package com.masa34.nk225analyzer.UI.Card;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

// ※要実装
public class MacdCard extends Nk225CardBase {

    public MacdCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM2;
    }

    public void bindViewHolder(ViewHolder holder) {
        ViewHolder2 holder2 = (ViewHolder2)holder;
        holder2.setTitle("MACD");
        holder2.setSubTitle1("MACD");
        //holder2.setValue1(String.format("%.2f", macd));
        holder2.setValue1("---");
        holder2.setSubTitle2("シグナル");
        //holder2.setValue2(String.format("%.2f", signal));
        holder2.setValue2("---");
    }
}
