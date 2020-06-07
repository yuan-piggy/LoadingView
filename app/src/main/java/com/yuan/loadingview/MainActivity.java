package com.yuan.loadingview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //模拟获取后台数据
        final SplashView splashView = (SplashView) findViewById(R.id.thirdScreenView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                splashView.disappear();
            }
        },2000);
    }
}
