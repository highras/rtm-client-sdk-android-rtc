package com.rtcsdk;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.livedata.rtc.RTCEngine;
import com.rtcsdk.RTMStruct.RTMAnswer;
import com.rtcsdk.RTMStruct.RoomInfo;
import com.rtcsdk.UserInterface.IRTMCallback;
import com.rtcsdk.UserInterface.IRTMEmptyCallback;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RTMRTC extends RTMChat{
    /**
     *管理员权限操作
     * @param roomId 房间id
     * @param uids
     * @param command
     * 0 赋予管理员权
     * 1 剥夺管理员权限
     * 2 禁止发送音频数据
     * 3 允许发送音频数据
     * 4 禁止发送视频数据
     * 5 允许发送视频数据
     * 6 关闭他人麦克风
     * 7 关闭他人摄像头
     * @return
     */
    public void adminCommand (IRTMEmptyCallback callback, long roomId, HashSet<Long> uids,  int command){
        Quest quest = new Quest("adminCommand");
        quest.param("rid", roomId);
        quest.param("uids", uids);
        quest.param("command", command);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 开启摄像头
     */
    public RTMAnswer openCamera(){
        cameraStatus =  true;
        String ret = RTCEngine.setCameraFlag(true);
        if (!ret.isEmpty())
            return genRTMAnswer(videoError,"ret");
        return genRTMAnswer(okRet);
    }

    /**
     * 关闭摄像头
     */
    public void closeCamera(){
        cameraStatus = false;
        RTCEngine.setCameraFlag(false);
    }

    /**
     * 摄像头切换
     * @param front true-使用前置  false-使用后置
     */
    public void switchCamera(boolean front){
        RTCEngine.switchCamera(front);
    }

    /**
     * 打开麦克风(音频模式进入房间初始默认关闭  视频模式进入房间默认开启)
     */
    public void openMic(){
        RTCEngine.canSpeak(true);
    }

    /**
     * 关闭麦克风
     */
    public void closeMic(){
        RTCEngine.canSpeak(false);
    }


    /**
     * 设置麦克风增益等级(声音自动增益 取值 范围0-10)
     */
    public void setMicphoneLevel(int level){
        if (level <= 0 || level >= 10)
            return;
        RTCEngine.setMicphoneGain(level);
    }

    /**
     * 取消订阅视频流
     * @param roomId 房间id
     * @param uids 取消订阅的成员列表
     * @return
     */
    public void unsubscribeVideo(long roomId, HashSet<Long> uids){
        Quest quest = new Quest("unsubscribeVideo");
        quest.param("rid", roomId);
        quest.param("uids", uids);
        sendQuestEmptyCallback(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
            }
        },quest);
        for (Long id : uids)
            RTCEngine.unsubscribeUser(id);
    }

    /**
     * 创建RTC房间
     * @roomId 房间id
     * @roomType 1-voice 2-video(视频房间摄像头默认关闭 麦克风默认开启)
     * @enableRecord 是否开启云端录制 0-不开启录制 1-开启录制
     * @param callback 回调
     */
    public void createRTCRoom(@NonNull final IRTMCallback<RoomInfo> callback, final long roomId, final int roomType, int enableRecord) {
        final RoomInfo ret = new RoomInfo();
        ret.roomId = roomId;
        ret.roomTyppe = roomType;
        Quest quest = new Quest("createRTCRoom");
        quest.param("rid", roomId);
        quest.param("type", roomType);
        quest.param("enableRecord", enableRecord);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    String roomToken = rtmUtils.wantString(answer,"token");
                    enterRTCRoomReal(callback,roomId, roomToken,ret, roomType);
                }
                else {
                    ret.errorCode = errorCode;
                    ret.errorMsg = answer.getErrorMessage();
                    callback.onResult(ret, genRTMAnswer(answer, errorCode));
                }
            }
        });
    }

    /**
     * 进入RTC房间
     * @param callback 回调
     * @param roomId   房间id
     */
    public void enterRTCRoom(@NonNull final IRTMCallback<RoomInfo> callback, final long roomId) {
        super.enterRTCRoom(callback,roomId);
    }

    /**
     * 订阅视频流
     * @param roomId 房间id
     * @param userViews  key-订阅的用户id value-显示用户的surfaceview(需要 宽高比 3：4)
     */
    public RTMAnswer subscribeVideos(long roomId, HashMap<Long, SurfaceView> userViews){
        for (Map.Entry<Long, SurfaceView> it:userViews.entrySet()) {
            SurfaceView tmp = it.getValue();
            float ratio =  (float) tmp.getWidth() / tmp.getHeight();
            if (ratio < 0.749 ||  ratio> 0.759){
                return genRTMAnswer(videoError,"user " + it.getKey() + " unfitable aspect ratio");
            }
            long uid = it.getKey();
        }

        Quest quest = new Quest("subscribeVideo");
        quest.param("rid", roomId);
        quest.param("uids", userViews.keySet());
        Answer  ret = sendQuest(quest);
        RTMAnswer answer = genRTMAnswer(ret);
        if (answer.errorCode == okRet){
            for (Map.Entry<Long, SurfaceView> it:userViews.entrySet()) {
                RTCEngine.bindDecodeSurface(it.getKey(), it.getValue().getHolder().getSurface());
            }
        }
        return answer;
    }

    /**
     * 邀请用户加入RTC房间(非强制，需要对端确认)(发送成功仅代表收到该请求，至于用户最终是否进入房间结果未知)
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     需要邀请的用户列表
     */
    public void inviteUserIntoRTCRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids){
        Quest quest = new Quest("inviteUserIntoRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 设置目前活跃的房间(仅对语音房间有效)
     * @param roomId
     */
    public RTMAnswer setActivityRoom(long roomId){
        String msg = RTCEngine.setActivityRoom(roomId);
        if (msg.isEmpty()){
            return genRTMAnswer(okRet);
        }
        else
            return genRTMAnswer(voiceError,msg);
    }

    /**
     * 切换扬声器听筒(耳机状态下不操作)(默认扬声器)
     * @param usespeaker true-使用扬声器 false-使用听筒
     */
    public void switchOutput(final boolean usespeaker){
        if (isHeadsetOn())
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                RTCEngine.switchVoiceOutput(usespeaker);
            }
        }).start();
    }

    /**
     * 设置语音开关(开启语音功能或者关闭语音功能(备注:默认开启 如果为语音功能关闭则麦克风自动关闭)
     * @param status
     */
    public RTMAnswer setVoiceStat(boolean status){
        String msg = RTCEngine.setVoiceStat(status);
        if (msg.isEmpty())
            return genRTMAnswer(okRet);
        else
            return genRTMAnswer(voiceError,msg);
    }

    /**离开RTC房间
     * @param roomId   房间id
     */
    public void leaveRTCRoom(final long roomId, final IRTMEmptyCallback callback){
        Quest quest = new Quest("exitRTCRoom");
        quest.param("rid",roomId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    RTCEngine.leaveRTCRoom(roomId);
                }
                callback.onResult(genRTMAnswer(answer, errorCode));
            }
        });
    }

    /**
     * 屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     屏蔽语音的用户列表
     */
    public void blockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids){
        Quest quest = new Quest("blockUserVoiceInRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 解除屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     解除屏蔽语音的用户列表
     */
    public void unblockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids){
        Quest quest = new Quest("unblockUserVoiceInRTCRoom");
        quest.param("rid",roomId);
        quest.param("uids",uids);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     * 获取语RTC房间成员列表
     * @param callback 回调<RoomInfo>
     */
    public void getRTCRoomMembers(@NonNull final IRTMCallback<RoomInfo> callback, long roomId) {
        Quest quest = new Quest("getRTCRoomMembers");
        quest.param("rid", roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RoomInfo tt = new RoomInfo();
                if (errorCode == okRet) {
                    tt.uids = rtmUtils.wantLongHashSet(answer, "uids");
                    tt.managers = rtmUtils.wantLongHashSet(answer, "administrators");
                    tt.owner = rtmUtils.wantInt(answer,"owner");
                }
                callback.onResult(tt, genRTMAnswer(answer, errorCode));
            }
        });
    }

    /**
     * 获取RTC房间成员个数
     * @param callback 回调
     */
    public void getRTCRoomMemberCount(@NonNull final IRTMCallback<Integer> callback, long roomId) {
        Quest quest = new Quest("getRTCRoomMemberCount");
        quest.param("rid", roomId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                int count = 0;
                if (errorCode == okRet)
                    count = rtmUtils.wantInt(answer,"count");
                callback.onResult(count, genRTMAnswer(answer, errorCode));
            }
        });
    }

    /**
     * 切换视频质量
     * @level 视频质量详见RTMStruct.CaptureLevle
     * @return
     */
    public RTMAnswer switchVideoQuality(int level){
        if (currVideoLevel == level)
            return genRTMAnswer(okRet);
        currVideoLevel = level;
        String msg = RTCEngine.switchVideoCapture(level);
        if (msg.isEmpty())
            return genRTMAnswer(okRet);
        return genRTMAnswer(videoError,msg);
    }


    /**
     * 设置预览view(需要传入的view 真正建立完成)
     * @return
     */
    public void setPreview(SurfaceView view){
        RTCEngine.setpreview(view.getHolder().getSurface());
    }


    /****************P2P*****************/
    /**
     *发起p2p音视频请求(对方是否回应通过 pushP2PRTCEvent接口返回)
     * @param type 1-实时语音  2-实时音视频
     * @SurfaceView view(如果为实时频频 自己预览的view 需要view创建完成并可用)
     * @param toUid 对方id
     */
    public void requestP2PRTC(final int type , final long toUid, final SurfaceView view, final IRTMEmptyCallback callback){
        if (RTCEngine.isInRTCRoom() > 0){
            callback.onResult(genRTMAnswer(voiceError, "please leaveRTC room first"));
            return;
        }

        if (lastCallId > 0){
            callback.onResult(genRTMAnswer(voiceError, "already in p2p type"));
            return;
        }

        Quest quest = new Quest("requestP2PRTC");
        quest.param("type", type);
        quest.param("peerUid", toUid);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    lastCallId = rtmUtils.wantLong(answer, "callId");
                    peerUid = toUid;
                    lastP2Ptype = type;
                    if (type == 2) {
                        String msg = RTCEngine.requestP2PVideo(view.getHolder().getSurface());
                        if (!msg.isEmpty()){
                            callback.onResult(genRTMAnswer(videoError, msg));
                            return;
                        }
//                        RTCEngine.setpreview(view.getHolder().getSurface());
//                        RTCEngine.setCameraFlag(true);
//                        RTCEngine.setVoiceStat(true);
//                        RTCEngine.canSpeak(true);
                    }
                }
                callback.onResult(genRTMAnswer(answer, errorCode));
            }
        });
    }

    /**
     * 取消p2p RTC请求
     * @param callback
     */
    public void cancelP2PRTC(final IRTMEmptyCallback callback){
        if (lastCallId <0) {
            callback.onResult(genRTMAnswer(0));
            return;
        }
        Quest quest = new Quest("cancelP2PRTC");
        quest.param("callId", lastCallId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet){
//                    closeP2P();
                }
                callback.onResult(genRTMAnswer(answer, errorCode));
            }
        });
    }

    void closeP2P(){
        RTCEngine.closeP2P();
        lastCallId = 0;
        peerUid = 0;
        lastP2Ptype = 0;
    }


    /**
     * 关闭p2p 会话
     * @param callback
     */
    public void closeP2PRTC(final IRTMEmptyCallback callback){
        if (lastCallId <0) {
            callback.onResult(genRTMAnswer(0));
            return;
        }
        Quest quest = new Quest("closeP2PRTC");
        quest.param("callId", lastCallId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                closeP2P();
//                if (errorCode == okRet){
//                    closeP2P();
//                }
                callback.onResult(genRTMAnswer(answer, errorCode));
            }
        });
    }

    /**
     * 接受p2p 会话
     * @param callback
     * @param preview 自己预览的view(仅视频)
     * @param bindview 对方的view(仅视频)
     */
    public void acceptP2PRTC(final IRTMEmptyCallback callback, final SurfaceView preview, final SurfaceView bindview){
        if (lastCallId <= 0) {
            callback.onResult(genRTMAnswer(0));
            return;
        }
        Quest quest = new Quest("acceptP2PRTC");
        quest.param("callId", lastCallId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == 0){
                    String ret = RTCEngine.startP2P(lastP2Ptype, peerUid, lastCallId);
                    if (!ret.isEmpty()){
                        callback.onResult(genRTMAnswer(ErrorCode.FPNN_EC_CORE_UNKNOWN_ERROR.value(),"acceptP2PRTC but startP2P error"));
                        return;
                    }

                    if (lastP2Ptype == 2 && preview != null && bindview != null) {
                        setPreview(preview);
                        openCamera();
                        RTCEngine.bindDecodeSurface(peerUid, bindview.getHolder().getSurface());
                    }
                }
                callback.onResult(genRTMAnswer(answer, errorCode));
            }
        });
    }

    /**
     * 拒绝p2p 会话
     * @param callback
     */
    public void refuseP2PRTC(final IRTMEmptyCallback callback){
        if (lastCallId <0) {
            callback.onResult(genRTMAnswer(0));
            return;
        }
        Quest quest = new Quest("refuseP2PRTC");
        quest.param("callId", lastCallId);
        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                callback.onResult(genRTMAnswer(answer, errorCode));
            }
        });
    }
}


