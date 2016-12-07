/********************************************************************************
**  Copyright (c) 2012, 深圳市飞瑞斯科技有限公司
**  All rights reserved.
**
**  文件名称： NetDeal.cpp
**  参考：对TCP、UDP、232、485所有交互业务处理
**
**  当前版本：1.0
**
**  创建作者：张祖异
**  创建日期:  2012.5.21
**
**  修改作者：
**  修改日期:
********************************************************************************/
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
#include "public.h"


extern int g_AuthFlag;
extern int g_CompareFlag;

//修改密码返回结果
int g_ModifyPasswdFlag = -1;
//注册人脸
int g_RegisterFaceFlag = -1;

char g_chUserName[32] = {0};
char g_chPwd[32] = {0};
char g_chMac[24] = {0};

int g_FaceNum = 0;//符合要求的人脸数量


//using namespace FrameWork;

/* 终端作为客户端时，保存全局的socket id */
int giClieFd = -1;
int giServerFd[MAX_CONN_NUM] = {-1, -1, -1, -1, -1};

unsigned int gaiResponsePkts = 1;    //主动上报的应答数
unsigned int gLastResponsePkt = 0;  //主动上报的上次最后应答数
unsigned int guiUpResponseNum = 0;  //主动上报记录应答的总数，30条写一次时间配置

COMPARE_RESULT g_CompareResult;
COMPARE_1T1_RESULT g_1T1compareResult;

COMPARE_RESULT g_CmpRst[10];


int g_Score = 30;
limit_score_t g_LimitScore;


/**************************************************************\
** 函数名称： DoTcpServer
** 功能： 终端作为服务器端，协议处理函数
** 参数：   
         TcpObj : tcp连接对象
         InFd   : socket id
** 返回：0-成功，其它-失败
** 创建作者： 颜廷军
** 创建日期： 2012-8-22
** 修改作者： 
** 修改日期： 
\**************************************************************/
int DoTcpServer(CommLayerTcp *&TcpObj, int InFd) {
    int ret;
    int flag = 0;
    PROTOCOL_PACK InStrProPack, OutStrProPack;
    INLINK_SHARE StrInlinkShare;

    time_t tRecvTime = 0;    /* SDK接收到数据包的时间 */
    time_t tNoRecvTime = 0; /* SDK收不到数据的时间 */

    memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
    OutStrProPack.data = NULL;

    memset(&InStrProPack, 0, sizeof(PROTOCOL_PACK));
    InStrProPack.data = NULL;

    memset(&StrInlinkShare, 0, sizeof(INLINK_SHARE));
    StrInlinkShare.randFlag = 0;
    StrInlinkShare.randNum = 0;
    StrInlinkShare.authFlag = -1;


    tRecvTime = time(NULL);

    while (1) {
        memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
        OutStrProPack.data = NULL;

        memset(&InStrProPack, 0, sizeof(PROTOCOL_PACK));
        InStrProPack.data = NULL;

        ret = -1;

        /* 读取报文头部分 */
        ret = TcpObj->ReadN(InFd, RECV_TIME_OUT, (void *) (&InStrProPack.head),
                            sizeof(InStrProPack.head));
#if 0
        /* 如果ret为-2，表明没有收到数据，则continue */
        if (ret == -2)
        {
            continue;
        }
#endif
        if (ret == -2) {
            /* 如果ret为-2，表明没有收到数据，则continue */
            tNoRecvTime = time(NULL);

            /* 收不到SDK时间为3个心跳周期则重连 */
            //TRACE("%d ... %s %d\r\n", tNoRecvTime - tRecvTime, __FUNCTION__, __LINE__);
            if (tNoRecvTime - tRecvTime > 30) {
                break;
            }
            else {
                continue;
            }
        }

        tRecvTime = time(NULL);

        /* 如果ret为0或为-1，表明网络异常，则break，重连 */
        if ((ret == 0) || (ret == -1)) {
            LOGD("ret %d %s %d\r\n", ret, __FUNCTION__, __LINE__);

            flag = -1;

            break;
        }

        InStrProPack.head.flag = ntohs(InStrProPack.head.flag);
        InStrProPack.head.index = ntohs(InStrProPack.head.index);
        InStrProPack.head.dataLen = ntohl(InStrProPack.head.dataLen);

        if (InStrProPack.head.dataLen > 0) {
            InStrProPack.data = (char *) Malloc(sizeof(char) * (InStrProPack.head.dataLen + 1));
            if (InStrProPack.data == NULL) {
                flag = -1;

                break;
            }

            ret = -1;
            memset(InStrProPack.data, 0, sizeof(char) * (InStrProPack.head.dataLen + 1));

            /* 读取报文内容部分 */
            ret = TcpObj->ReadN(InFd, RECV_TIME_OUT, (void *) InStrProPack.data,
                                InStrProPack.head.dataLen);
            if (ret <= 0) {
                flag = -1;

                break;
            }

            if ((unsigned int) ret != InStrProPack.head.dataLen) {
                flag = -1;

                break;
            }
        }

        /* 进行报文内容解析 */
        ret = AllDealTcp(InFd, &StrInlinkShare, &InStrProPack, &OutStrProPack);
        if (ret == 0) {
            /* 如果数据包子类型为识别用户或注册人脸，则不在此处集中发送应答包，
               而由这2个子类型的具体处理函数来发送应答包 */
            if ((USER_VERIFY_USER != InStrProPack.head.subType) &&
                (USER_ENROLL_FACE != InStrProPack.head.subType)) {
                if (CommLayerTcp::PushSendDataQueue(InFd, &OutStrProPack) == false) {
                }
            }
        }

        Free(InStrProPack.data);
        Free(OutStrProPack.data);

        usleep(20 * 1000);
    }

    //FreeRecordsGet(&(StrInlinkShare.recordsGet));

    Free(InStrProPack.data);
    Free(OutStrProPack.data);

    return (flag);
}

