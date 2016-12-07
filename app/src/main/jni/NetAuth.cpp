#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <ctype.h>
#include <time.h>

#include "netComm.h"
#include "netLib.h"

#include "NetDeal.h"
#include "maintainOutLib.h"
#include "facelib.h"
#include "Public.h"


/* ���һ���ϴ���¼ʱ�� */
time_t lastRecordTime = 0;
char s_nVerifyTrapMode = 0;
int g_AuthFlag = 0;

extern pthread_mutex_t gRwPicMutex;
//-2�ȶ�ʧ��
//-1�ȶ�û��Ӧ
//0�ȶԳɹ�����û����ע������
//1���ϱ�ʾ�ɹ����ض��ٸ�ע������
int g_CompareFlag = -1;	
//������IP����״̬
int g_ServerSockFlag = 0;

#define _TERMINAL_TYPE_NO      "0" /* ��� */
#define _SIGNATURE		       ""

/*
**  ����ͻ�Ӧ������
0-�ɹ�;����-ʧ��
*/
int DealResponse(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack)
{
	int retVal = 0;
#if 0
	char *pSn = NULL;
	char szPlain[64];
	char szFinal[16];
	char sSN[16 + 1];
	int nPlainLen = 0;
	unsigned char md5HashCode[MD5_CHECK_CODE_LEN] = {0};
	LPAUTH_RAND pRandInfo;
	int nRandom;
	//MD5_CONTEXT md5_context;

	/*  �ж����ݰ����� */
	if ((InStrProPack->head.subType == ACK) 
		&& (InStrProPack->head.dataLen == 4) 
		&& (InStrProPack->head.index == AUTH_INDEX))
	{
		pRandInfo = (LPAUTH_RAND)InStrProPack->data;
		
		memset(szPlain, 0, sizeof(szPlain));
		memset(szFinal, 0, sizeof(szFinal));
		memset(sSN, 0, sizeof(sSN));

		strcpy(szPlain, VIELI_SERVER_PAWD);
		nPlainLen += strlen(VIELI_SERVER_PAWD);

		pSn = GetSerailStr();
		if(pSn)
		{
			strncpy(sSN, pSn, sizeof(sSN) - 1);
		}

		memcpy(&szPlain[nPlainLen], sSN, sizeof(sSN) - 1);
		nPlainLen += sizeof(sSN)-1;

		nRandom = ntohl(pRandInfo->rand);
		memcpy(&szPlain[nPlainLen], &nRandom, sizeof(nRandom));
		nPlainLen += sizeof(nRandom);
		
		//MD5Init(&md5_context);
		//MD5Update(&md5_context, (unsigned char*)szPlain, nPlainLen);
		//MD5Final(md5HashCode, &md5_context);

		/* �ն��յ�����֤����󣬷�����֤����� */
		//retVal = TcpSendPack(PROTOCOL_HEAD_FLAG, InStrProPack->head.index, AUTH,
		//	AUTH_AUTHENTICATE, sizeof( md5HashCode ), (void *)md5HashCode, OutStrProPack);
	}
	else if((InStrProPack->head.subType == ACK) 
		&& (InStrProPack->head.dataLen == 0) 
		&& (InStrProPack->head.index == AUTH_INDEX))
	{
		TRACE("Sucess Sucess Sucess Sucess Sucess ..............%s %d\r\n", __FUNCTION__, __LINE__);
		/* ��������֤�ɹ� */
		StrInlinkShare->authFlag = 1;

		/* ����Ҫ���� */
		retVal = -1;
	}
	else if((InStrProPack->head.subType == NAK) 
		&& (InStrProPack->head.dataLen == 0) 
		&& (InStrProPack->head.index == AUTH_INDEX))
	{
		TRACE("Validation fails ..............%s %d\r\n", __FUNCTION__, __LINE__);
		/* ��������֤ʧ�� */
		StrInlinkShare->authFlag = -2;

		/* ����Ҫ���� */
		retVal = -1;
	}
	else
	{
		retVal = -1;
	}
#endif
	return retVal;	
}

/* ------------------------------------------------------------------------- */
/*
**  ����ͻ�������֤����
**  ��ȡ��֤������ɹ�����0����֤�ɹ�����1����֤ʧ�ܷ��ظ���
*/
int DealMsgAuth(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack)
{
	LOGD("InStrProPack->head.subType:%d %s %d\r\n",InStrProPack->head.subType, __FUNCTION__, __LINE__);
	LOGD("InStrProPack->head.dataLen:%d %s %d\r\n",InStrProPack->head.dataLen, __FUNCTION__, __LINE__);
	int retVal = 1;
	CONFIG_SECURITY_STR strCfgSecurity;

	/*  �ж����ݰ����� */
	if((InStrProPack->head.subType == NMSG_SUBTYPE_RESPONSE) 
		&& (InStrProPack->head.dataLen >0))
	{//��¼�ɹ�
		retVal = 1;
		
		g_AuthFlag = 1;
		StrInlinkShare->randFlag = 1;
	}
	else if((InStrProPack->head.subType == NMSG_SUBTYPE_ERROR)
		&& (InStrProPack->head.dataLen == 4))
	{
		retVal = -2;
		g_AuthFlag = -1;

	}
	else//���������ش���
	{
		retVal = -1;
		g_AuthFlag = 0;
	}
	return retVal;
}

