#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "public.h"
#include "external/include/jpeglib.h"
#include "facelib.h"

int giBytesPerPixel = 2;
int giDepth = 16;
//YUV2RGB�㷨����
int D[256], F[256], E[256];
////////////////////////////////////////////////////////////////////////////////
// CMutex class

CMutex::CMutex() {
    pthread_mutex_init(&m_Mutex, NULL);
}

CMutex::~CMutex() {
    pthread_mutex_destroy(&m_Mutex);
}

void CMutex::Lock() {
    pthread_mutex_lock(&m_Mutex);
}

void CMutex::Unlock() {
    pthread_mutex_unlock(&m_Mutex);
}

///////////////////////////////////////////////////////////////////
//
// BMP struct
//
BMP::BMP() {
    pBits = NULL;
}

BMP::~BMP() {
    if (pBits) Free(pBits);
    pBits = NULL;
}


/////////////////////////////////////////////////
//
// *** COLOR SPACE CONVERT ***
//
// YUV to RGB:
//	R = Y - 0.0009267*(U-128) + 1.4016868*(V-128)
//	G = Y - 0.3436954*(U-128) - 0.7141690*(V-128)
//	B = Y + 1.7721604*(U-128) + 0.0009902*(V-128)
// RGB to YUV:
//	Y = 0.299*R + 0.587*G + 0.114*B
//	U = -0.169*R - 0.331*G +0.5*B +128
//	V = 0.5*R -0.419*G -0.081*B + 128
/////////////////////////////////////////////////
RGB yuv2rgb(BYTE y, BYTE u, BYTE v) {
    int r, g, b;

    r = ((y << 8) + 359 * (v - 128)) >> 8;
    g = ((y << 8) - 88 * (u - 128) - 183 * (v - 128)) >> 8;
    b = ((y << 8) + 454 * (u - 128)) >> 8;

    if (r < 0)
        r = 0;
    else if (r > 255)
        r = 255;

    if (g < 0)
        g = 0;
    else if (g > 255)
        g = 255;

    if (b < 0)
        b = 0;
    else if (b > 255)
        b = 255;

    return MAKERGB(r, g, b);
}

/******************************************************************************
 * �������ƣ� CFramebuffer.CreateBmpFromYBuf
 * ���ܣ� ��Y�����������Bmpͼ��
 * ������ 	nWidth: ���
 			nHeight: �߶�
 			pYBuf: Y��������
 * ���أ� ���BMPͼ��
 * �������ߣ� 
 * �������ڣ� 2012-6-18
 * �޸����ߣ�
 * �޸����ڣ�
 ******************************************************************************/
LPBMP CreateBmpFromYBuf(int nWidth, int nHeight, char *pYBuf) {
    LPBMP pBmp = new BMP;
    if (pBmp == NULL)
        return NULL;
    memset(pBmp, 0, sizeof(BMP));

    pBmp->nWidth = nWidth;
    pBmp->nHeight = nHeight;
    pBmp->nBitsPerPixel = giBytesPerPixel * 8;
    pBmp->nWidthBytes = pBmp->nWidth * giBytesPerPixel;

    pBmp->pBits = (char *) Malloc(pBmp->nWidthBytes * pBmp->nHeight);
    if (pBmp->pBits == NULL) {
        delete pBmp;
        pBmp = NULL;
    }
    else {
        int i, j;
        unsigned char *pDst = (unsigned char *) pBmp->pBits, *pY = (unsigned char *) pYBuf;

        RGB rgb;
        for (j = 0; j < nHeight; j++) {
            for (i = 0; i < nWidth; i++) {
                rgb = yuv2rgb(*pY++, 0x80, 0x80);
                if (giDepth == 8);//*pDst = MatchColor(rgb);
                else if (giDepth == 16)
                    *((RGB16 *) pDst) = RGB_TO_RGB565(rgb);
                else if (giDepth == 24 || giDepth == 32)
                    memcpy(pDst, &rgb, giDepth / 8);
                else
                    memset(pDst, 0, giDepth / 8);
                pDst += giDepth / 8;
            }
        }
    }

    return pBmp;
}

void jpeg_error_exit(j_common_ptr cinfo) {
}

