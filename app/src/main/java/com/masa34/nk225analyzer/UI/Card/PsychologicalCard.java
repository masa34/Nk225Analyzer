package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;
import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class PsychologicalCard extends Nk225CardBase {

    public PsychologicalCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM1;
    }

    public void bindViewHolder(ViewHolder holder) {
        double psychological = entity.getPsychological();

        ViewHolder1 holder1 = (ViewHolder1) holder;
        holder1.setTitle("サイコロジカル・ライン");
        holder1.setValue(String.format("%.2f", psychological));
        if (psychological >= 75.0) {
            holder1.setValueColor(Color.RED);
        } else if (psychological <= 25.0) {
            holder1.setValueColor(Color.BLUE);
        }
    }
}
