package com.masa34.nk225analyzer.Util;

public class Marker {
    public BusinessDay time;    // "time": { "year": 2026, "month": 9, "day": 20 },
    public String position;     // aboveBar / belowBar
    public String color;        // "#FF0000" など
    public String shape;        // "circle"
    public String text;         // 空文字

    public Marker(BusinessDay date, String position, String color, String shape, String text) {
        this.time = date;
        this.position = position;
        this.color = color;
        this.shape = shape;
        this.text = text;
    }
}
