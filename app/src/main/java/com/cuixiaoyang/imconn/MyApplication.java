package com.cuixiaoyang.imconn;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cuixiaoyang.connection.Connection;
import com.cuixiaoyang.connection.Constant;
import com.cuixiaoyang.connection.msg.Msg;
import com.cuixiaoyang.connection.msg.ReplyMsg;
import com.cuixiaoyang.imconn.model.database.MsgDatabase;
import com.cuixiaoyang.imconn.viewModel.ChatViewModel;
import com.example.appdb.InitDatabase;
import com.example.appdb.model.entity.Message;
import com.example.appdb.modelApi.FriendApi;

/**
 * @author
 * @date 2020/10/12.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class MyApplication extends Application {
    private Context mContext;
    private ChatViewModel chatViewModel;

    @Override

    public void onCreate() {
        super.onCreate();
        mContext = this;
        InitDatabase.initDatabase(mContext);
//        FriendApi.insertFriendToList("10.11.15.243","xiaoxiong","R.drawable.your",
//                1,"10.11.15.243",1,"",0,(b)->{});
//        FriendApi.insertFriendToList("10.11.15.223","xiaocui","R.drawable.my",
//                1,"10.11.15.223",1,"",0,(b)->{});
        chatViewModel = new ChatViewModel();
        Connection.getInstance().initServer();
        Connection.getInstance().setOnMsgListener(new Connection.OnMsgListener() {
            @Override
            public void onReceiveMsg(int type, String fromIp, Msg msg) {
                String deviceId = Utils.findDeviceId(fromIp);
                switch (type) {
                    case Constant.MESSAGE_TEXT:
                        //插入数据库
                        chatViewModel.insertMsg(makeMsgFromReceText(deviceId, msg), (b) -> {
                            //b = true代表插入成功   b = false代表插入失败
                        });
                        break;
                    case Constant.MESSAGE_IMAGE:
                        byte[] content = (byte[]) msg.getContent();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
                        String path = Utils.saveBitmap(bitmap, mContext);
                        String miniPath = Utils.saveMiniBitmap(bitmap, mContext);
                        //插入数据库
                        chatViewModel.insertMsg(makeMsgFromReceImg(deviceId,path,miniPath,msg),(b)->{
                            //b = true代表插入成功   b = false代表插入失败
                        });
                        break;
                    case Constant.MESSAGE_REPLY:
                        long time = msg.getTime();
                        int replyCode = ((ReplyMsg) msg).getReplyCode();
                        if (replyCode == 20) {
                            //改变数据库消息状态
                            chatViewModel.changeSentStatue(time, 1, (b) -> {
                            });
                        } else {
                            //改变数据库消息状态
                            chatViewModel.changeSentStatue(time, 0, (b) -> {
                            });
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private Message makeMsgFromReceText(String deviceId, Msg msg){
        Message info = new Message();
        info.setMTime(msg.getTime());
        info.setMText((String) msg.getContent());
        info.setMMessageType(msg.getType());
        info.setMFriendID(deviceId);
        info.setMSendOrReceive(1);
        info.setMSendStatus(1);
        info.setMReadStatus(0);
        return info;
    }

    private Message makeMsgFromReceImg(String deviceId, String path,String miniPath, Msg msg){
        Message info = new Message();
        info.setMTime(msg.getTime());
        info.setMMessageType(msg.getType());
        info.setMPicture(path);
        info.setMPictureThumbnail(miniPath);
        info.setMFriendID(deviceId);
        info.setMSendOrReceive(1);
        info.setMSendStatus(1);
        info.setMReadStatus(0);
        return info;
    }
}
