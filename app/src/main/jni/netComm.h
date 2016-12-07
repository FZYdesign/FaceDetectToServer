#ifndef _NETCOMM_H
#define _NETCOMM_H

#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>

#define _COMM_DEBUG             1

#define RINGS_SETTING_NUM 5
#define FUNC_KEYS_NUM 10
#define FUNC_KEYS_DES_LEN 32
#define AUTO_SLEEP_NUMS 4



#ifndef SOMAXCONN
	#define SOMAXCONN 1024
#endif
	
/***********************网络库一些公共函数模块 begin*****************************/

#define ERROR_LOG_NAME "./error.log"

extern int ReadableTimeout(int iFd, int iSec);

extern bool AllocCharMatrix(int RowNum, int LowNum, char ***OutChar);

extern void FreeCharMatrix(char ***OutChar, int RowNum);

extern bool GetHostAllIp(char ***AllIp, int *OutIpNum);

extern void CleanupMutex(void *arg);

/***********************网络库一些公共函数模块 end*****************************/

/***********************链队列C++模板程序 begin*****************************/
//using namespace std;

template<class T> struct Node_Cpp
{
	T data;
	Node_Cpp<T> *next;
};

template<class T> class LinkQueue_Cpp
{
public:
	LinkQueue_Cpp();
	~LinkQueue_Cpp();
	bool Push(T x);
	void PrintQueue();
	T* Front();
	bool Pop();
	int Size();
	void Clear();

	bool Empty()
	{
		return(front == rear);//(空队的条件)
	}

private:
	Node_Cpp<T> *rear;
	Node_Cpp<T> *front;
};



/**************************************************************\
** 函数名称： LinkQueue_Cpp
** 功能： 模板构造函数
** 参数： 
** 返回： 无
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
template<class T> LinkQueue_Cpp<T>::LinkQueue_Cpp()
{
	front = new Node_Cpp<T>;
	front->next=NULL;
	rear = front;
}

/**************************************************************\
** 函数名称： ~LinkQueue_Cpp
** 功能： 模板析造函数
** 参数： 
** 返回： 无
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
template<class T> LinkQueue_Cpp<T>::~LinkQueue_Cpp()
{
	printf("front %p rear %p\r\n", front, rear);
	if(front != NULL)
	{
		Node_Cpp<T> *ptr;
		ptr = front;
		front = front->next;
		delete ptr;
		ptr = NULL;
	}	
}

/**************************************************************\
** 函数名称： Push
** 功能： 入队列
** 参数： 
** 返回： 无
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
template<class T> bool LinkQueue_Cpp<T>::Push(T x)
{
	Node_Cpp<T> *pt = new Node_Cpp<T>;
	pt->data = x;
	pt->next = NULL;	//注意，一定要这一句，最后一个节点next域为NULL
	rear->next = pt;	//新节点放到最后面
	rear = pt;			//尾巴为新节点

	return(true);
}

/**************************************************************\
** 函数名称： Pop
** 功能： 删除队列头节点
** 参数： 
** 返回： 无
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
template<class T> bool LinkQueue_Cpp<T>::Pop()
{
	Node_Cpp<T> *ptr;

	if(front == rear)
		return false;

	ptr = front;
	front = front->next;

	delete ptr;
	ptr = NULL;

	return true;
}

/**************************************************************\
** 函数名称： Front
** 功能： 返回队列头节点
** 参数： 
** 返回： 无
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
template<class T> T* LinkQueue_Cpp<T>::Front()
{
	if (front == NULL)
	{
		printf("front %p %s %d\r\n", front, __FUNCTION__, __LINE__);
		return NULL;
	}

	if (front->next == NULL)
	{
		printf("front->next %p %s %d\r\n", front->next, __FUNCTION__, __LINE__);
		return NULL;
	}
	
	return (&(front->next->data));
}

/**************************************************************\
** 函数名称： PrintQueue
** 功能： 打印所有数据
** 参数： 
** 返回： 无
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
template<class T> void LinkQueue_Cpp<T>::PrintQueue()
{
	Node_Cpp<T>* ptr;

	if(front->next==NULL)
	{
		return;
	}

	ptr = front->next;
	while(ptr!=NULL)
	{
		//cout<< "ptr->data:" <<ptr->data << endl;
		ptr=ptr->next;
	}
	return;
}

/**************************************************************\
** 函数名称： Size
** 功能： 返回队列大小
** 参数： 
** 返回： 无
** 创建作者： 张祖异
** 创建日期： 2012-5-21
** 修改作者：
** 修改日期：
\**************************************************************/
template<class T> int LinkQueue_Cpp<T>::Size()
{
	Node_Cpp<T> *ptr = front->next;
	int Count=0;
	
	while(ptr)
	{
		ptr = ptr->next;
		Count++;
	}
	return Count;
}

