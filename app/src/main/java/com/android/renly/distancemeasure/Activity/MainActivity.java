package com.android.renly.distancemeasure.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.renly.distancemeasure.App;
import com.android.renly.distancemeasure.Bean.MeasureData;
import com.android.renly.distancemeasure.DB.MySQLiteOpenHelper;
import com.android.renly.distancemeasure.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.myToolBar)
    FrameLayout myToolBar;
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @BindView(R.id.btn_left)
    FrameLayout btnLeft;
    @BindView(R.id.btn_right)
    FrameLayout btnRight;
    @BindView(R.id.tv_rightbtn)
    TextView tvRightbtn;
    @BindView(R.id.iv_leftbtn)
    ImageView ivLeftbtn;
    @BindView(R.id.iv_rightbtn)
    ImageView ivRightbtn;
    @BindView(R.id.lv_main)
    ListView lvMain;
    @BindView(R.id.tv_time)
    Chronometer timer;

    private Unbinder unbinder;
    public static final int REQUEST_CODE = 256;
    /**
     * 左边按钮初始状态为不可点击
     * false - 不可点击
     * true - 可点击
     * 注意：只有停止状态可复位。
     */
    private boolean State_btn_left = false;

    /**
     * 有边按钮初始状态为预备状态
     * false - 预备状态 start 绿色
     * true - 运行状态 stop 红色
     */
    private boolean State_btn_right = false;
    /**
     * 测量结果:
     * 0 - 未测量
     * 1 - 测量中...
     * 2 - 驻车自动成功
     * 3 - 驻车自动失败
     */
    private static final int NOT_MEASURE = 0;
    private static final int MEASUREING = 1;
    private static final int SUCCESS_MEASURE = 2;
    private static final int FAIL_MEASURE = 3;
    private int measureResult = NOT_MEASURE;

    // 限制时间
    private static final int END_TIME = 5;
    // 限制长度
    private static final int END_DISTANCE = 5;
    // MAC地址
    private String MACAddr = "20:18:06:14:10:73";

    private String carid = "测试车牌000";
    private String carDirection = "测试方向";
    private int startDistance = 0;
    private int nowDistance = 0;
    private String measureTime;
    private String theTime;
    private int theID;

    private boolean isBlueToothConnected = false;
    private boolean isFirstData = true;
    private boolean isNewMeasure = true;

    private String[] keys = new String[]{
            "车牌/车架号：",
            "车辆朝向：",
            "初始时刻距离：",
            "目前距离：",
            "测量结果：",
    };

    private String[] values = new String[]{
            "获取中...",
            "获取中...",
            "获取中...",
            "获取中...",
            "未测量",
    };

    private SimpleAdapter adapter;
    private List<Map<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        initData();
    }

    private BluetoothAdapter mBluetoothAdapter;

    private void initData() {
        title.setText("实验测量");
        Intent intent = getIntent();
        isNewMeasure = intent.getBooleanExtra("isNewMeasure", false);
        if (isNewMeasure) {
            // 新的实验
            values[0] = intent.getStringExtra("cardId");
            carid = values[0];
            values[1] = intent.getStringExtra("direction");
            carDirection = values[1];
        } else {
            // 历史实验
            carid = intent.getStringExtra("cardId");
            carDirection = intent.getStringExtra("direction");
            startDistance = intent.getIntExtra("startDirection", 0);
            nowDistance = intent.getIntExtra("nowDirection", 0);
            theID = intent.getIntExtra("theID", 0);
            measureTime = intent.getStringExtra("measureTime");
            MACAddr = intent.getStringExtra("MACAddr");
            values[0] = carid;
            values[1] = carDirection;
            values[2] = startDistance + " cm";
            values[3] = nowDistance + " cm";
            values[4] = intent.getStringExtra("result");

            int x = nowDistance - startDistance;
            if (x < 99)
                tvDistance.setText(x + " cm");
            else
                tvDistance.setText("超限");
            timer.setBase(convertStrTimeToLong(measureTime));
            timer.setText(measureTime);
        }
        setBtnNotTouch();

        list = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            Map<String, String> objectMap = new HashMap<>();
            objectMap.put("key", keys[i]);
            objectMap.put("value", values[i]);
            list.add(objectMap);
        }
        adapter = new SimpleAdapter(this, list, R.layout.item_data, new String[]{"key", "value"}, new int[]{R.id.key, R.id.value});
        lvMain.setAdapter(adapter);
    }

    private void setBtnNotTouch() {
        btnLeft.setClickable(false);
        btnRight.setClickable(false);
        ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
        ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
    }

    private void setBtnTouch(){
        btnLeft.setClickable(true);
        btnRight.setClickable(true);
        ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
        ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_normal));
    }

    @OnClick({R.id.back, R.id.btn_left, R.id.btn_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                if (isNewMeasure)
                    finishMeasure();
                else
                    finish();
                break;
            case R.id.btn_left:
                if (State_btn_left)
                    recreateTimer();
                break;
            case R.id.btn_right:
                if (!State_btn_right) {
                    // 预备状态转运行状态
                    if (isBlueToothConnected)
                        // 已经连接上蓝牙
                        startTimer();
                } else {
                    // 运行状态转停止状态
                    stopTimer();
                }
                break;
        }
    }

    /**
     * 结束实验保存数据
     */
    private void finishMeasure() {
        new AlertDialog.Builder(this)
                .setTitle("保存此次实验数据吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveMeasureData();
                        finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .create()
                .show();
    }

    /**
     * 复位计时器
     */
    private void recreateTimer() {
        recreate();
//        tvDistance.setText("0 cm");
//        timer.setBase(SystemClock.elapsedRealtime()); //计数器清零
//        ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
//        list.get(2).put("value","获取中...");
//        list.get(3).put("value","获取中...");
//        list.get(4).put("value", "未测量");
//        isFirstData = true;
//        updateResult(NOT_MEASURE);
//
//        initBluetooth();
    }

    /**
     * 停止计时
     */
    private void stopTimer() {
        timer.stop();
        State_btn_right = false;
        tvRightbtn.setText("启动");
        ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_normal));
        State_btn_left = true;
        ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_enable));
        tvDistance.setTextColor(getResources().getColor(R.color.text_color_sec));

        stopBlutoothThread();
    }

    /**
     * 开始计时
     */
    private void startTimer() {
        // 开启线程
        handler.sendEmptyMessage(START_CONNECT);

        timer.setBase(convertStrTimeToLong(timer.getText().toString()));
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
        timer.setFormat("0" + String.valueOf(hour) + ":%s");
        timer.start();
        State_btn_right = true;
        tvRightbtn.setText("停止");
        tvDistance.setTextColor(getResources().getColor(R.color.text_color_pri));
        ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_press));
        State_btn_left = false;
        ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));

        updateResult(MEASUREING);
    }

    /**
     * 更新测量结果
     */
    private void updateResult(int result) {
        list.get(4).put("key", "测量结果：");
        switch (result) {
            case NOT_MEASURE:
                measureResult = NOT_MEASURE;
                list.get(4).put("value", "未测量");
                break;
            case MEASUREING:
                measureResult = MEASUREING;
                list.get(4).put("value", "测量中");
                break;
            case SUCCESS_MEASURE:
                measureResult = SUCCESS_MEASURE;
                list.get(4).put("value", "驻车自动成功");
                break;
            case FAIL_MEASURE:
                measureResult = FAIL_MEASURE;
                list.get(4).put("value", "驻车自动失败");
                break;
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 将String类型的时间转换成long,如：12:01:08
     *
     * @param strTime String类型的时间
     * @return long类型的时间
     */
    protected long convertStrTimeToLong(String strTime) {
        String[] timeArry = strTime.split(":");
        long longTime = 0;
        if (timeArry.length == 2) {//如果时间是MM:SS格式
            longTime = Integer.parseInt(timeArry[0]) * 1000 * 60 + Integer.parseInt(timeArry[1]) * 1000;
        } else if (timeArry.length == 3) {//如果时间是HH:MM:SS格式
            longTime = Integer.parseInt(timeArry[0]) * 1000 * 60 * 60 + Integer.parseInt(timeArry[1])
                    * 1000 * 60 + Integer.parseInt(timeArry[2]) * 1000;
        }
        return SystemClock.elapsedRealtime() - longTime;
    }

    private void printLog(String str) {
        Log.e("print", str);
    }

    @Override
    public void onBackPressed() {
        if (isNewMeasure)
            finishMeasure();
        else
            finish();
    }

    @Override
    protected void onResume() {
        initBluetooth();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        stopBlutoothThread();
        super.onDestroy();
    }

    private void stopBlutoothThread() {
        if (mConnectThread != null)
            mConnectThread.cancel();
        if (mConnectedThread != null)
            mConnectedThread.cancel();
    }

    private void saveMeasureData() {
        String result = "";
        switch (measureResult) {
            case 0:
            case 1:
                result = "测量中止";
                break;
            case 2:
                result = "驻车自动测量成功";
                break;
            case 3:
                result = "驻车自动测量失败";
                break;
        }

        //获取当前时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        theTime = simpleDateFormat.format(date);
        measureTime = timer.getText().toString();
        MeasureData data = new MeasureData(carid, carDirection, startDistance, nowDistance, result, measureTime, theTime);

        MySQLiteOpenHelper mySQLiteOpenHelper = MySQLiteOpenHelper.getInstance(this);
        SQLiteDatabase db = mySQLiteOpenHelper.getWritableDatabase();
        synchronized (mySQLiteOpenHelper) {
            db.beginTransaction();

            db.execSQL(insertsql(data));

            db.setTransactionSuccessful();
            db.endTransaction();
        }

        db.close();
        mySQLiteOpenHelper.close();
        setResult(RESULT_OK);
    }

    private String insertsql(MeasureData data) {
        return "insert into " + MySQLiteOpenHelper.TABLE_NAME +
                "(" + MySQLiteOpenHelper.carId + "," +
                MySQLiteOpenHelper.carDirection + "," +
                MySQLiteOpenHelper.startDistance + "," +
                MySQLiteOpenHelper.nowDistance + "," +
                MySQLiteOpenHelper.measureResult + "," +
                MySQLiteOpenHelper.measureTime + "," +
//                MySQLiteOpenHelper.theID + "," +
                MySQLiteOpenHelper.theTime + ") values('" +
                data.getCarId() + "','" +
                data.getCarDirection() + "','" +
                data.getStartDistance() + "','" +
                data.getNowDistance() + "','" +
                data.getResult() + "','" +
                data.getMeasureTime() + "','" +
//                data.getTheID() + "','" +
                data.getTime() + "')";
    }

    private ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;

    private List<Integer> mBuffer;

    public void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mBuffer = new ArrayList<Integer>();
        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(MACAddr);
        connect(bluetoothDevice);
    }

    public void connect(BluetoothDevice device) {
        printLog("connect to: " + device);
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(App.SPP_UUID));
            } catch (IOException e) {
                printLog("create() failed" + e);
            }
            mmSocket = tmp;
        }

        public void run() {
            printLog("BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                isBlueToothConnected = true;
                mmSocket.connect();
            } catch (IOException e) {

                printLog("unable to connect() socket " + e);
                handler.sendEmptyMessage(NOT_CONNECT);
                isBlueToothConnected = false;
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    printLog("unable to close() socket during connection failure" + e2);
                }
                return;
            }

            mConnectThread = null;

            isBlueToothConnected = true;

            // Start the connected thread
            // Start the thread to manage the connection and perform
            // transmissions
            handler.sendEmptyMessage(CONNECT_SUCCESS);

            mConnectedThread = new ConnectedThread(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                printLog("close() of connect socket failed" + e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            printLog("create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                printLog("temp sockets not created" + e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            printLog("BEGIN mConnectedThread");
            byte[] buffer = new byte[256];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    printLog(bytes + "bytes");
                    if (isFirstData) {
                        Message msg = new Message();
                        msg.what = GET_FIRST_DATA;
                        Bundle bundle = new Bundle();
                        bundle.putInt("data", buffer[0]);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        isFirstData = false;
                    } else {
                        if (getMeasureTime() >= END_TIME) {
                            // 如果已经达到限制时间
                            Message msg = new Message();
                            msg.what = GET_LAST_DATA;
                            Bundle bundle = new Bundle();
                            bundle.putInt("data", buffer[0]);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            break;
                        }
                    }
                    // mHandler.sendEmptyMessage(MSG_NEW_DATA);
                } catch (IOException e) {
                    printLog("disconnected " + e);
                    handler.sendEmptyMessage(OUT_OF_CONNECTED);
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * 获取测量时间 s为单位
     *
     * @return
     */
    private int getMeasureTime() {
        int totalss = 0;
        String string = timer.getText().toString();
        String[] split = string.split(":");
        String string2 = split[0];
        int hour = Integer.parseInt(string2);
        int Hours = hour * 3600;
        String string3 = split[1];
        int min = Integer.parseInt(string3);
        int Mins = min * 60;
        int SS = Integer.parseInt(split[2]);
        totalss = Hours + Mins + SS;
        return totalss;
    }

    private static final int GET_FIRST_DATA = 512;
    private static final int GET_LAST_DATA = 1024;
    private static final int CONNECT_SUCCESS = 2048;
    private static final int OUT_OF_CONNECTED = 4096;
    private static final int NOT_CONNECT = 9192;
    private static final int START_CONNECT = 256;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_FIRST_DATA:
                    startDistance = msg.getData().getInt("data");
                    list.get(2).put("value", startDistance + " cm");
                    adapter.notifyDataSetChanged();
                    break;
                case GET_LAST_DATA:
                    stopTimer();
                    nowDistance = msg.getData().getInt("data");
                    list.get(3).put("value", nowDistance + " cm");
                    int x = nowDistance - startDistance;
                    if (x <= 99)
                        tvDistance.setText(x + " cm");
                    else
                        tvDistance.setText("超限");
                    if (x < END_DISTANCE && x >= 0)
                        updateResult(SUCCESS_MEASURE);
                    else
                        updateResult(FAIL_MEASURE);
                    stopBlutoothThread();
                    break;
                case CONNECT_SUCCESS:
                    isBlueToothConnected = true;
                    setBtnTouch();
                    Toast.makeText(MainActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                    break;
                case OUT_OF_CONNECTED:
                    isBlueToothConnected = false;
                    setBtnNotTouch();
                    if (timer.isActivated())
                        stopTimer();
                    Toast.makeText(MainActivity.this, "蓝牙断开连接", Toast.LENGTH_SHORT).show();
                    break;
                case NOT_CONNECT:
                    isBlueToothConnected = false;
                    setBtnNotTouch();
                    if (timer.isActivated())
                        stopTimer();
                    Toast.makeText(MainActivity.this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
                    break;
                case START_CONNECT:
                    mConnectedThread.start();
                    break;
            }
        }
    };
}
