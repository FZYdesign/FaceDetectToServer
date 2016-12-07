#include <pthread.h> 
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#ifndef __PUBLIC_H__
#define __PUBLIC_H__

#define R unsigned char
#define G unsigned char
#define B unsigned char
//每个像素的字节数
#define m_nBytesPerPixel  3

#define RGB unsigned long
#define RGB16 unsigned short

#define MAKERGB(r,g,b) (RGB)( ((r)<<16) | ((g)<<8) | (b) )
#define RGB_TO_RGB565(c) (RGB16)( ((((c)&0xff0000)>>19)<<11) | ((((c)&0xff00)>>10)<<5) | (((c)&0xff)>>3) )
#define RGB565_TO_RGB(c) (RGB)( (((c)&0x1f)<<3) | ((((c)>>5)&0x3f)<<10) | (((c)>>11)<<19)  | 0x070307)

#define Malloc(s) malloc(s)
#define Free(p) \
if(p != NULL)   \
{               \
	free(p);    \
	p = NULL;   \
}

#define PRINT_DEBUG
#ifndef TRACE
#ifdef PRINT_DEBUG

#define TRACE LOGD

#else

#define TRACE(...)

#endif
#endif

///////////////////////////////////////////////////////////////////////////////////////
// 基本类型定义
///////////////////////////////////////////////////////////////////////////////////////
#if defined(WIN32) || defined(OS_WINDOWS) || defined(SDK_WIN32)

	typedef char			*LPCHAR;

#else
	
	#ifndef BOOL
		typedef int				BOOL;
	#endif
	
	#ifndef BYTE
		typedef unsigned char	BYTE;
	#endif
	
	#ifndef SHORT
		typedef unsigned short	SHORT;
	#endif
	
	#ifndef UINT
		typedef unsigned int	UINT;
	#endif 
	
	#ifndef WORD
		typedef unsigned short	WORD;
	#endif
	
	#ifndef DWORD
		typedef unsigned int	DWORD;
	#endif
	
	#ifndef LONG
		typedef long			LONG;
	#endif
	
	#ifndef LPCHAR
		typedef char			*LPCHAR;
	#endif
	
	#ifndef LPBYTE
		typedef unsigned char   *LPBYTE;
	#endif	
		
	#ifndef HANDLE
		typedef void			*HANDLE;
	#endif

	#ifndef FALSE
		#define FALSE	0
	#endif
	
	#ifndef TRUE
		#define TRUE	1
	#endif
	
	#ifndef NULL
		#define NULL	0
	#endif
	
	#ifndef IN
		#define IN
	#endif
	
	#ifndef OUT
		#define OUT
	#endif
#endif



// 设置特征共享缓冲区大小（默认是20M） 
#define OD_CODEC_BUF_SIZE  (1 * 1024)
#define FACIAL_LIB_PATH     "/mnt/sdcard/faciallib"    /* 算法配置目录 */

#define GENERAL_PATH_LEN		128		//路径的长度
#define MAX_DIR_USER_NUM		2000	//每个目录下最大目录数  防止每个目录下用户数过多
#define USER_DATA_DIR_NAME		"/mnt/sdcard/userData"    //用户信息保存路径

#define FEATURESIZE_GENERAL     1024        /* 特征值长度 单位 byte */    
#define MAX_FEATURE_NUM         10          /* 模板数 */

#define MAX_USER_NUM			5000	/* 最大人数 */

#define FACEDETECT_FIND_NO_FACE		-255	/* 检测不到人脸场景时返回的错误码 */

/* 摄像头部分 */
#if 0
#define SCREEN_WIDTH        240
#define SCREEN_HEIGHT       320
#else
#define SCREEN_WIDTH        320
#define SCREEN_HEIGHT       240

#endif

#define VIDEO_CHANNEL_IRDA  0
#define VIDEO_CHANNEL_COLOR 1

#define V4L2_DEVICE_IRDA    "/dev/video0"

#define CLEAR(obj) memset(&obj, 0, sizeof(obj))

#define DISP_FMT_YCbCr422	(1 << 30)

#define YCbCr422_MASK		(DISP_FMT_YCbCr422 | 0xFFFF)
#define YCbCr422_Y0CbY1Cr	(DISP_FMT_YCbCr422 | 0x0000)
#define YCbCr422_CbY1CrY0	(DISP_FMT_YCbCr422 | 0x0001)
#define YCbCr422_Y1CrY0Cb	(DISP_FMT_YCbCr422 | 0x0002)
#define YCbCr422_CrY0CbY1	(DISP_FMT_YCbCr422 | 0x0004)
#define YCbCr422_CrY1CbY0	(DISP_FMT_YCbCr422 | 0x0008)
#define YCbCr422_Y1CbY0Cr	(DISP_FMT_YCbCr422 | 0x0010)
#define YCbCr422_CbY0CrY1	(DISP_FMT_YCbCr422 | 0x0020)
#define YCbCr422_Y0CrY1Cb	(DISP_FMT_YCbCr422 | 0x0040)

