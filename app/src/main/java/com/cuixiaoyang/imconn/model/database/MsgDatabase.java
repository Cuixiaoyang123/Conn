package com.cuixiaoyang.imconn.model.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cuixiaoyang.imconn.model.bean.MessageInfo;
import com.cuixiaoyang.imconn.model.dao.MsgDao;

/**
 * @author
 * @date 2020/10/12.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
@Database(entities = { MessageInfo.class }, version = 1,exportSchema = false)
public abstract class MsgDatabase extends RoomDatabase {
    private static final String DB_NAME = "MsgDatabase.db";
    private static volatile MsgDatabase instance;

    public static synchronized MsgDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    public static synchronized MsgDatabase getInstance() {
        if (instance == null) {
            throw new IllegalStateException("database is not initial yet !");
        }
        return instance;
    }

    private static MsgDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                MsgDatabase.class,
                DB_NAME).build();
    }

    public abstract MsgDao getUserDao();
}
