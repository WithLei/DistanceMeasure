package com.android.renly.distancemeasure.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.android.renly.distancemeasure.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

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
     * 1 - 测量中
     * 2 - 测量合格
     * 3 - 测量失败
     */
    private static final int NOT_MEASURE = 0;
    private static final int MEASUREING = 1;
    private static final int SUCCESS_MEASURE = 2;
    private static final int FAIL_MEASURE = 3;
    private int measureResult = NOT_MEASURE;

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
        initBluetooth();
        initData();
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt mBluetoothGatt;

    /**
     * 初始化蓝牙模块
     */
    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothAdapter == null){
            Toast.makeText(this, "当前手机不支持蓝牙功能", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //若没打开则打开蓝牙
            mBluetoothAdapter.enable();
            Toast.makeText(this, "打开蓝牙", Toast.LENGTH_SHORT).show();
        }
        bluetoothDevice = mBluetoothAdapter.getRemoteDevice("80:AD:16:DB:3B:4D");
//        bluetoothDevice = mBluetoothAdapter.getRemoteDevice("20:18:06:14:10:73");
        //如果Gatt在运行,将其关闭
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        //连接蓝牙设备并获取Gatt对象hhj
        mBluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, true, bluetoothGattCallback);
    }

    /**
     * 蓝牙返回数据函数
     */
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e("print","111");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.e("print","设备连接成功");

                    //搜索Service
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e("print","设备连接断开");
                }
            }else if(status == BluetoothGatt.GATT_FAILURE)
                Log.e("print","设备连接失败");
            else
                Log.e("print","设备其他原因连接失败" );
            Log.e("print","status" + status);
            Log.e("print","newState" + newState);
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //根据UUID获取Service中的Characteristic,并传入Gatt中
//            BluetoothGattService bluetoothGattService = gatt.getService(UUID_SERVICE);
//            BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID_NOTIFY);
//
//            boolean isConnect = gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
//            if (isConnect){
//
//            }else {
//                Log.i("geanwen", "onServicesDiscovered: 设备一连接notify失败");
//            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {//数据改变
            super.onCharacteristicChanged(gatt, characteristic);
            String data = new String(characteristic.getValue());
            Log.i("print", "onCharacteristicChanged: " + data);
        }
    };

    private void initData() {
        Intent intent = getIntent();
        values[0] = intent.getStringExtra("cardId");
        values[1] = intent.getStringExtra("direction");
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

    @OnClick({R.id.back, R.id.btn_left, R.id.btn_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.btn_left:
                recreateTimer();
                break;
            case R.id.btn_right:
                if (!State_btn_right) {
                    // 预备状态转运行状态
                    startTimer();
                } else {
                    // 运行状态转停止状态
                    stopTimer();
                }
                break;
        }
    }

    /**
     * 复位计时器
     */
    private void recreateTimer() {
        if (State_btn_left) {
            tvDistance.setText("3 cm");
            timer.setBase(SystemClock.elapsedRealtime()); //计数器清零
            ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
            list.get(4).put("key","测量结果：");
            list.get(4).put("value","未测量");
            adapter.notifyDataSetChanged();
        }
        updateResult(NOT_MEASURE);
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
    }

    /**
     * 开始计时
     */
    private void startTimer() {
        timer.setBase(convertStrTimeToLong(timer.getText().toString()));
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
        timer.setFormat("0"+String.valueOf(hour)+":%s");
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
        list.get(4).put("key","测量结果：");
        switch (result){
            case NOT_MEASURE:
                list.get(4).put("value","未测量");
                break;
            case MEASUREING:
                list.get(4).put("value","测量中");
                break;
            case SUCCESS_MEASURE:
                list.get(4).put("value","测量成功");
                break;
            case FAIL_MEASURE:
                list.get(4).put("value","测量失败");
                break;
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 将String类型的时间转换成long,如：12:01:08
     * @param strTime String类型的时间
     * @return long类型的时间
     * */
    protected long convertStrTimeToLong(String strTime) {
        String []timeArry=strTime.split(":");
        long longTime=0;
        if (timeArry.length==2) {//如果时间是MM:SS格式
            longTime=Integer.parseInt(timeArry[0])*1000*60+Integer.parseInt(timeArry[1])*1000;
        }else if (timeArry.length==3){//如果时间是HH:MM:SS格式
            longTime=Integer.parseInt(timeArry[0])*1000*60*60+Integer.parseInt(timeArry[1])
                    *1000*60+Integer.parseInt(timeArry[2])*1000;
        }
        return SystemClock.elapsedRealtime()-longTime;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

}
