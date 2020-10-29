package com.cuixiaoyang.imconn.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cuixiaoyang.connection.Connection;
import com.cuixiaoyang.connection.Constant;
import com.cuixiaoyang.connection.msg.TextMsg;
import com.cuixiaoyang.imconn.R;
import com.cuixiaoyang.imconn.Utils;
import com.cuixiaoyang.imconn.view.PreviewImgActivity;
import com.cuixiaoyang.imconn.viewModel.ChatViewModel;
import com.example.appdb.model.entity.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengshawn on 2017/8/10.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> listData;
    private ChatViewModel chatViewModel;
    private int myProfile, yourProfile;
    private Context context;

    public ChatAdapter(Context context, ChatViewModel chatViewModel, List<Message> listData, @DrawableRes int myProfile, @DrawableRes int yourProfile) {
        this.context = context;
        this.chatViewModel = chatViewModel;
        this.listData = listData;
        this.myProfile = myProfile;
        this.yourProfile = yourProfile;
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).getMMessageType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.i("ChatAdapter", "onCreateViewHolder: ");
        View view = null;
        switch (viewType) {
            case Constant.MESSAGE_TEXT:
                view = LayoutInflater.from(context).inflate(R.layout.item_wechat_msg_text, parent, false);
                return new TextMsgViewHolder(view);
            case Constant.MESSAGE_IMAGE:
                view = LayoutInflater.from(context).inflate(R.layout.item_wechat_msg_img, parent, false);
                return new ImgMsgViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.i("ChatAdapter", "onBindViewHolder: ");
        Message messageInfo = listData.get(position);
        Message preMsgData = null;
        if (holder instanceof TextMsgViewHolder) {
            TextMsgViewHolder textMsgViewHolder = (TextMsgViewHolder) holder;
            if (position >= 1)
                preMsgData = listData.get(position - 1);
            switch (messageInfo.getMSendOrReceive()) {
                case 1://本机接收消息
                    initTimeStamp(textMsgViewHolder, messageInfo, preMsgData);
                    textMsgViewHolder.senderLayout.setVisibility(View.GONE);
                    textMsgViewHolder.receiverLayout.setVisibility(View.VISIBLE);
                    textMsgViewHolder.receiveMsg.setText(messageInfo.getMText());
                    textMsgViewHolder.sendMsg.setOnLongClickListener((view)->{
                        chatViewModel.deleteMsg(listData.get(position).getMID(),(b) -> {});
                        listData.remove(position);
                        notifyDataSetChanged();
                        return false;
                            }
                    );
                    textMsgViewHolder.receiver_profile.setImageDrawable(context.getDrawable(yourProfile));
                    break;


                case 0://本机发送消息
                    initTimeStamp(textMsgViewHolder, messageInfo, preMsgData);
                    textMsgViewHolder.senderLayout.setVisibility(View.VISIBLE);
                    textMsgViewHolder.receiverLayout.setVisibility(View.GONE);
                    textMsgViewHolder.sendMsg.setText(messageInfo.getMText());
                    textMsgViewHolder.sendMsg.setOnLongClickListener((view)->{
                        chatViewModel.deleteMsg(listData.get(position).getMID(),(b) -> {});
                        listData.remove(position);
                        notifyDataSetChanged();
                        return false;
                            }
                    );
                    textMsgViewHolder.send_profile.setImageDrawable(context.getDrawable(myProfile));

                    switch (messageInfo.getMSendStatus()) {
                        case 0://发送消息失败
                            textMsgViewHolder.progressBar_send.hide();
                            textMsgViewHolder.send_defeat.setVisibility(View.VISIBLE);
                            break;
                        case 1://发送消息成功
                            textMsgViewHolder.progressBar_send.hide();
                            textMsgViewHolder.send_defeat.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            textMsgViewHolder.progressBar_send.show();
                            textMsgViewHolder.send_defeat.setVisibility(View.INVISIBLE);
                            break;
                    }
//                    textMsgViewHolder.send_profile.setImageResource(messageInfo.getProfile_res());
                    break;
            }
        } else if (holder instanceof ImgMsgViewHolder) {
            ImgMsgViewHolder imgMsgViewHolder = (ImgMsgViewHolder) holder;
            if (position >= 1)
                preMsgData = listData.get(position - 1);
            switch (messageInfo.getMSendOrReceive()) {
                case 1://本机接收图片
                    initTimeStamp(imgMsgViewHolder, messageInfo, preMsgData);
                    imgMsgViewHolder.senderLayout.setVisibility(View.GONE);
                    imgMsgViewHolder.receiverLayout.setVisibility(View.VISIBLE);
                    String minImgPath = messageInfo.getMPictureThumbnail();
                    Glide.with(context).load(new File(minImgPath)).into(imgMsgViewHolder.receiveImg);
//                    if (messageInfo.getSendStatus() != -1) imgMsgViewHolder.progressBar_receive.hide();
//                    if (messageInfo.getSendStatus() == 0)
                    imgMsgViewHolder.receiver_profile.setImageDrawable(context.getDrawable(yourProfile));
                    imgMsgViewHolder.receiveImg.setOnClickListener(v ->
                            context.startActivity(new Intent(context, PreviewImgActivity.class).putExtra("url", messageInfo.getMPictureThumbnail()))
                    );
                    imgMsgViewHolder.receiveImg.setOnLongClickListener(view ->{
                        chatViewModel.deleteMsg(listData.get(position).getMID(), (b) -> {});
                        listData.remove(position);
                        notifyDataSetChanged();
                        return false;
                        }

                    );
                    break;


                case 0://本机发送图片
                    initTimeStamp(imgMsgViewHolder, messageInfo, preMsgData);
                    imgMsgViewHolder.senderLayout.setVisibility(View.VISIBLE);
                    imgMsgViewHolder.receiverLayout.setVisibility(View.GONE);
                    String minImgPath1 = messageInfo.getMPictureThumbnail();
                    Glide.with(context).load(new File(minImgPath1)).into(imgMsgViewHolder.sendImg);
                    if (messageInfo.getMSendStatus() != -1) imgMsgViewHolder.progressBar_send.hide();
                    if (messageInfo.getMSendStatus() == 0) imgMsgViewHolder.send_defeat.setVisibility(View.VISIBLE);
                    imgMsgViewHolder.sendImg.setOnClickListener(view -> {
                        context.startActivity(new Intent(context, PreviewImgActivity.class).putExtra("url", messageInfo.getMPictureThumbnail()));
                    });
                    imgMsgViewHolder.sendImg.setOnLongClickListener(view ->{
                        chatViewModel.deleteMsg(listData.get(position).getMID(), (b) -> {});
                        listData.remove(position);
                        notifyDataSetChanged();
                        return false;
                        }
                    );

//                    imgMsgViewHolder.sendImg.setImageURI(Uri.parse(messageInfo.getMinImgPath()));//setImageBitmap(BitmapFactory.decodeFile(messageInfo.getMinImgPath()))
                    imgMsgViewHolder.send_profile.setImageDrawable(context.getDrawable(myProfile));
                    switch (messageInfo.getMSendStatus()) {
                        case 0://发送消息失败
                            imgMsgViewHolder.progressBar_send.hide();
                            imgMsgViewHolder.send_defeat.setVisibility(View.VISIBLE);
                            break;
                        case 1://发送消息成功
                            imgMsgViewHolder.progressBar_send.hide();
                            imgMsgViewHolder.send_defeat.setVisibility(View.INVISIBLE);
                            break;
                        default:
                            imgMsgViewHolder.progressBar_send.show();
                            imgMsgViewHolder.send_defeat.setVisibility(View.INVISIBLE);
                            break;
                    }
                    break;
            }
        }

    }


    private void initTimeStamp(TextMsgViewHolder holder, Message currentMsgData, Message preMsgData) {
        String showTime;
        if (preMsgData == null) {
            showTime = Utils.calculateShowTime(Utils.getCurrentMillisTime(), currentMsgData.getMTime());
        } else {
            showTime = Utils.calculateShowTime(currentMsgData.getMTime(), preMsgData.getMTime());
        }
        if (showTime != null) {
            holder.timeStamp.setVisibility(View.VISIBLE);
            holder.timeStamp.setText(showTime);
        } else {
            holder.timeStamp.setVisibility(View.GONE);
        }

    }

    private void initTimeStamp(ImgMsgViewHolder holder, Message currentMsgData, Message preMsgData) {
        String showTime;
        if (preMsgData == null) {
            showTime = Utils.calculateShowTime(Utils.getCurrentMillisTime(), currentMsgData.getMTime());
        } else {
            showTime = Utils.calculateShowTime(currentMsgData.getMTime(), preMsgData.getMTime());
        }
        if (showTime != null) {
            holder.timeStamp.setVisibility(View.VISIBLE);
            holder.timeStamp.setText(showTime);
        } else {
            holder.timeStamp.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    class TextMsgViewHolder extends RecyclerView.ViewHolder {

        ImageView receiver_profile, send_profile,send_defeat;
        TextView timeStamp, receiveMsg, sendMsg;
        RelativeLayout senderLayout;
        LinearLayout receiverLayout;
        ContentLoadingProgressBar progressBar_send;

        public TextMsgViewHolder(View itemView) {
            super(itemView);
            receiver_profile =  itemView.findViewById(R.id.item_wechat_msg_iv_receiver_profile);
            send_profile =  itemView.findViewById(R.id.item_wechat_msg_iv_sender_profile);
            timeStamp =  itemView.findViewById(R.id.item_wechat_msg_iv_time_stamp);
            send_defeat = itemView.findViewById(R.id.item_wechat_msg_iv_sender_defeat);
            receiveMsg =  itemView.findViewById(R.id.item_wechat_msg_tv_receiver_msg);
            sendMsg =  itemView.findViewById(R.id.item_wechat_msg_tv_sender_msg);
            progressBar_send = itemView.findViewById(R.id.progressBar_tv_sender);
            senderLayout =  itemView.findViewById(R.id.item_wechat_msg_layout_sender);
            receiverLayout =  itemView.findViewById(R.id.item_wechat_msg_layout_receiver);
        }
    }

    class ImgMsgViewHolder extends RecyclerView.ViewHolder {

        ImageView receiver_profile, send_profile, receiveImg, sendImg,send_defeat;
        TextView timeStamp;
        RelativeLayout senderLayout;
        LinearLayout receiverLayout;
        ContentLoadingProgressBar progressBar_send;

        public ImgMsgViewHolder(View itemView) {
            super(itemView);
            receiver_profile =  itemView.findViewById(R.id.item_wechat_msg_iv_receiver_profile);
            send_profile =  itemView.findViewById(R.id.item_wechat_msg_iv_sender_profile);
            timeStamp =  itemView.findViewById(R.id.item_wechat_msg_iv_time_stamp);
            receiveImg =  itemView.findViewById(R.id.item_wechat_msg_iv_receiver_msg);
            send_defeat = itemView.findViewById(R.id.item_wechat_msg_iv_sender_defeat);
            sendImg =  itemView.findViewById(R.id.item_wechat_msg_iv_sender_msg);
            progressBar_send = itemView.findViewById(R.id.progressBar_iv_sender);
            senderLayout =  itemView.findViewById(R.id.item_wechat_msg_layout_sender);
            receiverLayout =  itemView.findViewById(R.id.item_wechat_msg_layout_receiver);
        }
    }


}
