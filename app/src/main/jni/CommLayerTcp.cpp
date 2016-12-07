/********************************************************************************
**  Copyright (c) 2012, �����з���˹�Ƽ����޹�˾
**  All rights reserved.
**
**  �ļ����ƣ� CommLayerTcp.cpp
**  �ο�������˹����ʶ���ն�ͨ��Э��V2.0.doc/V1.5.doc�����TCP��������
**
**  ��ǰ�汾��1.0
**
**  �������ߣ�������
**  ��������:  2012.5.21
**
**  �޸����ߣ�
**  �޸�����:
********************************************************************************/
#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <ctype.h>
#include <time.h>
#include <sys/types.h>
//#include <sys/signal.h>
#include <errno.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <sys/wait.h>
#include <sys/mman.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/stat.h>
//#include <assert.h>
#include <arpa/inet.h>
#include <pthread.h>
#include <sys/mman.h>
#include <iostream>

#include "netComm.h"
#include "netLib.h"
#include "NetDeal.h"

#include "maintainOutLib.h"
#include <pthread.h>
#include "facelib.h"

#include <sys/select.h>
#include <sys/socket.h>
#include <arpa/inet.h>


//using namespace std;

int iThreadExit = 0;

unsigned short CommLayerTcp::DataIndex = 0;
pthread_mutex_t CommLayerTcp::SendDataQueueMutex;
pthread_cond_t CommLayerTcp::SendDataQueueCond;
LinkQueue_Cpp<StrTcpSendData> CommLayerTcp::SendDataQueue;
int CommLayerTcp::iInstance= 0;

/**************************************************************\
** �������ƣ�CommLayerTcp
** ���ܣ� TCP�๹�캯��
** ������   
         ��
** ���أ���
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
CommLayerTcp::CommLayerTcp()
{
	int i;
	
	iServPort = -1;
	memset(acServIp, 0, sizeof(acServIp));
	memset(acClieIp, 0, sizeof(acClieIp));
	iCliePort = -1;
	iSocketServFd = -1;

	for (i = 0; i < MAX_CONN_NUM; i++)
	{
		iAllFd[i] = -1;
	}
	
	iCurrConnNum = 0;
	
	iClientAuthSucc = 0;

	iSocketServFd = -1;
	iSocketClieFd = -1;

	for (i = 0; i < MAX_SEND_THREADS; i++)
	{
		SendThreadId[i] = 0;
	}

	for (i = 0; i < MAX_CONN_NUM; i++)
	{
		RecvThreadId[i] = 0;
	}

	ClientSendHeartbeatThreadId = 0;

	/* ��������Fd������ */
	if (pthread_mutex_init(&AllFdMutex, NULL) != 0)
	{
		//return -1;
	}

    /* ��������Fd������ */
	if (pthread_mutex_init(&RecvThreadIDMutex, NULL) != 0)
	{
		//return -1;
	}
    
	if (iInstance++ == 0)
	{
		/* �������Ͷ��л����� */
		if (pthread_mutex_init(&CommLayerTcp::SendDataQueueMutex, NULL) != 0)
		{
			//return -1;
		}

		/* �������Ͷ����������� */
		if (pthread_cond_init(&CommLayerTcp::SendDataQueueCond, NULL) != 0)
		{
			//return -1;
		}
	}
}

CommLayerTcp::CommLayerTcp(const CommLayerTcp& orig)
{
}

CommLayerTcp& CommLayerTcp::operator = (const CommLayerTcp &rhs)
{
	return *this;
}

/* ���������IP��˿� */
void CommLayerTcp::CommLayerResetIpAndPort(const SERVER_OR_CLIENT InModel, const char *Ip, const int Port)
{
	if(InModel == TCP_CLIENT)
	{
		iCliePort = Port;
		strncpy(acClieIp, Ip, sizeof(acClieIp) - 1);
	}
	else if(InModel == TCP_SERVER)
	{
		iServPort = Port;
		strncpy(acServIp, Ip, sizeof(acServIp) - 1);
	}
	else
	{
		return;
	}
}

/**************************************************************\
** �������ƣ� CommLayerTcpInit
** ���ܣ�TCP������ʼ������
** ������   
            InModel : �ͻ��˻�������ˣ�0-�ͻ��ˣ�1-��������
            Ip 		: IP
            Port 	: �˿�
** ���أ�0-�ɹ�������-ʧ��
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
int CommLayerTcp::CommLayerTcpInit(const SERVER_OR_CLIENT InModel, const char *Ip, const int Port)
{
	int i;
	
	if(InModel == TCP_CLIENT)
	{
		iCliePort = Port;
		strncpy(acClieIp, Ip, sizeof(acClieIp) - 1);
	}
	else if(InModel == TCP_SERVER)
	{
		iServPort = Port;
		strncpy(acServIp, Ip, sizeof(acServIp) - 1);
	}
	else
	{
		return -1;
	}

	/* ������ݷ��Ͷ��� */
	CommLayerTcp::SendDataQueue.Clear();

	for (i = 0; i < MAX_CONN_NUM; i++)
	{
		iAllFd[i] = -1;
	}
	
	iCurrConnNum = 0;

	for (i = 0; i < MAX_CONN_NUM; i++)
	{
		RecvThreadId[i] = 0;
	}
	
	ClientSendHeartbeatThreadId = 0;

	return 0;
}