template<class T> void LinkQueue_Cpp<T>::Clear()
{//如果在中T data有new，那么就先delete后才能Clear
	Node_Cpp<T> *ptr;
	
	for(;;)
	{
		ptr = NULL;
		
		if(front == rear)
			break;
	
		ptr = front;
		front = front->next;

		if(ptr != NULL)
		{
			delete ptr;
			ptr = NULL;
		}
	}
	return;
}

/***********************链队列C++模板程序 end*****************************/

//#ifdef WIN32
	#pragma pack(1)
	#define PACK_ALIGN
//#else
//	#define PACK_ALIGN __attribute__((packed))
//#endif



/***********************网络库相关 begin*****************************/
//飞瑞斯人脸识别终端通信协议V2.0.doc/V1.5.doc，针对TCP的网络层库

///////////////////协议相关宏、结构等定义 begin/////////////////
//using namespace std;


typedef int SOCKET;




/* 包类型 */
#define AUTH                	0	
#define GET						1
#define GET_NEXT				2
#define PUT						3
#define PUT_NEXT				4
#define RESPONSE				5
#define TRAP                	6
#define HEARTBEAT				7

/* 子类型 */
/* 连接建立 */
#define AUTH_RANDOM				0
#define AUTH_AUTHENTICATE		1


/* 声音设置 */
/* 网络设置 */
/* 韦根输出设置 */
/* 开门延时设置 */
/* IO设置 */
/* 门铃设置 */
/* 定时响铃设置 */
/* 设备同步设置 */
/* 考勤间隔设置 */
/* 功能键设置 */
/* 语言设置 */
/* 外壳锁告警设置 */
/* 自动休眠设置 */
/* 时间显示格式设置 */
/* 识别门限设置 */
/* 安全密码设置 */
/* 屏幕校准设置 */
/* 人体感应设置 */
/* 恢复出厂默认设置 */
/* 系统信息 */
/* 存储信息 */
/* 系统重启 */
/* 设置定时响铃持续次数 */


/* 用户管理 */
/* 用户或管理员管理 */
/* 获取用户或管理员信息 */
/* 获取用户或管理员模板 */
/* 下发用户或管理员模板 */
/* 一对一识别用户 */
#define USER_VERIFY_USER		64
/* 登记人脸 */
#define USER_ENROLL_FACE		65


#define ACK					0
#define NAK					1


#define USER_NAME_LEN		16
#define MD5_CHECK_CODE_LEN	16


#define FACETCP_SERVER_PORT		32108         //服务器默认端口
#define FACETCP_VERSION		    0x02000000    //通信协议版本号

// 数据包类型(网络连接类型)
#define NTYPE_FACECAP_WORK      0x0001        //网络连接类型:手机人脸抓拍设备

// 数据包消息类型(协议号)
#define NMSG_CNT_LOGIN		    0x00000100    //登录
#define NMSG_CNT_HEARTBEAT	    0x00000200	  //心跳			
#define NMSG_FACE_COMPARE		0x00020100	  //图片识别比对
#define NMSG_FACE_COMPARE1T1	0x00020101	  //1比1图片识别比对
#define NMSG_FLIB_GET_SUB		0x00010401	  //获取人脸模板照片
#define NMSG_USER_PASMOD		0x10010600    //修改密码
#define NMSG_USER_FACE_ADD		0x10010901	  //添加注册人脸
#define NMSG_CNT_FACELOGIN		0x00000102	  //人脸登录


