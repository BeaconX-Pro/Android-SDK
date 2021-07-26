package com.moko.beaconxpro.activity;


import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.moko.beaconxpro.AppConstants;
import com.moko.beaconxpro.R;
import com.moko.beaconxpro.dialog.AlertMessageDialog;
import com.moko.beaconxpro.dialog.LoadingMessageDialog;
import com.moko.beaconxpro.entity.ValidParams;
import com.moko.beaconxpro.fragment.DeviceFragment;
import com.moko.beaconxpro.fragment.SettingFragment;
import com.moko.beaconxpro.fragment.SlotFragment;
import com.moko.beaconxpro.service.DfuService;
import com.moko.beaconxpro.utils.FileUtils;
import com.moko.beaconxpro.utils.ToastUtils;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.IdRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @BindView(R.id.frame_container)
    FrameLayout frameContainer;
    @BindView(R.id.radioBtn_slot)
    RadioButton radioBtnSlot;
    @BindView(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @BindView(R.id.radioBtn_device)
    RadioButton radioBtnDevice;
    @BindView(R.id.rg_options)
    RadioGroup rgOptions;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    private FragmentManager fragmentManager;
    private SlotFragment slotFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mPassword;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mIsClose;
    private ValidParams validParams;
    private int validCount;
    private int lockState;
    private boolean mReceiverTag = false;
    private int mDisconnectType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        validParams = new ValidParams();
        mPassword = getIntent().getStringExtra(AppConstants.EXTRA_KEY_PASSWORD);
        fragmentManager = getFragmentManager();
        initFragment();
        rgOptions.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getDeviceType());
            orderTasks.add(OrderTaskAssembler.getSlotType());
            orderTasks.add(OrderTaskAssembler.getDeviceMac());
            orderTasks.add(OrderTaskAssembler.getConnectable());
            orderTasks.add(OrderTaskAssembler.getManufacturer());
            orderTasks.add(OrderTaskAssembler.getDeviceModel());
            orderTasks.add(OrderTaskAssembler.getProductDate());
            orderTasks.add(OrderTaskAssembler.getHardwareVersion());
            orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
            orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
            orderTasks.add(OrderTaskAssembler.getBattery());
            orderTasks.add(OrderTaskAssembler.getLockState());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                if (mIsClose)
                    return;
                if (mDisconnectType > 0)
                    return;
                if (MokoSupport.getInstance().isBluetoothOpen()) {
                    if (isUpgrading) {
                        tvTitle.postDelayed(() -> {
                            dismissDFUProgressDialog();
                        }, 2000);
                    } else {
                        AlertMessageDialog dialog = new AlertMessageDialog();
                        dialog.setTitle("Dismiss");
                        dialog.setMessage("The device disconnected!");
                        dialog.setConfirm("Exit");
                        dialog.setCancelGone();
                        dialog.setOnAlertConfirmListener(() -> {
                            setResult(RESULT_OK);
                            finish();
                        });
                        dialog.show(getSupportFragmentManager());
                    }
                }
            }
            if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                // 设备连接成功，通知页面更新
                showSyncingProgressDialog();
                tvTitle.postDelayed(() -> MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState()), 1500);
            }
        });

    }


    private String unLockResponse;

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_DISCONNECT:
                        if (value.length >= 1) {
                            mDisconnectType = value[0] & 0xff;
                            if (mDisconnectType == 1 && isModifyPassword) {
                                isModifyPassword = false;
                                dismissSyncProgressDialog();
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setMessage("Modify password success!\nPlease reconnect the Device.");
                                dialog.setCancelGone();
                                dialog.setConfirm(R.string.ok);
                                dialog.setOnAlertConfirmListener(() -> {
                                    setResult(RESULT_OK);
                                    finish();
                                });
                                dialog.show(getSupportFragmentManager());
                            } else if (mDisconnectType == 2) {
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setMessage("Reset success!\nPlease reconnect the Device.");
                                dialog.setCancelGone();
                                dialog.setConfirm(R.string.ok);
                                dialog.setOnAlertConfirmListener(() -> {
                                    setResult(RESULT_OK);
                                    finish();
                                });
                                dialog.show(getSupportFragmentManager());
                            }
                        }
                        break;
                }
            }
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
                if (validParams.isEmpty() && validCount < 2) {
                    validCount++;
                    getDeviceInfo();
                } else {
                    validCount = 0;
                }
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_DEVICE_TYPE:
                        if (value.length >= 1) {
                            int deviceType = value[0] & 0xff;
                            slotFragment.setDeviceType(deviceType);
                            settingFragment.setDeviceType(deviceType);
                        }
                        break;
                    case CHAR_SLOT_TYPE:
                        if (value.length >= 6) {
                            slotFragment.updateSlotType(value);
                        }
                        break;
                    case CHAR_CONNECTABLE:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            if (value.length >= 1) {
                                settingFragment.setConnectable(value);
                                validParams.connectable = MokoUtils.byte2HexString(value[0]);
                            }
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                        }
                        break;
                    case CHAR_PARAMS:
                        if (value.length >= 2) {
                            int key = value[1] & 0xff;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(key);
                            if (configKeyEnum == null) {
                                return;
                            }
                            switch (configKeyEnum) {
                                case GET_DEVICE_MAC:
                                    if (value.length >= 10) {
                                        String valueStr = MokoUtils.bytesToHexString(value);
                                        String mac = valueStr.substring(valueStr.length() - 12).toUpperCase();
                                        String macShow = String.format("%s:%s:%s:%s:%s:%s", mac.substring(0, 2), mac.substring(2, 4), mac.substring(4, 6), mac.substring(6, 8), mac.substring(8, 10), mac.substring(10, 12));
                                        deviceFragment.setDeviceMac(macShow);
                                        mDeviceMac = macShow;
                                        validParams.mac = macShow;
                                    }
                                    break;
                                case GET_CONNECTABLE:
                                    if (value.length >= 5) {
                                        settingFragment.setConnectable(value);
                                        validParams.connectable = MokoUtils.byte2HexString(value[4]);
                                    }
                                    break;
                                case GET_BUTTON_POWER:
                                    if (value.length >= 5) {
                                        boolean enable = value[4] == 1;
                                        settingFragment.setButtonPower(enable);
                                    }
                                    break;
//                                    case GET_IBEACON_UUID:
//                                        if (value.length >= 20) {
//                                            slotFragment.setiBeaconUUID(value);
//                                        }
//                                        break;
//                                    case GET_IBEACON_INFO:
//                                        if (value.length >= 9) {
//                                            slotFragment.setiBeaconInfo(value);
//                                        }
//                                        break;
                                case SET_CLOSE:
                                    if ("eb260000".equals(MokoUtils.bytesToHexString(value).toLowerCase())) {
                                        ToastUtils.showToast(DeviceInfoActivity.this, "Success!");
                                        settingFragment.setClose();
                                        back();
                                    }
                                    break;
                                case GET_TRIGGER_DATA:
                                    if (value.length >= 4) {
                                        slotFragment.setTriggerData(value);
                                    }
                                    break;
                                case GET_HW_RESET_ENABLE:
                                    if (value.length >= 4) {
                                        int enable = value[4] & 0xFF;
                                        settingFragment.setHWResetEnable(enable);
                                    }
                                    break;

                            }
                        }
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        deviceFragment.setManufacturer(value);
                        validParams.manufacture = "1";
                        break;
                    case CHAR_MODEL_NUMBER:
                        deviceFragment.setDeviceModel(value);
                        validParams.productModel = "1";
                        break;
                    case CHAR_SERIAL_NUMBER:
                        // 判断新旧版本
                        String serialNumber = new String(value);
                        int year = Integer.parseInt(serialNumber.substring(0, 4));
                        MokoSupport.isNewVersion = year >= 2021;
                        deviceFragment.setProductDate(value);
                        validParams.manufactureDate = "1";
                        break;
                    case CHAR_HARDWARE_REVISION:
                        deviceFragment.setHardwareVersion(value);
                        validParams.hardwareVersion = "1";
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        deviceFragment.setFirmwareVersion(value);
                        validParams.firmwareVersion = "1";
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        deviceFragment.setSoftwareVersion(value);
                        validParams.softwareVersion = "1";
                        break;
                    case CHAR_BATTERY:
                        deviceFragment.setBattery(value);
                        validParams.battery = "1";
                        break;
                    case CHAR_ADV_SLOT_DATA:
                        if (value.length >= 1) {
                            slotFragment.setSlotData(value);
                        }
                        break;
                    case CHAR_RADIO_TX_POWER:
                        if (value.length >= 1) {
                            slotFragment.setTxPower(value);
                        }
                        break;
                    case CHAR_ADV_INTERVAL:
                        if (value.length >= 2) {
                            slotFragment.setAdvInterval(value);
                        }
                        break;
                    case CHAR_ADV_TX_POWER:
                        if (value.length >= 1) {
                            slotFragment.setAdvTxPower(value);
                        }
                        break;
                    case CHAR_LOCK_STATE:
                        String valueHexStr = MokoUtils.bytesToHexString(value);
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
//                            if ("eb63000100".equals(valueHexStr.toLowerCase())) {
//                                // 设备上锁
//                                if (isModifyPassword) {
//                                    isModifyPassword = false;
//                                    dismissSyncProgressDialog();
//                                    AlertMessageDialog dialog = new AlertMessageDialog();
//                                    dialog.setMessage("Modify password success!\nPlease reconnect the Device.");
//                                    dialog.setCancelGone();
//                                    dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
//                                        @Override
//                                        public void onClick() {
//                                            setResult(RESULT_OK);
//                                            back();
//                                        }
//                                    });
//                                    dialog.show(getSupportFragmentManager());
//                                }
//                            }
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            if ("01".equals(valueHexStr.toLowerCase())) {
                                lockState = 1;
                                settingFragment.setPasswordVerify(true);
                            } else if ("02".equals(valueHexStr.toLowerCase())) {
                                lockState = 2;
                                settingFragment.setPasswordVerify(false);
                            }
                            settingFragment.setModifyPasswordVisiable(!TextUtils.isEmpty(mPassword));
                        }
                        break;
                    case CHAR_UNLOCK:
                        if (responseType == OrderTask.RESPONSE_TYPE_READ) {
                            unLockResponse = MokoUtils.bytesToHexString(value);
                            XLog.i("返回的随机数：" + unLockResponse);
                            showSyncingProgressDialog();
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setUnLock(mPassword, value));
                        }
                        if (responseType == OrderTask.RESPONSE_TYPE_WRITE) {
                            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getLockState());
                        }
                        break;
