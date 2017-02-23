package test_serialport;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnOpen, btnRead;
    private TextView tvRead;

    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    private SerialPort serialPort;
    private int size;
    private SerialPortFinder finder;

    private static final String TAG = "TAG";
    private byte[] buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpen = (Button) findViewById(R.id.btn_open);
        btnRead = (Button) findViewById(R.id.btn_read);
        tvRead = (TextView) findViewById(R.id.tv_read);
        findViewById(R.id.btn_get_devices).setOnClickListener(this);
        btnOpen.setOnClickListener(this);
        btnRead.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open:
                openSerialPort();
                break;

            case R.id.btn_read:
                readReceivedData();
                break;

            case R.id.btn_get_devices:
                getAllSerialPortDevices();
                break;
        }
    }

    /**
     * 打开串口
     */
    private void openSerialPort() {
        try {
            serialPort = new SerialPort(new File("/dev/ttyMT2"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileOutputStream = (FileOutputStream) serialPort.getOutputStream();
        fileInputStream = (FileInputStream) serialPort.getInputStream();
        Toast.makeText(this, "打开成功", Toast.LENGTH_SHORT).show();
    }


    /**
     * 读取串口数据
     */
    private void readReceivedData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    buffer = new byte[64];
                    if (fileInputStream == null) return;
                    try {
                        size = fileInputStream.read(buffer);
                        Log.d(TAG, "size:"+size);
                        if (size > 0) {
                            onDataReceived(buffer, size);
                        }

                        Thread.sleep(1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    /**
     * 把获取到的数据转成十六进制后设置到TextView显示
     * @param buffer
     * @param size
     */
    private void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvRead.append(byteArrayToHex(buffer, size).toUpperCase()+"\n");
            }
        });
    }

    /**
     * byte数组转成十六进制
     * @param buffer
     * @param size
     * @return
     */
    private String byteArrayToHex(byte[] buffer, int size) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            strBuilder.append(String.format("%02x", buffer[i]));
            //strBuilder.append(" ");
        }
        return strBuilder.toString();
    }


    /**
     * 获取所有的串口设备
     */
    private void getAllSerialPortDevices() {
        finder = new SerialPortFinder();
        String[] devices = finder.getAllDevices();
        String[] paths = finder.getAllDevicesPath();
        Log.d(TAG, Arrays.toString(devices));
        Log.d(TAG, Arrays.toString(paths));
    }
}