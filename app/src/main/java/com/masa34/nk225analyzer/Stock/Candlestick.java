package com.masa34.nk225analyzer.Stock;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Candlestick extends RealmObject {
    @PrimaryKey
    private long id;

    @Index
    private Date date;

    private double openingPrice;
    private double closingPrice;
    private double highPrice;
    private double lowPrice;

    private boolean marketClosing;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public void setOpeningPrice(double openingPrice) {
        this.openingPrice = openingPrice;
    }

    public double getClosingPrice() {
        return closingPrice;
    }

    public void setClosingPrice(double closingPrice) {
        this.closingPrice = closingPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public boolean getMarketClosing() {
        return marketClosing;
    }

    public void setMarketClosing(boolean marketClosing) {
        this.marketClosing = marketClosing;
    }
}
