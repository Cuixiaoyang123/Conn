package com.cuixiaoyang.connection.msg;

import com.cuixiaoyang.connection.Constant;

import java.util.Arrays;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class TextMsg extends Msg<String>{

    public TextMsg(String text) {
        type = Constant.MESSAGE_TEXT;
        content = text;
    }
    public TextMsg(byte[] bytes) {
        type = Constant.MESSAGE_TEXT;
        content = Arrays.toString(bytes);
    }

    @Override
    public String getContent() {
        return content;
    }
}
