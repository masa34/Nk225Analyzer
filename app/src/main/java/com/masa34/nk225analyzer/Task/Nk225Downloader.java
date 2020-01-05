package com.masa34.nk225analyzer.Task;

import android.os.AsyncTask;
import android.util.Log;

import com.masa34.nk225analyzer.Stock.Candlestick;
import com.masa34.nk225analyzer.Stock.MarketCalendar;
import com.masa34.nk225analyzer.Stock.MarketT1;
import com.masa34.nk225analyzer.Stock.Nk225Entity;
import com.masa34.nk225analyzer.Stock.StockUtils;
import com.masa34.nk225analyzer.Util.DateUtils;
import com.masa34.nk225analyzer.Util.Validator.CandlestickValidator;
import com.masa34.nk225analyzer.Util.Validator.MarketValidator;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.masa34.nk225analyzer.Util.DateUtils.convertToDate;

public class Nk225Downloader extends AsyncTask<Void, Void, Boolean> {

    private final String TAG = "Nk225Downloader";

    private DownloadCallBack callBack;
    private boolean isInProcess;

    public Nk225Downloader(DownloadCallBack callBack) {
        Log.d(TAG, "Nk225Downloader");
        this.callBack = callBack;
    }

    // Activity#onCreateで, 処理中であることを確認した上で呼んであげる
    public void setCallBack(DownloadCallBack callBack) {
        this.callBack = callBack;
    }

    public synchronized boolean isInProcess() { return isInProcess; }

