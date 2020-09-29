package com.cuixiaoyang.connection;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author
 * @date 2020/9/28.
 * GitHub：Cuixiaoyang123
 * email：1227687610@qq.com
 * description：
 */
public class Broadcast {
    public static String getBroadcast() {
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

}
