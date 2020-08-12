package com.moko.support.task;

import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.ConnectableTask
 */
public class ConnectableTask extends OrderTask {

    public byte[] data;

    public ConnectableTask(int responseType) {
        super(OrderType.connectable, OrderEnum.CONNECTABLE, responseType);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
