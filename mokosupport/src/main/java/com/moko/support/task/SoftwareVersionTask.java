package com.moko.support.task;

import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;

/**
 * @Date 2018/1/20
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.task.SoftwareVersionTask
 */
public class SoftwareVersionTask extends OrderTask {

    public byte[] data;

    public SoftwareVersionTask() {
        super(OrderType.softwareVersion, OrderEnum.SOFTWARE_VERSION, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
