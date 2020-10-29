package com.cuixiaoyang.imconn.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


import com.cuixiaoyang.imconn.model.bean.MessageInfo;

import java.util.List;

/**
 * @author
 * @date 2020/10/12.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
@Dao
public interface MsgDao {

    @Query("SELECT * FROM messageInfo")
    List<MessageInfo> getAllMsgs();

    @Query("SELECT * FROM messageInfo WHERE mFriendID =:deviceId order by mTime ASC")
    List<MessageInfo> getMsgsWithSomebody(String deviceId);

    @Insert
    void insert(MessageInfo msg);

    @Query("UPDATE messageInfo SET mSendStatus= :sign WHERE mTime = :time")
    void setSentStatus(long time,int sign);

    @Update
    void update(MessageInfo msg);

    @Delete
    void delete(MessageInfo msg);
}