int jpeg_compress(char *pFileName, unsigned char *pData, int nWidth, int nHeight, int bColor,
                  int nQuality) {
    int ret = 0;
    FILE *outfile = fopen(pFileName, "wb"); /* target file */
    if (outfile == NULL) {
        LOGD("JPEG: Save JPEG Can not open file %s\r\n", pFileName);
        ret = -1;
    } else {
        struct jpeg_compress_struct cinfo;

        struct jpeg_error_mgr emgr;

        /* Step 1: allocate and initialize JPEG compression object */
        cinfo.err = jpeg_std_error(&emgr);
        emgr.error_exit = jpeg_error_exit;

        /* Now we can initialize the JPEG compression object. */
        jpeg_create_compress(&cinfo);

        /* Step 2: specify data destination (eg, a file) */
        jpeg_stdio_dest(&cinfo, outfile);

        /* Step 3: set parameters for compression */

        /* First we supply a description of the input image.
        * Four fields of the cinfo struct must be filled in:
        */
        cinfo.image_width = nWidth;    /* image widthPix and height, in pixels */
        cinfo.image_height = nHeight;
        if (bColor) {
            cinfo.input_components = 3;        /* # of color components per pixel */
            cinfo.in_color_space = JCS_RGB;    /* colorspace of input image */
        } else {
            cinfo.input_components = 1;        /* # of color components per pixel */
            cinfo.in_color_space = JCS_GRAYSCALE;    /* colorspace of input image */
        }

        /* Now use the library's routine to set default compression parameters.
        * (You must set at least cinfo.in_color_space before calling this,
        * since the defaults depend on the source color space.)
        */

        jpeg_set_defaults(&cinfo);

        /* Now you can set any non-default parameters you wish to.
        * Here we just illustrate the use of quality (quantization table) scaling:
        */
        jpeg_set_quality(&cinfo, nQuality, TRUE /* limit to baseline-JPEG values */);

        /* Step 4: Start compressor */

        /* TRUE ensures that we will write a complete interchange-JPEG file.
        * Pass TRUE unless you are very sure of what you're doing.
        */

        jpeg_start_compress(&cinfo, TRUE);

        /* Step 5: while (scan lines remain to be written) */
        /*           jpeg_write_scanlines(...); */

        /* Here we use the library's state variable cinfo.next_scanline as the
        * loop counter, so that we don't have to keep track ourselves.
        * To keep things simple, we pass one scanline per call; you can pass
        * more if you wish, though.
        */

        while (cinfo.next_scanline < cinfo.image_height) {
            /* jpeg_write_scanlines expects an array of pointers to scanlines.
             * Here the array is only one element long, but you could pass
             * more than one scanline at a time if that's more convenient.
             */
            jpeg_write_scanlines(&cinfo, &pData, 1);
            if (bColor) pData += nWidth * 3;
            else pData += nWidth;
        }

        /* Step 6: Finish compression */
        jpeg_finish_compress(&cinfo);

        /* Step 7: release JPEG compression object */

        /* This is an important step since it will release a good deal of memory. */
        jpeg_destroy_compress(&cinfo);

        /* After finish_compress, we can close the output file. */
        fclose(outfile);
    }
    return ret;
}

/******************************************************************************
 * �������ƣ� CFramebuffer.SaveJpg
 * ���ܣ� ����JPGͼƬ
 * ������ pFileName�����ͼƬ��
 		pBMP:�����ͼƬ����
 		bColor����ɫ��ڰ�ͼƬ
 		nQuality:ͼ������
 * ���أ� 
 * �������ߣ� 
 * �������ڣ� 
 * �޸����ߣ�
 * �޸����ڣ�2012-6-14
 ******************************************************************************/
BOOL SaveJpg(char *pFileName, LPBMP pBmp, BOOL bColor, int nQuality) {
    BOOL bRet = FALSE;
    if (pBmp == NULL)
        return bRet;

    if (pBmp->pBits == NULL)
        return bRet;

    RGB rgb;
    unsigned char r = 0, g = 0, b = 0, *pDataBuf = (unsigned char *) Malloc(
            pBmp->nWidth * pBmp->nHeight * 3);
    if (pDataBuf == NULL) {
        TRACE("CFramebuffer::SaveJpg: Save JPEG Out Of Memory (%ld,%ld,%ld,%d)!\r\n",
              pBmp->nWidth * pBmp->nHeight * 3, pBmp->nWidth, pBmp->nHeight, giBytesPerPixel);
        return bRet;
    }

    unsigned char *pSrc = (unsigned char *) pBmp->pBits, *pDst = pDataBuf;
    for (int row = 0; row < pBmp->nHeight; row++) {
        for (int col = 0; col < pBmp->nWidth; col++) {
            switch (giDepth) {
                case 8:
                    //rgb = MapColor(*pSrc++);
                    //r = (rgb>>16) & 0xFF;
                    //g = (rgb>>8) & 0xFF;
                    //b = rgb & 0xFF;
                    break;
                case 16:
                    rgb = RGB565_TO_RGB(*((RGB16 *) pSrc));
                    pSrc += sizeof(RGB16);

                    r = (rgb >> 16) & 0xFF;
                    g = (rgb >> 8) & 0xFF;
                    b = rgb & 0xFF;
                    break;
                case 24:
                case 32:
                    b = *pSrc++;
                    g = *pSrc++;
                    r = *pSrc++;
                    if (giDepth == 32) pSrc++;
                    break;
                default:
                    break;
            }
            if (bColor) {
                *pDst++ = r;
                *pDst++ = g;
                *pDst++ = b;
            }
            else {
                *pDst++ = (299 * r + 587 * g + 114 * b) / 1000;
            }
        }
    }

    bRet = (jpeg_compress(pFileName, pDataBuf, pBmp->nWidth, pBmp->nHeight, bColor, nQuality) == 0);

    Free(pDataBuf);

    return bRet;
}

