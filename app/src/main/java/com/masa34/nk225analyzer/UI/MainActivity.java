package com.masa34.nk225analyzer.UI;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.masa34.nk225analyzer.R;
import com.masa34.nk225analyzer.Stock.Nk225Entity;
import com.masa34.nk225analyzer.Task.Nk225Downloader;
import com.masa34.nk225analyzer.Task.Nk225ListReader;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<List<Nk225Entity>> {

    private final String TAG = "MainActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private Nk225PagerAdapter pagerAdapter;

    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // Realm初期化
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this).build());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.green, R.color.blue, R.color.orange);
        swipeRefreshLayout.setOnRefreshListener(this);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.tab_strip);
        pagerTabStrip.setVisibility(View.INVISIBLE);

        // 広告初期化
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-0365443143303373~5579598474");
        adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        // 非同期処理
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        adView.destroy();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (downloader != null && downloader.isInProcess()) {
            // 新しいアクティビティへの参照を持つCallbackを設定してあげる
            downloader.setCallBack(new DownloaderCallBack(this));

            showProgressDialog(this);
        }

        adView.resume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        if (downloader != null && downloader.isInProcess()) {
            // アクティビティが消える前にダイアログを終了させる
            dismissProgressDialog();
        }

        adView.pause();
        super.onPause();
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

        switch (item.getItemId()) {
            case R.id.action_download:
                downloader = new Nk225Downloader(new DownloaderCallBack(MainActivity.this));
                downloader.execute();
                return true;

            case R.id.action_settings:
                Toast.makeText(this, "設定", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.action_db_init:
                RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
                Realm.deleteRealm(realmConfig);
                Realm.setDefaultConfiguration(realmConfig);
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

    private static Nk225Downloader downloader;

    private class DownloaderCallBack implements Nk225Downloader.DownloadCallBack {
        private Context context;

        public DownloaderCallBack(Context context) {
            Log.d(TAG, "DownloaderCallBack");
            this.context = context;
        }

        @Override
        public void onPreDownload() {
            Log.d(TAG, "onPreDownload");
            showProgressDialog(context);
        }

        @Override
        public void onPostDownload(boolean result) {
            Log.d(TAG, "onPostDownload");
            dismissProgressDialog();

            Toast.makeText(MainActivity.this, "画面を下に引っ張り、表示を更新して下さい", Toast.LENGTH_LONG).show();
        }
    }
}
