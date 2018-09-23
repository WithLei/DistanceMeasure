package com.android.renly.distancemeasure.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.renly.distancemeasure.Adapter.experimentAdapter;
import com.android.renly.distancemeasure.App;
import com.android.renly.distancemeasure.Bean.MeasureData;
import com.android.renly.distancemeasure.DB.MySQLiteOpenHelper;
import com.android.renly.distancemeasure.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ListActivity extends Activity {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.iv_toolbar_menu)
    ImageView ivToolbarMenu;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private View dialogView;
    private EditText et_carId;
    private Spinner spinner;
    private EditText et_MACAddr;
    private experimentAdapter adapter;

    private Unbinder unbinder;

    private List<MeasureData>experimentList;
    private static MySQLiteOpenHelper mySQLiteOpenHelper;
    private static SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        unbinder = ButterKnife.bind(this);
        queryDB();
        initData();
        initView();
    }

    private void queryDB() {
        experimentList = new ArrayList<>();
        mySQLiteOpenHelper = MySQLiteOpenHelper.getInstance(this);
        db = mySQLiteOpenHelper.getWritableDatabase();
        if (App.isFirstIn(this)){
            insertDB();
        }
        if (!db.isOpen())
            db = mySQLiteOpenHelper.getReadableDatabase();
        db.beginTransaction();

        synchronized (mySQLiteOpenHelper){
            // 开启查询 获得游标
            Cursor cursor = db.query(MySQLiteOpenHelper.TABLE_NAME,null,null,null,null,null,null);

            // 判断游标是否为空
            if (cursor.moveToLast()){
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

                    MeasureData data = new MeasureData(carid,carDirection,startDistance,nowDistance,result,measureTime,theTime,theID);
                    experimentList.add(data);
                }while (cursor.moveToPrevious());
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

        App.setFirst_In(this,false);
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

    private void deleteDB(MeasureData data){
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
        // 增加分割线
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // Set RecyclerView ItemAnimator.
        recyclerView.setItemAnimator(new SlideInLeftAnimator());

//        SlideInUpAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(1f));
//        recyclerView.setItemAnimator(animator);

        // 动画时长
        recyclerView.getItemAnimator().setAddDuration(1000);
        recyclerView.getItemAnimator().setRemoveDuration(1000);
        recyclerView.getItemAnimator().setMoveDuration(1000);
        recyclerView.getItemAnimator().setChangeDuration(1000);

        // 插值器
//        SlideInLeftAnimator animator = new SlideInLeftAnimator();
//        animator.setInterpolator(new OvershootInterpolator());
//        recyclerView.setItemAnimator(animator);

        adapter = new experimentAdapter(this,experimentList);
        recyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));
        adapter.setItemClickListener(new experimentAdapter.MyItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                MeasureData obj = experimentList.get(pos);
                Intent intent = new Intent(ListActivity.this, MainActivity.class);
                intent.putExtra("isNewMeasure",false);
                intent.putExtra("cardId",obj.getCarId());
                intent.putExtra("direction",obj.getCarDirection());
                intent.putExtra("startDirection",obj.getStartDistance());
                intent.putExtra("measureTime",obj.getMeasureTime());
                intent.putExtra("nowDirection",obj.getNowDistance());
                intent.putExtra("result",obj.getResult());
                intent.putExtra("theID",obj.getTheID());
                startActivityForResult(intent, MainActivity.REQUEST_CODE);
            }
        });

        adapter.setItemLongClickListener(new experimentAdapter.MyItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int pos) {
                new AlertDialog.Builder(ListActivity.this)
                        .setTitle("请选择")
                        .setItems(R.array.menu, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteDB(experimentList.get(pos));
                                adapter.remove(pos);
                            }
                        })
                        .create()
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
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
                            intent.putExtra("MACAddr",MACAddr);
                            intent.putExtra("isNewMeasure",true);
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
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
