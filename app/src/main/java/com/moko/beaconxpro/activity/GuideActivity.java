package com.moko.beaconxpro.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import com.elvishew.xlog.XLog;
import com.moko.beaconxpro.R;
import com.moko.beaconxpro.dialog.PermissionDialog;
import com.moko.beaconxpro.utils.Utils;
import com.permissionx.guolindev.PermissionX;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;


/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.beaconxpro.activity.GuideActivity
 */
public class GuideActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            //申请存储权限 6-9的版本走这里 需要申请写SD卡权限和定位权限
            if (!Utils.isLocServiceEnable(this)) {
                showOpenLocationDialog();
                return;
            }
            if (!isWriteStoragePermissionOpen() || !isLocationPermissionOpen()) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION}, getResources().getString(R.string.permission_storage_need_content),
                        getResources().getString(R.string.permission_storage_close_content));
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            //判断GPS是否打开  10-11走这里 不再申请写SD权限 申请了也没用
            if (!Utils.isLocServiceEnable(this)) {
                showOpenLocationDialog();
                return;
            }
            //申请定位权限 BLUETOOTH BLUETOOTH_ADMIN不属于动态权限
            if (!isLocationPermissionOpen()) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, getResources().getString(R.string.permission_location_need_content),
                        getResources().getString(R.string.permission_location_close_content));
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //申请蓝牙、定位权限 12及以上版本还是需要位置权限 如果没有位置权限扫描的设备类型会受到限制  12以上版本走这里
            if (!Utils.isLocServiceEnable(this)) {
                showOpenLocationDialog();
                return;
            }
            if (!hasBlePermission() || !isLocationPermissionOpen()) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, getResources().getString(R.string.permission_ble_content),
                        getResources().getString(R.string.permission_ble_close_content));
                return;
            }
        }
        gotoMain();
    }

    private void gotoMain() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 1000);
    }

    private void requestPermissions(String[] permissions, String requestContent, String closeContent) {
        PermissionX.init(this).permissions(permissions).
                onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(new PermissionDialog(deniedList, requestContent))).
                onForwardToSettings((scope, deniedList) -> scope.showForwardToSettingsDialog(new PermissionDialog(deniedList, closeContent))).
                request((allGranted, grantedList, deniedList) -> {
                    XLog.i("333333" + allGranted);
                    if (allGranted) requestPermission();
                    else finish();
                });
    }

    @TargetApi(Build.VERSION_CODES.S)
    private boolean hasBlePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void showOpenLocationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.location_need_title)
                .setMessage(R.string.location_need_content)
                .setPositiveButton(getString(R.string.permission_open), (dialog1, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startLauncher.launch(intent);
                }).setNegativeButton(getString(R.string.cancel), (dialog12, which) -> finish())
                .create();
        dialog.show();
    }

    private final ActivityResultLauncher<Intent> startLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> requestPermission());
}
