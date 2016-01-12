package appinstall.android.hc.com.appinstall.views;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import appinstall.android.hc.com.appinstall.R;
import appinstall.android.hc.com.appinstall.controller.AppManager;
import appinstall.android.hc.com.appinstall.datas.AppData;
import appinstall.android.hc.com.appinstall.datas.AppDataList;

/**
 * Created by paulguo on 2016/1/12.
 */
public class AppListBaseAdapter extends BaseAdapter {
    AppManager appManager = AppManager.getInstance();
    AppDataList appDataList = new AppDataList();

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

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        Picasso.with(context).load(Uri.parse(data.getIcon())).into(icon);

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
        if (appManager.isDownloading(data)) {
            button.setText(R.string.app_cancel_downloading);
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
                    appManager.startDownload(context, data);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
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
