package com.cuixiaoyang.imconn;

/**
 * @author
 * @date 2020/10/23.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * 键盘管理类
 * Created by fanxudong on 2017/7/11.
 */

public class KeyboardManager implements ViewTreeObserver.OnGlobalLayoutListener {

    private View mContentView;
    private int mOriginHeight;
    private int mUnChangeHeight;
    private KeyBoardChangeListener mKeyBoardListen;

    public static KeyboardManager getInstance(Activity activity){
        return new KeyboardManager(activity);
    }

    public interface KeyBoardChangeListener {

        void onKeyboardChange(boolean isShow, int keyboardHeight);

    }

    public void addOnKeyBoardChangeListener(KeyBoardChangeListener keyBoardChangeListen) {
        this.mKeyBoardListen = keyBoardChangeListen;
    }
    public void removeOnKeyBoardChangeListener() {
        this.mKeyBoardListen = null;
    }

    public KeyboardManager(Activity activity) {
        if (activity == null) {
            return;
        }
        mContentView = findContentView(activity);
        if (mContentView != null) {
            addContentTreeObserver();
        }
    }

    private View findContentView(Activity contextObj) {
        return contextObj.findViewById(android.R.id.content);
    }

    private void addContentTreeObserver() {
        mContentView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        int currentHeight = mContentView.getHeight();
        if (currentHeight == 0) {
            return;
        }
        boolean isChange = false;
        if (mUnChangeHeight == 0) {
            mUnChangeHeight = currentHeight;
            mOriginHeight = currentHeight;
        } else {
            if (mUnChangeHeight != currentHeight) {
                isChange = true;
                mUnChangeHeight = currentHeight;
            } else {
                isChange = false;
            }
        }
        if (isChange) {
            boolean isShow;
            int keyboardHeight = 0;
            if (mOriginHeight == currentHeight) {
                isShow = false;
            } else {
                keyboardHeight = mOriginHeight - currentHeight;
                isShow = true;
            }

            if (mKeyBoardListen != null) {
                mKeyBoardListen.onKeyboardChange(isShow, keyboardHeight);
            }
        }
    }

    public void destroy() {
        if (mContentView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        }
    }

}