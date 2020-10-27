package com.cuixiaoyang.connection;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.stream.Stream;

/**
 * @author
 * @date 2020/9/28.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class ConnUtils {
    public static String getBroadcastHost() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
                NetworkInterface ni = niEnum.nextElement();

                if (!ni.isLoopback()) {
                    for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                        if (interfaceAddress.getBroadcast() != null) {
                            return interfaceAddress.getBroadcast().toString().substring(1);
                        }
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static byte[] steamToByte(InputStream input) {
        byte[] b = new byte[1024];
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            while ((len = input.read(b, 0, b.length)) != -1) {
                baos.write(b, 0, len);
            }
            byte[] buffer = baos.toByteArray();
            return buffer;

        }catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

}
