package com.moko.support.task;

import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.AdvTxPowerTask
 */
public class AdvTxPowerTask extends OrderTask {

    public byte[] data;

    public AdvTxPowerTask(int responseType) {
        super(OrderType.advTxPower, OrderEnum.ADV_TX_POWER, responseType);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