//                    case CHAR_RESET_DEVICE:
//                        if (lockState == 2) {
//                            ToastUtils.showToast(DeviceInfoActivity.this, "Communication Timeout!");
//                        } else {
//                            ToastUtils.showToast(DeviceInfoActivity.this, "Reset successfully!\nPlease reconnect the Device.");
//                        }
//                        break;
                }
            }
        });
    }

    public void getSlotType() {
        showSyncingProgressDialog();
        tvTitle.postDelayed(() -> MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSlotType()), 1500);
    }

    private void getDeviceInfo() {
        showSyncingProgressDialog();
        validParams.reset();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
        orderTasks.add(OrderTaskAssembler.getConnectable());
        if (MokoSupport.isNewVersion) {
            orderTasks.add(OrderTaskAssembler.getButtonPower());
        }
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getProductDate());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getHWResetEnable());
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getLockState());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setTitle("Dismiss");
                            dialog.setCancelGone();
                            dialog.setMessage("The current system of bluetooth is not available!");
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            showSyncingProgressDialog();
                            showSyncingProgressDialog();
                            ArrayList<OrderTask> orderTasks = new ArrayList<>();
                            orderTasks.add(OrderTaskAssembler.getSlotType());
                            orderTasks.add(OrderTaskAssembler.getDeviceMac());
                            orderTasks.add(OrderTaskAssembler.getConnectable());
                            orderTasks.add(OrderTaskAssembler.getManufacturer());
                            orderTasks.add(OrderTaskAssembler.getDeviceModel());
                            orderTasks.add(OrderTaskAssembler.getProductDate());
                            orderTasks.add(OrderTaskAssembler.getHardwareVersion());
                            orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
                            orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
                            orderTasks.add(OrderTaskAssembler.getBattery());
                            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                            break;

                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FIRMWARE) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String firmwareFilePath = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(firmwareFilePath)) {
                    return;
                }
                final File firmwareFile = new File(firmwareFilePath);
                if (firmwareFile.exists()) {
                    final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                            .setDeviceName(mDeviceName)
                            .setKeepBond(false)
                            .setDisableNotification(true);
                    starter.setZip(null, firmwareFilePath);
                    starter.start(this, DfuService.class);
                    showDFUProgressDialog("Waiting...");
                } else {
                    Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @OnClick({R.id.tv_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
        }
    }

    private void back() {
        MokoSupport.getInstance().disConnectBle();
        mIsClose = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initFragment() {
        slotFragment = SlotFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, slotFragment)
                .add(R.id.frame_container, settingFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(slotFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();

    }

    private void showSlotFragment() {
        if (slotFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(settingFragment)
                    .hide(deviceFragment)
                    .show(slotFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.slot_title));
    }

    private void showSettingFragment() {
        if (settingFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(slotFragment)
                    .hide(deviceFragment)
                    .show(settingFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.setting_title));
    }

    private void showDeviceFragment() {
        if (deviceFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(slotFragment)
                    .hide(settingFragment)
                    .show(deviceFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.device_title));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioBtn_slot:
                showSlotFragment();
                getSlotType();
                break;
            case R.id.radioBtn_setting:
                showSettingFragment();
                getDeviceInfo();
                break;
            case R.id.radioBtn_device:
                showDeviceFragment();
                getDeviceInfo();
                break;
        }
    }

    private boolean isModifyPassword;

    public void modifyPassword(String password) {
        isModifyPassword = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setLockState(password));
    }

    public void resetDevice() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice());
    }


    public void setConnectable(boolean isConneacted) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setConnectable(isConneacted));
        orderTasks.add(OrderTaskAssembler.getConnectable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setButtonPower(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setButtonPower(enable));
        orderTasks.add(OrderTaskAssembler.getButtonPower());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setDirectedConnectable(boolean noPassword) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setLockStateDirected(noPassword));
        orderTasks.add(OrderTaskAssembler.getLockState());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setHWResetEnable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setHWResetEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getHWResetEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setClose() {
        mIsClose = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
    }

    public void chooseFirmwareFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertMessageDialog dialog = new AlertMessageDialog();
        if (isUpgradeCompleted) {
            dialog.setMessage("DFU Successfully!\nPlease reconnect the device.");
        } else {
            dialog.setMessage("Opps!DFU Failed.\nPlease try again!");
        }
        dialog.setCancelGone();
        dialog.setConfirm(R.string.ok);
        dialog.setOnAlertConfirmListener(() -> {
            isUpgrading = false;
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }


    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private int mDeviceConnectCount;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                ToastUtils.showToast(DeviceInfoActivity.this, "Error:DFU Failed");
                MokoSupport.getInstance().disConnectBle();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            isUpgrading = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }


        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            XLog.w("onDfuCompleted...");
            isUpgradeCompleted = true;
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            String progress = String.format("Progress:%d%%", percent);
            XLog.i(progress);
            mDFUDialog.setMessage(progress);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            XLog.i("DFU Error:" + message);
        }
    };
}
