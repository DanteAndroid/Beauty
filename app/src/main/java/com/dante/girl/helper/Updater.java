package com.dante.girl.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import com.blankj.utilcode.utils.EncryptUtils;
import com.blankj.utilcode.utils.PhoneUtils;
import com.blankj.utilcode.utils.ToastUtils;
import com.dante.girl.BuildConfig;
import com.dante.girl.R;
import com.dante.girl.base.Constants;
import com.dante.girl.model.AppInfo;
import com.dante.girl.net.API;
import com.dante.girl.net.NetService;
import com.dante.girl.ui.SettingFragment;
import com.dante.girl.utils.AppUtil;
import com.dante.girl.utils.SpUtil;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.Arrays;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Update app helper.
 */

public class Updater {
    public static final String SHARE_APP = "share_app";
    private static final String TAG = "Updater";
    private static CompositeSubscription compositeSubscription = new CompositeSubscription();
    private final Activity context;
    private DownloadHelper helper;

    private Updater(Activity context) {
        this.context = context;
    }

    public static Updater getInstance(Activity context) {
        return new Updater(context);

    }

    private void downloadAndInstall(final AppInfo appInfo) {
        Subscription subscription = new RxPermissions(context)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .filter(granted -> granted)
                .subscribe(aBoolean -> {
                    String url = API.DOWNLOAD_BASE + "/" + appInfo.getVersion() +
                            "/" + appInfo.getApkName();
                    helper = new DownloadHelper(context, url);
                    helper.downWithDownloadManager(getApkName(appInfo.getVersion()), getApkName(appInfo.getFormerVersion()));
                }, throwable -> ToastUtils.showShortToast(throwable.getMessage()));
        compositeSubscription.add(subscription);
    }

    public void check() {
        Subscription subscription = new RxPermissions(context)
                .request(Manifest.permission.READ_PHONE_STATE)
                .subscribeOn(Schedulers.io())
                .subscribe(granted -> {
                    if (SpUtil.getString("deviceId").isEmpty()) {
                        if (granted) {
                            String device = EncryptUtils.encryptMD5ToString(PhoneUtils.getIMEI(), Constants.DANTE);
                            SpUtil.save("deviceId", device);
                        } else {
                            ToastUtils.showShortToast(R.string.permission_statement);
                        }
                    }

                });
        compositeSubscription.add(subscription);

        NetService.getInstance(API.GITHUB_RAW).getAppApi().getAppInfo()
                .filter(appInfo -> {
                    SpUtil.save("vip", Arrays.toString(appInfo.getVip().toArray()));
                    return appInfo.getVersionCode() > BuildConfig.VERSION_CODE;//版本有更新
                })
                .doOnNext(appInfo -> SpUtil.save(Updater.SHARE_APP, appInfo.getShareApp()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showDialog, Throwable::printStackTrace);
    }

    private void showDialog(final AppInfo appInfo) {
        boolean needUpdate = appInfo.isForceUpdate() || SpUtil.getBoolean(SettingFragment.CHECK_VERSION);
        new AlertDialog.Builder(context).setTitle(R.string.new_version)
                .setCancelable(!needUpdate)//需要更新就不可取消
                .setMessage(String.format(context.getString(R.string.update_message), appInfo.getMessage()))
                .setNeutralButton(R.string.go_market, (dialog, which) -> AppUtil.goMarket(context))
                .setPositiveButton(R.string.update, (dialog, which) -> downloadAndInstall(appInfo)).show();
    }

    private String getApkName(String version) {
        return "Beauty_" + version + ".apk";
    }

    public void release() {
        if (helper != null) {
            helper.release();
        }
        compositeSubscription.unsubscribe();
    }
}
