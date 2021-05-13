package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.beaconxpro.R;
import com.moko.beaconxpro.able.ISlotDataAction;
import com.moko.beaconxpro.activity.SlotDataActivity;
import com.moko.beaconxpro.utils.ToastUtils;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.entity.TxPowerEnum;

import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IBeaconFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "IBeaconFragment";

    public static final String UUID_PATTERN = "[A-Fa-f0-9]{8}-(?:[A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12}";

    @BindView(R.id.sb_adv_tx_power)
    SeekBar sbAdvTxPower;
    @BindView(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R.id.et_major)
    EditText etMajor;
    @BindView(R.id.et_minor)
    EditText etMinor;
    @BindView(R.id.et_uuid)
    EditText etUuid;
    @BindView(R.id.tv_adv_tx_power)
    TextView tvAdvTxPower;
    @BindView(R.id.tv_tx_power)
    TextView tvTxPower;
    @BindView(R.id.et_adv_interval)
    EditText etAdvInterval;

    private Pattern pattern;

    private SlotDataActivity activity;

    public IBeaconFragment() {
    }

    public static IBeaconFragment newInstance() {
        IBeaconFragment fragment = new IBeaconFragment();
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
        View view = inflater.inflate(R.layout.fragment_ibeacon, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbAdvTxPower.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        pattern = Pattern.compile(UUID_PATTERN);
        //限制只输入大写，自动小写转大写
        etUuid.setTransformationMethod(new A2bigA());
        etUuid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!pattern.matcher(input).matches()) {
                    if (input.length() == 9 && !input.endsWith("-")) {
                        String show = input.substring(0, 8) + "-" + input.substring(8, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                    if (input.length() == 14 && !input.endsWith("-")) {
                        String show = input.substring(0, 13) + "-" + input.substring(13, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                    if (input.length() == 19 && !input.endsWith("-")) {
                        String show = input.substring(0, 18) + "-" + input.substring(18, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                    if (input.length() == 24 && !input.endsWith("-")) {
                        String show = input.substring(0, 23) + "-" + input.substring(23, input.length());
                        etUuid.setText(show);
                        etUuid.setSelection(show.length());
                    }
                }
            }
        });
        setDefault();
        return view;
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbAdvTxPower.setProgress(41);
            sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            etAdvInterval.setText(advIntervalProgress + "");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
                int advTxPowerProgress = activity.slotData.rssi_1m + 100;
                sbAdvTxPower.setProgress(advTxPowerProgress);
                advTxPowerBytes = MokoUtils.toByteArray(activity.slotData.rssi_1m, 1);
                tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_1m));
            } else if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                sbAdvTxPower.setProgress(41);
                advTxPowerBytes = MokoUtils.toByteArray(-59, 1);
                tvAdvTxPower.setText(String.format("%ddBm", -59));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                sbAdvTxPower.setProgress(advTxPowerProgress);
                advTxPowerBytes = MokoUtils.toByteArray(activity.slotData.rssi_0m, 1);
                tvAdvTxPower.setText(String.format("%ddBm", activity.slotData.rssi_0m));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            txPowerBytes = MokoUtils.toByteArray(activity.slotData.txPower, 1);
            tvTxPower.setText(String.format("%ddBm", activity.slotData.txPower));
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
            etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            StringBuilder stringBuilder = new StringBuilder(activity.slotData.iBeaconUUID);
            stringBuilder.insert(8, "-");
            stringBuilder.insert(13, "-");
            stringBuilder.insert(18, "-");
            stringBuilder.insert(23, "-");
            etUuid.setText(stringBuilder.toString().toUpperCase());
            etMajor.setSelection(etMajor.getText().toString().length());
            etMinor.setSelection(etMinor.getText().toString().length());
            etUuid.setSelection(etUuid.getText().toString().length());
        }
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

    private byte[] advIntervalBytes;
    private byte[] advTxPowerBytes;
    private byte[] txPowerBytes;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        upgdateData(seekBar.getId(), progress);
    }


    public void upgdateData(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_adv_tx_power:
                int advTxPower = progress - 100;
                tvAdvTxPower.setText(String.format("%ddBm", advTxPower));
                advTxPowerBytes = MokoUtils.toByteArray(advTxPower, 1);
                break;
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                int txPower = txPowerEnum.getTxPower();
                tvTxPower.setText(String.format("%ddBm", txPower));
                txPowerBytes = MokoUtils.toByteArray(txPower, 1);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private String major;
    private String minor;
    private String uuidHex;
    private byte[] iBeaconParamsBytes;

    @Override
    public boolean isValid() {
        String majorStr = etMajor.getText().toString();
        String minorStr = etMinor.getText().toString();
        String uuidStr = etUuid.getText().toString().replaceAll("-", "");
        String advInterval = etAdvInterval.getText().toString();
        if (TextUtils.isEmpty(majorStr) || TextUtils.isEmpty(minorStr) || TextUtils.isEmpty(uuidStr)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (Integer.valueOf(majorStr) > 65535 || Integer.valueOf(minorStr) > 65535 || uuidStr.length() != 32) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (TextUtils.isEmpty(advInterval)) {
            ToastUtils.showToast(activity, "The Adv Interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(activity, "The Adv Interval range is 1~100");
            return false;
        }
        major = String.format("%04X", Integer.valueOf(majorStr));
        minor = String.format("%04X", Integer.valueOf(minorStr));
        uuidHex = uuidStr;
        String iBeaconParamsHex = SlotFrameTypeEnum.IBEACON.getFrameType() + uuidHex + major + minor;
        iBeaconParamsBytes = MokoUtils.hex2bytes(iBeaconParamsHex);
        advIntervalBytes = MokoUtils.toByteArray(advIntervalInt * 100, 2);
        return true;
    }

    @Override
    public void sendData() {
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlot(activity.slotData.slotEnum));
        orderTasks.add(OrderTaskAssembler.setSlotData(iBeaconParamsBytes));
        orderTasks.add(OrderTaskAssembler.setRadioTxPower(txPowerBytes));
        orderTasks.add(OrderTaskAssembler.setAdvTxPower(advTxPowerBytes));
        orderTasks.add(OrderTaskAssembler.setAdvInterval(advIntervalBytes));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public class A2bigA extends ReplacementTransformationMethod {

        @Override
        protected char[] getOriginal() {
            char[] aa = {'a', 'b', 'c', 'd', 'e', 'f'};
            return aa;
        }

        @Override
        protected char[] getReplacement() {
            char[] cc = {'A', 'B', 'C', 'D', 'E', 'F'};
            return cc;
        }
    }

    @Override
    public void resetParams() {
        if (activity.slotData.frameTypeEnum == activity.currentFrameTypeEnum) {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            etAdvInterval.setText(advIntervalProgress + "");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            int advTxPowerProgress = activity.slotData.rssi_1m + 100;
            sbAdvTxPower.setProgress(advTxPowerProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);

            etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            StringBuilder stringBuilder = new StringBuilder(activity.slotData.iBeaconUUID);
            stringBuilder.insert(8, "-");
            stringBuilder.insert(13, "-");
            stringBuilder.insert(18, "-");
            stringBuilder.insert(23, "-");
            etUuid.setText(stringBuilder.toString().toUpperCase());
            etMajor.setSelection(etMajor.getText().toString().length());
            etMinor.setSelection(etMinor.getText().toString().length());
            etUuid.setSelection(etUuid.getText().toString().length());
        } else {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbAdvTxPower.setProgress(41);
            sbTxPower.setProgress(6);

            etMajor.setText("");
            etMinor.setText("");
            etUuid.setText("");
        }
    }
}
