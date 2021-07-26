package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;


public class ParamsTask extends OrderTask {
    public byte[] data;

    public ParamsTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(ParamsKeyEnum key) {
        switch (key) {
//            case GET_SLOT_TYPE:
            case GET_DEVICE_MAC:
//            case GET_DEVICE_NAME:
            case GET_CONNECTABLE:
            case GET_BUTTON_POWER:
            case GET_IBEACON_UUID:
            case GET_IBEACON_INFO:
            case SET_CLOSE:
            case GET_AXIS_PARAMS:
            case GET_TH_PERIOD:
            case GET_STORAGE_CONDITION:
            case GET_DEVICE_TIME:
            case SET_TH_EMPTY:
            case GET_TRIGGER_DATA:
            case GET_HW_RESET_ENABLE:
                createGetConfigData(key.getParamsKey());
                break;
        }
    }

    private void createGetConfigData(int configKey) {
        data = new byte[]{(byte) 0xEA, (byte) configKey, (byte) 0x00, (byte) 0x00};
    }

    public void setiBeaconData(int major, int minor, int advTxPower) {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_IBEACON_INFO.getParamsKey()) + "0005"
                + String.format("%04X", major) + String.format("%04X", minor) + MokoUtils.int2HexString(Math.abs(advTxPower));
        data = MokoUtils.hex2bytes(value);
    }

    public void setiBeaconUUID(String uuidHex) {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_IBEACON_UUID.getParamsKey()) + "0010"
                + uuidHex;
        data = MokoUtils.hex2bytes(value);
    }

    public void setConneactable(boolean isConnectable) {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_CONNECTABLE.getParamsKey()) + "0001"
                + (isConnectable ? "01" : "00");
        data = MokoUtils.hex2bytes(value);
    }

    public void setButtonPower(boolean enable) {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_BUTTON_POWER.getParamsKey()) + "0001"
                + (enable ? "01" : "00");
        data = MokoUtils.hex2bytes(value);
    }

    public void setAxisParams(int rate, int scale, int sensitivity) {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_AXIS_PARAMS.getParamsKey()) + "0003"
                + String.format("%02X", rate) + String.format("%02X", scale) + String.format("%02X", sensitivity);
        data = MokoUtils.hex2bytes(value);
    }

    public void setTHPriod(int period) {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_TH_PERIOD.getParamsKey()) + "0002"
                + String.format("%04X", period);
        data = MokoUtils.hex2bytes(value);
    }

    public void setStorageCondition(int storageType, String storageData) {
        String value = "00";
        switch (storageType) {
            case 0:
            case 1:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_STORAGE_CONDITION.getParamsKey()) + "0003"
                        + String.format("%02X", storageType) + storageData;
                break;
            case 2:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_STORAGE_CONDITION.getParamsKey()) + "0005"
                        + String.format("%02X", storageType) + storageData;
                break;
            case 3:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_STORAGE_CONDITION.getParamsKey()) + "0002"
                        + String.format("%02X", storageType) + storageData;
                break;
        }

        data = MokoUtils.hex2bytes(value);
    }

    public void setDeviceTime(int year, int month, int day, int hour, int minute, int second) {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_DEVICE_TIME.getParamsKey()) + "0006"
                + String.format("%02X", year) + String.format("%02X", month) + String.format("%02X", day)
                + String.format("%02X", hour) + String.format("%02X", minute) + String.format("%02X", second);
        data = MokoUtils.hex2bytes(value);
    }

    public void setTriggerData() {
        String value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_TRIGGER_DATA.getParamsKey()) + "000100";
        data = MokoUtils.hex2bytes(value);
    }

    public void setTriggerData(int triggerType, boolean isAbove, int params, boolean isStart) {
        String value = "00";
        byte[] paramsBytes = MokoUtils.short2Byte((short) params);
        switch (triggerType) {
            case 1:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_TRIGGER_DATA.getParamsKey()) + "0005"
                        + "01" + (isAbove ? "01" : "02") + MokoUtils.bytesToHexString(paramsBytes) + (isStart ? "01" : "02");
                break;
            case 2:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_TRIGGER_DATA.getParamsKey()) + "0005"
                        + "02" + (isAbove ? "01" : "02") + MokoUtils.bytesToHexString(paramsBytes) + (isStart ? "01" : "02");
                break;
        }
        data = MokoUtils.hex2bytes(value);
    }

    public void setTriggerData(int triggerType, int params, boolean isStart) {
        String value = "00";
        switch (triggerType) {
            case 3:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_TRIGGER_DATA.getParamsKey()) + "0004"
                        + "03" + String.format("%04X", params) + (isStart ? "01" : "02");
                break;
            case 4:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_TRIGGER_DATA.getParamsKey()) + "0004"
                        + "04" + String.format("%04X", params) + (isStart ? "01" : "02");
                break;
            case 5:
                value = "EA" + MokoUtils.int2HexString(ParamsKeyEnum.SET_TRIGGER_DATA.getParamsKey()) + "0004"
                        + "05" + String.format("%04X", params) + (isStart ? "02" : "01");
                break;
        }
        data = MokoUtils.hex2bytes(value);
    }

    public void setHWResetEnable(int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) ParamsKeyEnum.SET_HW_RESET_ENABLE.getParamsKey(),
                (byte) 0x00,
                (byte) 0x01,
                (byte) enable
        };
    }
}
