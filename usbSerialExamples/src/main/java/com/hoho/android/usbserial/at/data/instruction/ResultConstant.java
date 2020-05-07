package com.hoho.android.usbserial.at.data.instruction;

/**
 * @Description:
 * @Author: yxf
 * @CreateDate: 2020/5/7 10:11
 * @UpdateUser: yxf
 * @UpdateDate: 2020/5/7 10:11
 */
public class ResultConstant {

    public static final String AT = "OK";
    public static final String AT_LOST = "OK+LOST";

    public static final String ROLE_START = "OK+Set:1";
    public static final String ROLE_END = "OK+Set:1";

    public static final String IMME_START = "OK+Set:1";
    public static final String IMME_END = "OK+Set:1";

    public static final String DISC_START = "OK+DISCS";
    public static final String DISC_END = "OK+DISCE";

    public static final String COON_START = "OK+CONNA";
    public static final String COON_END = "OK+CONN";

    public static final String COON_FAIL = "OK+CONNF";
    public static final String DIS_CONN = "OK+LOST";

    public static final String NOTIFY_SUCCESS = "OK+DATA-OK";
    public static final String NOTIFY_OFF = "OK+DATA-OK";

    public static final String SET_WAY = "OK+SEND-OK";

}
