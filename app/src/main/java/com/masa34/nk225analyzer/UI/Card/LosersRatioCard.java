package com.masa34.nk225analyzer.UI.Card;

import android.graphics.Color;
import com.masa34.nk225analyzer.Stock.Nk225Entity;

public class LosersRatioCard extends Nk225CardBase {

    public LosersRatioCard(Nk225Entity entity) {
        super(entity);
    }

    public int getItemViewType() {
        return TYPE_ITEM1;
    }

    public void bindViewHolder(ViewHolder holder) {
        ViewHolder1 holder1 = (ViewHolder1)holder;
        holder1.setTitle("騰落レシオ");
        if (entity.getMarketClosing()) {
            double losersRatio = entity.getLosersRatio();

            holder1.setValue(String.format("%+.2f", losersRatio));
            if (losersRatio >= 130.0) {
                holder1.setValueColor(Color.RED);
            } else if (losersRatio <= 70.0) {
                holder1.setValueColor(Color.BLUE);
            }
        } else {
            // 騰落レシオは引け後でないと計算できない（※仕様）
            holder1.setValue("-");
        }
    }
}
