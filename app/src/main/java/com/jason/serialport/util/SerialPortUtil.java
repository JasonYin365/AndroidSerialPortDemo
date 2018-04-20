package com.jason.serialport.util;

import android.serialport.api.SerialPort;

import com.jason.serialport.bean.ComBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Create by Jason.Yin on 2018/4/20.
 * 串口辅助工具类
 */
public abstract class SerialPortUtil {

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private int mBaudRate = 9600;
    private String mPortPath = "/dev/ttyS1";
    private int mDelay = 500;
    private byte[] mOutPutData = new byte[]{0x30};
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private boolean isOpen = false;

    public SerialPortUtil(String port, int baudRate) {
        mBaudRate = baudRate;
        mPortPath = port;
    }

    public SerialPortUtil() {
        this("/dev/ttyS1", 9600);
    }

    public void open() throws IOException,SecurityException {
        mSerialPort = new SerialPort(new File(mPortPath), mBaudRate, 0);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();
        mReadThread = new ReadThread();
        mReadThread.start();
        mSendThread = new SendThread();
        mSendThread.setSuspendFlag();
        mSendThread.start();
        isOpen = true;
    }

    public void close() {
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        if (mSendThread != null) {
            mSendThread.interrupt();
        }
        isOpen = false;
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                try {
                    if (mInputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[512];
                    int size = mInputStream.read(buffer);
                    if (size > 0) {
                        ComBean comReceiveData = new ComBean(mPortPath, buffer, size);
                        onDataReceived(comReceiveData);
                    }
                    Thread.sleep(50);
                } catch (Throwable e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    protected abstract void onDataReceived(ComBean comRecData);

    private class SendThread extends Thread {
        public boolean suspendFlag = true; //控制线程的执行

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                synchronized (this) {
                    while (suspendFlag) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                send(getOutPutData());
                try {
                    Thread.sleep(mDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }
    }

    private byte[] getOutPutData() {
        return mOutPutData;
    }

    public void setOutPutData(byte[] outPutData) {
        mOutPutData = outPutData;
    }

    public void send(byte[] outArray) {
        try {
            mOutputStream.write(outArray);
        } catch (IOException e) {

        }
    }

    public void sendHex(String strHex) {
        send(ByteDisposeUtil.hexToByteArr(strHex));
    }

    public void sendStr(String str) {
        send(str.getBytes());
    }

    public int getBaudRate() {
        return mBaudRate;
    }

    public boolean setBaudRate(int baudRate) {
        if (isOpen) {
            return false;
        } else {
            mBaudRate = baudRate;
            return true;
        }
    }

    public boolean setBaudRate(String strBaudRate){
        return setBaudRate(Integer.parseInt(strBaudRate));
    }
    public boolean setPort(String strPort) {
        if (isOpen) {
            return false;
        } else {
            mPortPath = strPort;
            return true;
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setStrOutPutData(String str) {
        mOutPutData = str.getBytes();
    }

    public void setHexOutPutData(String strHex) {
        mOutPutData = ByteDisposeUtil.hexToByteArr(strHex);
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    public void startSend() {
        if (mSendThread != null) {
            mSendThread.setResume();
        }
    }

    public void stopSend() {
        if (mSendThread != null) {
            mSendThread.setSuspendFlag();
        }
    }
}
