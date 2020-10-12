package com.cuixiaoyang.imconn;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuixiaoyang.connection.Broadcast;
import com.cuixiaoyang.connection.Connection;
import com.cuixiaoyang.connection.Constant;
import com.cuixiaoyang.connection.msg.Msg;
import com.cuixiaoyang.connection.msg.TextMsg;

public class MainActivity extends AppCompatActivity {


    private TextView result,result2, tv_ip;
    private Button send_text,send_img,send_online,send_outline;
    private EditText input;
    private ImageView iv_receive;
    private int count = 0;

    @SuppressLint("HandlerLeak")
    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case Constant.MESSAGE_TEXT:
//                    result2.setText("client:" + msg.obj + "\n");
//                    break;
//                case Constant.MESSAGE_IMAGE:
//                    iv_receive.setImageBitmap((Bitmap) msg.obj);
//            }
            if (msg.what == 1) {
//                result.append("server:" + msg.obj + "\n");
                result.setText("server:" + msg.obj + "\n");
            }else if (msg.what == 21){
//                result2.append("client:" + msg.obj + "\n");

            }
        }

    };
    //10.11.15.223  10.11.15.243
    //  mi "192.168.43.243"  lenovo "192.168.43.168";
    private String sendToIP = "192.168.43.243";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建server

        Connection.getInstance().initServer();
        setContentView(R.layout.activity_main);
        result = (TextView) findViewById(R.id.result);
        result2 = findViewById(R.id.result1);
        iv_receive = findViewById(R.id.iv_receive);
        tv_ip = findViewById(R.id.tv_ip);
        tv_ip.setText("本机IP:"+getlocalip());
        send_text = findViewById(R.id.send_text);
        send_img = findViewById(R.id.send_img);
        send_online = findViewById(R.id.send_online);
        send_outline = findViewById(R.id.send_outline);
        input = (EditText) findViewById(R.id.input);
        Connection.getInstance().setOnMsgListener(new Connection.onMsgListener() {
            @Override
            public void onReceiveMsg(int type, Msg msg) {
                switch (type) {
                    case Constant.MESSAGE_TEXT:
                        result2.setText("client:" + msg.getContent());
                        break;
                    case Constant.MESSAGE_IMAGE:
                        byte[] content = (byte[]) msg.getContent();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
                        iv_receive.setImageBitmap(bitmap);
                        break;
                    case Constant.MESSAGE_REPLY:
                        result.setText("client:" + msg.getContent());
                        break;
//                    case Constant.BROADCAST_ONLINE:
//                        result2.setText("上线IP:"+msg.getContent());
//                        break;
//                    case Constant.BROADCAST_OUTLINE:
//                        result2.setText("下线IP:"+msg.getContent());
//                        break;
                    default:
                        break;
                }
            }
        });
        send_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String inputContent = input.getText().toString();
//                result.append("client:" + inputContent + "\n");
                //启动线程 向服务器发送和接收信息
                //10.11.15.223  10.11.15.242  10.11.15.244
                //192.168.43.243              192.168.43.168
//                byte[] messages = inputContent.getBytes("utf-8");
                Connection.getInstance().sendText(sendToIP,inputContent);

            }
        });
        send_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                final String inputContent = input.getText().toString();
                //10.11.15.223  10.11.15.242
                //将图片bitmap转换成字节数组
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        (count % 2 == 0) ? R.mipmap.table : R.mipmap.hua);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                byte[] messages = baos.toByteArray();
//                byte[] messages = new byte[1024*1024];
                Connection.getInstance().sendImage(sendToIP,bitmap,10);
            }
        });

        send_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Connection.getInstance().sendInnerBroadcast(getlocalip(), Constant.BROADCAST_ONLINE);
            }
        });

    }



    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        //  Log.d(Tag, "int ip "+ipAddress);
        if (ipAddress == 0) return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }
}