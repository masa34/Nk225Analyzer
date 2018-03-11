package com.masa34.nk225analyzer.Stock;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import io.realm.Realm;
import io.realm.Sort;

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

        double value = 0.0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .equalTo("date", date)
                    .findAll();

            if (results.size() >= 1) {
                value = results.get(0).getClosingPrice();
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return value;
    }

    // 前日比
    public static double change(Date date) {

        double change = 0.0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, 2);

            if (results.size() >= 2) {
                change = results.get(0).getClosingPrice() - results.get(1).getClosingPrice();
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return change;
    }

    // n日移動平均算出
    public static double movingAverage(Date date, int period) {

        if (period <= 0) {
            // エラー
        }

        double total = 0.0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, period);

            for (ListIterator it = results.listIterator(); it.hasNext(); ) {
                total += ((Candlestick) it.next()).getClosingPrice();
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return total / period;
    }

    // 値幅
    public static double priceRange(Date date) {

        double range = 0.0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .equalTo("date", date)
                    .findAll();

            if (results.size() >= 1) {
                range = results.get(0).getHighPrice() - results.get(0).getLowPrice();
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return range;
    }

    // n日移動平均算出
    public static double priceRangeAverage(Date date, int period) {

        if (period <= 0) {
            // エラー
        }

        double total = 0.0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, period);

            for (ListIterator it = results.listIterator(); it.hasNext(); ) {
                total += (((Candlestick) it.next()).getHighPrice() - ((Candlestick) it.next()).getLowPrice());
            }
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return total / period;
    }

    // n日RSI
    public static double rsi(Date date, int period) {

        if (period <= 0) {
            // エラー
        }

        double numerator = 0.0;
        double denominator = 0.0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, period + 1);

            double price = results.get(0).getClosingPrice();
            for (ListIterator it = results.listIterator(1); it.hasNext(); ) {
                double prevPrice = ((Candlestick) it.next()).getClosingPrice();

                denominator += Math.abs(price - prevPrice);

                if (price > prevPrice) {
                    numerator += (price - prevPrice);
                }

                price = prevPrice;
            }

            // n日間変動なし
            if (denominator == 0.0) {
                return 50.0;
            }
        } catch (IndexOutOfBoundsException e) {
            // 例外をThrow
            //throw new Exception();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return numerator / denominator * 100.0;
    }

    // n日RCI
    public static double rci(Date date, int period) {

        if (period <= 0) {
            // エラー
        }

        double rci = 0.0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, period + 1);

            // 価格順位
            int[] ranks = new int[period];
            Arrays.fill(ranks, 1);
            for (int i = 0; i < period; ++i) {
                double value = results.get(i).getClosingPrice();
                for (int j = 0; j < period; ++j) {
                    if (results.get(j).getClosingPrice() > value) {
                        ranks[i]++;
                    }
                }
            }

            // 日付順位と価格順位の差の2乗の合計
            double d = 0.0;
            for (int i = 0; i < period; ++i) {
                // 同値の場合は平均の順位とする
                double rank = 0.0;
                int count = 0;
                for (int j = 0; j < period; ++j) {
                    if (ranks[i] == ranks[j]) {
                        rank += (ranks[i] + count);
                        ++count;
                    }
                }
                rank /= count;

                // 日付順位と価格順位の差の2乗
                d += ((rank - (i + 1)) * (rank - (i + 1)));
            }

            rci = (1.0 - ((6.0 * d) / (period * (period * period - 1)))) * 100.0;

        } catch (IndexOutOfBoundsException e) {
            // 例外をThrow
            //throw new Exception();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return rci;
    }

    // n日標準偏差
    public static double standardDeviation(Date date, int period) {

        if (period <= 0) {
            // エラー
        }

        double sigma = 0.0;

        Realm realm = null;
        try {
            double ma = movingAverage(date, period);

            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, period);

            double total2 = 0.0;
            for (ListIterator it = results.listIterator(); it.hasNext(); ) {
                double price = ((Candlestick) it.next()).getClosingPrice();

                total2 += ((price - ma) * (price - ma));
            }

            sigma = Math.sqrt(total2 / period);

        } catch (IndexOutOfBoundsException e) {
            // 例外をThrow
            //throw new Exception();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return sigma;
    }

    // n期間サイコロジカル算出
    public static double psychological(Date date, int period) {

        if (period <= 0) {
            // エラー
        }

        int plus = 0;
        int minus = 0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<Candlestick> results = realm.where(Candlestick.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, period + 1);

            double price = results.get(0).getClosingPrice();
            for (ListIterator it = results.listIterator(1); it.hasNext(); ) {
                double prevPrice = ((Candlestick) it.next()).getClosingPrice();

                double change = price - prevPrice;
                if (change > 0.0) {
                    ++plus;
                } else if (change < 0.0) {
                    ++minus;
                }

                price = prevPrice;
            }

            // n日間変動なし
            if (plus + minus == 0) {
                return 50.0;
            }
        } catch (IndexOutOfBoundsException e) {
            // 例外をThrow
            //throw new Exception();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return plus / (double) (plus + minus) * 100.0;
    }

    // 騰落レシオ
    public static double losersRatio(Date date, int period) {

        if (period <= 0) {
            // エラー
        }

        int advances = 0;
        int decliners = 0;

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            List<MarketT1> results = realm.where(MarketT1.class)
                    .lessThanOrEqualTo("date", date)
                    .findAllSorted("date", Sort.DESCENDING)
                    .subList(0, period);

            for (ListIterator it = results.listIterator(); it.hasNext(); ) {
                MarketT1 marketT1 = (MarketT1)it.next();
                advances += marketT1.getAdvances();
                decliners += marketT1.getDecliners();
            }
        } catch (IndexOutOfBoundsException e) {
            // 例外をThrow
            //throw new Exception();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        if (decliners == 0) {
            return Double.MAX_VALUE;
        }

        return (double)advances / decliners * 100.0;
    }
}
