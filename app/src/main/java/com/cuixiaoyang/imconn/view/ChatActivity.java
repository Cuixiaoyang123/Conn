package com.cuixiaoyang.imconn.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cuixiaoyang.connection.Connection;
import com.cuixiaoyang.connection.Constant;
import com.cuixiaoyang.connection.msg.ImageMsg;
import com.cuixiaoyang.connection.msg.Msg;
import com.cuixiaoyang.connection.msg.ReplyMsg;
import com.cuixiaoyang.connection.msg.TextMsg;
import com.cuixiaoyang.imconn.KeyboardManager;
import com.cuixiaoyang.imconn.R;
import com.cuixiaoyang.imconn.Utils;
import com.cuixiaoyang.imconn.adapter.ChatAdapter;
import com.cuixiaoyang.imconn.model.bean.MessageInfo;
import com.cuixiaoyang.imconn.viewModel.ChatViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ChatActivity";

    private static final int REQUEST_GALLERY = 101;
    private ChatViewModel chatViewModel;
    private TextView result, result2, tv_ip;
    private Button send_text, send_img, send_online, send_outline;
    private EditText input;
    private ImageView iv_receive;
    private Context mContext;
    private List<MessageInfo> allMsgs = new ArrayList<>();
    private int count = 0;
    //执行线程池 数据库的访问需要在子线程执行
    private ExecutorService mThreadPool = Executors.newSingleThreadExecutor();

    //10.11.15.223  10.11.15.243
