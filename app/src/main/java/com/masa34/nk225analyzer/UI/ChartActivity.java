package com.masa34.nk225analyzer.UI;

import com.google.gson.Gson;
import com.masa34.nk225analyzer.Db.Dao.CandlestickDao;
import com.masa34.nk225analyzer.Db.Entity.Candlestick;
import com.masa34.nk225analyzer.Db.Dao.Nk225EntityDao;
import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;
import com.masa34.nk225analyzer.Db.Nk225AnalyzerDatabase;
import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Util.StockUtils;
import com.masa34.nk225analyzer.Util.Candle;
import com.masa34.nk225analyzer.Util.Marker;
import com.masa34.nk225analyzer.Util.SeriesValue;
import com.masa34.nk225analyzer.Util.BusinessDay;
import com.masa34.nk225analyzer.BuildConfig;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.ConsoleMessage;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChartActivity extends AppCompatActivity {

    private final String TAG = "ChartActivity";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_chart);

        setupWebView();
        webView.loadUrl("file:///android_asset/chart.html");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        // チャートを明示的に破棄（JS側のチャートオブジェクトを消す）
        if (webView != null) {
            // JS 側のチャートを安全に破棄
            webView.evaluateJavascript("if(window._lwChart && window._lwChart.remove) { window._lwChart.remove(); } window._lwChart = null;", null);

            // WebView 自体も破棄
            webView.onPause();
            webView.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // WebView が null なら再セットアップ（通常は null になっていないはず）
        if (webView == null) {
            setupWebView();
            webView.loadUrl("file:///android_asset/chart.html");
        } else {
            // 既存の WebView を再開
            webView.onResume();
            webView.resumeTimers();
            webView.loadUrl("file:///android_asset/chart.html");
        }
    }

    private void setupWebView() {
        Log.d(TAG, "setupWebView");

        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (BuildConfig.DEBUG) {
            // アプリ起動時に一度だけ
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            // WebView に Console を受け取る WebChromeClient を設定
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage cm) {
                    Log.d("WebConsole", cm.message() + " -- " + cm.sourceId() + ":" + cm.lineNumber());
                    return true;
                }
            });
        }

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient());
    }

    // ------------------------------
    // チャートへデータ送信
    // ------------------------------
    private void sendDataToChart() {
        Log.d(TAG, "sendDataToChart");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            executor.execute(() -> {
                // DBアクセスはバックグランドで実行する

                var candles = new ArrayList<Candle>();
                var markers = new ArrayList<Marker>();
                var ma5s = new ArrayList<SeriesValue>();
                var ma25s = new ArrayList<SeriesValue>();

                Nk225AnalyzerDatabase db = Nk225AnalyzerApp.getDatabase();
                Nk225EntityDao nkDao = db.nk225EntityDao();
                CandlestickDao csDao = db.candlestickDao();

                List<Nk225Entity> entities = nkDao.findAll();
                for (int i = 0; i < entities.size(); i++) {
                    var entity = entities.get(i);

                    var d = entity.getDate();
                    if (d == null) {
                        Log.e(TAG, "Date is null for Nk225Entity id=" + entity.getId());
                        continue; // ← null はスキップ
                    }

                    var candlestick = csDao.findByDate(d);
                    if (candlestick == null) {
                        continue;
                    }

                    // 日足
                    var bd = new BusinessDay(d);
                    candles.add(new Candle(bd, candlestick.getOpeningPrice(), candlestick.getHighPrice(), candlestick.getLowPrice(), candlestick.getClosingPrice()));

                    // 評価
                    var result = StockUtils.getStockEvaluation((entity));
                    switch (result.status) {
                        case TOP:
                            markers.add(new Marker(bd, "aboveBar", "#FF0000", "circle", ""));
                            break;
                        case EXPENSIVE:
                            markers.add(new Marker(bd, "aboveBar", "#FF8000", "circle", ""));
                            break;
                        case NEUTRAL:
                            break;
                        case CHEAP:
                            markers.add(new Marker(bd, "belowBar", "#0080FF", "circle", ""));
                            break;
                        case BOTTOM:
                            markers.add(new Marker(bd, "belowBar", "#0000FF", "circle", ""));
                            break;
                    }

                    // 5日移動平均線
                    var ma5 = entity.getMovingAverage5();
                    ma5s.add(new SeriesValue(bd, ma5));

                    // 25日移動平均線
                    var ma25 = entity.getMovingAverage25();
                    ma25s.add(new SeriesValue(bd, ma25));
                }

                Gson gson = new Gson();
                String jsonStocks = gson.toJson(candles);
                Log.d(TAG, jsonStocks);
                String jsonMarkers = gson.toJson(markers);
                Log.d(TAG, jsonMarkers);
                String jsonMa5 = gson.toJson(ma5s);
                Log.d(TAG, jsonMa5);
                String jsonMa25 = gson.toJson(ma25s);
                Log.d(TAG, jsonMa25);

                // WebViewの操作のみメインスレッドで実行する
                new Handler(Looper.getMainLooper()).post(() -> {
                    String stocksBase64 = Base64.encodeToString(jsonStocks.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                    String markersBase64 = Base64.encodeToString(jsonMarkers.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                    String ma5Base64 = Base64.encodeToString(jsonMa5.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
                    String ma25Base64 = Base64.encodeToString(jsonMa25.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

                    String script = String.format(
                        "setChartData(atob('%s'), atob('%s'), atob('%s'), atob('%s'));",
                        stocksBase64,
                        markersBase64,
                        ma5Base64,
                        ma25Base64
                    );
                    webView.evaluateJavascript(script, null);
                });
            });
        } finally {
            executor.shutdown();
        }
    }

    // ------------------------------
    // JS → Java ブリッジ
    // ------------------------------
    public class WebAppInterface {
        @JavascriptInterface
        public void onChartReady() {
            runOnUiThread(() -> sendDataToChart());
        }

        @JavascriptInterface
        public void onCandleClicked(String dateStr) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("SELECTED_DATE", dateStr);
            setResult(RESULT_OK, resultIntent);
            finish(); // 画面を閉じてメインに戻る
        }
    }
}