#define V4L2_FRM_FMT_PLANAR     ('S')         /* planar frame format */
#define V4L2_FRM_FMT_TILE       ('T')         /* YUV420 tile(macro block) mode */
#define V4L2_FRM_FMT_PACK       ('P')         /* package frame format */

//彩色 yuv420SP to RGB244
#define YCbCrtoR(Y,Cb,Cr)       (100*Y + 114*(Cr-128))/100
#define YCbCrtoG(Y,Cb,Cr)       (100*Y - 39*(Cb-128) - 58*(Cr-128))/100
#define YCbCrtoB(Y,Cb,Cr)       (100*Y + 203*(Cb-128))/100
/*
#define YCbCrtoR(Y,Cb,Cr)       (1000*Y + 1402*(Cr-128))/1000
#define YCbCrtoG(Y,Cb,Cr)       (1000*Y - 344*(Cb-128) - 714*(Cr-128))/1000
#define YCbCrtoB(Y,Cb,Cr)       (1000*Y + 1772*(Cb-128))/1000

#define YCbCrtoR(Y,Cb,Cr)       (1000*Y + 1371*(Cr-128))/1000
#define YCbCrtoG(Y,Cb,Cr)       (1000*Y - 336*(Cb-128) - 698*(Cr-128))/1000
#define YCbCrtoB(Y,Cb,Cr)       (1000*Y + 1732*(Cb-128))/1000

#define YCbCrtoR(Y,Cb,Cr)       (100*Y + 114*(Cr-128))/100
#define YCbCrtoG(Y,Cb,Cr)       (100*Y - 39*(Cb-128) - 58*(Cr-128))/100
#define YCbCrtoB(Y,Cb,Cr)       (100*Y + 203*(Cb-128))/100
*/


/*
 * [31:24]: 'S'/'P'/'T' => CIMCFG.SEP,	planar(separate) mode / package mode / tile mode(only for YUV420)
 * [23:16]: 0 ~ 7 => CIMCFG.PACK
 * [15:8] : reserved
 * [7:0]  : reserved
 */
#define cim_set_fmt_priv(a, b, c, d)	\
	((unsigned int)(a) | ((unsigned int)(b) << 8) | ((unsigned int)(c) << 16) | ((unsigned int)(d) << 24))

#define NB_BUFFER 2  

static const signed short redAdjust[] = {
-161,-160,-159,-158,-157,-156,-155,-153,
-152,-151,-150,-149,-148,-147,-145,-144,
-143,-142,-141,-140,-139,-137,-136,-135,
-134,-133,-132,-131,-129,-128,-127,-126,
-125,-124,-123,-122,-120,-119,-118,-117,
-116,-115,-114,-112,-111,-110,-109,-108,
-107,-106,-104,-103,-102,-101,-100, -99,
-98, -96, -95, -94, -93, -92, -91, -90,
-88, -87, -86, -85, -84, -83, -82, -80,
-79, -78, -77, -76, -75, -74, -72, -71,
-70, -69, -68, -67, -66, -65, -63, -62,
-61, -60, -59, -58, -57, -55, -54, -53,
-52, -51, -50, -49, -47, -46, -45, -44,
-43, -42, -41, -39, -38, -37, -36, -35,
-34, -33, -31, -30, -29, -28, -27, -26,
-25, -23, -22, -21, -20, -19, -18, -17,
-16, -14, -13, -12, -11, -10,  -9,  -8,
-6,  -5,  -4,  -3,  -2,  -1,   0,   1,
2,   3,   4,   5,   6,   7,   9,  10,
11,  12,  13,  14,  15,  17,  18,  19,
20,  21,  22,  23,  25,  26,  27,  28,
29,  30,  31,  33,  34,  35,  36,  37,
38,  39,  40,  42,  43,  44,  45,  46,
47,  48,  50,  51,  52,  53,  54,  55,
56,  58,  59,  60,  61,  62,  63,  64,
66,  67,  68,  69,  70,  71,  72,  74,
75,  76,  77,  78,  79,  80,  82,  83,
84,  85,  86,  87,  88,  90,  91,  92,
93,  94,  95,  96,  97,  99, 100, 101,
102, 103, 104, 105, 107, 108, 109, 110,
111, 112, 113, 115, 116, 117, 118, 119,
120, 121, 123, 124, 125, 126, 127, 128,
};

