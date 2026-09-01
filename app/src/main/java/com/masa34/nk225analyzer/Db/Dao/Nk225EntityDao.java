package com.masa34.nk225analyzer.Db.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Date;

import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;
@Dao
public interface Nk225EntityDao {

    @Query("SELECT * FROM nk225entity WHERE date > :date ORDER BY date ASC")
    List<Nk225Entity> findAfter(Date date);

    @Query("SELECT MAX(date) FROM nk225entity")
    Date findMaxDate();

    @Insert
    void insertAll(List<Nk225Entity> list);
}