void saveRaw(unsigned char *pRawBuf, int iLen) {

    static int iii = 0;
    FILE *writeStream = NULL;
    char caTempFile[128] = {0};

    sprintf(caTempFile, "/mnt/sdcard/rong.yuv");
    LOGD("caTempFile:%s", caTempFile);

    if ((writeStream = fopen(caTempFile, "w+")) != NULL) {
        //fwrite(s_dispSifBuf, 1, SIF_HEIGHT * SIF_WIDTH * 3 /2, writeStream);
        fwrite(pRawBuf, 1, iLen, writeStream);

        fclose(writeStream);
    }
    else {
        LOGD("fopen file error! %s %d\r\n", __FUNCTION__, __LINE__);
    }

    iii++;
}

void saveRgb(unsigned char *pRawBuf, int iLen) {
    static int iii = 0;
    FILE *writeStream = NULL;
    char caTempFile[128] = {0};

    sprintf(caTempFile, "/mnt/sdcard/rong/rong.rgb");

    if ((writeStream = fopen(caTempFile, "wb")) != NULL) {
        //fwrite(s_dispSifBuf, 1, SIF_HEIGHT * SIF_WIDTH * 3 /2, writeStream);
        fwrite(pRawBuf, 1, iLen, writeStream);

        fclose(writeStream);
    }
    else {
        TRACE("fopen file error! %s %d\r\n", __FUNCTION__, __LINE__);
    }

    iii++;

}

/**************************************
// YUV420SP��ʱ����ת90��
// direct: 1 ---˳ʱ��
        0 ---��ʱ��
**************************************/
void YUV420SPRotate90(void *pYuv420, void *pOutYuv420, int width, int height, int nDirect) {

}

/*
void YUV420SPToYUV420P ( char * yuv420sp,  char * yuv420,  int  width,  int  height )
{
  
   int PixelsCount  = width * height;
   int i = 0, j= 0;

   if (yuv420sp== NULL || yuv420 == NULL)
   {
           return;
   }

    //copy  Y
    for (i = 0; i < PixelsCount;  i++)
    {
       *(yuv420 +i) = *(yuv420sp + i);
    }
 
    //copy  Cb(U)
    i = 0;
    for (j = 0;  j < PixelsCount / 2;  j+=2)
    {
       *(yuv420 + (i + PixelsCount)) = *(yuv420sp + (j + PixelsCount));
       i++;
    }

    //copy Cr(V)
    i = 0;
    for(j = 1;  j < PixelsCount/2;  j+=2)
    {
       *(yuv420 +(i+PixelsCount * 5/4)) = *(yuv420sp + (j + PixelsCount));
       i++;
    }
}
*/
void YUV420SPToYUV420P(char *yuv420sp, char *yuv420, int width, int height) {

    int PixelsCount = width * height;
    int i = 0, j = 0;


    if (yuv420sp == NULL || yuv420 == NULL) {
        return;
    }

    //copy  Y
    for (i = 0; i < PixelsCount; i++) {
        *(yuv420 + i) = *(yuv420sp + i);
    }

#if 0
    //420SP��ʽ: YYYYUV
   //copy  Cb(U)
   i = 0;
   for (j = 0;  j < PixelsCount / 2;  j+=2)
   {
      *(yuv420 + (i + PixelsCount)) = *(yuv420sp + (j + PixelsCount));
      i++;
   }

   //copy Cr(V)
   i = 0;
   for(j = 0;  j < PixelsCount/2;  j+=2)
   {
      *(yuv420 +(i+PixelsCount * 5/4)) = *(yuv420sp + (j + 1 + PixelsCount));
      i++;
   }
#else
    //420SP��ʽ: YYYYVU
    //copy  Cb(U)
    i = 0;
    for (j = 0; j < PixelsCount / 2; j += 2) {
        *(yuv420 + (i + PixelsCount)) = *(yuv420sp + (j + 1 + PixelsCount));
        i++;
    }

    //copy Cr(V)
    i = 0;
    for (j = 0; j < PixelsCount / 2; j += 2) {
        *(yuv420 + (i + PixelsCount * 5 / 4)) = *(yuv420sp + (j + PixelsCount));
        i++;
    }
#endif
}