static const signed short greenAdjust1[] = {
34,  34,  33,  33,  32,  32,  32,  31,
31,  30,  30,  30,  29,  29,  28,  28,
28,  27,  27,  27,  26,  26,  25,  25,
25,  24,  24,  23,  23,  23,  22,  22,
21,  21,  21,  20,  20,  19,  19,  19,
18,  18,  17,  17,  17,  16,  16,  15,
15,  15,  14,  14,  13,  13,  13,  12,
12,  12,  11,  11,  10,  10,  10,   9,
9,   8,   8,   8,   7,   7,   6,   6,
6,   5,   5,   4,   4,   4,   3,   3,
2,   2,   2,   1,   1,   0,   0,   0,
0,   0,  -1,  -1,  -1,  -2,  -2,  -2,
-3,  -3,  -4,  -4,  -4,  -5,  -5,  -6,
-6,  -6,  -7,  -7,  -8,  -8,  -8,  -9,
-9, -10, -10, -10, -11, -11, -12, -12,
-12, -13, -13, -14, -14, -14, -15, -15,
-16, -16, -16, -17, -17, -17, -18, -18,
-19, -19, -19, -20, -20, -21, -21, -21,
-22, -22, -23, -23, -23, -24, -24, -25,
-25, -25, -26, -26, -27, -27, -27, -28,
-28, -29, -29, -29, -30, -30, -30, -31,
-31, -32, -32, -32, -33, -33, -34, -34,
-34, -35, -35, -36, -36, -36, -37, -37,
-38, -38, -38, -39, -39, -40, -40, -40,
-41, -41, -42, -42, -42, -43, -43, -44,
-44, -44, -45, -45, -45, -46, -46, -47,
-47, -47, -48, -48, -49, -49, -49, -50,
-50, -51, -51, -51, -52, -52, -53, -53,
-53, -54, -54, -55, -55, -55, -56, -56,
-57, -57, -57, -58, -58, -59, -59, -59,
-60, -60, -60, -61, -61, -62, -62, -62,
-63, -63, -64, -64, -64, -65, -65, -66,
};

static const signed short greenAdjust2[] = {
 74,  73,  73,  72,  71,  71,  70,  70,
69,  69,  68,  67,  67,  66,  66,  65,
65,  64,  63,  63,  62,  62,  61,  60,
60,  59,  59,  58,  58,  57,  56,  56,
55,  55,  54,  53,  53,  52,  52,  51,
51,  50,  49,  49,  48,  48,  47,  47,
46,  45,  45,  44,  44,  43,  42,  42,
41,  41,  40,  40,  39,  38,  38,  37,
37,  36,  35,  35,  34,  34,  33,  33,
32,  31,  31,  30,  30,  29,  29,  28,
27,  27,  26,  26,  25,  24,  24,  23,
23,  22,  22,  21,  20,  20,  19,  19,
18,  17,  17,  16,  16,  15,  15,  14,
13,  13,  12,  12,  11,  11,  10,   9,
9,   8,   8,   7,   6,   6,   5,   5,
4,   4,   3,   2,   2,   1,   1,   0,
0,   0,  -1,  -1,  -2,  -2,  -3,  -4,
-4,  -5,  -5,  -6,  -6,  -7,  -8,  -8,
-9,  -9, -10, -11, -11, -12, -12, -13,
-13, -14, -15, -15, -16, -16, -17, -17,
-18, -19, -19, -20, -20, -21, -22, -22,
-23, -23, -24, -24, -25, -26, -26, -27,
-27, -28, -29, -29, -30, -30, -31, -31,
-32, -33, -33, -34, -34, -35, -35, -36,
-37, -37, -38, -38, -39, -40, -40, -41,
-41, -42, -42, -43, -44, -44, -45, -45,
-46, -47, -47, -48, -48, -49, -49, -50,
-51, -51, -52, -52, -53, -53, -54, -55,
-55, -56, -56, -57, -58, -58, -59, -59,
-60, -60, -61, -62, -62, -63, -63, -64,
-65, -65, -66, -66, -67, -67, -68, -69,
-69, -70, -70, -71, -71, -72, -73, -73,
};