// 数据包消息子类型(请求/应答包的子类型)
#define NMSG_SUBTYPE_REQUEST    0x0001       //请求
#define NMSG_SUBTYPE_RESPONSE   0x0002       //应答
#define NMSG_SUBTYPE_ERROR      0x0003       //错误


/* 报文头标志 */
#define PROTOCOL_HEAD_FLAG	0x7E
#define PROTOCOL_TAIL_FLAG	0x7F


#define AUTH_INDEX 			65000
#define UPLOAD_INDEX 		65001
#define CHECK_INDEX 		65002
#define NEEDRECONECT 		65003
#define TRAP_ADD_USER 		65004
#define FACE_HEARTBEAT     	0

#define MAX_CONN_NUM 		5	/* 服务端支持的最大连接数 */
#define MAX_SEND_THREADS 	1	/* 最大发送线程 */
#define RECV_TIME_OUT		2

extern int giServerFd[MAX_CONN_NUM];

typedef struct PROTOCOL_HEAD
{ 
	unsigned char	flag;			// 包标识	(固定为0x7E)
	unsigned int	index;			// 序号 
	unsigned short 	pack;			// 数据包的分包信息（位结构:15~8分包数量;7~0分包序号；x0000 表示未分包）
	unsigned short	packType;		// 包类型	
	unsigned int	msgtype;		// 数据包消息类型(协议号)
	unsigned short	subType;		// 子类型	
	unsigned int	dataLen;		// 数据长度 

} PACK_ALIGN PROTOCOL_HEAD,*LPPROTOCOL_HEAD;


typedef struct PROTOCOL_PACK
{ 
	PROTOCOL_HEAD 	head;	    // 数据头	
	char			*data;		// 数据内容 
	unsigned short  chksum;		 // 数据包校验和(从为包序号开始到数据体的字节数据累加)
	unsigned char	csuffix;	// 数据包后缀（固定为0x7F，数据包的传输时，需加入后缀作为包结束标志）
}PACK_ALIGN PROTOCOL_PACK,*LPPROTOCOL_PACK;

//数据包消息类型(协议号):NMSG_CNT_LOGIN
//数据包消息子类型(请求/应答包的子类型):NMSG_SUBTYPE_REQUEST)
typedef struct _LOGIN
{
    unsigned int      ver;      //通信协议版本号
    char       user[32]; //登录用户名
    char       pass[32]; //登录口令
    char	   mac[24]; //mac地址
} LOGIN;

//
//人脸登录数据包
typedef struct _FACE_LOGIN
{
    unsigned int      ver;      //通信协议版本号
    char       user[32]; //登录用户名
    int       faceLen; //人脸照片大小
} FACE_LOGIN;
//修改密码结构数据包
typedef struct _MODIFY_PASSWD
{
	char       userName[32]; //登录用户名
	char       oldPasswd[32]; //旧密码
	char       newPasswd[32]; //新密码
} MODIFY_PASSWD;


//用户登陆成功,服务器返回该用户的详细信息（包含权限）
//数据包消息类型(协议号):NMSG_CNT_LOGIN
//数据包消息子类型(请求/应答包的子类型): NMSG_SUBTYPE_RESPONSE)
typedef struct _LOGIN_RESP
{
    unsigned int      id;           //用户标识ID
    char       user[32];     //用户名
    char       pass[32];     //口令
    unsigned int      usertype;     //用户类型
    char       RuleList[512];//用户权限(使用","分隔的权限标识ID的字符串。)
} LOGIN_RESP;

//用户登陆失败,服务器返回错误信息（包含错误码）
//数据包消息类型(协议号):NMSG_CNT_LOGIN
//数据包消息子类型(请求/应答包的子类型): NMSG_SUBTYPE_ERROR)
typedef struct _LOGIN_ERR
{
    unsigned int      err;      //错误码(1:用户名或密码错误)
} LOGIN_ERR;


