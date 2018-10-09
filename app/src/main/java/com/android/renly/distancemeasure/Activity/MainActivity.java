package com.android.renly.distancemeasure.Activity;

import android.annotation.SuppressLint;
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
import com.android.renly.distancemeasure.Utils.TimeUtil;

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
     * 测验结果:
     * 0 - 未测量
     * 1 - 测量中...
     * 2 - 合格
     * 3 - 不合格
     */
    private static final int NOT_MEASURE = 0;
    private static final int MEASUREING = 1;
    private static final int SUCCESS_MEASURE = 2;
    private static final int FAIL_MEASURE = 3;
    private int measureResult = NOT_MEASURE;

    // 限制时间
    private static final int END_TIME = 300;
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
    private boolean isCompleted = false;

    private String[] keys = new String[]{
            "车牌/车架号：",
            "车辆朝向：",
            "初始时刻距离：",
            "目前距离：",
            "测验结果：",
    };

    private String[] values = new String[]{
            "获取中...",
            "获取中...",
            "获取中...",
            "获取中...",
            "未测量",
    };

    private int[] imgs = new int[]{
            R.drawable.ic_directions_car_black_24dp,
            R.drawable.ic_swap_calls_black_24dp,
            R.drawable.ic_alarm_black_24dp,
            R.drawable.ic_alarm_on_black_24dp,
            R.drawable.ic_chrome_reader_mode_black_24dp,
    };

    private SimpleAdapter adapter;
    private List<Map<String, Object>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        initData();
        if (isNewMeasure)
            initBluetooth();
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
            MACAddr = intent.getStringExtra("MACAddr");
        } else {
            // 历史实验
            carid = intent.getStringExtra("cardId");
            carDirection = intent.getStringExtra("direction");
            startDistance = intent.getIntExtra("startDirection", 0);
            nowDistance = intent.getIntExtra("nowDirection", 0);
            theID = intent.getIntExtra("theID", 0);
            measureTime = intent.getStringExtra("measureTime");
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
            timer.setBase(TimeUtil.convertStrTimeToLong(measureTime));
            timer.setText(measureTime);
        }
        setBtnNotTouch();

        list = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("key", keys[i]);
            objectMap.put("value", values[i]);
            objectMap.put("img", imgs[i]);
            list.add(objectMap);
        }
        adapter = new SimpleAdapter(this, list, R.layout.item_data, new String[]{"key", "value", "img"}, new int[]{R.id.key, R.id.value, R.id.img});
        lvMain.setAdapter(adapter);
    }

    private void setBtnNotTouch() {
        if (btnLeft != null && btnRight != null) {
            btnLeft.setClickable(false);
            btnRight.setClickable(false);
            ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
            ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
        }
    }

    private void setBtnTouch() {
        if (btnLeft != null && btnRight != null) {
            btnLeft.setClickable(true);
            btnRight.setClickable(true);
            ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
            if (tvRightbtn.getText().toString().equals("启动"))
                ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_normal));
            else
                ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_press));
        }
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
    }

    /**
     * 停止计时
     */
    private void stopTimer() {
        timer.stop();
        State_btn_left = true;
        State_btn_right = false;
        tvRightbtn.setText("启动");
        if (isBlueToothConnected){
            ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_normal));
            ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_enable));
            tvDistance.setTextColor(getResources().getColor(R.color.text_color_sec));
        }else{
            setBtnNotTouch();
        }

        stopBlutoothThread();
        if (isCompleted){
            ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
            btnRight.setClickable(false);
        }
    }

    /**
     * 开始计时
     */
    private void startTimer() {
//        // 开启线程
        if (State_btn_left == true) {
            BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(MACAddr);
            connect(bluetoothDevice);
        }
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (mConnectedThread != null) {
                        mConnectedThread.start();
                        break;
                    }
                }
            }
        }.start();

        timer.setBase(TimeUtil.convertStrTimeToLong(timer.getText().toString()));
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
     * 更新测验结果
     */
    private void updateResult(int result) {
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
                list.get(4).put("value", "合格");
                break;
            case FAIL_MEASURE:
                measureResult = FAIL_MEASURE;
                list.get(4).put("value", "不合格");
                break;
        }
        adapter.notifyDataSetChanged();
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
    protected void onDestroy() {
        unbinder.unbind();
        handler.removeCallbacksAndMessages(null);
        stopBlutoothThread();
        super.onDestroy();
    }

    private void stopBlutoothThread() {
        if (mConnectThread != null) {
            mConnectThread.interrupt();
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.interrupt();
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }


    private void saveMeasureData() {
        String result = "";
        switch (measureResult) {
            case 0:
                result = "未测量";
                break;
            case 1:
                result = "测量中止";
                break;
            case 2:
                result = "合格";
                break;
            case 3:
                result = "不合格";
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
            if (Thread.interrupted())
                return;
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
            if (Thread.interrupted()) {
                printLog("return");
                return;
            }
            printLog("BEGIN mConnectedThread");
            byte[] buffer = new byte[256];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                synchronized (this) {
                    try {
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);
                        if (State_btn_left) {
                            printLog("Thread.interrupted()");
                            break;
                        }
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
                            } else {
                                // 限制时间内
                                Message msg = new Message();
                                msg.what = GET_DATA;
                                Bundle bundle = new Bundle();
                                bundle.putInt("data", buffer[0]);
                                msg.setData(bundle);
                                handler.sendMessage(msg);
                            }
                        }
                        // mHandler.sendEmptyMessage(MSG_NEW_DATA);
                    } catch (IOException e) {
                        printLog("disconnected " + e);
//                        handler.sendEmptyMessage(OUT_OF_CONNECTED);
                        break;
                    }
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
    private static final int GET_DATA = 128;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (timer != null)
                switch (msg.what) {
                    case GET_DATA:
                        nowDistance = msg.getData().getInt("data");
                        list.get(3).put("value", nowDistance + " cm");
                        adapter.notifyDataSetChanged();
                        int x = nowDistance - startDistance;
                        if (x <= 99)
                            tvDistance.setText(x + " cm");
                        else
                            tvDistance.setText("超限");
                        break;
                    case GET_FIRST_DATA:
                        startDistance = msg.getData().getInt("data");
                        list.get(2).put("value", startDistance + " cm");
                        adapter.notifyDataSetChanged();
                        break;
                    case GET_LAST_DATA:
                        isCompleted = true;
                        stopTimer();
                        nowDistance = msg.getData().getInt("data");
                        list.get(3).put("value", nowDistance + " cm");
                        int xx = nowDistance - startDistance;
                        if (xx <= 99)
                            tvDistance.setText(xx + " cm");
                        else
                            tvDistance.setText("超限");
                        if (xx < END_DISTANCE && xx >= 0)
                            updateResult(SUCCESS_MEASURE);
                        else
                            updateResult(FAIL_MEASURE);
                        break;
                    case CONNECT_SUCCESS:
                        isBlueToothConnected = true;
                        setBtnTouch();
                        Toast.makeText(MainActivity.this, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                        break;
                    case OUT_OF_CONNECTED:
                        isBlueToothConnected = false;
                        if (!State_btn_left)
                            setBtnNotTouch();
                        stopTimer();
                        Toast.makeText(MainActivity.this, "蓝牙断开连接", Toast.LENGTH_SHORT).show();
                        break;
                    case NOT_CONNECT:
                        isBlueToothConnected = false;
                        if (!State_btn_left)
                            setBtnNotTouch();
                        stopTimer();
                        Toast.makeText(MainActivity.this, "蓝牙未连接", Toast.LENGTH_SHORT).show();
                        break;
                }
        }
    };
}
