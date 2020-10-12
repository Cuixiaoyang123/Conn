package com.cuixiaoyang.connection;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.cuixiaoyang.connection.exception.NoListenerException;
import com.cuixiaoyang.connection.msg.ImageMsg;
import com.cuixiaoyang.connection.msg.Msg;
import com.cuixiaoyang.connection.msg.ReplyMsg;
import com.cuixiaoyang.connection.msg.TextMsg;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
                        onMsgListener.onReceiveMsg(Constant.MESSAGE_TEXT, (TextMsg) msg.obj);
                        break;
                    case Constant.MESSAGE_IMAGE:
                        onMsgListener.onReceiveMsg(Constant.MESSAGE_IMAGE, (ImageMsg) msg.obj);
                        break;
                    case Constant.MESSAGE_REPLY:
                        onMsgListener.onReceiveMsg(Constant.MESSAGE_REPLY, new ReplyMsg((int) msg.obj,String.valueOf((int) msg.obj)));
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

            } catch (NoListenerException e) {
//                e.printStackTrace();
                RuntimeException exception = new RuntimeException(e);
                throw exception;
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

                        ObjectOutputStream oos;

                        //读取信息
                        StringBuilder result = new StringBuilder();
                        String buffer;

                        Socket socket = serverSocket.accept();
                        String fromIp = socket.getInetAddress().getHostAddress();

                        //向client发送 确认消息
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(new ReplyMsg(Constant.CONNECTION_ACCEPT,""));
                        oos.flush();
                        socket.shutdownOutput();

                        //获取 客户端信息
                        ObjectInputStream ois = new ObjectInputStream(
                                                    new BufferedInputStream(
                                                        socket.getInputStream()));
                        Msg msgRec = (Msg) ois.readObject();
//                        ois.read(messageType,0,messageType.length);
//                        Log.i(TAG, "run: messageType = " + Arrays.toString(messageType));

                        switch ((msgRec.getType())) {
                            case Constant.MESSAGE_TEXT:
                                //第一种方式：获取String类型的消息
//                                BufferedReader bff = new BufferedReader(new InputStreamReader
//                                        (ois));
//                                while ((buffer = bff.readLine()) != null) {
//                                    result.append(buffer);
//                                    Log.i(TAG, "run: result= " + buffer);
//                                }
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

//                                Bitmap bitmap = BitmapFactory.decodeStream(ois);
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

    public void sendMessage(final String host, final Msg msg) {

        new Thread(){
            @Override
            public void run() {
                //定义消息
                Message message = new Message();
                message.what = Constant.MESSAGE_REPLY;//发消息后的（服务端返回的）回复消息

                Socket socket = null;
                //获取输出流
                ObjectOutputStream oos = null;
                //获取输入流
                ObjectInputStream ois = null;
                try {
                    //连接服务器 并设置连接超时为5秒
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(host, Constant.SERVER_PORT), 1000);

                    //获取输出流
                    oos = new ObjectOutputStream(socket.getOutputStream());

                    //向服务器发送信息
//                    byte[] bytes = encodeByte(type, host, content);
                    oos.writeObject(msg);
//                    if (bytes.length > 340 * 1024) {
//                        //多次写入
//                        int count = (int) Math.ceil(bytes.length / (340.0 * 1024));
//                        for (int i = 0; i < count; i++) {
//                            if (i == count - 1) {
//                                oos.write(bytes, i * 304 * 1024, bytes.length - i * 304 * 1024);
//                            }
//                            oos.write(bytes, i * 304 * 1024, 304 * 1024);
//                        }
//                    } else {
//                        oos.write(bytes);
//                    }
                    oos.flush();

                    //获取输入流
                    ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

                    //读取发来服务器信息
                     ReplyMsg replyMsg = (ReplyMsg) ois.readObject();
//                    StringBuilder result = new StringBuilder();
//                    String buffer = "";
//                    while ((buffer = ois.readLine()) != null) {
//                        result.append(buffer);
//                        Log.i("cxy", "run: result= " + buffer);
//                    }

                    message.obj = replyMsg.getReplyCode();
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(message);
                    //关闭各种输入输出流
                    ois.close();
                    oos.close();
                    socket.close();
                } catch (SocketTimeoutException aa) {
                    //连接超时 在UI界面显示消息
                    message.obj =  Constant.DISCONNECTION_BAD_NETWORK;
                    //发送消息 修改UI线程中的组件
                    handler.sendMessage(message);
                } catch (IOException e) {
                    // 对方不在线
                    if (e.getLocalizedMessage().equals("Broken pipe")) {
                        message.obj =  Constant.DISCONNECTION_BROKEN_PIPE;
                    }else {
                        message.obj =  Constant.DISCONNECTION_OUTLINE;
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
