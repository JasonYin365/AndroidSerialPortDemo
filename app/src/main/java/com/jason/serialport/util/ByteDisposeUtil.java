package com.jason.serialport.util;


/**
 * Create by Jason.Yin on 2018/4/16.
 * 字节处理的工具类
 */
public class ByteDisposeUtil {
    /**
     * 根据输入的参数，判断是奇数or偶数
     */
    public static int isOdd(int num) {
        return num & 0x01;
    }

    /**
     * Hex字符串转为int
     *
     * @param inHex
     * @return
     */
    public static int hexToInt(String inHex) {
        return Integer.parseInt(inHex, 16);
    }

    /**
     * 1字节转2个Hex字符
     *
     * @param inByte
     * @return
     */
    public static String byte2Hex(Byte inByte) {
        //%02x     格式控制: 以十六进制输出,2为指定的输出字段的宽度.如果位数小于2,则左端补0
        return String.format("%02x", inByte).toUpperCase();
    }

    /**
     * Hex字符串转byte
     *
     * @param inHex
     * @return
     */
    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    /**
     * 字节数组转转hex字符串
     *
     * @param inBytArr
     * @return
     */
    public static String byteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        int j = inBytArr.length;
        for (int i = 0; i < j; i++) {
            strBuilder.append(byte2Hex(inBytArr[i]));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    /**
     * 字节数组转转hex字符串，可选长度
     *
     * @param inBytArr
     * @param offset
     * @param byteCount
     * @return
     */
    public static String ByteArrToHex(byte[] inBytArr, int offset, int byteCount) {
        StringBuilder strBuilder = new StringBuilder();
        int j = byteCount;
        for (int i = offset; i < j; i++) {
            strBuilder.append(byte2Hex(inBytArr[i]));
        }
        return strBuilder.toString();
    }

    /**
     * hex字符串转字节数组
     *
     * @param inHex
     * @return
     */
    public static byte[] hexToByteArr(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (isOdd(hexlen) == 1) {//odd
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {//偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }
}
