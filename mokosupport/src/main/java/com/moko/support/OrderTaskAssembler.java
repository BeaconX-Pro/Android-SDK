package com.moko.support;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.ParamsKeyEnum;
import com.moko.support.entity.SlotEnum;
import com.moko.support.task.GetAdvIntervalTask;
import com.moko.support.task.GetAdvSlotDataTask;
import com.moko.support.task.GetAdvTxPowerTask;
import com.moko.support.task.GetBatteryTask;
import com.moko.support.task.GetConnectableTask;
import com.moko.support.task.GetDeviceTypeTask;
import com.moko.support.task.GetFirmwareRevisionTask;
import com.moko.support.task.GetHardwareRevisionTask;
import com.moko.support.task.GetLockStateTask;
import com.moko.support.task.GetManufacturerNameTask;
import com.moko.support.task.GetModelNumberTask;
import com.moko.support.task.GetRadioTxPowerTask;
import com.moko.support.task.GetSerialNumberTask;
import com.moko.support.task.GetSlotTypeTask;
import com.moko.support.task.GetSoftwareRevisionTask;
import com.moko.support.task.GetUnlockTask;
import com.moko.support.task.ParamsTask;
import com.moko.support.task.ResetDeviceTask;
import com.moko.support.task.SetAdvIntervalTask;
import com.moko.support.task.SetAdvSlotDataTask;
import com.moko.support.task.SetAdvSlotTask;
import com.moko.support.task.SetAdvTxPowerTask;
import com.moko.support.task.SetConnectableTask;
import com.moko.support.task.SetLockStateTask;
import com.moko.support.task.SetRadioTxPowerTask;
import com.moko.support.task.SetUnlockTask;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class OrderTaskAssembler {

    /**
     * @Description 获取设备锁状态get lock state
     */
    public static OrderTask getLockState() {
        GetLockStateTask task = new GetLockStateTask();
        return task;
    }

    /**
     * @Description 设置设备锁方式
     */
    public static OrderTask setLockStateDirected(boolean isDirected) {
        SetLockStateTask task = new SetLockStateTask();
        task.setData(isDirected ? new byte[]{0x02} : new byte[]{0x01});
        return task;
    }

    /**
     * @Description 设置设备锁状态set lock state
     */
    public static OrderTask setLockState(String newPassword) {
        if (passwordBytes != null) {
            XLog.i("旧密码：" + MokoUtils.bytesToHexString(passwordBytes));
            byte[] bt1 = newPassword.getBytes();
            byte[] newPasswordBytes = new byte[16];
            for (int i = 0; i < newPasswordBytes.length; i++) {
                if (i < bt1.length) {
                    newPasswordBytes[i] = bt1[i];
                } else {
                    newPasswordBytes[i] = (byte) 0xff;
                }
            }
            XLog.i("新密码：" + MokoUtils.bytesToHexString(newPasswordBytes));
            // 用旧密码加密新密码
            byte[] newPasswordEncryptBytes = encrypt(newPasswordBytes, passwordBytes);
            if (newPasswordEncryptBytes != null) {
                SetLockStateTask task = new SetLockStateTask();
                byte[] unLockBytes = new byte[newPasswordEncryptBytes.length + 1];
                unLockBytes[0] = 0;
                System.arraycopy(newPasswordEncryptBytes, 0, unLockBytes, 1, newPasswordEncryptBytes.length);
                task.setData(unLockBytes);
                return task;
            }
        }
        return null;
    }

    /**
     * @Description 获取解锁加密内容get unlock
     */
    public static OrderTask getUnLock() {
        GetUnlockTask task = new GetUnlockTask();
        return task;
    }

    private static byte[] passwordBytes;

    /**
     * @Description 解锁set unlock
     */
    public static OrderTask setUnLock(String password, byte[] value) {
        byte[] bt1 = password.getBytes();
        passwordBytes = new byte[16];
        for (int i = 0; i < passwordBytes.length; i++) {
            if (i < bt1.length) {
                passwordBytes[i] = bt1[i];
            } else {
                passwordBytes[i] = (byte) 0xff;
            }
        }
        XLog.i("密码：" + MokoUtils.bytesToHexString(passwordBytes));
        byte[] unLockBytes = encrypt(value, passwordBytes);
        if (unLockBytes != null) {
            SetUnlockTask task = new SetUnlockTask();
            task.setData(unLockBytes);
            return task;
        }
        return null;
    }

    /**
     * @Date 2018/1/22
     * @Author wenzheng.liu
     * @Description 加密
     */
    public static byte[] encrypt(byte[] value, byte[] password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password, "AES");// 转换为AES专用密钥
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化为加密模式的密码器
            byte[] result = cipher.doFinal(value);// 加密
            byte[] data = Arrays.copyOf(result, 16);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description 获取通道类型
     */
    public static OrderTask getSlotType() {
        GetSlotTypeTask task = new GetSlotTypeTask();
        return task;
    }


    /**
     * @Description 获取设备类型
     */
    public static OrderTask getDeviceType() {
        GetDeviceTypeTask task = new GetDeviceTypeTask();
        return task;
    }

    /**
     * @Description 获取3轴参数
     */
    public static OrderTask getAxisParams() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_AXIS_PARAMS);
        return task;
    }

    /**
     * @Description 设置3轴参数
     */
    public static OrderTask setAxisParams(int rate, int scale, int sensitivity) {
        ParamsTask task = new ParamsTask();
        task.setAxisParams(rate, scale, sensitivity);
        return task;
    }

    /**
     * @Description 获取温湿度采样率
     */
    public static OrderTask getTHPeriod() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_TH_PERIOD);
        return task;
    }

    /**
     * @Description 设置温湿度采样率
     */
    public static OrderTask setTHPeriod(int period) {
        ParamsTask task = new ParamsTask();
        task.setTHPriod(period);
        return task;
    }

    /**
     * @Description 获取存储条件
     */
    public static OrderTask getStorageCondition() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_STORAGE_CONDITION);
        return task;
    }

    /**
     * @Description 设置存储条件
     */
    public static OrderTask setStorageCondition(int storageType, String storageData) {
        ParamsTask task = new ParamsTask();
        task.setStorageCondition(storageType, storageData);
        return task;
    }

    /**
     * @Description 获取设备时间
     */
    public static OrderTask getDeviceTime() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_DEVICE_TIME);
        return task;
    }

    /**
     * @Description 设置设备时间
     */
    public static OrderTask setDeviceTime(int year, int month, int day, int hour, int minute, int second) {
        ParamsTask task = new ParamsTask();
        task.setDeviceTime(year, month, day, hour, minute, second);
        return task;
    }

    public static OrderTask setTHEmpty() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.SET_TH_EMPTY);
        return task;
    }

    /**
     * @Description 获取设备MAC
     */
    public static OrderTask getDeviceMac() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_DEVICE_MAC);
        return task;
    }

    /**
     * @Description 获取连接状态
     */
    public static OrderTask getConnectable() {
        GetConnectableTask task = new GetConnectableTask();
        return task;
    }

    /**
     * @Description 设置连接状态
     */
    public static OrderTask setConnectable(boolean isConnectable) {
        SetConnectableTask task = new SetConnectableTask();
        task.setData(isConnectable ? MokoUtils.toByteArray(1, 1) : MokoUtils.toByteArray(0, 1));
        return task;
    }

    /**
     * @Description 获取按键关键
     */
    public static OrderTask getButtonPower() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_BUTTON_POWER);
        return task;
    }

    /**
     * @Description 设置按键关键
     */
    public static OrderTask setButtonPower(boolean enable) {
        ParamsTask task = new ParamsTask();
        task.setButtonPower(enable);
        return task;
    }

    /**
     * @Description 获取制造商
     */
    public static OrderTask getManufacturer() {
        GetManufacturerNameTask task = new GetManufacturerNameTask();
        return task;
    }

    /**
     * @Description 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        GetModelNumberTask task = new GetModelNumberTask();
        return task;
    }

    /**
     * @Description 获取生产日期
     */
    public static OrderTask getProductDate() {
        GetSerialNumberTask task = new GetSerialNumberTask();
        return task;
    }

    /**
     * @Description 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        GetHardwareRevisionTask task = new GetHardwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        GetFirmwareRevisionTask task = new GetFirmwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        GetSoftwareRevisionTask task = new GetSoftwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取电池电量
     */
    public static OrderTask getBattery() {
        GetBatteryTask task = new GetBatteryTask();
        return task;
    }

    /**
     * @Description 切换通道
     */
    public static OrderTask setSlot(SlotEnum slot) {
        SetAdvSlotTask task = new SetAdvSlotTask();
        task.setData(slot);
        return task;
    }

    /**
     * @Description 获取通道数据
     */
    public static OrderTask getSlotData() {
        GetAdvSlotDataTask task = new GetAdvSlotDataTask();
        return task;
    }

    /**
     * @Description 设置通道信息
     */
    public static OrderTask setSlotData(byte[] data) {
        SetAdvSlotDataTask task = new SetAdvSlotDataTask();
        task.setData(data);
        return task;
    }

    /**
     * @Description 获取信号强度
     */
    public static OrderTask getRadioTxPower() {
        GetRadioTxPowerTask task = new GetRadioTxPowerTask();
        return task;
    }

    /**
     * @Description 设置信号强度
     */
    public static OrderTask setRadioTxPower(byte[] data) {
        SetRadioTxPowerTask task = new SetRadioTxPowerTask();
        task.setData(data);
        return task;
    }

    /**
     * @Description 获取广播间隔
     */
    public static OrderTask getAdvInterval() {
        GetAdvIntervalTask task = new GetAdvIntervalTask();
        return task;
    }

    /**
     * @Description 设置广播间隔
     */
    public static OrderTask setAdvInterval(byte[] data) {
        SetAdvIntervalTask task = new SetAdvIntervalTask();
        task.setData(data);
        return task;
    }

    /**
     * @Description 设置广播强度
     */
    public static OrderTask setAdvTxPower(byte[] data) {
        SetAdvTxPowerTask advTxPowerTask = new SetAdvTxPowerTask();
        advTxPowerTask.setData(data);
        return advTxPowerTask;
    }

    /**
     * @Description 设置广播强度
     */
    public static OrderTask getAdvTxPower() {
        GetAdvTxPowerTask task = new GetAdvTxPowerTask();
        return task;
    }

    /**
     * @Description 获取iBeaconUUID
     */
    public static OrderTask getiBeaconUUID() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_IBEACON_UUID);
        return task;
    }

    /**
     * @Description 设置iBeaconUUID
     */
    public static OrderTask setiBeaconUUID(String uuidHex) {
        ParamsTask task = new ParamsTask();
        task.setiBeaconUUID(uuidHex);
        return task;
    }

    /**
     * @Description 获取iBeaconInfo
     */
    public static OrderTask getiBeaconInfo() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_IBEACON_INFO);
        return task;
    }

    /**
     * @Description 关机
     */
    public static OrderTask setClose() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.SET_CLOSE);
        return task;
    }

    /**
     * @Description 恢复出厂设置
     */
    public static OrderTask resetDevice() {
        ResetDeviceTask task = new ResetDeviceTask();
        return task;
    }

    public static OrderTask getTrigger() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_TRIGGER_DATA);
        return task;
    }

    public static OrderTask setTriggerClose() {
        ParamsTask task = new ParamsTask();
        task.setTriggerData();
        return task;
    }

    public static OrderTask setTHTrigger(int triggerType, boolean isAbove, int params, boolean isStart) {
        ParamsTask task = new ParamsTask();
        task.setTriggerData(triggerType, isAbove, params, isStart);
        return task;
    }

    public static OrderTask setTappedMovesTrigger(int triggerType, int params, boolean isStart) {
        ParamsTask task = new ParamsTask();
        task.setTriggerData(triggerType, params, isStart);
        return task;
    }

    public static OrderTask getHWResetEnable() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.GET_HW_RESET_ENABLE);
        return task;
    }

    public static OrderTask setHWResetEnable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setHWResetEnable(enable);
        return task;
    }
}
