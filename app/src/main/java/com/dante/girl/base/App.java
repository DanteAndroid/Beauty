package com.dante.girl.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.utils.Utils;
import com.bugtags.library.Bugtags;
import com.dante.girl.BuildConfig;
import com.dante.girl.utils.SpUtil;
import com.github.anrwatchdog.ANRWatchDog;
import com.squareup.leakcanary.LeakCanary;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

/**
 * Custom application for libs init etc.
 */
public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    private static final String TAG = "App";
    public static boolean noCache;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        noCache = SpUtil.getString(Constants.CACHE_STRATEGY, "0").equals("0");
        Log.d(TAG, "onCreate: " + SpUtil.getString(Constants.CACHE_STRATEGY, "0"));
        Bugtags.start("1ddf7128d535505cc4adbda213e8c12f", this, Bugtags.BTGInvocationEventNone);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        new ANRWatchDog().start();
        new ANRWatchDog().setANRListener(error -> {
            Bugtags.sendException(error.getCause());
        }).start();
        Utils.init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
//        Colorful.defaults()
//                .primaryColor(Colorful.ThemeColor.RED)
//                .accentColor(Colorful.ThemeColor.BLUE)
//                .translucent(false)
//                .dark(true);
//        Colorful.init(this);
    }
}