LPBMP CreateBmpFromYUV420PBuf(int nWidth, int nHeight, char *pYuv420pBuf) {
    LPBMP pBmp = new BMP;
    if (pBmp == NULL)
        return NULL;
    memset(pBmp, 0, sizeof(BMP));

    pBmp->nWidth = nWidth;
    pBmp->nHeight = nHeight;
    pBmp->nBitsPerPixel = m_nBytesPerPixel * 8;
    pBmp->nWidthBytes = pBmp->nWidth * m_nBytesPerPixel;

    pBmp->pBits = (char *) Malloc(pBmp->nWidthBytes * pBmp->nHeight);
    if (pBmp->pBits == NULL) {
        delete pBmp;
        pBmp = NULL;
    }
    else {
        int i, j;
        unsigned char *pDst = (unsigned char *) pBmp->pBits, *pY = (unsigned char *) pYuv420pBuf, *pU =
                pY + nWidth * nHeight, *pV = pU + nWidth * nHeight / 4;
        if (0) {
            for (j = 0; j < nHeight; j++) {
                for (i = 0; i < nWidth; i++) {
                    if (*pY < 0x10)
                        *pY = 0x10;
                    else if (*pY > 0xEB)
                        *pY = 0xEB;
                    *pDst++ = *pU;
                    *pDst++ = *pY;
                    *pDst++ = *pV;
                    *pDst++ = *pY;
                    pY++;
                    if (i % 2) {
                        pU++;
                        pV++;
                    }
                }
                if (j % 2 == 0) {
                    pU -= nWidth / 2;
                    pV -= nWidth / 2;
                }
            }
        }
        else {
            RGB rgb;
            for (j = 0; j < nHeight; j++) {
                for (i = 0; i < nWidth; i++) {
                    rgb = yuv2rgb(*pY, *pU, *pV);
                    *((RGB16 *) pDst) = RGB_TO_RGB565(rgb);
                    pDst += 2;

                    pY++;
                    if (i % 2) {
                        pU++;
                        pV++;
                    }
                }
                if (j % 2 == 0) {
                    pU -= nWidth / 2;
                    pV -= nWidth / 2;
                }
            }
        }
    }
    return pBmp;
}




