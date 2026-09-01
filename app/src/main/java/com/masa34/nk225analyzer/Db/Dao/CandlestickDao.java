package com.masa34.nk225analyzer.Db.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Date;

import com.masa34.nk225analyzer.Db.Entity.Candlestick;
import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;

@Dao
public interface CandlestickDao {
    // ① 日付で1件
    @Query("SELECT * FROM candlestick WHERE date = :date LIMIT 1")
    Candlestick findByDate(Date date);

    // ② 日付以下の最新 n 件
    @Query("SELECT * FROM candlestick WHERE date <= :date ORDER BY date DESC LIMIT :limit")
    List<Candlestick> findLatestUpTo(Date date, int limit);

    // ③ 日付範囲
    @Query("SELECT * FROM candlestick WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    List<Candlestick> findByDateRange(Date start, Date end);

    // ④ 全件
    @Query("SELECT * FROM candlestick ORDER BY date ASC")
    List<Candlestick> findAll();

    // ⑤ 最新1件
    @Query("SELECT * FROM candlestick ORDER BY date DESC LIMIT 1")
    Candlestick findLatest();

    @Query("SELECT * FROM candlestick WHERE date > :date ORDER BY date ASC")
    List<Candlestick> findAfter(Date date);

    @Query("SELECT MAX(date) FROM candlestick")
    Date findMaxDate();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Candlestick candlestick);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Candlestick> candlesticks);
}
