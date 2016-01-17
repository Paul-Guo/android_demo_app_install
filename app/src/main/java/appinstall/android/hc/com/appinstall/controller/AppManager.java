package appinstall.android.hc.com.appinstall.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.common.base.Functions;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;

import net.android.hc.com.HttpHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import appinstall.android.hc.com.appinstall.datas.AppData;
import appinstall.android.hc.com.appinstall.datas.AppDataList;
import appinstall.android.hc.com.appinstall.views.AppListBaseAdapter;
import okhttp3.internal.io.FileSystem;

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

    private PackageManager getPm(Context context) {
        if (null == pm) {
            if (null != context) {
                pm = context.getPackageManager();
            }
        }
        return pm;
    }

    public AppDataList getAppDataListFromNet(final String url) {
        HttpHelper.OkHttpResponse response = null;
        try {
            response = httpHelper.getWizHttpCodeReturn(new URL(url));
            if (null != response) {
                String string = response.getResponseString();
                string = string.replace("mailesong.test.com", "cn.com.mcdonalds.m4d");
                AppDataList appDataList = AppDataList.fromJsonStr(string);
                Log.e("test_app_list", new Gson().toJson(appDataList).toString());
                return appDataList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != response) {
                response.close();
            }
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
        if (null != data) {
            return isInstalled(context, data.getPkg());
        }
        return false;
    }

    public boolean isInstalled(Context context, String pkg) {
        if (null != pkg && null != context) {
            PackageInfo info = null;
            PackageManager pm = getPm(context);
            try {
                info = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
            } catch (Exception e) {
//                        e.printStackTrace();
            }
            return null != info && null != info.packageName && info.packageName.equals(pkg);
        }
        return false;
    }

    public DownloadAppAsyncTask startDownload(final Context context, final AppData data,
                                              final AppListBaseAdapter.ViewHolder viewHolder) {
        synchronized (asyncTaskHashMap) {
            if (null != data && null != data.getPkg()) {
                if (!isDownloading(data)) {
                    DownloadAppAsyncTask asyncTask = new DownloadAppAsyncTask(
                            asyncTaskHashMap, context, data, viewHolder, httpHelper);
                    asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    asyncTaskHashMap.put(data.getPkg(), asyncTask);
                    return asyncTask;
                }
            }
            return null;
        }
    }

    public boolean isDownloading(AppData data) {
        synchronized (asyncTaskHashMap) {
            DownloadAppAsyncTask asyncTask = getDownloadingAsyncTask(data);
            if (null != asyncTask && !asyncTask.isCancelled()) {
                AsyncTask.Status status = asyncTask.getStatus();
                return status != AsyncTask.Status.FINISHED;
            }
            return false;
        }
    }

    public DownloadAppAsyncTask getDownloadingAsyncTask(AppData data) {
        synchronized (asyncTaskHashMap) {
            if (null != data && null != data.getPkg()) {
                AsyncTask asyncTask = null;
                if (asyncTaskHashMap.containsKey(data.getPkg())) {
                    asyncTask = asyncTaskHashMap.get(data.getPkg());
                }
                if (asyncTask instanceof DownloadAppAsyncTask) {
                    return (DownloadAppAsyncTask) asyncTask;
                }
            }
            return null;
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
        try {
            FileSystem.SYSTEM.delete(new File(FilesManager.getCachedFileFromPkg(pkg)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                HashMap<String, String> ps = new HashMap<String, String>();
                ps.put("id", "13816872244");
                ps.put("package", pkg);
                HttpHelper.OkHttpResponse response = null;
                try {
                    response = httpHelper.postWizHttpCodeReturn(new URL("http://182.254.150.12:8080/log"), ps);
                    if (null != response) {
                        Log.e("test", response.getMsg() + response.getResponseString() + response.getHttpCode());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != response) {
                        response.close();
                    }
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public String getPkgFromIntent(Intent intent) {
        if (null != intent) {
            final Bundle intentExtras = intent.getExtras();
            if (null != intentExtras) {
                String action = intentExtras.getString("action");
                if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                        || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                        || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                    Uri data = intent.getData();
                    return data.getSchemeSpecificPart();
                }
            }
        }
        return null;
    }

    public boolean handleIntent(Intent intent) {
        if (null != intent) {
            final Bundle intentExtras = intent.getExtras();
            if (null != intentExtras) {
                String action = intentExtras.getString("action");
                if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    Uri data = intent.getData();
                    Log.e("test_app_list", data.toString());
                    Log.e("test_app_list", data.getSchemeSpecificPart());
                    reportInstalledApk(data.getSchemeSpecificPart());
                }
                if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                        || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                        || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                    return true;
                }
            }
        }
        return false;
    }
}