//图片比对
typedef struct _COMPARE
{
    unsigned int      score;      //比对门限分值
    unsigned int      maxres;     //比对命中返回最大数量(默认为1最大为10)
    unsigned short    imgtype;    //图片类型(当前仅支持JPEG 压缩的图片数据，取值0x0001 )
    unsigned int      imgsize;    //图片数据长度
}TCOMPARE;

typedef struct _COMPARE_DETECT//人脸检测信息
{
    int      fdet_l;          //人脸特征区域左
    int      fdet_t;          //人脸特征区域上
    int      fdet_r;          //人脸特征区域右
    int      fdet_b;          //人脸特征区域下
    int      fdet_quality;    //人脸图片的图像质量
    char       reserved[64];    //保留字段
}COMPARE_DETECT;

typedef struct _COMPARE_RESULT //比对结果
{
    unsigned int      score;          //比对的识别分值
    unsigned int      pid;            //人物id
    unsigned int      f_id;           //人物的人脸模板id(注意一个人物可以有多个人脸模板)
    char      		  f_name[64];     //名称
    unsigned short    f_type;         //类型(未知:0x0000 黑名单:0x0001 员工:0x0100 驾驶员:0x0101)
    unsigned short	  f_sst;          //敏感等级--暂时保留未定义默认填
    char      		  f_sex;          //性别(未知:0x00 男:0x01 女:0x02)
    char     		  f_age;          //年龄(5-127)
    char      		  f_remarks[256]; //备注信息
    char       		  reserved[64];   //保留字段
}COMPARE_RESULT;

typedef struct _RECT_FACE
	{
		int lx;
		int ly;
		int rx;
		int ry;
	}RECT_FACE;

typedef struct _COMPARE_1T1_RESULT //1:1 比对结果
{
	char			imgExter[32];
    int     		score;          //比对的识别分值
    char       		reserved[64];   //保留字段
    _RECT_FACE      rect1;
	int 			quarity1;
	_RECT_FACE      rect2;
	int 			quarity2;
}COMPARE_1T1_RESULT;



//图片比对
typedef struct _COMPARE1T1
{
			char	  imgExter[32];
    		int       score;      //比对门限分值
    unsigned short    imgtype;    //图片类型(当前仅支持JPEG 压缩的图片数据，取值0x0001 )
    unsigned char     reserve[64];    //预留64字节
}TCOMPARE1T1;
//....发送比对请求是加图片1长度、图片1数据、图片2长度、图片2数据


#if 1
typedef struct RECORDS_GET
{
	//RECORD_SET_S* pRecords;		/* 数据库数据的记录集 */
	char *pFeatures;
	int nDateLen;
	int nFieldNum;                  /* pRecords 记录的有效字段数 */
	int nPackType;					/* 包类型 */
	int nSubType;					/* 子类型 */
	int nMaxRecords;			    /* 数据库中满足条件的最大记录数 */
	int nCurRecords;			    /* 当前已经读取的记录数 */
	int nNo;					    /* 当前要发送的记录序号 */
	int nUserNo;				    /* 用户编号 */
	char logType;                   /* 日志类型 */
	char logLevel;                  /* 日志级别 */
	//char sql[1024];	
    unsigned int searchConditionFlag;
    char photoFlag;         		/* 是否传输记录照片标记 */
} RECORDS_GET, *LPRECORDS_GET;

#endif

typedef struct RECORDS_PUT
{
	int nPackType;				/* 包类型 */
	int nSubType;				/* 子类型 */
	int nNo;					/* 当前要发送的记录序号 */	
	int nUserNo;				/* 用户编号 */
	unsigned int ulFileSize;  	/* 传输文件大小*/	
	unsigned int ulRecivedSize;   /* 已经接受到数据大小*/	
} RECORDS_PUT, *LPRECORDS_PUT;

#if 0

