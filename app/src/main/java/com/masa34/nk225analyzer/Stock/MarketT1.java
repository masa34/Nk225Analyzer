package com.masa34.nk225analyzer.Stock;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class MarketT1 extends RealmObject {
    @PrimaryKey
    private long id;

    @Index
    private Date date;

    // 出来高
    private long volume;

    // 売買代金
    private long turnover;

    // 値上がり銘柄
    private int advances;

    // 値下がり銘柄
    private int decliners;

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

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public long getTurnover() {
        return turnover;
    }

    public void setTurnover(long turnover) {
        this.turnover = turnover;
    }

    public int getAdvances() {
        return advances;
    }

    public void setAdvances(int advances) {
        this.advances = advances;
    }

    public int getDecliners() {
        return decliners;
    }

    public void setDecliners(int decliners) {
        this.decliners = decliners;
    }
}