/**************************************************************\
** �������ƣ� CommLayerTcpDestroy
** ���ܣ�TCP�������ٺ���
** ������   
         ��
** ���أ���
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
void CommLayerTcp::CommLayerTcpDestroy()
{
	if(pthread_mutex_destroy(&AllFdMutex) != 0)
	{
	}

    if(pthread_mutex_destroy(&RecvThreadIDMutex) != 0)
	{
	}
    
	if (--iInstance == 0)
	{
		if(pthread_cond_destroy(&CommLayerTcp::SendDataQueueCond) != 0)
		{
		}

		if(pthread_mutex_destroy(&CommLayerTcp::SendDataQueueMutex) != 0)
		{
		}

		CommLayerTcp::SendDataQueue.Clear();
	}
	
	if(iSocketServFd != -1)
	{
		close(iSocketServFd);
        iSocketServFd = -1;
	}
	
	if(iSocketClieFd != -1)
	{
		close(iSocketClieFd);
        iSocketClieFd = -1;
	}
	
	return;
}

/**************************************************************\
** �������ƣ� ~CommLayerTcp
** ���ܣ� TCP����������
** ������   
         ��
** ���أ���
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
CommLayerTcp::~CommLayerTcp()
{	
	CommLayerTcpDestroy();
}

/**************************************************************\
** �������ƣ� CreateTcpServ
** ���ܣ� TCP�������˴������̣�ͣ����listen�׶�
** ������   
         ��
** ���أ�true-�ɹ���false-ʧ��
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
bool CommLayerTcp::CreateTcpServ()
{
	int fd = -1;
	bool ReFlag = true;
	const int on = 1;
	struct sockaddr_in addr;
	unsigned short usPort = 0;

	if(iServPort <= 0)
	{
		ReFlag = false;
		goto FAIL;
	}

	if((fd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
	{
		ReFlag = false;
		goto FAIL;
	}
    
	if(setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0)
	{
		ReFlag = false;
		goto FAIL;
	}

	memset(&addr, 0, sizeof(addr));
	addr.sin_family = AF_INET;
	usPort = (unsigned short)iServPort;
	addr.sin_port = htons(usPort);

	if(strlen(acServIp) != 0)
	{
		TRACE("acServIp[%s] usPort[%d][%s %d\r\n",acServIp,usPort, __FUNCTION__, __LINE__);
		/* �ж�ip�Ϸ��� */
		if(inet_pton(AF_INET, (const char *)acServIp, (void *)&addr.sin_addr) == -1)
		{
			ReFlag = false;
			goto FAIL;
		}
	}
	else
	{
		ReFlag = false;
		goto FAIL;
	}

	if(bind(fd, (struct sockaddr *) &addr, sizeof(addr)) < 0)
	{
        TRACE(" %s %d\r\n", __FUNCTION__, __LINE__);

		ReFlag = false;
		goto FAIL;
	}

	if(listen(fd, SOMAXCONN) < 0)
	{
        TRACE(" %s %d\r\n", __FUNCTION__, __LINE__);
        
		ReFlag = false;
		goto FAIL;
	}

	iSocketServFd = fd;

FAIL:
	if((fd != -1) && (ReFlag == false))
	{
		close(fd);
	}

	return ReFlag;
}

/**************************************************************\
** �������ƣ� CreateTcpAccept
** ���ܣ� �ն���Ϊ������������accept�׶�
** ������   
         ��
** ���أ���
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
void CommLayerTcp::CreateTcpAccept()
{
	int accept_fd = -1;
	bool ret = true;
	socklen_t clilen;
	struct sockaddr_in cliaddr;
	pthread_t ThreadId;
	StrLayerTcpTmp *TTNP = NULL;

	while (1)
	{
		clilen = sizeof(struct sockaddr_in);
		memset(&cliaddr, 0x00, sizeof(struct sockaddr_in));
		accept_fd = -1;

		if ((accept_fd = accept(iSocketServFd, (struct sockaddr *)&cliaddr, &clilen)) < 0)
		{
			LOGD("accept failed %d %s %s %d",errno,strerror(errno),__FILE__,__LINE__);
			break;
		}

		/* ��¼�������ӵ�Fd */
		if (!AddAllFd(accept_fd))
		{
			close(accept_fd);
			
			continue;
		}

		TTNP = NULL;
		TTNP = new StrLayerTcpTmp;
		
		memset(TTNP, 0, sizeof(StrLayerTcpTmp));
		TTNP->iFd= accept_fd;
		TTNP->Obj = this;

		/* �ն���Ϊ�������ˣ�����һ�����Ӵ������߳� */
		if(pthread_create(&ThreadId, NULL, &TcpServSubThread, (void *)TTNP) != 0)
		{
			goto FAIL;
		}
		else
		{
			ret = AddRecvThreadID(ThreadId);
			if(false == ret)
			{
				LOGD("Too Much Client Thread:%d %s %d\r\n", iCurrConnNum,__FILE__,__LINE__);
				if(ThreadId > 0)
        		{
                    //pthread_cancel(ThreadId);
        		}
			}
		}

        LOGD("iCurrConnNum %d %s %d \r\n", iCurrConnNum, __FUNCTION__, __LINE__);
	}

FAIL:
	if(iSocketServFd != -1)
	{
		close(iSocketServFd);
        iSocketServFd = -1;
	}

	if(accept_fd != -1)
	{
		close(accept_fd);
        accept_fd = -1;
	}

	return;
}

