package appinstall.android.hc.com.appinstall;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Objects;

import java.util.ArrayList;

import appinstall.android.hc.com.appinstall.controller.AppManager;
import appinstall.android.hc.com.appinstall.datas.AppData;
import appinstall.android.hc.com.appinstall.datas.AppDataList;
import appinstall.android.hc.com.appinstall.views.AppListBaseAdapter;

public class MainActivity extends AppCompatActivity {
    AppManager appManager = AppManager.getInstance();

    ArrayList<String> scoredPkgs = new ArrayList<>();
    long myScore = 0;
    TextView myScoreTextView;

    final AppListBaseAdapter appListViewBaseAdapter = new AppListBaseAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myScoreTextView = (TextView) findViewById(R.id.myScoreText);
        updateMyScrollText();

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
                updateMyScrollText();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        checkIntent(getIntent());
    }

    private void updateMyScrollText() {
        if (appListViewBaseAdapter.getCount() > 0) {
            for (String pkg : scoredPkgs) {
                if (appManager.isInstalled(this, pkg)) {
                    for (AppData appData : appListViewBaseAdapter.getAppDataList().getApps()) {
                        if (Objects.equal(pkg, appData.getPkg())) {
                            myScore += appData.getScore();
                        }
                    }
                }
            }
            scoredPkgs.clear();
        }
        myScoreTextView.setText(String.valueOf(myScore));
        myScoreTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_fade_in_from_bottom));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(final Intent intent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appManager.handleIntent(intent)) {
                    String pkg = appManager.getPkgFromIntent(intent);
                    if (null != pkg) {
                        scoredPkgs.add(pkg);
                    }
                    appListViewBaseAdapter.notifyDataSetChanged();
                }
            }
        });
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
        updateMyScrollText();
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
