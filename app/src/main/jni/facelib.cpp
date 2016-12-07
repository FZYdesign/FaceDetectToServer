#include <unistd.h>
#include <pthread.h> 
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <time.h>
#include <netinet/in.h>
#include <linux/route.h>
#include <pthread.h> /* support MULTI-THREADS lock */
#include <asm/types.h>
#include <sys/mman.h>
#include <linux/types.h>
#include <linux/videodev2.h>  
#include<jni.h>

#include "facelib.h"
#include "Public.h"
#include "FiCe.h"
#include "FiFacial.h"
#include "StdDefEx.h"
#include "FiFacialStd.h"
#include "netComm.h"
#include "NetDeal.h"
#include "gb2312.h"
#include "ftpclient.h"


/*
 * RecognizeFace.c
 *
 *  Created on: 2013-11-8
 *      Author: Administrator
 */
//识别采集jpg图片质量
#define DEFAULT_JPGE_QUALITY 100
//图片最大旋转次数
#define IMAGE_MAX_ROTATE_TIMES 2

#define INVALID         0xFFFFFFFF
#define INVALID_VALUE       INVALID

#ifndef INVALID_HANDLE_VALUE
#define INVALID_HANDLE_VALUE    ((HANDLE)INVALID)
#endif

/*******************************************全局变量*****************************************/
/* 全局的传入算法的特征指针 */
char *gpUsersFeature = NULL;

/* 打开算法库的句柄 */
OD_CE_HANDLE ghCe = NULL;

/* 打开算法库编码模块的句柄 */
OD_CODEC_HANDLE ghFacialCodec;

/* 打开算法库编码模块的错误码 */
int giCodecErrorNo = 0;

/* 应用程序与算法之间的共享内存，20M字节 */
void *gpSharedMem = NULL;

/* 应用程序与算法之间的共享内存大小 */
int giSharedMemSize = 0;

/* 特征数目 */
int giTotalFeatureNum = 0;

/* 存储特征值密钥 */
char gcaFeaturePwd[33] = {0};

/* 记录全部用户与管理员的信息 */
USERINFO gstrUserList[MAX_USER_NUM] = {0};

static char s_D1_BUF[D1_WIDTH * D1_HEIGHT * 2];
static char s_SIF_BUF[2][SIF_HEIGHT * SIF_WIDTH * 4];
static char s_SIF_BUF_TMP[2][SIF_HEIGHT * SIF_WIDTH * 2] = {{0}};

char g_SIF_BUF_Send[SIF_HEIGHT * SIF_WIDTH * 2] = {0};

/* 临时共享buf */
char g_cShareMem[20 * 1024] = {0};

HANDLE g_hVideoColor = INVALID_HANDLE_VALUE;
HANDLE g_hVideoIrda = INVALID_HANDLE_VALUE;

CMutex g_mtVideoDisp;
CMutex g_mtVideoDetect;

BOOL s_bEnableVideo = TRUE;

pthread_attr_t gtAttr;
pthread_attr_t *gtpAttr = NULL;

pthread_t gtReadIrdaID = 0;

static char *s_pShareMem = NULL;

/* 用户总数 */
int giUserNum = 0;
//是否存在抓拍到人脸的照片
int giPictureFaceFlag = 0;


int g_rwPic = 0;
pthread_mutex_t gRwPicMutex;


extern void StartNetModule();

FI_FACIAL_DETECT_PARAM detect_param;

// 打开算法库引擎
OD_CE_HANDLE OD_OpenEngine() {
    OD_CE_HANDLE hCe = NULL;

    /* modified by ytj 20110808，根据5.2.2红外普通算法接口，
     FI_OpenEngine函数后2个参数需要指定cmemk内存的起始地址与结束地址 */
    hCe = (OD_CE_HANDLE) FI_OpenEngine(FI_FALSE, 0x88000000, 0x8BFFFFFF);

    return hCe;
}