/**************************************************************\
** 函数名称： DoTcpClient
** 功能： 终端作为客户端，协议处理函数
** 参数：   
         TcpObj : tcp连接对象
** 返回：0-成功，其它-失败
** 创建作者： 颜廷军
** 创建日期： 2012-8-22
** 修改作者： 
** 修改日期： 
\**************************************************************/
int DoTcpClient(CommLayerTcp *&TcpObj) {
    int ret;
    int flag = 0;
    int ClieFd = -1;
    PROTOCOL_PACK InStrProPack;
    PROTOCOL_PACK OutStrProPack;
    INLINK_SHARE StrInlinkShare;
    bool IfReconn = false;
    time_t tLastTime;

    time_t tRecvTime = 0;    /* SDK接收到数据包的时间 */
    time_t tNoRecvTime = 0; /* SDK收不到数据的时间 */

    int i = 0;
    int iClientAuthSucc = 0;
    unsigned char ucProTail[3] = {0x00};

    while (1) {
        if (iThreadExit > 0) {
            //LOGD("quite exit  DoTcpClient %s %d\r\n",__FUNCTION__, __LINE__);
            break;
        }
        memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
        OutStrProPack.data = NULL;

        memset(&InStrProPack, 0, sizeof(PROTOCOL_PACK));
        InStrProPack.data = NULL;

        memset(&StrInlinkShare, 0, sizeof(INLINK_SHARE));
        StrInlinkShare.randFlag = 0;
        StrInlinkShare.randNum = 0;
        StrInlinkShare.authFlag = -1;

        IfReconn = false;
        ClieFd = -1;

        TcpObj->SetSocketClieFd(-1);
        if (1 == g_ipChangeFlag) {
            if (TcpObj->CommLayerTcpInit(TCP_CLIENT, g_acClieIp, g_iCliePort) < 0) {
                break;
            }
            g_ipChangeFlag = 0;
            LOGD("ret %d %s %d\r\n", ret, __FUNCTION__, __LINE__);
        }

        /* 连接服务器端 */
        if (!(TcpObj->CreateTcpClient())) {
            LOGD(">>>>>>>>>>>>>>>>>>>>>>> %s %d\r\n", __FUNCTION__, __LINE__);

            sleep(RECONNECT_SERVER_TIME);
            continue;
        }

        ClieFd = TcpObj->GetSocketClieFd();

        giClieFd = ClieFd;

        //socketed to server
        setServerSockFlag(1);

        //sleep(8);

        if (strlen(g_chUserName) > 0) {
            if (strlen(g_chPwd) <= 0) {
                ClientFaceAuth(g_chUserName, g_chMac);
            } else {
                /* 连接认证 */
                ClientSendAuth(g_chUserName, g_chPwd, g_chMac);
            }
        }
        tRecvTime = time(NULL);

        while (1) {
            if (iThreadExit > 0) {
                //LOGD("quite exit  DoTcpClient %s %d\r\n",__FUNCTION__, __LINE__);
                break;
            }
            if (1 == g_ipChangeFlag) {
                if (TcpObj->CommLayerTcpInit(TCP_CLIENT, g_acClieIp, g_iCliePort) < 0) {
                    IfReconn = false;
                }
                else {
                    IfReconn = true;
                }
                g_ipChangeFlag = 0;
                LOGD("ret %d %s %d\r\n", ret, __FUNCTION__, __LINE__);
                break;
            }

            memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
            OutStrProPack.data = NULL;

            memset(&InStrProPack, 0, sizeof(PROTOCOL_PACK));
            InStrProPack.data = NULL;

            ret = -1;

            /* 读取报文头部分 */
            ret = TcpObj->ReadN(ClieFd, RECV_TIME_OUT, (void *) (&InStrProPack.head),
                                sizeof(InStrProPack.head));

            /* 如果ret为0或为-1，表明网络异常，则break，重连 */
            if ((ret == 0) || (ret == -1)/* || (iUploadRecord == -3)*/) {
                LOGD("ret %d %s %d\r\n", ret, __FUNCTION__, __LINE__);

                IfReconn = true;

                break;
            }
            else if (ret == -2) {
                /* 如果ret为-2，表明没有收到数据，则continue */
                tNoRecvTime = time(NULL);

                /* 收不到SDK时间为3个心跳周期则重连 */
                //TRACE("%d ... %s %d\r\n", tNoRecvTime - tRecvTime, __FUNCTION__, __LINE__);
                if (tNoRecvTime - tRecvTime > 30) {
                    IfReconn = true;
                    LOGD("ret %d %s %d\r\n", ret, __FUNCTION__, __LINE__);
                    break;
                }
                else {
                    LOGD("ret %d %s %d\r\n", ret, __FUNCTION__, __LINE__);
                    continue;
                }
            }

            tRecvTime = time(NULL);

            //g_AuthFlag = 1;
            //LOGD("ret %d InStrProPack.head.dataLen %d %s %d\r\n", ret, InStrProPack.head.dataLen, __FUNCTION__, __LINE__);
            if (ret != sizeof(InStrProPack.head)) {
                LOGD("ret %d InStrProPack.head.dataLen %d %s %d\r\n", ret,
                     InStrProPack.head.dataLen, __FUNCTION__, __LINE__);

                IfReconn = true;

                break;
            }

            unsigned short chksum = check_sum((unsigned char *) &InStrProPack.head + 1,
                                              sizeof(PROTOCOL_HEAD) - 1);

            /*
            InStrProPack.head.flag = ntohs(InStrProPack.head.flag);
            InStrProPack.head.index = ntohs(InStrProPack.head.index);
            InStrProPack.head.dataLen = ntohl(InStrProPack.head.dataLen);
            */
            //InStrProPack.head.flag = ntohs(InStrProPack.head.flag);
            InStrProPack.head.index = ntohl(InStrProPack.head.index);
            InStrProPack.head.pack = ntohs(InStrProPack.head.pack);
            InStrProPack.head.packType = ntohs(InStrProPack.head.packType);
            InStrProPack.head.msgtype = ntohl(InStrProPack.head.msgtype);
            InStrProPack.head.subType = ntohs(InStrProPack.head.subType);
            InStrProPack.head.dataLen = ntohl(InStrProPack.head.dataLen);

            //LOGD("%06x",InStrProPack.head.msgtype);
            //LOGD("packType %d msgtype %d subType %d %s %d \r\n",
            //	InStrProPack.head.packType, InStrProPack.head.msgtype,InStrProPack.head.subType, __FUNCTION__, __LINE__);

            if (InStrProPack.head.msgtype != NMSG_CNT_HEARTBEAT) {
                LOGD("InStrProPack.head.index %d InStrProPack.head.dataLen %d %s %d \r\n",
                     InStrProPack.head.index, InStrProPack.head.dataLen, __FUNCTION__, __LINE__);
                LOGD("sizeof(InStrProPack.head)[%d]", sizeof(InStrProPack.head));
            }

            if (InStrProPack.head.dataLen > 0) {
                InStrProPack.data = (char *) malloc(sizeof(char) * (InStrProPack.head.dataLen + 1));
                if (InStrProPack.data == NULL) {
                    break;
                }

                memset(InStrProPack.data, 0, sizeof(char) * (InStrProPack.head.dataLen + 1));

                ret = -1;

                /* 读取报文内容部分 */
                ret = TcpObj->ReadN(ClieFd, RECV_TIME_OUT, (void *) InStrProPack.data,
                                    InStrProPack.head.dataLen);
                if ((unsigned int) ret != InStrProPack.head.dataLen) {
                    LOGD("ret %d InStrProPack.head.dataLen %d %s %d\r\n", ret,
                         InStrProPack.head.dataLen, __FUNCTION__, __LINE__);

                    IfReconn = true;

                    break;
                }
            }

            chksum += check_sum((unsigned char *) InStrProPack.data, InStrProPack.head.dataLen);

            //读取校验和包尾巴

            memset(ucProTail, 0, sizeof(ucProTail));
            ret = TcpObj->ReadN(ClieFd, RECV_TIME_OUT, (void *) ucProTail, 3);
            if ((unsigned int) ret != 3) {
                LOGD("ret %d read usTail fail %s %d\r\n", ret, __FUNCTION__, __LINE__);

                IfReconn = true;

                break;
            }

            unsigned short recvSum = 0;
            memcpy(&recvSum, ucProTail, 2);
            recvSum = ntohs(recvSum);

            //LOGD("chksum[%d] recvSum[%d] %s %d",chksum,recvSum, __FUNCTION__, __LINE__);

            memcpy(&InStrProPack.csuffix, ucProTail + 2, 1);
            //InStrProPack.csuffix = ntohs(InStrProPack.csuffix);
            //LOGD("InStrProPack.csuffix[%02x]\r\n",InStrProPack.csuffix);

            // 校验结果比较和包尾比较
            //	TRACE("ucCheckXor: %x ucProTail: %x %s %d\r\n",ucCheckXor, ucProTail[0],__FUNCTION__, __LINE__);
            //TRACE("InStrProPack.usTail: %x %s %d\r\n",InStrProPack.usTail, __FUNCTION__, __LINE__);
            if (InStrProPack.csuffix != PROTOCOL_TAIL_FLAG) {
                LOGD("InStrProPack.csuffix[%02x] %s %d\r\n", InStrProPack.csuffix, __FUNCTION__,
                     __LINE__);
                //continue;
            }

            /* 进行报文内容解析，并将欲发回的数据OutStrProPack插入队列 */
            ret = AllDealTcp(ClieFd, &StrInlinkShare, &InStrProPack, &OutStrProPack);
            if (ret == 0) {
                /* 如果数据包子类型为识别用户或注册人脸，则不在此处集中发送应答包，
                   而由这2个子类型的具体处理函数来发送应答包 */
                /*
                if ((USER_VERIFY_USER != InStrProPack.head.subType) && (USER_ENROLL_FACE != InStrProPack.head.subType))
                {
                    if(CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false)
                    {
                    }
                }
                */
            }
            Free(InStrProPack.data);
            Free(OutStrProPack.data);

            //临时使用下
            //g_AuthFlag = 1;
            //StrInlinkShare.authFlag = 1;

            /* 服务器验证失败，重连 */
            if (StrInlinkShare.authFlag == -2) {
                LOGD("Reconnect... %s %d\r\n", __FUNCTION__, __LINE__);
                //IfReconn = true;

                //break;
            }
            else if (StrInlinkShare.authFlag == 1) {
                iClientAuthSucc = TcpObj->GetClientAuthSucc();
                if (iClientAuthSucc == 0) {
                    /* 认证成功，可以发心跳 */
                    TcpObj->SetClientAuthSucc(1);
                }
            }

            usleep(20 * 1000);
        }
        TcpObj->CloseSocketClieFd();
        //Free(InStrProPack.data);
        //Free(OutStrProPack.data);

        //FreeRecordsGet(&(StrInlinkShare.recordsGet));

        LOGD("IfReconn %d %s %d\r\n", IfReconn, __FUNCTION__, __LINE__);

        if (IfReconn) {

            giClieFd = -1;

            /* 重置 */
            TcpObj->SetClientAuthSucc(0);

            //sleep(RECONNECT_SERVER_TIME);
            sleep(1);

            IfReconn = false;

            continue;
        }
        else {
            break;
        }
    }

    //LOGD("Exit DoTcpClient %s %d \r\n", __FUNCTION__, __LINE__);

    return flag;
}

