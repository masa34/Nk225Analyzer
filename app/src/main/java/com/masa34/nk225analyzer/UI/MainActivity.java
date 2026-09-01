package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Db.Entity.Nk225Entity;
import com.masa34.nk225analyzer.Task.AbstractNk225DownloadProcess;
import com.masa34.nk225analyzer.Task.Nk225ListReader;
import com.masa34.nk225analyzer.Util.DateUtils;
import com.masa34.nk225analyzer.Util.Nk225Preference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<List<Nk225Entity>> {

    private final String TAG = "MainActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private Nk225PagerAdapter pagerAdapter;

    private AdView adView;

    private boolean isStartup = false;
    private boolean isVisible = false;
    private boolean needReflesh = false;

    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.orange);
        swipeRefreshLayout.setOnRefreshListener(this);

        PagerTabStrip pagerTabStrip = findViewById(R.id.tab_strip);
        pagerTabStrip.setVisibility(View.INVISIBLE);

        // 広告初期化
        MobileAds.initialize(this);
        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        isStartup = true;
        needReflesh = false;

        // 初回メッセージ
        Nk225Preference preference = Nk225Preference.getInstance(this);
        if (!preference.getDownloaded()) {
            new AlertDialog.Builder(this)
                .setTitle("お知らせ")
                .setMessage("データ管理を改善しました。\r\n再ダウンロードが必要です。\r\n端末によっては時間がかかる場合があります。")
                .show();

            preference.setDownloaded(true);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        needReflesh = false;

        adView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG,"onRestoreInstanceState");

        isStartup = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        isVisible = true;

        adView.resume();

        if (downloader != null && downloader.isInProcess()) {
            showProgressDialog(this);
            return;
        }

        if (isStartup) {
            if (Nk225Preference.getInstance(this).isAutoDownload()) {
                // 自動ダウンロード
                downloader = new AutoNk225DownloadProcess();
                downloader.execute();
            } else {
                getSupportLoaderManager().initLoader(0, null, this);
            }
        }

        if (needReflesh) {
            needReflesh = false;

            onRefresh();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        isStartup = false;

        if (downloader != null && downloader.isInProcess()) {
            // アクティビティが消える前にダイアログを終了させる
            dismissProgressDialog();
        }

        isVisible = false;

        adView.pause();
        super.onPause();
    }

    // BACKボタンが押された時の処理
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown");

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 評価の訴求
            Nk225Preference preference = Nk225Preference.getInstance(this);
            String review = preference.getReviewDate();
            if (review.isEmpty()) {
                boolean dispAlert = false;
                String later = preference.getLaterDate();
                if (later.isEmpty()) {
                    dispAlert = true;
                }
                else {
                    try {
                        // 前回「あとで」を選択してから3日以上経過していたら再度表示
                        Date nowDate = DateUtils.getNow();
                        Date laterDate = DateUtils.convertToDate(later, "yyyy/MM/dd");
                        int diffDay = DateUtils.DifferenceDays(nowDate, laterDate);
                        if (diffDay > 3) {
                            dispAlert = true;
                        }
                    }
                    catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }

                if (dispAlert) {
                    new AlertDialog.Builder(this)
                        .setTitle("評価のお願い")
                        .setMessage("ご利用ありがとうございます\n開発の励みになるので、良ければ★5のレビューをお願いします")
                        .setPositiveButton("評価する", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Nk225Preference preference = Nk225Preference.getInstance(MainActivity.this);
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
                                preference.setReviewDate(fmt.format(DateUtils.getNow()));

                                // レビュー画面を表示
                                Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.masa34.nk225analyzer"));
                                startActivity(intent);

                                MainActivity.this.finish();
                            }
                        })
                        .setNegativeButton("あとで", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Nk225Preference preference = Nk225Preference.getInstance(MainActivity.this);
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
                                preference.setLaterDate(fmt.format(DateUtils.getNow()));

                                MainActivity.this.finish();
                            }
                        })
                        .show();

                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        int id = item.getItemId();

        if (id == R.id.action_download) {
            downloader = new ManualNk225DownloadProcess();
            downloader.execute();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new android.content.Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        } else if (id == R.id.action_db_init) {
            // RDB初期化
            Nk225AnalyzerApp.resetDatabase(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh");

        // 非同期処理
        getSupportLoaderManager().restartLoader(0, null, this);

        // 更新が終了したらインジケータ非表示
        swipeRefreshLayout.setRefreshing(false);
    }

    // 以下、非同期データ読み込み処理のコールバック

    @Override
    public Loader<List<Nk225Entity>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        Nk225ListReader loader = new Nk225ListReader(getApplication());
        loader.forceLoad();

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<Nk225Entity>> loader, List<Nk225Entity> data) {
        Log.d(TAG, "onLoadFinished");

        // 指定日数以前のデータは捨てる
        int displayPeriod = Nk225Preference.getInstance(this).getDisplayPeriod();
        if (displayPeriod > 0) {
            int from = 0;
            int to = data.size() - displayPeriod;
            if (to < 0) {
                to = 0;
            }
            data.subList(from, to).clear();
        }

        pagerAdapter = new Nk225PagerAdapter(getSupportFragmentManager());
        pagerAdapter.setNk225Entitiy(data);
        pagerAdapter.notifyDataSetChanged();

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        // 最終ページ（最新日付）を表示
        viewPager.setCurrentItem(pagerAdapter.getCount() - 1);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.tab_strip);
        if (data.isEmpty()) {
            pagerTabStrip.setVisibility(View.INVISIBLE);
        } else {
            pagerTabStrip.setDrawFullUnderline(true);
            pagerTabStrip.setTabIndicatorColor(Color.DKGRAY);
            pagerTabStrip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Nk225Entity>> loader) {
        Log.d(TAG, "onLoaderReset");

        // 今回は無視する
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //SecondActivityから戻ってきた場合
            case (REQUEST_CODE):
                if (resultCode == RESULT_OK) {
                    //OKボタンを押して戻ってきたときの処理
                    if (data.getBooleanExtra("displayPeriodChanged", false)) {
                        // 表示期間が変更された
                        needReflesh = true;
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    //キャンセルボタンを押して戻ってきたときの処理
                } else {
                    //その他
                }
                break;

            default:
                break;
        }
    }

    // 以下ダウンロード関連処理

    private SimpleProgressDialog progressDialog;

    private void showProgressDialog(Context context) {
        Log.d(TAG, "showProgressDialog");

        dismissProgressDialog();
        progressDialog = new SimpleProgressDialog(context);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        Log.d(TAG, "dismissProgressDialog");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    private static AbstractNk225DownloadProcess downloader = null;

    private class ManualNk225DownloadProcess extends AbstractNk225DownloadProcess {

        @Override
        protected void onPreDownloadProcess() {
            showProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPostDownloadProcess(boolean result) {
            dismissProgressDialog();

            if (result) {
                Toast.makeText(MainActivity.this, "画面を下に引っ張り、表示を更新して下さい", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "株価データの取得に失敗しました。\nしばらく時間をおいてから再度お試しください", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class AutoNk225DownloadProcess extends AbstractNk225DownloadProcess {

        @Override
        protected void onPreDownloadProcess() {
            showProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPostDownloadProcess(boolean result) {
            dismissProgressDialog();

            if (isVisible) {
                onRefresh();
            } else {
                needReflesh = true;
            }

            if (!result) {
                Toast.makeText(MainActivity.this, "株価データの取得に失敗しました。\nしばらく時間をおいてから再度お試しください", Toast.LENGTH_LONG).show();
            }
        }
    }
}
