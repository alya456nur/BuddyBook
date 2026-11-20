package com.example.buddybook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends BaseActivity {

    private static final long SPLASH_DURATION = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen =
                SplashScreen.installSplashScreen(this);

        long startTime = System.currentTimeMillis();

        splashScreen.setKeepOnScreenCondition(() ->
                System.currentTimeMillis() - startTime
                        < SPLASH_DURATION
        );

        super.onCreate(savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            startActivity(
                    new Intent(
                            SplashActivity.this,
                            MainActivity.class
                    )
            );

            finish();

        }, SPLASH_DURATION);
    }
}