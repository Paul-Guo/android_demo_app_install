package appinstall.android.hc.com.appinstall;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import appinstall.android.hc.com.appinstall.controller.AppManager;
import appinstall.android.hc.com.appinstall.datas.AppDataList;
import appinstall.android.hc.com.appinstall.views.AppListBaseAdapter;

public class MainActivity extends AppCompatActivity {
    AppManager appManager = AppManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        final ListView appListView = (ListView) findViewById(R.id.appsListView);
        appListView.setAdapter(appListViewBaseAdapter);
        new AsyncTask<Void, Void, AppDataList>() {
            @Override
            protected AppDataList doInBackground(Void... params) {
                return appManager.getAppDataListFromNet("http://182.254.150.12:8080/apps");
            }

            @Override
            protected void onPostExecute(AppDataList aVoid) {
                super.onPostExecute(aVoid);
                appListViewBaseAdapter.setAppDataList(aVoid);
                appListViewBaseAdapter.notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        checkIntent(getIntent());
    }

    final AppListBaseAdapter appListViewBaseAdapter = new AppListBaseAdapter();

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(final Intent intent) {
        if (null != intent) {
            final Bundle intentExtras = intent.getExtras();
            if (null != intentExtras) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String action = intentExtras.getString("action");
                        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                            Uri data = intent.getData();
                            Log.e("test_app_list", data.toString());
                            Log.e("test_app_list", data.getSchemeSpecificPart());
                            appManager.reportInstalledApk(data.getSchemeSpecificPart());
                        }
                        appListViewBaseAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        appManager.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        appListViewBaseAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