//      mi "192.168.43.243"  lenovo "192.168.43.168";
//    private String deviceId = "10.11.15.223";
//    private int deviceIdProfile = R.drawable.your;
//    private int myProfile = R.drawable.my;
//    private String sendToIP = "10.11.15.223";

    private String deviceId = "10.11.15.243";
    private int deviceIdProfile = R.drawable.my;
    private int myProfile = R.drawable.your;
    private String sendToIP = "10.11.15.243";

    private ChatAdapter adapter;
    private RecyclerView rv;
    private EditText et_msg;
    private View include_select;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        //实例化viewModel
        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        chatViewModel.messageList.observe(this, list -> {
            allMsgs.clear();
             allMsgs.addAll(list);
            adapter.notifyDataSetChanged();
            rv.scrollToPosition(allMsgs.size() - 1);
        });
        //创建server
        Connection.getInstance().initServer(new Connection.OnMsgListener() {
            @Override
            public void onReceiveMsg(int type,String fromIp, Msg msg) {
                String deviceId = findDeviceId(fromIp);
                switch (type) {
                    case Constant.MESSAGE_TEXT:
                        chatViewModel.insertMsg(makeMsgFromReceText(deviceId,msg),deviceId,()->{
                        });
                        break;
                    case Constant.MESSAGE_IMAGE:
                        byte[] content = (byte[]) msg.getContent();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(content, 0, content.length);
                        String path = Utils.saveBitmap(bitmap, mContext);
                        String miniPath = Utils.saveMiniBitmap(bitmap, mContext);
                        chatViewModel.insertMsg(makeMsgFromReceImg(deviceId,path,miniPath,msg),deviceId,()->{

                        });
                        break;
                    case Constant.MESSAGE_REPLY:
                        long time = msg.getTime();
                        int replyCode = ((ReplyMsg) msg).getReplyCode();
                        if (replyCode == 20) {
                            chatViewModel.changeSentStatue(time, 1, deviceId, () -> {
                            });
                        }else {
                            chatViewModel.changeSentStatue(time, 0, deviceId, () -> {
                            });
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        setContentView(R.layout.activity_wechat_chat);
        initComponents();
        chatViewModel.getAllMsgWithSomebody(sendToIP);

    }

    private String findDeviceId(String fromIp) {
        return fromIp;
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

    private MessageInfo makeMsgFromSendText(String deviceId, Msg msg) {
        MessageInfo info = new MessageInfo();
        info.setTime(msg.getTime());
        info.setText((String) msg.getContent());
        info.setType(msg.getType());
        info.setDeviceId(deviceId);
        info.setSendOrReceive(0);
        info.setReadStatus(1);
        return info;
    }
    private MessageInfo makeMsgFromSendImg(String deviceId, String path,String miniPath,Msg msg) {
        MessageInfo info = new MessageInfo();
        info.setTime(msg.getTime());
        info.setMinImgPath(miniPath);
        info.setImgPath(path);
        info.setType(msg.getType());
        info.setDeviceId(deviceId);
        info.setSendOrReceive(0);
        info.setReadStatus(1);
        return info;
    }

    private MessageInfo makeMsgFromReceText(String deviceId, Msg msg){
        MessageInfo info = new MessageInfo();
        info.setTime(msg.getTime());
        info.setText((String) msg.getContent());
        info.setType(msg.getType());
        info.setDeviceId(deviceId);
        info.setSendOrReceive(1);
        info.setReadStatus(0);
        return info;
    }

    private MessageInfo makeMsgFromReceImg(String deviceId, String path,String miniPath, Msg msg){
        MessageInfo info = new MessageInfo();
        info.setTime(msg.getTime());
        info.setType(msg.getType());
        info.setImgPath(path);
        info.setMinImgPath(miniPath);
        info.setDeviceId(deviceId);
        info.setSendOrReceive(1);
        info.setReadStatus(0);
        return info;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initComponents() {
        SwipeRefreshLayout refreshLayout = findViewById(R.id.refresh_layout);
        include_select = findViewById(R.id.more_select);
        ImageView iv_back = findViewById(R.id.activity_wechat_chat_back);
        TextView tv_user = findViewById(R.id.activity_wechat_chat_tv_name);
        ImageView iv_voice = findViewById(R.id.activity_wechat_chat_iv_voice);
        ImageView iv_photo = findViewById(R.id.chat_more_img);
        ImageView iv_camera = findViewById(R.id.chat_more_camera);
        et_msg = findViewById(R.id.activity_wechat_chat_et_msg);
        ImageView iv_emoji = findViewById(R.id.activity_wechat_chat_iv_emoji);
        ImageView iv_add = findViewById(R.id.activity_wechat_chat_iv_add);
        Button btn_send = findViewById(R.id.activity_wechat_chat_btn_send);
        rv = findViewById(R.id.activity_wechat_chat_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this, allMsgs,myProfile,deviceIdProfile);
        rv.setAdapter(adapter);

        iv_back.setOnClickListener((v) -> finish());
        btn_send.startAnimation(getVisibleAnim(false, btn_send));
        btn_send.setVisibility(View.GONE);
        tv_user.setText(deviceId);

        rv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                rv.setFocusable(true);
                rv.setFocusableInTouchMode(true);
                rv.requestFocus();
                include_select.setVisibility(View.GONE);
                hideKeyboard();
                return false;
            }
        });


        et_msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("tag", "onTextChanged --- start -> " + start + " , count ->" + count + "，before ->" + before);
                if (start == 0 && count > 0) {
                    btn_send.startAnimation(getVisibleAnim(true, btn_send));
                    btn_send.setVisibility(View.VISIBLE);
                    iv_add.setVisibility(View.GONE);
                }
                if (start == 0 && count == 0) {
                    //btn_send.startAnimation(getVisibleAnim(false, btn_send));
                    btn_send.setVisibility(View.GONE);
                    iv_add.startAnimation(getVisibleAnim(true, iv_add));
                    iv_add.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
//        et_msg.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
////                if (!hasFocus) {
////                    include_select.setVisibility(View.GONE);
////                    hideKeyboard();
////                }
//            }
//        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                Toast.makeText(mContext, "refreshing ...", Toast.LENGTH_SHORT).show();

                refreshLayout.setRefreshing(false);
            }
        });

        iv_photo.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        iv_voice.setOnClickListener(this);
        iv_emoji.setOnClickListener(this);
        iv_add.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode ) {
            case REQUEST_GALLERY:
                if (data == null) break;
                Log.i(TAG, "onActivityResult: data.getData()="+data.getData());
                sendImg(data.getData());
                break;
        }
    }

    /**
     * 应用层封装的发送图片的方法：
     * 1.将图片封装成ImageMsg
     * 2.存储到本地room数据库
     * 3.调用依赖底层IM通信模块  发送此ImageMsg
     * @param imgUri 系统图库返回的图片Uri
     */
    private void sendImg(Uri imgUri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] messages = baos.toByteArray();
        ImageMsg imageMsg = new ImageMsg(messages);
        Connection.getInstance().sendMessage(sendToIP,imageMsg);
        String deviceId = findDeviceId(sendToIP);
        String path = Utils.saveBitmap(bitmap, mContext);
        String miniPath = Utils.saveMiniBitmap(bitmap, mContext);
        MessageInfo info = makeMsgFromSendImg(deviceId,path,miniPath, imageMsg);
        chatViewModel.insertMsg(info, deviceId, ()->{
        });
    }

    private void sendImg(Drawable drawable) {
        BitmapDrawable bp = (BitmapDrawable) drawable;
        Bitmap bitmap = bp.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] messages = baos.toByteArray();
        ImageMsg imageMsg = new ImageMsg(messages);
        Connection.getInstance().sendMessage(sendToIP,imageMsg);
        String deviceId = findDeviceId(sendToIP);
        String path = Utils.saveBitmap(bitmap, mContext);
        String miniPath = Utils.saveMiniBitmap(bitmap, mContext);
        MessageInfo info = makeMsgFromSendImg(deviceId,path,miniPath, imageMsg);
        chatViewModel.insertMsg(info, deviceId, ()->{
        });
    }

    /**
     *  应用层封装的发送文本的方法：
     * 1.将文本封装成TextMsg
     * 2.存储到本地room数据库
     * 3.调用依赖底层IM通信模块  发送此TextMsg
     * @param text
     */
    private void sendText(String text) {
        TextMsg textMsg = new TextMsg(text);
        Connection.getInstance().sendMessage(sendToIP,textMsg);
        String deviceId = findDeviceId(sendToIP);
        MessageInfo info = makeMsgFromSendText(deviceId, textMsg);
        chatViewModel.insertMsg(info, deviceId, ()->{
        });
    }

    private void showKeyboard() {
        InputMethodManager inputManager =
                (InputMethodManager) et_msg.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        et_msg.requestFocus();
        inputManager.showSoftInput(et_msg, 0);
    }

    private void hideKeyboard() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        et_msg.clearFocus();
        imm.hideSoftInputFromWindow(et_msg.getWindowToken(), 0);
    }


    private Animation getVisibleAnim(boolean show, View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int y = view.getMeasuredHeight() / 4;
        int x = view.getMeasuredWidth() / 4;
        if (show) {
            ScaleAnimation showAnim = new ScaleAnimation(0.01f, 1f, 0.01f, 1f, x, y);
            showAnim.setDuration(200);
            return showAnim;
        } else {
            ScaleAnimation hiddenAnim = new ScaleAnimation(1f, 0.01f, 1f, 0.01f, x, y);
            hiddenAnim.setDuration(200);
            return hiddenAnim;
        }
    }

    private Animation getIncludeVisibleAnim(boolean show) {
        if (show) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
            alphaAnimation.setDuration(800);
            return alphaAnimation;
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
            alphaAnimation.setDuration(800);
            return alphaAnimation;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_wechat_chat_iv_add:
                int visi = include_select.getVisibility();
                if (visi == View.GONE){
                    hideKeyboard();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            include_select.startAnimation(getIncludeVisibleAnim(true));
                            include_select.setVisibility(View.VISIBLE);
                        }
                    }, 100);

                } else if (visi == View.VISIBLE) {
                    include_select.startAnimation(getIncludeVisibleAnim(false));
                    include_select.setVisibility(View.GONE);
                    showKeyboard();
                }
                break;
            case R.id.activity_wechat_chat_iv_emoji:
                Toast.makeText(mContext, "表情包待开发...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.chat_more_camera:
                Toast.makeText(mContext, "拍照待开发...", Toast.LENGTH_SHORT).show();
                break;
            case R.id.chat_more_img:
                Intent toGallery = new Intent(Intent.ACTION_GET_CONTENT);
                toGallery.setType("image/*");
                toGallery.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(toGallery, REQUEST_GALLERY);
                break;
            case R.id.activity_wechat_chat_iv_voice:
                Toast.makeText(mContext, "正在完善中...", Toast.LENGTH_SHORT).show();
                break;

            case R.id.activity_wechat_chat_btn_send:
                String text = et_msg.getText().toString();
                sendText(text);
                et_msg.setText("");
                break;
        }
    }

    public void onResendImg(View view) {
        Drawable drawable = ((ImageView) ((ViewGroup) view.getParent()).getChildAt(2)).getDrawable();
        sendImg(drawable);
    }

    public void onResendText(View view) {
        String text = ((TextView)((ViewGroup) view.getParent()).getChildAt(2)).getText().toString();
        sendText(text);
    }

}