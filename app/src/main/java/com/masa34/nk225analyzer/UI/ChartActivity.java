package com.masa34.nk225analyzer.UI;

import com.masa34.nk225analyzer.Db.Dao.CandlestickDao;
import com.masa34.nk225analyzer.Db.Dao.Nk225EntityDao;
import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;
import com.masa34.nk225analyzer.Db.Nk225AnalyzerDatabase;
import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Util.StockUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.annotation.NonNull;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.graphics.Color;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.tradingview.lightweightcharts.view.ChartsView;
import com.tradingview.lightweightcharts.api.chart.models.color.IntColor;
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor;
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions;
import com.tradingview.lightweightcharts.api.options.models.ChartOptions;
import com.tradingview.lightweightcharts.api.options.models.GridLineOptions;
import com.tradingview.lightweightcharts.api.options.models.GridOptions;
import com.tradingview.lightweightcharts.api.options.models.LayoutOptions;
import com.tradingview.lightweightcharts.api.options.models.LineSeriesOptions;
import com.tradingview.lightweightcharts.api.options.models.PriceScaleOptions;
import com.tradingview.lightweightcharts.api.series.enums.SeriesMarkerShape;
import com.tradingview.lightweightcharts.api.series.enums.LineWidth;
import com.tradingview.lightweightcharts.api.series.enums.SeriesMarkerPosition;
import com.tradingview.lightweightcharts.api.series.models.BarData;
import com.tradingview.lightweightcharts.api.series.models.LineData;
import com.tradingview.lightweightcharts.api.series.models.SeriesMarker;
import com.tradingview.lightweightcharts.api.series.models.Time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChartActivity extends AppCompatActivity {

    private final String TAG = "ChartActivity";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_chart);

        WebView.setWebContentsDebuggingEnabled(true);

        EdgeToEdge.enable(this);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);

        var chartContainer = findViewById(R.id.chart_container);

        if (chartContainer != null) {
            Toolbar toolbar = findViewById(R.id.toolbar);
            ChartsView chartsView = findViewById(R.id.charts_view);

            // ==========================================
            // 実機 WebView の制限を解除する設定
            // ==========================================
            if (chartsView != null && chartsView.getChildAt(0) instanceof WebView internalWebView) {
                WebSettings webSettings = internalWebView.getSettings();

                // TradingViewライブラリが内部で使用するDOMストレージを実機でも確実に有効化
                webSettings.setDomStorageEnabled(true);
                webSettings.setJavaScriptEnabled(true);

                // ローカルファイル（file://）からのアクセス制限を解除する
                webSettings.setAllowFileAccess(true); // ファイルアクセス自体を許可
                webSettings.setAllowFileAccessFromFileURLs(true); // ファイルからのファイル読み込みを許可
                webSettings.setAllowUniversalAccessFromFileURLs(true); // すべてのオリジン間アクセスを許可
            }

            // ==========================================
            // ヘッダー（ツールバー）の設定
            // ==========================================
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                // タイトルを非表示にする（文字を何も出さない）
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                // 戻るボタン（←）だけを表示する
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            ViewCompat.setOnApplyWindowInsetsListener(chartContainer, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                View appBarLayout = findViewById(R.id.app_bar_layout);

                // ツールバーの上部に、ステータスバーの高さ分のパディングを設定する
                appBarLayout.setPadding(
                    appBarLayout.getPaddingLeft(),
                    systemBars.top,
                    appBarLayout.getPaddingRight(),
                    appBarLayout.getPaddingBottom()
                );

                Insets navBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());

                // ツールバーの上部に、ステータスバーの高さ分のパディングを設定する
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) chartsView.getLayoutParams();
                if (params != null) {
                    params.bottomMargin = navBarInsets.bottom;
                    chartsView.setLayoutParams(params);
                }

                return windowInsets;
            });


            // ==========================================
            // チャートの描画
            // ==========================================
            ChartOptions chartOptions = new ChartOptions();

            // 背景色と文字色の設定
            LayoutOptions layoutOptions = new LayoutOptions();
            layoutOptions.setBackground(new SolidColor(Color.parseColor("#131722")));
            layoutOptions.setTextColor(new IntColor(Color.parseColor("#D1D4DC")));
            chartOptions.setLayout(layoutOptions);

            // グリッド線（背景の網目）を透明にして非表示化
            GridOptions gridOptions = new GridOptions();
            GridLineOptions vertLines = new GridLineOptions();
            vertLines.setColor(new IntColor(Color.TRANSPARENT));
            GridLineOptions horzLines = new GridLineOptions();
            horzLines.setColor(new IntColor(Color.TRANSPARENT));
            gridOptions.setVertLines(vertLines);
            gridOptions.setHorzLines(horzLines);
            chartOptions.setGrid(gridOptions);

            // 各種軸の境界線の色
            PriceScaleOptions priceScaleOptions = new PriceScaleOptions();
            priceScaleOptions.setBorderColor(new IntColor(Color.parseColor("#2A2E39")));
            chartOptions.setRightPriceScale(priceScaleOptions);

            chartsView.getApi().applyOptions(chartOptions);

            CandlestickSeriesOptions candleOptions = new CandlestickSeriesOptions();
            candleOptions.setUpColor(new IntColor(Color.parseColor("#26A69A")));
            candleOptions.setDownColor(new IntColor(Color.parseColor("#EF5350")));
            candleOptions.setBorderUpColor(new IntColor(Color.parseColor("#26A69A")));
            candleOptions.setBorderDownColor(new IntColor(Color.parseColor("#EF5350")));
            candleOptions.setWickUpColor(new IntColor(Color.parseColor("#26A69A")));
            candleOptions.setWickDownColor(new IntColor(Color.parseColor("#EF5350")));

            executorService.execute(() -> {
                List<BarData> candles = new ArrayList<>();
                List<SeriesMarker> markers = new ArrayList<>();
                List<LineData> ma5s = new ArrayList<>();
                List<LineData> ma25s = new ArrayList<>();

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

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    var bd = new Time.BusinessDay(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));

                    // 日足
                    candles.add(new BarData(bd, (float)candlestick.getOpeningPrice(), (float)candlestick.getHighPrice(), (float)candlestick.getLowPrice(), (float)candlestick.getClosingPrice(), null));

                    // 評価
                    var result = StockUtils.getStockEvaluation((entity));
                    switch (result.status) {
                        case TOP:
                            markers.add(new SeriesMarker(bd, SeriesMarkerPosition.ABOVE_BAR, SeriesMarkerShape.CIRCLE, null, new IntColor(Color.parseColor("#FF0000")), "", "", null));
                            break;
                        case EXPENSIVE:
                            markers.add(new SeriesMarker(bd, SeriesMarkerPosition.ABOVE_BAR, SeriesMarkerShape.CIRCLE, null, new IntColor(Color.parseColor("#FF8000")), "", "", null));
                            break;
                        case NEUTRAL:
                            break;
                        case CHEAP:
                            markers.add(new SeriesMarker(bd, SeriesMarkerPosition.BELOW_BAR, SeriesMarkerShape.CIRCLE, null, new IntColor(Color.parseColor("#0080FF")), "", "", null));
                            break;
                        case BOTTOM:
                            markers.add(new SeriesMarker(bd, SeriesMarkerPosition.BELOW_BAR, SeriesMarkerShape.CIRCLE, null, new IntColor(Color.parseColor("#0000FF")), "", "", null));
                            break;
                    }

                    // 5日移動平均線
                    var ma5 = (float)entity.getMovingAverage5();
                    ma5s.add(new LineData(bd, ma5, null));

                    // 25日移動平均線
                    var ma25 = (float)entity.getMovingAverage25();
                    ma25s.add(new LineData(bd, ma25, null));
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (candles.isEmpty()) {
                        Log.w(TAG, "ローソク足データが0件のため描画をスキップします");
                        return;
                    }

                    // メインスレッド上で安全にシリーズを作成し、データをセット
                    chartsView.getApi().addCandlestickSeries(candleOptions, candleSeries -> {
                        // ローソク足データをセット
                        candleSeries.setData(candles);

                        // マーカー描画
                        candleSeries.setMarkers(markers);

                        // 5日移動平均線
                        {
                            LineSeriesOptions lineOptions = new LineSeriesOptions();
                            lineOptions.setColor(new IntColor(Color.rgb(0,255,0)));
                            lineOptions.setLineWidth(LineWidth.ONE);
                            chartsView.getApi().addLineSeries(lineOptions, lineSeries -> {
                                lineSeries.setData(ma5s);
                                return null;
                            });
                        }

                        // 25日移動平均線
                        {
                            LineSeriesOptions lineOptions = new LineSeriesOptions();
                            lineOptions.setColor(new IntColor(Color.rgb(255,0,0)));
                            lineOptions.setLineWidth(LineWidth.ONE);
                            chartsView.getApi().addLineSeries(lineOptions, lineSeries -> {
                                lineSeries.setData(ma25s);
                                return null;
                            });
                        }

                        return null;
                    });
                });
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        // 押されたボタンが「戻るボタン（android.R.id.home）」だった場合
        if (item.getItemId() == android.R.id.home) {
            finish(); // 現在のチャート画面を閉じて、前のメイン画面に戻る
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
