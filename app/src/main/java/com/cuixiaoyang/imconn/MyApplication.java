package com.cuixiaoyang.imconn;

import android.app.Application;

import com.cuixiaoyang.imconn.model.database.MsgDatabase;
import com.example.appdb.InitDatabase;

/**
 * @author
 * @date 2020/10/12.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        InitDatabase.initDatabase(this);
        MsgDatabase.getInstance(this);
    }
}
