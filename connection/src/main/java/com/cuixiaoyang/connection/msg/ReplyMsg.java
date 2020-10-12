package com.cuixiaoyang.connection.msg;

import com.cuixiaoyang.connection.Constant;

import java.io.Serializable;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class ReplyMsg extends Msg<String> implements Serializable {
//    private static final long serialVersionUID = 156768L;


    private int replyCode;

    public ReplyMsg(int replyCode,String text) {
        type = Constant.MESSAGE_REPLY;
        this.replyCode = replyCode;
        this.content = text;
    }

    public int getReplyCode() {
        return replyCode;
    }

    @Override
    public String getContent() {
        return content;
    }
}
