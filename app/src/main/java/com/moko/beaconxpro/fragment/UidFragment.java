package com.moko.beaconxpro.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.moko.support.MokoSupport;
import com.moko.support.entity.SlotFrameTypeEnum;
import com.moko.support.entity.TxPowerEnum;
import com.moko.support.utils.MokoUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UidFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "UidFragment";
    @Bind(R.id.et_namespace)
    EditText etNamespace;
    @Bind(R.id.et_instance_id)
    EditText etInstanceId;
    @Bind(R.id.sb_adv_tx_power)
    SeekBar sbAdvTxPower;
    @Bind(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @Bind(R.id.tv_adv_tx_power)
    TextView tvAdvTxPower;
    @Bind(R.id.tv_tx_power)
    TextView tvTxPower;
    @Bind(R.id.et_adv_interval)
    EditText etAdvInterval;

    private SlotDataActivity activity;

    public UidFragment() {
    }

    public static UidFragment newInstance() {
        UidFragment fragment = new UidFragment();
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
        View view = inflater.inflate(R.layout.fragment_uid, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbAdvTxPower.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        etNamespace.setTransformationMethod(new A2bigA());
        etInstanceId.setTransformationMethod(new A2bigA());
        setValue();
        return view;
    }

    private void setValue() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            etAdvInterval.setText("10");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            sbAdvTxPower.setProgress(100);
            sbTxPower.setProgress(6);
        } else {
            int advIntervalProgress = activity.slotData.advInterval / 100;
            etAdvInterval.setText(advIntervalProgress + "");
            etAdvInterval.setSelection(etAdvInterval.getText().toString().length());
            advIntervalBytes = MokoUtils.toByteArray(activity.slotData.advInterval, 2);

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                sbAdvTxPower.setProgress(100);
                advTxPowerBytes = MokoUtils.toByteArray(0, 1);
                tvAdvTxPower.setText(String.format("%ddBm", 0));
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
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.UID) {
            etNamespace.setText(activity.slotData.namespace);
            etInstanceId.setText(activity.slotData.instanceId);
            etNamespace.setSelection(etNamespace.getText().toString().length());
            etInstanceId.setSelection(etInstanceId.getText().toString().length());
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
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        ButterKnife.unbind(this);
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
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.UID) {
            upgdateData(seekBar.getId(), progress);
            activity.onProgressChanged(seekBar.getId(), progress);
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            upgdateData(seekBar.getId(), progress);
        }
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
    public void upgdateProgress(int viewId, int progress) {
        switch (viewId) {
            case R.id.sb_adv_tx_power:
                sbAdvTxPower.setProgress(progress);
                break;
            case R.id.sb_tx_power:
                sbTxPower.setProgress(progress);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private byte[] uidParamsBytes;

    @Override
    public boolean isValid() {
        String namespace = etNamespace.getText().toString();
        String instanceId = etInstanceId.getText().toString();
        String advInterval = etAdvInterval.getText().toString();
        if (TextUtils.isEmpty(namespace) || TextUtils.isEmpty(instanceId)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (namespace.length() != 20 || instanceId.length() != 12) {
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
        String uidParamsStr = activity.slotData.frameTypeEnum.getFrameType() + namespace + instanceId;
        uidParamsBytes = MokoUtils.hex2bytes(uidParamsStr);
        advIntervalBytes = MokoUtils.toByteArray(advIntervalInt, 2);
        return true;
    }

    @Override
    public void sendData() {
        MokoSupport.getInstance().sendOrder(
                // 切换通道，保证通道是在当前设置通道里
                activity.mMokoService.setSlot(activity.slotData.slotEnum),
                activity.mMokoService.setSlotData(uidParamsBytes),
                activity.mMokoService.setRadioTxPower(txPowerBytes),
                activity.mMokoService.setAdvTxPower(advTxPowerBytes),
                activity.mMokoService.setAdvInterval(advIntervalBytes)
        );
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
}
