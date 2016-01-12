package appinstall.android.hc.com.appinstall.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.common.io.Resources;
import com.google.gson.Gson;

import net.android.hc.com.HttpHelper;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import appinstall.android.hc.com.appinstall.datas.AppData;
import appinstall.android.hc.com.appinstall.datas.AppDataList;

/**
 * Created by paulguo on 2016/1/11.
 */
public class AppManager {
    private static final String tag = AppManager.class.getName();

    private AppManager() {

    }

    private static AppManager instance;

    public static final AppManager getInstance() {
        if (null == instance) {
            instance = new AppManager();
        }
        return instance;
    }

    private HttpHelper httpHelper = new HttpHelper();
    private HashMap<String, AsyncTask> asyncTaskHashMap = new HashMap<>();
    private PackageManager pm;

    public final static String getCachePath() {
        return Environment.getExternalStorageDirectory() + "/AppInstall/cache";
    }

    private PackageManager getPm(Context context) {
        if (null == pm) {
            if (null != context) {
                pm = context.getPackageManager();
            }
        }
        return pm;
    }

    public AppDataList getAppDataListFromNet(final String url) {
        try {
            String string = Resources.toString(new URL(url), Charset.defaultCharset());
            string = string.replace("mailesong.test.com", "cn.com.mcdonalds.m4d");
            AppDataList appDataList = AppDataList.fromJsonStr(string);
            Log.e("test_app_list", new Gson().toJson(appDataList).toString());
            return appDataList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean openApk(Context context, String pkg) {
        if (null != pkg && null != context) {
            PackageManager packageManager = getPm(context);
            Intent intent = packageManager.getLaunchIntentForPackage(pkg);
            if (null != intent) {
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    public boolean installApk(Context context, Uri uri) {
        if (null != uri && null != context) {
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setDataAndType(uri, "application/*");
            intent.setComponent(new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerActivity"));
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    public boolean unInstallApk(Context context, AppData data) {
        if (null != data && null != data.getPkg() && null != context) {
            Uri packageUri = Uri.parse("package:" + data.getPkg());
            Intent uninstallIntent =
                    new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            context.startActivity(uninstallIntent);
            return true;
        }
        return false;
    }

    public boolean isInstalled(Context context, AppData data) {
        if (null != data && null != data.getPkg() && null != context) {
            PackageInfo info = null;
            PackageManager pm = getPm(context);
            try {
                info = pm.getPackageInfo(data.getPkg(), PackageManager.GET_ACTIVITIES);
            } catch (Exception e) {
//                        e.printStackTrace();
            }
            return null != info && null != info.packageName && info.packageName.equals(data.getPkg());
        }
        return false;
    }

    public boolean startDownload(final Context context, final AppData data) {
        synchronized (asyncTaskHashMap) {
            if (null != data && null != data.getPkg()) {
                if (!isDownloading(data)) {
                    DownloadAppAsyncTask asyncTask = new DownloadAppAsyncTask(asyncTaskHashMap, context, data);
                    asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    asyncTaskHashMap.put(data.getPkg(), asyncTask);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isDownloading(AppData data) {
        synchronized (asyncTaskHashMap) {
            if (null != data && null != data.getPkg()) {
                AsyncTask asyncTask = null;
                if (asyncTaskHashMap.containsKey(data.getPkg())) {
                    asyncTask = asyncTaskHashMap.get(data.getPkg());
                }
                if (null != asyncTask && !asyncTask.isCancelled()) {
                    AsyncTask.Status status = asyncTask.getStatus();
                    return status != AsyncTask.Status.FINISHED;
                }
            }
            return false;
        }
    }

    public boolean cancelDownloading(AppData data) {
        synchronized (asyncTaskHashMap) {
            if (null != data && null != data.getPkg()) {
                AsyncTask asyncTask = null;
                if (asyncTaskHashMap.containsKey(data.getPkg())) {
                    asyncTask = asyncTaskHashMap.get(data.getPkg());
                }
                if (null != asyncTask && !asyncTask.isCancelled()) {
                    asyncTask.cancel(true);
                    if (asyncTask instanceof DownloadAppAsyncTask) {
                        ((DownloadAppAsyncTask) asyncTask).release();
                    } else {
                    }
                    asyncTaskHashMap.remove(data.getPkg());
                    return true;
                }
            }
            return false;
        }
    }

    public void onDestroy() {
        synchronized (asyncTaskHashMap) {
            for (String key : asyncTaskHashMap.keySet()) {
                asyncTaskHashMap.get(key).cancel(true);
            }
            asyncTaskHashMap.clear();
        }
    }

    public void reportInstalledApk(final String pkg) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> ps = new HashMap<String, String>();
                ps.put("id", "13816872244");
                ps.put("package", pkg);
                try {
                    HttpHelper.OkHttpResponse response = httpHelper.postWizHttpCodeReturn(new URL("http://182.254.150.12:8080/log"), ps);
                    Log.e("test", response.getMsg() + response.getResponseString() + response.getHttpCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