/**************************************
direct: 1 ---˳ʱ��
        0 ---��ʱ��

**************************************/
/*
void* rotate90_yuv420p(void* pYuv420, int nWidth, int nHeight, void*pOutYuv420, BOOL bUV, int nDirect)
{
	static char s_yuv[D1_WIDTH* D1_HEIGHT*2];
	int i;
	int j;
	int nImgLen = nWidth*nHeight;
	unsigned char *pSrc = (unsigned char*)pYuv420;
	unsigned char *pDst = (unsigned char*)s_yuv;

	if(pOutYuv420)
	{
		pDst = (unsigned char*)pOutYuv420;
	}
	
	if(nDirect)
	{
		for ( j = 0; j < nHeight; j++)
		{
			pDst += nHeight-1;

			for ( i = 0; i < nWidth; i++)
			{
				*pDst = *pSrc++;

				if(*pDst==0)
				{
					*pDst = 16;
				}
				else if(*pDst<16)
				{
					*pDst = 17;
				}
				else if(*pDst>235)
				{
					*pDst=235;
				}
				
				*pDst = (*pDst-16)*256/220;

				pDst += nHeight;
			}
			
			pDst -= nImgLen+nHeight;
		}

		if(bUV)
		{
			pDst += nImgLen;
			nWidth /= 2;
			nHeight /= 2;
			nImgLen = nWidth*nHeight;

			for ( j = 0; j < nHeight; j++)
			{
				pDst += nHeight-1;
				
				for ( i = 0; i < nWidth; i++)
				{
					*pDst = *pSrc++;
					if(*pDst<16)
					{
						*pDst = 16;
					}
					else if(*pDst>240)
					{
						*pDst=240;
					}
					
					pDst += nHeight;
				}
				
				pDst -= nImgLen+nHeight;
			}

			pDst += nImgLen;
			
			for ( j = 0; j < nHeight; j++)
			{
				pDst += nHeight-1;
				
				for ( i = 0; i < nWidth; i++)
				{
					*pDst = *pSrc++;
					if(*pDst<16)
					{
						*pDst = 16;
					}
					else if(*pDst>240)
					{
						*pDst=240;
					}
					
					pDst += nHeight;
				}
				
				pDst -= nImgLen+nHeight;
			}
		}
	}
	else
	{
		for ( j = 0; j < nHeight; j++)
		{
			pDst += nImgLen-1;
			
			for ( i = 0; i < nWidth; i++)
			{
				*pDst = *pSrc++;

				if(*pDst==0)
				{
					*pDst = 16;
				}
				else if(*pDst<16)
				{
					*pDst = 17;
				}
				else if(*pDst>235)
				{
					*pDst=235;
				}
				
				*pDst = (*pDst-16)*256/220;

				pDst -= nHeight;
			}
		}

		if(bUV)
		{
			pDst += nImgLen+nHeight;
			nWidth /= 2;
			nHeight /= 2;
			nImgLen = nWidth*nHeight;

			for ( j = 0; j < nHeight; j++)
			{
				pDst += nImgLen-1;
				
				for ( i = 0; i < nWidth; i++)
				{
					*pDst = *pSrc++;
					if(*pDst<16)
					{
						*pDst = 16;
					}
					else if(*pDst>240)
					{
						*pDst=240;
					}
					
					pDst -= nHeight;
				}
			}

			pDst += nImgLen+nHeight;
			for ( j = 0; j < nHeight; j++)
			{
				pDst += nImgLen-1;
				
				for ( i = 0; i < nWidth; i++)
				{
					*pDst = *pSrc++;
					if(*pDst<16)
					{
						*pDst = 16;
					}
					else if(*pDst>240)
					{
						*pDst=240;
					}
					
					pDst -= nHeight;
				}
			}
		}
	}

	return (pOutYuv420?pOutYuv420:s_yuv);
}
*/
/**************************************
direct: 1 ---˳ʱ��
		0 ---��ʱ��

**************************************/
void *rotate90_yuv420p(void *pYuv420, int nWidth, int nHeight, void *pOutYuv420, BOOL bUV,
                       int nDirect) {
    static char s_yuv[D1_WIDTH * D1_HEIGHT * 2];
    int i;
    int j;
    int nImgLen = nWidth * nHeight;
    unsigned char *pSrc = (unsigned char *) pYuv420;
    unsigned char *pDst = (unsigned char *) s_yuv;

    if (pOutYuv420) {
        pDst = (unsigned char *) pOutYuv420;
    }

    if (nDirect) {
        for (j = 0; j < nHeight; j++) {
            pDst += nHeight - 1;

            for (i = 0; i < nWidth; i++) {
                *pDst = *pSrc++;

                if (*pDst == 0) {
                    *pDst = 16;
                }
                else if (*pDst < 16) {
                    *pDst = 17;
                }
                else if (*pDst > 235) {
                    *pDst = 235;
                }

                *pDst = (*pDst - 16) * 256 / 220;

                pDst += nHeight;
            }

            pDst -= nImgLen + nHeight;
        }

        if (bUV) {
            pDst += nImgLen;
            nWidth /= 2;
            nHeight /= 2;
            nImgLen = nWidth * nHeight;

            for (j = 0; j < nHeight; j++) {
                pDst += nHeight - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst += nHeight;
                }

                pDst -= nImgLen + nHeight;
            }

            pDst += nImgLen;

            for (j = 0; j < nHeight; j++) {
                pDst += nHeight - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst += nHeight;
                }

                pDst -= nImgLen + nHeight;
            }
        }
    }
    else {
        for (j = 0; j < nHeight; j++) {
            pDst += nImgLen - 1;

            for (i = 0; i < nWidth; i++) {
                *pDst = *pSrc++;

                if (*pDst == 0) {
                    *pDst = 16;
                }
                else if (*pDst < 16) {
                    *pDst = 17;
                }
                else if (*pDst > 235) {
                    *pDst = 235;
                }

                *pDst = (*pDst - 16) * 256 / 220;

                pDst -= nHeight;
            }
        }

        if (bUV) {
            pDst += nImgLen + nHeight;
            nWidth /= 2;
            nHeight /= 2;
            nImgLen = nWidth * nHeight;

            for (j = 0; j < nHeight; j++) {
                pDst += nImgLen - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst -= nHeight;
                }
            }

            pDst += nImgLen + nHeight;
            for (j = 0; j < nHeight; j++) {
                pDst += nImgLen - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst -= nHeight;
                }
            }
        }
    }

    return (pOutYuv420 ? pOutYuv420 : s_yuv);
}

