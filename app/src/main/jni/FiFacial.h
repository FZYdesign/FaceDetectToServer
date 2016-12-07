
#ifndef _FI_FACIAL_H
#define _FI_FACIAL_H

#include "StdDefEx.h"
#include "FiFacialStd.h"

#ifdef __cplusplus
extern "C" {
#endif

#if defined(WIN32)
	#if defined(LIB_EXPORTS)	// 动态库
		#define FI_FACIAL_API __declspec(dllexport)
	#else
		#if defined(_LIB)		// 静态库
			#define FI_FACIAL_API 
		#elif defined(_VRE)
			#define FI_FACIAL_API 
		#else
			#define FI_FACIAL_API __declspec(dllimport)
		#endif
	#endif
#else
		#define FI_FACIAL_API 
#endif

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//									初始化/反初始化环境
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// 全局仅需要调用一次，即在系统初始化和在系统退出时各调用一次
FI_FACIAL_API FI_BOOL FI_FACIAL_InitContext();
FI_FACIAL_API void FI_FACIAL_UninitContext();

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//									通道基本函数
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// 设置特征共享缓冲区大小（默认是20M）
// 必须在FI_FACIAL_OpenCodec 之前调用方有效
// 目前允许设置的大小是>0 && <=20
FI_FACIAL_API FI_BOOL FI_FACIAL_SetInFeatureBufSize(int buf_size_m/*单位是M*/);

// 打开Codec，成功返回0，失败返回-1
FI_FACIAL_API FI_HANDLE FI_FACIAL_OpenCodec(FI_HANDLE hFiCe, char authKeys[32]);

// 初始化Codec
// 返回值:
// ==0 初始化失败
// ==1 初始化成功，并且认证通过
// ==2 初始化成功，强制版权保护	
FI_FACIAL_API int FI_FACIAL_InitCodec(FI_HANDLE hFiCodec, char randkeys[32]);

// 关闭Codec
FI_FACIAL_API void FI_FACIAL_CloseCodec(FI_HANDLE hFiCodec);

// 设置辅助目录
// 放置模型数据等，
// 如果没有设置，则默认为当前路径，
// 在FI_FACIAL_OpenCodec 之后，在FI_FACIAL_InitCodec  之前调用。
FI_FACIAL_API void FI_FACIAL_SetAuxDir(FI_HANDLE hFiCodec, char strAuxDir[256]);

// 获取算法版本号
// 版本号定义格式:  ModelMainVers . ModelSubVers . ProjDiffVers . Reserve
// 由于arm  使用小端序，故得到的unsigned int  version 解析如下:
// ModelMainVers 	= (unsigned char)(version>>0);
// ModelSubVers 	= (unsigned char)(version>>8);
// ProjDiffVers 	= (unsigned char)(version>>16);
// Reserve 		= (unsigned char)(version>>24);
// 如果返回的版本号是0，则表示获取版本号失败
FI_FACIAL_API unsigned int FI_FACIAL_GetVersion(FI_HANDLE hFiCodec);

// 获取算法序列号
// 返回值是SN  的字符个数
// 如果返回的SN  字符个数是0，则表示获取序列号失败
FI_FACIAL_API int FI_FACIAL_GetAlgorSN(FI_HANDLE hFiCodec, char strSN[256]);


// 获取输入特征共享缓冲区( 20 M )   地址
FI_FACIAL_API char * FI_FACIAL_GetInFeatureBuf(FI_HANDLE hFiCodec);


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//									人脸人眼检测
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// 人脸信息检测(  红外通用版本)
FI_FACIAL_API int FI_FACIAL_Detect_IR_GN(FI_HANDLE hFiCodec, const char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pResult);

// 人脸信息检测(  红外快速版本)
FI_FACIAL_API int FI_FACIAL_Detect_IR_FP(FI_HANDLE hFiCodec, const char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pResult);

// 人脸信息检测(  可见光通用版本)
FI_FACIAL_API int FI_FACIAL_Detect_VR_GN(FI_HANDLE hFiCodec, const char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pResult);

// 人脸信息检测(  可见光快速版本)
FI_FACIAL_API int FI_FACIAL_Detect_VR_FP(FI_HANDLE hFiCodec, const char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pResult);


//
// 人脸信息检测
// 返回值:
//	== -1 	检测失败
// 	>=  0	检测到的人脸个数
//
// FI_FACIAL_DETECT_WAY_INVALID        无效
// FI_FACIAL_DETECT_WAY_IR_GN		红外人脸检测普通版本
// FI_FACIAL_DETECT_WAY_IR_FP		红外人脸检测快速版本
// FI_FACIAL_DETECT_WAY_VR_GN		可见光人脸检测普通版本
// FI_FACIAL_DETECT_WAY_VR_FP		可见光人脸检测快速版本
// FI_FACIAL_DETECT_WAY_EyeStatus	专用眼睛闭合状态检测(混合版本，重在提高质量)
//							人眼闭合状态检测专用版本，使用可见光人脸+  红外人眼+  红外人眼状态
FI_FACIAL_API int FI_FACIAL_DetectX(FI_HANDLE hFiCodec, const char* pYBuf, const FI_FACIAL_DETECT_PARAM* pDetectParam, FI_FACIAL_DETECTX_RESULT *pResult/*, unsigned char * pIcon*/);

typedef enum FI_FACIAL_CROP_ICON_TYPE
{
	FI_FACIAL_CROP_FACE_ICON_FOR_DETECT,		/* 人脸检测时的人脸小图，分辨率是120x142 */
	FI_FACIAL_CROP_FACE_ICON_FOR_EYESTATUS,	/* 疲劳检测时的人脸小图，分辨率是120x142 */
	FI_FACIAL_CROP_FACE_ICON_FOR_FEATURE1,		/* 提取特征时的人脸小图，分辨率是120x142 */
	FI_FACIAL_CROP_FACE_ICON_FOR_FEATURE2,		/* 提取特征时的人脸小图，分辨率是128x128 */
	FI_FACIAL_CROP_EYE_ICON_FOR_EYESTATUS,		/* 疲劳检测时的眼睛小图，分辨率是24x12 */
}FI_FACIAL_CROP_ICON_TYPE;

typedef struct FI_FACIAL_CROP_ICON_PARAM
{
	int width;							/* 图像宽度*/
	int height;							/* 图像高度*/
	FI_RECT  rtFace;						/* 人脸坐标*/
	FI_POINT ptLeftEye;					/* 左眼坐标*/
	FI_POINT ptRightEye;					/* 右眼坐标*/
	FI_POINT ptMouth;					/* 右眼坐标*/
	FI_FACIAL_CROP_ICON_TYPE type;		/* 裁剪类型*/
}FI_FACIAL_CROP_ICON_PARAM;

FI_FACIAL_API FI_BOOL FI_FACIAL_CropIcon(FI_HANDLE hFiCodec, const unsigned char* pYBuf, FI_FACIAL_CROP_ICON_PARAM param, unsigned char* pIconBuf, int* nIconWidth, int* nIconHeight);


//// old，don't use
//// 本接口已经过时，仅仅是兼容以前老版本
static int FI_FACIAL_Detect(FI_HANDLE hFiCodec, const char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pResult)
{
	return FI_FACIAL_Detect_IR_GN(hFiCodec, pYBuf, nWidth, nHeight, pResult);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//									人脸对象跟踪
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////
// 函数名称：FI_FACIAL_TrackX
// 函数描述：人脸对象跟踪
// 输入参数：
//				hFiCodec		通道句柄（FI_HANDLE）
//				pBufYUV420	图像数据( RGB  格式)
//				detectParam	检测参数
// 输出参数：
//				pResult		输出跟踪到的人脸对象信息
// 返回结果：
//				==-1 调用失败; >=0  跟踪到的对象个数
// 附加说明：
//				调用本接口会进行人脸对象跟踪，并返回所有跟踪到的人脸对象信息。
//				每次返回的对象包括实时对象和警报对象；
//				实时对象:  即当前帧有效跟踪的对象，此时调用FI_FACIAL_GetNewlyPhoto  接口获取实时照片；
//				警报对象:  即已经失踪的对象，此时调用FI_FACIAL_GetBestPhoto  接口获取最佳照片；
//				通过FI_FACIAL_OBJ_INFO  结构的bAlarm  来判断对象是实时对象还是警报对象。
//				
//				FI_FACIAL_OBJ_INFO objInfo;
//				if (objInfo.bAlarm)
//				{
//					// 警报对象
//
//					// 获取最佳照片，用于保存
//					FI_FACIAL_GetBestPhoto(...);
//				}
//				else
//				{
//					// 实时对象
//					
//					// 获取实时照片，用于显示
//					FI_FACIAL_GetNewlyPhoto(...);
//				}
//
///////////////////////////////////////////////////////////
FI_FACIAL_API int FI_FACIAL_TrackX(FI_HANDLE hFiCodec, const char* pBufYUV420, const FI_FACIAL_DETECT_PARAM* pDetectParam, FI_FACIAL_TRACKX_RESULT *pResult);

///////////////////////////////////////////////////////////
// 函数名称：FI_FACIAL_GetNewlyPhoto
// 函数描述：获取人脸对象最新照片
// 输入参数：
//				hFiCodec		通道句柄（FI_HANDLE）
//				nObjID			对象ID
// 输出参数：
//				pImageData		输出照片数据，为 RGB 24 格式，空间由调用者分配和释放，调用前必须分配足够大小的空间
//				nImageWidth		输出照片宽度
//				nImageHeight	输出照片高度
// 返回结果：
//				是否成功：==TRUE 成功；==FALSE 失败；
// 附加说明：
///////////////////////////////////////////////////////////
FI_FACIAL_API FI_BOOL FI_FACIAL_GetNewlyPhoto(FI_HANDLE hFiCodec, int nObjID, 
													unsigned char *pImageData, int *nImageWidth, int *nImageHeight);


///////////////////////////////////////////////////////////
// 函数名称：FI_FACIAL_GetBestPhoto
// 函数描述：获取人脸对象最佳照片
// 输入参数：
//				hFiCodec		通道句柄（FI_HANDLE）
//				nObjID			对象ID
// 输出参数：
//				pImageData		输出照片数据，为 RGB 24 格式，空间由调用者分配和释放，调用前必须分配足够大小的空间
//				nImageWidth		输出照片宽度
//				nImageHeight	输出照片高度
// 返回结果：
//				是否成功：==TRUE 成功；==FALSE 失败；
// 附加说明：
///////////////////////////////////////////////////////////
FI_FACIAL_API FI_BOOL FI_FACIAL_GetBestPhoto(FI_HANDLE hFiCodec,  int nObjID,
									   				unsigned char *pImageData, int *nImageWidth, int *nImageHeight);

///////////////////////////////////////////////////////////
// 函数名称：FI_FACIAL_GetBestPhotoBackground
// 函数描述：获取人脸对象最佳照片背景图像
// 输入参数：
//				hFiCodec		通道句柄（FI_HANDLE）
//				nObjID			对象ID
// 输出参数：
//				pImageData		输出照片数据，为 RGB 24 格式，空间由调用者分配和释放，调用前必须分配足够大小的空间
//				nImageWidth		输出照片宽度
//				nImageHeight	输出照片高度
// 返回结果：
//				是否成功：==TRUE 成功；==FALSE 失败；
// 附加说明：
///////////////////////////////////////////////////////////
FI_FACIAL_API FI_BOOL FI_FACIAL_GetBestPhotoBackground(FI_HANDLE hFiCodec,  int nObjID,
									   				unsigned char *pImageData, int *nImageWidth, int *nImageHeight);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//									人眼状态检测
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// 红外人眼状态检测
// *pEyeStatus
// 		==0 完全闭眼
//		==1 双眼睁开
//		==2 左眼睁开
//		==3 右眼睁开
// 红外人脸闭合状态检测采用新算法原理，效果比可见光版本(比较旧的算法原理) 好
FI_FACIAL_API int FI_FACIAL_DetectEyeStatusIR(FI_HANDLE hFiCodec, char* pYBuf, int nWidth, int nHeight, FI_POINT ptLeftEye, FI_POINT ptRightEye, int *pEyeStatus);

// 可见光人眼状态检测
// *pEyeStatus
// 		==0 完全闭眼
//		==1 双眼睁开
//		==2 左眼睁开
//		==3 右眼睁开
FI_FACIAL_API int FI_FACIAL_DetectEyeStatusVR(FI_HANDLE hFiCodec, char* pYBuf, int nWidth, int nHeight, FI_POINT ptLeftEye, FI_POINT ptRightEye, int *pEyeStatus);


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//									特征提取比对
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

// JNM 人脸特征
/***************************************************************
 * 返回的数据:
 * 1. 提取的特征数据(1024 Byte)
 * 2. 传递进来的灰度图(width x height)
 * 3. ICON1, 裁剪的初始小图(120 x 142)
 * 4. ICON2, 经过直方图滤波的小图(120 x 142)
 返回值:
 ==0    成功
 ==-1  逻辑异常失败
 ==-2  照片攻击失败(目前仅仅支持FI_FACIAL_GetJNMFeature  接口)
 ****************************************************************
*/ 
FI_FACIAL_API int FI_FACIAL_GetJNMFeature(FI_HANDLE hFiCodec, const char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pFacial, char* pFeatureJNM, int nFeatureBufSize);
FI_FACIAL_API int FI_FACIAL_GetJNMFeatureAdjust(FI_HANDLE hFiCodec, const char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pFacial, char* pFeatureJNM, int nFeatureBufSize, float fBrightnessReference, int nAdjust);
FI_FACIAL_API int FI_FACIAL_GetJNMFeature_FromIcon(FI_HANDLE hFiCodec, const char* pIconYBuf, char* pFeatureJNM);

// 提取粗糙特征，用于特征训练
FI_FACIAL_API FI_BOOL FI_FACIAL_GetJNMRawFeature(FI_HANDLE hFiCodec, char* pYBuf, int nWidth, int nHeight, LPFI_FACIAL_FACE_INFO pFacial, FI_FACIAL_MBLBP_RAW_FEA_STG* pJNMRawFea);

// 输入二进制特征
FI_FACIAL_API float FI_FACIAL_CompareJNMFeature(FI_HANDLE hFiCodec, char* pCurFeature, char* pRefFeature);

// 输入原始char  特征
FI_FACIAL_API float FI_FACIAL_CompareJNMFeatureByRawFea(FI_HANDLE hFiCodec, char* pCurFeature, char* pRefFeature, int nFeaLength);

/*
	pCurFeature = Feature(1024Byte) + UserFeaNum(1Byte) + UserFeaNum(1Byte) + UserFeaNum(1Byte) + UserFeaNum(1Byte) ...
	pRefFeature = Feature(1024Byte) + Feature(1024Byte) + Feature(1024Byte) + Feature(1024Byte) + Feature(1024Byte) + ...
	UserFeaNum 表示pRefFeature  存储的参考特征数组中，第几个用户拥有几个参考特征。
*/
FI_FACIAL_API int FI_FACIAL_CompareJNMFeatureMore(FI_HANDLE hFiCodec, char* pCurFeature, char* pRefFeature, int nRefFeatureNum, LPFI_FACIAL_COMPARE_FEATURE_RESULT pResult);
FI_FACIAL_API int FI_FACIAL_Proj_Comp(FI_HANDLE hFiCodec, char* pDataInBuf, char* pCompResult);

//
FI_FACIAL_API int FI_FACIAL_CompareJNMFeatureX(FI_HANDLE hFiCodec, LPFI_FACIAL_COMPARE_FEATURE_PARAM_X pParam, char* pRefFeatureBuf, LPFI_FACIAL_COMPARE_FEATURE_RESULT_X pResult);


/////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
//
//									数字图像处理
//
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 

// 图像处理
FI_FACIAL_API int FI_FACIAL_Rolate_Yuv422_D1_To_Y_VGA_AND_SIF(FI_HANDLE hFiCodec, char* pD1Yuv422, char* pVga, char* pSif);
FI_FACIAL_API int FI_FACIAL_Rolate_Yuv422_D1_To_YUV420P_SIF(FI_HANDLE hFiCodec, char* pD1Yuv422, char* pSif, int nAngle, int bMirror, int nOffsetX, int nOffsetY, int nZoom);
FI_FACIAL_API int FI_FACIAL_Rolate_Yuv422_D1_To_YUV420P_2SIF(FI_HANDLE hFiCodec, char* pD1Yuv422, char* pSif1, char* pSif2, int nOffsetX, int nOffsetY, int nZoom);

FI_FACIAL_API int FI_FACIAL_Rolate_Y_Rotate_320x240_to_240x320(FI_HANDLE hFiCodec, unsigned char *bufYSrc, int nWidth, int nHeight, unsigned char *bufYDst);

FI_FACIAL_API void FI_FACIAL_YUV422VGA_TO_YUV420P_And_Rotate90(FI_HANDLE hFiCodec, char* pYuv422Vga, char* pVga, char* pSif, FI_BOOL bUV);

// 测试
FI_FACIAL_API int FI_FACIAL_Test_Mem(FI_HANDLE hFiCodec, char* pInBuf, char* pOutBuf, int nLen);


////
#ifdef __cplusplus
}
#endif

#endif //_FI_FACIAL_H

