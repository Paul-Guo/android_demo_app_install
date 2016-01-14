package appinstall.android.hc.com.appinstall.controller;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

import appinstall.android.hc.com.appinstall.datas.AppData;
import appinstall.android.hc.com.appinstall.views.AppListBaseAdapter;

/**
 * Created by paulguo on 2016/1/12.
 */
public class DownloadAppAsyncTask extends AsyncTask<Void, Integer, File> {

    DownloadAppAsyncTask(HashMap<String, AsyncTask> asyncTaskHashMap,
                         Context context,
                         AppData data,
                         AppListBaseAdapter appListBaseAdapter) {
        this.context = context;
        this.asyncTaskHashMap = asyncTaskHashMap;
        this.data = data;
        this.appListBaseAdapter = appListBaseAdapter;
    }

    Context context;
    HashMap<String, AsyncTask> asyncTaskHashMap;
    AppData data;
    AppListBaseAdapter appListBaseAdapter;
    Closer closer;
    long size;
    int maxProgress = 100;
    int progress;

    public long getSize() {
        return size;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    protected File doInBackground(Void... params) {
        File file = null;
        publishProgress(0);
        if (null != data) {
            try {
                closer = Closer.create();
                URL url = new URL(data.getUrl());
                ByteSource byteSource = Resources.asByteSource(url);
                size = byteSource.size();
                InputStream inputStream = byteSource.openStream();
                closer.register(inputStream);
                String cache = FilesManager.getCachedFileFromPkg(data.getPkg());
                file = new File(cache);
                Files.createParentDirs(file);
                ByteSink byteSink = Files.asByteSink(file);
                final OutputStream outputStream = byteSink.openStream();
                closer.register(outputStream);
                long readResult = ByteStreams.readBytes(inputStream,
                        new ByteProcessor<Long>() {
                            long readRet = 0;

                            @Override
                            public boolean processBytes(byte[] bytes, int off, int len) throws IOException {
                                outputStream.write(bytes, off, len);
                                readRet += len;
                                publishProgress(size > 0 ? (int) (maxProgress * readRet / size) : 0);
                                return true;
                            }

                            @Override
                            public Long getResult() {
                                return readRet;
                            }
                        });
                if (size != readResult) {
                    file = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                file = null;
            } finally {
                closeStream();
            }
        }
        publishProgress(maxProgress);
        return file;
    }

    @Override
    protected void onPostExecute(File o) {
        Log.e("test", "finished : " + data.getPkg() + "/" + o);
        super.onPostExecute(o);
        synchronized (asyncTaskHashMap) {
            asyncTaskHashMap.remove(data.getPkg());
        }
        if (null != o) {
            if (o.exists()) {
                AppManager.getInstance().installApk(context, Uri.fromFile(o));
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progress = values[0];
        if (null != appListBaseAdapter) {
            appListBaseAdapter.notifyDataSetChanged();
        }
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        closeStream();
        Log.e("test", "canceled 1 : " + data.getPkg());
    }

    @Override
    protected void onCancelled(File file) {
        super.onCancelled(file);
        closeStream();
        Log.e("test", "canceled 2 : " + data.getPkg());
    }

    private void closeStream() {
        try {
            if (null != closer) {
                closer.close();
                closer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        execute(new Runnable() {
            @Override
            public void run() {
                closeStream();
            }
        });
    }
}