/*
**  发送日志信息结构
*/
typedef struct LOG_INFOM
{
	char logTime[32];			/* 日志时间 */
	char logType;				/* 日志类型 */
	char logLevel;			/* 日志级别 */
	char logTitle[16];			/* 日志标题 */
	char logLen;				/* 描述长度 */
	char logDesc[1];			/* 描述 */
	
} PACK_ALIGN LOG_INFOM, *LPLOG_INFOM;

typedef enum {
	PARAM_ERROR,			/* 参数错误 */
	MSG_SUBTYPE_ERR,		/* 消息子类型错误 */
	DEL_RECORD_ERR,			/* 删除记录失败 */
	NO_SUCH_USER,			/* 用户不存在 */
	TIME_SET_FAIL,			/* 设置时间失败 */
	GET_USER_INFO_FAIL,		/* 获取用户信息失败 */
	RECOGNIZE_FACE_FAIL,	/* 人脸识别失败 */
	CLIENT_QUIT,			/* 客户端掉线 */
	GET_USER_TMPL_FAIL,		/* 获取用户模板失败 */
	ENROLL_FACE_FAIL,		/* 登记人脸失败 */
	CARD_ALREADY_EXIST,		/* 卡号已注册 */
	DATA_FORMAT_ERROR       /* 消息子类型数据格式错误*/
} ERROR_NO;

typedef struct ENROLL_USER{
	unsigned long	userid;	/* 用户编号长度（4字节） */
	char			sign;	/* 保存用户标识（1字节） */
	char            cType;	/* 用户类型（1字节），0-普通用户，1-管理员 */
} PACK_ALIGN ENROLL_USER,LPENROLL_USER;

typedef struct VERIFY_INFO
{
	unsigned int	nUserNo;
	char            nStatus;
	char	        nThreshold;
	char		    nPhototype;
	int             nPhotoLen;
	char            photo[1];
}PACK_ALIGN VERIFY_INFO, *LPVERIFY_INFO;


typedef struct PACK_INFO
{
	char  bRemovable;
	int   sockID;
	int   nPackLen;
	char  *pPack;
	struct  PACK_INFO* next;
}PACK_ALIGN PACK_INFO, *LPPACK_INFO;

typedef struct PACK_LIST{
	int   curNum;
	LPPACK_INFO pPackList;
}PACK_ALIGN PACK_LIST, * LPPACK_LIST;

#endif

typedef struct
{
	int randFlag;		/* 每个连接内的随机数标志 */
	int randNum;		/* 每个连接内的随机数数目 */
	int authFlag;		/* 每个连接内的认证标志 */

	RECORDS_GET recordsGet;
	RECORDS_PUT recordsPut;
}PACK_ALIGN INLINK_SHARE, *LPINLINK_SHARE;

#if 0
typedef struct AUTH_RAND
{
	int rand;
} PACK_ALIGN AUTH_RAND,*LPAUTH_RAND;

typedef struct
{
	unsigned char ucMute;	/* 按键音开关 */
	unsigned char ucMuteVoice;	/* 语音开关 */
	unsigned char iVolSpeaker;	/* 音量级别 */
} PACK_ALIGN AUDIO_SET,*LPAUDIO_SET;

typedef struct
{
	char caIp[16];			/* 终端IP */
	char caNetmask[16];		/* 子网掩码 */
	char caGateway[16];		/* 网关 */
	char caDns[16];			/* DNS服务器IP */
} PACK_ALIGN NETWORK_SET,*LPNETWORK_SET;

typedef struct
{
	unsigned char caWgEvenNum;			/* 偶校验位数 */
	unsigned char caWgOemCode;			/* OEM码 */
	unsigned char caWgSectionCode;		/* 区码 */
	unsigned char caWgUserCode;			/* 用户码 */
	unsigned char caWgOddNum;			/* 奇校验位数 */
} PACK_ALIGN WIEGANDIN_SET,*LPWIEGANDIN_SET;

