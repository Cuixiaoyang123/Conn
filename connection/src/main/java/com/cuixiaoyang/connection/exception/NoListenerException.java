package com.cuixiaoyang.connection.exception;

/**
 * @author
 * @date 2020/10/7.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class NoListenerException extends Exception {
    private int value;

    public NoListenerException() {
    }

    public NoListenerException(String message, int value) {
        super(message);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
