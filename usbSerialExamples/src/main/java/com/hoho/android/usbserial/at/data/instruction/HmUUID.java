package com.hoho.android.usbserial.at.data.instruction;

/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/4/30 14:22
 * @UpdateUser: yxf
 * @UpdateDate: 2020/4/30 14:22
 */
public enum HmUUID {

    /**
     * 获取序列号
     */
    CHARACTOR_SERIAL_NUM("2A25", "000E"),
    /**
     * 特征:体温，可读(v1.0及其以后)可订阅(v1.5及其以后)
     */
    CHARACTOR_TEMP("fff7", "0019"),
    /**
     * 特征:缓存温度
     */
    CHARACTOR_CACHE_TEMP("fff8", "001D"),
    /**
     * 特征:缓存温度   体温贴校准时候使用 关闭体温贴
     */
    CHARACTOR_CACHE_TEMP_SEND("fff9", "0021"),

    /**
     * 特征:设备版本号（可读）
     */
    CHARACTOR_VERSION("2a26", "0010");
    private String realCharacteristic;
    private String characteristicAlias;

    HmUUID(String realCharacteristic, String characteristicAlias) {
        this.realCharacteristic = realCharacteristic;
        this.characteristicAlias = characteristicAlias;
    }

    public String getRealCharacteristic() {
        return realCharacteristic;
    }

    public String getCharacteristicAlias() {
        return characteristicAlias;
    }
}
