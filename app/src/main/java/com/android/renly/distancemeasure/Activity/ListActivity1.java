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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.renly.distancemeasure.Adapter.experimentAdapter;
import com.android.renly.distancemeasure.App;
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
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class ListActivity1 extends Activity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.iv_toolbar_menu)
    ImageView ivToolbarMenu;
    @BindView(R.id.listView)
    SwipeMenuListView listView;

    private View dialogView;
    private EditText et_carId;
    private Spinner spinner;
    private EditText et_MACAddr;

    private Unbinder unbinder;

    private List<MeasureData> experimentList;
    private static MySQLiteOpenHelper mySQLiteOpenHelper;
    private static SQLiteDatabase db;
    private List<Map<String, Object>> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list1);
        unbinder = ButterKnife.bind(this);
        queryDB();
        initData();
        initView();
    }

    private void queryDB() {
        experimentList = new ArrayList<>();
        mySQLiteOpenHelper = MySQLiteOpenHelper.getInstance(this);
        db = mySQLiteOpenHelper.getWritableDatabase();
        if (App.isFirstIn(this)) {
            insertDB();
        }
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

                    Map<String, Object>items = new HashMap<String, Object>();
                    items.put("carid",carid);
                    items.put("measureTime",measureTime);
                    items.put("result",result);
                    items.put("time",theTime);
                    list.add(items);
                    MeasureData data = new MeasureData(carid, carDirection, startDistance, nowDistance, result, measureTime, theTime, theID);
                    experimentList.add(data);
                } while (cursor.moveToPrevious());
            }
            cursor.close();
        }
        db.endTransaction();
        db.close();
        mySQLiteOpenHelper.close();
    }

    private void initData() {
    }

    private void insertDB() {
//        MeasureData data1 = new MeasureData("测试车牌号01","向下",1,7,"测量失败","00:00:17","2018.8.11 13:24",1);
//        MeasureData data2 = new MeasureData("测试车牌号02","向上",2,3,"测量成功","00:00:12","2018.4.27 17:35",2);
//        MeasureData data3 = new MeasureData("测试车牌号03","向上",0,3,"未测量","00:00:13","2018.6.3 9:08",3);
//        MeasureData data4 = new MeasureData("测试车牌号04","向上",0,3,"测量成功","00:00:12","2018.4.27 13:24",4);
//        MeasureData data5 = new MeasureData("测试车牌号05","向上",0,3,"测量成功","00:00:12","2018.4.27 13:24",5);
//
//        synchronized (mySQLiteOpenHelper) {
//            db.beginTransaction();
//
//            db.execSQL(insertsql(data1));
//            db.execSQL(insertsql(data2));
//            db.execSQL(insertsql(data3));
//            db.execSQL(insertsql(data4));
//            db.execSQL(insertsql(data5));
//
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        }

        App.setFirst_In(this, false);
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
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(DimmenUtils.dip2px(ListActivity1.this, 90));
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
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                list.remove(position);
                listView.invalidate();
                return false;
            }
        });

        // Left
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        listView.setAdapter(new SimpleAdapter(this,
                list,
                R.layout.item_experiment,
                new String[]{"carid","measureTime","result","time"},
                new int[]{R.id.carid,R.id.measureTime,R.id.result,R.id.time}));
        // 增加分割线
//        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        // Set RecyclerView ItemAnimator.
//        recyclerView.setItemAnimator(new SlideInLeftAnimator());
//
////        SlideInUpAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(1f));
////        recyclerView.setItemAnimator(animator);
//
//        // 动画时长
//        recyclerView.getItemAnimator().setAddDuration(1000);
//        recyclerView.getItemAnimator().setRemoveDuration(1000);
//        recyclerView.getItemAnimator().setMoveDuration(1000);
//        recyclerView.getItemAnimator().setChangeDuration(1000);
//
//        // 插值器
////        SlideInLeftAnimator animator = new SlideInLeftAnimator();
////        animator.setInterpolator(new OvershootInterpolator());
////        recyclerView.setItemAnimator(animator);
//
//        adapter = new experimentAdapter(this, experimentList);
//        recyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
//        adapter.setItemClickListener(new experimentAdapter.MyItemClickListener() {
//            @Override
//            public void onItemClick(View view, int pos) {
//                MeasureData obj = experimentList.get(pos);
//                Intent intent = new Intent(ListActivity1.this, MainActivity.class);
//                intent.putExtra("isNewMeasure", false);
//                intent.putExtra("cardId", obj.getCarId());
//                intent.putExtra("direction", obj.getCarDirection());
//                intent.putExtra("startDirection", obj.getStartDistance());
//                intent.putExtra("measureTime", obj.getMeasureTime());
//                intent.putExtra("nowDirection", obj.getNowDistance());
//                intent.putExtra("result", obj.getResult());
//                intent.putExtra("theID", obj.getTheID());
//                startActivityForResult(intent, MainActivity.REQUEST_CODE);
//            }
//        });
//
//        adapter.setItemLongClickListener(new experimentAdapter.MyItemLongClickListener() {
//            @Override
//            public void onItemLongClick(View view, final int pos) {
//                new AlertDialog.Builder(ListActivity1.this)
//                        .setTitle("请选择")
//                        .setItems(R.array.menu, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                deleteDB(experimentList.get(pos));
//                                adapter.remove(pos);
//                            }
//                        })
//                        .create()
//                        .show();
//            }
//        });
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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
                            Toast.makeText(ListActivity1.this, "请确认输入所有数据", Toast.LENGTH_SHORT).show();
                        else {
                            Intent intent = new Intent(ListActivity1.this, MainActivity.class);
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
//                    Log.e("print","onActivityResult()");
//                    queryDB();
//                    adapter.notifyItemChanged(experimentList.size()-1);
//                    Log.e("print","experimentList.size()" + experimentList.size());
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryDB();
//        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
