package com.masa34.nk225analyzer.Task;

import android.util.Log;

abstract public class AbstractNk225DownloadProcess {

    private final String TAG = "Nk225DownloadProcess";

    private Nk225Downloader downloader = null;

    private class DownloaderCallBack implements Nk225Downloader.DownloadCallBack {

        private final String TAG = "DownloaderCallBack";

        @Override
        public void onPreDownload() {
            Log.d(TAG, "onPreDownload");

            onPreDownloadProcess();
        }

        @Override
        public void onPostDownload(boolean result) {
            Log.d(TAG, "onPostDownload");

            onPostDownloadProcess(result);

            downloader = null;
        }
    }

    public void execute() {
        Log.d(TAG, "execute");

        downloader = new Nk225Downloader(new DownloaderCallBack());
        downloader.execute();
    }

    public boolean isInProcess() {
        Log.d(TAG, "isInProcess");

        if (downloader != null) {
            return downloader.isInProcess();
        }

        return false;
    }

    abstract protected void onPreDownloadProcess();
    abstract protected void onPostDownloadProcess(boolean result);
}