static const signed short blueAdjust[] = {
-276,-274,-272,-270,-267,-265,-263,-261,
-259,-257,-255,-253,-251,-249,-247,-245,
-243,-241,-239,-237,-235,-233,-231,-229,
-227,-225,-223,-221,-219,-217,-215,-213,
-211,-209,-207,-204,-202,-200,-198,-196,
-194,-192,-190,-188,-186,-184,-182,-180,
-178,-176,-174,-172,-170,-168,-166,-164,
-162,-160,-158,-156,-154,-152,-150,-148,
-146,-144,-141,-139,-137,-135,-133,-131,
-129,-127,-125,-123,-121,-119,-117,-115,
-113,-111,-109,-107,-105,-103,-101, -99,
-97, -95, -93, -91, -89, -87, -85, -83,
-81, -78, -76, -74, -72, -70, -68, -66,
-64, -62, -60, -58, -56, -54, -52, -50,
-48, -46, -44, -42, -40, -38, -36, -34,
-32, -30, -28, -26, -24, -22, -20, -18,
-16, -13, -11,  -9,  -7,  -5,  -3,  -1,
0,   2,   4,   6,   8,  10,  12,  14,
16,  18,  20,  22,  24,  26,  28,  30,
32,  34,  36,  38,  40,  42,  44,  46,
49,  51,  53,  55,  57,  59,  61,  63,
65,  67,  69,  71,  73,  75,  77,  79,
81,  83,  85,  87,  89,  91,  93,  95,
97,  99, 101, 103, 105, 107, 109, 112,
114, 116, 118, 120, 122, 124, 126, 128,
130, 132, 134, 136, 138, 140, 142, 144,
146, 148, 150, 152, 154, 156, 158, 160,
162, 164, 166, 168, 170, 172, 175, 177,
179, 181, 183, 185, 187, 189, 191, 193,
195, 197, 199, 201, 203, 205, 207, 209,
211, 213, 215, 217, 219, 221, 223, 225,
227, 229, 231, 233, 235, 238, 240, 242,
};

//存储2 个像素点
struct RGB24x2
{
    R r0;
	G g0;
	B b0;
    R r1;
	G g1;
	B b1;

};

struct buffer  
{  
	void *start;  
	size_t length;  
};  

struct format{
	unsigned int fourcc;	// compatible with V4L2
	unsigned int format;	// local definition, more detail
	unsigned int fmt_priv;	// CIMCFG.PACK & CIMCFG.SEP && CIMCR.MBEN
};

struct VIP
{
	char dev_name[64];
	int fd;
	int width;
	int height;
	struct format fmt;//int pixel_fmt;
	struct buffer buffers[NB_BUFFER];
};

typedef struct BMP 
{ // bm 
	long nWidth;
	long nHeight;
	long nWidthBytes;
	short nBitsPerPixel;
	int nPal;
	RGB pal[256];
	char* pBits;
	
public:
	BMP();
	~BMP();
	
} BMP, * LPBMP;

class CMutex
{
	pthread_mutex_t m_Mutex;
public:
	CMutex();
	~CMutex();
	void Lock();
	void Unlock();
};

BOOL ReadFile(const char* pSrcFile, char** pBuf, int* pLen);


int jpeg_compress(char* pFileName, unsigned char* pData, int nWidth, int nHeight, int bColor, int nQuality);

LPBMP CreateBmpFromYBuf(int nWidth, int nHeight, char* pYBuf);

BOOL SaveJpg(char* pFileName, LPBMP pBmp, BOOL bColor, int nQuality);

static inline void yuv2rgb565(const int y, const int u, const int v, unsigned short *rgb565);

void Yuyv2Rgb565Rotate90Mirror(const void *pYuyvData, void *pRgb565Data, const unsigned int w, const unsigned int h);

void Yuyv2yuv420pRotate90Mirror(const void *pYuyvData, void *pYuv420pData, const unsigned int w, const unsigned int h);

/**************************************
direct: 1 ---顺时针
        0 ---逆时针

**************************************/
void* rotate90_yuv420p(void* pYuv420, int nWidth, int nHeight, void*pOutYuv420, BOOL bUV, int nDirect);

void YUV420SPToYUV420P ( char * yuv420sp,  char * yuv420,  int  width,  int  height );
LPBMP CreateBmpFromYUV420PBuf(int nWidth, int nHeight, char* pYuv420pBuf);

void yuv420p_to_rgb565_ex(int size_x, int size_y, char * pSrcBuf, RGB24x2 *pbuff);

void cvt_420p_to_rgb565(int width, int height, unsigned char *src, unsigned short *dst);
void YUV420p_to_RGB24(unsigned char *yuv420[3], unsigned char *rgb24, int width, int height);


void* rotate90_yuv420sp(void* pYuv420, int nWidth, int nHeight, void*pOutYuv420, BOOL bUV, int nDirect);

void saveRaw(unsigned char* pRawBuf, int iLen);

void saveRgb(unsigned char * pRawBuf,int iLen);

void saveFile(const char* pSrcFile,unsigned char* pRawBuf, int iLen);

static RGB24x2 Conv_YCbCr_Rgb24x2(unsigned char y0, unsigned char y1, unsigned char cb0, unsigned char cr0);

void yuv420sp_to_rgb(int width, int height, unsigned char * pSrcBuf, RGB24x2 *pbuff);
#endif