OD_CODEC_HANDLE OD_OpenFacialCodec(OD_CE_HANDLE hCe, char *pStrAuxDir,
                                   char **ppFeatureInBuf, int *pnErrNo) {
    FI_HANDLE hFiCodec = NULL;
    int nErrNo = 0;
    //LOGD("fun:%s line: %d hCe: %p", __FUNCTION__, __LINE__, hCe);
    if (hCe) {
        char strRandKey[32] = {0}, strPlanCode[32] = {0};
        hFiCodec = FI_FACIAL_OpenCodec(hCe, strRandKey);
        if (hFiCodec) {
            int ret = -1;

            if (pStrAuxDir)
                FI_FACIAL_SetAuxDir(hFiCodec, pStrAuxDir);
            ret = DM2016_Authentication(strRandKey, strPlanCode);
#ifdef PLAT_TI6446
            int i, j, fd;
            time_t ct = time(NULL);
            struct tm* pTm = gmtime(&ct);

            if(ret<0) {
                // DM2016 key
                struct DM2610_IO {
                    unsigned char msg[20];
                    unsigned char digits[20];
                }val;
                char* pDev = (char *)"/dev/davincicmkey0";
                memcpy(strPlanCode, strRandKey, 20);
                fd = open(pDev, O_RDWR);
                if(fd<0) {
                    TRACE("Open DM2610 chip fail\r\n");
                    nErrNo = -4;
                } else {
                    memcpy(val.msg, strRandKey, 20);

                    for(i=0; i<10; i++) {
                        unsigned char ch = val.msg[i];
                        val.msg[i] = val.msg[20-1-i];
                        val.msg[20-1-i] = ch;
                    }
                    for(i=0; i<10; i++) { /* retry 10 times */
                        if(ioctl(fd, DM2610_IO_READB_CMD, &val)>=0) break;
                        usleep(100*1000);
                    }
                    if(i==10) {
                        TRACE("DM2610_IO_READB_CMD fail\r\n");
                        nErrNo = -5;
                    } else {
                        for(j=0; j<5; j++) {
                            for(i=0; i<2; i++) {
                                unsigned char ch = val.digits[j*4+i];
                                val.digits[j*4+i] = val.digits[(j+1)*4-1-i];
                                val.digits[(j+1)*4-1-i] = ch;
                            }
                        }

                        memcpy(strPlanCode, val.digits, 20);
                    }
                    close(fd);
                }
            }

            fd=socket(AF_INET,SOCK_DGRAM,0);
            if(fd>=0) {
                struct ifreq ifr;
                memset(&ifr,0,sizeof(ifr));
                strncpy(ifr.ifr_name,"eth0",IFNAMSIZ);

                ioctl(fd,SIOCGIFHWADDR,&ifr,sizeof(ifr));

                memcpy(strPlanCode+20,ifr.ifr_hwaddr.sa_data,6);
                close(fd);
            }
            if(pTm) {
                if(pTm->tm_year>110) pTm->tm_year=110;
                i = (pTm->tm_year+1900)*10000+(pTm->tm_mon+1)*100+pTm->tm_mday;
                memcpy(strPlanCode+26, &i, 4);
            }

            if(nErrNo) PassData(strPlanCode, 20, "facial");
            PassData(strPlanCode+20, 12, "facial");
#endif
            ret = FI_FACIAL_InitCodec(hFiCodec, strPlanCode);

            if (ppFeatureInBuf)
                *ppFeatureInBuf = FI_FACIAL_GetInFeatureBuf(hFiCodec);
        } else
            printf("FI_FACIAL_OpenCodec: Fail!");
    }

    return (OD_CODEC_HANDLE) hFiCodec;
}

void OD_freeShareMem() {
    int nlen = OD_CODEC_BUF_SIZE;
    if (s_pShareMem) {
        FI_FreeContigBuf(s_pShareMem, nlen);
    }
}

char *OD_mallocShareMem() {
    int nlen = OD_CODEC_BUF_SIZE;

    s_pShareMem = FI_MallocContigBuf(nlen);

    return s_pShareMem;
}

void OD_CloseFacialCodec(OD_CODEC_HANDLE hCodec) {
    if (hCodec) {
        FI_FACIAL_CloseCodec(hCodec);
    }
}

// 关闭算法库引擎
void OD_CloseEngine(OD_CE_HANDLE hCe) {
    //TRACE("%s %d \r\n", __FUNCTION__, __LINE__);

    if (hCe)
        FI_CloseEngine((FI_HANDLE) hCe);
}

int OD_DetectFacial(OD_CODEC_HANDLE hCodec, char *pYBuf, int nWidth,
                    int nHeight, FI_FACIAL_DETECTX_RESULT *pResult) {
    int ret = -1;

    detect_param.width = nWidth;
    detect_param.height = nHeight;
    detect_param.detectWay = FI_FACIAL_DETECT_WAY_VR_GN;
    detect_param.detectNum = FI_FACIAL_DETECT_MAX;//FI_FACIAL_DETECT_ALL;
    detect_param.bDetectFace = 1;
    detect_param.bDetectEye = 1;
    detect_param.bDetectEyeStatus = 1;
    detect_param.bDetectMouth = 1;

    /* added by ytj 20110808，6446平台使用 普通算法 */
    if (hCodec) {
        //LOGD("OD_DetectFacial() hCodec:%p line:%d", hCodec, __LINE__);
        ret = FI_FACIAL_DetectX(hCodec, pYBuf, &detect_param, pResult);
    }
    return ret;
}

/* 打桩 */
int DM2016_Authentication(char *pEncryptedCode, char *pPlanCode) {
    return 0;
}

void InitFacial() {
    int i = 0;

    if (ghCe != NULL) {
        return;
    }

    ghCe = OD_OpenEngine();
    LOGD("InitFaceLib()ghCe: %p fun:%s line: %d", ghCe, __FUNCTION__, __LINE__);
    if (ghCe) {
        (void) FI_FACIAL_SetInFeatureBufSize(OD_CODEC_BUF_SIZE / (1024 * 1024));

        for (i = 0; i < 10; i++) {
            ghFacialCodec = OD_OpenFacialCodec(ghCe, (char *) FACIAL_LIB_PATH,
                                               &gpUsersFeature, &giCodecErrorNo);
            LOGD("fun:%s line: %d ghFacialCodec: %p", __FUNCTION__, __LINE__, ghFacialCodec);
            if (ghFacialCodec) {
                if (gpUsersFeature) {
                    giSharedMemSize = OD_CODEC_BUF_SIZE;
                    gpSharedMem = gpUsersFeature;
                }
                break;
            }

            usleep(10 * 1000);
        }

        if (ghFacialCodec == NULL) {
            gpSharedMem = OD_mallocShareMem();
            if (gpSharedMem) {
                giSharedMemSize = OD_CODEC_BUF_SIZE;
            }
        }
    } else {
        giCodecErrorNo = -1;
    }
}

