package com.cuixiaoyang.imconn;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author
 * @date 2020/10/13.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class Utils {
    //保存原图
    public static String saveBitmap(Bitmap bm, Context mContext) {
        //指定我们想要存储文件的地址
        String name = getRandomString(16);
        String TargetPath = mContext.getFilesDir() + "/images/";
        //判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);

            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture is save to your phone! path = "+saveFile.getPath());
                return saveFile.getPath();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static String findDeviceId(String fromIp) {
        return fromIp;
    }

    //保存缩略图
    public static String saveMiniBitmap(Bitmap bm, Context mContext) {
        //指定我们想要存储文件的地址
        String name = getRandomString(16);
        String TargetPath = mContext.getFilesDir() + "/images/";
        //判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);

            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 10, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture is save to your phone! path = "+saveFile.getPath());
                return saveFile.getPath();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    static boolean fileIsExist(String fileName)
    {
        //传入指定的路径，然后判断路径是否存在
        File file=new File(fileName);
        if (file.exists())
            return true;
        else{
            //file.mkdirs() 创建文件夹的意思
            return file.mkdirs();
        }
    }

    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getStatusBarHeight(Context context) {
        Resources res = context.getResources();
        int resId = res.getIdentifier("status_bar_height", "dimen", "android");
        return res.getDimensionPixelSize(resId);

        //也可以通过反射
        /**
         Class<?> c = null;
         Object obj = null;
         Field field = null;
         int x = 0, statusBarHeight = 0;
         try {
         c = Class.forName("com.android.internal.R$dimen");
         obj = c.newInstance();
         field = c.getField("status_bar_height");
         x = Integer.parseInt(field.get(obj).toString());
         statusBarHeight = context.getResources().getDimensionPixelSize(x);
         } catch (Exception e1) {
         e1.printStackTrace();
         }
         return statusBarHeight;
         */
    }

    /**
     * 用户友好时间显示
     *
     * @param nowTime 现在时间毫秒
     * @param preTime 之前时间毫秒
     * @return 符合用户习惯的时间显示
     */
    public static String calculateShowTime(long nowTime, long preTime) {
        if (nowTime <= 0 || preTime <= 0)
            return null;
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd-HH-mm-E");
        String now = format.format(new Date(nowTime));
        String pre = format.format(new Date(preTime));
        String[] nowTimeArr = now.split("-");
        String[] preTimeArr = pre.split("-");
        //当天以内,年月日相同，超过五分钟显示
        if (nowTimeArr[0].equals(preTimeArr[0]) && nowTimeArr[1].equals(preTimeArr[1]) && nowTimeArr[2].equals(preTimeArr[2]) && nowTime - preTime > 5 * 60000) {
            return preTimeArr[3] + ":" + preTimeArr[4];
        }
        //一周以内
        else if (Integer.valueOf(nowTimeArr[2]) - Integer.valueOf(preTimeArr[2]) > 0 && nowTime - preTime < 7 * 24 * 60 * 60 * 1000) {

            if (Integer.valueOf(nowTimeArr[2]) - Integer.valueOf(preTimeArr[2]) == 1)
                return "昨天 " + preTimeArr[3] + ":" + preTimeArr[4];
            else
                return preTimeArr[5] + " " + preTimeArr[3] + ":" + preTimeArr[4];
        }
        //一周以上
        else if (nowTime - preTime > 7 * 24 * 60 * 60 * 1000) {
            return preTimeArr[0] + "年" + preTimeArr[1] + "月" + preTimeArr[2] + "日" + " " + preTimeArr[3] + ":" + preTimeArr[4];
        }
        return null;
    }


    public static long getCurrentMillisTime() {
        return System.currentTimeMillis();
    }



    @TargetApi(19)
    private static void setTransparentStatus(boolean on, Activity activity) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams params = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on)
            params.flags |= bits;
        else
            params.flags &= ~bits;

        win.setAttributes(params);
    }



}