/**************************************************************\
** �������ƣ� CreateTcpClient
** ���ܣ� ��Ϊ�ͻ��ˣ����ӷ�����
** ������   
         ��
** ���أ�true-�ɹ���false-ʧ��
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
bool CommLayerTcp::CreateTcpClient()
{
	int sockfd = -1;
	struct sockaddr_in servaddr;
	
	if((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
	{
		LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
		return false;
	}

	//���ý��պͷ��ͳ�ʱ
    struct timeval timeo = {1, 0};
    setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO, &timeo, sizeof(timeo));
    setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &timeo, sizeof(timeo));

	bzero(&servaddr,sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons((unsigned int)iCliePort);
	inet_pton(AF_INET, acClieIp, &servaddr.sin_addr);

	LOGD("iCliePort[%d]  acClieIp[%s]%s %d\r\n",iCliePort, acClieIp,__FUNCTION__, __LINE__);

	if(connect(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) == -1)
	{
		LOGD("CreateTcpClient Fail %s %d\r\n", __FUNCTION__, __LINE__);
		if(sockfd != -1)
		{
			close(sockfd);
			sockfd = -1;
		}
		
		return false;
	}
	
	iSocketClieFd = sockfd;
	
	return true;
}

/**************************************************************\
** �������ƣ� ReadableTimeout
** ���ܣ� ���ճ�ʱ�жϺ���
** ������ Fd-������, iSec-����
** ���أ�
** �������ߣ� ������
** �������ڣ� 2012-5-21
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
int CommLayerTcp::ReadableTimeout(int iFd, int iSec)
{
	fd_set rset;
	struct timeval	tv;

	if(iFd < 0)
	{
		LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);
		
		return -1;
	}

	FD_ZERO(&rset);
	FD_SET(iFd, &rset);
	
	tv.tv_sec = iSec;
	tv.tv_usec = 500 * 1000;

	return (select(iFd + 1, &rset, NULL, NULL, &tv));
}

/**************************************************************\
** �������ƣ� WriteN
** ���ܣ� ���ͺ���
** ������ 
		iFd-������
		vpBuf-���͵�����
		iBufLen-���͵ĳ���
** ���أ� �������ݵĳ��Ȼ�-1Ϊ����
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
int CommLayerTcp::WriteN(int iFd, const void *vpBuf, int iBufLen)
{
	int nleft;
	int nwritten;
    int iCount = 0;
	const char *ptr;
	
	LPPROTOCOL_HEAD pHead;

    if (vpBuf == NULL)
    {
        return -1;
    }
    
	pHead = (LPPROTOCOL_HEAD)vpBuf;

	ptr = (const char *)vpBuf;
	nleft = iBufLen;
	
	while(nleft > 0)
	{
		if((nwritten = write(iFd, ptr, nleft)) <= 0)
		{
			if(/*nwritten < 0 && */errno == EINTR)
			{
				nwritten = 0;
			}
			else
			{
				LOGD("errno: %d, %s %d\r\n", errno, __FUNCTION__, __LINE__); 
				LOGD("ErrorMsg: %s, %s %d\r\n", strerror(errno), __FUNCTION__, __LINE__); 

                iCount++;
                if (iCount <= 3)
                {
                    LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);
                    continue;
                }
				return -1;
			}
		}

        iCount = 0;
        
		nleft -= nwritten;
		ptr += nwritten;
	}
	
	return iBufLen;
}

/**************************************************************\
** �������ƣ� ReadN
** ���ܣ� ���ͺ���
** ������ iFd-������, vpBuf-���յ�����, iBufLen-���յĳ���, iTimeOut-��ʱʱ��
** ���أ�
        ����0Ϊ�������ݵĳ��ȣ�
 		-2Ϊ��ʱ��
 		-3Ϊ����������������
 		-1��������Ϊ����������
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
int CommLayerTcp::ReadN(int iFd, int iTimeOut, void *vpBuf, int iBufLen)
{
	int nleft;
	int nread;
	int i;
	char *ptr;
	int ret = 0;
	
	ptr = (char *)vpBuf;
	nleft = iBufLen;
	
	while (nleft > 0)
	{
		i = ReadableTimeout(iFd, iTimeOut);
		if(i == 0)
		{
			ret = -2;
			break;
		}
		else if(i < 0)
		{
			ret = -1;
			break;
		}
		else
		{
			if((nread = read(iFd, ptr, nleft)) < 0)
			{
				if(errno == EINTR)
				{
					nread = 0;
				}
				else
				{
					return -1;
				}
			}
			else if(nread == 0)
			{
                LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);

				break;
			}
			
			nleft -= nread;
			ptr += nread;
			ret += nread;
		}
	}

	return ret;
}

/**************************************************************\
** �������ƣ� DoTcpSendThreadPool
** ���ܣ� �ܵĶ��ⷢ���߳�
** ������
** ���أ� 0�ɹ���-1ʧ��
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
extern unsigned short check_sum(unsigned char* data, int  data_len);
void CommLayerTcp::DoTcpSendThreadPool(pthread_t InThreadId)
{
	StrTcpSendData TmpStrData;
    StrTcpSendData* pstrTcpSendData = NULL;
	int iWritedLen = 0;

	memset(&TmpStrData, 0, sizeof(StrTcpSendData));
	TmpStrData.pSendBuf = NULL;
	TmpStrData.iSendBufLen = 0;
	TmpStrData.iFd = -1;

	pthread_cleanup_push(CleanupMutex, (void *)&CommLayerTcp::SendDataQueueMutex);
	
	if(pthread_mutex_lock(&CommLayerTcp::SendDataQueueMutex) != 0)
	{
	}

	while(CommLayerTcp::SendDataQueue.Empty())
	{
		if(iThreadExit > 0)
		{
			//LOGD(">quit exit TcpSendThreadPool %s %d\r\n",__FUNCTION__, __LINE__);
			//iThreadExit = 2;
			break;
		}
		if(pthread_cond_wait(&CommLayerTcp::SendDataQueueCond, &CommLayerTcp::SendDataQueueMutex) != 0)
		{
		}
	}
    pstrTcpSendData = CommLayerTcp::SendDataQueue.Front();
    if (pstrTcpSendData == NULL)
    {
        LOGD("send data is null %s %d\r\n", __FUNCTION__, __LINE__);
        goto ERROR;
    }
	TmpStrData.iSendBufLen = pstrTcpSendData->iSendBufLen;
	TmpStrData.iFd = pstrTcpSendData->iFd;
	TmpStrData.pSendBuf = (char *)Malloc(TmpStrData.iSendBufLen + 1);
    if (TmpStrData.pSendBuf == NULL)
    {
        LOGD("send databuf is null %s %d\r\n", __FUNCTION__, __LINE__);

        goto ERROR;
    }
	
	memset(TmpStrData.pSendBuf, 0, sizeof(char)*(TmpStrData.iSendBufLen + 1));
	memcpy(TmpStrData.pSendBuf, pstrTcpSendData->pSendBuf, TmpStrData.iSendBufLen);

	/* �ͷŶ����еĽڵ��ڴ棬���ڴ��ڷ���ʱ���� */
	Free(pstrTcpSendData->pSendBuf);

	CommLayerTcp::SendDataQueue.Pop();