void DeInitFacial() {
    if (ghCe) {
        if (ghFacialCodec == NULL) {
            if (gpSharedMem) {
                OD_freeShareMem();
            }
        }

        gpSharedMem = NULL;
        giSharedMemSize = 0;
    }
    if (ghFacialCodec) {
        OD_CloseFacialCodec(ghFacialCodec);
        ghFacialCodec = NULL;
    }
    if (ghCe) {
        OD_CloseEngine(ghCe);
        ghCe = NULL;
    }
}

/**
 *  初始化人脸识别算法库
 *  return 返回执行结果
 */

//void initFaceLib(JNIEnv* env, jobject thiz) {
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_initFaceLib(JNIEnv *env, jobject thiz) {
    //LOGD("initFaceLib()");
    //初始化算法
    InitFacial();
    LOGD("DoTcpSendThreadPool %s %d\r\n", __FUNCTION__, __LINE__);
}

/**
 *  释放初始化人脸识别算法库
 *  return 返回执行结果
 */

//void releaseFaceLib(JNIEnv* env, jobject thiz) {
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_releaseFaceLib(JNIEnv *env, jobject thiz) {
    //LOGD("releaseFaceLib()");
    //释放申请资源
    DeInitFacial();
}

void RoateData(LONG width, LONG height, unsigned char *srcData, unsigned char *dstData, int Bpp) {
    //Bpp  byte per pixel
    int i, j;
    int k;
    unsigned char *tmpSrc = srcData;
    unsigned char *tmpDst = dstData;

    width *= Bpp;        //每像素占三字节
    for (i = 0; i < width; i += Bpp)
        //for (i = width - 3; i >= 0 ; i -= 3)
    {
        //for(j = 0; j < height; j++)
        for (j = height - 1; j >= 0; j--) {
            for (k = 0; k < Bpp; k++) {
                *tmpDst = *(tmpSrc + j * width + i + k);
                tmpDst++;
            }
        }
    }
}

void rotateYUV240SP(unsigned char *src, unsigned char *des, int width, int height) {

    int wh = width * height;
    //旋转Y
    int k = 0;
    for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
            des[k] = src[width * j + i];
            k++;
        }
    }

    for (int i = 0; i < width; i += 2) {
        for (int j = 0; j < height / 2; j++) {
            des[k] = src[wh + width * j + i];
            des[k + 1] = src[wh + width * j + i + 1];
            k += 2;
        }
    }


}

/**
 *  人脸识别
 *  return 返回执行结果
 */
