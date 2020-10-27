package com.cuixiaoyang.imconn.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cuixiaoyang.imconn.model.bean.MessageInfo;
import com.cuixiaoyang.imconn.model.dao.MsgDao;
import com.cuixiaoyang.imconn.model.database.MsgDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author
 * @date 2020/10/13.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class ChatViewModel extends ViewModel {
    private MsgDao msgDao = null;
    private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();
    //可修改的LiveData
    private MutableLiveData<List<MessageInfo>> _messageList = new MutableLiveData<>();
    //对View暴露的 被监听到 不可改的 LiveData
    public LiveData<List<MessageInfo>> messageList = _messageList;

    public ChatViewModel() {
        initDao();
    }

    private void initDao() {
        if (msgDao == null) {
            msgDao = MsgDatabase.getInstance().getUserDao();
        }
    }

    //往聊天信息表中插入一条消息
    public void insertMsg(MessageInfo info, String deviceId, Callback callback) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                msgDao.insert(info);
                List<MessageInfo> allMsgs = msgDao.getMsgsWithSomebody(deviceId);
                _messageList.postValue(allMsgs);
                callback.onInsertInfoDone();
            }
        });

    }

    //往聊天信息表中插入一条消息
    public void changeSentStatue(long time, int sign, String deviceId, Callback callback) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                msgDao.setSentStatus(time,sign);
                List<MessageInfo> allMsgs = msgDao.getMsgsWithSomebody(deviceId);
                _messageList.postValue(allMsgs);
                callback.onInsertInfoDone();
            }
        });

    }


    //查询聊天信息表 找到和指定deviceId人的消息列表
    public void getAllMsgWithSomebody(String deviceId){
        mThreadPool.execute(()->{
            List<MessageInfo> allMsgs = msgDao.getMsgsWithSomebody(deviceId);
            _messageList.postValue(allMsgs);
    });
    }

    public interface Callback{
        void onInsertInfoDone();
    }
}
