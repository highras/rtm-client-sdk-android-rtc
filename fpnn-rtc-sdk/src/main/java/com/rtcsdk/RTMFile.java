package com.rtcsdk;

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
        Room,
        OnlySave
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
        public IRTMDoubleValueCallback<Long,Long> callback;
        public RTMAudioStruct audioAttrs;//给语音用 语言+时长
    }

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
            case OnlySave:
                quest.param("cmd", "uploadfile");
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
    private void realSendFile(final IRTMDoubleValueCallback<Long,Long> callback, FileTokenType tokenType, long targetId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioAttrs) {
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

    /**
     * 发送文件 async
     * @param callback  IRTMDoubleValueCallback<服务器返回时间,消息id>回调
     * @param peerUid   目标uid
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public void sendFile(long peerUid, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioInfo,IRTMDoubleValueCallback<Long,Long> callback ) {
        realSendFile(callback, FileTokenType.P2P, peerUid, mtype, fileContent, filename, attrs,audioInfo);
    }


    /**
     * 发送群组文件 async
     * @param callback  IRTMDoubleValueCallback<服务器返回时间,消息id>回调
     * @param groupId   群组id
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public void  sendGroupFile(long groupId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs,RTMAudioStruct audioInfo,IRTMDoubleValueCallback<Long,Long> callback){
        realSendFile(callback, FileTokenType.Group, groupId, mtype, fileContent, filename, attrs,audioInfo);
    }

    /**
     * 发送房间文件 async
     * @param callback  IRTMDoubleValueCallback<服务器返回时间,消息id>回调
     * @param roomId   房间id
     * @param mtype     消息类型
     * @param fileContent   文件内容
     * @param filename      文件名字
     * @param attrs 文件属性信息
     * @param audioInfo rtm语音结构
     */
    public void  sendRoomFile(long roomId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioInfo, IRTMDoubleValueCallback<Long,Long> callback){
        realSendFile(callback, FileTokenType.Room, roomId, mtype, fileContent, filename, attrs,audioInfo);
    }


    /**
     * 发送文件并返回url
     * @param mtype 文件类型
     * @param fileName 文件名字(需要带后缀)
     * @param fileContent  文件内容
     * @param audioInfo  RTM录音完成返回的结构
     * @param callback IRTMDoubleValueCallback<文件url,文件大小>回调
     */
    public void uploadFile(final FileMessageType mtype, final String fileName, final byte[] fileContent, final RTMAudioStruct audioInfo, final IRTMDoubleValueCallback<String,Long> callback){
        fileToken(new DoubleStringCallback() {
            @Override
            public void onResult(String token, final String endpoint, RTMAnswer tokenanswer) {
                    if (tokenanswer.errorCode == okRet){
                        TCPClient fileClient = fecthFileGateClient(endpoint);
                        if (fileClient != null){
                            Quest quest = new Quest("uploadfile");
                            quest.param("pid", getPid());
                            quest.param("uid",getUid());
                            quest.param("token",token);
                            quest.param("file",fileContent);
                            quest.param("mtype", mtype.value());

                            SendFileInfo sendFileInfo = new SendFileInfo();
                            sendFileInfo.fileContent = fileContent;
                            sendFileInfo.filename = fileName;
                            sendFileInfo.token = token;
                            sendFileInfo.audioAttrs = audioInfo;
                            String tmp = buildFileAttrs(sendFileInfo);

                            quest.param("attrs", tmp);
                            fileClient.sendQuest(quest, new FunctionalAnswerCallback() {
                                @Override
                                public void onAnswer(Answer answer, int errorCode) {
                                    String url = "";
                                    long size = 0;

                                    if (errorCode == okRet){
                                        url = rtmUtils.wantString(answer,"url");
                                        size = rtmUtils.wantLong(answer,"size");
                                    }
                                    callback.onResult(url, size, genRTMAnswer(answer,errorCode));
                                }
                            }, rtmConfig.globalFileQuestTimeoutSeconds);
                        }
                        else{
                            final TCPClient client = TCPClient.create(endpoint, true);
                            Quest quest = new Quest("uploadfile");
                            quest.param("pid", getPid());
                            quest.param("uid",getUid());
                            quest.param("token",token);
                            quest.param("file",fileContent);
                            quest.param("mtype", mtype.value());

                            SendFileInfo sendFileInfo = new SendFileInfo();
                            sendFileInfo.fileContent = fileContent;
                            sendFileInfo.filename = fileName;
                            sendFileInfo.token = token;
                            sendFileInfo.audioAttrs = audioInfo;
                            String tmp = buildFileAttrs(sendFileInfo);
                            quest.param("attrs", tmp);

                            client.sendQuest(quest, new FunctionalAnswerCallback() {
                                @Override
                                public void onAnswer(Answer answer, int errorCode) {
                                    String url = "";
                                    long size = 0;

                                    if (errorCode == okRet){
                                        url = rtmUtils.wantString(answer,"url");
                                        size = rtmUtils.wantLong(answer,"size");
                                        activeFileGateClient(endpoint, client);
                                    }
                                    callback.onResult(url, size, genRTMAnswer(answer,errorCode));
                                }
                            }, rtmConfig.globalFileQuestTimeoutSeconds);
                        }
                    }
                    else {
                        callback.onResult("", 0L, tokenanswer);
                    }
                }
        }, FileTokenType.OnlySave, 0);

    }
}
