/********************************************************************************
**  Copyright (c) 2012, 深圳市飞瑞斯科技有限公司
**  All rights reserved.
**
**  文件名称： netLib.h
**  参考： 
**
**  当前版本：1.0
**
**  创建作者：张祖异
**  创建日期:  2012.5.21
**
**  修改作者：
**  修改日期:
********************************************************************************/

#ifndef NETLIB_H
#define NETLIB_H

//#include "Comm.h"

#define COMM_PORT                   30000
#define COMM_PORT_SERVER            30001

#ifdef WIN32
	#pragma pack(1)
	#define PACK_ALIGN
#else
	#define PACK_ALIGN __attribute__((packed))
#endif

typedef struct
{	
	int iServPort;		/* 终端作为服务器端，侦听的端口 */
	char *acServIp;		/* 终端作为服务器端，accept的IP */
	int iCliePort;		/* 终端作为客户端，连接的服务器端口 */
	char *acClieIp;		/* 终端作为客户端，连接的服务器IP */
}PACK_ALIGN TcpThreadsData;

/**************************************************************\
** 函数名称： DoingAllForTcpClientThread
** 功能： 终端作为客户端线程
** 参数：   
            arg : 
** 返回：无
** 创建作者： 颜廷军
** 创建日期： 2012-8-22
** 修改作者： 
** 修改日期： 
\**************************************************************/
void *DoingAllForTcpClientThread(void *arg);

/**************************************************************\
** 函数名称： DoingAllForTcpServerThread
** 功能： 终端作为服务端线程
** 参数：   
            arg : 
** 返回：无
** 创建作者： 颜廷军
** 创建日期： 2012-8-22
** 修改作者： 
** 修改日期： 
\**************************************************************/
void *DoingAllForTcpServerThread(void *arg);



#ifdef WIN32
    #pragma pack()
#endif
	#undef PACK_ALIGN

#endif /* NETLIB_H */