ERROR:
	if(pthread_mutex_unlock(&CommLayerTcp::SendDataQueueMutex) != 0)
	{
	}
	
	pthread_cleanup_pop(0);

	//unsigned short ret1 = check_sum((unsigned char*)TmpStrData.pSendBuf+1, TmpStrData.iSendBufLen - 4);
	//LOGD("int [%d] hex:%02x htons:%02x",ret1, ret1,htons(ret1));
	
	//*(unsigned short*)(TmpStrData.pSendBuf+TmpStrData.iSendBufLen-3) = htons(ret1);

	iWritedLen = -1;
	//LOGD("TmpStrData.iFd[%d] SNEDLEN[%d] %s %d\r\n",TmpStrData.iFd,TmpStrData.iSendBufLen,__FUNCTION__, __LINE__);
	iWritedLen = WriteN(TmpStrData.iFd, (void *)(TmpStrData.pSendBuf), TmpStrData.iSendBufLen);

	/*
	LOGD("\r\n");
	for(int j = 0;j <TmpStrData.iSendBufLen ;j++)
	{
		LOGD("%02x ",(char)TmpStrData.pSendBuf[j]);
	}
	LOGD("\r\n");
	*/

	
	
	if(iWritedLen != TmpStrData.iSendBufLen)
	{
	}
	
	Free(TmpStrData.pSendBuf);
	
	TmpStrData.pSendBuf = NULL;

	return;
}

