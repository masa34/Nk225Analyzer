package com.masa34.nk225analyzer.Util;

import com.masa34.nk225analyzer.Db.Dao.MarketT1Dao;
import com.masa34.nk225analyzer.UI.Nk225AnalyzerApp;
import com.masa34.nk225analyzer.Db.Dao.CandlestickDao;
import com.masa34.nk225analyzer.Db.Entity.Candlestick;
import com.masa34.nk225analyzer.Db.Entity.MarketT1;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class StockUtils {
    // コンストラクタ
    // インスタンス化禁止
    private StockUtils() {
    }

    public static Candlestick MergeChart(List<Candlestick> candlesticks) {

        if (candlesticks.size() > 0) {

            // 日時の昇順にソート
            Collections.sort(candlesticks, new Comparator<Candlestick>() {
                public int compare(Candlestick candlestick1, Candlestick candlestick2) {
                    return candlestick1.getDate().compareTo(candlestick2.getDate());
                }
            });

            int first = 0;
            int last = candlesticks.size() - 1;
            Candlestick candlestick = new Candlestick();
            candlestick.setDate(candlesticks.get(last).getDate());
            candlestick.setOpeningPrice(candlesticks.get(first).getOpeningPrice());
            candlestick.setClosingPrice(candlesticks.get(last).getClosingPrice());
            candlestick.setHighPrice(Collections.max(candlesticks, new Comparator<Candlestick>() {
                public int compare(Candlestick candlestick1, Candlestick candlestick2) {
                    double price1 = candlestick1.getHighPrice();
                    double price2 = candlestick2.getHighPrice();
                    if (price1 > price2) {
                        return 1;
                    } else if (price1 < price2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }).getHighPrice());
            candlestick.setLowPrice(Collections.min(candlesticks, new Comparator<Candlestick>() {
                public int compare(Candlestick candlestick1, Candlestick candlestick2) {
                    double price1 = candlestick1.getLowPrice();
                    double price2 = candlestick2.getLowPrice();
                    if (price1 > price2) {
                        return 1;
                    } else if (price1 < price2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }).getLowPrice());

            return candlestick;
        }

        return null;
    }

    // 日経平均株価（終値）
    public static double value(Date date) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        Candlestick candlestick = dao.findByDate(date);
        if (candlestick != null) {
            return candlestick.getClosingPrice();
        }

        return 0.0;
    }

    // 前日比
    public static double change(Date date) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        List<Candlestick> candlesticks = dao.findLatestUpTo(date, 2);
        if (candlesticks.size() < 2) {
            return 0.0;
        }

        return candlesticks.get(0).getClosingPrice() - candlesticks.get(1).getClosingPrice();
    }

    // n日移動平均算出
    public static double movingAverage(Date date, int period) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        List<Candlestick> candlesticks = dao.findLatestUpTo(date, period);
        if (candlesticks.size() < period) {
            return 0.0;
        }

        double total = 0.0;
        for (Candlestick c : candlesticks) {
            total += c.getClosingPrice();
        }

        return total / period;
    }

    // 値幅
    public static double priceRange(Date date) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        Candlestick candlestick = dao.findByDate(date);
        if (candlestick != null) {
            return candlestick.getHighPrice() - candlestick.getLowPrice();
        }

        return 0.0;
    }

    // n日平均値幅
    public static double priceRangeAverage(Date date, int period) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        List<Candlestick> candlesticks = dao.findLatestUpTo(date, period);
        if (candlesticks.size() < period) {
            return 0.0;
        }

        double total = 0.0;
        for (Candlestick c : candlesticks) {
            total += (c.getHighPrice() - c.getLowPrice());
        }

        return total / period;
    }

    // n日RSI
    public static double rsi(Date date, int period) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        List<Candlestick> candlesticks = dao.findLatestUpTo(date, period + 1);
        if (candlesticks.size() < period + 1) {
            return 50.0;
        }

        double numerator = 0.0;
        double denominator = 0.0;

        double price = candlesticks.get(0).getClosingPrice();

        for (int i = 1; i < candlesticks.size(); i++) {
            double prev = candlesticks.get(i).getClosingPrice();
            double diff = price - prev;

            denominator += Math.abs(diff);
            if (diff > 0) numerator += diff;

            price = prev;
        }

        if (denominator == 0.0) return 50.0;

        return numerator / denominator * 100.0;
    }

    // n日RCI
    public static double rci(Date date, int period) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        List<Candlestick> candlesticks = dao.findLatestUpTo(date, period + 1);
        if (candlesticks.size() < period + 1) {
            return 0.0;
        }

        // 価格順位
        int[] ranks = new int[period];
        Arrays.fill(ranks, 1);

        for (int i = 0; i < period; ++i) {
            double value = candlesticks.get(i).getClosingPrice();
            for (int j = 0; j < period; ++j) {
                if (candlesticks.get(j).getClosingPrice() > value) {
                    ranks[i]++;
                }
            }
        }

        // 日付順位と価格順位の差の2乗の合計
        double d = 0.0;

        for (int i = 0; i < period; ++i) {

            // 同値の場合は平均順位にする
            double rank = 0.0;
            int count = 0;

            for (int j = 0; j < period; ++j) {
                if (ranks[i] == ranks[j]) {
                    rank += (ranks[i] + count);
                    ++count;
                }
            }

            rank /= count;

            // 日付順位との差の2乗
            d += ((rank - (i + 1)) * (rank - (i + 1)));
        }

        // RCI 計算式
        double rci = (1.0 - ((6.0 * d) / (period * (period * period - 1)))) * 100.0;

        return rci;
    }

    // n日標準偏差
    public static double standardDeviation(Date date, int period) {

        double ma = movingAverage(date, period);

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        List<Candlestick> candlesticks = dao.findLatestUpTo(date, period);
        if (candlesticks.size() < period) {
            return 0.0;
        }

        double total2 = 0.0;
        for (Candlestick c : candlesticks) {
            double diff = c.getClosingPrice() - ma;
            total2 += diff * diff;
        }

        return Math.sqrt(total2 / period);
    }

    // n期間サイコロジカル算出
    public static double psychological(Date date, int period) {

        CandlestickDao dao = Nk225AnalyzerApp.getDatabase().candlestickDao();

        List<Candlestick> candlesticks = dao.findLatestUpTo(date, period + 1);
        if (candlesticks.size() < period + 1) {
            return 50.0;
        }

        int plus = 0;
        int minus = 0;

        double price = candlesticks.get(0).getClosingPrice();

        for (int i = 1; i < candlesticks.size(); i++) {
            double prev = candlesticks.get(i).getClosingPrice();
            double diff = price - prev;

            if (diff > 0) {
                plus++;
            } else if (diff < 0) {
                minus++;
            }

            price = prev;
        }

        if (plus + minus == 0) {
            return 50.0;
        }

        return plus / (double)(plus + minus) * 100.0;
    }

    // 騰落レシオ
    public static double losersRatio(Date date, int period) {

        MarketT1Dao dao = Nk225AnalyzerApp.getDatabase().marketT1Dao();

        List<MarketT1> t1s = dao.findLatestUpTo(date, period);
        if (t1s.size() < period) {
            return 0.0;
        }

        int advances = 0;
        int decliners = 0;

        for (MarketT1 m : t1s) {
            advances += m.getAdvances();
            decliners += m.getDecliners();
        }

        if (decliners == 0) {
            return Double.MAX_VALUE;
        }

        return (double) advances / decliners * 100.0;
    }
}
