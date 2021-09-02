package com.moko.beaconxpro.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.dialog.AlertMessageDialog;
import com.moko.bxp.nordic.activity.NordicMainActivity;
import com.moko.bxp.tla.activity.TLAMainActivity;


public class MainActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bxp);
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