JNIEXPORT jintArray JNICALL Java_cn_runvision_utils_FaceNative_recognizeFace(JNIEnv *env, jobject thiz,
                                                                      jint width,
                                                                      jint height, jboolean isFront,
                                                                      jbyteArray imgData) {
    int nFeatureBufSize = 1024;
    char *pFeatureJNM = NULL;
    int ret = -1;
    int ret1 = 10;
    int yWidth = width;
    int yHeight = height;
    //识别模式，默认为1注册模式
    int iMode = 1;
    int iEyeWidthMin = 0;
    int iEyeWidthMax = 0;
    int iEyeWidth = 0;
    int i = 0;

    //默认旋转90度
    signed short rotateValue = 0;
    // 返回人脸坐标(left, top, right, bottom)
    jint pFace[13] = {0};
    FI_FACIAL_DETECTX_RESULT strFacialInfo;
    // 传入的图像数据
    jbyte *jbts = (env)->GetByteArrayElements(imgData, 0);
    //传入的数据长度
    jsize len = (env)->GetArrayLength(imgData);
    // 图片数据存储空间

    char *g_SIF_BUF = new char[yWidth * yHeight * 2];
    memset(g_SIF_BUF, 0, yWidth * yHeight * 2);
    char *g_Temp_BUF = new char[yWidth * yHeight * 2];
    memset(g_Temp_BUF, 0, yWidth * yHeight * 2);

    char *g_Temp_BUF2 = new char[yWidth * yHeight * 2];
    memset(g_Temp_BUF2, 0, yWidth * yHeight * 2);
    memcpy(g_Temp_BUF, jbts, len);

    char *g_Temp_BUF3 = new char[yWidth * yHeight * 2];
    memset(g_Temp_BUF3, 0, yWidth * yHeight * 2);
    //saveRaw((unsigned char*) g_Temp_BUF, SIF_HEIGHT * SIF_WIDTH * 2);
    //LOGD("recognizeFace() width:%d height:%d imgData.length:%d %s %d\r\n",width, height, len, __FUNCTION__, __LINE__);
    if (JNI_TRUE == isFront) {
        rotateValue = 270;
        //识别逆时针旋转90度
        //void YUV420SPToYUV420P ( void * yuv420sp,  void * yuv420,  int  width,  int  height )
        //LOGD("yWidth[%d] yHeight[%d]\r\n",yWidth,yHeight);

        YUV420SPToYUV420P(g_Temp_BUF, g_Temp_BUF2, yWidth, yHeight);
        rotate90_yuv420p(g_Temp_BUF2, yWidth, yHeight, g_SIF_BUF, TRUE, 0);

        memset(&strFacialInfo, 0, sizeof(strFacialInfo));
        ret = OD_DetectFacial(ghFacialCodec, g_SIF_BUF, yHeight, yWidth,
                              &strFacialInfo);
        //LOGD("rotateValue:%d ret:%d strFacialInfo.faceNum:%d %s %d\r\n", rotateValue,ret, strFacialInfo.faceNum, __FUNCTION__, __LINE__);
        if (ret <= 0 || strFacialInfo.faceNum <= 0) {
            //LOGD("ret <= 0 || strFacialInfo.faceNum <= 0 %s %d\r\n",
            //        __FUNCTION__, __LINE__);
            //不旋转
            rotateValue = 0;
            memset(g_SIF_BUF, 0, yWidth * yHeight * 2);
            memcpy(g_SIF_BUF, jbts, len);
            // 识别原始图片
            memset(&strFacialInfo, 0, sizeof(strFacialInfo));
            ret = OD_DetectFacial(ghFacialCodec, g_SIF_BUF, yWidth, yHeight,
                                  &strFacialInfo);
            //LOGD("rotateValue:%d ret:%d strFacialInfo.faceNum:%d %s %d\r\n", rotateValue,ret, strFacialInfo.faceNum, __FUNCTION__, __LINE__);
            if (ret <= 0 || strFacialInfo.faceNum <= 0) {
                int j = 0;
                //LOGD("ret <= 0 %s %d\r\n", __FUNCTION__, __LINE__);
                //拷贝数据到目标区域。
                memset(g_Temp_BUF, 0, yWidth * yHeight * 2);
                memcpy(g_Temp_BUF, jbts, len);
                // 识别其他两个角度的图片
                for (j = 0; j < IMAGE_MAX_ROTATE_TIMES; j++) {
                    int mWidth = yHeight;
                    rotateValue = ((j + 1) * 90);
                    memset(g_SIF_BUF, 0, yWidth * yHeight * 2);
                    rotate90_yuv420p(g_Temp_BUF, yWidth, yHeight, g_SIF_BUF, 0,
                                     1);
                    yHeight = yWidth;
                    yWidth = mWidth;

                    //LOGD("yWidth:%d yHeight:%d rotateValue:%d %s %d\r\n",
                    //        yWidth, yHeight, rotateValue, __FUNCTION__, __LINE__);
                    memset(&strFacialInfo, 0, sizeof(strFacialInfo));
                    ret = OD_DetectFacial(ghFacialCodec, g_SIF_BUF, yWidth,
                                          yHeight, &strFacialInfo);
                    //LOGD("rotateValue:%d ret:%d strFacialInfo.faceNum:%d %s %d\r\n", rotateValue,ret, strFacialInfo.faceNum, __FUNCTION__, __LINE__);
                    if (ret > 0 && strFacialInfo.faceNum > 0) {
                        break;
                    }
                    //拷贝数据到目标区域。
                    memset(g_Temp_BUF, 0, yWidth * yHeight * 2);
                    memcpy(g_Temp_BUF, g_SIF_BUF, yWidth * yHeight * 2);
                }

            }
        }
    } else {
        rotateValue = 90;
        //识别逆时针旋转90度
        YUV420SPToYUV420P(g_Temp_BUF, g_Temp_BUF2, yWidth, yHeight);
        rotate90_yuv420p(g_Temp_BUF2, yWidth, yHeight, g_SIF_BUF, TRUE, 1);

        // rotate90_yuv420p(g_Temp_BUF, yWidth, yHeight, g_SIF_BUF, 0, 1);
        memset(&strFacialInfo, 0, sizeof(strFacialInfo));
        //LOGD("rotateValue:%d ret:%d strFacialInfo.faceNum:%d %s %d\r\n", rotateValue,ret, strFacialInfo.faceNum, __FUNCTION__, __LINE__);
        ret = OD_DetectFacial(ghFacialCodec, g_SIF_BUF, yHeight, yWidth,
                              &strFacialInfo);
        // LOGD("rotateValue:%d ret:%d strFacialInfo.faceNum:%d %s %d\r\n", rotateValue,ret, strFacialInfo.faceNum, __FUNCTION__, __LINE__);
        if (ret <= 0 || strFacialInfo.faceNum <= 0) {
            //LOGD("ret <= 0 || strFacialInfo.faceNum <= 0 %s %d\r\n",
            //        __FUNCTION__, __LINE__);
            //不旋转
            rotateValue = 0;
            memset(g_Temp_BUF, 0, yWidth * yHeight * 2);
            memcpy(g_Temp_BUF, jbts, len);
            // 识别原始图片
            memset(&strFacialInfo, 0, sizeof(strFacialInfo));
            ret = OD_DetectFacial(ghFacialCodec, g_Temp_BUF, yWidth, yHeight,
                                  &strFacialInfo);
            //LOGD("rotateValue:%d ret:%d strFacialInfo.faceNum:%d %s %d\r\n", rotateValue,ret, strFacialInfo.faceNum, __FUNCTION__, __LINE__);
            if (ret <= 0 || strFacialInfo.faceNum <= 0) {
                int j = 0;
                //LOGD("ret <= 0 %s %d\r\n", __FUNCTION__, __LINE__);
                //拷贝数据到目标区域。
                memset(g_Temp_BUF, 0, yWidth * yHeight * 2);
                memcpy(g_Temp_BUF, g_SIF_BUF, yWidth * yHeight * 2);
                // 识别其他两个角度的图片
                for (j = 0; j < IMAGE_MAX_ROTATE_TIMES; j++) {
                    int mWidth = yHeight;
                    rotateValue = ((j + 1) * 90);
                    memset(g_SIF_BUF, 0, yWidth * yHeight * 2);
                    rotate90_yuv420p(g_Temp_BUF, yWidth, yHeight, g_SIF_BUF, 0,
                                     1);
                    yHeight = yWidth;
                    yWidth = mWidth;

                    //LOGD("yWidth:%d yHeight:%d rotateValue:%d %s %d\r\n",
                    //        yWidth, yHeight, rotateValue, __FUNCTION__, __LINE__);
                    memset(&strFacialInfo, 0, sizeof(strFacialInfo));
                    ret = OD_DetectFacial(ghFacialCodec, g_SIF_BUF, yWidth,
                                          yHeight, &strFacialInfo);
                    //LOGD("rotateValue:%d ret:%d strFacialInfo.faceNum:%d %s %d\r\n", rotateValue,ret, strFacialInfo.faceNum, __FUNCTION__, __LINE__);
                    if (ret > 0 && strFacialInfo.faceNum > 0) {
                        break;
                    }
                    //拷贝数据到目标区域。
                    memset(g_Temp_BUF, 0, yWidth * yHeight * 2);
                    memcpy(g_Temp_BUF, g_SIF_BUF, yWidth * yHeight * 2);
                }

            }
        }
    }
    //delete[] g_Temp_BUF;
    // delete[] g_SIF_BUF;
    //LOGD("recognizeFace() ret:%d nFaceNum:%d %s %d\r\n", ret, strFacialInfo.faceInfo[0].nFaceNum, __FUNCTION__, __LINE__);
    if (ret > 0) {
        int index = 0; //标记返回的人脸索引
        int maxFace = 0;
        int sumLen = 0;
        int result = 0;
        giPictureFaceFlag = 1;

        //char strFile[] = "/mnt/sdcard/rong/rong.jpg";
        //char strFile[] = "/data/data/com.firs.facedetecttosvr/temp/facePic.jpg";
        char strFile[] = "/data/data/cn.runvision.facedetect/temp/facePic.jpg";
        memcpy(g_SIF_BUF_Send, g_SIF_BUF, SIF_HEIGHT * SIF_WIDTH * 2);


        //rotateYUV240SP((unsigned char*)jbts,(unsigned char*)g_SIF_BUF_Send,width,height);

        // 保存原始图像YUV420SP格式
        //saveRaw((unsigned char*) g_SIF_BUF_Send, SIF_HEIGHT * SIF_WIDTH * 2);
        RGB24x2 *bmp = (RGB24x2 *) malloc(3 * width * height);
        memset((unsigned char *) bmp, 0, 3 * width * height);

        // 转换原始图片为RGB格式
        //yuv420sp_to_rgb(240, 320, (unsigned char*) g_SIF_BUF_Send, bmp);

        yuv420p_to_rgb565_ex(480, 640, g_SIF_BUF_Send, bmp);
        //cvt_420p_to_rgb565(240, 320, (unsigned char*)g_SIF_BUF_Send, bmp2);
        //unsigned char *yuv420[3];
        //void YUV420p_to_RGB24(unsigned char *yuv420[3], unsigned char *rgb24, int width, int height)
        //YUV420p_to_RGB24((unsigned char **)g_SIF_BUF_Send, (unsigned char *)bmp2, 240, 320);

        //yuv420sp_to_rgb(width, height, (unsigned char*) g_SIF_BUF, bmp);
        // 保存RGB格式图像
        //saveRgb((unsigned char*)bmp, 3 * width * height);
        // 保存图像为JPG格式
        //LOGD("strFile[%s]\r\n",strFile);

        //RoateData(width,height, (unsigned char*)bmp, (unsigned char*)bmp1, 1);

        pthread_mutex_lock(&gRwPicMutex);
        result = jpeg_compress((char *) strFile, (unsigned char *) bmp, 480,
                               640, TRUE, 50);
        if (0 != result) {
            //  JPG图像转换成功。
            ret = result;
        }
        pthread_mutex_unlock(&gRwPicMutex);
        Free(bmp);


        // 返回的结果集
        pFace[0] = ret;
        pFace[1] = strFacialInfo.faceInfo[0].rcFace.left;
        pFace[2] = strFacialInfo.faceInfo[0].rcFace.top;
        pFace[3] = strFacialInfo.faceInfo[0].rcFace.right;
        pFace[4] = strFacialInfo.faceInfo[0].rcFace.bottom;
        pFace[5] = rotateValue;
        if (ret1 == 0) {
            pFace[6] = 1;
        } else if (ret1 == -1) {
            pFace[6] = 2;
        }
        else if (ret1 == -2) {
            pFace[6] = 3;
        }
        else {

            pFace[6] = 0;
        }

        pFace[7] = strFacialInfo.faceInfo[0].ptLeftEye.x;
        pFace[8] = strFacialInfo.faceInfo[0].ptLeftEye.y;
        pFace[9] = strFacialInfo.faceInfo[0].ptRightEye.x;
        pFace[10] = strFacialInfo.faceInfo[0].ptRightEye.y;
        pFace[11] = strFacialInfo.faceInfo[0].ptMouthPos.x;
        pFace[12] = strFacialInfo.faceInfo[0].ptMouthPos.y;
    }
    else {
        giPictureFaceFlag = 0;
    }

    delete[] g_Temp_BUF;
    delete[] g_SIF_BUF;

    //LOGD("recognizeFace() ret:%d rotateValue:%d %s %d\r\n",ret, rotateValue, __FUNCTION__, __LINE__);
    jintArray intArray = (env)->NewIntArray(sizeof(pFace) / 4);
    (env)->SetIntArrayRegion(intArray, 0, sizeof(pFace) / 4, pFace);

    (env)->ReleaseByteArrayElements(imgData, jbts, 0);
    return intArray;

}

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_initTcp(JNIEnv *env, jobject obj) {
    StartNetModule();
    return 1;
}

