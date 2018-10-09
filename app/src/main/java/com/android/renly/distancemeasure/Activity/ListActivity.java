package com.android.renly.distancemeasure.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.renly.distancemeasure.Adapter.MyAdapter;
import com.android.renly.distancemeasure.Bean.MeasureData;
import com.android.renly.distancemeasure.DB.MySQLiteOpenHelper;
import com.android.renly.distancemeasure.R;
import com.android.renly.distancemeasure.Utils.DimmenUtils;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ListActivity extends Activity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.iv_toolbar_menu)
    ImageView ivToolbarMenu;
    @BindView(R.id.listView)
    SwipeMenuListView listView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;

    private View dialogView;
    private EditText et_carId;
    private Spinner spinner;
    private EditText et_MACAddr;

    private Unbinder unbinder;

    private List<MeasureData> experimentList;
    private MyAdapter adapter;
    private static MySQLiteOpenHelper mySQLiteOpenHelper;
    private static SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        unbinder = ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        queryDB();
    }

    private void queryDB() {
        experimentList = new ArrayList<>();
        mySQLiteOpenHelper = MySQLiteOpenHelper.getInstance(this);
        db = mySQLiteOpenHelper.getWritableDatabase();
        if (!db.isOpen())
            db = mySQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        synchronized (mySQLiteOpenHelper) {
            // 开启查询 获得游标
            Cursor cursor = db.query(MySQLiteOpenHelper.TABLE_NAME, null, null, null, null, null, null);

            // 判断游标是否为空
            if (cursor.moveToLast()) {
                // 遍历游标
                do {
                    String carid = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.carId));
                    String measureTime = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.measureTime));
                    String theTime = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.theTime));
                    String result = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.measureResult));
                    int theID = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.theID));
                    String carDirection = cursor.getString(cursor.getColumnIndex(MySQLiteOpenHelper.carDirection));
                    int startDistance = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.startDistance));
                    int nowDistance = cursor.getInt(cursor.getColumnIndex(MySQLiteOpenHelper.nowDistance));

                    Map<String, Object> items = new HashMap<String, Object>();
                    items.put("carid", carid);
                    items.put("measureTime", measureTime);
                    items.put("result", result);
                    items.put("time", theTime);
                    MeasureData data = new MeasureData(carid, carDirection, startDistance, nowDistance, result, measureTime, theTime, theID);
                    experimentList.add(data);
                } while (cursor.moveToPrevious());
            }
            cursor.close();
        }
        db.endTransaction();
        db.close();
        mySQLiteOpenHelper.close();
        new Thread(){
            @Override
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (swipeRefresh.isRefreshing())
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefresh.setRefreshing(false);

                        }
                    });
            }
        }.start();

    }


    private void deleteDB(MeasureData data) {
        db = mySQLiteOpenHelper.getWritableDatabase();
        db.beginTransaction();

        db.execSQL(deletesql(data.getTheID()));

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private String deletesql(int theID) {
        return "delete from " + MySQLiteOpenHelper.TABLE_NAME +
                " where " + MySQLiteOpenHelper.theID + " = " + theID;
    }

    private void initView() {
        initRefresh();

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(DimmenUtils.dip2px(ListActivity.this, 90));
                // set item title
                deleteItem.setTitle("删除");
                // set item title fontsize
                deleteItem.setTitleSize(18);
                // set item title font color
                deleteItem.setTitleColor(Color.WHITE);

                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
        listView.setMenuCreator(creator);

        // Left
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        adapter = new MyAdapter(this, experimentList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                MeasureData obj = experimentList.get(pos);
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                intent.putExtra("isNewMeasure", false);
                intent.putExtra("cardId", obj.getCarId());
                intent.putExtra("direction", obj.getCarDirection());
                intent.putExtra("startDirection", obj.getStartDistance());
                intent.putExtra("measureTime", obj.getMeasureTime());
                intent.putExtra("nowDirection", obj.getNowDistance());
                intent.putExtra("result", obj.getResult());
                intent.putExtra("theID", obj.getTheID());
                startActivityForResult(intent, MainActivity.REQUEST_CODE);
            }
        });
        listView.setAdapter(adapter);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Log.e("print", "delete " + position);
                deleteDB(experimentList.get(position));
                experimentList.remove(position);
                adapter.remove(position);
                return false;
            }
        });
    }

    private void initRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initData();
            }
        });
    }


    private void initDialog() {
        dialogView = View.inflate(this, R.layout.dialog_add, null);
        et_carId = dialogView.findViewById(R.id.et_carid);
        spinner = dialogView.findViewById(R.id.spinner);
        et_MACAddr = dialogView.findViewById(R.id.MAC_addr);
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String carId = et_carId.getText().toString();
                        String MACAddr = et_MACAddr.getText().toString();
                        if (TextUtils.isEmpty(carId))
                            Toast.makeText(ListActivity.this, "请确认输入所有数据", Toast.LENGTH_SHORT).show();
                        else {
                            Intent intent = new Intent(ListActivity.this, MainActivity.class);
                            intent.putExtra("cardId", carId);
                            intent.putExtra("direction", spinner.getSelectedItem().toString());
                            intent.putExtra("MACAddr", MACAddr);
                            intent.putExtra("isNewMeasure", true);
                            startActivityForResult(intent, MainActivity.REQUEST_CODE);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(true)
                .setTitle("创建一个测试项目")
                .create()
                .show();
    }

    @OnClick(R.id.iv_toolbar_menu)
    public void onViewClicked() {
        initDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MainActivity.REQUEST_CODE:
                    // 刷新列表
                    recreate();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryDB();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
