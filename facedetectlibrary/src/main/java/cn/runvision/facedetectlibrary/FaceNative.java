package cn.runvision.facedetectlibrary;

public class FaceNative {
    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("jpeg");
        System.loadLibrary("face_identify");
    }

    /**
     * 初始化人脸识别算法库 return 返回执行结果
     */
    public static native void initFaceLib();

    /**
     * 释放初始化人脸识别算法库 return 返回执行结果
     */
    public static native void releaseFaceLib();

    /**
     * 
     * @param width
     * @param height
     * @param imgData
     * @param isFront 是否前置相机
     * @return int[] {int ret, left, top , right, bottom, rotate}
     *                              // =============== nEyeStatusThreshold>=0 返回睁闭眼分析结果=========================
                                // nEyeStatus==-1 检测失败;
                                // nEyeStatus==0  完全闭眼;
                                // nEyeStatus==1  双眼睁开;
                                // nEyeStatus==2  左眼睁开;
                                // nEyeStatus==3  右眼睁开;
     */
    public static native int[] recognizeFace(int width, int height, boolean isFront, byte[] imgData);
    
    //public static native int SendLoginMsg(byte[] username,byte[] pwd);
    public static native void initTcp();
    public static native void UserAuth(byte[] username, byte[] pwd,  byte[] mac);
    public static native void UserFaceAuth(byte[] username, byte[] mac);
    
    public static native void ModifyPasswd(byte[] username, byte[] oldPasswd, byte[] newPasswd);
    public static native int getModifyPasswdResult();
    
    public static native void RegisterFace();
    public static native int getRegisterFaceResult();
    
    public static native void SetServerIP(byte[] serverip, int iPort, int ipChanged);
    public static native int getAuth();
    public static native int getCompareFlag();
    public static native int getPictureFaceFlag();
    public static native void sendPicture();
    public static native void sendPicture1T1();
    public static native void takeMouldPic();
    public static native void setThreadExit();
    public static native compareresult getCompareResult(compareresult cmprest, int faceNum);
    public static native int get1T1CompareResult();
    public static native int getServerSockFlag();
    public static native void SetScore(int faceNum, int iScore, int iScore1to1);
    public static native int ftpUpdateApp();
    public static native int ftpApkVersion();

    
}
