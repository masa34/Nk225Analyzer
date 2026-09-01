package com.masa34.nk225analyzer.Db.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Date;

import com.masa34.nk225analyzer.Db.Entity.MarketT1;

@Dao
public interface MarketT1Dao {
    @Query("SELECT * FROM market_t1 ORDER BY date DESC")
    List<MarketT1> findAll();

    @Query("SELECT * FROM market_t1 WHERE date <= :date ORDER BY date DESC LIMIT :limit")
    List<MarketT1> findLatestUpTo(Date date, int limit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MarketT1 entity);
}