void *rotate90_yuv420sp(void *pYuv420, int nWidth, int nHeight, void *pOutYuv420, BOOL bUV,
                        int nDirect) {
    static char s_yuv[D1_WIDTH * D1_HEIGHT * 2];
    int i;
    int j;
    int nImgLen = nWidth * nHeight;
    unsigned char *pSrc = (unsigned char *) pYuv420;
    unsigned char *pDst = (unsigned char *) s_yuv;

    if (pOutYuv420) {
        pDst = (unsigned char *) pOutYuv420;
    }

    if (nDirect) {
        for (j = 0; j < nHeight; j++) {
            pDst += nHeight - 1;

            for (i = 0; i < nWidth; i++) {
                *pDst = *pSrc++;

                if (*pDst == 0) {
                    *pDst = 16;
                }
                else if (*pDst < 16) {
                    *pDst = 17;
                }
                else if (*pDst > 235) {
                    *pDst = 235;
                }

                *pDst = (*pDst - 16) * 256 / 220;

                pDst += nHeight;
            }

            pDst -= nImgLen + nHeight;
        }

        if (bUV) {
            pDst += nImgLen;
            nWidth /= 2;
            nHeight /= 2;
            nImgLen = nWidth * nHeight;

            for (j = 0; j < nHeight; j++) {
                pDst += nHeight - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst += nHeight;
                }

                pDst -= nImgLen + nHeight;
            }

            pDst += (nImgLen * 2);

            for (j = 0; j < nHeight; j++) {
                pDst += nHeight - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst += nHeight;
                }

                pDst -= nImgLen + nHeight;
            }
        }
    }
    else {
        for (j = 0; j < nHeight; j++) {
            pDst += nImgLen - 1;

            for (i = 0; i < nWidth; i++) {
                *pDst = *pSrc++;

                if (*pDst == 0) {
                    *pDst = 16;
                }
                else if (*pDst < 16) {
                    *pDst = 17;
                }
                else if (*pDst > 235) {
                    *pDst = 235;
                }

                *pDst = (*pDst - 16) * 256 / 220;

                pDst -= nHeight;
            }
        }

        if (bUV) {
            pDst += nImgLen + nHeight;
            nWidth /= 2;
            nHeight /= 2;
            nImgLen = nWidth * nHeight;

            for (j = 0; j < nHeight; j++) {
                pDst += nImgLen - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst -= nHeight;
                }
            }

            pDst += nImgLen + nHeight;
            for (j = 0; j < nHeight; j++) {
                pDst += nImgLen - 1;

                for (i = 0; i < nWidth; i++) {
                    *pDst = *pSrc++;
                    if (*pDst < 16) {
                        *pDst = 16;
                    }
                    else if (*pDst > 240) {
                        *pDst = 240;
                    }

                    pDst -= nHeight;
                }
            }
        }
    }

    return (pOutYuv420 ? pOutYuv420 : s_yuv);
}


static RGB24x2 Conv_YCbCr_Rgb24x2(unsigned char y0, unsigned char y1, unsigned char cb0,
                                  unsigned char cr0)  // second solution... by junon
{
    RGB24x2 rgb = {0};
    int r0, g0, b0, r1, g1, b1;
    // 4 frames/s @192MHz, 12MHz ; 6 frames/s @450MHz, 12MHz
    r0 = YCbCrtoR(y0, cb0, cr0);
    g0 = YCbCrtoG(y0, cb0, cr0);
    b0 = YCbCrtoB(y0, cb0, cr0);
    r1 = YCbCrtoR(y1, cb0, cr0);
    g1 = YCbCrtoG(y1, cb0, cr0);
    b1 = YCbCrtoB(y1, cb0, cr0);


    if (r0 > 255) r0 = 255;
    if (r0 < 0) r0 = 0;

    if (g0 > 255) g0 = 255;
    if (g0 < 0) g0 = 0;

    if (b0 > 255) b0 = 255;
    if (b0 < 0) b0 = 0;

    if (r1 > 255) r1 = 255;
    if (r1 < 0) r1 = 0;

    if (g1 > 255) g1 = 255;
    if (g1 < 0) g1 = 0;

    if (b1 > 255) b1 = 255;
    if (b1 < 0) b1 = 0;
    rgb.r0 = (unsigned char) r0;
    rgb.g0 = (unsigned char) g0;
    rgb.b0 = (unsigned char) b0;
    rgb.r1 = (unsigned char) r1;
    rgb.g1 = (unsigned char) g1;
    rgb.b1 = (unsigned char) b1;
    return rgb;
}

#define YCbCrtoR(Y, Cb, Cr)       (1000*Y + 1371*(Cr-128))/1000
#define YCbCrtoG(Y, Cb, Cr)       (1000*Y - 336*(Cb-128) - 698*(Cr-128))/1000
#define YCbCrtoB(Y, Cb, Cr)       (1000*Y + 1732*(Cb-128))/1000

static unsigned int Conv_YCbCr_Rgb(unsigned char y0, unsigned char y1, unsigned char cb0,
                                   unsigned char cr0)  // second solution... by junon
{

    int r0, g0, b0, r1, g1, b1;
    unsigned int rgb0, rgb1, rgb;

#if 1 // 4 frames/s @192MHz, 12MHz ; 6 frames/s @450MHz, 12MHz
    r0 = YCbCrtoR(y0, cb0, cr0);
    g0 = YCbCrtoG(y0, cb0, cr0);
    b0 = YCbCrtoB(y0, cb0, cr0);
    r1 = YCbCrtoR(y1, cb0, cr0);
    g1 = YCbCrtoG(y1, cb0, cr0);
    b1 = YCbCrtoB(y1, cb0, cr0);
#endif

    if (r0 > 255) r0 = 255;
    if (r0 < 0) r0 = 0;
    if (g0 > 255) g0 = 255;
    if (g0 < 0) g0 = 0;
    if (b0 > 255) b0 = 255;
    if (b0 < 0) b0 = 0;

    if (r1 > 255) r1 = 255;
    if (r1 < 0) r1 = 0;
    if (g1 > 255) g1 = 255;
    if (g1 < 0) g1 = 0;
    if (b1 > 255) b1 = 255;
    if (b1 < 0) b1 = 0;

    /* 5:6:5 16bit format*/
    rgb0 = (((unsigned short) r0 >> 3) << 11) | (((unsigned short) g0 >> 2) << 5) |
           (((unsigned short) b0 >> 3) << 0);      //RGB565.
    rgb1 = (((unsigned short) r1 >> 3) << 11) | (((unsigned short) g1 >> 2) << 5) |
           (((unsigned short) b1 >> 3) << 0);      //RGB565.

    rgb = (rgb1 << 16) | rgb0;

    return (rgb);
}

