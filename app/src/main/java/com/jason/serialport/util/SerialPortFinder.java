package com.jason.serialport.util;

import android.nfc.Tag;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

/**
 * Create by Jason.Yin on 2018/4/17.
 * 串口工具类
 */
public class SerialPortFinder {

    private static final String TAG = "SerialPortFinder";

    public class SerialPortDriver {
        private String mSerialPortDriverName;
        private String mSerialPortDriverRoot;

        public SerialPortDriver(String name, String root) {
            mSerialPortDriverName = name;
            mSerialPortDriverRoot = root;
        }

        Vector<File> mDevices = null;

        public Vector<File> getDevices() {
            if (mDevices == null) {
                mDevices = new Vector<>();
                File dev = new File("/dev");
                File[] files = dev.listFiles();
                for (int i = 0; i < files.length; i++) {
                    //遍历dev目录，根据getDrivers找到那些设备是serial，然后根据serial对应的name找到对应的串口节点
                    //eg:/dev/smd   在dev目录遍历，需找匹配'/dev/smd'开头的节点
                    if (files[i].getAbsolutePath().startsWith(mSerialPortDriverRoot)) {
                        Log.d(TAG, "Found devices :" + files[i]);
                        mDevices.add(files[i]);
                    }
                }
            }
            return mDevices;
        }

        public String getName() {
            return mSerialPortDriverName;
        }
    }

    private Vector<SerialPortDriver> mComDrivers = null;

    Vector<SerialPortDriver> getDrivers() throws IOException {
        if (mComDrivers == null) {
            mComDrivers = new Vector<>();
            LineNumberReader lineNumberReader = new LineNumberReader(new FileReader("/proc/tty/drivers"));
            String str;
            while ((str = lineNumberReader.readLine()) != null) {
                // Since driver name may contain spaces, we do not extract driver name with split()

                /*/dev/tty             /dev/tty        5       0 system:/dev/tty
                dev/console         /dev/console    5       1 system:console
                dev/ptmx            /dev/ptmx       5       2 system
                rfcomm               /dev/rfcomm   216 0-255 serial
                g_serial             /dev/ttyGS    234 0-3 serial
                usbserial            /dev/ttyUSB   188 0-511 serial
                acm                  /dev/ttyACM   166 0-31 serial
                smd_tty_driver       /dev/smd      244 0-36 serial
                msm_serial_hsl       /dev/ttyHSL   245 0-2 serial
                msm_serial_hs        /dev/ttyHS    246 0-255 serial
                pty_slave            /dev/pts      136 0-1048575 pty:slave
                pty_master           /dev/ptm      128 0-1048575 pty:master*/
                String driverName = str.substring(0, 0x15).trim();
                Log.d(TAG, " driverName :" + driverName);
                String[] w = str.split(" +");
                if ((w.length >= 5) && (w[w.length - 1].equals("serial"))) {
                    Log.d(TAG, "Found new driver " + driverName + " on " + w[w.length - 4]);
                    mComDrivers.add(new SerialPortDriver(driverName, w[w.length - 4]));
                }
            }
            lineNumberReader.close();
        }
        return mComDrivers;
    }

    /**
     * 1、读取/proc/tty/drivers，遍历每行，以空格切割每行的字符串，最后一列为serial，则为串口对应的节点，取第一列节点名称和第二列串口路径
     * 2、遍历dev目录，根据第一步传入的路径，在dev目录下寻找所有以第一步传入路径开头的节点名称eg： /dev/ttyGS  在dev目录找到
     * @return
     */
    public String[] getAllComPath() {
        Vector<String> devices = new Vector<>();
        Iterator<SerialPortDriver> comDriver;
        try {
            comDriver = getDrivers().iterator();
            while (comDriver.hasNext()) {
                SerialPortDriver portDriver = comDriver.next();
                Iterator<File> iterator = portDriver.getDevices().iterator();
                while (iterator.hasNext()) {
                    String path = iterator.next().getAbsolutePath();
                    devices.add(path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices.toArray(new String[devices.size()]);
    }
}