extern int ClientSendAuth(char *pUserName, char *pPwd, char *pMac);

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_UserAuth(JNIEnv *env, jobject obj,
                                                            jbyteArray username, jbyteArray pwd,
                                                            jbyteArray mac) {
    char chUserName[32] = {0};
    char chPwd[32] = {0};
    char chMac[32] = {0};

    //读取用户名
    jbyte *jbtsUser = (env)->GetByteArrayElements(username, 0);
    jsize lenuser = (env)->GetArrayLength(username);
    if (lenuser > sizeof(chUserName)) {
        lenuser = sizeof(chUserName);
    }
    memcpy(chUserName, jbtsUser, lenuser);

    //读取密码
    jbyte *jbtspwd = (env)->GetByteArrayElements(pwd, 0);
    jsize lenpwd = (env)->GetArrayLength(pwd);
    if (lenpwd > sizeof(chPwd)) {
        lenpwd = sizeof(chPwd);
    }
    memcpy(chPwd, jbtspwd, lenpwd);

    //读取mac地址
    jbyte *jbtsmac = (env)->GetByteArrayElements(mac, 0);
    jsize lenmac = (env)->GetArrayLength(mac);
    if (lenmac > sizeof(chMac)) {
        lenmac = sizeof(chMac);
    }
    memcpy(chMac, jbtsmac, lenmac);


    memset(g_chUserName, 0x00, sizeof(g_chUserName));
    memset(g_chPwd, 0x00, sizeof(g_chPwd));
    memset(g_chMac, 0x00, sizeof(g_chMac));

    strncpy(g_chUserName, chUserName, strlen(chUserName));
    strncpy(g_chPwd, chPwd, strlen(chPwd));
    strncpy(g_chMac, chMac, strlen(chMac));

    ClientSendAuth(g_chUserName, g_chPwd, g_chMac);

    (env)->ReleaseByteArrayElements(username, jbtsUser, 0);
    (env)->ReleaseByteArrayElements(pwd, jbtspwd, 0);

    return 1;
}

