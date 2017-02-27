package com.masa34.nk225analyzer.Task;

import android.util.Log;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Nk225CsvReader {

    private final String TAG = "Nk225CsvReader";

    public static interface CsvReadCallBack {
        public void onPreCsvRead();
        public void onCsvRead(String[] values);
        public void onPostCsvRead(boolean result);
    }

    private CsvReadCallBack callBack;

    public Nk225CsvReader(CsvReadCallBack callBack) {
        Log.d(TAG, "Nk225CsvReader");
        this.callBack = callBack;
    }

    public boolean execute(URL url) {

        boolean result = true;

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        if (callBack != null) {
            callBack.onPreCsvRead();
        }

        try {
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "Shift-JIS"));

            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);

                if (callBack != null) {
                    String[] values = line.replace("\"", "").split(",", 0);
                    callBack.onCsvRead(values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            result = false;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                result = false;
            }
        }

        if (callBack != null) {
            callBack.onPostCsvRead(result);
        }

        return true;
    }
}
