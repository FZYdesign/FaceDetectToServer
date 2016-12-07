package cn.runvision.facedetectlibrary;

/**
 * Created by Jant on 2016/11/4.
 */

public class FaceDetectSDK {


    //人脸图片上传

    /**
     * 上传人脸图片，
     * 保存人脸图片到本地
     */
    public void uploadFacePic(){

    }


    /**
     * 开始识别人脸
     * 返回成功，失败，超时
     * 保存成功和失败的识别记录
     */
    public void startRecognizeFaces(){

    }


    /**
     * 查询人脸识别记录，
     * 返回信息：现场抓拍的人脸图片，id卡上的人脸，识别结果，识别成功率，识别时间等
     */
    public void queryRecognizeRecords(){

    }




    //----------------------------------下面的这些方法是ndk方法的函数-------------------------------------------------

    /**
     * 初始化人脸识别算法库 return 返回执行结果
     */
    public  void initFaceLib(){

        FaceNative.initFaceLib();

    }

    /**
     * 释放初始化人脸识别算法库 return 返回执行结果
     */
    public void releaseFaceLib(){
        FaceNative.releaseFaceLib();
    }

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
    public  int[] recognizeFace(int width, int height, boolean isFront, byte[] imgData){
      return  FaceNative.recognizeFace(width,height,isFront,imgData);

    }

    //public  int SendLoginMsg(byte[] username,byte[] pwd){

    public  void initTcp(){
        FaceNative.initTcp();
    }
    public  void UserAuth(byte[] username, byte[] pwd,  byte[] mac){

        FaceNative.UserAuth(username,pwd,mac);
    }
    public  void UserFaceAuth(byte[] username, byte[] mac){
        FaceNative.UserFaceAuth(username,mac);
    }

    public  void ModifyPasswd(byte[] username, byte[] oldPasswd, byte[] newPasswd){

        FaceNative.ModifyPasswd(username,oldPasswd,newPasswd);
    }

    public  int getModifyPasswdResult(){
        return  FaceNative.getModifyPasswdResult();
    }

    public  void RegisterFace(){
        FaceNative.RegisterFace();
    }
    public  int getRegisterFaceResult(){
       return FaceNative.getRegisterFaceResult();
    }

    public  void SetServerIP(byte[] serverip, int iPort, int ipChanged){
        FaceNative.SetServerIP(serverip,iPort,ipChanged);
    }
    public  int getAuth(){
        return FaceNative.getAuth();
    }
    public  int getCompareFlag(){
        return FaceNative.getCompareFlag();
    }
    public  int getPictureFaceFlag(){
        return FaceNative.getPictureFaceFlag();
    }


    public  void sendPicture(){
        FaceNative.sendPicture();
    }
    public  void sendPicture1T1(){
        FaceNative.sendPicture1T1();
    }
    public  void takeMouldPic(){
        FaceNative.takeMouldPic();
    }

    public  void setThreadExit(){
        FaceNative.setThreadExit();
    }
    public  compareresult getCompareResult(compareresult cmprest, int faceNum){
        return FaceNative.getCompareResult(cmprest, faceNum);
    }
    public  int get1T1CompareResult(){
        return FaceNative.get1T1CompareResult();
    }
    public  int getServerSockFlag(){
        return FaceNative.getServerSockFlag();
    }
    public  void SetScore(int faceNum, int iScore, int iScore1to1){
        FaceNative.SetScore(faceNum, iScore, iScore1to1);
    }
    public  int ftpUpdateApp(){
        return FaceNative.ftpUpdateApp();
    }
    public  int ftpApkVersion(){
        return FaceNative.ftpApkVersion();
    }



}