/**************************************************************\
** �������ƣ� GetClientAuthSucc
** ���ܣ� ����ն���Ϊ�ͻ����Ƿ�������֤�ɹ�
** ������
** ���أ� ����iSocketServFd
** �������ߣ� ������
** �������ڣ� 2012-5-21
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
int CommLayerTcp::GetClientAuthSucc()
{
	return iClientAuthSucc;
}

void CommLayerTcp::SetClientAuthSucc(int flag)
{
	iClientAuthSucc = flag;
}

/**************************************************************\
** �������ƣ� GetSocketServFd
** ���ܣ� ���iSocketServFd
** ������
** ���أ� ����iSocketServFd
** �������ߣ� ������
** �������ڣ� 2012-5-21
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
int CommLayerTcp::GetSocketServFd()
{
	return iSocketServFd;
}

/**************************************************************\
** �������ƣ� GetSocketClieFd
** ���ܣ� ���GetSocketClieFd
** ������
** ���أ� ����GetSocketClieFd
** �������ߣ� ������
** �������ڣ� 2012-5-21
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
int CommLayerTcp::GetSocketClieFd()
{
	return iSocketClieFd;
}

void CommLayerTcp::SetSocketClieFd(int InFd)
{
	iSocketClieFd = InFd;
}

void CommLayerTcp::SetSendThreadId(pthread_t ThreadId, int i)
{
	SendThreadId[i] = ThreadId;
	
	return;
}

void CommLayerTcp::GetSendThreadId(pthread_t *OutThreadId, int i)
{
	*OutThreadId = SendThreadId[i];
	
	return;
}

void CommLayerTcp::GetRecvThreadId(pthread_t *OutThreadId, int i)
{
	*OutThreadId = RecvThreadId[i];
	
	return;
}

void CommLayerTcp::SetRecvThreadId(pthread_t ThreadId, int i)
{
	RecvThreadId[i] = ThreadId;
	
	return;
}

pthread_t CommLayerTcp::GetClientSendHeartbeatThreadId()
{
	return ClientSendHeartbeatThreadId;
}

void CommLayerTcp::SetClientSendHeartbeatThreadId(pthread_t Inid)
{
	ClientSendHeartbeatThreadId = Inid;
	
	return;
}

void CommLayerTcp::CloseSocketClieFd()
{
	close(iSocketClieFd);
	iSocketClieFd = -1;
	
	return;
}

int CommLayerTcp::GetCurrConnNum()
{
	return iCurrConnNum;
}

/**************************************************************\
** �������ƣ� AddAllFd
** ���ܣ� ���������ӵ�Fd��¼����
** ������   
         InFd : �������ӵ�Fd
** ���أ�true-�ɹ���false-ʧ��
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
bool CommLayerTcp::AddAllFd(int InFd)
{
	int i = 0;
	bool ret = false;
	
	pthread_cleanup_push(CleanupMutex, (void *)&AllFdMutex);
	
	if(pthread_mutex_lock(&AllFdMutex) != 0)
	{
	}
	
	if(iCurrConnNum >= MAX_CONN_NUM)
	{
		ret = false;
		goto SETALLFD_END;
	}
	
	for(i = 0; i < MAX_CONN_NUM; i++)
	{
		if(iAllFd[i] == -1)
		{
			iAllFd[i] = InFd;
			iCurrConnNum++;

			break;
		}
	}

    memcpy(giServerFd, iAllFd, sizeof(iAllFd));
	
	if(i >= MAX_CONN_NUM)
	{
		ret = false;
		goto SETALLFD_END;
	}

	ret = true;

SETALLFD_END:
	if(pthread_mutex_unlock(&AllFdMutex) != 0)
	{
	}
	
	pthread_cleanup_pop(0);
	
	return(ret);
}

bool CommLayerTcp::SubAllFd(int InFd)
{
	int i=0, iCount=0;
	bool ret=false;
	
	pthread_cleanup_push(CleanupMutex, (void *)&AllFdMutex);
	if(pthread_mutex_lock(&AllFdMutex) != 0)
	{
	}
	
	if(iCurrConnNum <= 0)
	{
		ret = false;
		goto SETALLFD_END;
	}
	
	iCount = 0;
	
	for(i=0; i<MAX_CONN_NUM; i++)
	{
		if(iAllFd[i] == InFd)
		{
			close(iAllFd[i]);
			iAllFd[i] = -1;
			iCurrConnNum--;
			iCount++;
		}
	}

    memcpy(giServerFd, iAllFd, sizeof(iAllFd));

	if(iCount > 1)
	{
	}
	
	ret = true;

SETALLFD_END:
	if(pthread_mutex_unlock(&AllFdMutex) != 0)
	{
	}
	pthread_cleanup_pop(0);
	
	return(ret);
}

bool CommLayerTcp::AddRecvThreadID(pthread_t rThreadid)
{
	int i = 0;
	bool ret = false;
	
	pthread_cleanup_push(CleanupMutex, (void *)&RecvThreadIDMutex);
	
	if(pthread_mutex_lock(&RecvThreadIDMutex) != 0)
	{
	}
	
	if(iCurrConnNum > MAX_CONN_NUM)
	{
		ret = false;
		goto ADDRECV_END;
	}
	
	for(i = 0; i < MAX_CONN_NUM; i++)
	{
		if(RecvThreadId[i] == 0)
		{
			RecvThreadId[i] = rThreadid;
			//TRACE("add Client Num is %d %d %s %d\r\n",iCurrConnNum, rThreadid,__FILE__,__LINE__);
			break;
		}
	}
	
	if(i >= MAX_CONN_NUM)
	{
		ret = false;
		goto ADDRECV_END;
	}

	ret = true;

ADDRECV_END:
	if(pthread_mutex_unlock(&RecvThreadIDMutex) != 0)
	{
	}
	
	pthread_cleanup_pop(0);
	
	return(ret);
}

bool CommLayerTcp::SubRecvThreadID(pthread_t rThreadid)
{
	int i=0, iCount=0;
	bool ret=false;
	
	pthread_cleanup_push(CleanupMutex, (void *)&RecvThreadIDMutex);
	if(pthread_mutex_lock(&RecvThreadIDMutex) != 0)
	{
	}

	if(iCurrConnNum < 0)
	{
		ret = false;
		goto SUBRECV_END;
	}
	
	iCount = 0;
	for(i=0; i<MAX_CONN_NUM; i++)
	{
		//TRACE("sub Client 3Num is %d %d %d %s %d\r\n",iCurrConnNum,rThreadid,RecvThreadId[i],__FILE__,__LINE__);
		if(RecvThreadId[i] == rThreadid)
		{
			RecvThreadId[i] = 0;
			iCount++;
		}
	}
	
	if(iCount > 1)
	{
	}
	
	ret = true;

SUBRECV_END:
	if(pthread_mutex_unlock(&RecvThreadIDMutex) != 0)
	{
	}
	pthread_cleanup_pop(0);
	
	return(ret);
}

bool CommLayerTcp::SubAllCliFd()
{
	int i=0, iCount=0;
	bool ret=false;
	
	pthread_cleanup_push(CleanupMutex, (void *)&AllFdMutex);
	if(pthread_mutex_lock(&AllFdMutex) != 0)
	{
	}
	
	if(iCurrConnNum <= 0)
	{
		ret = false;
		goto SETALLFD_END;
	}
	
	iCount = 0;
	
	for(i=0; i<MAX_CONN_NUM; i++)
	{
		if(iAllFd[i] != -1)
		{
			close(iAllFd[i]);
			iAllFd[i] = -1;
			iCurrConnNum--;
			iCount++;
		}
	}

    memcpy(giServerFd, iAllFd, sizeof(iAllFd));

	if(iCount > 1)
	{
	}
	
	ret = true;

SETALLFD_END:
	if(pthread_mutex_unlock(&AllFdMutex) != 0)
	{
	}
	pthread_cleanup_pop(0);
	
	return(ret);
}

bool CommLayerTcp::PushSendDataQueue(int iFd, PROTOCOL_PACK *InStrProPack)
{	
	short dosignal = 0;
	StrTcpSendData TmpStrData;
	
	unsigned int dataLen = ntohl(InStrProPack->head.dataLen);

	pthread_cleanup_push(CleanupMutex, (void *)&CommLayerTcp::SendDataQueueMutex);
	if(pthread_mutex_lock(&CommLayerTcp::SendDataQueueMutex) != 0)
	{
		return(false);
	}

	memset(&TmpStrData, 0, sizeof(StrTcpSendData));
	TmpStrData.pSendBuf = NULL;
	TmpStrData.iSendBufLen = 0;

	TmpStrData.iSendBufLen = dataLen + sizeof(InStrProPack->head)+3;
	TmpStrData.iFd = iFd;

	//TmpStrData.pSendBuf = new char[TmpStrData.iSendBufLen + 1];
	TmpStrData.pSendBuf = (char *)Malloc(TmpStrData.iSendBufLen + 1);
	if (TmpStrData.pSendBuf != NULL)
	{
		memset(TmpStrData.pSendBuf, 0, TmpStrData.iSendBufLen + 1);
		memcpy(TmpStrData.pSendBuf, (char *)&(InStrProPack->head), sizeof(InStrProPack->head));
		memcpy(TmpStrData.pSendBuf + sizeof(InStrProPack->head), (char *)(InStrProPack->data), dataLen);
		memcpy(TmpStrData.pSendBuf + sizeof(InStrProPack->head)+dataLen, (char *)(&InStrProPack->chksum), 3);

		/*
		if ((InStrProPack->head.packType != RESPONSE)
			&& (InStrProPack->head.packType != HEARTBEAT))
		{
			TRACE("InStrProPack->head.packType %d InStrProPack->head.subType %d %s %d\r\n", 
				InStrProPack->head.packType, InStrProPack->head.subType, __FUNCTION__, __LINE__);
		}
		*/
		
		CommLayerTcp::SendDataQueue.Push(TmpStrData);

        dosignal = 1;
	}
	
	if(pthread_mutex_unlock(&CommLayerTcp::SendDataQueueMutex) != 0)
	{
		return(false);
	}
	
	pthread_cleanup_pop(0);

	if(dosignal)
	{
		if(pthread_cond_signal(&CommLayerTcp::SendDataQueueCond) != 0)
		{
			return(false);
		}
	}

	return(true);
}