    @Override
    protected void onPreExecute() {

        Log.d(TAG, "onPreExecute");

        isInProcess = true;

        if (callBack != null) {
            callBack.onPreDownload();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        Log.d(TAG, "doInBackground");

        // 株式データダウンロード
        if (!downloadCsv()) {
            return false;
        }

        // テクニカルの計算
        if (!calculationTechnical()) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        Log.d(TAG, "onPostExecute");

        isInProcess = false;

        if (callBack != null) {
            callBack.onPostDownload(result);
        }
    }

    public static interface DownloadCallBack {
        public void onPreDownload();
        public void onPostDownload(boolean result);
    }

    private boolean downloadCsv() {

        Log.d(TAG, "downloadCsv");

        // ※ダウンロード要否判定

        if (!downloadNk225Csv()) {
            return false;
        }

        // ※騰落レシオを計算するための情報が取得できなくなったため暫定対応とする
        //if (!downloadMarketT1Csv()) {
        //    return false;
        //}

        return true;
    }

    private boolean downloadNk225Csv() {

        Log.d(TAG, "downloadNk225Csv");

        // ダウンロード開始日(ダウンロード済み最新データの日時)
        Realm realm = null;
        Date fromDate;
        try {
            realm = Realm.getDefaultInstance();

            fromDate = realm.where(Candlestick.class).maximumDate("date");
            if (fromDate == null) {
                // ※日付はstring.xmlに書きたい
                fromDate = DateUtils.convertToDate("2015/01/01", "yyyy/MM/dd");
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        // ダウンロード終了日(直近の立会日)
        Date toDate;
        boolean isMarketOpening = false;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH) + 1;
            int d = cal.get(Calendar.DATE);
            toDate = DateUtils.convertToDate(String.format("%04d/%02d/%02d", y, m, d), "yyyy/MM/dd");
            if (MarketCalendar.isMarketHoliday(toDate)) {
                toDate = MarketCalendar.getLastBussinessDay(toDate);
            } else {
                if (cal.get(Calendar.HOUR_OF_DAY) >= 9) {
                    isMarketOpening = true;
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        }

        if (fromDate.compareTo(toDate) != 0) {
            try {
                final String NK225_URL = "https://indexes.nikkei.co.jp/nkave/historical/nikkei_stock_average_daily_jp.csv";
                Log.d(TAG, NK225_URL);

                Nk225CsvReader csvReader = new Nk225CsvReader(new Nk225CsvReader.CsvReadCallBack() {

                    Realm realm = null;

                    @Override
                    public void onPreCsvRead() {
                        realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                    }

                    @Override
                    public void onCsvRead(String[] values) {
                        try {
                            String dateFormat = "yyyy/MM/dd";
                            if (CandlestickValidator.isValid(values, dateFormat)) {
                                Date date = DateUtils.convertToDate(values[0], dateFormat);

                                if (realm.where(Candlestick.class).equalTo("date", date).count() == 0) {
                                    Candlestick candlestick = realm.createObject(Candlestick.class);

                                    long nextId = 1;
                                    Number maxId = realm.where(Candlestick.class).max("id");
                                    if (maxId != null) nextId = maxId.longValue() + 1;
                                    candlestick.setId(nextId);

                                    candlestick.setDate(date);
                                    candlestick.setOpeningPrice(Float.parseFloat(values[2]));
                                    candlestick.setHighPrice(Float.parseFloat(values[3]));
                                    candlestick.setLowPrice(Float.parseFloat(values[4]));
                                    candlestick.setClosingPrice(Float.parseFloat(values[1]));
                                    // ※騰落レシオを計算するための情報が取得できなくなったため暫定対応とする
                                    //candlestick.setMarketClosing(true);
                                    candlestick.setMarketClosing(false);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    }

                    @Override
                    public void onPostCsvRead(boolean result) {
                        if (result) {
                            realm.commitTransaction();
                        } else  {
                            realm.cancelTransaction();
                        }

                        realm.close();
                    }
                });

                if (!csvReader.execute(new URL(NK225_URL))) {
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }
        } else {
            // 最新がダウンロード済みのため、ダウンロード処理は不要
        }

        return true;
    }

    private boolean downloadMarketT1Csv() {

        Log.d(TAG, "downloadMarketT1Csv");

        // ダウンロード開始日(ダウンロード済み最新データの日時)
        Date fromDate;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            fromDate = realm.where(MarketT1.class).maximumDate("date");
            if (fromDate == null) {
                fromDate = convertToDate("2015/01/01", "yyyy/MM/dd");
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        // ダウンロード終了日(直近の立会日)
        Date toDate;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH) + 1;
            int d = cal.get(Calendar.DATE);
            toDate = convertToDate(String.format("%04d/%02d/%02d", y, m, d), "yyyy/MM/dd");
            if (MarketCalendar.isMarketHoliday(toDate)) {
                toDate = MarketCalendar.getLastBussinessDay(toDate);
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        }

        if (fromDate.compareTo(toDate) != 0) {
            int fromYear = DateUtils.getYear(fromDate);
            int toYear = DateUtils.getYear(toDate);

            for (int yy = fromYear; yy <= toYear; ++yy) {
                try {
                    final String T1_URL = "http://k-db.com/statistics/T1/%1$d?download=csv";
                    Log.d(TAG, String.format(T1_URL, yy));

                    Nk225CsvReader csvReader = new Nk225CsvReader(new Nk225CsvReader.CsvReadCallBack() {
                        Realm realm = null;

                        @Override
                        public void onPreCsvRead() {
                            realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                        }

                        @Override
                        public void onCsvRead(String[] values) {
                            try {
                                if (MarketValidator.isValid(values)) {

                                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date = fmt.parse(values[0]);

                                    if (realm.where(MarketT1.class).equalTo("date", date).count() == 0) {
                                        MarketT1 marketT1 = realm.createObject(MarketT1.class);

                                        long nextId = 1;
                                        Number maxId = realm.where(MarketT1.class).max("id");
                                        if (maxId != null) nextId = maxId.longValue() + 1;
                                        marketT1.setId(nextId);

                                        marketT1.setDate(date);
                                        marketT1.setVolume(Long.parseLong(values[1]));
                                        marketT1.setTurnover(Long.parseLong(values[2]));
                                        marketT1.setAdvances(Integer.parseInt(values[5]));
                                        marketT1.setDecliners(Integer.parseInt(values[7]));
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
                            }
                        }

                        @Override
                        public void onPostCsvRead(boolean result) {
                            if (result) {
                                realm.commitTransaction();
                            } else  {
                                realm.cancelTransaction();
                            }

                            realm.close();
                        }
                    });

                    if (!csvReader.execute(new URL(String.format(T1_URL, yy)))) {
                        return false;
                    }

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return false;
                }
            }
        } else {
            // 最新がダウンロード済みのため、ダウンロード処理は不要
        }

        return true;
    }

    private boolean calculationTechnical() {

        Log.d(TAG, "calculationTechnical");

        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            // テクニカルを計算するのは2年前の1月1日以降
            Date fromDate = realm.where(Nk225Entity.class).maximumDate("date");
            if (fromDate == null) {
                int year = DateUtils.getYear(new Date()) - 2;
                fromDate = DateUtils.convertToDate(String.valueOf(year) + "/01/01", "yyyy/MM/dd");
            }

            RealmResults<Candlestick> results = realm.where(Candlestick.class)
                    .greaterThan("date", fromDate)
                    .findAllSorted("date", Sort.ASCENDING);

            for (int i = 0; i < results.size(); ++i) {

                Nk225Entity nk225 = realm.createObject(Nk225Entity.class);

                long nextId = 1;
                Number maxId = realm.where(Nk225Entity.class).max("id");
                if (maxId != null) nextId = maxId.longValue() + 1;
                nk225.setId(nextId);

                boolean marketClosing = results.get(i).getMarketClosing();

                Date date = results.get(i).getDate();
                nk225.setDate(date);

                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");

                double value = StockUtils.value(date);
                nk225.setValue(value);
                Log.d(TAG, fmt.format(date) + ":日経平均株価(終値) " + String.valueOf(value));

                double change = StockUtils.change(date);
                nk225.setChange(change);
                Log.d(TAG, fmt.format(date) + ":前日比 " + String.valueOf(change));

                double ma5 = StockUtils.movingAverage(date, 5);
                nk225.setMovingAverage5(ma5);
                Log.d(TAG, fmt.format(date) + ":5日移動平均線 " + String.valueOf(ma5));

                double ma25 = StockUtils.movingAverage(date, 25);
                nk225.setMovingAverage25(ma25);
                Log.d(TAG, fmt.format(date) + ":25日移動平均線 " + String.valueOf(ma25));

                double sigma = StockUtils.standardDeviation(date, 25);
                nk225.setStandardDeviation(sigma);
                Log.d(TAG, fmt.format(date) + ":標準偏差(25) " + String.valueOf(sigma));

                double range = StockUtils.priceRange(date);
                nk225.setPriceRange(range);
                Log.d(TAG, fmt.format(date) + ":当日値幅 " + String.valueOf(range));

                double range20 = StockUtils.priceRangeAverage(date, 20);
                nk225.setPriceRangeAverage20(range20);
                Log.d(TAG, fmt.format(date) + ":20日平均値幅 " + String.valueOf(range20));

                double rsi = StockUtils.rsi(date, 14);
                nk225.setRsi(rsi);
                Log.d(TAG, fmt.format(date) + ":RSI(14) " + String.valueOf(rsi));

                double rci = StockUtils.rci(date, 9);
                nk225.setRci(rci);
                Log.d(TAG, fmt.format(date) + ":RCI(9) " + String.valueOf(rci));

                double psycho = StockUtils.psychological(date, 12);
                nk225.setPsychological(psycho);
                Log.d(TAG, fmt.format(date) + ":Psychological(12) " + String.valueOf(psycho));

                Date losersDate = date;
                if (!marketClosing) {
                    // 騰落レシオが計算できるのは引け後、場中は前日の騰落レシオで代用
                    losersDate = MarketCalendar.getLastBussinessDay(date);
                }
                double losersRatio = StockUtils.losersRatio(losersDate, 25);
                nk225.setLosersRatio(losersRatio);
                Log.d(TAG, fmt.format(losersDate) + ":騰落レシオ(25) " + String.valueOf(losersRatio));

                nk225.setMarketClosing(marketClosing);
            }

            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();

            Log.e(TAG, e.toString());
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        return true;
    }
}
