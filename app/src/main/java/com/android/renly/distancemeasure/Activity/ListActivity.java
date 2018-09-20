package com.android.renly.distancemeasure.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.renly.distancemeasure.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ListActivity extends Activity{
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.iv_toolbar_menu)
    ImageView ivToolbarMenu;

    private View dialogView;
    private EditText et_carId;
    private Spinner spinner;

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        unbinder = ButterKnife.bind(this);
    }

    private void initDialog() {
        dialogView = View.inflate(this, R.layout.dialog_add, null);
        et_carId = dialogView.findViewById(R.id.et_carid);
        spinner = dialogView.findViewById(R.id.spinner);
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String carId = et_carId.getText().toString();
                        if (TextUtils.isEmpty(carId))
                            Toast.makeText(ListActivity.this, "请确认输入所有数据", Toast.LENGTH_SHORT).show();
                        else{
                            Intent intent = new Intent(ListActivity.this,MainActivity.class);
                            intent.putExtra("cardId",carId);
                            intent.putExtra("direction",spinner.getSelectedItem().toString());
                            startActivityForResult(intent,MainActivity.REQUEST_CODE);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case MainActivity.REQUEST_CODE:
                    // 刷新列表
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
