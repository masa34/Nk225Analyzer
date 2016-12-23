package com.masa34.nk225analyzer.UI.Card;

import com.masa34.nk225analyzer.Stock.Nk225Entity;

public abstract class Nk225CardBase {
    public static final int TYPE_EVALUATION = 0;
    public static final int TYPE_ITEM1 = 1;
    public static final int TYPE_ITEM2 = 2;

    protected Nk225Entity entity;

    public Nk225CardBase(Nk225Entity entity) {
        this.entity = entity;
    }

    public abstract int getItemViewType();
    public abstract void bindViewHolder(ViewHolder holder);
}
