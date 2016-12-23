package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class RciCard extends Nk225CardBase {

    public RciCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM1;
    }

    public void bindViewHolder(ViewHolder holder) {
        double rci = entity.getRci();

        ViewHolder1 holder1 = (ViewHolder1)holder;
        holder1.setTitle("RCI");
        holder1.setValue(String.format("%.2f", rci));
        if (rci >= 60.0) {
            holder1.setValueColor(Color.RED);
        } else if (rci <= -60.0) {
            holder1.setValueColor(Color.BLUE);
        }
    }
}
