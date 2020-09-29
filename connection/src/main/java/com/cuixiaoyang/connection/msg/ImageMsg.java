package com.cuixiaoyang.connection.msg;

import android.graphics.Bitmap;

import com.cuixiaoyang.connection.Constant;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class ImageMsg extends Msg<Bitmap> {

    public ImageMsg(Bitmap img) {
        type = Constant.MESSAGE_IMAGE;
        content = img;
    }

    @Override
    public Bitmap getContent() {
        return content;
    }
}
