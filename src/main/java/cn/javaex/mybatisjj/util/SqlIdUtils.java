package cn.javaex.mybatisjj.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

/**
 * ID生成工具类
 *
 * @author 陈霓清
 */
public class SqlIdUtils {
    private static final long TWEPOCH = 1543376515623L;
    private static final long WORKER_ID_BITS = 10L;
    private static final long DATA_CENTER_ID_BITS = 0L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);
    private static long WORKER_ID;
    private static long DATA_CENTER_ID;
    private static long SEQUENCE = 0L;
    private static long LAST_TIMESTAMP = -1L;

    static {
        long localIp = Long.parseLong(getSiteLocalIp().replace(".", ""));
        WORKER_ID = localIp % (-1L ^ (-1L << WORKER_ID_BITS));
        DATA_CENTER_ID = 0L;
    }

    /**
     * 生成UUID
     *
     * @return
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 得到唯一数字ID（字符串形式）
     *
     * @return
     */
    public static synchronized String getLongIdStr() {
        return String.valueOf(getLongId());
    }

    /**
     * 得到唯一数字ID
     *
     * @return
     */
    public static synchronized long getLongId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < LAST_TIMESTAMP) {
            throw new RuntimeException(String.format("Clock moved backwards", LAST_TIMESTAMP - timestamp));
        }

        if (LAST_TIMESTAMP == timestamp) {
            SEQUENCE = (SEQUENCE + 1) & SEQUENCE_MASK;
            if (SEQUENCE == 0) {
                timestamp = tilNextMillis(LAST_TIMESTAMP);
            }
        } else {
            SEQUENCE = 0L;
        }

        LAST_TIMESTAMP = timestamp;

        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (DATA_CENTER_ID << DATA_CENTER_ID_SHIFT)
                | (WORKER_ID << WORKER_ID_SHIFT)
                | SEQUENCE;
    }

    private static String getSiteLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.getName() != null && networkInterface.getName().startsWith("eth")) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress.isSiteLocalAddress() && !inetAddress.isMulticastAddress()) {
                            return inetAddress.getHostAddress() == null ? "127.0.0.1" : inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("get site local ip error.");
        }

        return "127.0.0.1";
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();

        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }

        return timestamp;
    }

}
