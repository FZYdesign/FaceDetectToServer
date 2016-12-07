#ifndef __USER_OUT_LIB_H__
#define __USER_OUT_LIB_H__
#include <pthread.h>

#ifdef __cplusplus 
extern "C" { 
#endif

#ifdef WIN32
#define PACK_ALIGN
#else
#define PACK_ALIGN __attribute__((packed))
#endif

#define TRACE  printf
#define Free   free
#define Malloc  malloc

#define MAX_SCREEN_NUM 8
#define eWEEKDAY_NUM = 7

extern int iThreadExit;
/*
**  发送出错信息结构
*/
typedef struct ERROR_INFO
{
	int errorNo;			       /* 错误代码 */
	unsigned char errorLen;	/* 错误信息长度 */
	char errorMsg[256];		/* 错误信息描述 */
	
} PACK_ALIGN ERROR_INFO, *LPERROR_INFO;


/* 网络配置 */
typedef struct ConfigNetWork 
{
    char cNetType;              /* 网络类型 0-DHCP, 1-STATIC, 2-ADSL */
    char caIp[16];              /* 终端自身的IP地址 */
    char caNetMask[16];         /* 子网掩码 */
    char caGateway[16];         /* 网关 */
    char caDns[16];				/* DNS 服务器 */
	char caDns2[16]; 			
    char caMac[6];              /* MAC地址 */
    char caNtpServer[16];       /* 非空时与此SNTP 服务器同步时间 */
    char cNtpServerMode;        /* NTP 服务模式: 0-禁用，1-服务端，2-客户端，3-服务端+客户端 */
    char caServerIP[16];		/* 终端作为客户端，连接的服务器IP */
    int  iPort;					/* 终端作为客户端，连接的服务器端口 */
} CONFIG_NETWORK_STR;

typedef struct ConfigSecurity 
{
	char caWifiPwd[32];    	 /* wifi密码 */
	
	char caUnlockPwd[32];    /* 开门密码 */
	char caSdPwd[32];        /* SD卡密码 */
    char caWebUser[32];      /* 网页登录用户名 */
    char caWebPwd[32];       /* 网页登录密码 */
	unsigned char cKeyboardOrder;    /* 数字键排序 0-正常，1-乱序 */
}CONFIG_SECURITY_STR;

#if 0

#define CONFIG_BASE_PATH        "config/configBase.xml"
#define CONFIG_NETWORK_PATH     "config/configNetWork.xml"
#define CONFIG_THRESHOLD_PATH   "config/configThreshold.xml"
#define CONFIG_SECURITY_PATH    "config/configSecurity.xml"
#define CONFIG_ATTENDANCE_PATH  "config/configAttendance.xml"
#define CONFIG_OTHERS_PATH      "config/configOthers.xml"
#define CONFIG_AUDIO_PATH       "config/configAudIo.xml"
#define CONFIG_VIDEO_PATH       "config/configVideo.xml"
#define CONFIG_LOGINREASON_PATH "config/configLoginReason.xml"
#define CONFIG_GROUPATTR_PATH   "config/configGroupAttr.xml"
#define CONFIG_AUTOONOFF_PATH   "config/configAutoOnOff.xml"
#define CONFIG_RING_PATH        "config/configRing.xml"
#define CONFIG_HOLIDAY_PATH     "config/configHoliday.xml"
#define CONFIG_WIEGAND_PATH     "config/configWiegand.xml"
#define CONFIG_SYNC_PATH		"config/configSync.xml"

/* 系统配置 */
typedef struct ConfigSys
{ 
	short sRecogRectX, sRecogRectY;		/* 识别框校准坐标 */
} CONFIG_SYS_STR;

/* 基本配置 */
typedef struct ConfigBase
{ 
    unsigned short usAlgorithmVersion;  /* 算法版本 */
    unsigned char ucLanguage;           /* 语言 1-english, 2-简体中文，3-繁体中文 */
    unsigned char ucTz;                 /* 时区 */
    unsigned char ucYear, ucMonth, ucDay, ucHour, ucMinute, ucSecond;   /* 系统时间 */
    short sTouchX, sTouchY, sTouchWidth, sTouchHeight;  /* 触摸屏校准坐标 */
    unsigned char ucTouchFlip;          /* 屏幕翻转  0-normal, 1-flip */
    short sVideoOffsetX, sVideoOffsetY, sVideoZoom; /* 彩色镜头校准:  彩色图象需要的偏移及放大倍数( 倍数扩大100 倍转成定点) */
    int iAvg_x;                         /* 黑白图像与彩色图像X坐标差 */
    int iAvg_y;                         /* 黑白图像与彩色图像Y坐标差 */
    char cWiegandOut;                   /* 韦根输出的状态  0：不存在  1：韦根26，2: 韦根34 */
	char cDoorbell;						/* 门铃 */
    unsigned short usWiegandHID;        /* 韦根输出的HID码 */
    unsigned short usAttendInterval;    /* 考勤间隔 0 - 考勤时间间隔不生效，1 ~ 300，考勤时间间隔，单位为分钟 */
	short sRecogRectX, sRecogRectY;		/* 识别框校准坐标 */
	time_t  strLastGetUserRecords;      /* 最后同步的用户记录时间 */
} CONFIG_BASE_STR;



typedef struct ConfigThreshold 
{
	unsigned char ucRecognise;      /* 识别门限 */
	unsigned char ucUpdate;         /* 更新门限 */
    unsigned char ucVerifyThreshold;/* 验证门限 */  
	unsigned char ucTemplateNum;    /* 模板数量 */
	unsigned char ucTemplateVerify; /* 检测时段 0x01-注册时启用，0x02-识别时启用*/
	unsigned char ucDiffThreshold;  /* 防止同人注册门限 */
	unsigned char ucSecondAverage;	/* 第二识别门限 */
	unsigned char ucSecondMaxNum;	/* 第二识别门限次数 */		
}CONFIG_THRESHOLD_STR;
    


typedef struct ConfigAttendance 
{
    int iRecordsPerPage;        /* 每页显示的记录数 */
    int iTimeDay;               /* 日期分隔时间点 ，此时间之前视为头一天，否则视为当天 */
    int iTimeLate;              /* 上班晚到超过此时间视为未打卡 */
    int iTimeEarlyLeave;        /* 下班早退超过此时间视为未打卡 */
    int iTimeUp1;               /* 上午上班时间 */
    int iTimeDown1;             /* 上午下班时间 */
    int iTimeUp2;               /* 下午上班时间 */
    int iTimeDown2;             /* 下午下班时间 */
    int iTimeUp3;               /* 晚上上班时间 */
    int iTimeDown3;             /* 晚上下班时间 */
    int iTimeUp4;               /* 保留 */
    int iTimeDown4;             /* 保留 */
    int iTimeOverTime;          /* 此时间之后下班才视为加班 */
    int iTimeLateGrace;       /* 迟到宽限分钟数 */
    int iTimeEarlyLeaveGrace; /* 早退宽限分钟数 */
    int iTimeNoon;              /* 中午分隔点，区分上午下班还是下午上班 */
    int iTimeEvening;           /* 晚上分隔点，区分下午下班还是晚上上班 */
}CONFIG_ATTENDANCE_STR;

typedef struct ConfigScreen
{
    unsigned char ucStatus;     /* 该屏保使能状态，0 - 禁止，1 - 开启 */		
    char caPicPath[256];        /* 屏保图片路径 */
}CONFIG_SCREEN_STR ;

typedef struct ConfigOthers
{
    unsigned char ucRecogMode;          /* 全局识别模式，0,一对一;1,一对多;2,刷卡 */
    unsigned char ucSense;              /* 人体感应 */
    unsigned char ucDoorMode;           /* 韦根输出格式，1, WG26; 2, WG34 */
    char caDoorCommand[16];             /* 开门命令协议 */ 
    int iCommLen;                       /* 命令长度 */
    unsigned char ucAlarmShell;         /* 外壳拆卸报警，0-关，1-开 */
    time_t  strLastGetAccessRecords;    /* 最后获取考勤记录时间 */
    unsigned short usaRs485ControlCode[4];  /* 485对码代码 */  
    unsigned char ucAccessCode;         /* 访问方式  0, WGCODE; 1, CARD*/
    int iDoorTime;                      /* 485开门延时 */
    unsigned short usaRs485ControlCodeEx[2];
    int iIoDoorTime;                    /* 开门持续时间: 单位秒，范围 1-15 */
    char cFaceCatch;                    /* 人脸抓拍选项*/
    char cSenseThreshold;               /* 感应灵敏级别 */  
    unsigned char ucMGInput;        /* 开关门磁输入，0 - 关闭，1 - 开启 */	
    unsigned char ucMGDelayTime;    /* 门磁告警延迟时间: 单位秒，范围 1-99 */   
    unsigned char ucFireInput;      /* 消防输入，0 - 关闭，1 - 开启 */	
    unsigned char ucIODoorOutPut;   /* 开关量输出，0 - 关闭，1 - 开启 */
    unsigned char ucAudioOutPut;    /* 报警输出，0 - 关闭，1 - 开启 */
    unsigned char ucSyncMode;       /* 同步方式，0 - 手动，1 - 自动 */
    unsigned char ucTimeMode;       /* 时间显示方式，
                                        0.YY-MM-DD, 1.YY/MM/DD, 2.MM-DD-YY
                                        3.MM/DD/YY, 4.DD-MM-YY, 5.DD/MM/YY */
    struct ConfigScreen Screensaver[MAX_SCREEN_NUM];     /* 屏保图片路径 */
    unsigned char ucCurScreensaverIndex;    /* 当前保用屏保下标 */

	char caPhoneNum[12];					/* 手机号码 */
} CONFIG_OTHERS_STR;

typedef struct ConfigAudIo 
{
    int iVolMic;                        /* 麦音量 */
    int iVolSpeaker;                    /* 声音音量 静音 0 低 1 中 2 高 3 */
    unsigned char ucMute;               /* 按键静音标志，0-发声，1-静音 */
    unsigned char ucMuteVoice;          /* 语音静音标志，0-发声，1-静音 */
    unsigned char ucVoiceLang;          /* 发音语言 0-english, 1-简体中文，2-繁体中文(hk), 2-繁体中文(tw)*/
}CONFIG_AUDIO_STR;

typedef	struct ConfigVideo 
{
    int iaBright[4];  /* 黑白摄像头参数 */
    int iaContrast[4];
    int iaHue[4];
    int iaExposure[4]; 
} CONFIG_VIDEO_STR;

typedef struct ConfigReason 
{
	unsigned char ucEnable;
	char caScription[32+1];
}CONFIG_REASON_STR;

typedef	struct ConfigLoginReason 
{
    unsigned char ucActive;         /* 功能使能 */
    unsigned char ucRecogMode[10]; /* 10个功能键对应的识别模式，0,一对一;1,一对多;2, 刷卡 */
    struct ConfigReason Reason[10];
}CONFIG_LOGINREASON_STR;

typedef	struct ConfigGroupAttr
{
    int iGroupId;        /* 组ID */
    int iNumPrefix;     
    char caEquipPwd[32+1];
    unsigned char ucAttr; /*normal--0,disable--1,in--2,out--2*/
    unsigned char ucType; /*0,开门端; 1,控制端*/
    char caEquipName[64+1];
    int iEquipId;
}CONFIG_GROUPATTR_STR;

typedef	struct ConfigAutoOnOff  
{
	char cHolidayOff;        /* 节假日自动关机 */
	char cTimerOff;          /* 定时开关机 */
	unsigned int auiOffTime[4];   /* 关机时间 */
	unsigned int auiOnTime[4];    /* 开机时间 */
	char caForceOnPwd[16];    /* 强制开机密码 */
}CONFIG_AUTO_ON_OFF_STR;

typedef struct Ring
{
    unsigned char ucStatus;   /* 该条响铃的使能状态，0 - 禁止，1 - 开启 */		
    unsigned char ucHour;     /* 小时 */
    unsigned char ucMinute;   /* 分钟 */
    unsigned char ucTimes;    /* 播放次数 */
    unsigned char ucIndex;    /* 铃声序号 */
    unsigned char ucaWeekday[7]; /* 周一至周日的使能状态，0 - 禁止，1 - 开启 */
}RING_STR;

typedef struct ConfigRing
{
    struct Ring Ring[10];
	unsigned int ulDuringTimes;		/* 每次响铃持续次数 */
}CONFIG_RING_STR;

#define MAX_HOLIDAY_NUM 24
typedef struct Holiday
{
    unsigned char ucStatus;   /* 使能状态，0 - 禁止，1 - 开启 */		
    char caName[33];          /* 节日名称 */
    int iStartTime;           /* 开始时间 格式 20120501 */
    int iEndTime;             /* 结束时间 格式 20120505 */
}HOLIDAY_STR;

typedef struct ConfigHoliday
{
    unsigned char ucaWeekday[7]; /* 周一至周日的使能状态，0 - 禁止，1 - 开启 */
    struct Holiday Holiday[MAX_HOLIDAY_NUM];
}CONFIG_HOLIDAY_STR;

typedef	struct ConfigWiegand   
{
	int iBitWidth;              /* 韦根输入的位宽 */
	short sImpulseWidth;        /* 韦根输入的脉冲宽度 范围20～800us， 默认100us */
	short sImpulseInterval;     /* 韦根输入的脉冲间隔  范围200-20000us  默认900us */
	char cOutPutMode;           /* 韦根输出的格式 0：不存在 1:带设备号的韦根26  2:不带设备号的韦根26  3:带设备号的韦根34  4:不带设备号的韦根34 */
	unsigned char ucDevId;      /* 设备号 范围0-255 */
	short sImpulseWidthout;     /* 韦根输出的脉冲宽度  范围20～800us， 默认100us */
	short sImpulseIntervalout;  /* 韦根输出的脉冲间隔  范围200-20000us  默认900us */
	char cCardParse;            /* 卡号解析  0：WG34   1：WG26   2：ABA-10   3：ABA-8   4：ABA-5   默认为WG34 */
	char cOutPutFormat;         /* 韦根输出内容，0-工号，1-卡号 */
	char caOoutPutContent[16];   /* 选择固定内容时， 要填写该字段  */
	unsigned short cErrorId;    /* 失败id  范围：0-65534  当选择关闭时不输出。默认为关闭。 */
	unsigned short usAreaCode;  /* 区码 韦根26 <= 254 韦根34 <= 65534 */
}CONFIG_WIEGAND_STR;

/* 同步配置 */
typedef struct ConfigSync
{
	char caIp[16];              /* 同步功能的组IP地址 */
	char caPwd[10];				/* 同步功能的密码*/
	unsigned char ucSwitch;		/* 同步: 0 关 ; 1 开 */
}CONFIG_SYNC_STR;	




/**************************************************************\
** 函数名称： GetConfigBase
** 功能： 获取基本配置
** 参数： pstrConfigBase 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigBase(CONFIG_BASE_STR *pstrConfigBase);

/**************************************************************\
** 函数名称： GetConfigNetWork
** 功能： 获取网络配置
** 参数： pstrConfigNetwork 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigNetWork(CONFIG_NETWORK_STR *pstrConfigNetwork);

/**************************************************************\
** 函数名称： GetConfigThreshold
** 功能： 获取阔值配置
** 参数： pstrConfigThreshold 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigThreshold(CONFIG_THRESHOLD_STR *pstrConfigThreshold);

/**************************************************************\
** 函数名称： GetConfigSecurity
** 功能： 获取安全配置
** 参数： pstrConfigSecurity 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigSecurity(CONFIG_SECURITY_STR *pstrConfigSecurity);

/**************************************************************\
** 函数名称： GetConfigAttendance
** 功能： 获取考勤配置
** 参数： pstrConfigAttendance 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigAttendance(CONFIG_ATTENDANCE_STR *pstrConfigAttendance);

/**************************************************************\
** 函数名称： GetConfigOthers
** 功能： 获取其它配置
** 参数： pstrConfigOthers 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigOthers(CONFIG_OTHERS_STR *pstrConfigOthers);

/**************************************************************\
** 函数名称： GetConfigAudIo
** 功能： 获取声音配置
** 参数： pstrConfigAudio 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigAudIo(CONFIG_AUDIO_STR *pstrConfigAudio);

/**************************************************************\
** 函数名称： GetConfigVideo
** 功能： 获取摄像机配置
** 参数： pstrConfigVideo 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigVideo(CONFIG_VIDEO_STR *pstrConfigVideo);

/**************************************************************\
** 函数名称： GetConfigLoginReason
** 功能： 获取功能键配置
** 参数： pstrConfigLoginReason 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigLoginReason(CONFIG_LOGINREASON_STR *pstrConfigLoginReason);

/**************************************************************\
** 函数名称： GetConfigGroupAttr
** 功能： 获取组配置
** 参数： pstrConfigGroupAttr 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigGroupAttr(CONFIG_GROUPATTR_STR *pstrConfigGroupAttr);

/**************************************************************\
** 函数名称： GetConfigAutoOnOff
** 功能： 获取自动开关机配置
** 参数： pstrConfigAutoOnOff 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigAutoOnOff(CONFIG_AUTO_ON_OFF_STR *pstrConfigAutoOnOff);

/**************************************************************\
** 函数名称： GetConfigRing
** 功能： 获取响铃配置
** 参数： pstrConfigRing 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigRing(CONFIG_RING_STR *pstrConfigRing);

/**************************************************************\
** 函数名称： GetConfigHoliday
** 功能： 获取节假日配置
** 参数： pstrConfigHoliday 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-07-06
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigHoliday(CONFIG_HOLIDAY_STR *pstrConfigHoliday);

/**************************************************************\
** 函数名称： GetConfigWiegand
** 功能： 获取韦根配置
** 参数： pstrConfigWiegand 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigWiegand(CONFIG_WIEGAND_STR *pstrConfigWiegand);
/**************************************************************\
** 函数名称： GetConfigSync
** 功能： 获取同步功能配置
** 参数： pstrConfigSync 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-10-18
** 修改作者： 
** 修改日期： 
\**************************************************************/
void GetConfigSync(CONFIG_SYNC_STR *pstrConfigSync);


/**************************************************************\
** 函数名称： SetConfigBase
** 功能： 设置基本配置
** 参数： pstrConfigBase 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigBase(CONFIG_BASE_STR *pstrConfigBase);

/**************************************************************\
** 函数名称： SetConfigSys
** 功能： 设置基本配置
** 参数： pstrConfigSys 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigSys(CONFIG_SYS_STR *pstrConfigSys);

/**************************************************************\
** 函数名称： SetConfigNetwork
** 功能： 设置网络配置
** 参数： pstrConfigNetwork 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigNetwork(CONFIG_NETWORK_STR *pstrConfigNetwork);

/**************************************************************\
** 函数名称： SetConfigThreshold
** 功能： 设置Threshold配置
** 参数： pstrConfigThreshold 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigThreshold(CONFIG_THRESHOLD_STR *pstrConfigThreshold);

/**************************************************************\
** 函数名称： SetConfigSecurity
** 功能： 设置安全配置
** 参数： pstrConfigSecurity 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigSecurity(CONFIG_SECURITY_STR *pstrConfigSecurity);

/**************************************************************\
** 函数名称： SetConfigAttendance
** 功能： 设置考勤配置
** 参数： pstrConfigAttendance 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigAttendance(CONFIG_ATTENDANCE_STR *pstrConfigAttendance);

/**************************************************************\
** 函数名称： SetConfigOthers
** 功能： 设置考勤配置
** 参数： pstrConfigOthers 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigOthers(CONFIG_OTHERS_STR *pstrConfigOthers);

/**************************************************************\
** 函数名称： SetConfigAudIo
** 功能： 设置音量相关配置
** 参数： pstrConfigOthers 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigAudIo(CONFIG_AUDIO_STR *pstrConfigAudio);

/**************************************************************\
** 函数名称： SetConfigVideo
** 功能： 设置摄像头相关配置
** 参数： pstrConfigVideo 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigVideo(CONFIG_VIDEO_STR *pstrConfigVideo);

/**************************************************************\
** 函数名称： SetConfigLoginReason
** 功能： 设置功能键相关配置
** 参数： pstrConfigLoginReason 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigLoginReason(CONFIG_LOGINREASON_STR *pstrConfigLoginReason);

/**************************************************************\
** 函数名称： SetConfigGroupAttr
** 功能： 设置功能键相关配置
** 参数： pstrConfigGroupAttr 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigGroupAttr(CONFIG_GROUPATTR_STR *pstrConfigGroupAttr);

/**************************************************************\
** 函数名称： SetConfigAutoOnOff
** 功能： 设置自动开关机相关配置
** 参数： pstrConfigAutoOnOff 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigAutoOnOff(CONFIG_AUTO_ON_OFF_STR *pstrConfigAutoOnOff);

/**************************************************************\
** 函数名称： SetConfigRing
** 功能： 设置响铃相关配置
** 参数： pstrConfigRing 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-27
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigRing(CONFIG_RING_STR *pstrConfigRing);

/**************************************************************\
** 函数名称： SetConfigHoliday
** 功能： 设置节假日配置
** 参数： pstrConfigHoliday 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-07-06
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigHoliday(CONFIG_HOLIDAY_STR *pstrConfigHoliday);

/**************************************************************\
** 函数名称： SetConfigWiegand
** 功能： 设置韦根配置
** 参数： pstrConfigWiegand 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-06-21
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigWiegand(CONFIG_WIEGAND_STR *pstrConfigWiegand);
/**************************************************************\
** 函数名称： SetConfigSync
** 功能： 设置同步配置
** 参数： pstrConfigSync 指针
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-10-18
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetConfigSync(CONFIG_SYNC_STR *pstrConfigSync);

/**************************************************************\
** 函数名称： WriteFlash
** 功能： 往设备中写入内容
** 参数： mtd	写入的设备
          offset写入的偏移量
          pData	写入的数据缓存
          nLen	写入数据的长度
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-10-18
** 修改作者： 
** 修改日期： 
\**************************************************************/
int WriteFlash(char* mtd, int offset, char* pData, int nLen);


/**************************************************************\
** 函数名称： ReadFlash
** 功能： 从设备中读内容
** 参数： mtd	写入的设备
          offset写入的偏移量
          pData	写入的数据缓存
          nLen	写入数据的长度
** 返回： 
** 创建作者： 朱坚
** 创建日期： 2012-10-18
** 修改作者： 
** 修改日期： 
\**************************************************************/
int ReadFlash(char* mtd, int offset, int nBufLen, char* pBuf);

/******************************************************************************
 * 函数名称：   SetNet
 * 功能：       设置网络参数
 * 参数： 	pIfName:设备名
 		dwIpAddr:IP地址
 		dwSubMask:子网掩码
 		pGW:网关
 		pDNS:首选DNS服务器
 		pDNS2:次选DNS服务器
 		pMac:Mac地址
 		pHostname:主机名
 		nMTU：最大传输单元
 		bOnBoot：启动时是否激活网卡
 * 返回：       0：成功；其它：失败
 * 创建作者： 
 * 创建日期： 2012-6-21
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
int SetNet(char* pIfName, unsigned long dwIpAddr, unsigned long dwSubMask, char* pGW, char* pDNS, char* pDNS2, char* pMac, char* pHostname, unsigned short nMTU, int bOnBoot);

/******************************************************************************
 * 函数名称： getnet
 * 功能： 得到网络参数
 * 参数： 	pIfName:设备名
 			pdwIpAddrp:IP地址
 			pdwSubMask:子网掩码
 			pGW:网关
 			pDNS:DNS服务器
 			pDNS2:
 			pMac:Mac地址
 			pHostname:主机名
 * 返回： 
 * 创建作者： 
 * 创建日期： 2012-6-21
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
int getnet(char* pIfName, unsigned long* pdwIpAddr, unsigned long* pdwSubMask, char* pGW, char* pDNS, char* pDNS2, char* pMac, char* pHostname);

/**************************************************************\
** 函数名称： GetIPAddr
** 功能： 获取IP地址
** 参数： pIP	获取到的IP
          len	要获取的长度
** 返回： 无
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
int GetIPAddr(char *pIP,int len);

/**************************************************************\
** 函数名称： DelAllRecord
** 功能： 删除全部记录和索引文件
** 参数： 无
** 返回：  0 : 成功; -1 : 失败
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
int DelAllRecord();


/**************************************************************\
** 函数名称： DelRecords
** 功能： 删除iUserID 某个时间以前的记录
** 参数： tCurtime：某个时间
          iUserID : 某个用户Id
** 返回： 无
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
int DelRecords(time_t tCurtime, int iUserID);

/**************************************************************\
** 函数名称： GetItemList
** 功能：  获取简要记录队列头指针
** 参数： 无
** 返回：  
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
//FEATURE_Q_HEADER * GetItemList();

/**************************************************************\
** 函数名称： FreeRecordList
** 功能： 释放已经查询的简要记录队列
** 参数： 无
** 返回： 无
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
void FreeRecordList();

/**************************************************************\
** 函数名称： DelRecordsByTime
** 功能： 删除所有用户某个时间以前的记录
** 参数： tCurtime：某个时间
** 返回： 0：成功；-1：失败；
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
int DelRecordsByTime(time_t tCurtime);


/******************************************************************************
 * 函数名称： GetSearchRecordNum
 * 功能： 获取当前查询记录条数
 * 参数： 无
 * 返回： 出勤时间
 * 创建作者： LPH
 * 创建日期： 2012-11-05
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
int GetSearchRecordNum();

/**************************************************************\
** 函数名称： GetCurUSBTransNum
** 功能： 获取当前U盘已导入/导出记录、用户条数
** 参数： 无
** 返回： 当前记录No
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
int GetCurUSBTransNum();

/**************************************************************\
** 函数名称： SetCurUSBTransNum
** 功能： 设置当前U盘已导入/导出记录、用户条数
** 参数： iValue：要设置的值
** 返回： 无
** 创建作者： 朱坚
** 创建日期： 2012-07-02
** 修改作者： 
** 修改日期： 
\**************************************************************/
void SetCurUSBTransNum(int iValue);



/******************************************************************************
 * 函数名称： formatSDEx
 * 功能： 格式化SD卡
 * 参数： 无
 * 返回： 0：成功，-1：失败；
 * 创建作者： LPH
 * 创建日期： 2012-11-05
 * 修改作者：
 * 修改日期：
 ******************************************************************************/
int formatSDEx();

int ExportAllLog(char *pDstPath);

#endif
#ifdef __cplusplus 
}
#endif


#endif