typedef struct
{
	char cWiegandOutMode;		/* 韦根输出模式选择,0：韦根26; 1：韦根34 */
	char cOutPutFormat;         /* 韦根输出内容，0-工号，1-卡号 */
	unsigned short usAreaCode;  /* 区码 韦根26 <= 254 韦根34 <= 65534 */
} PACK_ALIGN WIEGANDOUT_SET,*LPWIEGANDOUT_SET;

typedef struct
{
	unsigned char ucDelayTime;			/* 开门延时 */
} PACK_ALIGN DOORDELAY_SET,*LPDOORDELAY_SET;

typedef struct
{
	unsigned char ucMGInput;        /* 开关门磁输入，0 - 关闭，1 - 开启 */
	unsigned char ucMGDelayTime;	/* 门磁延时 */
	unsigned char ucFireInput;      /* 消防输入，0 - 关闭，1 - 开启 */
	unsigned char ucIODoorOutPut;   /* 开关量输出，0 - 关闭，1 - 开启 */
    unsigned char ucAudioOutPut;    /* 报警输出，0 - 关闭，1 - 开启 */
} PACK_ALIGN IOCONFIG_SET,*LPIOCONFIG_SET;

typedef struct
{
	char cDoorbell;						/* 门铃 */
} PACK_ALIGN BELL_SET,*LPBELL_SET;

typedef struct
{
    unsigned char ucRingHour;     /* 小时 */
    unsigned char ucRingMinute;   /* 分钟 */
	unsigned char ucRingIndex;	  /* 序号 */
	unsigned char ucRingSwitch;   /* 开关 */
} PACK_ALIGN RING_SET,*LPRING_SET;

typedef struct
{
	RING_SET stRingsSetting[RINGS_SETTING_NUM];
} PACK_ALIGN RINGS_SET,*LPRINGS_SET;

typedef struct
{
    unsigned char ucSyncMode;       /* 同步方式，0 - 手动，1 - 自动 */
} PACK_ALIGN DEVICE_SYNC_SET,*LPDEVICE_SYNC_SET;

typedef struct
{
	unsigned short usAttendInterval;    /* 考勤间隔 0 - 考勤时间间隔不生效，1 ~ 300，考勤时间间隔，单位为分钟 */
} PACK_ALIGN ATTEND_INTERVAL_SET,*LPATTEND_INTERVAL_SET;

typedef struct
{
    unsigned char ucEnable;         /* 功能使能 */
	char caKeysScription[FUNC_KEYS_NUM][FUNC_KEYS_DES_LEN+1];
} PACK_ALIGN FUNCTION_KEYS_SET,*LPFUNCTION_KEYS_SET;

typedef struct
{
	unsigned char ucLanguage;           /* 语言 1-english, 2-简体中文，3-繁体中文 */
} PACK_ALIGN LANGUAGE_SET,*LPLANGUAGE_SET;

typedef struct
{
	 unsigned char ucAlarmShell;         /* 外壳拆卸报警*/
} PACK_ALIGN LOCK_SHELL_ALARM_SET,*LPLOCK_SHELL_ALARM_SET;

typedef struct
{
	unsigned char ucOffTimeHour;
	unsigned char ucOffTimeMinute;
	unsigned char ucOnTimeHour;
	unsigned char ucOnTimeMinute;
} PACK_ALIGN SINGLE_AUTO_SLEEP_SET,*LPSINGLE_AUTO_SLEEP_SET;

typedef struct
{
	SINGLE_AUTO_SLEEP_SET strAutoSleepCfg[AUTO_SLEEP_NUMS];
} PACK_ALIGN AUTO_SLEEP_SET,*LPAUTO_SLEEP_SET;

typedef struct
{
	unsigned char ucStatus;     /* 该屏保使能状态，0 - 禁止，1 - 开启 */
} PACK_ALIGN SCREEN_SAVER_SET,*LPSCREEN_SAVER_SET;

typedef struct
{
	unsigned char ucTimeMode;       /* 时间显示方式，0.YY-MM-DD, 1.YY/MM/DD, 2.MM-DD-YY, 3.MM/DD/YY, 4.DD-MM-YY, 5.DD/MM/YY */
} PACK_ALIGN SHOW_TIME_MODE_SET,*LPSHOW_TIME_MODE_SET;

