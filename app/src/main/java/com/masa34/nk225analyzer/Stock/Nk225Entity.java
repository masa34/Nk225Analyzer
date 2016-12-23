package com.masa34.nk225analyzer.Stock;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Nk225Entity extends RealmObject implements Serializable {
    @PrimaryKey
    private long id;

    @Index
    private Date date;

    // 日経平均株価(終値)
    private double value;

    // 前日比
    private double change;

    // 騰落レシオ
    private double losersRatio;

    // 5日移動平均
    private double movingAverage5;

    // 25日移動平均
    private double movingAverage25;

    // 25日標準偏差（σ）
    private double standardDeviation25;

    // RSI
    private double rsi;

    // RCI
    private double rci;

    // サイコロ
    private double psychological;

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

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getLosersRatio() {
        return losersRatio;
    }

    public void setLosersRatio(double losersRatio) {
        this.losersRatio = losersRatio;
    }

    public double getMovingAverage5() {
        return movingAverage5;
    }

    public void setMovingAverage5(double ma5) {
        this.movingAverage5 = ma5;
    }

    public double getMovingAverage25() {
        return movingAverage25;
    }

    public void setMovingAverage25(double ma25) {
        this.movingAverage25 = ma25;
    }

    public double getStandardDeviation() {
        return standardDeviation25;
    }

    public void setStandardDeviation(double sigma) {
        this.standardDeviation25 = sigma;
    }

    public double getRsi() {
        return rsi;
    }

    public void setRsi(double rsi) {
        this.rsi = rsi;
    }

    public double getRci() {
        return rci;
    }

    public void setRci(double rci) {
        this.rci = rci;
    }

    public double getPsychological() {
        return psychological;
    }

    public void setPsychological(double psychological) {
        this.psychological = psychological;
    }
}
