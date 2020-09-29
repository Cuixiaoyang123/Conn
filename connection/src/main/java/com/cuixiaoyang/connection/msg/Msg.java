package com.cuixiaoyang.connection.msg;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public  abstract class Msg<T> {
    protected int type;
    protected T content;

    public int getType(){
        return type;
    };

    public abstract <T> T getContent();
}
