package appinstall.android.hc.com.appinstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by paulguo on 2016/1/12.
 */
public class MainBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(intent);
    }
}