//extern int ClientSendFaceAuth(char* pUserName, char* pMac, char* pFace, int len);
extern int ClientFaceAuth(char *pUserName, char *pMac);

//用户人脸登录
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_UserFaceAuth(JNIEnv *env, jobject obj,
                                                                jbyteArray username,
                                                                jbyteArray mac) {
    //char strFile[] = "/data/data/com.firs.facedetecttosvr/temp/facePic.jpg";
    //int len = 0;
    //char* pPic = NULL;
    char chUserName[32] = {0};
    char chMac[32] = {0};
    LOGD("Java_cn_runvision_utils_FaceNative_UserFaceAuth");
    //读取用户名
    jbyte *jbtsUser = (env)->GetByteArrayElements(username, 0);
    jsize lenuser = (env)->GetArrayLength(username);
    if (lenuser > sizeof(chUserName)) {
        lenuser = sizeof(chUserName);
    }
    memcpy(chUserName, jbtsUser, lenuser);

    //读取mac地址
    jbyte *jbtsmac = (env)->GetByteArrayElements(mac, 0);
    jsize lenmac = (env)->GetArrayLength(mac);
    if (lenmac > sizeof(chMac)) {
        lenmac = sizeof(chMac);
    }
    memcpy(chMac, jbtsmac, lenmac);


    memset(g_chUserName, 0x00, sizeof(g_chUserName));
    memset(g_chMac, 0x00, sizeof(g_chMac));
    strncpy(g_chUserName, chUserName, strlen(chUserName));
    strncpy(g_chMac, chMac, strlen(chMac));

/*
	LOGD("READ PIC FILE");
	pthread_mutex_lock(&gRwPicMutex);
	ReadFile(strFile, &pPic, &len);
	pthread_mutex_unlock(&gRwPicMutex);
	LOGD("SEND PIC FILE  len[%d]",len);
	
	ClientSendFaceAuth(g_chUserName,g_chMac,pPic,len);
	LOGD("ClientSendFaceAuth end");
	*/
    ClientFaceAuth(g_chUserName, g_chMac);

    (env)->ReleaseByteArrayElements(username, jbtsUser, 0);
    (env)->ReleaseByteArrayElements(mac, jbtsmac, 0);

    /*
    if(pPic != NULL)
    {
        free(pPic);
        pPic = NULL;
    }
    */
    return 1;
}


//清空用户名和密码
void clearUserName() {
    memset(g_chUserName, 0x00, sizeof(g_chUserName));
    memset(g_chPwd, 0x00, sizeof(g_chPwd));
}

extern int SetClientIP(char *pclientIp, int clientPort, int ipChanged);

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_SetServerIP(JNIEnv *env, jobject obj,
                                                               jbyteArray ip, jint iPort,
                                                               jint ipChanged) {
    char acIp[16] = {0};

    // 传入的数据
    jbyte *jbts = (env)->GetByteArrayElements(ip, 0);
    //数据长度
    jsize len = (env)->GetArrayLength(ip);
    // 清空用户名，重新登录验证
    clearUserName();

    if (len > sizeof(acIp)) {
        len = sizeof(acIp);
    }

    memcpy(acIp, jbts, len);

    setServerSockFlag(0);

    SetClientIP(acIp, iPort, ipChanged);

    (env)->ReleaseByteArrayElements(ip, jbts, 0);

}

