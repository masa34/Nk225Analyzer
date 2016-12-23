package com.masa34.nk225analyzer.Task;

import android.os.AsyncTask;
import android.util.Log;

import com.masa34.nk225analyzer.Util.DateUtils;
import com.masa34.nk225analyzer.Stock.Candlestick;
import com.masa34.nk225analyzer.Stock.MarketCalendar;
import com.masa34.nk225analyzer.Stock.MarketT1;
import com.masa34.nk225analyzer.Stock.Nk225Entity;
import com.masa34.nk225analyzer.Stock.StockUtils;
import com.masa34.nk225analyzer.Util.Validator.CandlestickValidator;
import com.masa34.nk225analyzer.Util.Validator.MarketValidator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

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

        // ※ダウンロード要否判定

        if (!downloadCsv()) {
            return false;
        }

        // ※テクニカルなどの計算（あとでリファクタリング）
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            // テクニカルを計算するのは2016/1/1以降
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
            Date startDate = realm.where(Nk225Entity.class).maximumDate("date");
            if (startDate == null) {
                startDate = DateUtils.convertToDate("2016/01/01");
            }

            RealmResults<Candlestick> results = realm.where(Candlestick.class)
                    .greaterThan("date", startDate)
                    .findAllSorted("date", Sort.ASCENDING);

            for (int i = 0; i < results.size(); ++i) {

                Nk225Entity nk225 = realm.createObject(Nk225Entity.class);

                long nextId = 1;
                Number maxId = realm.where(Nk225Entity.class).max("id");
                if (maxId != null) nextId = maxId.longValue() + 1;
                nk225.setId(nextId);

                Date date = results.get(i).getDate();
                nk225.setDate(date);

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

                double rsi = StockUtils.rsi(date, 14);
                nk225.setRsi(rsi);
                Log.d(TAG, fmt.format(date) + ":RSI(14) " + String.valueOf(rsi));

                double rci = StockUtils.rci(date, 9);
                nk225.setRci(rci);
                Log.d(TAG, fmt.format(date) + ":RCI(9) " + String.valueOf(rci));

                double psycho = StockUtils.psychological(date, 12);
                nk225.setPsychological(psycho);
                Log.d(TAG, fmt.format(date) + ":Psychological(12) " + String.valueOf(psycho));

                double losersRatio = StockUtils.losersRatio(date, 25);
                nk225.setLosersRatio(losersRatio);
                Log.d(TAG, fmt.format(date) + ":騰落レシオ(25) " + String.valueOf(losersRatio));
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

    // ※以下冗長

    private boolean downloadCsv() {

        Log.d(TAG, "downloadCsv");

        if (!downloadNk225Csv()) {
            return false;
        }

        if (!downloadMarketT1Csv()) {
            return false;
        }

        return true;
    }

    private boolean downloadNk225Csv() {
        Log.d(TAG, "downloadNk225Csv");

        // ダウンロード開始日
        Date fromDate;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            // ※日付はstring.xmlに書きたい
            fromDate = DateUtils.convertToDate("2015/01/01");

            Date maxDate = realm.where(Candlestick.class).maximumDate("date");
            if (maxDate != null) {
                fromDate = maxDate;
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        // 直近の立会日
        Date toDate;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH) + 1;
            int d = cal.get(Calendar.DATE);
            toDate = DateUtils.convertToDate(String.format("%04d/%02d/%02d", y, m, d));
            if (MarketCalendar.isMarketHoliday(toDate)) {
                toDate = MarketCalendar.getLastBussinessDay(toDate);
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        }

        // 最新がダウンロード済みのため、ダウンロード処理は不要
        if (fromDate.compareTo(toDate) == 0) {
            return true;
        }

        int fromYear = DateUtils.getYear(fromDate);
        int toYear = DateUtils.getYear(toDate);

        for (int yy = fromYear; yy <= toYear; ++yy) {
            try {
                final String NK225_URL = "http://k-db.com/indices/I101/1d/%1$d?download=csv";
                Log.d(TAG, String.format(NK225_URL, yy));
                URL url = new URL(String.format(NK225_URL, yy));

                if (!downloadNk225Csv(url)) {
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }

        return true;
    }

    private boolean downloadNk225Csv(URL url) {
        Realm realm = null;
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            final int TIMEOUT_READ = 5000;
            final int TIMEOUT_CONNECT = 30000;
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
            urlConnection.setReadTimeout(TIMEOUT_READ);
            urlConnection.setConnectTimeout(TIMEOUT_CONNECT);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestProperty("User-Agent", "");
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);

                String[] values = line.replace("\"", "").split(",", 0);

                if (CandlestickValidator.isValid(values)) {
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = fmt.parse(values[0]);

                    if (realm.where(Candlestick.class).equalTo("date", date).count() > 0) {
                        continue;
                    }

                    Candlestick candlestick = realm.createObject(Candlestick.class);

                    long nextId = 1;
                    Number maxId = realm.where(Candlestick.class).max("id");
                    if (maxId != null) nextId = maxId.longValue() + 1;
                    candlestick.setId(nextId);

                    candlestick.setDate(date);
                    candlestick.setOpeningPrice(Float.parseFloat(values[1]));
                    candlestick.setHighPrice(Float.parseFloat(values[2]));
                    candlestick.setLowPrice(Float.parseFloat(values[3]));
                    candlestick.setClosingPrice(Float.parseFloat(values[4]));
                }
            }

            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();

            Log.e(TAG, e.toString());
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (realm != null) {
                    realm.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }

        return true;
    }

    private boolean downloadMarketT1Csv() {
        Log.d(TAG, "downloadMarketT1Csv");

        // ダウンロード済み最新データの日時
        Date fromDate;
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();

            fromDate = DateUtils.convertToDate("2015/01/01");

            Date maxDate = realm.where(MarketT1.class).maximumDate("date");
            if (maxDate != null) {
                fromDate = maxDate;
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        } finally {
            if (realm != null) {
                realm.close();
            }
        }

        // 直近の立会日
        Date toDate;
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH) + 1;
            int d = cal.get(Calendar.DATE);
            toDate = DateUtils.convertToDate(String.format("%04d/%02d/%02d", y, m, d));
            if (MarketCalendar.isMarketHoliday(toDate)) {
                toDate = MarketCalendar.getLastBussinessDay(toDate);
            }
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
            return false;
        }

        // 最新がダウンロード済みのため、ダウンロード処理は不要
        if (fromDate.compareTo(toDate) == 0) {
            return true;
        }

        int fromYear = DateUtils.getYear(fromDate);
        int toYear = DateUtils.getYear(toDate);

        for (int yy = fromYear; yy <= toYear; ++yy) {
            try {
                final String T1_URL = "http://k-db.com/statistics/T1/%1$d?download=csv";
                Log.d(TAG, String.format(T1_URL, yy));
                URL url = new URL(String.format(T1_URL, yy));

                if (!downloadMarketT1Csv(url)) {
                    return false;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }

        return true;
    }

    private boolean downloadMarketT1Csv(URL url) {
        Realm realm = null;
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            final int TIMEOUT_READ = 5000;
            final int TIMEOUT_CONNECT = 30000;
            urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
            urlConnection.setReadTimeout(TIMEOUT_READ);
            urlConnection.setConnectTimeout(TIMEOUT_CONNECT);
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setRequestProperty("User-Agent", "");
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);

                String[] values = line.replace("\"", "").split(",", 0);

                if (MarketValidator.isValid(values)) {

                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = fmt.parse(values[0]);

                    if (realm.where(MarketT1.class).equalTo("date", date).count() > 0) {
                        continue;
                    }

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

            realm.commitTransaction();
        } catch (Exception e) {
            realm.cancelTransaction();

            Log.e(TAG, e.toString());
            return false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (realm != null) {
                    realm.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                return false;
            }
        }

        return true;
    }
}
