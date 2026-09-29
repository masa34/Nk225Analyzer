package com.masa34.nk225analyzer.Util;

public class Candle {
    public BusinessDay time;
    public double open;
    public double high;
    public double low;
    public double close;

    public Candle(BusinessDay date, double open, double high, double low, double close) {
        this.time = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}
