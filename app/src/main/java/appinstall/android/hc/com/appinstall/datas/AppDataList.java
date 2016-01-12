package appinstall.android.hc.com.appinstall.datas;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by paulguo on 2016/1/11.
 */
public class AppDataList {
    private int code;
    private ArrayList<AppData> apps = new ArrayList<>();

    public ArrayList<AppData> getApps() {
        return apps;
    }

    public void setApps(ArrayList<AppData> apps) {
        this.apps = apps;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public final static AppDataList fromJsonStr(String str) {
        return new GsonBuilder().setFieldNamingStrategy(new FieldNamingStrategy() {
            @Override
            public String translateName(Field f) {
                String filedName = FieldNamingPolicy.IDENTITY.translateName(f);
                if ("pkg".equals(filedName)) {
                    filedName = "package";
                }
                return filedName;
            }
        }).create().fromJson(str, AppDataList.class);
    }
}