extern int getAuth();

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getAuth(JNIEnv *env, jobject obj) {
    return getAuth();
}

extern int getCompareFlag();

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getCompareFlag(JNIEnv *env, jobject obj) {
    return getCompareFlag();
}

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getPictureFaceFlag(JNIEnv *env, jobject obj) {
    return giPictureFaceFlag;
}

//JNIEXPORT jobject  JNICALL Java_com_re_struct_MainActivity_call(JNIEnv * env, jobject obj, jobject classobj)  
JNIEXPORT jobject  JNICALL Java_cn_runvision_utils_FaceNative_getCompareResult(JNIEnv *env, jobject obj,
                                                                        jobject classobj,
                                                                        int faceNum) {
    char outbuf[64] = {0};
    COMPARE_RESULT cmprest;
    jclass objectClass = env->FindClass("cn/runvision/utils/compareresult");

    jfieldID name = env->GetFieldID(objectClass, "name", "Ljava/lang/String;");
    jfieldID num = env->GetFieldID(objectClass, "score", "I");
    jfieldID num2 = env->GetFieldID(objectClass, "faceId", "I");

    cmprest = get_CompareResult(faceNum);
    GB2312ToUTF_8(outbuf, cmprest.f_name, strlen(cmprest.f_name));

    env->SetObjectField(classobj, name, env->NewStringUTF(outbuf));
    env->SetIntField(classobj, num, cmprest.score);
    env->SetIntField(classobj, num2, cmprest.f_id);
    return classobj;
}

JNIEXPORT int  JNICALL Java_cn_runvision_utils_FaceNative_get1T1CompareResult(JNIEnv *env, jobject obj) {
    COMPARE_1T1_RESULT i1T1 = get_1T1CompareResult();
    return i1T1.score;
}

extern int snedPictureToSvr(char *pPicture, int len);

extern void setCompareFlag(int iCompareFlag);

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_sendPicture(JNIEnv *env, jobject obj) {
    char *cSIF_BUF = new char[SIF_HEIGHT * SIF_WIDTH * 2];
    memcpy(cSIF_BUF, g_SIF_BUF_Send, SIF_HEIGHT * SIF_WIDTH * 2);

    char strFile[] = "/data/data/cn.runvision.facedetect/temp/facePic.jpg";
    char strFile2[] = "/data/data/cn.runvision.facedetect/temp/facePic_temp.jpg";
    int len = 0;
    char *pPic = NULL;

    LOGD("READ PIC FILE");
    pthread_mutex_lock(&gRwPicMutex);
    ReadFile(strFile, &pPic, &len);
    pthread_mutex_unlock(&gRwPicMutex);
    LOGD("SEND PIC FILE  len[%d]", len);
    setCompareFlag(-1);
    snedPictureToSvr(pPic, len);
    LOGD("SENDED PIC FILE");
    //void saveFile(const char* pSrcFile,unsigned char* pRawBuf, int iLen)
    saveFile(strFile2, (unsigned char *) pPic, len);
    if (pPic != NULL) {
        free(pPic);
        pPic = NULL;
    }
    delete[] cSIF_BUF;
}

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_sendPicture1T1(JNIEnv *env, jobject obj) {
    char strFile[] = "/data/data/cn.runvision.facedetect/temp/facePic.jpg";
    char strFile2[] = "/data/data/cn.runvision.facedetect/temp/facePic_temp.jpg";
    char strFile1T1[] = "/data/data/cn.runvision.facedetect/temp/idcardpic.jpg";
    int len = 0;
    int len1T1 = 0;
    char *pPic = NULL;
    char *pPic1T1 = NULL;

    LOGD("READ PIC FILE");
    pthread_mutex_lock(&gRwPicMutex);
    ReadFile(strFile, &pPic, &len);
    pthread_mutex_unlock(&gRwPicMutex);
    LOGD("SEND PIC FILE  len[%d]", len);

    ReadFile(strFile1T1, &pPic1T1, &len1T1);
    LOGD("SEND PIC1T1 FILE  len1T1[%d]", len1T1);
    setCompareFlag(-1);
    sendPictureToSvr1T1(pPic, len, pPic1T1, len1T1);
    LOGD("SENDED PIC FILE");
    //void saveFile(const char* pSrcFile,unsigned char* pRawBuf, int iLen)
    saveFile(strFile2, (unsigned char *) pPic, len);
    if (pPic != NULL) {
        free(pPic);
        pPic = NULL;
    }
    if (pPic1T1 != NULL) {
        free(pPic1T1);
        pPic1T1 = NULL;
    }
}

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_RegisterFace(JNIEnv *env, jobject obj) {
    char strFile[] = "/data/data/cn.runvision.facedetect/temp/facePic.jpg";
    int len = 0;
    char *pPic = NULL;

    LOGD("READ PIC FILE");
    pthread_mutex_lock(&gRwPicMutex);
    ReadFile(strFile, &pPic, &len);
    pthread_mutex_unlock(&gRwPicMutex);
    LOGD("SEND PIC FILE  len[%d]", len);
    setRegisterFaceFlag(-1);
    sendRegisterFace(pPic, len);
    LOGD("SENDED PIC FILE");

    if (pPic != NULL) {
        free(pPic);
        pPic = NULL;
    }
}

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getRegisterFaceResult(JNIEnv *env, jobject obj) {
    return getRegisterFaceFlag();
}

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_ModifyPasswd(JNIEnv *env, jobject obj,
                                                                jbyteArray username,
                                                                jbyteArray oldpasswd,
                                                                jbyteArray newpasswd) {
    char chUserName[32] = {0};
    char chOldPasswd[32] = {0};
    char chNewPasswd[32] = {0};

    //读取用户名
    jbyte *jbtsUser = (env)->GetByteArrayElements(username, 0);
    jsize lenuser = (env)->GetArrayLength(username);
    if (lenuser > sizeof(chUserName)) {
        lenuser = sizeof(chUserName);
    }
    memcpy(chUserName, jbtsUser, lenuser);

    //读取oldpasswd地址
    jbyte *jbtsOld = (env)->GetByteArrayElements(oldpasswd, 0);
    jsize lenOld = (env)->GetArrayLength(oldpasswd);
    if (lenOld > sizeof(chOldPasswd)) {
        lenOld = sizeof(chOldPasswd);
    }
    memcpy(chOldPasswd, jbtsOld, lenOld);

    //读取newpasswd地址
    jbyte *jbtsNew = (env)->GetByteArrayElements(newpasswd, 0);
    jsize lenNew = (env)->GetArrayLength(newpasswd);
    if (lenNew > sizeof(chNewPasswd)) {
        lenNew = sizeof(chNewPasswd);
    }
    memcpy(chNewPasswd, jbtsNew, lenNew);

    //ClientFaceAuth(g_chUserName,g_chMac);
    setModifyPasswdFlag(-1);
    sendModifyPasswd(chUserName, chOldPasswd, chNewPasswd);

    (env)->ReleaseByteArrayElements(username, jbtsUser, 0);
    (env)->ReleaseByteArrayElements(oldpasswd, jbtsOld, 0);
    (env)->ReleaseByteArrayElements(newpasswd, jbtsNew, 0);
}

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getModifyPasswdResult(JNIEnv *env, jobject obj) {
    return getModifyPasswdFlag();
}