int dataRcr(unsigned char *data, int len) {
    unsigned short sum = 0;
    int i = 0;
    for (i = 0; i < len; i++) {
        sum += data[i];
    }
    return sum;
}

unsigned short check_sum(unsigned char *data, int data_len) //计算数据校验和
{
    if ((NULL == data) || (0 == data_len)) {
        return 0;
    }

    unsigned short sum = 0;

    unsigned int n = data_len;
    unsigned char *src = data;

    while (n-- > 0) {
        //LOGD("%02x ",*src);
        sum += *src++;
    }

    return sum;
}


/**************************************************************\
** 函数名称： TcpSendPack
** 功能： 构造发送数据包
** 参数：
			flag 	 : 包标识
			index    : 序号
			packType : 包类型
			subType  : 子类型
			dataLen  : 数据长度
			data     :数据内容
			OutStrProPack : 响应包
** 返回： 0-成功
          其它-失败
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
int TcpSendPack(unsigned short flag, unsigned short index,
                unsigned short packType, unsigned int msgtype, unsigned short subType,
                unsigned int dataLen, void *data, PROTOCOL_PACK *OutStrProPack) {
    int i = 0;
    unsigned short chksum = 0;
    OutStrProPack->data = NULL;
    OutStrProPack->data = (char *) Malloc(dataLen + 1);

    if (OutStrProPack->data == NULL) {
        return -1;
    }
    //LOGD("send packType[%d] msgtype[%d] subTyep[%d]\r\n",packType,msgtype,subType);

    memset(OutStrProPack->data, 0, (dataLen + 1));
    OutStrProPack->head.flag = flag;
    OutStrProPack->head.index = htonl(index);
    OutStrProPack->head.pack = htons(0x00);
    OutStrProPack->head.packType = htons(packType);
    OutStrProPack->head.msgtype = htonl(msgtype);
    OutStrProPack->head.subType = htons(subType);
    OutStrProPack->head.dataLen = htonl(dataLen);
    memcpy(OutStrProPack->data, (char *) data, dataLen);

    OutStrProPack->chksum = check_sum((unsigned char *) &OutStrProPack->head + 1,
                                      sizeof(PROTOCOL_HEAD) - 1);

    OutStrProPack->chksum += check_sum((unsigned char *) OutStrProPack->data, dataLen);

    OutStrProPack->chksum = htons(OutStrProPack->chksum);
    //LOGD("<<<<<<<<<<<<<<<<<<<<<<chksum[%02x] OutStrProPack->chksum[%02x]\r\n",chksum,OutStrProPack->chksum);
    OutStrProPack->csuffix = PROTOCOL_TAIL_FLAG;

    //LOGD("<<<<<<<<<<<<<<<<<<<<<<sizeof(OutStrProPack->head)[%d] \r\n",sizeof(OutStrProPack->head));

    return 0;
}

/**************************************************************\
** 函数名称： AllDealTcp
** 功能： 协议解析，进行相应包类型处理
** 参数： 
		SockFd			: socket id
		StrInlinkShare	: 
		InStrProPack	: 接收到数据包
		OutStrProPack	: 要发送的数据包
** 返回： 0成功；-1失败
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
int AllDealTcp(int SockFd, INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack,
               PROTOCOL_PACK *OutStrProPack) {
    int retVal = -1;

    //LOGD("recv InStrProPack->head.msgtype 0x%x InStrProPack->head.subType 0x%x %s %d\r\n",
    //			InStrProPack->head.msgtype, InStrProPack->head.subType, __FUNCTION__, __LINE__);


    if (InStrProPack->head.msgtype != NMSG_CNT_HEARTBEAT) {
        LOGD("InStrProPack->head.packType 0x%x InStrProPack->head.subType 0x%x %s %d\r\n",
             InStrProPack->head.packType, InStrProPack->head.subType, __FUNCTION__, __LINE__);
    }

    //不是手机数据报，返回报错
    if (InStrProPack->head.packType != NTYPE_FACECAP_WORK) {
        return -1;
    }


    switch (InStrProPack->head.msgtype)        /* 分析数据包类型 */
    {
        case NMSG_CNT_FACELOGIN: //人脸登录
        case NMSG_CNT_LOGIN://处理密码登录返回结果
        {
            StrInlinkShare->authFlag = DealMsgAuth(StrInlinkShare, InStrProPack, OutStrProPack);
            if (StrInlinkShare->authFlag < 0) {
                retVal = -1;
            }
            else {
                retVal = 0;
            }

            break;
        }
        case NMSG_FACE_COMPARE://处理验证返回结果
        {
            LOGD("InStrProPack->head.packType 0x%x InStrProPack->head.subType 0x%x %s %d\r\n",
                 InStrProPack->head.packType, InStrProPack->head.subType, __FUNCTION__, __LINE__);
            if (StrInlinkShare->authFlag == 1) {
                //FreeRecordsGet(&(StrInlinkShare->recordsGet));

                retVal = DealMsgGet(StrInlinkShare, InStrProPack, OutStrProPack);
            }
            else {
                retVal = -1;
            }

            break;
        }
        case NMSG_FACE_COMPARE1T1://处理1:1比对验证返回结果
        {
            LOGD("InStrProPack->head.packType 0x%x InStrProPack->head.subType 0x%x %s %d\r\n",
                 InStrProPack->head.packType, InStrProPack->head.subType, __FUNCTION__, __LINE__);
            if (StrInlinkShare->authFlag == 1) {
                //FreeRecordsGet(&(StrInlinkShare->recordsGet));

                retVal = DealMsgGet1T1(StrInlinkShare, InStrProPack, OutStrProPack);
            }
            else {
                retVal = -1;
            }

            break;
        }
        case NMSG_USER_PASMOD://修改密码返回结果
        {
            LOGD("InStrProPack->head.packType 0x%x InStrProPack->head.subType 0x%x %s %d\r\n",
                 InStrProPack->head.packType, InStrProPack->head.subType, __FUNCTION__, __LINE__);
            if (StrInlinkShare->authFlag == 1) {
                retVal = DealMsgModifyPasswd(StrInlinkShare, InStrProPack, OutStrProPack);
            }
            else {
                retVal = -1;
            }

            break;
        }
        case NMSG_USER_FACE_ADD://注册人脸
        {
            LOGD("InStrProPack->head.packType 0x%x InStrProPack->head.subType 0x%x %s %d\r\n",
                 InStrProPack->head.packType, InStrProPack->head.subType, __FUNCTION__, __LINE__);
            if (StrInlinkShare->authFlag == 1) {
                retVal = DealMsgRegisterFace(StrInlinkShare, InStrProPack, OutStrProPack);
            }
            else {
                retVal = -1;
            }

            break;
        }
        case NMSG_FLIB_GET_SUB://处理获取人脸模板照片
        {
            LOGD("InStrProPack->head.packType %d InStrProPack->head.subType %d %s %d\r\n",
                 InStrProPack->head.packType, InStrProPack->head.subType, __FUNCTION__, __LINE__);
            if (StrInlinkShare->authFlag == 1) {
                //FreeRecordsGet(&(StrInlinkShare->recordsGet));

                retVal = DealMsgGetFace(StrInlinkShare, InStrProPack, OutStrProPack);
            }
            else {
                retVal = -1;
            }

            break;
        }
        case NMSG_CNT_HEARTBEAT: {
            if ((StrInlinkShare->authFlag == 1) && (InStrProPack->head.subType == 0)) {
                retVal = DealHeartbeat(StrInlinkShare, InStrProPack, OutStrProPack);
            }
            else {
                retVal = -1;
            }

            break;
        }

        default://包类型错误
        {
            retVal = -1;

            break;
        }
    }

    return retVal;
}

