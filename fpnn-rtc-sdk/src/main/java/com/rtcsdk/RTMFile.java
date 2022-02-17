package com.rtcsdk;

import androidx.annotation.NonNull;

import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.rtcsdk.RTMStruct.*;
import com.rtcsdk.UserInterface.IRTMDoubleValueCallback;

import org.json.JSONObject;
import java.security.MessageDigest;

class RTMFile extends RTMSystem {
    interface DoubleStringCallback{
        void onResult(String str1, String str2, RTMAnswer answer);
    }


    enum FileTokenType {
        P2P,
        Group,
        Room
    }

    private static class SendFileInfo {
        public FileTokenType actionType;

        public long xid;
        public byte mtype;
        public byte[] fileContent;
        public String filename;
        public JSONObject attrs;

        public String token;
        public String endpoint;
        public String fileExt;
        public long lastActionTimestamp;
        public UserInterface.IRTMDoubleValueCallback<Long,Long> callback;
        public RTMAudioStruct audioAttrs;//给语音用 语言+时长
    }

    //重载
    public void sendFile(IRTMDoubleValueCallback<Long,Long> callback, long peerUid, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs) {
        sendFile(callback, peerUid, mtype, fileContent, filename, attrs,null);
    }

    public ModifyTimeStruct sendFile(long peerUid, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs) {
        return sendFile(peerUid, mtype, fileContent, filename,attrs,null);
    }

    public void sendGroupFile(IRTMDoubleValueCallback<Long,Long> callback, long groupId, FileMessageType mtype, byte[] fileContent, String filename,JSONObject attrs) {
        sendGroupFile(callback, groupId, mtype, fileContent, filename, attrs,null);
    }

    public ModifyTimeStruct sendGroupFile(long groupId, FileMessageType mtype, byte[] fileContent, String filename,JSONObject attrs) {
        return sendGroupFile(groupId, mtype, fileContent, filename, attrs,null);
    }

    public void  sendRoomFile(IRTMDoubleValueCallback<Long,Long> callback, long roomId, FileMessageType mtype, byte[] fileContent, String filename,JSONObject attrs) {
        sendRoomFile(callback, roomId, mtype, fileContent, filename, attrs,null);
    }

    public ModifyTimeStruct sendRoomFile(long roomId, FileMessageType mtype, byte[] fileContent, String filename,JSONObject attrs) {
        return sendRoomFile(roomId, mtype, fileContent, filename, attrs,null);
    }
    //重载end