//ƽ��YUV420תRGB24
void YUV420p_to_RGB24(unsigned char *yuv420[3], unsigned char *rgb24, int width, int height) {
//  int begin = GetTickCount();
    int R1, G1, B1, Y, U, V;
    int x, y;
    int nWidth = width >> 1; //ɫ���źſ��
    for (y = 0; y < height; y++) {
        for (x = 0; x < width; x++) {
            Y = *(yuv420[0] + y * width + x);
            U = *(yuv420[1] + ((y >> 1) * nWidth) + (x >> 1));
            V = *(yuv420[2] + ((y >> 1) * nWidth) + (x >> 1));
            R1 = Y + 1.402 * (V - 128);
            G1 = Y - 0.34414 * (U - 128) - 0.71414 * (V - 128);
            B1 = Y + 1.772 * (U - 128);

            //��ֹԽ��
            if (R1 > 255)R1 = 255;
            if (R1 < 0)R1 = 0;
            if (G1 > 255)G1 = 255;
            if (G1 < 0)G1 = 0;
            if (B1 > 255)B1 = 255;
            if (B1 < 0)B1 = 0;

            *(rgb24 + ((height - y - 1) * width + x) * 3) = B1;
            *(rgb24 + ((height - y - 1) * width + x) * 3 + 1) = G1;
            *(rgb24 + ((height - y - 1) * width + x) * 3 + 2) = R1;
//    *(rgb24 + (y*width + x)*3) = B;
//    *(rgb24 + (y*width + x)*3 + 1) = G;
//    *(rgb24 + (y*width + x)*3 + 2) = R;   
        }
    }
}


void cvt_420p_to_rgb565(int width, int height, unsigned char *src, unsigned short *dst) {
    int line, col, linewidth;
    int y, u, v, yy, vr, ug, vg, ub;
    int r, g, b;
    const unsigned char *py, *pu, *pv;

    linewidth = width >> 1;
    py = src;
    pu = py + (width * height);
    pv = pu + (width * height) / 4;

    y = *py++;
    yy = y << 8;
    u = *pu - 128;
    ug = 88 * u;
    ub = 454 * u;
    v = *pv - 128;
    vg = 183 * v;
    vr = 359 * v;

    for (line = 0; line < height; line++) {
        for (col = 0; col < width; col++) {
            r = (yy + vr) >> 8;
            g = (yy - ug - vg) >> 8;
            b = (yy + ub) >> 8;

            if (r < 0) r = 0;
            if (r > 255) r = 255;
            if (g < 0) g = 0;
            if (g > 255) g = 255;
            if (b < 0) b = 0;
            if (b > 255) b = 255;
            *dst++ = (((unsigned short) r >> 3) << 11) | (((unsigned short) g >> 2) << 5) |
                     (((unsigned short) b >> 3) << 0);

            y = *py++;
            yy = y << 8;
            if (col & 1) {
                pu++;
                pv++;

                u = *pu - 128;
                ug = 88 * u;
                ub = 454 * u;
                v = *pv - 128;
                vg = 183 * v;
                vr = 359 * v;
            }
        }
        if ((line & 1) == 0) {
            pu -= linewidth;
            pv -= linewidth;
        }
    }
}


void yuv420p_to_rgb565_ex(int size_x, int size_y, char *pSrcBuf, RGB24x2 *pbuff) {
    unsigned char *buffer_y = NULL;
    unsigned char *buffer_cb = NULL;
    unsigned char *buffer_cr = NULL;
    RGB24x2 rgb_data0 = {0};
    int x, y;

    int line_wid;
    line_wid = 480 * 2 / 4;
    buffer_y = (unsigned char *) &pSrcBuf[0];
    buffer_cb = (unsigned char *) &pSrcBuf[640 * 480];
    buffer_cr = (unsigned char *) &pSrcBuf[640 * 480 + 640 * 480 / 4];

    for (y = 0; y < size_y; y++) // YCbCr 4:2:0 format
    {
        for (x = 0; x < size_x; x += 2) {
            rgb_data0 = Conv_YCbCr_Rgb24x2(*buffer_y, *(buffer_y + 1), *buffer_cb++, *buffer_cr++);

            pbuff[y * line_wid + x / 2] = rgb_data0;
            buffer_y += 2;

            if ((x == (size_x - 2)) && ((y % 2) == 0)) // when x is last pixel & y is even number
            {
                buffer_cb -= size_x / 2;
                buffer_cr -= size_x / 2;
            }
        }
    }
}

