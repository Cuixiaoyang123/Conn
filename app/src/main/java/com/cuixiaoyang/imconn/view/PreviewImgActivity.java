package com.cuixiaoyang.imconn.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.cuixiaoyang.imconn.R;

public class PreviewImgActivity extends AppCompatActivity {
    private ZoomImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_img);

        String url = getIntent().getStringExtra("url");

        iv = findViewById(R.id.iv_zoom);
        Glide.with(this).load(url).into(iv);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}