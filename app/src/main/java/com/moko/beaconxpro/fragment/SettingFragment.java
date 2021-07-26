package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.activity.AxisDataActivity;
import com.moko.beaconxpro.activity.DeviceInfoActivity;
import com.moko.beaconxpro.activity.THDataActivity;
import com.moko.beaconxpro.dialog.AlertMessageDialog;
import com.moko.beaconxpro.dialog.ModifyPasswordDialog;
import com.moko.ble.lib.utils.MokoUtils;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";
    @BindView(R.id.iv_connectable)
    ImageView ivConnectable;
    @BindView(R.id.iv_power)
    ImageView ivPower;
    @BindView(R.id.iv_button_power)
    ImageView ivButtonPower;
    @BindView(R.id.rl_password)
    RelativeLayout rlPassword;
    @BindView(R.id.iv_password_verify)
    ImageView ivPasswordVerify;
    @BindView(R.id.rl_axis)
    RelativeLayout rlAxis;
    @BindView(R.id.rl_th)
    RelativeLayout rlTh;
    @BindView(R.id.rl_reset_facotry)
    RelativeLayout rlResetFacotry;
    @BindView(R.id.iv_hw_reset)
    ImageView ivHwReset;
    @BindView(R.id.rl_hw_reset)
    RelativeLayout rlHwReset;

    private DeviceInfoActivity activity;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @OnClick({R.id.rl_password, R.id.rl_update_firmware, R.id.rl_reset_facotry, R.id.iv_connectable,
            R.id.iv_power, R.id.iv_button_power, R.id.iv_password_verify, R.id.rl_axis, R.id.rl_th,
            R.id.iv_hw_reset})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_password:
                final ModifyPasswordDialog modifyPasswordDialog = new ModifyPasswordDialog(activity);
                modifyPasswordDialog.setOnModifyPasswordClicked(new ModifyPasswordDialog.ModifyPasswordClickListener() {
                    @Override
                    public void onEnsureClicked(String password) {
                        activity.modifyPassword(password);
                    }
                });
                modifyPasswordDialog.show();
                Timer modifyTimer = new Timer();
                modifyTimer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                modifyPasswordDialog.showKeyboard();
                            }
                        });
                    }
                }, 200);
                break;
            case R.id.rl_update_firmware:
                activity.chooseFirmwareFile();
                break;
            case R.id.rl_reset_facotry:
                final AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
                resetDeviceDialog.setMessage("Are you sure to reset the device？");
                resetDeviceDialog.setConfirm(R.string.ok);
                resetDeviceDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                    @Override
                    public void onClick() {
                        activity.resetDevice();
                    }
                });
                resetDeviceDialog.show(activity.getSupportFragmentManager());
                break;
            case R.id.iv_connectable:
                if (isConneacted) {
                    final AlertMessageDialog connectAlertDialog = new AlertMessageDialog();
                    connectAlertDialog.setMessage("Are you sure to set the device non-connectable？");
                    connectAlertDialog.setConfirm(R.string.ok);
                    connectAlertDialog.setOnAlertConfirmListener(() -> {
                        activity.setConnectable(false);
                    });
                    connectAlertDialog.show(activity.getSupportFragmentManager());
                } else {
                    activity.setConnectable(true);
                }
                break;
            case R.id.iv_power:
                final AlertMessageDialog powerAlertDialog = new AlertMessageDialog();
                powerAlertDialog.setMessage("Are you sure to turn off the device?Please make sure the device has a button to turn on!");
                powerAlertDialog.setConfirm(R.string.ok);
                powerAlertDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                    @Override
                    public void onClick() {
                        activity.setClose();
                    }
                });
                powerAlertDialog.show(activity.getSupportFragmentManager());
                break;
            case R.id.iv_button_power:
                if (enableButtonPower) {
                    final AlertMessageDialog buttonPowerAlertDialog = new AlertMessageDialog();
                    buttonPowerAlertDialog.setMessage("If disable Button Power OFF, then it  cannot power off beacon by press button operation.");
                    buttonPowerAlertDialog.setConfirm(R.string.ok);
                    buttonPowerAlertDialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                        @Override
                        public void onClick() {
                            activity.setButtonPower(false);
                        }
                    });
                    buttonPowerAlertDialog.show(activity.getSupportFragmentManager());
                } else {
                    activity.setButtonPower(true);
                }
                break;
            case R.id.iv_password_verify:
                if (passwordVerify) {
                    final AlertMessageDialog directAlertDialog = new AlertMessageDialog();
                    directAlertDialog.setMessage("Are you sure to disable password verification？");
                    directAlertDialog.setConfirm(R.string.ok);
                    directAlertDialog.setOnAlertConfirmListener(() -> {
                        activity.setDirectedConnectable(true);
                    });
                    directAlertDialog.show(activity.getSupportFragmentManager());
                } else {
                    activity.setDirectedConnectable(false);
                }
                break;
            case R.id.rl_axis:
                // 3轴配置
                startActivity(new Intent(getActivity(), AxisDataActivity.class));
                break;
            case R.id.rl_th:
                // 温湿度
                startActivity(new Intent(getActivity(), THDataActivity.class));
                break;
            case R.id.iv_hw_reset:
                isHWResetEnable = !isHWResetEnable;
                activity.setHWResetEnable(isHWResetEnable);
                break;
        }
    }

    boolean isConneacted;

    public void setConnectable(byte[] value) {
        int connectable = Integer.parseInt(MokoUtils.byte2HexString(value[0]), 16);
        isConneacted = connectable == 1;
        if (connectable == 1) {
            ivConnectable.setImageResource(R.drawable.connectable_checked);
        } else {
            ivConnectable.setImageResource(R.drawable.connectable_unchecked);
        }
    }

    public void setClose() {
        ivPower.setImageResource(R.drawable.connectable_unchecked);
    }

    private boolean passwordVerify;

    public void setPasswordVerify(boolean passwordVerify) {
        this.passwordVerify = passwordVerify;
        ivPasswordVerify.setImageResource(passwordVerify ? R.drawable.connectable_checked : R.drawable.connectable_unchecked);
    }

    private boolean enableButtonPower;

    public void setButtonPower(boolean enable) {
        this.enableButtonPower = enable;
        ivButtonPower.setImageResource(enable ? R.drawable.connectable_checked : R.drawable.connectable_unchecked);
    }

    public void setModifyPasswordVisiable(boolean isSupportModifyPassword) {
        rlPassword.setVisibility(isSupportModifyPassword ? View.VISIBLE : View.GONE);
        rlResetFacotry.setVisibility(passwordVerify ? View.VISIBLE : View.GONE);
    }

    public void setDeviceType(int deviceType) {
        switch (deviceType) {
            case 0:
                rlAxis.setVisibility(View.GONE);
                rlTh.setVisibility(View.GONE);
                break;
            case 1:
                rlAxis.setVisibility(View.VISIBLE);
                rlTh.setVisibility(View.GONE);
                break;
            case 2:
                rlAxis.setVisibility(View.GONE);
                rlTh.setVisibility(View.VISIBLE);
                break;
            case 3:
                rlAxis.setVisibility(View.VISIBLE);
                rlTh.setVisibility(View.VISIBLE);
                break;
        }
    }

    private boolean isHWResetEnable;

    public void setHWResetEnable(int enable) {
        rlHwReset.setVisibility(View.VISIBLE);
        isHWResetEnable = enable == 1;
        ivHwReset.setImageResource(enable == 1 ? R.drawable.connectable_checked : R.drawable.connectable_unchecked);
    }
}