// ��YUV420spͼ��ת��ΪRGB24ͼ��
void yuv420sp_to_rgb(int width, int height, unsigned char *pSrcBuf, RGB24x2 *pbuff) {
    unsigned char *buffer_y = NULL;
    unsigned char *buffer_CbCr = NULL;
    RGB24x2 rgb_data0 = {0};
    int x, y;

    int line_wid;
    line_wid = width * 2 / 4;
    buffer_y = (unsigned char *) &pSrcBuf[0];
    buffer_CbCr = (unsigned char *) &pSrcBuf[width * height];

    for (y = 0; y < height; y++) // YCbCr 4:2:0 format
    {
        for (x = 0; x < width; x += 2) {
            rgb_data0 = Conv_YCbCr_Rgb24x2(*buffer_y, *(buffer_y + 1), *(buffer_CbCr + 1),
                                           *(buffer_CbCr));
            pbuff[y * line_wid + x / 2] = rgb_data0;
            buffer_y += 2;
            buffer_CbCr += 2;
            if ((x == (width - 2)) && ((y % 2) == 0)) // when x is last pixel & y is even number
            {
                buffer_CbCr -= width;
            }
        }
    }
    //LOGD("saveRgb len=%d function:%s line:%d",3*width*height,__FUNCTION__,__LINE__);
    //saveRgb((unsigned char*)pbuff, 3*width*height);
}

BOOL ReadFile(const char *pSrcFile, char **pBuffer, int *pLen) {
    BOOL bRet = FALSE;
    int hSrc = -1;
    char *pBuf = NULL;
    struct stat st;

    if (stat(pSrcFile, &st) == 0 && st.st_size > 0) {
        hSrc = open(pSrcFile, O_RDONLY);

        if (hSrc != -1) {
            int buflen = st.st_size;
            int left = st.st_size;
            *pLen = st.st_size;
            *pBuffer = pBuf = (char *) Malloc(buflen);

            if (pBuf == NULL) {
                LOGD("ReadFile: Malloc %d bytes buffer fail\r\n", buflen);
            }
            else {
                while (left) {
                    if (read(hSrc, pBuf, buflen) != buflen)
                        break;

                    left -= buflen;

                    if (buflen > left) {
                        buflen = left;
                    }
                }

                if (left == 0) {
                    bRet = TRUE;
                }
            }
        }

        if (hSrc != -1) {
            close(hSrc);
        }
        else {
            LOGD("Open source file %s fail\r\n", pSrcFile);
        }

    }

    return bRet;
}

void saveFile(const char *pSrcFile, unsigned char *pRawBuf, int iLen) {
    FILE *writeStream = NULL;

    if ((writeStream = fopen(pSrcFile, "wb")) != NULL) {
        //fwrite(s_dispSifBuf, 1, SIF_HEIGHT * SIF_WIDTH * 3 /2, writeStream);
        fwrite(pRawBuf, 1, iLen, writeStream);

        fclose(writeStream);
    }
    else {
        TRACE("fopen file error! %s %d\r\n", __FUNCTION__, __LINE__);
    }
}



/*
	//��ʼ��YUV2RGB��������
	void table_init()
	{
	    int i;
	    for(i=0;i<256;i++)
	    {
	        D[i]=i*1224;
	        D[i]=D[i]>>12;

	        E[i]=i*2404;
	        E[i]=E[i]>>12;

	        F[i]=i*467;
	        F[i]=F[i]>>12;
	    }
	}

	void calc_lum_single()

	{
	    int i, IMGSIZE;
	    int in[] = {0};
	    int out[] = {0};

	    for(i = 0; i < IMGSIZE; i++)
	    {
	       int r,g,b,y;

	        r = D[in[i].r];//���
	        g = E[in[i].g];
	        b = F[in[i].b];
	        y = r + g + b;
	        out[i] = y;
	    }
	}

	void calc_lum_double()

	{

	    int i, IMGSIZE;
	    int in[] = {0};
	    int out[] = {0};

	    for(i = 0; i < IMGSIZE; i += 2) //һ�β��д���2������
	    {
	       int r,g,b,y,r1,g1,b1,y1;

	        r = D[in[i].r];//��� //�������һ��ALUִ��
	        g = E[in[i].g];
	        b = F[in[i].b];
	        y = r + g + b;
	        out[i] = y;

	        r1 = D[in[i + 1].r];//��� //������ڶ���ALUִ��
	        g1 = E[in[i + 1].g];
	        b1 = F[in[i + 1].b];
	        y = r1 + g1 + b1;
	        out[i + 1] = y;
	    }
	}
*/
