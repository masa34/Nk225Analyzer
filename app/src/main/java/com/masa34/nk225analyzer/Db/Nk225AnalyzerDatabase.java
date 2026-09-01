package com.masa34.nk225analyzer.Db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.masa34.nk225analyzer.Db.Converter.DateConverter;
import com.masa34.nk225analyzer.Db.Dao.CandlestickDao;
import com.masa34.nk225analyzer.Db.Dao.MarketT1Dao;
import com.masa34.nk225analyzer.Db.Dao.Nk225EntityDao;
import com.masa34.nk225analyzer.Db.Entity.Candlestick;
import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;
import com.masa34.nk225analyzer.Db.Entity.MarketT1;

@Database(
        entities = {
            Candlestick.class,
            Nk225Entity.class,
            MarketT1.class
        },
        version = 1,
        exportSchema = true
)
@TypeConverters({DateConverter.class})
public abstract class Nk225AnalyzerDatabase extends RoomDatabase {
    public abstract CandlestickDao candlestickDao();
    public abstract Nk225EntityDao nk225EntityDao();
    public abstract MarketT1Dao marketT1Dao();
}
