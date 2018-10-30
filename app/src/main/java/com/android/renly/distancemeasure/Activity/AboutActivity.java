package com.android.renly.distancemeasure.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.renly.distancemeasure.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends Activity {
    @BindView(R.id.btn_back)
    ImageView btnBack;
    @BindView(R.id.header)
    FrameLayout header;
    @BindView(R.id.tv_about_title)
    TextView tvAboutTitle;
    @BindView(R.id.tv_about_content)
    TextView tvAboutContent;
    @BindView(R.id.tv_about_url)
    TextView tvAboutUrl;
    @BindView(R.id.version)
    TextView version;
    @BindView(R.id.server_version)
    TextView serverVersion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_back, R.id.tv_about_url})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.tv_about_url:
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://github.com/WithLei/DistanceMeasure"));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                break;
        }
    }
}