/**********************************
�������ƣ� ClientSendAuth
��������: ��������������Ӽ�Ȩ����
��ڲ�����
		sockID	: socket ID
����ֵ  ��
		0	: �ɹ�
		����: ʧ��
************************************/
int ClientSendAuth(char* pUserName, char* pPwd, char* pMac)
{
	g_AuthFlag = 0;
	int ret = 0;
	char *pSn = NULL;

	int ClieFd = -1;
	PROTOCOL_PACK OutStrProPack;
	
	memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
	OutStrProPack.data = NULL;

	ClieFd = giClieFd;
	
	LOGIN userInfo;
	int nLen = sizeof(LOGIN);

	memset(&userInfo, 0, sizeof(LOGIN));
	
	userInfo.ver = htonl(FACETCP_VERSION);
	strncpy(userInfo.user, pUserName, strlen(pUserName));
	strncpy(userInfo.pass, pPwd, strlen(pPwd));
	strncpy(userInfo.mac, pMac, strlen(pMac));

	LOGD("login in username[%s] pwd[%s] mac[%s] nLen[%d]\r\n",userInfo.user,userInfo.pass,userInfo.mac,nLen);

	if(TcpSendPack(PROTOCOL_HEAD_FLAG, AUTH_INDEX, NTYPE_FACECAP_WORK,NMSG_CNT_LOGIN,
			NMSG_SUBTYPE_REQUEST, nLen, (void *)&userInfo, &OutStrProPack) < 0)
	{
		ret = -1;
		goto ClientSendAuth_END;
	}
//LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
	if(CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false)
	{
		ret = -1;
		goto ClientSendAuth_END;
	}
	//LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
ClientSendAuth_END:

	Free(OutStrProPack.data);
	//LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
	return ret;
}


/**********************************
�������ƣ� ClientSendFaceAuth
��������: ��������������Ӽ�Ȩ����,������֤
��ڲ�����
		sockID	: socket ID
����ֵ  ��
		0	: �ɹ�
		����: ʧ��
************************************/
int ClientSendFaceAuth(char* pUserName, char* pMac, char* pFace, int len)
{
	g_AuthFlag = 0;
	int ret = 0;
	char *pSn = NULL;
	char *pTemp = NULL;
	int macLen = 24;
	int ClieFd = -1;
	PROTOCOL_PACK OutStrProPack;
	
	memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
	OutStrProPack.data = NULL;

	ClieFd = giClieFd;
	
	FACE_LOGIN userInfo;
	int nLen = sizeof(FACE_LOGIN);

	memset(&userInfo, 0, sizeof(FACE_LOGIN));
	
	userInfo.ver = htonl(FACETCP_VERSION);
	strncpy(userInfo.user, pUserName, strlen(pUserName));
	userInfo.faceLen = htonl(len);
	//strncpy(userInfo.pass, pPwd, strlen(pPwd));

	//LOGD("login in username[%s] pwd[%s] nLen[%d]\r\n",userInfo.user,userInfo.pass,nLen);

	pTemp = (char*)malloc(nLen+len+macLen+1);
	memcpy(pTemp,&userInfo,nLen);
	//LOGD("memcpy(pTemp+nLen,pPicture,nLen[%d])sizeof(unsigned short)[%d]picCompare.maxres[%d]",nLen,sizeof(unsigned short),picCompare.maxres);
	memcpy(pTemp+nLen,pFace,len);
	//��mac��ַ
	memcpy(pTemp+nLen+len,pMac,macLen);
	
	if(TcpSendPack(PROTOCOL_HEAD_FLAG, AUTH_INDEX, NTYPE_FACECAP_WORK,NMSG_CNT_FACELOGIN,
			NMSG_SUBTYPE_REQUEST, nLen+len+macLen, (void *)pTemp, &OutStrProPack) < 0)
	{
		ret = -1;
		goto ClientSendAuth_END;
	}
//LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
	if(CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false)
	{
		ret = -1;
		goto ClientSendAuth_END;
	}
	//LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
ClientSendAuth_END:

	Free(OutStrProPack.data);
	//LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n",__FUNCTION__, __LINE__);
	return ret;
}

int ClientFaceAuth(char* pUserName, char* pMac)
{
	char strFile[] = "/data/data/com.firs.facedetecttosvr/temp/facePic.jpg";
	int len = 0;
	char* pPic = NULL;

	LOGD("READ PIC FILE");
	pthread_mutex_lock(&gRwPicMutex);
	ReadFile(strFile, &pPic, &len);
	pthread_mutex_unlock(&gRwPicMutex);
	LOGD("pUserName [%s] SEND PIC FILE  len[%d]",pUserName,len);
	
	ClientSendFaceAuth(g_chUserName,g_chMac,pPic,len);
	LOGD("ClientSendFaceAuth end");
	if(pPic != NULL)
	{
		free(pPic);
		pPic = NULL;
	}
	return 1;
}

int getAuth()
{
	return g_AuthFlag;
}
int getServerSockFlag()
{
	return g_ServerSockFlag;
}
void setServerSockFlag(int serverSockFlag)
{
	g_ServerSockFlag = serverSockFlag;
}
int getCompareFlag()
{
	return g_CompareFlag;
}
void setCompareFlag(int iCompareFlag)
{
	g_CompareFlag = iCompareFlag;
}
