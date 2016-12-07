#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <android/log.h>
#include "FiCe.h"
#include "FiFacial.h"
#include "StdDefEx.h"
#include "FiFacialStd.h"

static const char *TAG="Face_Identify";
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
//static const char* classname = "com/firs/facedetecttosvr/common/Face_Native";
static const char* classname = "cn/runvision/facedetect/utils/Face_Native";

/********************************************************************************
**  Copyright (c) 2012, 深圳市飞瑞斯科技有限公司
**  All rights reserved.
**
**  文件名称： faceLib.h
**  参考：飞瑞斯人脸识别
**
**  当前版本：1.0
**
**  创建作者：jason
**  创建日期:  2013.11.08
**
**  修改作者： 
**  修改日期: 
********************************************************************************/

#ifndef __FACE_LIB_H__
#define __FACE_LIB_H__

/* 该结构体为检测到的人眼、脸位置坐标 */
typedef struct __FacePositionData
{
    int iFaceLeft;          /* 脸左边X坐标 */
    int iFaceTop;           /* 脸上边Y坐标 */
    int iFaceRight;         /* 脸右边X坐标 */
    int iFaceBottom;        /* 脸下边Y坐标 */
    int iEyeLeftX;          /* 左眼X坐标 */
    int iEyeLeftY;          /* 左眼Y坐标 */
    int iEyeRightX;         /* 右眼X坐标 */
    int iEyeRightY;         /* 右眼Y坐标 */
    int iDistance;			/* 距离 1太远 －1太近 */
	int iMatchPos;			/* 识别分数 */
}FACE_POSITION_DATA_STR;

// 算法库句柄
typedef void* OD_CE_HANDLE;
typedef void* OD_CODEC_HANDLE;

typedef struct
{
	unsigned long id;
	int numMBLBP;
} USERINFO, *LPUSERINFO, **LLPUSERINFO;

#ifdef __cplusplus
extern "C" {
#endif

// 打开算法库引擎
OD_CE_HANDLE OD_OpenEngine();

OD_CODEC_HANDLE OD_OpenFacialCodec(OD_CE_HANDLE hCe, char* pStrAuxDir, char** ppFeatureInBuf, int* pnErrNo);


void OD_freeShareMem();

char *OD_mallocShareMem();

void OD_CloseFacialCodec(OD_CODEC_HANDLE hCodec);


// 关闭算法库引擎
void OD_CloseEngine(OD_CE_HANDLE hCe);


int OD_DetectFacial(OD_CODEC_HANDLE hCodec, char* pYBuf, int nWidth, int nHeight, FI_FACIAL_DETECTX_RESULT* pResult);


/* 打桩 */
int DM2016_Authentication(char* pEncryptedCode, char* pPlanCode);


/******************************************************************************
 * 函数名称： InitFaceLib
 * 功能： 初始化人脸库
 * 参数： piUserId: 用户id数组 iUserNum: 用户数
 * 返回： 无
 * 创建作者： Jason
 * 创建日期： 2013-11-27
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
void InitFaceLib(int* piUserId, int iUserNum);

/******************************************************************************
 * 函数名称： DeinitFaceLib
 * 功能： 释放人脸库资源
 * 参数： 无
 * 返回： 无
 * 创建作者： Jason
 * 创建日期： 2013-11-27
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
void DeinitFaceLib();

/******************************************************************************
 * 函数名称： GetFacePosition
 * 功能： 获取检测到的人脸的位置坐标
 * 参数： pFacePositionData： 			
 * 返回： 无；
 * 创建作者： 
 * 创建日期： 2012-12-3
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
void GetFacePosition(FACE_POSITION_DATA_STR *pFacePositionData);

/******************************************************************************
 * 函数名称： ClearFacePosition
 * 功能： 清掉人脸坐标信息
 * 参数： 
 * 返回： 
 * 创建作者： Jason
 * 创建日期： 2013-4-11
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
void ClearFacePosition();

/******************************************************************************
 * 函数名称： VerifyFace
 * 功能： 识别人脸 
 * 参数： puiMaxCompId:最大分数对应id，piMaxCompVal:最大分数，
          uiId:传入uiId为0时代表1:N，其它为1:1识别，输出分数及id
 * 返回： -255没检测到人脸，-1失败，0成功
 * 创建作者： Jason
 * 创建日期： 2013-11-20
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
int VerifyFace(unsigned int* puiMaxCompId, int* piMaxCompVal, unsigned int uiId);

/******************************************************************************
 * 函数名称： EnrollFace
 * 功能： 注册人脸
 * 参数： uiId:用户id，iMaxNum:最大注册张数
 * 返回： 已注册张数
 *        注册传入uiId > 0，当达到iMaxNum时存储特征并加载到算法内存,
 *        外部需判断返回值，如果达到最大值iMaxNum则结束注册人脸过程
 * 创建作者： Jason
 * 创建日期： 2013-11-20
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
int EnrollFace(unsigned int uiId, int iMaxNum);

extern char* getSvrIP();



JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_initFaceLib(JNIEnv* env, jobject thiz);
JNIEXPORT jintArray JNICALL Java_cn_runvision_utils_FaceNative_recognizeFace(JNIEnv* env, jobject thiz, jint width,
        jint height, jboolean isFront, jbyteArray imgData);
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_releaseFaceLib(JNIEnv* env, jobject thiz);


JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_initTcp( JNIEnv* env,jobject obj);
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_UserAuth( JNIEnv* env,jobject obj,jbyteArray username,jbyteArray pwd,jbyteArray mac);
//用户人脸登录
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_UserFaceAuth( JNIEnv* env,jobject obj,jbyteArray username,jbyteArray mac);
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_SetServerIP( JNIEnv* env,jobject obj, jbyteArray ip, jint iPort, jint ipChanged);
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getAuth( JNIEnv* env,jobject obj);
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getCompareFlag( JNIEnv* env,jobject obj);
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getPictureFaceFlag( JNIEnv* env,jobject obj);
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_sendPicture( JNIEnv* env,jobject obj);
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_setThreadExit( JNIEnv* env,jobject obj);

JNIEXPORT jobject  JNICALL Java_cn_runvision_utils_FaceNative_getCompareResult(JNIEnv * env, jobject obj, jobject classobj, int faceNum);

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getServerSockFlag( JNIEnv* env,jobject obj);

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_SetScore( JNIEnv* env,jobject obj, jint faceNum,jint iScore, jint iScore1to1);

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_ftpUpdateApp( JNIEnv* env,jobject obj);
//下载apk版本信息
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_ftpApkVersion( JNIEnv* env,jobject obj);

//发送1对1比对图片
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_sendPicture1T1( JNIEnv* env,jobject obj);

//拍摄人脸模板
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_takeMouldPic( JNIEnv* env,jobject obj);

JNIEXPORT int  JNICALL Java_cn_runvision_utils_FaceNative_get1T1CompareResult(JNIEnv * env, jobject obj);

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_ModifyPasswd( JNIEnv* env,jobject obj,jbyteArray username,jbyteArray oldpasswd, jbyteArray newpasswd);
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getModifyPasswdResult( JNIEnv* env,jobject obj);

//注册人脸
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_RegisterFace( JNIEnv* env,jobject obj);
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getRegisterFaceResult( JNIEnv* env,jobject obj);

#ifdef __cplusplus
}
#endif

#endif//__TEMPLATE_LIB_H__

