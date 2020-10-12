package com.cuixiaoyang.connection.msg;

import java.io.Serializable;

/**
 * @author
 * @date 2020/9/27.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class Msg<T> implements Serializable {

    protected int type;
    protected long time;
    protected T content;

    public Msg() {
        this.time = getSystemTime();
    }


    private long getSystemTime(){
        return System.currentTimeMillis();
    };

    public long getTime() {
        return time;
    }


    public int getType(){
        return type;
    };

    public T getContent(){
        return content;
    };
}
