package com.fudan.helper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.fudan.callingu.MyApplication;

import java.io.File;

/**
 * Created by FanJin on 2018/4/7.
 */

public class UpdateAppReceiver extends BroadcastReceiver {
    public UpdateAppReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 处理下载完成
        Cursor c = null;

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            if (DownloadAppUtils.downloadUpdateApkId >= 0) {
                long downloadId = DownloadAppUtils.downloadUpdateApkId;
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                DownloadManager downloadManager = (DownloadManager) context
                        .getSystemService(Context.DOWNLOAD_SERVICE);
                c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    int status = c.getInt(c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_FAILED) {
                        downloadManager.remove(downloadId);

                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        if (DownloadAppUtils.downloadUpdateApkFilePath != null) {
                            File file = new File(DownloadAppUtils.downloadUpdateApkFilePath);
                            Intent install = new Intent();
                            install.setAction(Intent.ACTION_VIEW);
                            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri uri = Uri.fromFile(file);
                            if (Build.VERSION.SDK_INT < 23) {
                                install.addCategory("android.intent.category.DEFAULT");
                            }else if (Build.VERSION.SDK_INT >= 24){
                                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);  // must get the permission!
                                uri = FileProvider.getUriForFile(MyApplication.getContext(), "com.fudan.callingu.provider", file); // FileProvider is required!
                            }
                            install.setDataAndType(uri, "application/vnd.android.package-archive");
                            context.startActivity(install);
                        }
                    }
                }
                c.close();
            }
        }
    }
}