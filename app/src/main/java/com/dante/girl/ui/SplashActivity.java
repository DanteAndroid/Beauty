package com.dante.girl.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dante.girl.MainActivity;
import com.dante.girl.R;
import com.dante.girl.base.App;
import com.dante.girl.base.Constants;
import com.dante.girl.model.DataBase;
import com.dante.girl.model.Image;
import com.dante.girl.utils.Imager;
import com.dante.girl.utils.SpUtil;

import java.util.Date;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 1800;
    private static final int SPLASH_DURATION_SHORT = 1500;
    private static final String SPLASH = "splash";

    public static int FULL_SCREEN_UI = View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    private static String type;
    private boolean noCache;

    public static void updateSplash(String type, boolean force) {
        Date now = new Date();
        boolean needUpdate = lastUpdateIsTenMinBefore();
        if (needUpdate || force) {
            if (App.noCache) {
                type = Constants.FAVORITE;
            }
            String url = DataBase.getRandomImage(type);
            Log.d(TAG, "updateSplash: ");
            if (TextUtils.isEmpty(url)) {
                Log.d(TAG, "updateSplash: no picture");
                return;
            }
            SpUtil.save(Constants.DATE, now.getTime());
            SpUtil.save(SPLASH, url);
        }
    }

    public static boolean lastUpdateIsTenMinBefore() {
        Date now = new Date();
        return (now.getTime() - SpUtil.getLong(Constants.DATE)) > 10 * 60 * 1000;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_UI);
        type = SpUtil.getString(SettingFragment.RANDOM_SPLASH);
        if (type.isEmpty()) {
            type = "0";
        }
        if (type.equals("origin")) {
            getWindow().setBackgroundDrawableResource(R.drawable.splash);
            startAppDelay(SPLASH_DURATION_SHORT);

        } else {
            updateSplash(type, false);
            loadImage();
        }

    }

    private void loadImage() {
        ImageView splash = new ImageView(this);
        splash.setScaleType(ImageView.ScaleType.CENTER_CROP);
        setContentView(splash);
        String url = SpUtil.getString(SPLASH);
        if (url.isEmpty()) {
            Glide.with(this).load(R.drawable.splash).into(splash);
            updateSplash(type, false);
            startAppDelay(SPLASH_DURATION_SHORT);
        } else {
            Image image = DataBase.findImageByUrl(url);
            if (image == null) {
                Glide.with(this).load(R.drawable.splash).into(splash);
            } else {
                Imager.loadWithHeader(this, image, splash);
            }
            startAppDelay(SPLASH_DURATION_SHORT);
        }
    }

    private void startAppDelay(int delay) {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, delay);
    }

    @Override
    public void onBackPressed() {
        //disable back button when showing splash
    }

}
