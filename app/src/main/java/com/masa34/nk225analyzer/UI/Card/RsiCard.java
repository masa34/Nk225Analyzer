package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;
import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class RsiCard extends Nk225CardBase {

    public RsiCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM1;
    }

    public void bindViewHolder(ViewHolder holder) {
        double rsi = entity.getRsi();

        ViewHolder1 holder1 = (ViewHolder1)holder;
        holder1.setTitle("RSI");
        holder1.setValue(String.format("%.2f", rsi));
        if (rsi >= 70.0) {
            holder1.setValueColor(Color.RED);
        } else if (rsi <= 30.0) {
            holder1.setValueColor(Color.BLUE);
        }
    }
}