/* ------------------------------------------------------------------------- */
/*
**  函数: 查询请求（GET）
**  功能: 传入一个数据包，根据数据包的子类型做不同的处理
**  返回: 该函数处理成功返回0，出错则返回-1
*/
int DealMsgGet(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack,
               PROTOCOL_PACK *OutStrProPack) {
    int retVal = -1;
    int iret = 0;
    COMPARE_RESULT compareResult;
    int score = 0;

    // 处理该子类型信息
    switch (InStrProPack->head.subType) {
        case NMSG_SUBTYPE_RESPONSE:
            LOGD("NMSG_SUBTYPE_RESPONSE InStrProPack->head.dataLen[%d]sizeof(COMPARE_RESULT)[%d]\r\n",
                 InStrProPack->head.dataLen, sizeof(COMPARE_RESULT));
            if (InStrProPack->head.dataLen > 0) {
                if ((InStrProPack->head.dataLen - sizeof(COMPARE_DETECT)) %
                    sizeof(COMPARE_RESULT) == 0) {
                    g_FaceNum = (InStrProPack->head.dataLen - sizeof(COMPARE_DETECT)) /
                                sizeof(COMPARE_RESULT);

                    for (int i = 0; i < g_LimitScore.faceNum; i++) {
                        memset(&g_CmpRst[i], 0x00, sizeof(COMPARE_RESULT));
                    }
                    for (int i = 0; i < g_FaceNum; i++) {
                        memcpy(&g_CmpRst[i], InStrProPack->data + sizeof(COMPARE_DETECT) +
                                             i * (sizeof(COMPARE_RESULT)), sizeof(COMPARE_RESULT));
                        //memcpy(&score, InStrProPack->data+sizeof(COMPARE_DETECT),sizeof(int));
                        g_CmpRst[i].score = htonl(g_CmpRst[i].score);
                        g_CmpRst[i].f_id = htonl(g_CmpRst[i].f_id);
                        LOGD("g_CmpRst[i].score[%u] name[%s]", g_CmpRst[i].score,
                             g_CmpRst[i].f_name);
                    }

                    if (g_FaceNum != 0) {
                        g_CompareFlag = 0;//g_LimitScore.faceNum - g_FaceNum;
                    }
                    else {
                        g_CompareFlag = -3;
                        break;
                    }

                    //LOGD("g_CompareFlag[%d] g_FaceNum[%d]\r\n",g_CompareFlag,g_FaceNum);
                    //发送读取注册照片
                    for (int i = 0; i < g_FaceNum; i++) {
                        LOGD("GET PIC [%d]\r\n", i);
                        sendGetSvrPicture(g_CmpRst[i].f_id);
                    }
                } else {
                    g_CompareFlag = -2;
                }

            }

            break;
        case NMSG_SUBTYPE_ERROR:
            g_CompareFlag = -2;
            LOGD("NMSG_SUBTYPE_ERROR  InStrProPack->head.dataLen[%d]\r\n",
                 InStrProPack->head.dataLen);

            memcpy(&iret, InStrProPack->data, sizeof(int));
            iret = htonl(iret);
            LOGD(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>iret[%d]\r\n", iret);

            break;

        default: {
            retVal = -1;

            break;
        }
    }

    return retVal;
}

/* ------------------------------------------------------------------------- */
/*
**  函数: 查询请求（GET）
**  功能: 传入一个数据包，根据数据包的子类型做不同的处理
**  返回: 该函数处理成功返回0，出错则返回-1
*/
int DealMsgGet1T1(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack,
                  PROTOCOL_PACK *OutStrProPack) {
    int retVal = -1;
    int iret = 0;
    COMPARE_RESULT compareResult;
    int score = 0;

    // 处理该子类型信息
    switch (InStrProPack->head.subType) {
        case NMSG_SUBTYPE_RESPONSE:
            LOGD("1T1  NMSG_SUBTYPE_RESPONSE InStrProPack->head.dataLen[%d]sizeof(COMPARE_1T1_RESULT)[%d]\r\n",
                 InStrProPack->head.dataLen, sizeof(COMPARE_1T1_RESULT));
            if (InStrProPack->head.dataLen > 0) {
#if 1
                if (InStrProPack->head.dataLen == sizeof(COMPARE_1T1_RESULT)) {
                    memset(&g_1T1compareResult, 0x00, sizeof(COMPARE_1T1_RESULT));
                    memcpy(&g_1T1compareResult, InStrProPack->data, sizeof(COMPARE_1T1_RESULT));
                    //memcpy(&score, InStrProPack->data+sizeof(COMPARE_DETECT),sizeof(int));
                    g_1T1compareResult.score = htonl(g_1T1compareResult.score);

                    LOGD("g_1T1compareResult.score[%u]", g_1T1compareResult.score);
                    //if(g_1T1compareResult.score/1000000 > g_Score)
                    if (g_1T1compareResult.score / 1000000 > g_LimitScore.score1to1) {
                        g_CompareFlag = 0;
                    }
                    else {
                        g_CompareFlag = -2;
                    }

                } else {
                    g_CompareFlag = -2;
                }

#endif

            }

            break;
        case NMSG_SUBTYPE_ERROR:
            g_CompareFlag = -2;
            LOGD("1T1 NMSG_SUBTYPE_ERROR  InStrProPack->head.dataLen[%d]\r\n",
                 InStrProPack->head.dataLen);

            memcpy(&iret, InStrProPack->data, sizeof(int));
            iret = htonl(iret);
            LOGD(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>iret[%d]\r\n", iret);

            break;

        default: {
            retVal = -1;

            break;
        }
    }

    return retVal;
}

//修改密码返回结果
int DealMsgModifyPasswd(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack,
                        PROTOCOL_PACK *OutStrProPack) {
    int retVal = -1;
    int iret = 0;
    COMPARE_RESULT compareResult;
    int score = 0;

    // 处理该子类型信息
    switch (InStrProPack->head.subType) {
        case NMSG_SUBTYPE_RESPONSE:
            LOGD("modify passwd  NMSG_SUBTYPE_RESPONSE [%d]\r\n", InStrProPack->head.dataLen);
            if (InStrProPack->head.dataLen == 0) {
                g_ModifyPasswdFlag = 1;

            }
            else {
                g_ModifyPasswdFlag = 0;
            }

            break;
        case NMSG_SUBTYPE_ERROR:
            g_ModifyPasswdFlag = 0;

            LOGD(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>g_ModifyPasswdFlag[%d]\r\n", g_ModifyPasswdFlag);

            break;

        default: {
            retVal = -1;

            break;
        }
    }

    return retVal;
}

//注册人脸
int DealMsgRegisterFace(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack,
                        PROTOCOL_PACK *OutStrProPack) {
    int retVal = -1;
    int iret = 0;
    COMPARE_RESULT compareResult;
    int score = 0;

    // 处理该子类型信息
    switch (InStrProPack->head.subType) {
        case NMSG_SUBTYPE_RESPONSE:
            LOGD("modify passwd  NMSG_SUBTYPE_RESPONSE [%d]\r\n", InStrProPack->head.dataLen);
            if (InStrProPack->head.dataLen == 2) {
                g_RegisterFaceFlag = 1;

            }
            else {
                g_RegisterFaceFlag = 0;
            }

            break;
        case NMSG_SUBTYPE_ERROR:
            g_RegisterFaceFlag = 0;

            LOGD(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>g_ModifyPasswdFlag[%d]\r\n", g_ModifyPasswdFlag);

            break;

        default: {
            retVal = -1;

            break;
        }
    }

    return retVal;
}


int DealMsgGetFace(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack,
                   PROTOCOL_PACK *OutStrProPack) {
    int retVal = -1;
    int iret = 0;
    COMPARE_RESULT compareResult;
    int score = 0;
    unsigned int faceLen = 0;
    unsigned int faceId = 0;
    char bufFile[256] = {0};

    // 处理该子类型信息
    switch (InStrProPack->head.subType) {
        case NMSG_SUBTYPE_RESPONSE:
            LOGD("NMSG_SUBTYPE_RESPONSE InStrProPack->head.dataLen[%d]sizeof(COMPARE_RESULT)[%d]\r\n",
                 InStrProPack->head.dataLen, sizeof(COMPARE_RESULT));
            if (InStrProPack->head.dataLen > 0) {
                memcpy(&faceId, InStrProPack->data, sizeof(unsigned int));
                memcpy(&faceLen, InStrProPack->data + 344, sizeof(unsigned int));
                faceId = htonl(faceId);
                faceLen = htonl(faceLen);

                LOGD("79999999999999999999999 InStrProPack->head.dataLen[%d]faceLen[%d]\r\n",
                     InStrProPack->head.dataLen, faceLen);


                if (InStrProPack->head.dataLen - 344 - 4 == faceLen) {

                    sprintf(bufFile, "/data/data/com.firs.facedetecttosvr/temp/%d.jpg", faceId);
                    //保存照片
                    saveFile(bufFile,
                             (unsigned char *) InStrProPack->data + 344 + sizeof(unsigned int),
                             faceLen);
                    g_CompareFlag++;
                    if (g_FaceNum == g_CompareFlag) {
                        LOGD("g_FaceNum[%d] g_CompareFlag[%d]\r\n", g_FaceNum, g_CompareFlag);
                        //LOGD("g_LimitScore.faceNum[%d]",g_LimitScore.faceNum);
                        //实际符合要求的人脸已经接收完毕，结束本次接收人脸模板
                        g_CompareFlag = g_LimitScore.faceNum;
                        g_FaceNum = 0;
                    }
                }
            }

            break;
        case NMSG_SUBTYPE_ERROR:
            //g_CompareFlag = -4;
            LOGD("NMSG_SUBTYPE_ERROR  InStrProPack->head.dataLen[%d]\r\n",
                 InStrProPack->head.dataLen);

            memcpy(&iret, InStrProPack->data, sizeof(int));
            iret = htonl(iret);
            LOGD(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>iret[%d]\r\n", iret);

            break;

        default: {
            retVal = -1;

            break;
        }
    }

    return retVal;
}

/**************************************************************\
** 函数名称： DealHeartbeat
** 功能： 处理心跳包
** 参数： 该函数处理成功返回0，出错则返回负数
** 返回：
** 创建作者： 颜廷军
** 创建日期： 2012-8-22
** 修改作者：
** 修改日期：
\**************************************************************/
int DealHeartbeat(INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack,
                  PROTOCOL_PACK *OutStrProPack) {
    int retVal = -1;

    retVal = TcpSendPack(PROTOCOL_HEAD_FLAG, InStrProPack->head.index, NTYPE_FACECAP_WORK,
                         NMSG_CNT_HEARTBEAT,
                         NMSG_SUBTYPE_RESPONSE, 0, NULL, OutStrProPack);

    return retVal;
}

/**************************************************************\
** 函数名称： DealDoSendHeartbeat
** 功能： 发送心跳
** 参数： 该函数处理成功返回0，出错则返回负数
** 返回：
** 创建作者： 颜廷军
** 创建日期： 2012-8-22
** 修改作者：
** 修改日期：
\**************************************************************/
int DealDoSendHeartbeat(CommLayerTcp *&TcpObj) {
    PROTOCOL_PACK OutStrProPack;
    int ret = 0;
    int ClieFd;

    ClieFd = TcpObj->GetSocketClieFd();

    memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
    OutStrProPack.data = NULL;

    if (TcpSendPack(PROTOCOL_HEAD_FLAG, CHECK_INDEX, NTYPE_FACECAP_WORK, NMSG_CNT_HEARTBEAT,
                    NMSG_SUBTYPE_REQUEST, 0, NULL, &OutStrProPack) < 0) {
        ret = -1;
    }

    //LOGD("NMSG_CNT_HEARTBEAT:[%d]\r\n",ClieFd);

    if (CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false) {
        ret = -1;
    }

    Free(OutStrProPack.data);

    return (ret);
}

/* 发送NAK应答包 */
int SendErrorMsg(unsigned short usPackIndex, int iErrorNo, char *pErrorMsg,
                 PROTOCOL_PACK *OutStrProPack) {
    ERROR_INFO strErrInfo;
    int iMsgLen;
    int iErrLen;

    iErrLen = sizeof(strErrInfo) - sizeof(strErrInfo.errorMsg);

    memset(&strErrInfo, 0, sizeof(ERROR_INFO));
    strErrInfo.errorNo = htonl(iErrorNo);

    if (pErrorMsg != NULL) {
        iMsgLen = strlen(pErrorMsg);
        if (iMsgLen > 255) {
            iMsgLen = 255;
        }

        strncpy(strErrInfo.errorMsg, pErrorMsg, iMsgLen);
        iErrLen += iMsgLen;
    }

    TcpSendPack(PROTOCOL_HEAD_FLAG, usPackIndex, NTYPE_FACECAP_WORK,
                NMSG_CNT_LOGIN, NMSG_SUBTYPE_ERROR, iErrLen, (void *) &strErrInfo, OutStrProPack);

    return 0;
}

int snedPictureToSvr(char *pPicture, int len) {
    //g_AuthFlag = 0;
    int ret = 0;

    int ClieFd = -1;
    PROTOCOL_PACK OutStrProPack;
    char *pTemp = NULL;

    memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
    OutStrProPack.data = NULL;

    ClieFd = giClieFd;

    TCOMPARE picCompare;
    int nLen = sizeof(TCOMPARE);

    memset(&picCompare, 0, sizeof(TCOMPARE));

    if (1 == g_LimitScore.faceNum) {
        picCompare.score = htonl(g_LimitScore.score * 1000000);
    }
    else {
        picCompare.score = htonl(45000000);
    }
    picCompare.maxres = htonl(g_LimitScore.faceNum);
    picCompare.imgtype = htons(0x0001);
    picCompare.imgsize = htonl(len);

    pTemp = (char *) malloc(nLen + len + 1);
    memcpy(pTemp, &picCompare, nLen);
    LOGD("memcpy(pTemp+nLen,pPicture,nLen[%d])sizeof(unsigned short)[%d]picCompare.maxres[%d]",
         nLen, sizeof(unsigned short), picCompare.maxres);
    memcpy(pTemp + nLen, pPicture, len);


    LOGD("login in imgtype[%d] imgsize[%lu] len[%d]\r\n", picCompare.imgtype, picCompare.imgsize,
         len);

    if (TcpSendPack(PROTOCOL_HEAD_FLAG, AUTH_INDEX, NTYPE_FACECAP_WORK, NMSG_FACE_COMPARE,
                    NMSG_SUBTYPE_REQUEST, nLen + len, (void *) pTemp, &OutStrProPack) < 0) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    if (CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    ClientSendAuth_END:

    Free(OutStrProPack.data);

    return ret;
}

int sendPictureToSvr1T1(char *pPicture, int len, char *pPicture1T1, int len1T1) {
    //g_AuthFlag = 0;
    int ret = 0;

    int ClieFd = -1;
    PROTOCOL_PACK OutStrProPack;
    char *pTemp = NULL;
    int iPic1Len = len;
    int iPic2Len = len1T1;
    int datalen = 0;


    memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
    OutStrProPack.data = NULL;

    ClieFd = giClieFd;

    TCOMPARE1T1 picCompare;
    int nLen = sizeof(TCOMPARE1T1);

    memset(&picCompare, 0x00, sizeof(TCOMPARE1T1));

    LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);

    datalen = nLen + sizeof(len) + len + sizeof(len1T1) + len1T1;
    LOGD("datalen[%d] %s %d\r\n", datalen, __FUNCTION__, __LINE__);
    pTemp = (char *) malloc(datalen + 1);
    if (pTemp == NULL) {
        LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);
        return -1;
    }
    iPic1Len = htonl(iPic1Len);
    iPic2Len = htonl(iPic2Len);

    picCompare.score = htonl(g_LimitScore.score1to1 * 1000000);
    picCompare.imgtype = htons(0x0001);
    //picCompare.imgsize  = htonl(len);
    LOGD("len[%d] len1t1[%d]%s %d\r\n", len, len1T1, __FUNCTION__, __LINE__);

    LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);
    memcpy(pTemp, &picCompare, nLen);
    LOGD("memcpy(pTemp+nLen,pPicture,nLen[%d])sizeof(unsigned short)[%d]", nLen,
         sizeof(unsigned short));

    memcpy(pTemp + nLen, &iPic1Len, sizeof(int));
    memcpy(pTemp + nLen + sizeof(int), pPicture, len);

    memcpy(pTemp + nLen + sizeof(int) + len, &iPic2Len, sizeof(int));
    memcpy(pTemp + nLen + sizeof(int) + len + sizeof(int), pPicture1T1, len1T1);


    LOGD("login in imgtype[%d]  len[%d]\r\n", picCompare.imgtype, len);

    if (TcpSendPack(PROTOCOL_HEAD_FLAG, AUTH_INDEX - 1, NTYPE_FACECAP_WORK, NMSG_FACE_COMPARE1T1,
                    NMSG_SUBTYPE_REQUEST, datalen, (void *) pTemp, &OutStrProPack) < 0) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    if (CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    ClientSendAuth_END:

    Free(OutStrProPack.data);

    return ret;
}

int sendRegisterFace(char *pPicture, int len) {
    int ret = 0;
    int ClieFd = -1;
    PROTOCOL_PACK OutStrProPack;
    char *pTemp = NULL;
    int datalen = 0;
    int iPicLen = len;

    memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
    OutStrProPack.data = NULL;

    ClieFd = giClieFd;

    datalen = sizeof(int) + len;

    //memset(&picCompare, 0x00, sizeof(TCOMPARE1T1));

    LOGD("datalen[%d] %s %d\r\n", datalen, __FUNCTION__, __LINE__);
    pTemp = (char *) malloc(datalen + 1);
    if (pTemp == NULL) {
        LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);
        return -1;
    }
    iPicLen = htonl(len);

    LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);
    memcpy(pTemp, &iPicLen, sizeof(int));
    //LOGD("memcpy(pTemp+nLen,pPicture,nLen[%d])sizeof(unsigned short)[%d]",nLen,sizeof(unsigned short));
    memcpy(pTemp + sizeof(int), pPicture, len);

    if (TcpSendPack(PROTOCOL_HEAD_FLAG, AUTH_INDEX - 1, NTYPE_FACECAP_WORK, NMSG_USER_FACE_ADD,
                    NMSG_SUBTYPE_REQUEST, datalen, (void *) pTemp, &OutStrProPack) < 0) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    if (CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    ClientSendAuth_END:

    Free(OutStrProPack.data);

    return ret;
}

int sendGetSvrPicture(unsigned int faceId) {
    //g_AuthFlag = 0;
    int ret = 0;

    int ClieFd = -1;
    PROTOCOL_PACK OutStrProPack;
    char *pTemp = NULL;

    memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
    OutStrProPack.data = NULL;

    ClieFd = giClieFd;

    faceId = htonl(faceId);

    int nLen = sizeof(unsigned int);

    if (TcpSendPack(PROTOCOL_HEAD_FLAG, AUTH_INDEX, NTYPE_FACECAP_WORK, NMSG_FLIB_GET_SUB,
                    NMSG_SUBTYPE_REQUEST, nLen, (void *) &faceId, &OutStrProPack) < 0) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    if (CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    ClientSendAuth_END:

    Free(OutStrProPack.data);

    return ret;
}

COMPARE_RESULT get_CompareResult(int faceNum) {
    //return g_CompareResult;
    if (faceNum < 0 || faceNum >= 10) {
        return g_CmpRst[0];
    }
    return g_CmpRst[faceNum];
}

COMPARE_1T1_RESULT get_1T1CompareResult() {
    return g_1T1compareResult;
}

int sendModifyPasswd(char *pUserName, char *pOldPasswd, char *pNewPasswd) {
    int ret = 0;
    int ClieFd = -1;
    PROTOCOL_PACK OutStrProPack;
    int datalen = 0;
    MODIFY_PASSWD modifyPsw;

    memset(&OutStrProPack, 0, sizeof(PROTOCOL_PACK));
    OutStrProPack.data = NULL;

    ClieFd = giClieFd;
    datalen = sizeof(MODIFY_PASSWD);

    memset(&modifyPsw, 0x00, sizeof(MODIFY_PASSWD));
    memcpy(modifyPsw.userName, pUserName, strlen(pUserName));
    memcpy(modifyPsw.oldPasswd, pOldPasswd, strlen(pOldPasswd));
    memcpy(modifyPsw.newPasswd, pNewPasswd, strlen(pNewPasswd));

    LOGD(" %s %d\r\n", __FUNCTION__, __LINE__);

    if (TcpSendPack(PROTOCOL_HEAD_FLAG, AUTH_INDEX - 1, NTYPE_FACECAP_WORK, NMSG_USER_PASMOD,
                    NMSG_SUBTYPE_REQUEST, datalen, (void *) &modifyPsw, &OutStrProPack) < 0) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    if (CommLayerTcp::PushSendDataQueue(ClieFd, &OutStrProPack) == false) {
        ret = -1;
        goto ClientSendAuth_END;
    }

    ClientSendAuth_END:

    Free(OutStrProPack.data);

    return ret;
}

int getModifyPasswdFlag() {
    return g_ModifyPasswdFlag;
}

void setModifyPasswdFlag(int flag) {
    g_ModifyPasswdFlag = flag;
}

int getRegisterFaceFlag() {
    return g_RegisterFaceFlag;
}

void setRegisterFaceFlag(int flag) {
    g_RegisterFaceFlag = flag;
}