//拍摄人脸模板
JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_takeMouldPic(JNIEnv *env, jobject obj) {
    char *cSIF_BUF = new char[SIF_HEIGHT * SIF_WIDTH * 2];
    memcpy(cSIF_BUF, g_SIF_BUF_Send, SIF_HEIGHT * SIF_WIDTH * 2);

    char strFile[] = "/data/data/cn.runvision.facedetect/temp/facePic.jpg";
    char strFile2[] = "/data/data/cn.runvision.facedetect/temp/idcardpic.jpg";
    int len = 0;
    char *pPic = NULL;

    pthread_mutex_lock(&gRwPicMutex);
    ReadFile(strFile, &pPic, &len);
    pthread_mutex_unlock(&gRwPicMutex);
    LOGD("takeMouldPIC FILE  len[%d]", len);
    //void saveFile(const char* pSrcFile,unsigned char* pRawBuf, int iLen)
    saveFile(strFile2, (unsigned char *) pPic, len);
    if (pPic != NULL) {
        free(pPic);
        pPic = NULL;
    }
    delete[] cSIF_BUF;
}

extern void setThreadExit();

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_setThreadExit(JNIEnv *env, jobject obj) {
    setThreadExit();
}

JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_getServerSockFlag(JNIEnv *env, jobject obj) {
    return getServerSockFlag();
}

JNIEXPORT void JNICALL Java_cn_runvision_utils_FaceNative_SetScore(JNIEnv *env, jobject obj, jint faceNum,
                                                            jint iScore, jint iScore1to1) {
    //g_Score = iScore;
    g_LimitScore.faceNum = faceNum;
    g_LimitScore.score = iScore;
    g_LimitScore.score1to1 = iScore1to1;
}

//下载apk
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_ftpUpdateApp(JNIEnv *env, jobject obj) {
    int sock = -1;
    int ret = -1;
    unsigned long long stor_size = 0;
    int stop = 0;
    //ftp_quit(1);
    sock = ftp_connect(getSvrIP(), 2121, "anonymous", "2121@hotmail.com");
    //LOGD("ftp_connect  sock[%d]",sock);
    ret = ftp_retrfile(sock, "FaceDetectToSvr.apk", "/mnt/sdcard/FaceDetectToSvr.apk", &stor_size,
                       &stop);
    LOGD("ftp_connect  sock[%d] ret[%d]", sock, ret);
    ftp_quit(sock);
    return ret;
}
//下载apk版本信息
JNIEXPORT jint JNICALL Java_cn_runvision_utils_FaceNative_ftpApkVersion(JNIEnv *env, jobject obj) {
    int sock = -1;
    unsigned long long stor_size = 0;
    int stop = 0;
    int ret = -1;
    //ftp_quit(1);
    //sock = ftp_connect(getSvrIP(), 2121, "usertest", "App2016" );
    sock = ftp_connect(getSvrIP(), 2121, "anonymous", "2121@hotmail.com");
    //LOGD("ftp_connect  sock[%d]",sock);
    ret = ftp_retrfile(sock, "apkversion.xml",
                       "/data/data/cn.runvision.facedetect/shared_prefs/apkversion.xml",
                       &stor_size, &stop);
    ftp_quit(sock);
    return ret;
}




