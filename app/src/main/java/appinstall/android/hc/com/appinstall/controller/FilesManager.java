package appinstall.android.hc.com.appinstall.controller;

import android.os.Environment;

/**
 * Created by 鸿程 on 2016/1/14.
 */
public class FilesManager {

    public final static String getCachePath() {
        return Environment.getExternalStorageDirectory() + "/AppInstall/cache";
    }

    public final static String getCachedFileFromPkg(String pkg) {
        return getCachePath() + "/" + pkg;
    }

    public final static String getFileSize(long size) {
        long kb = size % 1024;
        long sizekb = size / 1024;
        long mb = sizekb % 1024;
        long sizemb = sizekb / 1024;
        long gb = sizemb % 1024;
        long sizegb = sizemb / 1024;
        if (sizegb > 0) {
            return String.format("%.2fGB", sizegb + gb / 1024f);
        } else if (sizemb > 0) {
            return String.format("%.2fMB", sizemb + mb / 1024f);
        } else if (sizekb > 0) {
            return String.format("%.2fKB", sizekb + kb / 1024f);
        } else {
            return String.format("%.2fB", (float) size);
        }
    }
}