/**************************************************************\
** �������ƣ� TcpSendThreadPool
** ���ܣ� ���������̳߳�
** ������ ��
** ���أ�
** �������ߣ� ������
** �������ڣ� 2012-5-21
** �޸����ߣ�
** �޸����ڣ�
\**************************************************************/
void *TcpSendThreadPool(void *arg)
{
	pthread_t ThreadId;

	CommLayerTcp *TcpObj = static_cast<CommLayerTcp *>(arg);

	ThreadId = pthread_self();

	if(pthread_detach(ThreadId) != 0)
	{
	}
	
	while(1)
	{
		if( iThreadExit > 0)
		{
			//LOGD(">quit exit TcpSendThreadPool %s %d\r\n",__FUNCTION__, __LINE__);
			//iThreadExit = 2;
			break;
		}
		TcpObj->DoTcpSendThreadPool(ThreadId);
	}
	LOGD("QUIT TcpSendThreadPool %s %d\r\n",__FILE__,__LINE__);
	iThreadExit--;

	pthread_exit(NULL);
}

/**************************************************************\
** �������ƣ� TcpServSubThread
** ���ܣ� �ն���Ϊ�������ˣ����Ӵ������߳�
** ������   
         arg :
** ���أ���
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
void *TcpServSubThread(void *arg)
{
	pthread_t ThreadId;
	int AcceptFd;

	StrLayerTcpTmp *TTNP_T = static_cast<StrLayerTcpTmp *>(arg);
	CommLayerTcp *TcpObj = TTNP_T->Obj;
	AcceptFd = TTNP_T->iFd;

	if(TTNP_T != NULL)
	{
		delete TTNP_T;
		TTNP_T = NULL;
	}

	ThreadId = pthread_self();

	if(pthread_detach(ThreadId) != 0)
	{
	}

	if(DoTcpServer(TcpObj, AcceptFd) < 0)
	{
	}
	
	if(close(AcceptFd) == -1)
	{
	}
	TRACE("CLOSE CLIENT SOCKET %d %s %d\r\n",AcceptFd,__FILE__,__LINE__);

	if(!TcpObj->SubAllFd(AcceptFd))
	{
	}
	
	if(!TcpObj->SubRecvThreadID(ThreadId))
	{
	}

	return(NULL);
}

void *TcpSrvAccept(void *arg)
{
	pthread_t ThreadId;

	CommLayerTcp *TcpObj = static_cast<CommLayerTcp *>(arg);

	ThreadId = pthread_self();

	if(pthread_detach(ThreadId) != 0)
	{
	}
	
	TcpObj->CreateTcpAccept();
    
	//MessageSend(MSG_RECONECT_SERVER, NULL, 0);

	//MessageSend(MSG_SRV_RESET, NULL, 0);
	//MessageSend(MSG_CLI_RECONN, NULL, 0);

    return(NULL);
}

/**************************************************************\
** �������ƣ� DoingAllForTcpServerThread
** ���ܣ� �ն���Ϊ���������߳�
** ������   
            arg : 
** ���أ���
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
void *DoingAllForTcpServerThread(void *arg)
{
	CommLayerTcp *TcpObj = NULL;
	pthread_t ThreadId;
	char **AllIp = NULL;
	int IpNum = 0;
	pthread_t pthread_id;
	TcpThreadsData TPTemp;
	int i;
	int isocketFd;
	CONFIG_NETWORK_STR TmpConfigNetWork;

	//pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    
	memset(&TPTemp, 0x00, sizeof(TcpThreadsData));
	memcpy(&TPTemp, (TcpThreadsData *)arg, sizeof(TcpThreadsData));
	
	if(arg != NULL)
	{
		Free(arg);
		arg = NULL;
	}
	
	pthread_id = pthread_self();
	if(pthread_detach(pthread_id) != 0)
	{
	}

	TcpObj = new CommLayerTcp();

	GetHostAllIp(&AllIp, &IpNum);

	FreeCharMatrix(&AllIp, IpNum);

	LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
	/* ���������߳� */
	for(i = 0; i < MAX_SEND_THREADS; i++)
	{
		if(pthread_create(&ThreadId, NULL, &TcpSendThreadPool, (void *)TcpObj) != 0)
		{
			exit(-1);
		}
		else
		{
			TcpObj->SetSendThreadId(ThreadId, i);
		}
	}
	while(1)
	{
	sleep(1000);
	# if 1
		/* TCP������ʼ�� */
		if(TcpObj->CommLayerTcpInit(TCP_SERVER, TPTemp.acServIp, TPTemp.iServPort) < 0)
		{
			goto ServerThreadEnd;
		}

		/* TCP�������˴������̣�ͣ����listen�׶� */
		if(!(TcpObj->CreateTcpServ()))
		{
            sleep(1);
			
			continue;
		}
		        
		if(pthread_create(&ThreadId, NULL, &TcpSrvAccept, (void *)TcpObj) != 0)
		{
            TRACE(" %s %d\r\n", __FUNCTION__, __LINE__);
            
			goto ServerThreadEnd;
		}
		
		while(1)
		{
			usleep(10*1000);
            
			/* ���յ�������������Ϣ��Ϣ�������������� */
/*
			if (MessageGet(MSG_SRV_RESET, NULL, 0, 0) >= 0)
			{
				TRACE("\r\nrecv MSG_SRV_RESET %s %d\r\n\r\n", __FUNCTION__, __LINE__);
				break;
			}

			if (MessageGet(MSG_SRV_QUIT, NULL, 0, 0) >= 0)
			{
				TRACE("\r\nrecv MSG_SRV_QUIT %s %d\r\n\r\n", __FUNCTION__, __LINE__);
				goto ServerThreadEnd;
			}
			*/
		}

		TcpObj->SubAllCliFd();
		isocketFd = TcpObj->GetSocketServFd();

		if(isocketFd != -1)
		{
			close(isocketFd);	
		}
		
		memset(&TmpConfigNetWork, 0, sizeof(TmpConfigNetWork));
		//GetConfigNetWork(&TmpConfigNetWork);

		/* ����ip��Ч */
		unsigned long ulIP = inet_addr(TmpConfigNetWork.caIp);
		unsigned long ulNetMask = inet_addr(TmpConfigNetWork.caNetMask);
		/*
		SetNet((char *)"eth0",
				ulIP,
				ulNetMask,
				TmpConfigNetWork.caGateway,
				TmpConfigNetWork.caDns,
				0,
				NULL,
				NULL,
				0,
				1);
		*/
		strncpy(TPTemp.acServIp, TmpConfigNetWork.caIp, sizeof(TmpConfigNetWork.caIp));
		
		if(ThreadId > 0)
		{
           // pthread_cancel(ThreadId);
            ThreadId = 0;
		}
		
		for(i = 0; i < MAX_CONN_NUM; i++)
		{
			ThreadId = 0;
			
			TcpObj->GetRecvThreadId(&ThreadId, i);
			if(ThreadId > 0)
			{
               //pthread_cancel(ThreadId);
               TcpObj->SetRecvThreadId(0, i);
               ThreadId = 0;
			}
		}		
		#endif
	}
	
