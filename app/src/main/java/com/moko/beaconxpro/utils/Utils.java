package com.moko.beaconxpro.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.core.content.FileProvider;

public class Utils {

    /**
     * @Date 2017/4/6
     * @Author wenzheng.liu
     * @Description 发送邮件
     */
    public static void sendEmail(Context context, String address, String body, String subject, String tips, File... files) {
        if (files.length == 0) {
            return;
        }
        Intent intent;
        if (files.length == 1) {
            intent = new Intent(Intent.ACTION_SEND);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uri = IOUtils.insertDownloadFile(context, files[0]);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, "com.moko.beaconxpro.fileprovider", files[0]);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(files[0]);
            }
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.putExtra(Intent.EXTRA_TEXT, body);
        } else {
            ArrayList<Uri> uris = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Uri fileUri = IOUtils.insertDownloadFile(context, files[i]);
                    uris.add(fileUri);
                } else {
                    uris.add(Uri.fromFile(files[i]));
                }
            }
            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            ArrayList<CharSequence> charSequences = new ArrayList<>();
            charSequences.add(body);
            intent.putExtra(Intent.EXTRA_TEXT, charSequences);
        }
        String[] addresses = {address};
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.setType("message/rfc822");
        Intent.createChooser(intent, tips);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static String getVersionInfo(Context context) {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packInfo != null) {
            String version = packInfo.versionName;
            return String.format("%s", version);
        }
        return "";
    }

    /**
     * 手机是否开启位置服务，如果没有开启那么所有app将不能使用定位功能
     */
    public static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    /**
     * calendar转换成字符串时间
     *
     * @param calendar
     * @param pattern
     * @return
     */
    public static String calendar2strDate(Calendar calendar, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        return sdf.format(calendar.getTime());
    }
}
