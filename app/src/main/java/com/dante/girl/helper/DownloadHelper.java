package com.dante.girl.helper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.dante.girl.BuildConfig;
import com.dante.girl.R;
import com.dante.girl.base.App;

import java.io.File;


/**
 * Download and install apk file.
 */
public class DownloadHelper {
    private static final String TAG = "DownloadHelper";
    private BroadcastReceiver receiver;
    private String url;
    private String apkName;
    private Context context;
    private DownloadManager manager;
    private File apkFile;
    private long taskId;
    private File oldFile;
    private boolean isDownloading;


    public DownloadHelper(Context context, String url) {
        this.url = url;
        this.context = context;
    }

    private void startInstall() {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && apkFile.exists()) {
            install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            install.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            Uri apkUri = apkFile.exists() ? Uri.fromFile(apkFile) : manager.getUriForDownloadedFile(taskId);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }
        context.startActivity(install);
        if (BuildConfig.DEBUG) {
            Toast.makeText(App.context, R.string.install_hint, Toast.LENGTH_LONG).show();
        }
        isDownloading = false;//无需反注册
        if (receiver != null) context.unregisterReceiver(receiver);
    }

    public void downWithDownloadManager(String name, String oldName) {
        manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri resource = Uri.parse(url);
        if (TextUtils.isEmpty(name)) {
            apkName = url.substring(url.lastIndexOf('/') + 1);
        } else {
            apkName = name;
        }
        apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName);
        oldFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), oldName);
        if (oldFile.exists()) {
            boolean result = oldFile.delete();
            Log.i(TAG, "downWithDownloadManager:  old apk file deleted " + result);
        }
        if (apkFile.exists()) {
            Log.i(TAG, "downWithDownloadManager: exists" + apkFile.getPath());
            startInstall();
            return;
        }
        registerInstall();
        DownloadManager.Request request = new DownloadManager.Request(resource);
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, apkName);
        request.setDescription(context.getResources().getString(R.string.downloading));
        taskId = manager.enqueue(request);

    }

    private void registerInstall() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //可取得下载的id，适用与多个下载任务的监听
                startInstall();
                Log.i("test", "Downloaded id " + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
            }
        };
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        isDownloading = true;
    }

    public void release() {
        if (apkFile != null && apkFile.exists()) {
            boolean result = apkFile.delete();
            Log.i(TAG, "release:  apk file deleted " + result);
        }
        if (isDownloading && receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }
}
