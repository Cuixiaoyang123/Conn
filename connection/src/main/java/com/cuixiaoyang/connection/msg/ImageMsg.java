package com.cuixiaoyang.connection.msg;

import android.graphics.Bitmap;

import com.cuixiaoyang.connection.Constant;

import java.io.Serializable;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class ImageMsg extends Msg<byte[]> implements Serializable {
//    private static final long serialVersionUID = 1786657L;

    public ImageMsg(byte[] img) {
        super();
        type = Constant.MESSAGE_IMAGE;
        content = img;
    }
//    public ImageMsg(byte[] imgBytes) {
//        super();
//        type = Constant.MESSAGE_IMAGE;
//        content = img;
//    }

    @Override
    public byte[] getContent() {
        return content;
    }
}
