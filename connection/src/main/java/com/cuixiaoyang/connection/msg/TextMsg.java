package com.cuixiaoyang.connection.msg;

import com.cuixiaoyang.connection.Constant;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class TextMsg extends Msg<String> implements Serializable {

    private static final long serialVersionUID = 123433L;


    public TextMsg(String text) {
        super();
        type = Constant.MESSAGE_TEXT;
        content = text;
    }

    @Override
    public String getContent() {
        return content;
    }
}
