package com.dante.girl.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.utils.CleanUtils;
import com.blankj.utilcode.utils.EncryptUtils;
import com.blankj.utilcode.utils.FileUtils;
import com.blankj.utilcode.utils.PhoneUtils;
import com.bugtags.library.Bugtags;
import com.dante.girl.R;
import com.dante.girl.base.App;
import com.dante.girl.base.Constants;
import com.dante.girl.model.DataBase;
import com.dante.girl.utils.AppUtil;
import com.dante.girl.utils.SpUtil;
import com.dante.girl.utils.UiUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * the view in setting activity.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    public static final String CLEAR_CACHE = "clear_cache";
    public static final String FEED_BACK = "feedback";
    public static final String CHECK_VERSION = "check_version";
    public static final String RANDOM_SPLASH = "random_splash";
    public static final String LIKE_DOWNLOAD = "like_download";
    public static final String SECRET_MODE = "secret_mode";
    public static final String THEME_COLOR = "theme_color";
    public static final String ABOUT = "about";
    private static final long DURATION = 300;
    private static final String TAG = "SettingFragment";
    public static final java.lang.String ERCIYUAN_HOME = "erciyuan_home";
    private Preference clearCache;
    private EditTextPreference feedback;
    private Preference version;
    private Preference splash;
    private Preference about;

    private View rootView;
    private long startTime;
    private boolean first = true;
    private int secretIndex;
    private ListPreference cacheStrategy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        clearCache = findPreference(CLEAR_CACHE);
        feedback = (EditTextPreference) findPreference(FEED_BACK);
        about = findPreference(ABOUT);
        version = findPreference(CHECK_VERSION);
        splash = findPreference(RANDOM_SPLASH);
//        theme = findPreference(THEME_COLOR);
        refreshCache();
        splash.setOnPreferenceChangeListener((preference, o) -> {
            SplashActivity.updateSplash((String) o, true);
            return true;
        });
        cacheStrategy = (ListPreference) findPreference(Constants.CACHE_STRATEGY);
        cacheStrategy.setOnPreferenceChangeListener((preference, newValue) -> {
            App.noCache = newValue.equals("0");
            splash.setEnabled(!newValue.equals("0"));
            return true;
        });

        about.setOnPreferenceClickListener(preference -> {
            new RxPermissions(getActivity()).request(Manifest.permission.READ_PHONE_STATE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(granted -> {
                        if (granted) {
                            return EncryptUtils.encryptMD5ToString(PhoneUtils.getIMEI(), Constants.DANTE);
                        }
                        return null;
                    }).subscribe(device -> {
                if (TextUtils.isEmpty(device)) {
                    UiUtils.showSnack(rootView, R.string.permission_statement);
                } else {
                    SpUtil.save("deviceId", device);
                    AppUtil.donate(getActivity());
                }
            });
            return true;
        });
        clearCache.setOnPreferenceClickListener(this);
        feedback.setOnPreferenceChangeListener((preference, newValue) -> {
            if (TextUtils.isEmpty((CharSequence) newValue)) return false;
            Bugtags.sendFeedback((String) newValue);
            UiUtils.showSnack(rootView, R.string.thanks_for_feedback);
            return true;
        });
    }

    private void refreshCache() {
        String cache = String.format(getString(R.string.set_current_cache) + " %s", getDataSize());
        clearCache.setSummary(cache);
    }

    private void secretStepTwo() {
        if (System.currentTimeMillis() - startTime < DURATION * (secretIndex + 1)) {
            if (secretIndex > 2) {
                Log.i("test", "splash " + secretIndex);
                secretIndex++;
            }
        }
        if (secretIndex == 6) {
            if (SpUtil.getBoolean(SECRET_MODE)) {
                SpUtil.save(SECRET_MODE, false);
                secretIndex = 0;
                UiUtils.showSnack(rootView, R.string.secret_mode_closed);
            } else {
                SpUtil.save(SECRET_MODE, true);
                secretIndex = 0;
                UiUtils.showSnackLong(rootView, R.string.secret_mode_opened);
            }
            secretIndex++;
        }
    }

    private void secretStepOne() {
        if (first) {
            startTime = System.currentTimeMillis();
            first = false;
        }
        if (System.currentTimeMillis() - startTime < DURATION * (secretIndex + 1)) {
            if (secretIndex < 3) {
                secretIndex++;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (null == rootView) {
            rootView = super.onCreateView(inflater, container, savedInstanceState);
        }
        return rootView;

    }

    private String getDataSize() {
        File file = App.context.getCacheDir();
        return FileUtils.getDirSize(file);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case CLEAR_CACHE:
                Observable.just(clearCache())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(success -> {
                            if (success) {
                                refreshCache();
                                Snackbar.make(rootView, R.string.clear_finished, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.deep_clean, view -> AppUtil.openAppInfo(getActivity())).show();
                            } else {
                                UiUtils.showSnack(rootView, R.string.clear_cache_failed);
                            }
                        });

                break;
        }
        return true;
    }

    private boolean clearCache() {
        DataBase.clearAllImages();
        return CleanUtils.cleanInternalCache();
    }

    private void sendEmailFeedback() {
        //This is wired, I used ACTION_SENDTO at first
        //but check intent returns unsafe
        //so I change to ACTION_VIEW (like the system do)
        Intent email = new Intent(Intent.ACTION_SENDTO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            email = new Intent(Intent.ACTION_VIEW);
        }
        if (AppUtil.isIntentSafe(email)) {
            email.setData(Uri.parse("mailto:danteandroi@gmail.com"));
            email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback");
            email.putExtra(Intent.EXTRA_TEXT, "Hi，");
            startActivity(email);
        } else {
            UiUtils.showSnack(rootView, R.string.email_not_install);
        }
    }

}
