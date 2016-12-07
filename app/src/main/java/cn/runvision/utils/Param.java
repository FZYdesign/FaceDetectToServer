package cn.runvision.utils;

public class Param {
    public final static String DEFAULT_NAME = "i light";
    public final static String DEFAULT_IP = "192.168.2.3";
    public final static String DEFAULT_PORT = "5000";
    public final static String DEFAULT_CHANNEL = "0";
    public final static String DEFALUT_WIFI_FILTER = "Christmas";  // "Christmas"; "R2WiFi";
    public final static String DEFAULT_MAC = "--";
    public final static int DEFAULT_ID = 0x8000;
    public final static short DEFAULT_MSG_ID = (short)0x8000;
    public final static byte DEFAULT_COMMAND = (short)0x00;
    public final static byte ALL_ON = 0x25;
    public final static byte ALL_OFF = 0x29;
    public final static byte ONE_ON = 0x28;
    public final static byte ONE_OFF = 0x2b;
    public final static byte TWO_ON = 0x2d;
    public final static byte TWO_OFF = 0x23;
    public final static byte THREE_ON = 0x27;
    public final static byte THREE_OFF = 0x2a;
    public final static byte FOUR_ON = 0x22;
    public final static byte FOUR_OFF = 0x26;
    public final static byte SPEED_ADD = 0x2c;  // 速度加
    public final static byte SPEED_DEL = 0x24;  // 速度减
    public final static byte MODEL = 0x2e;      // 模式

    public final static byte DEFAULT_LIGHT = 0x20;
    public final static byte BASE_LIGHT = 0x10;
    public final static byte MAX_LIGHT = 15;
    public final static byte MIN_LIGHT = 0;

    public final static byte DEFAULT_COLOR = (byte)0x80;
    public final static byte MAX_COLOR = 0x7F;
    public final static byte MIN_COLOR  = 0x40;

    public final static byte DEFAULT_SPEED = (byte)0x03;
    public final static byte MIN_SPEED = (byte)0x01;
    public final static byte MAX_SPEED = (byte)0x09;

    public final static int MAX_ROTATE = 359;
    public final static int MIN_ROTATE = 0;

    public final static int MAX_MODEL = 0x08;
    public final static int MIN_MODEL = 0x00;
}
