package com.cuixiaoyang.connection;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import com.cuixiaoyang.connection.exception.NoListenerException;
import com.cuixiaoyang.connection.msg.ImageMsg;
import com.cuixiaoyang.connection.msg.Msg;
import com.cuixiaoyang.connection.msg.ReplyMsg;
import com.cuixiaoyang.connection.msg.TextMsg;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author
 * @date 2020/9/24.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class Connection {

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                if (onMsgListener == null) {
                        throw new NoListenerException("请注册监听回调", 0);
                }
                switch (msg.what) {
                    case Constant.MESSAGE_TEXT:
                        onMsgListener.onReceiveMsg(Constant.MESSAGE_TEXT,msg.getData().getString("fromIp"), (TextMsg) msg.obj);
                        break;
                    case Constant.MESSAGE_IMAGE:
                        onMsgListener.onReceiveMsg(Constant.MESSAGE_IMAGE,msg.getData().getString("fromIp"), (ImageMsg) msg.obj);
                        break;
                    case Constant.MESSAGE_REPLY:
                        onMsgListener.onReceiveMsg(Constant.MESSAGE_REPLY, msg.getData().getString("fromIp"),(ReplyMsg) msg.obj);
                        break;
                    default:
                        break;
                }

            } catch (NoListenerException e) {
                RuntimeException exception = new RuntimeException(e);
                throw exception;
            }
        }
    };
    private boolean blackListFlag = false; //黑名单标志位

    public interface OnMsgListener {
        void onReceiveMsg(int type,String fromIP, Msg msg);
    }


    private static final String TAG = "Connection";
    private static final String SERVER_STATE = "200";

    public OnMsgListener onMsgListener;
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

//    public void setOnMsgListener(onMsgListener listener) {
//        this.onMsgListener = listener;
//    }

    public void addToBlackList(){
        if (!blackListFlag) {
            blackListFlag = true;
        }
    }

    public void initServer(OnMsgListener onMsgListener) {
        this.onMsgListener = onMsgListener;

        new Thread("listener") {

            private ObjectInputStream ois = null;
            private Socket socket = null;
            private ObjectOutputStream oos = null;

            @Override
            public void run() {

                try {
                    ServerSocket serverSocket = new ServerSocket(Constant.SERVER_PORT);
                    while (true) {
                        Message msg = new Message();


                        //读取信息
                        StringBuilder result = new StringBuilder();
                        String buffer;

                        socket = serverSocket.accept();
                        String fromIp = socket.getInetAddress().getHostAddress();
                        Bundle bundle = new Bundle();
                        bundle.putString("fromIp", fromIp);
                        msg.setData(bundle);

                        //获取 客户端信息
                        ois = new ObjectInputStream(
                                                    new BufferedInputStream(
                                                        socket.getInputStream()));
                        Msg msgRec = (Msg) ois.readObject();

                        //向客户端发送 回复消息
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        if (blackListFlag) {
                        }
                        oos.writeObject(new ReplyMsg(Constant.CONNECTION_ACCEPT,"",msgRec.getTime()));
                        oos.flush();
                        socket.shutdownOutput();

                        //根据不同的type 转成不同的Msg对象 并发送的UI线程的handler中
                        switch ((msgRec.getType())) {
                            case Constant.MESSAGE_TEXT:
                                TextMsg textMsg = (TextMsg) msgRec;
                                msg.what = Constant.MESSAGE_TEXT;//接文本消息
                                msg.obj = textMsg;
                                handler.sendMessage(msg);

                                ois.close();
                                oos.close();
                                socket.close();
                                break;
                            case Constant.MESSAGE_IMAGE:
                                ImageMsg imageMsg = (ImageMsg) msgRec;
                                msg.what = Constant.MESSAGE_IMAGE;//接Image消息
                                msg.obj = imageMsg;
                                handler.sendMessage(msg);
                                ois.close();
                                oos.close();
                                socket.close();
                                break;
                            case Constant.MESSAGE_VOICE:
                                break;
                            default:
                                break;
                        }

                    }
                } catch (IOException | ClassNotFoundException e1) {
                    e1.printStackTrace();
                }finally {
                    try {
                        if (ois == null) {
                            ois.close();
                        }
                        if (oos == null) {
                            oos.close();
                        }
                        if (socket == null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

    /**
     * 发送文本消息
     * @param host 对方IP
     * @param content 文本内容
     */
    public void sendText(String host, String content) {
        try {
            byte[] messages = content.getBytes("utf-8");
            sendMessage(host, new TextMsg(content));
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
        sendMessage(host,new ImageMsg(messages));
    }

    public void sendImage(final String host, final Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] messages = baos.toByteArray();
        sendMessage(host, new ImageMsg(messages));
    }

    public void sendImage(final String host, final Uri bitmap) {

        try {
            File file = new File(new URI(bitmap.toString()));
            InputStream inputStream = new FileInputStream(file);
            byte[] messages = ConnUtils.steamToByte(inputStream);
            sendMessage(host, new ImageMsg(messages));
        } catch (URISyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String host, final Msg msg) {

        //将消息存到数据库中
//        MsgDatabase.getInstance

        new Thread(){
            private Socket socket = null;
            private ObjectOutputStream oos = null;
            private ObjectInputStream ois = null;
            @Override
            public void run() {
                //定义消息
                Message message = new Message();
                message.what = Constant.MESSAGE_REPLY;//发消息后的（服务端返回的）回复消息

                Bundle bundle = new Bundle();
                bundle.putString("fromIp", host);
                message.setData(bundle);
                
                try {
                    //连接服务器 并设置连接超时为1秒
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host, Constant.SERVER_PORT), 1000);

                    //获取输出流
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    //向服务器发送信息
                    oos.writeObject(msg);
                    oos.flush();


                    //获取输入流
                    ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

                    //读取发来服务器信息
                    ReplyMsg replyMsg = (ReplyMsg) ois.readObject();

                    message.obj = replyMsg;
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(message);
                    //关闭各种输入输出流
                    ois.close();
                    oos.close();
                    socket.close();
                } catch (SocketTimeoutException aa) {
                    //连接超时 在UI界面显示消息
                    message.obj = new ReplyMsg(Constant.DISCONNECTION_BAD_NETWORK, "",msg.getTime());
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(message);
                } catch (IOException e) {
                    // 对方不在线
                    if (e.getLocalizedMessage().equals("Broken pipe")) {
                        message.obj = new ReplyMsg(Constant.DISCONNECTION_BROKEN_PIPE, "",msg.getTime());
                    }else {
                        message.obj = new ReplyMsg(Constant.DISCONNECTION_OUTLINE, "",msg.getTime());
                    }
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(message);
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                    if (ois!=null) {
                        ois.close();
                    }
                    if (oos != null) {
                        oos.close();
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

    private byte[] encodeByte(int type, long time, byte[] content) {
        byte[] bytes = new byte[1 + 8 + content.length];
        byte[] timeStamp = ConnUtils.long2Bytes(time);

        bytes[0] = (byte) type; //标记消息类型
        System.arraycopy(timeStamp,0,bytes,1,timeStamp.length);
        System.arraycopy(content,0,bytes,9,content.length);
        Log.i(TAG, "run: Byte[]大小为 " + bytes.length + ",其中timeStamp.length=" + timeStamp.length
        +",content.length="+content.length);
        return bytes;
    }

}
