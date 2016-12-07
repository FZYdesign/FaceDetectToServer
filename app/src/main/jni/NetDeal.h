
#ifndef NETDEAL_H
#define NETDEAL_H
#include<unistd.h>
#include "netComm.h"
/* ������������ʱʱ�� */
#define RECONNECT_SERVER_TIME 1
extern int g_iCliePort;
/* �ն���Ϊ�������˵�IP */
extern char g_acClieIp[16];
extern int g_ipChangeFlag;			//������ip�������־
extern char g_chUserName[32];
extern char g_chPwd[32];
extern char g_chMac[24];

extern int giClieFd;
//�û�ģ����Ƭ��Ϣ
typedef struct
{
	char photoName[256];
	int photoLen;
}tmpl_photo_info_t;

typedef struct
{
	int faceNum;
	int score;
	int score1to1;
}limit_score_t;


extern int g_AuthFlag;
extern int g_Score;
extern limit_score_t g_LimitScore;
extern time_t GetTimeFromDate(char *buf);
extern int CopyFileToBuf(char *buf, int n, char *file);
extern char *FeatureDecrypt(char *pEncryFeature, int nFeatureLen, int *pnFeatureSize, int *pnFeatureNum);

int DealMsgGet(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);
//1:1���ؽ��
int DealMsgGet1T1(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);

int DealHeartbeat(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);
int DealDoSendHeartbeat(CommLayerTcp *&TcpObj);
int sendGetSvrPicture(unsigned int faceId);
int DealMsgGetFace(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);

/* 1.���ӽ��� */
int DealResponse(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);
int DealMsgAuth(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);
int ClientSendAuth(char* pUserName, char* pPwd, char* pMac);
int ClientSendFaceAuth(char* pUserName, char* pMac, char* pFace, int len);
int ClientFaceAuth(char* pUserName, char* pMac);
int sendModifyPasswd(char* pUserName, char* pOldPasswd, char* pNewPasswd);
//�޸����뷵�ؽ��
int DealMsgModifyPasswd(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);
int getModifyPasswdFlag();
void setModifyPasswdFlag(int flag);

int snedPictureToSvr(unsigned char* pPicture, int len);
int sendPictureToSvr1T1(char* pPicture, int len, char* pPicture1T1, int len1T1);

//ע������
int sendRegisterFace(char* pPicture, int len);
int DealMsgRegisterFace(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);
int getRegisterFaceFlag();
void setRegisterFaceFlag(int flag);


COMPARE_RESULT get_CompareResult(int faceNum);

COMPARE_1T1_RESULT get_1T1CompareResult();


int getServerSockFlag();

void setServerSockFlag(int serverSockFlag);

unsigned short check_sum(unsigned char* data, int  data_len); //��������У���

#endif