typedef struct
{
	unsigned char ucRecognise;      /* 识别门限 */
	unsigned char ucUpdate;         /* 更新门限 */
    unsigned char ucVerifyThreshold;/* 验证门限 */
} PACK_ALIGN RECOG_THRESHOLD_SET,*LPRECOG_THRESHOLD_SET;

typedef struct
{
	char cNumPre;			/* 号码前缀*/
	char caWebPwd[8];       /* 网页登录密码 */
	char caSdPwd[8];        /* SD卡密码 */
	char caUnlockPwd[8];    /*开门密码*/   //modify defect 72 2012-10-16  add caUnlockPwd
} PACK_ALIGN SAFE_CONFIG_SET,*LPSAFE_CONFIG_SET;

typedef struct
{
	char cSenseThreshold;              /* 人体感应级别 */
} PACK_ALIGN HUMAN_SENSE_SET,*LPHUMAN_SENSE_SET;

typedef struct
{
	char caProductID[16];
	char caSN[16];
	char caSoftwareVer[16];
	char caHardwareVer[16];
	char caUbootVer[16];
	char caKernelVer[16];
	char caProtocolVer[16];
	char caArithmeticVer[16];
} PACK_ALIGN DEVICE_INFO_GET,*LPDEVICE_INFO_GET;

typedef struct
{
	int iCurUserNum;
	int iLimitUserNum;
	int iCurRecordNum;
	int iLimitRecordNum;
	int iCurFeatureNum;
	int iLimitFeatureNum;
	int iCurLogNum;
	int iLimitLogNum;
} PACK_ALIGN STORAGE_INFO_GET,*LPSTORAGE_INFO_GET;	

#endif

///////////////////协议相关宏、结构等定义 end/////////////////

///////////////////网络核心部分 begin/////////////////
enum SERVER_OR_CLIENT
{
	TCP_CLIENT,
	TCP_SERVER
};

/* 发送数据结构 */
typedef struct //SendData
{
	int iFd;			/* 发送socket描述符或accept描述符 */
	int iSendBufLen;	/* 发送数据长度 */
	char *pSendBuf;	/* 发送数据 */
}PACK_ALIGN StrTcpSendData;

typedef struct //TCP_BUF
{
	unsigned short usFlag;		/* 包标识	*/
	unsigned short usIndex;		/* 序号 */
	char cPackType;				/* 包类型	*/
	char cSubType;			/* 子类型	*/
	
	int iFd;					/* 发送socket描述符或accept描述符 */
	
	int iTcpBufLen;				/* 数据长度 */
	char *pTcpBuf;				/* 数据 */
}PACK_ALIGN PROTOCOL_PACK111;


typedef int ( *TREAD_DOING )(int acceptFd);

class CommLayerTcp
{
private:
	int iServPort;				/* 终端作为服务器端，侦听的端口 */
	char acServIp[16];			/* 终端作为服务器端，accept的IP */
	int iCliePort;				/* 终端作为客户端，连接的服务器端口 */
	char acClieIp[16];			/* 终端作为客户端，连接的服务器IP */
	int iSocketServFd;			/* 服务端SOCKET返回 */
	int iSocketClieFd;			/* 客户端SOCKET返回 */
	int iAllFd[MAX_CONN_NUM];	/* 用来保存所有ACCEPT返回或作为客户端的socket返回 */
	int iCurrConnNum;			/* 当前连接数目 */
	int iClientAuthSucc;		/* 终端作为客户端，连接认证成功 0-不成功，1-成功 */

	pthread_mutex_t AllFdMutex;					/* iAllFd的互斥锁 */
	pthread_mutex_t RecvThreadIDMutex;			/* RecvThreadId的互斥锁 */

	static unsigned short DataIndex;

	pthread_t SendThreadId[MAX_SEND_THREADS];
	pthread_t RecvThreadId[MAX_CONN_NUM];

