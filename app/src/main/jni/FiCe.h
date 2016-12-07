
#ifndef _FI_CE_H
#define _FI_CE_H

#ifdef __cplusplus
extern "C" {
#endif

#include "StdDefEx.h"

/***************************************************
 * 函数名称：打开引擎
 * 输入参数：bIOCmemBuf 是否启用基于CMEM分配的I/O参数。==FI_TRUE，app基于CMEM分配参数; ==FI_FALSE，app基于arm端分配参数
 * 			cmemStart CMEM 起始地址
 * 			cmemEnd   CMEM 结束地址
 * 输出参数：无
 * 返回结果：!=NULL 返回引擎句柄; ==NULL 打开引擎失败
 ***************************************************
*/
FI_HANDLE FI_OpenEngine(FI_BOOL bIOCmemBuf, unsigned long cmemStart, unsigned long cmemEnd);

/***************************************************
 * 函数名称：关闭引擎
 * 输入参数：hFiCe 由 FI_OpenEngine 打开的引擎句柄
 * 输出参数：无
 * 返回结果：无
 ***************************************************
*/
void FI_CloseEngine(FI_HANDLE hFiCe);


// 分配共享缓冲区
char *FI_MallocContigBuf(int bufSize);
void FI_FreeContigBuf(char *pBuf, int bufSize);


#ifdef __cplusplus
}
#endif

#endif // _FI_CE_H


