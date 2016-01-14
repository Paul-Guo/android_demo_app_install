package appinstall.android.hc.com.appinstall.views;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

import appinstall.android.hc.com.appinstall.R;
import appinstall.android.hc.com.appinstall.controller.AppManager;
import appinstall.android.hc.com.appinstall.controller.DownloadAppAsyncTask;
import appinstall.android.hc.com.appinstall.controller.FilesManager;
import appinstall.android.hc.com.appinstall.datas.AppData;
import appinstall.android.hc.com.appinstall.datas.AppDataList;

/**
 * Created by paulguo on 2016/1/12.
 */
public class AppListBaseAdapter extends BaseAdapter {
    AppManager appManager = AppManager.getInstance();
    AppDataList appDataList = new AppDataList();

    HashMap<AppData, Boolean> dataShowingScore = new HashMap<>();

    @Override
    public int getCount() {
        return appDataList.getApps().size();
    }

    @Override
    public AppData getItem(int position) {
        return appDataList.getApps().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        if (null == convertView) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.app_list_item, parent, false);
        }
        final AppData data = getItem(position);

        final View scoreLayout = convertView.findViewById(R.id.scoreLayout);
        updateScoreLayout(scoreLayout, data);
        final TextView scoreText = (TextView) convertView.findViewById(R.id.scoreText);
        scoreText.setText(String.valueOf(data.getScore()));

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        Picasso.with(context).load(Uri.parse(data.getIcon())).into(icon);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataShowingScore.put(data, !(dataShowingScore.containsKey(data) && dataShowingScore.get(data)));
                updateScoreLayout(scoreLayout, data);
            }
        });

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(data.getTitle());

        Button open = (Button) convertView.findViewById(R.id.open);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appManager.openApk(context, data.getPkg());
            }
        });
        Button button = (Button) convertView.findViewById(R.id.button);
        open.setVisibility(View.GONE);
        View progressLayout = convertView.findViewById(R.id.progressLayout);
        progressLayout.setVisibility(View.GONE);
        if (appManager.isDownloading(data)) {
            button.setText(R.string.app_cancel_downloading);
            progressLayout.setVisibility(View.VISIBLE);
            DownloadAppAsyncTask downloadAppAsyncTask = appManager.getDownloadingAsyncTask(data);
            int progress = downloadAppAsyncTask.getProgress();
            int maxProgress = downloadAppAsyncTask.getMaxProgress();
            long size = downloadAppAsyncTask.getSize();
            TextView progressText = (TextView) progressLayout.findViewById(R.id.progressText);
            progressText.setText(FilesManager.getFileSize(size * progress / maxProgress) + " / " + FilesManager.getFileSize(size));
            ProgressBar progressBar = (ProgressBar) progressLayout.findViewById(R.id.progress);
            progressBar.setProgress(progress);
            progressBar.setMax(maxProgress);
        } else if (appManager.isInstalled(context, data)) {
            button.setText(R.string.app_uninstall);
            open.setVisibility(View.VISIBLE);
        } else {
            button.setText(R.string.app_install);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appManager.isDownloading(data)) {
                    appManager.cancelDownloading(data);
                } else if (appManager.isInstalled(context, data)) {
                    appManager.unInstallApk(context, data);
                } else {
                    appManager.startDownload(context, data, AppListBaseAdapter.this);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    private void updateScoreLayout(final View scoreLayout, AppData data) {
        if (dataShowingScore.containsKey(data) && dataShowingScore.get(data)) {
            if (scoreLayout.getVisibility() == View.GONE) {
                scoreLayout.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(
                        scoreLayout.getContext(), android.R.anim.fade_in);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                scoreLayout.startAnimation(anim);
            }
        } else {
            if (scoreLayout.getVisibility() != View.GONE) {
                Animation anim = AnimationUtils.loadAnimation(
                        scoreLayout.getContext(), android.R.anim.fade_out);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        scoreLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                scoreLayout.startAnimation(anim);
            }
        }
    }

    public AppDataList getAppDataList() {
        return appDataList;
    }

    public void setAppDataList(AppDataList appDataList) {
        if (null != appDataList) {
            this.appDataList = appDataList;
        }
    }
}
