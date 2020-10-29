package com.cuixiaoyang.imconn.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appdb.InitDatabase;
import com.example.appdb.model.dao.MessageDao;
import com.example.appdb.model.entity.Message;

import java.util.Collections;
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
    private MessageDao messageDao = null;
    private ExecutorService mThreadPool = Executors.newCachedThreadPool();
    //可修改的LiveData
    private MutableLiveData<List<Message>> _messageList = new MutableLiveData<>();
    //对View暴露的 被监听到 不可改的 LiveData
    public LiveData<List<Message>> messageList = _messageList;

    public ChatViewModel() {
        initDao();
    }

    private void initDao() {
        if(messageDao == null)
            synchronized (ChatViewModel.class) {
                if (messageDao == null) {
                    messageDao = InitDatabase.getAppDB().getMessageDao();
                }
            }
    }

    //后台往聊天信息表中插入一条消息
    public void insertMsg(Message info,Callback callback) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                long id = messageDao.insertOneMessageToList(info);
                if (id > 0) {
                    callback.onExecute(true);
                } else {
                    callback.onExecute(false);
                }
            }
        });

    }

    //聊天界面往聊天信息表中插入一条消息
    public void insertMsg(Message info,String deviceId,int num ,Callback callback) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                long id = messageDao.insertOneMessageToList(info);
                if (id > 0) {
                    List<Message> allMsgs = messageDao.getFriendMessageList(deviceId, num, 0);
                    if (allMsgs != null) {
                        Collections.reverse(allMsgs);
                        _messageList.postValue(allMsgs);
                    }
                    callback.onExecute(true);
                } else {
                    callback.onExecute(false);
                }
            }
        });

    }

    //删除聊天信息表中的某条消息
    public void deleteMsg(int m_ID,Callback callback){
        mThreadPool.execute( () ->{
            Message m=messageDao.getMessageInfo(m_ID);
            if(m!=null){
                int result=messageDao.deleteOneMessageFromList(m);
                if(result!=0){
                    callback.onExecute(true);
                }else{
                    callback.onExecute(false);
                }
            }else{
                callback.onExecute(false);
            }
        });
    }

    //改变消息的发送状态
    public void changeSentStatue(long m_Time, int m_SendStatus, Callback callback) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Message m=messageDao.getMessageInfoByTime(m_Time);
                if(m!=null){
                    m.setMSendStatus(m_SendStatus);
                    int result=messageDao.updateMessageInfo(m);
                    if(result!=0){
                        callback.onExecute(true);
                    }else{
                        callback.onExecute(false);
                    }
                }else{
                    callback.onExecute(false);
                }
            }
        });

    }


    //更新聊天信息表 找到和指定deviceId人的消息列表
    public void updateMsgWithSomebody(String deviceId,int num,int start){
        mThreadPool.execute(()->{
            List<Message> allMsgs = messageDao.getFriendMessageList(deviceId, num, start);
            if (allMsgs != null) {
                Collections.reverse(allMsgs);
                _messageList.postValue(allMsgs);
            }
        });
    }

    //上拉刷新获取更多聊天信息表 （和指定deviceId人的消息列表）
    public void obtainMoreMsgWithSomebody(String deviceId,int num,int start,Callback callback){
        mThreadPool.execute(()->{
            List<Message> allMsgs = messageDao.getFriendMessageList(deviceId, num, start);
            if (allMsgs != null) {
                Collections.reverse(allMsgs);
                _messageList.postValue(allMsgs);
                callback.onExecute(true);
            }
        });
    }

    public interface Callback{
        void onExecute(boolean b);
    }
}
