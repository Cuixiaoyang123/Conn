package com.cuixiaoyang.connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cuixiaoyang.connection.msg.ImageMsg;
import com.cuixiaoyang.connection.msg.Msg;
import com.cuixiaoyang.connection.msg.ReplyMsg;
import com.cuixiaoyang.connection.msg.TextMsg;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * @author
 * @date 2020/9/24.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class Connection {

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case Constant.MESSAGE_TEXT:
                    onMsgListener.onReceiveMsg(Constant.MESSAGE_TEXT, new TextMsg((String) msg.obj));
                    break;
                case Constant.MESSAGE_IMAGE:
                    onMsgListener.onReceiveMsg(Constant.MESSAGE_IMAGE, new ImageMsg((Bitmap) msg.obj));
                    break;
                case Constant.MESSAGE_REPLY:
                    onMsgListener.onReceiveMsg(Constant.MESSAGE_REPLY,new ReplyMsg(msg.arg1, (String) msg.obj));
                    break;
//                case Constant.BROADCAST_ONLINE:
//                    onMsgListener.onReceiveMsg(Constant.BROADCAST_ONLINE,new TextMsg((String) msg.obj));
//                    break;
//                case Constant.BROADCAST_OUTLINE:
//                    onMsgListener.onReceiveMsg(Constant.BROADCAST_OUTLINE,new TextMsg((String) msg.obj));
//                    break;
                default:
                    break;
            }
        }
    };

    public interface onMsgListener {
        void onReceiveMsg(int type, Msg msg);
    }


    private static final String TAG = "Connection";
    private static final String SERVER_STATE = "200";

    public onMsgListener onMsgListener;
    private static Connection connection;

    public static Connection getInstance() {

        if (connection == null) {
            synchronized (Connection.class) {
                if (connection == null) {
                    connection = new Connection();
                }
            }
        }
        return connection;
    }

    public void setOnMsgListener(onMsgListener listener) {
        this.onMsgListener = listener;
    }

    public void initServer() {

        new Thread("listener") {
            @Override
            public void run() {


                //消息类型
                byte[] messageType = new byte[1];
                try {
                    ServerSocket serverSocket = new ServerSocket(Constant.SERVER_PORT);
                    while (true) {
                        Message msg = new Message();

                        OutputStream output;

                        //读取信息
                        StringBuilder result = new StringBuilder();
                        String buffer;

                        Socket socket = serverSocket.accept();
                        //向client发送 确认消息
                        output = socket.getOutputStream();
                        output.write(SERVER_STATE.getBytes("utf-8"));
                        output.flush();
                        socket.shutdownOutput();

                        //获取 客户端信息
                        InputStream is = socket.getInputStream();

                        is.read(messageType);
                        Log.i(TAG, "run: messageType = " + Arrays.toString(messageType));

                        switch ((int)messageType[0]) {
                            case Constant.MESSAGE_TEXT:
                                //第一种方式：获取String类型的消息
                                BufferedReader bff = new BufferedReader(new InputStreamReader
                                        (is));
                                while ((buffer = bff.readLine()) != null) {
                                    result.append(buffer);
                                    Log.i(TAG, "run: result= " + buffer);
                                }

                                msg.what = Constant.MESSAGE_TEXT;//接文本消息
                                msg.obj = result.toString();
                                handler.sendMessage(msg);

                                bff.close();
                                is.close();
                                output.close();
                                socket.close();
                                break;
                            case Constant.MESSAGE_IMAGE:
                                Bitmap bitmap = BitmapFactory.decodeStream(is);
                                msg.what = Constant.MESSAGE_IMAGE;//接Image消息
                                msg.obj = bitmap;
                                handler.sendMessage(msg);
                                is.close();
                                output.close();
                                socket.close();
                                break;
                            case Constant.MESSAGE_VOICE:
                                break;
                            default:
                                break;
                        }

                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();

    }

//    /**
//     *
//     * @param myselfIp 自己的IP
//     * @param broadcast 广播的类型 上线 下线 。。。
//     */
//    public void sendInnerBroadcast(String myselfIp, int broadcast) {
//        String host = ConnUtils.getBroadcastHost();
//        try {
//            byte[] messages = myselfIp.getBytes("utf-8");
//            sendMessage(host, broadcast, messages);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 发送文本消息
     * @param host 对方IP
     * @param content 文本内容
     */
    public void sendMessage(final String host, final String content) {
        try {
            byte[] messages = content.getBytes("utf-8");
            sendMessage(host, Constant.MESSAGE_TEXT, messages);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    /**
     * * 发送图片消息
     * @param host 对方IP
     * @param bitmap 文本内容
     * @param quality 图片质量 0 — 100
     */
    public void sendImage(final String host, final Bitmap bitmap,int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] messages = baos.toByteArray();
        sendMessage(host, Constant.MESSAGE_IMAGE,messages);
    }

    public void sendImage(final String host, final Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] messages = baos.toByteArray();
        sendMessage(host, Constant.MESSAGE_IMAGE,messages);
    }

    public void sendMessage(final String host, final int type, final byte[] content) {

        new Thread(){
            @Override
            public void run() {
                //定义消息
                Message msg = new Message();
                msg.what = Constant.MESSAGE_REPLY;//发消息后的（服务端返回的）回复消息

                Socket socket = null;
                //获取输出流
                OutputStream os = null;
                //获取输入流
                BufferedReader bff = null;
                try {
                    //连接服务器 并设置连接超时为5秒
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host, Constant.SERVER_PORT), 1000);

                    //获取输出流
                    os = socket.getOutputStream();
                    //获取输入流
                    bff = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    //向服务器发送信息
                    byte[] bytes = encodeByte(type, content);
                    if (bytes.length > 340 * 1024) {
                        //多次写入
                        int count = (int) Math.ceil(bytes.length / (340.0 * 1024));
                        for (int i = 0; i < count; i++) {
                            if (i == count - 1) {
                                os.write(bytes, i * 304 * 1024, bytes.length - i * 304 * 1024);
                            }
                            os.write(bytes, i * 304 * 1024, 304 * 1024);
                        }
                    } else {
                        os.write(bytes);
                    }
                    os.flush();

                    //读取发来服务器信息
                    StringBuilder result = new StringBuilder();
                    String buffer = "";
                    while ((buffer = bff.readLine()) != null) {
                        result.append(buffer);
                        Log.i("cxy", "run: result= " + buffer);
                    }
                    msg.arg1 = Constant.MESSAGE_CONNECTION;
                    msg.obj = result.toString();
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(msg);
                    //关闭各种输入输出流
                    bff.close();
                    os.close();
                    socket.close();
                } catch (SocketTimeoutException aa) {
                    //连接超时 在UI界面显示消息
                    msg.arg1 = Constant.MESSAGE_DISCONNECTION;
                    msg.obj =  "服务器连接失败！请检查网络是否打开";
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    // 对方不在线
                    msg.arg1 = Constant.MESSAGE_DISCONNECTION;
                    if (e.getLocalizedMessage().equals("Broken pipe")) {
                        msg.obj =  "图片大小超过300kb";
                    }else {
                        msg.obj =  "无法链接到该好友";
                    }
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }finally {
                    try {
                    if (bff!=null) {
                        bff.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    private byte[] encodeByte(int type, byte[] content) {
        byte[] bytes = new byte[1 + content.length];
        bytes[0] = (byte) type; //标记消息类型
        System.arraycopy(content,0,bytes,1,content.length);
        Log.i(TAG, "run: Byte[]大小为 "+ bytes.length);
        return bytes;
    }

    class SendThread extends Thread {
        private SendThread sendThread;
        public SendThread getDefault() {
            if (sendThread == null) {
                synchronized (SendThread.class) {
                    if (sendThread == null) {
                        sendThread = new SendThread();
                    }
                }
            }
            return sendThread;
        }

    }

}