	pthread_t ClientSendHeartbeatThreadId;

public:

	static LinkQueue_Cpp<StrTcpSendData> SendDataQueue;	/* 发送数据队列 */
	static pthread_mutex_t SendDataQueueMutex;	/* 发送队列互斥锁 */
	static pthread_cond_t SendDataQueueCond;	/* 发送队列条件变量 */
	static int iInstance;

	CommLayerTcp();

	CommLayerTcp(const CommLayerTcp &orig);

	CommLayerTcp &operator = (const CommLayerTcp &);

	~CommLayerTcp();

	void CommLayerResetIpAndPort(const SERVER_OR_CLIENT InModel, const char *Ip, const int Port);
	
	int CommLayerTcpInit(const SERVER_OR_CLIENT InModel, const char *Ip, const int Port);

	void CommLayerTcpDestroy();

	bool CreateTcpServ();

	void CreateTcpAccept();

	bool CreateTcpClient();

	int ReadableTimeout(int iFd, int iSec);

	int WriteN(int iFd, const void *vpBuf, int iBufLen);

	int ReadN(int iFd, int iTimeOut, void *vpBuf, int iBufLen);

	void DoTcpSendThreadPool(pthread_t InThreadId);

	int GetClientAuthSucc();

	void SetClientAuthSucc(int flag);

	int GetSocketServFd();
	
	int GetSocketClieFd();

	void SetSocketClieFd(int InFd);

	void SetSendThreadId(pthread_t ThreadId, int i);

	void GetSendThreadId(pthread_t *OutThreadId, int i);

	void SetRecvThreadId(pthread_t ThreadId, int i);

	void GetRecvThreadId(pthread_t *OutThreadId, int i);

	pthread_t GetClientSendHeartbeatThreadId();

	void SetClientSendHeartbeatThreadId(pthread_t Inid);

	void CloseSocketClieFd();

	int GetCurrConnNum();

	bool AddAllFd(int InFd);

	bool SubAllFd(int InFd);

	bool AddRecvThreadID(pthread_t rThreadid);

	bool SubRecvThreadID(pthread_t rThreadid);


	bool SubAllCliFd();

	static bool PushSendDataQueue(int iFd, PROTOCOL_PACK *InStrProPack);

};

typedef struct
{
	CommLayerTcp *Obj;
	int iFd;
}StrLayerTcpTmp;

extern void *TcpSendThreadPool(void *arg);

extern void *TcpServSubThread(void *arg);

extern void *DoingAllForTcpServerThread(void *arg);

extern void *ClientSendHeartbeatThread(void *arg);

/********************************************
*********************************************
********************************************/

///////////////////网络核心部分 end/////////////////

///////////////////业务相关的处理函数 begin/////////////////
extern int DoTcpServer(CommLayerTcp *&TcpObj, int InFd);

extern int DoTcpClient(CommLayerTcp *&TcpObj);

extern int AllDealTcp(int SockFd, INLINK_SHARE *StrInlinkShare, PROTOCOL_PACK *InStrProPack, PROTOCOL_PACK *OutStrProPack);

extern int DealDoSendHeartbeat(CommLayerTcp *&TcpObj);

extern int CheckFeatureFile(char *pFileName, int *pnFeatureNum, int *pnFeatureSize);

extern int GetTcpFeatureData(char *pInFeature[], int InFeatureNum, int InFeatureSize, 
	char **OutFeatures , int *OutFeaturesLen);

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
extern int TcpSendPack(unsigned short flag, unsigned short index,
			unsigned short packType,unsigned int msgtype, unsigned short subType, unsigned int dataLen, void *data, PROTOCOL_PACK *OutStrProPack);

///////////////////业务相关的处理函数 end/////////////////


int SendErrorMsg(unsigned short usPackIndex, int iErrorNo, char * pErrorMsg, PROTOCOL_PACK *OutStrProPack);


/***********************网络库相关 end*****************************/

#ifdef WIN32
    #pragma pack()
#endif
	#undef PACK_ALIGN

#endif  /* _NETCOMM_H */
