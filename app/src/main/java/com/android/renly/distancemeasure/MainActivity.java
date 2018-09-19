package com.android.renly.distancemeasure;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.renly.distancemeasure.Utils.TimeUtil;

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
    @BindView(R.id.tv_time)
    TextView tvTime;
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

    private Unbinder unbinder;
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
     * 是否暂停
     */
    private boolean isPause = false;

    private String[] keys = new String[]{
            "车牌/车架号：",
            "车辆朝向：",
            "初始时刻距离：",
            "目前距离：",
            "测量结果：",
    };

    private String[] values = new String[]{
            "浙A 123456",
            "上",
            "0 cm",
            "3 cm",
            "合格",
    };

    private Thread TimerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        List<Map<String, String>> list = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            Map<String, String> objectMap = new HashMap<>();
            objectMap.put("key", keys[i]);
            objectMap.put("value", values[i]);
            list.add(objectMap);
        }
        lvMain.setAdapter(new SimpleAdapter(this,list,R.layout.item_data,new String[]{"key","value"},new int[]{R.id.key,R.id.value}));

    }

    @OnClick({R.id.back, R.id.btn_left, R.id.btn_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.btn_left:
                if (State_btn_left) {
                    TimerThread.interrupt();
                    tvTime.setText("00:00.00");
                    tvDistance.setText("移动距离： 3 cm");
                    ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
                    isPause = false;
                }
                break;
            case R.id.btn_right:
                if (!State_btn_right) {
                    // 预备状态转运行状态
                    State_btn_right = true;
                    tvRightbtn.setText("停止");
                    ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_press));
                    State_btn_left = false;
                    ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_unenable));
                    startTimer();
                } else {
                    // 运行状态转停止状态
                    State_btn_right = false;
                    tvRightbtn.setText("启动");
                    ivRightbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_right_normal));
                    State_btn_left = true;
                    ivLeftbtn.setImageDrawable(getDrawable(R.drawable.shape_btn_left_enable));
                    stopTimer();
                }
                break;
        }
    }

    /**
     * 停止计时
     */
    private void stopTimer() {
        isPause = true;
        TimerThread.suspend();
    }

    /**
     * 开始计时
     */
    private void startTimer() {
        isPause = false;
        if (TimerThread.isAlive())
            TimerThread.run();
        else{
            TimerThread = new Thread(timeRunnable);
            TimerThread.run();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * 当前毫秒数
     */
    private long currentSecond = 0;
    private Handler handler = new Handler();
    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
            currentSecond = currentSecond + 10;
            tvTime.setText(TimeUtil.getFormatHMS(currentSecond));
            if (!isPause){
                // 递归
                handler.postDelayed(this,10);
            }
        }
    };
}