    //===========================[ File Token ]=========================//
    private void fileToken(final DoubleStringCallback callback, FileTokenType tokenType, long xid) {
        Quest quest = new Quest("filetoken");
        switch (tokenType) {
            case P2P:
                quest.param("cmd", "sendfile");
                quest.param("to", xid);
                break;
            case Group:
                quest.param("cmd", "sendgroupfile");
                quest.param("gid", xid);
                break;
            case Room:
                quest.param("cmd", "sendroomfile");
                quest.param("rid", xid);
                break;
        }

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                String token = "" ,endpoint = "";
                if (errorCode == okRet) {
                    token = rtmUtils.wantString(answer,"token");
                    endpoint = rtmUtils.wantString(answer,"endpoint");
                }
                callback.onResult(token, endpoint, genRTMAnswer(answer,errorCode));
            }
        });
    }

    private RTMAnswer fileToken(StringBuilder token, StringBuilder endpoint, FileTokenType tokenType, long xid) {
        Quest quest = new Quest("filetoken");
        switch (tokenType) {
            case P2P:
                quest.param("cmd", "sendfile");
                quest.param("to", xid);
                break;

            case Group:
                quest.param("cmd", "sendgroupfile");
                quest.param("gid", xid);
                break;

            case Room:
                quest.param("cmd", "sendroomfile");
                quest.param("rid", xid);
                break;
        }

        Answer answer = sendQuest(quest);
        if (answer !=null && answer.getErrorCode() == okRet){
            token.append(rtmUtils.wantString(answer,"token"));
            endpoint.append(rtmUtils.wantString(answer,"endpoint"));
        }
        return genRTMAnswer(answer, answer.getErrorCode());
    }

    //===========================[ File Utilies ]=========================//

    private String extraFileExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx == -1)
            return "";

        return filename.substring(idx + 1);
    }

    private String buildFileAttrs(SendFileInfo info) {
        String fileAttrs = "";
        try {
            MessageDigest  md5 = MessageDigest.getInstance("MD5");
            md5.update(info.fileContent);
            byte[] md5Binary = md5.digest();
            String md5Hex = rtmUtils.bytesToHexString(md5Binary, true) + ":" + info.token;

            md5 = MessageDigest.getInstance("MD5");
            md5.update(md5Hex.getBytes("UTF-8"));
            md5Binary = md5.digest();
            md5Hex = rtmUtils.bytesToHexString(md5Binary, true);
            JSONObject allatrrs = new JSONObject();

            if (info.attrs != null)
                allatrrs.put("custom",info.attrs);
            JSONObject rtmAttrs = new JSONObject();

            rtmAttrs.put("sign",md5Hex);

            if (info.filename != null && info.filename.length() > 0){
                rtmAttrs.put("filename",info.filename);
                info.fileExt = extraFileExtension(info.filename);
                rtmAttrs.put("ext", info.fileExt);
            }
            if (info.audioAttrs != null) {
                rtmAttrs.put("lang",info.audioAttrs.lang);
                rtmAttrs.put("duration",info.audioAttrs.duration);
                rtmAttrs.put("ext","amr");
                rtmAttrs.put("type","audiomsg");
                rtmAttrs.put("codec","AMR_WB");
                rtmAttrs.put("srate",16000);
            }
            allatrrs.put("rtm",rtmAttrs);
            fileAttrs = allatrrs.toString();
        } catch (Exception e) {
            errorRecorder.recordError("buildFileAttrs error " + e.getMessage());
        }
        return fileAttrs;
    }

    private Quest buildSendFileQuest(SendFileInfo info) {
        Quest quest = null;
        switch (info.actionType) {
            case P2P:
                quest = new Quest("sendfile");
                quest.param("to", info.xid);
                break;

            case Group:
                quest = new Quest("sendgroupfile");
                quest.param("gid", info.xid);
                break;

            case Room:
                quest = new Quest("sendroomfile");
                quest.param("rid", info.xid);
                break;
        }

        quest.param("pid", getPid());
        quest.param("from", getUid());
        quest.param("token", info.token);
        quest.param("mtype", info.mtype);
        quest.param("mid", Genid.genMid());

        quest.param("file", info.fileContent);
        quest.param("attrs", buildFileAttrs(info));

        return quest;
    }

    private int sendFileWithClient(final SendFileInfo info, final TCPClient client) {
        final Quest quest = buildSendFileQuest(info);
        client.sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                if (errorCode == okRet) {
                    try {
                        long mtime = rtmUtils.wantLong(answer,"mtime");
                        info.callback.onResult(mtime, rtmUtils.wantLong(quest,"mid"),genRTMAnswer(okRet));

                        activeFileGateClient(info.endpoint, client);
                        return;
                    } catch (Exception e) {
                        errorCode = ErrorCode.FPNN_EC_CORE_INVALID_PACKAGE.value();
                    }
                }
                info.callback.onResult(0L, 0L,genRTMAnswer(answer,errorCode));
            }
        });

        return okRet;
    }

    private int sendFileWithoutClient(final SendFileInfo info){
        String fileGateEndpoint;
        fileGateEndpoint = info.endpoint;

        final TCPClient client = TCPClient.create(fileGateEndpoint, true);
        client.setQuestTimeout(rtmConfig.globalFileQuestTimeoutSeconds);
        client.setErrorRecorder(errorRecorder);

        sendFileWithClient(info, client);

        return okRet;
    }

    private void getFileTokenCallback(SendFileInfo info, String token, String endpoint, RTMAnswer answer) throws InterruptedException {
        if (answer.errorCode == okRet) {
            info.token = token;
            info.endpoint = endpoint;
            int  err;

            TCPClient fileClient = fecthFileGateClient(info.endpoint);
            if (fileClient != null)
                err = sendFileWithClient(info, fileClient);
            else
                err = sendFileWithoutClient(info);

            if (err == okRet)
                return;
            else
                errorRecorder.recordError("send file error");
        } else
            info.callback.onResult(0L,0L, answer);
    }



    //===========================[ Real Send File ]=========================//
    private void realSendFile(final UserInterface.IRTMDoubleValueCallback<Long,Long> callback, FileTokenType tokenType, long targetId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioAttrs) {
        byte fileType = (byte)mtype.value();
        if (fileType < MessageType.IMAGEFILE || fileType > MessageType.NORMALFILE) {
            callback.onResult(0L,0L,genRTMAnswer(RTMErrorCode.RTM_EC_INVALID_MTYPE.value()));
            return ;
        }

        byte[] realData = fileContent;
        String realName = filename;
        if (audioAttrs!=null && audioAttrs.audioData != null) {
            if (!RTMAudio.getInstance().checkAudio(audioAttrs.audioData)) {
                ModifyTimeStruct tt = new ModifyTimeStruct();
                tt.errorCode = RTMErrorCode.RTM_EC_INVALID_FILE_OR_SIGN_OR_TOKEN.value();
                tt.errorMsg = "invalid audio type";
                callback.onResult(0L, 0L, tt);
                return;
            }
            fileType = MessageType.AUDIOFILE;
            realData = audioAttrs.audioData;
            realName = "";
        }

        SendFileInfo info = new SendFileInfo();
        info.actionType = tokenType;
        info.xid = targetId;
        info.mtype = fileType;
        info.fileContent = realData;
        info.filename = realName;
        info.attrs = attrs;
        info.lastActionTimestamp = rtmUtils.getCurrentMilliseconds();
        info.callback = callback;
        info.audioAttrs = audioAttrs;
        final SendFileInfo inFile = info;
        fileToken(new DoubleStringCallback() {
            @Override
            public void onResult(String token, String endpoint, RTMAnswer answer) {
                try {
                    getFileTokenCallback(inFile, token, endpoint, answer);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, tokenType, info.xid);
    }

    private ModifyTimeStruct realSendFile(FileTokenType tokenType, long targetId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioAttrs){
        ModifyTimeStruct ret = new ModifyTimeStruct();
        byte fileType = (byte)mtype.value();
        //----------[ 1. check mtype ]---------------//
        if (fileType < MessageType.IMAGEFILE || fileType > MessageType.NORMALFILE) {
            ret.errorCode = RTMErrorCode.RTM_EC_INVALID_MTYPE.value();
            ret.errorMsg = RTMErrorCode.getMsg(ret.errorCode);
            return ret;
        }

        byte[] realData = fileContent;
        String realName = filename;
        if (audioAttrs!=null && audioAttrs.audioData != null) {
            if (!RTMAudio.getInstance().checkAudio(audioAttrs.audioData)) {
                ret.errorCode = RTMErrorCode.RTM_EC_INVALID_FILE_OR_SIGN_OR_TOKEN.value();
                ret.errorMsg = "invalid audio type";
                return ret;
            }
            fileType = MessageType.AUDIOFILE;
            realData = audioAttrs.audioData;
            realName = "";
        }

        //----------[ 2. Get File Token ]---------------//

        StringBuilder token = new StringBuilder();
        StringBuilder endpoint = new StringBuilder();
        RTMAnswer fileAnswer = fileToken(token, endpoint, tokenType, targetId);
        if (fileAnswer.errorCode != okRet){
            ret.errorCode = fileAnswer.errorCode;
            ret.errorMsg = fileAnswer.errorMsg;
            return ret;
        }
        String realEndpoint = endpoint.toString();

        //----------[ 3. fetch file gate client ]---------------//
        try {
            TCPClient fileClient = fecthFileGateClient(realEndpoint);
            if (fileClient == null) {
                fileClient = TCPClient.create(realEndpoint);
                fileClient.setQuestTimeout(rtmConfig.globalFileQuestTimeoutSeconds);

                if (fileClient.connect(true)) {
                    activeFileGateClient(realEndpoint, fileClient);
                } else {
                    ret.errorCode = ErrorCode.FPNN_EC_CORE_INVALID_CONNECTION.value();
                    ret.errorMsg = "invalid conection";
                    return ret;
                }
            }

            //----------[ 4. build quest ]---------------//
            SendFileInfo info = new SendFileInfo();
            info.actionType = tokenType;
            info.xid = targetId;
            info.mtype = fileType;
            info.fileContent = realData;
            info.filename = realName;
            info.attrs = attrs;
            info.token = token.toString();
            info.audioAttrs = audioAttrs;

            Quest quest = buildSendFileQuest(info);
            Answer answer = fileClient.sendQuest(quest);

            if (answer.isErrorAnswer()){
                ret.errorCode = answer.getErrorCode();
                ret.errorMsg = answer.getErrorMessage();
                return ret;
            }
            activeFileGateClient(realEndpoint, fileClient);

            ret.errorCode = okRet;
            ret.errorMsg = "";
            ret.modifyTime = rtmUtils.wantLong(answer,"mtime");
            ret.messageId = rtmUtils.wantLong(quest,"mid");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ret.errorCode = ErrorCode.FPNN_EC_PROTO_UNKNOWN_ERROR.value();
            ret.errorMsg = "thread interupted";
            return ret;
        }
        return ret;
    }

    /**
     * 发送p2p文件 async
     * @param callback  IRTMDoubleValueCallback<Long,Long>接口回调
     * @param peerUid   目标uid
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public void sendFile(@NonNull UserInterface.IRTMDoubleValueCallback<Long,Long> callback, long peerUid, @NonNull FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioInfo) {
        realSendFile(callback, FileTokenType.P2P, peerUid, mtype, fileContent, filename, attrs,audioInfo);
    }

    /**
     * 发送p2p文件 sync
     * @param peerUid   目标uid
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public ModifyTimeStruct sendFile(long peerUid, @NonNull FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs,RTMAudioStruct audioInfo){
        return realSendFile(FileTokenType.P2P, peerUid, mtype, fileContent, filename, attrs,audioInfo);
    }

    /**
     * 发送群组文件 async
     * @param callback  IRTMDoubleValueCallback<Long,Long>接口回调
     * @param groupId   群组id
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public void  sendGroupFile(@NonNull UserInterface.IRTMDoubleValueCallback<Long,Long> callback, long groupId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs,RTMAudioStruct audioInfo){
        realSendFile(callback, FileTokenType.Group, groupId, mtype, fileContent, filename, attrs,audioInfo);
    }

    /**
     * 发送群组文件 sync
     * @param groupId   群组id
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public ModifyTimeStruct sendGroupFile(long groupId, @NonNull FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs,RTMAudioStruct audioInfo){
        return realSendFile(FileTokenType.Group, groupId, mtype, fileContent, filename, attrs,audioInfo);
    }

    /**
     * 发送房间文件 async
     * @param callback  IRTMDoubleValueCallback<Long,Long>接口回调
     * @param roomId   房间id
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public void  sendRoomFile(@NonNull UserInterface.IRTMDoubleValueCallback<Long,Long> callback, long roomId,@NonNull  FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs,RTMAudioStruct audioInfo){
        realSendFile(callback, FileTokenType.Room, roomId, mtype, fileContent, filename, attrs,audioInfo);
    }

    /**
     * 发送房间文件 sync
     * @param roomId   房间id
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public ModifyTimeStruct sendRoomFile(long roomId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs,RTMAudioStruct audioInfo){
        return realSendFile(FileTokenType.Room, roomId, mtype, fileContent, filename, attrs,audioInfo);
    }
}
