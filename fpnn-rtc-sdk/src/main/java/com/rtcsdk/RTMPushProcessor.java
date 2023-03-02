package com.rtcsdk;

import android.view.SurfaceView;

import com.rtcsdk.RTMStruct.RTMMessage;

import java.util.HashSet;

/***********需要用户重载的push消息类(如果有耗时操作 需要用户单开线程处理业务逻辑 以免阻塞后续的请求)************/
public class RTMPushProcessor
{
    int internalReloginMaxTimes = 30;

    /**
     * RTM链接断开 (默认会自动连接 kickout除外)(备注:链接断开会自动退出之前进入的RTM房间和RTC房间,并且订阅的视频流会自动解除 需要在重连成功再次加入房间并重新订阅)
     */
    public void rtmConnectClose(long uid){}

    /**
     * RTM重连开始接口 每次重连都会判断reloginWillStart返回值 若返回false则中断重连
     * 参数说明 uid-用户id  answer-上次重连的结果  reloginCount-将要重连的次数
     * 备注:需要用户设定一些条件 比如重连间隔 最大重连次数
     */
    public boolean reloginWillStart(long uid, int reloginCount){return true;};

    /**
     * RTM重连完成(如果 successful 为false表示最终重连失败,answer会有详细的错误码和错和错误信息 为true表示重连成功)
     * 备注:当用户的token过期或被加入黑名单 重连会直接返回 不会继续判断reloginWillStart
     */
    public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount){};

    /**
     * 被服务器踢下线(不会自动重连)
     */
    public void kickout(){}

    /**
     * 被踢出RTM房间
     */
    public void kickoutRoom(long roomId){}


    //push聊天消息(具体消息内容为 RTMMessage 中的translatedInfo)
    public void pushChat(RTMMessage msg){}
    public void pushGroupChat(RTMMessage msg){}
    public void pushRoomChat(RTMMessage msg){}
    public void pushBroadcastChat(RTMMessage msg){}


    //pushcmd命令消息(具体消息内容为 RTMMessage 中的stringMessage)
    public void pushCmd(RTMMessage msg){}
    public void pushGroupCmd(RTMMessage msg){}
    public void pushRoomCmd(RTMMessage msg){}
    public void pushBroadcastCmd(RTMMessage msg){}


    //pushmsg消息 (具体消息内容 根据业务自己的messagetype判断 如果为string类型消息RTMMessage中的stringMessage 不为空 反之 binaryMessage不为空)
    public void pushMessage(RTMMessage msg){}
    public void pushGroupMessage(RTMMessage msg){}
    public void pushRoomMessage(RTMMessage msg){}
    public void pushBroadcastMessage(RTMMessage msg){}


    //pushfile消息 (RTMMessage 中的fileInfo结构)
    public void pushFile(RTMMessage msg){}
    public void pushGroupFile(RTMMessage msg){}
    public void pushRoomFile(RTMMessage msg){}
    public void pushBroadcastFile(RTMMessage msg){}


    //实时音视频消息
    public void pushRTCRTT(long rtt){} //RTC延迟时间

    //推送语音翻译文本结果
    public void pushVoiceTranslate(String text, String slang, long uid){}


    public void pushEnterRTCRoom(long roomId, long userId, long time){} //某人进入语音房间
    public void pushExitRTCRoom(long roomId, long userId, long time){} //某人离开语音房间(如果有订阅关系会自动解除)
    public void pushRTCRoomClosed(long roomId){}//语音房间被关闭
    public void pushInviteIntoRTCRoom(long roomId, long userId){} //被邀请加入房间(需要再次调用进入房间接口真正进入语音房间)
    public void pushCameraClosed(long uid){} //某人摄像头关闭
    public void pushAdminCommand(int command, HashSet<Long> uids){} //接收服务端推送的控制指令(自己发起 自己也可以收到)
    //     * 0 赋予管理员权
    //     * 1 剥夺管理员权限
    //     * 2 禁止发送音频数据
    //     * 3 允许发送音频数据
    //     * 4 禁止发送视频数据
    //     * 5 允许发送视频数据
    //     * 6 关闭他人麦克风
    //     * 7 关闭他人摄像头
    //     */
    public void pushKickoutRTCRoom(long roomId){} //某人被踢出RTC房间
    public void pushPullRoom(long roomId, RTMStruct.RoomInfo info){} //被服务器拉入房间
    public void voiceSpeak(long uid){} //谁正在说话

    //推送p2p rtc请求
    public void pushRequestP2PRTC(long uid, int type){}

    //推送p2p rtc event 如果是接受对方视频请求 需要返回显示对方的surfaceview(如果事件为P2PRTCEvent.Accept 自动切换为主线程)
    public SurfaceView pushP2PRTCEvent(long uid, int type, RTMStruct.P2PRTCEvent event){return null;}
}


