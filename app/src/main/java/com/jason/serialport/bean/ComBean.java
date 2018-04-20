package com.jason.serialport.bean;


import java.text.SimpleDateFormat;

/**
 * Create by Jason.Yin on 2018/4/16.
 */
public class ComBean {
    public byte[] bRec = null;
    public String sRecTime = "";
    public String sComPort = "";

    public ComBean(String port, byte[] buffer, int size) {
        sComPort = port;
        bRec = new byte[size];
        for (int i = 0; i < size; i++) {
            bRec[i] = buffer[i];
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        sRecTime = dateFormat.format(System.currentTimeMillis());
    }
}