ServerThreadEnd:
    if(ThreadId > 0)
	{
      //  pthread_cancel(ThreadId);
        ThreadId = 0;
	}
    
	for(i = 0; i < MAX_SEND_THREADS; i++)
	{
		ThreadId = 0;
		
		TcpObj->GetSendThreadId(&ThreadId, i);

        if (ThreadId > 0)
        {
    		//pthread_cancel(ThreadId);
            TcpObj->SetSendThreadId(0, i);
            ThreadId = 0;
        }
	}

	for(i = 0; i < MAX_CONN_NUM; i++)
	{
		ThreadId = 0;
		
		TcpObj->GetRecvThreadId(&ThreadId, i);

        if(ThreadId > 0)
		{
           // pthread_cancel(ThreadId);
            TcpObj->SetRecvThreadId(0, i);
            ThreadId = 0;
		}
	}
    
	sleep(1);
    
	LOGD("QUIT NET SERVER %s %d\r\n",__FILE__,__LINE__);
    
	delete TcpObj;
	TcpObj = NULL;

	return(NULL);
}

/**************************************************************\
** �������ƣ� DoingAllForTcpClientThread
** ���ܣ� �ն���Ϊ�ͻ����߳�
** ������   
            arg : 
** ���أ���
** �������ߣ� ��͢��
** �������ڣ� 2012-8-22
** �޸����ߣ� 
** �޸����ڣ� 
\**************************************************************/
void *DoingAllForTcpClientThread(void *arg)
{
	CommLayerTcp *TcpObj = NULL;
	pthread_t ThreadId;
	pthread_t pthread_id;
	TcpThreadsData TPTemp;
	CONFIG_NETWORK_STR TmpConfigNetWork;
	int i;
	int ret = -1;

    //pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, NULL);
    
	memset(&TPTemp, 0, sizeof(TcpThreadsData));
	memcpy(&TPTemp, (TcpThreadsData *)arg, sizeof(TcpThreadsData));
	
	if(arg != NULL)
	{
		Free(arg);
		arg = NULL;
	}
	
	pthread_id = pthread_self();
	
	if(pthread_detach(pthread_id) != 0)
	{
	}

	TcpObj = new CommLayerTcp();

	/* ���������߳� */
    //ȡ�����ͻ��˵ķ��������߳�TcpSendThreadPool
	for(i = 0; i < MAX_SEND_THREADS; i++)
	{
		if(pthread_create(&ThreadId, NULL, &TcpSendThreadPool, (void *)TcpObj) != 0)
		{
			goto ClientThreadEnd;
		}
		else
		{
			TcpObj->SetSendThreadId(ThreadId, i);
		}
	}
    
	while(1)
	{
		if(iThreadExit > 0)
		{
			//LOGD("quite exit  DoTcpClient %s %d\r\n",__FUNCTION__, __LINE__);
			if(pthread_cond_signal(&CommLayerTcp::SendDataQueueCond) != 0)
			{
				return(false);
			}
			break;
		}
		
		if(TcpObj->CommLayerTcpInit(TCP_CLIENT, TPTemp.acClieIp, TPTemp.iCliePort) < 0)
		{
			goto ClientThreadEnd;
		}

		/* �������������߳� */
		
		if(pthread_create(&ThreadId, NULL, &ClientSendHeartbeatThread, (void *)TcpObj) != 0)
		{
			exit(-1);
		}
		else
		{	
			TcpObj->SetClientSendHeartbeatThreadId(ThreadId);
		}
		
		ret = DoTcpClient(TcpObj);
		LOGD("ret[%d] %s %d\r\n",ret , __FUNCTION__, __LINE__);
		if(2 == ret)
		{
			LOGD("\r\n NetWork Client moudle quit %s %d\r\n\r\n", __FUNCTION__, __LINE__);
			memset(&TmpConfigNetWork, 0, sizeof(TmpConfigNetWork));
			//GetConfigNetWork(&TmpConfigNetWork);
			TPTemp.iCliePort = TmpConfigNetWork.iPort;
			strncpy(TPTemp.acClieIp, TmpConfigNetWork.caServerIP, sizeof(TmpConfigNetWork.caServerIP));

    		sleep(2);
        }
		else if(3 == ret)
		{
			//MessageSend(MSG_NET_HEARTBEAT_QUIT, NULL, 0);
			break;
		}
		
		if(ThreadId > 0)
		{
            //pthread_cancel(ThreadId);
            ThreadId = 0;
		}
		
	}

	//LOGD("\r\n exit tcpclient th %s %d\r\n\r\n", __FUNCTION__, __LINE__);

ClientThreadEnd:
	ThreadId = TcpObj->GetClientSendHeartbeatThreadId();

    if(ThreadId > 0)
	{
       // pthread_cancel(ThreadId);
        TcpObj->SetClientSendHeartbeatThreadId(0);
	}

	for(int i = 0; i < MAX_SEND_THREADS; i++)
	{
		ThreadId = 0;
		TcpObj->GetSendThreadId(&ThreadId, i);

        if(ThreadId > 0)
		{
            //pthread_cancel(ThreadId);
            TcpObj->SetSendThreadId(0, i);
            ThreadId = 0;
		}
	}
	sleep(1);
	memset(g_chUserName,0x00,sizeof(g_chUserName));
	LOGD("QUIT DoingAllForTcpClientThread %s %d\r\n",__FILE__,__LINE__);
	iThreadExit--;
	g_AuthFlag = 0;
	delete TcpObj;
	TcpObj = NULL;

	return(NULL);
}

