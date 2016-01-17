package appinstall.android.hc.com.appinstall.controller;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import net.android.hc.com.HttpHelper;

import java.io.File;
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
                         AppListBaseAdapter.ViewHolder viewHolder,
                         HttpHelper httpHelper) {
        this.context = context;
        this.asyncTaskHashMap = asyncTaskHashMap;
        this.data = data;
        this.viewHolder = viewHolder;
        this.httpHelper = httpHelper;
    }

    HttpHelper httpHelper;
    Context context;
    HashMap<String, AsyncTask> asyncTaskHashMap;
    AppData data;
    AppListBaseAdapter.ViewHolder viewHolder;
    Closer closer;
    HttpHelper.OkHttpResponse response;
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
        Log.e("test", "start progress " + params);
        File file = null;
        publishProgress(0);
        if (null != data) {
            try {
                closer = Closer.create();
                String cache = FilesManager.getCachedFileFromPkg(data.getPkg());
                file = new File(cache);
                Files.createParentDirs(file);
//                final long length = file.length();
                final long length = 0;
                URL url = new URL(data.getUrl());
                HashMap<String, String> headers = new HashMap<>();
//                headers.put("range", length + "-");
                response = httpHelper.getWizHttpCodeReturn(url, headers);
                if (null != response) {
                    size = response.getLength();
                    InputStream inputStream = response.getInputStream();
                    closer.register(inputStream);
                    ByteSink byteSink = Files.asByteSink(file);
                    final OutputStream outputStream = byteSink.openStream();
                    closer.register(outputStream);
                    publishProgress(size > 0 ? (int) (maxProgress * length / size) : 0);
                    long readResult = ByteStreams.readBytes(inputStream,
                            new ByteProcessor<Long>() {
                                long readRet = length;

                                @Override
                                public boolean processBytes(byte[] bytes, int off, int len) throws IOException {
                                    outputStream.write(bytes, off, len);
                                    readRet += len;
                                    publishProgress(size > 0 ? (int) (maxProgress * readRet / size) : 0);
                                    return !isCancelled();
                                }

                                @Override
                                public Long getResult() {
                                    return readRet;
                                }
                            });
                    if (size <= 0 || size != readResult || isCancelled()) {
                        file = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                file = null;
            } finally {
                closeStream();
            }
        }
        publishProgress(maxProgress);
        Log.e("test", "end progress " + params);
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
        if (null != viewHolder) {
            viewHolder.updateProgress();
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
            if (null != response) {
                response.close();
                response = null;
            }
            if (null != closer) {
                closer.close();
                closer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
