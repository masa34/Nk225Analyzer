package com.masa34.nk225analyzer.UI.Card;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

// ※要実装
public class StochasticsCard extends Nk225CardBase {

    public StochasticsCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM2;
    }

    public void bindViewHolder(ViewHolder holder) {
        ViewHolder2 holder2 = (ViewHolder2)holder;
        holder2.setTitle("ストキャスティックス");
        holder2.setSubTitle1("%K");
        //holder2.setValue1(String.format("%+.2f", k));
        holder2.setValue1("---");
        holder2.setSubTitle2("%D");
        //holder2.setValue2(String.format("%+.2f", d));
        holder2.setValue2("---");
    }
}
