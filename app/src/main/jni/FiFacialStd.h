
#ifndef _FI_FACIAL_STD_H
#define _FI_FACIAL_STD_H

#include "StdDefEx.h"

#ifdef __cplusplus
extern "C" {

#endif
#define FEATURESIZE_1K                    1024 /* 1K 人脸特征长度 */
#define FEATURESIZE_2K                    2048 /* 2K 人脸特征长度 */
#define FEATURESIZE_JNM                1024 /* JNM 人脸特征长度*/
#define FEATURESIZE_RAW_JNM            3200 /* 裸JNM 人脸特征长度*/
#define FEATURESIZE_MAX                    FEATURESIZE_2K
#define FEATURESIZE_MBLBP                1024 /* MBLBP 人脸特征长度*/

typedef struct _FI_FACIAL_ROTATE_PARAM
{
    FI_OPCMD common;
    int nPic; /* 子图个数：1 or 2 or 4 */
    int angle[4]; /* 旋转角度，必须是90度的倍数，如90/180/-90 */
    int bMirror[4]; /* 是否镜象*/
    int offsetx[4], offsety[4], zoom[4]; /* 对旋转镜象后的图象进行偏移及放大(放大倍数已乘100转成定点) */
} FI_FACIAL_ROTATE_PARAM, *LPFI_FACIAL_ROTATE_PARAM;

typedef struct _FI_FACIAL_INIT_PARAM
{
    FI_OPCMD common;
    FI_AUTH_KEYS answerKey; /* 加密芯片根据随机码返回的认证码*/
} FI_FACIAL_INIT_PARAM, *LPFI_FACIAL_INIT_PARAM;

typedef struct _FI_FACIAL_INPUT_BASE_PARAM
{    
    FI_OPCMD common;
    int bRegistTemplate;  /* 0-识别，1-注册*/
    int nWidth;
    int nHeight;
} FI_FACIAL_INPUT_BASE_PARAM, *LPFI_FACIAL_INPUT_BASE_PARAM;

typedef struct _FI_FACIAL_DETECT_EYE_STATUS_PARAM
{    
    FI_OPCMD common;
    int nWidth;
    int nHeight;
    FI_POINT ptLeftEye;
    FI_POINT ptRightEye;
} FI_FACIAL_DETECT_EYE_STATUS_PARAM, *LPFI_FACIAL_DETECT_EYE_STATUS_PARAM;

typedef struct _FI_FACIAL_VERSION
{
    unsigned int version;
}FI_FACIAL_VERSION, *LPFI_FACIAL_VERSION;

typedef struct _FI_FACIAL_ALGORSN
{
    char sn[256];
}FI_FACIAL_ALGORSN, *LPFI_FACIAL_ALGORSN;

typedef struct _FI_FACIAL_INPUT_ADJUST_PARAM
{    
    FI_OPCMD common;
    int bRegistTemplate;  /* 0-识别，1-注册*/
    int nWidth;
    int nHeight;
    float fBrightnessReference;
    int nAdjust;
} FI_FACIAL_INPUT_ADJUST_PARAM, *LPFI_FACIAL_INPUT_ADJUST_PARAM;

typedef struct _FI_FACIAL_OUTPUT_INT
{
    int val;
} FI_FACIAL_OUTPUT_INT, *LPFI_FACIAL_OUTPUT_INT;

typedef struct _FI_FACIAL_OUTPUT_FLOAT
{
    float val;
} FI_FACIAL_OUTPUT_FLOAT, *LPFI_FACIAL_OUTPUT_FLOAT;

// 裸特征(  为投影前数据)  数据结构
// 必须确保32  位对齐，即结构大小刚好为4  字节的整数倍
typedef struct _FI_FACIAL_MBLBP_RAW_FEA_DATA
{
    int fea_length;
    unsigned char fea[FEATURESIZE_RAW_JNM];
}FI_FACIAL_MBLBP_RAW_FEA_DATA;

// 裸特征(  为投影前数据)  数据存储器
// 必须确保32  位对齐，即结构大小刚好为4  字节的整数倍
typedef struct _FI_FACIAL_MBLBP_RAW_FEA_STG
{
    FI_FACIAL_MBLBP_RAW_FEA_DATA    feaData1;
    FI_FACIAL_MBLBP_RAW_FEA_DATA    feaData2;
    unsigned char                     icon[120*142];    /* 小图*/
}FI_FACIAL_MBLBP_RAW_FEA_STG;


#if 1
/* 检测方式*/
typedef enum FI_FACIAL_DETECT_WAY_ENUM
{
    FI_FACIAL_DETECT_WAY_INVALID    = 0,        /*  无效*/
    FI_FACIAL_DETECT_WAY_IR_GN        = 1,        /*  红外通用*/
    FI_FACIAL_DETECT_WAY_IR_FP        = 2,        /*  红外快速*/
    FI_FACIAL_DETECT_WAY_VR_GN        = 3,        /*  可见光通用*/
    FI_FACIAL_DETECT_WAY_VR_FP        = 4,        /*  可见光快速*/
    FI_FACIAL_DETECT_WAY_EyeStatus    = 5,        /*  疲劳检测*/
}FI_FACIAL_DETECT_WAY_ENUM;

/* 检测个数*/
typedef enum FI_FACIAL_DETECT_NUM_ENUM
{
    FI_FACIAL_DETECT_ALL                = 0,        /*  返回所有人脸*/
    FI_FACIAL_DETECT_MAX                = 1,        /*  返回最大人脸*/
}FI_FACIAL_DETECT_NUM_ENUM;

/* 检测参数结构*/
typedef struct _FI_FACIAL_DETECT_PARAM
{
    int width;
    int height;

    enum FI_FACIAL_DETECT_WAY_ENUM     detectWay;
    enum FI_FACIAL_DETECT_NUM_ENUM     detectNum;

    int bDetectFace;            /* 是否检测人脸，当不检测人脸而检测眼睛的时候，算法将在指定的detectZone   区域内检测眼睛 */
    int bDetectEye;            /* 是否检测眼睛 */
    int bDetectEyeStatus;        /* 是否检测睁闭眼状态*/
    int bDetectMouth;        /* 是否检测嘴巴*/

    RECT detectZone;    /* 检测区域，既可以规定人脸检测区域，也可以指定眼睛检测区域 (即检测到的实际人脸区域)   */

    int nMinHeadWidth;    /* 最小人头宽度 */
    int nMaxHeadWidth;    /* 最大人头宽度 */

    int nEyeStatusThreshold; /*
                                nEyeStatusThreshold==-1 返回闭眼可信度分数；
                                nEyeStatusThreshold==0   返回睁闭眼分析结果，输入0   表示使用默认值；
                                nEyeStatusThreshold>=1   返回睁闭眼分析结果，nEyeStatusThreshold 表示闭眼阈值；

                                // 返回值:
                                // =============== nEyeStatusThreshold>=0 返回睁闭眼分析结果=========================
                                // nEyeStatus==-1 检测失败;
                                // nEyeStatus==0  完全闭眼;
                                // nEyeStatus==1  双眼睁开;
                                // nEyeStatus==2  左眼睁开;
                                // nEyeStatus==3  右眼睁开;
                                // =============== nEyeStatusThreshold==-1 返回闭眼可信度==============================
                                // nLeftCloseCreditScore 左眼可信度；
                                // nRightCloseCreditScore   右眼可信度；
                                // nEyeStatus == (nLeftCloseCreditScore<<8) + (nRightCloseCreditScore);

                                红外睁闭眼分析阈值，取值范围[1, 100]，默认值为50，输入0   表示使用默认值；
                                取值越小，即降低检测为闭眼情况的严格度，即理论上可以提升闭眼的检测率
                            */

    //////////////////////////////////////////////////////////////////////////////////////////////
    // 以下预留了100  个字段，用于定义参数位
    // 已经定义的参数为，需要在后面加注释，声明其意义
    //////////////////////////////////////////////////////////////////////////////////////////////
    int nField0;  /* 是否启用高光过滤:  ==1 启用;  ==0  禁止; */
    int nField1;  /* 高光亮度阈值，取值范围[0, 255]， 默认值是190 */
    int nField2;  /* 高光面积阈值，取值范围是[1, 900]，默认值是9 */
    int nField3;
    int nField4;
    int nField5;
    int nField6;
    int nField7;
    int nField8;
    int nField9;

#if 1
    int nField10;
    int nField11;
    int nField12;
    int nField13;
    int nField14;
    int nField15;
    int nField16;
    int nField17;
    int nField18;
    int nField19;

    int nField20;
    int nField21;
    int nField22;
    int nField23;
    int nField24;
    int nField25;
    int nField26;
    int nField27;
    int nField28;
    int nField29;

    int nField30;
    int nField31;
    int nField32;
    int nField33;
    int nField34;
    int nField35;
    int nField36;
    int nField37;
    int nField38;
    int nField39;

    int nField40;
    int nField41;
    int nField42;
    int nField43;
    int nField44;
    int nField45;
    int nField46;
    int nField47;
    int nField48;
    int nField49;
#endif

} FI_FACIAL_DETECT_PARAM, *LPFI_FACIAL_DETECT_PARAM;
#endif

// 人脸信息结构
typedef struct _FI_FACIAL_FACE_INFO
{
    int nFaceNum;                     /* 检测到的人脸个数*/
    FI_RECT  rcFace;                 /* 最大人脸的坐标*/
    FI_POINT ptLeftEye;            /* 左眼坐标*/
    FI_POINT ptRightEye;        /* 右眼坐标*/
    FI_POINT ptMouthPos;        /* 嘴巴坐标*/
    FI_UINT  nEyeStatus;
                        // 输入参数:
                        //         nEyeStatusThreshold>=0 返回睁闭眼分析结果，nEyeStatusThreshold==-1 返回闭眼可信度
                        // 返回值:
                        // =============== nEyeStatusThreshold>=0 返回睁闭眼分析结果=========================
                        // nEyeStatus==-1 检测失败;
                        // nEyeStatus==0  完全闭眼;
                        // nEyeStatus==1  双眼睁开;
                        // nEyeStatus==2  左眼睁开;
                        // nEyeStatus==3  右眼睁开;
                        // =============== nEyeStatusThreshold==-1 返回闭眼可信度==============================
                        // nLeftCloseCreditScore 左眼可信度；
                        // nRightCloseCreditScore   右眼可信度；
                        // nEyeStatus == (nLeftCloseCreditScore<<8) + (nRightCloseCreditScore);

} FI_FACIAL_FACE_INFO, *LPFI_FACIAL_FACE_INFO;

// 人脸对象结构
typedef struct _FI_FACIAL_OBJ_INFO
{
    int id;                                /* 对象ID*/

    FI_FACIAL_FACE_INFO  faceInfo;        /* 人脸区域*/

    FI_BOOL bAlarm;                        /* bAlarm==TRUE，    发生警报（即人脸对象失踪），此时获取并保存最佳人脸照片
                                         bAlarm==FALSE，    没有警报（即人脸对象跟踪），持续跟踪中...
                                        */

    int   nTrackPointNum;                    /* 跟踪轨迹点个数*/
    FI_POINT arrTrackPoint[256];            /* 跟踪轨迹点数组*/
} FI_FACIAL_OBJ_INFO, *LPFI_FACIAL_OBJ_INFO;

// 对象检测结果
typedef struct _FD_DETECT_FACE_RESULT
{
    int nFaceNum;
    FI_RECT rtFace[10];
} FI_FACIAL_DETECT_FACE_RESULT, *LPFI_FACIAL_DETECT_FACE_RESULT;

// 对象跟踪结果
typedef struct _FI_FACIAL_DETECTX_RESULT
{
    int faceNum;
    FI_FACIAL_FACE_INFO faceInfo[50];
} FI_FACIAL_DETECTX_RESULT, *LPFI_FACIAL_DETECTX_RESULT;

typedef struct _FI_FACIAL_TRACKX_RESULT
{
    int objNum;
    FI_FACIAL_OBJ_INFO objInfo[50];
} FI_FACIAL_TRACKX_RESULT, *LPFI_FACIAL_TRACKX_RESULT;

/////////////////////////////////////////////////////////////////////////////
// 获取人脸对象最近/  最佳照片参数/  结果结构
/////////////////////////////////////////////////////////////////////////////
typedef struct _FI_FACIAL_GET_PHOTO_PARAM
{    
    FI_OPCMD common;
    int nObjID;            /*人脸对象ID*/
} FI_FACIAL_GET_PHOTO_PARAM, *LPFI_FACIAL_GET_PHOTO_PARAM;

typedef struct _FI_FACIAL_GET_PHOTO_RESULT
{    
    int nSucceed;        /*是否执行成功:  ==1  成功;  ==0  失败*/
    int nImageWidth;        /*图像宽度*/
    int nImageHeight;    /*图像高度*/
} FI_FACIAL_GET_PHOTO_RESULT, *LPFI_FACIAL_GET_PHOTO_RESULT;

/////////////////////////////////////////////////////////////////////////////
// 
/////////////////////////////////////////////////////////////////////////////

// 人脸特征矩阵比对输入参数结构
typedef struct _FI_FACIAL_COMPARE_FEATURE_INPUT
{
    FI_OPCMD common;
    int bRegistTemplate; /* 0-识别，1-注册*/
    int nFeaNum;
} FI_FACIAL_COMPARE_FEATURE_INPUT, *LPFI_FACIAL_COMPARE_FEATURE_INPUT;

// 人脸特征比对值结果
typedef struct _FI_FACIAL_COMPARE_FEATURE_RESULT
{
    int nFeaNum;
    int nMaxValueNo;            /* 比对分数最大的用户中比对分数最大的模板序号 */
    float fMaxCompValue;        /*  用户匹配的最大比对分数值*/

    ////
    float fMaxCompValue_bd; // 最佳匹配值（差值/标准差）
    float fCompValueAve;        // 均值
    float fCompValueSd;            // 标准差
} FI_FACIAL_COMPARE_FEATURE_RESULT, *LPFI_FACIAL_COMPARE_FEATURE_RESULT;

////////////////////////////////////////// compare feature x ///////////////////////////////////////////////////

// 人脸特征比对参数结构
typedef struct _FI_FACIAL_COMPARE_FEATURE_PARAM_X
{
    FI_OPCMD common;

    ////
//    int bReturnTemplateMaxCompareValue;    /* 是否返回模板比对最大值*/
//    int bReturnPeopleMaxCompareValue;        /* 是否返回人员比对最大值*/

    int nStatTopRangeNum;                    /* 高值域统计模板个数*/
    int nStatBottomRangeNum;                /* 低值域统计模板个数*/

    int nFeaTemplateLength;                    /* 特征模板长度*/

    int nReserve1[50];                        /* 保留位*/

    ////
    char curFeaTemplate[2048];                /* 最大支持2048  大小的模板*/

    int nReserve2[50];                        /* 保留位*/
    char szReserve1[2048];                    /* 保留位*/

    ////
    int nRefUserNum;                            /* 参与比对的账户个数*/
    int nRefFeaNum;                            /* 参与比对的模板总数*/
    unsigned char arrRefUserFeaNum[2048];        /* 参数比对的每个账户拥有的模板数数组*/

    int nReserve3[50];                        /* 保留位*/
    char szReserve2[2048];                    /* 保留位*/

}FI_FACIAL_COMPARE_FEATURE_PARAM_X, *LPFI_FACIAL_COMPARE_FEATURE_PARAM_X;

// 人脸特征比对值结果
typedef struct _FI_FACIAL_COMPARE_FEATURE_RESULT_X
{
    /* 综合分析*/
    int nMatchPeopleIndex;                        /* 匹配人员序号*/
    int nMatchTemplageIndex;                    /* 匹配模板序号*/
    float fMaxCompareValue;                        /* 最大比对值*/

    /* 全值域分析*/
    /* 基于每个人所有模板的统计值*/
    int nMatchPeopleIndexForAllRange;            /* 匹配人员序号*/
    int nMatchTemplageIndexForAllRange;            /* 匹配模板序号*/
    float fMaxCompareValueForAllRange;            /* 最大比对值*/

    /* 高值域分析*/
    /* 基于每个人比值较高模板的统计值*/
    int nMatchPeopleIndexForTopRange;            /* 匹配人员序号*/
    int nMatchTemplageIndexForTopRange;        /* 匹配模板序号*/
    float fMaxCompareValueForTopRange;            /* 最大比对值*/

    /* 中值域分析*/
    /* 基于每个人比值居中模板的统计值*/
    int nMatchPeopleIndexForMiddleRange;        /* 匹配人员序号*/
    int nMatchTemplageIndexForMiddleRange;        /* 匹配模板序号*/
    float fMaxCompareValueForMiddleRange;        /* 最大比对值*/

    /* 低值域分析*/
    /* 基于每个人比值较低模板的统计值*/
    int nMatchPeopleIndexForBottomRange;        /* 匹配人员序号*/
    int nMatchTemplageIndexForBottomRange;        /* 匹配模板序号*/
    float fMaxCompareValueForBottomRange;        /* 最大比对值*/

    /* 返回匹配账户所有模板(  最多10  个模板)  比对值*/
    float fCompageValueTemplate0;
    float fCompageValueTemplate1;
    float fCompageValueTemplate2;
    float fCompageValueTemplate3;
    float fCompageValueTemplate4;
    float fCompageValueTemplate5;
    float fCompageValueTemplate6;
    float fCompageValueTemplate7;
    float fCompageValueTemplate8;
    float fCompageValueTemplate9;

    /* 返回每个模板比对值*/
    float fCompageValue[1024*10];

    ////
    int      nReserve1[50];                        /* 保留位*/
    float     fReserve1[50];                        /* 保留位*/

    ////
    float fMaxCompValue_bd;                     /* 最佳匹配值（差值/标准差）    */
    float fCompValueAve;                        /* 均值                                    */
    float fCompValueSd;                        /* 标准差                                */

    int      nReserve2[50];                        /* 保留位*/
    float     fReserve2[50];                        /* 保留位*/

    int nReserve3[50];                        /* 保留位*/
    char szReserve1[2048];                    /* 保留位*/

} FI_FACIAL_COMPARE_FEATURE_RESULT_X, *LPFI_FACIAL_COMPARE_FEATURE_RESULT_X;

////////////////////////////////////////// compare feature x ///////////////////////////////////////////////////

typedef struct _FI_FACIAL_IMAGEDEAL_PARAM
{
    FI_OPCMD common;
    int bHasUV;
} FI_FACIAL_IMAGEDEAL_PARAM, *LPFI_FACIAL_IMAGEDEAL_PARAM;


// 从arm  端传递配置信息和模型数据到dsp  端的全局信息解析结构
typedef struct _FI_FACIAL_TRANS_CONFIG_GLOBALINFO
{
     char g_guid[48];
     char g_version[48];
     int  g_nModelNum;
     int g_nScoreCF;
    int g_nScoreWeight1;
    int g_nScoreWeight2;
     int g_nThreshold;
     float g_fAlignRatio1;
     float g_fAlignRatio2;

    BOOL g_bEnableAntispooling;
    int  g_nAntispoolingFeaVer;
    int  g_nAntispoolingThreshold;

}FI_FACIAL_TRANS_CONFIG_GLOBALINFO;

// 从arm  端传递配置信息和模型数据到dsp  端，最少需要分配7  M  内存空间
#if 0
// 初始定义的版本，需要较大的内存空间支持
typedef struct _FI_FACIAL_TRANS_CONFIG_MODEL
{
    int  fbGlobalInfoLen;
    char fbGlobalInfo[1024];                        // FI_FACIAL_TRANS_CONFIG_GLOBALINFO

    ////
    int  fbModel1ParamLen;
    char fbModel1Param[1024];

    int  fbModel1ParaDataLen;
    char fbModel1ParaData[1024*1024*1];

    int  fbModel1ProjDataLen;
    char fbModel1ProjData[1024*1024*2];

    ////
    int  fbModel2ParamLen;
    char fbModel2Param[1024];

    int  fbModel2ParaDataLen;
    char fbModel2ParaData[1024*1024*1];

    int  fbModel2ProjDataLen;
    char fbModel2ProjData[1024*1024*2];

}FI_FACIAL_TRANS_CONFIG_MODEL;
#else
// 针对低成本机型修改的版本，支持紧缩的内存需求，最少需要分配3.5  M  内存空间
typedef struct _FI_FACIAL_TRANS_CONFIG_MODEL
{
    int  fbGlobalInfoLen;
    char fbGlobalInfo[1024];                        // FI_FACIAL_TRANS_CONFIG_GLOBALINFO

    ////
    int  fbModel1ParamLen;
    char fbModel1Param[1024];

    int  fbModel1ParaDataLen;
    char fbModel1ParaData[1024*100];  /* 目前实际最大需要是36K，这里定义最大支持100K */

    int  fbModel1ProjDataLen;
    char fbModel1ProjData[1024*1500]; /* 目前实际最大需要是1250K，这里定义最大支持1500K */

    ////
    int  fbModel2ParamLen;
    char fbModel2Param[1024];

    int  fbModel2ParaDataLen;
    char fbModel2ParaData[1024*100];  /* 目前实际最大需要是36K，这里定义最大支持100K */

    int  fbModel2ProjDataLen;
    char fbModel2ProjData[1024*1500]; /* 目前实际最大需要是1250K，这里定义最大支持1500K */

}FI_FACIAL_TRANS_CONFIG_MODEL;
#endif

#ifdef __cplusplus
}
#endif

#endif // _FI_FACIAL_STD_H


