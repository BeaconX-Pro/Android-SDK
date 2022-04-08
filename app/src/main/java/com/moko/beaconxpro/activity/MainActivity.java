package com.moko.beaconxpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.elvishew.xlog.XLog;
import com.moko.beaconxpro.R;
import com.moko.beaconxpro.dialog.AlertMessageDialog;
import com.moko.beaconxpro.utils.Utils;
import com.moko.bxp.nordic.activity.NordicMainActivity;
import com.moko.bxp.tla.activity.TLAMainActivity;


public class MainActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bxp);
        StringBuffer buffer = new StringBuffer();
        // 记录机型
        buffer.append("机型：");
        buffer.append(android.os.Build.MODEL);
        buffer.append("=====");
        // 记录版本号
        buffer.append("手机系统版本：");
        buffer.append(android.os.Build.VERSION.RELEASE);
        buffer.append("=====");
        // 记录APP版本
        buffer.append("APP版本：");
        buffer.append(Utils.getVersionInfo(this));
        XLog.d(buffer.toString());
    }

    public void onSelectBXPNordic(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, NordicMainActivity.class));
    }

    public void onSelectBXPTelink(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, TLAMainActivity.class));
    }

    @Override
    public void onBackPressed() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(R.string.main_exit_tips);
        dialog.setOnAlertConfirmListener(() -> MainActivity.this.finish());
        dialog.show(getSupportFragmentManager());
    }

    public void onAbout(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, AboutActivity.class));
    }
}