void *ClientSendHeartbeatThread(void *arg)
{
	pthread_t ThreadId;
	int ClieFd;
	int ClinetAuthSucc = 0;
	time_t tHeartbeat = time(NULL);

	CommLayerTcp *TcpObj = static_cast<CommLayerTcp *>(arg);

	ThreadId = pthread_self();

	if(pthread_detach(ThreadId) != 0)
	{
	}
	
	while(1)
	{
		/* ���յ�������������Ϣ��Ϣ�������������� */
		/*
		if (MessageGet(MSG_NET_HEARTBEAT_QUIT, NULL, 0, 0) >= 0)
		{
			TRACE("\r\nrecv MSG_NET_HEARTBEAT_QUIT %s %d\r\n\r\n", __FUNCTION__, __LINE__);

			break;
		}
		*/
		if( iThreadExit > 0)
		{
			//LOGD("QUIT exit ClientSendHeartbeatThread %s %d\r\n",__FILE__,__LINE__);
			//iThreadExit = 3;
			break;
		}
		
		ClieFd = -1;
		ClieFd = TcpObj->GetSocketClieFd();

		ClinetAuthSucc = TcpObj->GetClientAuthSucc();
		
		if(ClieFd > 0 && ClinetAuthSucc == 1 && (time(NULL) - tHeartbeat >10))
		{
			DealDoSendHeartbeat(TcpObj);
		}
		sleep(1);
	}
	LOGD("QUIT ClientSendHeartbeatThread %s %d\r\n",__FILE__,__LINE__);
	iThreadExit--;

	pthread_exit(NULL);
}

