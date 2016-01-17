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
    public static class ViewHolder {
        AppListBaseAdapter appListBaseAdapter;
        AppManager appManager;
        AppData data;
        public TextView scoreText;
        public View scoreLayout;
        public View progressLayout;
        public TextView progressText;
        public ProgressBar progressBar;
        public ImageView icon;
        public TextView title;
        public Button open;
        public Button button;

        public void updateProgress() {
            DownloadAppAsyncTask downloadAppAsyncTask = appManager.getDownloadingAsyncTask(data);
            if (null != downloadAppAsyncTask) {
                int progress = downloadAppAsyncTask.getProgress();
                int maxProgress = downloadAppAsyncTask.getMaxProgress();
                long size = downloadAppAsyncTask.getSize();
                progressText.setText(FilesManager.getFileSize(size * progress / maxProgress) + " / " + FilesManager.getFileSize(size));
                progressBar.setProgress(progress);
                progressBar.setMax(maxProgress);
            }
        }
    }
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
        final ViewHolder viewHolder;
        Object tag = convertView.getTag();
        if (tag instanceof ViewHolder) {
            viewHolder = (ViewHolder) tag;
        } else {
            viewHolder = new ViewHolder();
            viewHolder.appListBaseAdapter = this;
            viewHolder.appManager = appManager;
            viewHolder.scoreText = (TextView) convertView.findViewById(R.id.scoreText);
            viewHolder.scoreLayout = convertView.findViewById(R.id.scoreLayout);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.open = (Button) convertView.findViewById(R.id.open);
            viewHolder.button = (Button) convertView.findViewById(R.id.button);
            viewHolder.progressLayout = convertView.findViewById(R.id.progressLayout);
            viewHolder.progressText = (TextView) viewHolder.progressLayout.findViewById(R.id.progressText);
            viewHolder.progressBar = (ProgressBar) viewHolder.progressLayout.findViewById(R.id.progress);
            convertView.setTag(viewHolder);
        }
        final AppData data = getItem(position);
        viewHolder.data = data;

        updateScoreLayout(viewHolder.scoreLayout, data);
        viewHolder.scoreText.setText(String.valueOf(data.getScore()));

        Picasso.with(context).load(Uri.parse(data.getIcon())).into(viewHolder.icon);
        viewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataShowingScore.put(data, !(dataShowingScore.containsKey(data) && dataShowingScore.get(data)));
                updateScoreLayout(viewHolder.scoreLayout, data);
            }
        });

        viewHolder.title.setText(data.getTitle());

        viewHolder.open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appManager.openApk(context, data.getPkg());
            }
        });
        viewHolder.open.setVisibility(View.GONE);
        viewHolder.progressLayout.setVisibility(View.GONE);
        if (appManager.isDownloading(data)) {
            viewHolder.button.setText(R.string.app_cancel_downloading);
            viewHolder.progressLayout.setVisibility(View.VISIBLE);
            viewHolder.updateProgress();
        } else if (appManager.isInstalled(context, data)) {
            viewHolder.button.setText(R.string.app_uninstall);
            viewHolder.open.setVisibility(View.VISIBLE);
        } else {
            viewHolder.button.setText(R.string.app_install);
        }
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appManager.isDownloading(data)) {
                    appManager.cancelDownloading(data);
                } else if (appManager.isInstalled(context, data)) {
                    appManager.unInstallApk(context, data);
                } else {
                    appManager.startDownload(context, data, viewHolder);
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
