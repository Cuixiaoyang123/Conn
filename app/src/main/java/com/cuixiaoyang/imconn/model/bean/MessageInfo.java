package com.cuixiaoyang.imconn.model.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
@Entity
public class MessageInfo {

    @PrimaryKey(autoGenerate = true)
    private int msgId;
    private String fromDeviceId;
    private String deviceId;
    private int sendOrReceive;
    private int type;
    private long time;
    private String text;
    private String minImgPath;
    private String ImgPath;
    private int readStatus;
    private int sendStatus = -1;

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getFromDeviceId() {
        return fromDeviceId;
    }

    public void setFromDeviceId(String fromDeviceId) {
        this.fromDeviceId = fromDeviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getSendOrReceive() {
        return sendOrReceive;
    }

    public void setSendOrReceive(int sendOrReceive) {
        this.sendOrReceive = sendOrReceive;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMinImgPath() {
        return minImgPath;
    }

    public void setMinImgPath(String minImgPath) {
        this.minImgPath = minImgPath;
    }

    public String getImgPath() {
        return ImgPath;
    }

    public void setImgPath(String imgPath) {
        ImgPath = imgPath;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public int getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(int sendStatus) {
        this.sendStatus = sendStatus;
    }
}
