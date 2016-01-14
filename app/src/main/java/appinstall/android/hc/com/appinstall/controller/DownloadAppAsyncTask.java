package appinstall.android.hc.com.appinstall.controller;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import appinstall.android.hc.com.appinstall.datas.AppData;

/**
 * Created by paulguo on 2016/1/12.
 */
public class DownloadAppAsyncTask extends AsyncTask<Void, Integer, File> {

    DownloadAppAsyncTask(HashMap<String, AsyncTask> asyncTaskHashMap,
                         Context context,
                         AppData data) {
        this.context = context;
        this.asyncTaskHashMap = asyncTaskHashMap;
        this.data = data;
    }

    Context context;
    HashMap<String, AsyncTask> asyncTaskHashMap;
    AppData data;
    ByteSource byteSource = null;
    FileOutputStream fileOutputStream;

    @Override
    protected File doInBackground(Void... params) {
        File file = null;
        publishProgress(0);
        if (null != data) {
            try {
                String cache = AppManager.getCachePath() + "/" + data.getPkg();
                file = new File(cache);
                Files.createParentDirs(file);
                fileOutputStream = new FileOutputStream(file);
                byteSource = Resources.asByteSource(new URL(data.getUrl()));
                byteSource.copyTo(fileOutputStream);
                closeStream();
            } catch (Exception e) {
                e.printStackTrace();
                file = null;
            }
        }
        publishProgress(100);
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
            if (null != byteSource) {
                Closeables.close(byteSource.openStream(), true);
                byteSource = null;
            }
            if (null != fileOutputStream) {
                fileOutputStream.close();
                fileOutputStream = null;
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
