package appinstall.android.hc.com.appinstall;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Created by paulguo on 2016/1/12.
 */
public class MainBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent activityIntent = new Intent();
        activityIntent.setComponent(new ComponentName(context, MainActivity.class));
        activityIntent.setData(intent.getData());
        activityIntent.putExtra("action", intent.getAction());
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }
}
