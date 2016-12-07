
#include "maintainOutLib.h"
#include "netLib.h"
#include "NetDeal.h"
#include "netComm.h"
#include<jni.h>
#include "facelib.h"
/* �ն���Ϊ�ͻ��ˣ����ӵķ������˿� */
static int g_iServPort = 0;
/* �ն���Ϊ�ͻ��ˣ����ӵķ�����IP */
static char g_acServIp[16] = {0};
/* �ն���Ϊ�������ˣ����ŵĶ˿� */
int g_iCliePort = 0;
/* �ն���Ϊ�������˵�IP */
char g_acClieIp[16] = {0};
int g_ipChangeFlag = 0;			//������ip�������־
int SetClientIP(char *pclientIp, int clientPort, int ipChanged);

/* ��������ģ�� */
void StartNetModule()
{
	pthread_t NetThreadId;	
	CONFIG_NETWORK_STR TmpConfigNetWork;

	//iThreadExit = 0;
	
	memset(&TmpConfigNetWork, 0, sizeof(TmpConfigNetWork));
	//GetConfigNetWork(&TmpConfigNetWork);
	/*
	memset(g_acServIp, 0, sizeof(g_acServIp));
	memset(g_acClieIp, 0, sizeof(g_acClieIp));

	//"192.168.3.131"
	//strncpy(g_acServIp, TmpConfigNetWork.caIp, sizeof(g_acServIp));
	strncpy(g_acServIp, "192.168.3.130", strlen("192.168.3.130"));
	//strncpy(g_acClieIp, TmpConfigNetWork.caServerIP, sizeof(g_acClieIp));
	strncpy(g_acClieIp, "183.62.134.182", strlen("183.62.134.182"));
	*/
	//SetClientIP("183.62.134.182", COMM_PORT_SERVER, 0);
	
	//g_iCliePort = COMM_PORT_SERVER;//TmpConfigNetWork.iPort;
	//g_iServPort = COMM_PORT;

	TcpThreadsData *StrTcpTDTmp = (TcpThreadsData *)Malloc(sizeof(TcpThreadsData));
	TcpThreadsData *StrTcpTDTmp2 = (TcpThreadsData *)Malloc(sizeof(TcpThreadsData));
	
	StrTcpTDTmp->iServPort = g_iServPort;
	StrTcpTDTmp->acServIp = g_acServIp;
	StrTcpTDTmp->iCliePort = g_iCliePort;
	StrTcpTDTmp->acClieIp = g_acClieIp;

	memcpy(StrTcpTDTmp2,StrTcpTDTmp,sizeof(TcpThreadsData));
	/*
	if(pthread_create(&NetThreadId, NULL, &DoingAllForTcpServerThread, (void *)StrTcpTDTmp) != 0)
	{
		exit(-1);
	}
	*/
	//�ȴ������̶߳��˳������´���
	while(iThreadExit > 0)
	{
		sleep(1);
	}
	LOGD("\r\n xxxxxxxxxxxxx(10); Client moudle quit %s %d\r\n\r\n", __FUNCTION__, __LINE__);
	if(pthread_create(&NetThreadId, NULL, &DoingAllForTcpClientThread, (void *)StrTcpTDTmp2) != 0)
	{
		LOGD("\r\n 55555555555(10); DoingAllForTcpClientThread quit %s %d\r\n\r\n", __FUNCTION__, __LINE__);	
		exit(-1);
	}
	LOGD("\r\n 3333333333(10); Client moudle quit %s %d\r\n\r\n", __FUNCTION__, __LINE__);
	//sleep(1);
	LOGD("\r\n 88888888888sleep(10); Client moudle quit %s %d\r\n\r\n", __FUNCTION__, __LINE__);

}


int SetClientIP(char *pclientIp, int clientPort, int ipChanged)
{
	if(strlen(pclientIp)<=0 || clientPort <=0)
	{
		return -1;
	}

	memset(g_acClieIp, 0, sizeof(g_acClieIp));
	strncpy(g_acClieIp, pclientIp, strlen(pclientIp));

	g_iCliePort = clientPort;
	g_ipChangeFlag = ipChanged;
	
	return 1;
}
char* getSvrIP()
{
	return g_acClieIp;
}
void setThreadExit()
{
	iThreadExit = 3;
	g_AuthFlag = 0;
}

//ClientSendAuth();

/*
#ifdef __cplusplus
extern "C" {
#endif
*/

/*
int initTcp()
{
	StartNetModule();
	return 1;
}
*/
/*
#ifdef __cplusplus
}
#endif
*/
/*
int main()
{

	StartNetModule();
	while(1)
	{
		sleep(100);
	}
	return 1;
}
*/
