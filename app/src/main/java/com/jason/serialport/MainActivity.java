package com.jason.serialport;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jason.serialport.bean.ComBean;
import com.jason.serialport.util.ByteDisposeUtil;
import com.jason.serialport.util.SerialPortFinder;
import com.jason.serialport.util.SerialPortUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private EditText mEtShowOutResult;
    private Button mBtnClear;
    private RadioGroup mRgOption;
    private RadioButton mRbShowText;
    private RadioButton mRbShowHex;
    private EditText mEtSetReceiveDataLineNumber;
    private CheckBox mCbAuroClear;
    private EditText mEtInputCmd;
    private Spinner mSpinnerChooseTty;
    private Spinner mSpinnerChooseBaudrate;
    private ToggleButton mTbnOpenSerialPort;
    private EditText mEtIntervalTime;
    private CheckBox mCbAutoSendData;
    private Button mBtnAutoSendCmd;
    private SerialPortFinder mSerialPortFinder;
    private SerialPortUtil mSerialPortUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSerialPortFinder = new SerialPortFinder();
        mSerialPortUtil = new SerialPortUtil() {
            @Override
            protected void onDataReceived(final ComBean comRecData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayReceiveData(comRecData);
                    }
                });
            }
        };

        initView();
    }

    int mRecLines = 0;//接收区行数

    private void displayReceiveData(ComBean comRecData) {
        StringBuilder sb = new StringBuilder();
        sb.append(comRecData.sRecTime);
        sb.append("[");
        sb.append(comRecData.sComPort);
        sb.append("]");
        if (mRbShowText.isChecked()) {
            sb.append("[Txt] ");
            sb.append(new String(comRecData.bRec));
        } else if (mRbShowHex.isChecked()) {
            sb.append("[Hex] ");
            sb.append(ByteDisposeUtil.byteArrToHex(comRecData.bRec));
        }
        sb.append("\r\n");
        mEtShowOutResult.append(sb);
        mRecLines++;
        mEtSetReceiveDataLineNumber.setText(String.valueOf(mRecLines));
        if ((mRecLines > 500) && (mCbAuroClear.isChecked())) {//达到500项自动清除
            mEtShowOutResult.setText("");
            mEtSetReceiveDataLineNumber.setText("0");
            mRecLines = 0;
        }


    }

    private void initView() {
        mEtShowOutResult = (EditText) findViewById(R.id.et_show_out_result);
        mBtnClear = (Button) findViewById(R.id.bt_clear);
        mRgOption = (RadioGroup) findViewById(R.id.rg_option);
        mRbShowText = (RadioButton) findViewById(R.id.rb_show_txt);
        mRbShowHex = (RadioButton) findViewById(R.id.rb_show_hex);
        mEtSetReceiveDataLineNumber = (EditText) findViewById(R.id.et_set_receive_data_linenumber);
        mCbAuroClear = (CheckBox) findViewById(R.id.cb_autoClear);
        mEtInputCmd = (EditText) findViewById(R.id.et_input_cmd);
        mSpinnerChooseTty = (Spinner) findViewById(R.id.spinner_choose_tty);
        mSpinnerChooseBaudrate = (Spinner) findViewById(R.id.spinner_choose_baudrate);
        mTbnOpenSerialPort = (ToggleButton) findViewById(R.id.tb_open_serialport);
        mEtIntervalTime = (EditText) findViewById(R.id.et_interval_time);
        mCbAutoSendData = (CheckBox) findViewById(R.id.cb_auto_senddata);
        mBtnAutoSendCmd = (Button) findViewById(R.id.bt_auto_send_cmd);

        mEtInputCmd.setOnEditorActionListener(new EditorActionEvent());
        mEtIntervalTime.setOnEditorActionListener(new EditorActionEvent());

        mEtInputCmd.setOnFocusChangeListener(new FocusChangeEvent());
        mEtIntervalTime.setOnFocusChangeListener(new FocusChangeEvent());

        mRbShowHex.setOnClickListener(this);
        mRbShowText.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        mBtnAutoSendCmd.setOnClickListener(this);

        mCbAutoSendData.setOnCheckedChangeListener(this);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.baudrates_value, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerChooseBaudrate.setAdapter(adapter);

        String[] allComPath = mSerialPortFinder.getAllComPath();
        List<String> allDevices = Arrays.asList(allComPath);
        ArrayAdapter<String> ttyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allDevices);
        ttyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerChooseTty.setAdapter(ttyAdapter);

        mSpinnerChooseTty.setOnItemSelectedListener(this);
        mSpinnerChooseBaudrate.setOnItemSelectedListener(this);

        mTbnOpenSerialPort.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
    }

    @Override
    public void onClick(View view) {
        if (view == mBtnClear) {
            mEtShowOutResult.setText("");
        } else if (view == mBtnAutoSendCmd) {
            sendPortData(mEtInputCmd.getText().toString());
        }
    }

    private void sendPortData(String inputCmd) {
        if (mSerialPortUtil != null && mSerialPortUtil.isOpen()) {
            if (mRbShowText.isChecked()) {
                mSerialPortUtil.sendStr(inputCmd);
            } else if (mRbShowHex.isChecked()) {
                mSerialPortUtil.sendHex(inputCmd);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == mCbAutoSendData) {
            if (!mTbnOpenSerialPort.isChecked() && b) {
                compoundButton.setChecked(false);
                return;
            }
            setSendData(mEtInputCmd.getText().toString());
            setAutoSend(b);
        }
    }

    //设置自动发送模式开关
    private void setAutoSend(boolean isAutoSend) {
        if (isAutoSend) {
            mSerialPortUtil.startSend();
        } else {
            mSerialPortUtil.stopSend();
        }
    }

    class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                mSerialPortUtil.setPort(mSpinnerChooseTty.getSelectedItem().toString());
                mSerialPortUtil.setBaudRate(mSpinnerChooseBaudrate.getSelectedItem().toString());
                openComPort(mSerialPortUtil);
            } else {
                mSerialPortUtil.stopSend();
                mSerialPortUtil.close();
                mCbAutoSendData.setChecked(false);
            }
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView == mSpinnerChooseBaudrate || adapterView == mSpinnerChooseTty) {
            closeComPort(mSerialPortUtil);
            mCbAutoSendData.setChecked(false);
            mTbnOpenSerialPort.setChecked(false);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void closeComPort(SerialPortUtil serialPortUtil) {
        if (serialPortUtil != null) {
            serialPortUtil.stopSend();
            serialPortUtil.close();
        }
    }

    private void openComPort(SerialPortUtil serialPortUtil) {
        try {
            mSerialPortUtil.open();
        } catch (SecurityException e) {
            showToast("打开串口失败：没有串口读或写权限！");
        } catch (IOException e) {
            showToast("打开串口失败：未知错误！");
            e.printStackTrace();
        }
    }

    class EditorActionEvent implements EditText.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (textView == mEtInputCmd) {
                setSendData(textView.getText().toString());
            } else if (textView == mEtIntervalTime) {
                setDelayTime(textView.getText().toString());
            }
            return false;
        }
    }

    //编辑框焦点转移事件
    class FocusChangeEvent implements EditText.OnFocusChangeListener {

        @Override
        public void onFocusChange(View view, boolean b) {
            if (view == mEtInputCmd) {
                setSendData(mEtInputCmd.getText().toString());
            } else if (view == mEtIntervalTime) {
                setDelayTime(mEtIntervalTime.getText().toString());
            }
        }
    }

    private void setDelayTime(String s) {
        mSerialPortUtil.setDelay(Integer.parseInt(s));
    }

    private void setSendData(String sendStr) {
        if (mRbShowText.isChecked()) {
            mSerialPortUtil.setStrOutPutData(sendStr);
        } else if (mRbShowHex.isChecked()) {
            mSerialPortUtil.setHexOutPutData(sendStr);
        }
    }

}
