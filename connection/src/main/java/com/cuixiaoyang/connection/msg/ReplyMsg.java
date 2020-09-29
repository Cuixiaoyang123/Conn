package com.cuixiaoyang.connection.msg;

import com.cuixiaoyang.connection.Constant;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class ReplyMsg extends Msg<String> {

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
