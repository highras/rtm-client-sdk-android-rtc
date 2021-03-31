package com.example.rtvdemo;

import android.content.Context;
import android.util.Log;

import com.fpnn.sdk.ErrorCode;
import com.rtmsdk.RTMAudio;
import com.rtmsdk.RTMClient;
import com.rtmsdk.RTMStruct.AttrsStruct;
import com.rtmsdk.RTMStruct.AudioTextStruct;
import com.rtmsdk.RTMStruct.CheckResult;
import com.rtmsdk.RTMStruct.FileMessageType;
import com.rtmsdk.RTMStruct.GroupInfoStruct;
import com.rtmsdk.RTMStruct.HistoryMessage;
import com.rtmsdk.RTMStruct.HistoryMessageResult;
import com.rtmsdk.RTMStruct.MembersStruct;
import com.rtmsdk.RTMStruct.MessageType;
import com.rtmsdk.RTMStruct.ModifyTimeStruct;
import com.rtmsdk.RTMStruct.ProfanityType;
import com.rtmsdk.RTMStruct.PublicInfo;
import com.rtmsdk.RTMStruct.RTMAnswer;
import com.rtmsdk.RTMStruct.RTMAudioStruct;
import com.rtmsdk.RTMStruct.TranslateType;
import com.rtmsdk.RTMStruct.TranslatedInfo;
import com.rtmsdk.TranscribeLang;
import com.rtmsdk.TranslateLang;
import com.rtmsdk.UserInterface.IRTMCallback;
import com.rtmsdk.UserInterface.IRTMDoubleValueCallback;
import com.rtmsdk.UserInterface.IRTMEmptyCallback;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class TestClass {
    public static long peerUid = 101;
    public static long roomId = 100;
    public static long groupId = 100;
    public static byte sendMessgeType = 90;
    public static String setLang = "no";
    public static Map<Long, RTMClient> pushClients = new HashMap<>();
    public static Map<Long, String> pushUserTokens;
    public static File audioSave;

    public static long pid = 90000033;
    public static long loginUid = 9527;
    public static String token = "4065261c-f349-4209-bc72-cc6c253791a0";
    public static String dispatchEndpoint = "52.82.27.68:13325";
    public static RTMClient client = null;
    public static int lgonStatus = -1;
    public static Context mycontext;

    public static Map<String, CaseInterface> testMap;
    public static String roomBeizhu = " to room " + roomId;
    public static String groupBeizhu = " to group " + groupId;
    public static String userBeizhu = " to user " + peerUid;
//    public static byte[] audioData = null;
    public static File audioFile = null;
    public static byte[] rtmAudioData = null;
    public static byte[] audioData = null;
    public static byte[] videoData = null;
    public static byte[] piccontent = null;
    public static byte[] fileData = null;
    public static RTMAudioStruct audioStruct = null;
    public static long lastCloseTime = 0;
    boolean testunread = false;
    public enum MsgType {
        P2P,
        GROUP,
        ROOM,
        BROADCAST
    }

/*    public enum CaseType {
        CHAT,
        DATA,
        ROOM,
        GROUP,
        FRIEND,
        USERS,
        SYSTEM,
        MESSAGE,
        FILE,
        HISTORY,
        AUDIO
    }*/

    public static byte[] httpGetFile(String fileUrl) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true); // 同意输入流，即同意下载
            httpURLConnection.setUseCaches(false); // 不使用缓冲
            httpURLConnection.setRequestMethod("GET"); // 使用get请求
            httpURLConnection.setConnectTimeout(20 * 1000);
            httpURLConnection.setReadTimeout(20 * 1000);
            httpURLConnection.connect();

            int code = httpURLConnection.getResponseCode();

            if (code == 200) { // 正常响应
                InputStream inputStream = httpURLConnection.getInputStream();

                byte[] buffer = new byte[4096];
                int n = 0;
                while (-1 != (n = inputStream.read(buffer))) {
                    output.write(buffer, 0, n);
                }

                inputStream.close();
            }
            else {
                mylog.log("http return error " + code);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toByteArray();
    }

    public void addClients(long uid, RTMClient client){
        pushClients.put(uid, client);
    }

    public void startCase(String type) throws InterruptedException {
        if (testMap == null){
            mylog.log("rtmclient init error");
        return;
    }
        if (!testMap.containsKey(type))
            mylog.log("bad case type:" + type);
        else
            testMap.get(type).start();
    }

    public static void displayHistoryMessages(List<HistoryMessage> messages) {
        for (HistoryMessage hm : messages) {
            String str = "";
            if (hm.binaryMessage != null) {
                str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d,  binary message length :%d, attrs:%s, mtime:%d",
                        hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.binaryMessage.length, hm.attrs, hm.modifiedTime);
            } else {
                if (hm.messageType >= MessageType.IMAGEFILE && hm.messageType <= MessageType.NORMALFILE)
                    str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d,  fileinfo :%s, attrs:%s, mtime:%d",
                            hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.fileInfo.fileSize +" " + hm.fileInfo.url , hm.attrs, hm.modifiedTime);
                else
                    str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d,  message :%s, attrs:%s, mtime:%d",
                            hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.stringMessage, hm.attrs, hm.modifiedTime);
            }
            mylog.log(str);
        }
    }

    public void startAudioTest() {
//        client.sendAudio(peerUid,audioFile);
//        client.sendGroupAudio(groupId,audioFile);
//        TestClass.enterRoomSync();
        JSONObject lll = new JSONObject(){{
            try {
                put("userkey audio", "1111");
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }};
//        client.sendRoomFile(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                TestClass.outPutMsg(answer, "sendFile audio", TestClass.userBeizhu, mtime, messageId,false);
//            }
//        }, TestClass.roomId, FileMessageType.AUDIOFILE,TestClass.audioStruct.audioData,"",lll,TestClass.audioStruct);


        if (com.example.rtvdemo.TestClass.audioStruct == null)
            return;
        client.sendGroupFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupFile audio", com.example.rtvdemo.TestClass.groupBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.groupId, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioStruct.audioData,"",lll, com.example.rtvdemo.TestClass.audioStruct);
        if(true)
            return;

        client.sendRoomFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendFile audio", com.example.rtvdemo.TestClass.userBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.roomId, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioStruct.audioData,"",lll, com.example.rtvdemo.TestClass.audioStruct);

//        ModifyTimeStruct dd = client.sendAudio(peerUid,audioFile);
//        client.sendGroupAudio(groupId,audioFile);
//        if (testMap == null) {
//            mylog.log("rtmclient init error");
//            return;
//        }
//        AudioCase hh = (AudioCase) testMap.get("audio");
//        hh.sendAudio(data);
    }

    public void startCase() throws InterruptedException {
        for (com.example.rtvdemo.CaseInterface key : testMap.values())
            key.start();
    }

    public static void writeFile(byte[] data, File file)
    {
        try {
            FileOutputStream fos = new FileOutputStream(audioSave);
            fos.write(data,0,data.length);
            fos.flush();
            fos.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void loginRTM() {
        testMap = new HashMap<String, CaseInterface>() {
            {
                put("chat", new com.example.rtvdemo.ChatCase());
                put("data", new com.example.rtvdemo.DataCase());
                put("group", new com.example.rtvdemo.GroupCase());
                put("friend", new com.example.rtvdemo.FriendCase());
                put("room", new com.example.rtvdemo.RoomCase());
                put("file", new com.example.rtvdemo.FileCase());
                put("system", new com.example.rtvdemo.SystemCase());
                put("user", new com.example.rtvdemo.UserCase());
                put("history", new com.example.rtvdemo.HistoryCase());
                put("audio", new com.example.rtvdemo.AudioCase());
            }
        };

        RTMAnswer answer = client.login(token);
//        RTMAnswer answer = client.login(null);
//        RTMAnswer answer = client.login(token,TranslateLang.EN.getName(),null,"ipv4");
        lgonStatus = answer.errorCode;
        if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
            mylog.log(" " + loginUid + " login RTM success");
        else
            mylog.log(" " + loginUid + " login RTM error:" + answer.getErrInfo());

        for (final long uid : pushClients.keySet()) {
            final RTMClient loginClient = pushClients.get(uid);

            loginClient.login(new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    if (answer.errorCode == ErrorCode.FPNN_EC_OK.value()) {
                        mylog.log("user " + uid + " login success ");
//                            final List<Byte> unreadMessageTypes = new ArrayList<Byte>() {{
//                                add((byte)30);
//                                add((byte)32);
//                                add((byte)41);
//                                add((byte)66);
//                            }};
//
//                            Unread opi = loginClient.getSession();
//                            if (opi.errorCode == 0)
//                            {
//                                if (opi.p2pList.size() != 0){
//                                    UnreadNum groupunread = loginClient.getP2PUnread(new HashSet<>(opi.p2pList), unreadMessageTypes);
//                                    if (groupunread.errorCode == 0) {
//                                        for (String uid : groupunread.unreadInfo.keySet()) {
//                                            HistoryMessageResult hehe = loginClient.getP2PHistoryMessage(Long.valueOf(uid), true, groupunread.unreadInfo.get(uid), 0, 0,0,unreadMessageTypes);
//                                            for (RTMMessage kk: hehe.messages){
//                                                mylog.log(kk.getInfo());
//                                            }
//                                        }
//                                    }
//
//                                    loginClient.getP2PUnread(new IRTMCallback<Map<String, Integer>>() {
//                                        @Override
//                                        public void onResult(Map<String, Integer> unreadP2ps, RTMAnswer answer) {
//                                            if (answer.errorCode == 0) {
//                                                for (String gid : unreadP2ps.keySet()) {
//                                                    HistoryMessageResult hehe = loginClient.getP2PHistoryMessage(Long.valueOf(gid), true, unreadP2ps.get(gid), 0, 0, 0,unreadMessageTypes);
//                                                    for (RTMMessage kk: hehe.messages){
//                                                        mylog.log(kk.getInfo());
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }, new HashSet<>(opi.p2pList),unreadMessageTypes);
//                                }
//
//                                if (opi.groupList.size() != 0){
//                                    UnreadNum groupunread = loginClient.getGroupUnread(new HashSet<>(opi.groupList), unreadMessageTypes);
//                                    if (groupunread.errorCode == 0) {
//                                        for (String uid : groupunread.unreadInfo.keySet()) {
//                                            HistoryMessageResult hehe = loginClient.getGroupHistoryMessage(Long.valueOf(uid), true, groupunread.unreadInfo.get(uid), 0, 0,0,unreadMessageTypes);
//                                            for (RTMMessage kk: hehe.messages){
//                                                mylog.log(kk.getInfo());
//                                            }
//                                        }
//                                    }
//
//                                    loginClient.getGroupUnread(new IRTMCallback<Map<String, Integer>>() {
//                                        @Override
//                                        public void onResult(Map<String, Integer> unreadP2ps, RTMAnswer answer) {
//                                            if (answer.errorCode == 0) {
//                                                for (String gid : unreadP2ps.keySet()) {
//                                                    HistoryMessageResult hehe = loginClient.getGroupHistoryMessage(Long.valueOf(gid), true, unreadP2ps.get(gid), 0, 0, 0,unreadMessageTypes);
//                                                    for (RTMMessage kk: hehe.messages){
//                                                        mylog.log(kk.getInfo());
//                                                    }
//                                                }
//                                            }
//                                        }
//                                    }, new HashSet<>(opi.groupList),unreadMessageTypes);
//                                }
//                            }
                    } else {
                        mylog.log("user " + uid + " login failed " + answer.getErrInfo());
                    }
                }
            }, pushUserTokens.get(uid));
        }
    }


    public static byte[] fileToByteArray(File file) {
        byte[] data;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            data = baos.toByteArray();
            fis.close();
            baos.close();
        } catch (Exception e) {
            Log.e("rtmaudio","fileToByteArray error " + e.getMessage());
            return null;
        }
        return data;
    }

    public static void mySleep(int second) {
        try {
            Thread.sleep(second*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void mySleep1(int millsecond) {
        try {
            Thread.sleep(millsecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkClient() {
        return client != null;
    }


    public TestClass(long pid, long uid, String token, String dispatchEndpoint) {
        this.pid = pid;
        this.loginUid = uid;
        this.token = token;
        this.dispatchEndpoint = dispatchEndpoint;
    }

    public static void leaveRoomSync() {
        enterRoomSync(roomId);
    }

    public static void leaveRoomSync(final long roomId) {
        client.leaveRoom(new IRTMEmptyCallback(){
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer,"leaveroom" + roomId,"uid is " + loginUid);
            }
        },roomId);
        for (final long uid:pushClients.keySet()) {
            pushClients.get(uid).leaveRoom(new IRTMEmptyCallback(){
                @Override
                public void onResult(RTMAnswer answer) {
                    com.example.rtvdemo.TestClass.outPutMsg(answer,"leaveroom" + roomId,"uid is " + uid);
                }
            },roomId);
        }
    }


    public static void sayAllbye() {
        client.bye();
        for (final long uid:pushClients.keySet()) {
            pushClients.get(uid).bye();
        }
    }


    public static void enterRoomSync() {
        enterRoomSync(roomId);
    }

    public static void enterRoomSync(long roomid) {

            RTMAnswer answer = client.enterRoom(roomid);
            com.example.rtvdemo.TestClass.outPutMsg(answer,"enterroom" + roomid,"uid is " + loginUid);
            for (long uid:pushClients.keySet()) {
                answer = pushClients.get(uid).enterRoom(roomid);
                com.example.rtvdemo.TestClass.outPutMsg(answer,"enterroom" + roomid,"uid is " + uid);
            }
    }


    public static void enterRoomAsync() {
        enterRoomAsync(roomId);
    }

    public static void enterRoomAsync(final long roomid) {
        client.enterRoom(new IRTMEmptyCallback(){
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer,"enterroom" + roomid,"uid is " + loginUid);
            }
        },roomid);
        for (final long uid:pushClients.keySet()) {
            pushClients.get(uid).enterRoom(new IRTMEmptyCallback(){
                @Override
                public void onResult(RTMAnswer answer) {
                    com.example.rtvdemo.TestClass.outPutMsg(answer,"enterroom" + roomid,"uid is " + uid);
                }
            },roomid);
        }
    }


    public static boolean checkStatus() {
        if (lgonStatus != ErrorCode.FPNN_EC_OK.value()) {
            mylog.log("not available rtmclient");
            return false;
        }
        return true;
    }

    public static void asyncOutPutMsg(RTMAnswer answer, String method) {
        com.example.rtvdemo.TestClass.outPutMsg(answer, method, "", 0, 0L,false);
    }

    public static void asyncOutPutMsg(RTMAnswer answer, String method, String beizhu) {
        com.example.rtvdemo.TestClass.outPutMsg(answer, method, beizhu, 0, 0L,false);
    }

    public static void asyncOutPutMsg(RTMAnswer answer, String method, String beizhu, long mtime, long messageId) {
        com.example.rtvdemo.TestClass.outPutMsg(answer, method, beizhu, mtime, messageId,false);
    }


    public static void outPutMsg(RTMAnswer answer, String method) {
        com.example.rtvdemo.TestClass.outPutMsg(answer, method, "", 0, 0L,true);
    }

    public static void outPutMsg(RTMAnswer answer , String method, String beizhu) {
        com.example.rtvdemo.TestClass.outPutMsg(answer, method, beizhu, 0, 0L,true);
    }

    public static void outPutMsg(RTMAnswer answer, String method, String beizhu, long mtime, long messageId) {
        com.example.rtvdemo.TestClass.outPutMsg(answer, method, beizhu, mtime, messageId,true);
    }

    public static void outPutMsg(RTMAnswer answer, String method, String beizhu, long mtime, long messageId, boolean sync) {
        String syncType = "sync", msg = "";
        long xid = 0;
        if (!sync)
            syncType = "async";

        if (answer.errorCode == ErrorCode.FPNN_EC_OK.value()) {
            if (mtime > 0)
                msg = String.format("%s %s in %s successed, mtime is:%d, messageId is :%d", method, beizhu, syncType, mtime, messageId);
            else
                msg = String.format("%s %s in %s successed", method, beizhu, syncType);
        } else
            msg = String.format("%s %s in %s failed, errordes:%s", method, beizhu, syncType, answer.getErrInfo());
        mylog.log(msg);
    }
}

class AudioCase implements com.example.rtvdemo.CaseInterface
{
    public void start(){

    }
}

class ChatCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
//    RTMClient client = TestClass.pushClients.get(101L);
//    public static String textMessage = "{\"user\":{\"name\":\"alex\",\"age\":\"18\",\"isMan\":true}}";
    public static String textMessage = "chat test";
    public static String translateMessage = "fuck you";
    byte[] binaryData = {4,2,4};

    public JSONObject audioAtrrs = new JSONObject(){{
        try {

            put("userkey", "hahahah");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }};
//    audioAtrrs.

    String changeLang = TranslateLang.AR.getName();
    String lang = "en";

    JSONObject fileattrs = new JSONObject(){{
        try {
            put("mykey", "1111");
            put("mykey1", "2222");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }};

    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        mylog.log("start chat case\n");

        com.example.rtvdemo.TestClass.enterRoomSync();
//        TestClass.mySleep(1);
//        ClientEngine.stop();

//        TestClass.enterRoomSync(300);
//        TestClass.enterRoomSync(400);
        newInterface();
//        syncChatTest();
//        asyncChatTest();

        //黑名单发送测试
//        blackListSendTest();

        mylog.log("end chat case");
    }

    //------------------------[ Chat Demo ]-------------------------//
    void syncChatTest() {
        ModifyTimeStruct answer;

        answer = client.sendChat(com.example.rtvdemo.TestClass.peerUid, textMessage,null);
        com.example.rtvdemo.TestClass.outPutMsg(answer, "sendChat", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime, answer.messageId);

//        answer = client.sendCmd(TestClass.peerUid, textMessage);
//        TestClass.outPutMsg(answer, "sendCmd", TestClass.userBeizhu, answer.modifyTime,answer.messageId);
//
//        answer = client.sendMessage(TestClass.peerUid, TestClass.sendMessgeType, textMessage);
//        TestClass.outPutMsg(answer, "sendMessage", TestClass.userBeizhu, answer.modifyTime,answer.messageId);
//
//        answer = client.sendGroupChat(TestClass.groupId, textMessage);
//        TestClass.outPutMsg(answer, "sendGroupChat", TestClass.groupBeizhu, answer.modifyTime,answer.messageId);

//        answer = client.sendGroupCmd(TestClass.groupId, textMessage);
//        TestClass.outPutMsg(answer, "sendGroupCmd", TestClass.groupBeizhu, answer.modifyTime,answer.messageId);
//
//        answer = client.sendGroupMessage(TestClass.groupId, TestClass.sendMessgeType, textMessage);
//        TestClass.outPutMsg(answer, "sendGroupMessage", TestClass.groupBeizhu, answer.modifyTime,answer.messageId);
        if (true)
            return;


        AudioTextStruct iikk = client.audioToText(com.example.rtvdemo.TestClass.fileToByteArray(RTMAudio.getInstance().getRecordFile()), TranscribeLang.EN_US.getName(),"AMR_WB",16000);
        mylog.log("audioToText sync checkResult result " + iikk.text);

        CheckResult ooohh = client.audioCheck(com.example.rtvdemo.TestClass.fileToByteArray(RTMAudio.getInstance().getRecordFile()), TranscribeLang.EN_US.getName());
        mylog.log("audioCheck sync checkResult result " + ooohh.result);

//        answer = client.sendChat(TestClass.peerUid, translateMessage);
//        TestClass.outPutMsg(answer, "sendGroupChat", TestClass.groupBeizhu, answer.modifyTime,answer.messageId);

        if (true)
            return;
            answer = client.sendChat(com.example.rtvdemo.TestClass.peerUid, textMessage,null);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendChat", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendGroupChat(com.example.rtvdemo.TestClass.groupId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupChat", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendRoomChat(com.example.rtvdemo.TestClass.roomId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomChat", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendCmd(com.example.rtvdemo.TestClass.peerUid, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendCmd", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendGroupCmd(com.example.rtvdemo.TestClass.groupId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupCmd", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendRoomCmd(com.example.rtvdemo.TestClass.roomId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomCmd", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendMessage(com.example.rtvdemo.TestClass.peerUid, com.example.rtvdemo.TestClass.sendMessgeType, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendRoomMessage(com.example.rtvdemo.TestClass.roomId, com.example.rtvdemo.TestClass.sendMessgeType, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendGroupMessage(com.example.rtvdemo.TestClass.groupId, com.example.rtvdemo.TestClass.sendMessgeType, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupMessage", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime,answer.messageId);

//binary
        answer = client.sendMessage(com.example.rtvdemo.TestClass.peerUid, com.example.rtvdemo.TestClass.sendMessgeType, com.example.rtvdemo.TestClass.piccontent);
        com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage binary", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime,answer.messageId);

        answer = client.sendRoomMessage(com.example.rtvdemo.TestClass.roomId, com.example.rtvdemo.TestClass.sendMessgeType, com.example.rtvdemo.TestClass.piccontent);
        com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage binary", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

        answer = client.sendGroupMessage(com.example.rtvdemo.TestClass.groupId, com.example.rtvdemo.TestClass.sendMessgeType, com.example.rtvdemo.TestClass.piccontent);
        com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage binary", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime,answer.messageId);


            //增值服务测试
//            TranslatedInfo transInfo = client.translate(translateMessage, lang,TranslateType.Chat,ProfanityType.Censor);
            TranslatedInfo transInfo = client.translate(translateMessage, lang,null,null);
            String beizhu = "sourceText:" + transInfo.sourceText + " sourceLang:" + transInfo.source + " target:" + transInfo.targetText + " targetLang:" + transInfo.target;
            mylog.log("translate result is:" + beizhu);

            client.setTranslatedLanguage(com.example.rtvdemo.TestClass.setLang);
            mylog.log("setTranslatedLanguage :" + lang + "ok");

            transInfo = client.translate(translateMessage, lang);
            beizhu = "sourceText:" + transInfo.sourceText + " sourceLang:" + transInfo.source + " target:" + transInfo.targetText + " targetLang:" + transInfo.target;
            ;
            mylog.log("translate result is:" + beizhu);
    }

    void asyncChatTest() {
        client.sendRoomChat(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomChat", com.example.rtvdemo.TestClass.roomBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.roomId, textMessage);
        if (true)
            return;
//        for (int i =0;i<1;i++) {
//            client.sendRoomChat(new IRTMDoubleValueCallback<Long, Long>() {
//                @Override
//                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                    TestClass.outPutMsg(answer, "sendRoomChat", TestClass.roomBeizhu, mtime, messageId, false);
//                }
//            }, TestClass.roomId, textMessage);
//
//            client.sendRoomCmd(new IRTMDoubleValueCallback<Long, Long>() {
//                @Override
//                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                    TestClass.outPutMsg(answer, "sendRoomCmd", TestClass.roomBeizhu, mtime, messageId, false);
//                }
//            }, TestClass.roomId, textMessage);
//
//            client.sendRoomMessage(new IRTMDoubleValueCallback<Long, Long>() {
//                @Override
//                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                    TestClass.outPutMsg(answer, "sendRoomMessage", TestClass.roomBeizhu, mtime, messageId, false);
//                }
//            }, TestClass.roomId, TestClass.sendMessgeType, textMessage);
//
//            client.sendRoomFile(new IRTMDoubleValueCallback<Long, Long>() {
//                @Override
//                public void onResult(Long time, Long messageId, RTMAnswer answer) {
//                    TestClass.asyncOutPutMsg(answer, "sendRoomFile normal file", TestClass.roomBeizhu, time, messageId);
//                }
//            }, TestClass.roomId, FileMessageType.NORMALFILE, TestClass.fileData, "nihao.txt", fileattrs);
//
//
//            client.sendGroupChat(new IRTMDoubleValueCallback<Long,Long>() {
//                @Override
//                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                    TestClass.outPutMsg(answer, "sendGroupChat", TestClass.groupBeizhu, mtime, messageId,false);
//                }
//            }, TestClass.groupId, textMessage);
//
//
//            client.sendGroupCmd(new IRTMDoubleValueCallback<Long,Long>() {
//                @Override
//                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                    TestClass.outPutMsg(answer, "sendGroupCmd", TestClass.groupBeizhu, mtime, messageId,false);
//                }
//            }, TestClass.groupId, textMessage);
//
//            client.sendGroupMessage(new IRTMDoubleValueCallback<Long,Long>() {
//                @Override
//                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                    TestClass.outPutMsg(answer, "sendGroupMessage", TestClass.groupBeizhu, mtime, messageId,false);
//                }
//            }, TestClass.groupId, TestClass.sendMessgeType, textMessage);
//
//            client.sendGroupFile(new IRTMDoubleValueCallback<Long,Long>() {
//                @Override
//                public void onResult(Long time,Long messageId, RTMAnswer answer) {
//                    TestClass.asyncOutPutMsg(answer,"sendGroupFile image", TestClass.groupBeizhu, time,messageId);
//                }
//            },TestClass.groupId, FileMessageType.IMAGEFILE, TestClass.fileData, "nihao.txt",null);
//            TestClass.mySleep1(500);
//        }


//        String klk = null;
//        mylog.log(klk.toString());

        client.sendChat(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendchat", com.example.rtvdemo.TestClass.userBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.peerUid, textMessage);


        client.sendCmd(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendCmd", com.example.rtvdemo.TestClass.userBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.peerUid, textMessage);

//        client.sendAudio(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                TestClass.outPutMsg(answer, "sendAudio", TestClass.userBeizhu, mtime, messageId,false);
//            }
//        }, TestClass.peerUid, TestClass.audioFile);

        client.sendMessage(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage", com.example.rtvdemo.TestClass.userBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.peerUid, com.example.rtvdemo.TestClass.sendMessgeType, textMessage);

        client.sendMessage(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage binary", com.example.rtvdemo.TestClass.userBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.peerUid, com.example.rtvdemo.TestClass.sendMessgeType, binaryData);

        if (true)
            return;
        //group
        client.sendGroupChat(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupChat", com.example.rtvdemo.TestClass.groupBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.groupId, textMessage);


        client.sendGroupCmd(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupCmd", com.example.rtvdemo.TestClass.groupBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.groupId, textMessage);

//        client.sendGroupAudio(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                TestClass.outPutMsg(answer, "sendGroupAudio", TestClass.groupBeizhu, mtime, messageId,false);
//            }
//        }, TestClass.groupId, TestClass.audioFile);


        client.sendGroupMessage(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupMessage", com.example.rtvdemo.TestClass.groupBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.groupId, com.example.rtvdemo.TestClass.sendMessgeType, textMessage);

        client.sendGroupMessage(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupMessage  binary", com.example.rtvdemo.TestClass.groupBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.groupId, com.example.rtvdemo.TestClass.sendMessgeType, binaryData);

        //room
        client.sendRoomChat(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomChat", com.example.rtvdemo.TestClass.roomBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.roomId, textMessage);

        client.sendRoomCmd(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomCmd", com.example.rtvdemo.TestClass.roomBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.roomId, textMessage);


//        client.sendRoomAudio(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                TestClass.outPutMsg(answer, "sendRoomAudio", TestClass.roomBeizhu, mtime, messageId,false);
//            }
//        }, TestClass.roomId, TestClass.audioFile);

        client.sendRoomMessage(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomMessage", com.example.rtvdemo.TestClass.roomBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.roomId, com.example.rtvdemo.TestClass.sendMessgeType, textMessage);

        client.sendRoomMessage(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomMessage binary", com.example.rtvdemo.TestClass.roomBeizhu, mtime, messageId,false);
            }
        }, com.example.rtvdemo.TestClass.roomId, com.example.rtvdemo.TestClass.sendMessgeType, binaryData);

        if (true)
            return;


        TranslatedInfo ll = client.translate(null,lang);
        //增值服务测试
        client.translate(new IRTMCallback<TranslatedInfo>() {
            @Override
            public void onResult(TranslatedInfo transInfo, RTMAnswer answer) {
                String beizhu = "";
                if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
                    beizhu = "sourceText:" + transInfo.sourceText + " sourceLang:" + transInfo.source + " target:" + transInfo.targetText + " targetLang:" + transInfo.target;;
                com.example.rtvdemo.TestClass.outPutMsg(answer, "translate", beizhu, 0, 0,false);
            }
        }, translateMessage, lang, TranslateType.Chat, ProfanityType.Censor);

        client.setTranslatedLanguage(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "setTranslatedLanguage", com.example.rtvdemo.TestClass.setLang, 0, 0,false);
            }
        }, changeLang);
        com.example.rtvdemo.TestClass.mySleep(2);

        client.translate(new IRTMCallback<TranslatedInfo>() {
            @Override
            public void onResult(TranslatedInfo transInfo, RTMAnswer answer) {
                String beizhu = "";
                if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
                    beizhu = "sourceText:" + transInfo.sourceText + " sourceLang:" + transInfo.source + " target:" + transInfo.targetText + " targetLang:" + transInfo.target;;
                com.example.rtvdemo.TestClass.outPutMsg(answer, "translate", beizhu, 0, 0,false);
            }
        }, translateMessage, lang);
        com.example.rtvdemo.TestClass.mySleep(2);
    }

    void newInterface(){
        CheckResult pp1 = client.textCheck("System Notification");
        mylog.log("textCheck sync result is " + pp1.result);

        client.textCheck(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log(("textCheck async checkResult text " + checkResult.text));
                }
            }
        },"bitch you");


//        for (int ij = 0; ij<200;ij++) {
//            for (final long uid : TestClass.pushClients.keySet()) {
//                TestClass.pushClients.get(uid).closeRTM();
//                TestClass.pushClients = null;
//            }
//            for (Long id : TestClass.pushUserTokens.keySet()) {
//                RTMClient rtmUser = new RTMClient(TestClass.dispatchEndpoint, TestClass.pid, id, new RTMExampleQuestProcessor());
//                TestClass.pushClients.put(id, rtmUser);
//            }
//        }
        if(true)
            return;

        for (int i = 0; i<200;i++) {
            for (final long uid : com.example.rtvdemo.TestClass.pushClients.keySet()) {
                com.example.rtvdemo.TestClass.pushClients.get(uid).bye();
            }
            com.example.rtvdemo.TestClass.mySleep1(500);
            for (final long uid : com.example.rtvdemo.TestClass.pushClients.keySet()) {
                com.example.rtvdemo.TestClass.pushClients.get(uid).login(new IRTMEmptyCallback() {
                    @Override
                    public void onResult(RTMAnswer answer) {
                        if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
                            mylog.log("user " + uid + " login success ");
                    }
                }, com.example.rtvdemo.TestClass.pushUserTokens.get(uid));
            }
            com.example.rtvdemo.TestClass.mySleep1(500);
            com.example.rtvdemo.TestClass.enterRoomSync();
        }
        if (true)
            return;

        HashSet messageTypes = new HashSet<Integer>(){{add(30);}};
        HashSet messageTypes1 = new HashSet<Integer>(){{add(108);}};
////        HashSet messageTypes1 = new HashSet<Integer>(){{add(98);add(108);}};
//        HashSet removeMessageTypes = new HashSet<Integer>(){{add(30);}};
//
//        DevicePushOption deviceOptions;
//        RTMAnswer kkk;
//        client.addDevicePushOption(new IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMAnswer answer) {
//                mylog.log("addDevicePushOption result " + answer.getErrInfo());
//            }
//        },0, 100, messageTypes);
////        client.removeDevicePushOption(0,100, removeMessageTypes);
//
//        deviceOptions = client.getDevicePushOption();
//        mylog.log(deviceOptions.toString());
//
//       kkk = client.addDevicePushOption(0,100,messageTypes);
//        mylog.log("addDevicePushOption result " + kkk.getErrInfo());
//        if (true)
//            return;
//
////        //group
//       kkk = client.addDevicePushOption(1,100, messageTypes);
//        mylog.log("addDevicePushOption result " + kkk.getErrInfo());
//
//        deviceOptions = client.getDevicePushOption();
//        mylog.log(deviceOptions.toString());
//
//        kkk = client.addDevicePushOption(0,100, messageTypes1);
//        mylog.log("addDevicePushOption result " + kkk.getErrInfo());
//
//        deviceOptions = client.getDevicePushOption();
//        mylog.log(deviceOptions.toString());
//
////
////        DevicePushOption deviceOptions = client.getDevicePushOption();
////        mylog.log(deviceOptions.toString());
////
////        kkk = client.addDevicePushOption(1,111,null);
////        mylog.log("addDevicePushOption result " + kkk.getErrInfo());
////
////       deviceOptions = client.getDevicePushOption();
////        mylog.log(deviceOptions.toString());
////
//        client.removeDevicePushOption(1,100, removeMessageTypes);
//
//        deviceOptions = client.getDevicePushOption();
//        mylog.log(deviceOptions.toString());
////


//        //p2p
//        client.addDevicePushOption(new IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMAnswer answer) {
//                mylog.log("addDevicePushOption result " + answer.getErrInfo());
//            }
//        },0, 101, messageTypes);
//
//        client.addDevicePushOption(new IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMAnswer answer) {
//                mylog.log("addDevicePushOption result " + answer.getErrInfo());
//            }
//        },1, 100, messageTypes1);
//
//        TestClass.mySleep(1);
//
//       client.getDevicePushOption(new IRTMCallback<DevicePushOption>() {
//           @Override
//           public void onResult(DevicePushOption devicePushOption, RTMAnswer answer) {
//               mylog.log("getDevicePushOption result " + devicePushOption.toString());
//           }
//       });
////
//        TestClass.mySleep(5);
//
//
//        client.removeDevicePushOption(new IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMAnswer answer) {
//                mylog.log("removeDevicePushOption result " + answer.getErrInfo());
//
//            }
//        },0, 101, removeMessageTypes);
//
//        TestClass.mySleep(1);
//
//
//        client.getDevicePushOption(new IRTMCallback<DevicePushOption>() {
//            @Override
//            public void onResult(DevicePushOption devicePushOption, RTMAnswer answer) {
//                mylog.log("getDevicePushOption result " + devicePushOption.toString());
//            }
//        });
//        if (true)
//            return;
//
//
//        CheckResult lloo = client.imageCheck(TestClass.piccontent);
//        mylog.log("imageCheck sync checkResult result " + lloo.result);
//
//        client.imageCheck(new IRTMCallback<CheckResult>() {
//            @Override
//            public void onResult(CheckResult checkResult, RTMAnswer answer) {
//                mylog.log(" imageCheck " + answer.getErrInfo());
//                if (answer.errorCode == 0){
//                    mylog.log("imageCheck async checkResult result " + checkResult.result);
//                }
//            }
//        },TestClass.piccontent);

//        HashSet rids = new HashSet<Long>(){{add(TestClass.roomId);add(100L);add(200L);}};
//        MemberCount hehehaha = client.getRoomCount(rids);
//        for (Map.Entry<Long,Integer> rid: hehehaha.memberCounts.entrySet()){
//            mylog.log("room " + rid.getKey() + " membercount is " + rid.getValue());
//        }
//
//        client.getRoomCount(new IRTMCallback<Map<Long,Integer>>() {
//            @Override
//            public void onResult(Map<Long,Integer> mems, RTMAnswer answer) {
//                for (Map.Entry<Long,Integer> rid: mems.entrySet()){
//                    mylog.log("room " + rid.getKey() + " membercount is " + rid.getValue());
//                }            }
//        },rids);

//        client.getRoomMembers(new IRTMCallback<HashSet<Long>>() {
//            @Override
//            public void onResult(HashSet<Long> longs, RTMAnswer answer) {
//                mylog.log("room getRoomMembers is " + longs.toString());
//            }
//        },TestClass.roomId);

        if (true)
            return;


        AudioTextStruct iikk = client.audioToText(com.example.rtvdemo.TestClass.rtmAudioData, TranscribeLang.ZH_CN.getName(),"AMR_WB",16000);
        mylog.log("audioToText sync checkResult result " + iikk.text);


        CheckResult ooohh = client.audioCheck(com.example.rtvdemo.TestClass.rtmAudioData, TranscribeLang.EN_US.getName());
        mylog.log("audioCheck sync checkResult result " + ooohh.result);

        AudioTextStruct iikk2 = client.audioToTextURL("https://s3.cn-northwest-1.amazonaws.com.cn/rtm-filegated-test-cn-northwest-1/90000033/20201022/100/a92adc98ec315c2a52b51f248c53c233.amr", TranscribeLang.ZH_CN.getName());
        mylog.log("audioToTextURL sync checkResult result " + iikk2.text + " " + iikk2.lang);

        client.audioToTextURL(new IRTMCallback<AudioTextStruct>() {
            @Override
            public void onResult(AudioTextStruct audioTextStruct, RTMAnswer answer) {
                mylog.log("audioToTextURL async checkResult result " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("audioToTextURL async text " + audioTextStruct.text + " " + audioTextStruct.lang);
                }

            }
        },"https://s3.cn-northwest-1.amazonaws.com.cn/rtm-filegated-test-cn-northwest-1/90000033/20201022/100/a92adc98ec315c2a52b51f248c53c233.amr", TranscribeLang.ZH_CN.getName());

        AudioTextStruct iikk1 = client.audioToText(com.example.rtvdemo.TestClass.rtmAudioData, TranscribeLang.ZH_CN.getName());
        mylog.log("audioToText sync checkResult result " + iikk1.text + " " + iikk1.lang);

        client.audioToText(new IRTMCallback<AudioTextStruct>() {
            @Override
            public void onResult(AudioTextStruct audioTextStruct, RTMAnswer answer) {
                mylog.log("audioToText async checkResult result " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("audioToText async text " + audioTextStruct.text + " " + audioTextStruct.lang);
                }

            }
        }, com.example.rtvdemo.TestClass.rtmAudioData, TranscribeLang.ZH_CN.getName());

        com.example.rtvdemo.TestClass.mySleep(5);

        CheckResult pp = client.textCheck("fuck you");
        mylog.log("textCheck sync result is " + pp.result);

        client.textCheck(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log(("textCheck async checkResult text " + checkResult.text));
                }
            }
        },"bitch you");


        CheckResult opl = client.imageCheckURL("https://image.baidu.com/search/detail?ct=503316480&z=0&ipn=d&word=%E8%94%A1%E8%8B%B1%E6%96%87&step_word=&hs=0&pn=31&spn=0&di=100870&pi=0&rn=1&tn=baiduimagedetail&is=0%2C0&istype=2&ie=utf-8&oe=utf-8&in=&cl=2&lm=-1&st=-1&cs=3211985769%2C881571615&os=2143750784%2C4271208400&simid=4224450280%2C753660985&adpicid=0&lpn=0&ln=794&fr=&fmq=1603262060531_R&fm=result&ic=&s=undefined&hd=&latest=&copyright=&se=&sme=&tab=0&width=&height=&face=undefined&ist=&jit=&cg=&bdtype=0&oriquery=&objurl=http%3A%2F%2Fimg1.cache.netease.com%2Fcatchpic%2FC%2FCB%2FCBFA4A040907B972B6604449EC875E10.jpg&fromurl=ippr_z2C%24qAzdH3FAzdH3F45gjy_z%26e3B8mn_z%26e3Bv54AzdH3F8mAzdH3Fac80AzdH3F88AzdH3FBNbVGcBmaadcnBaH_z%26e3Bip4s%23u654%3D6jsjewgp&gsm=20&rpstart=0&rpnum=0&islist=&querylist=&force=undefined");
        mylog.log("imageCheckURL sync result is " + opl.result);

        client.imageCheckURL(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(" imageCheckURL " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("imageCheckURL async checkResult result " + checkResult.result);
                }
            }
        },"https://image.baidu.com/search/detail?ct=503316480&z=0&ipn=d&word=%E8%94%A1%E8%8B%B1%E6%96%87&step_word=&hs=0&pn=31&spn=0&di=100870&pi=0&rn=1&tn=baiduimagedetail&is=0%2C0&istype=2&ie=utf-8&oe=utf-8&in=&cl=2&lm=-1&st=-1&cs=3211985769%2C881571615&os=2143750784%2C4271208400&simid=4224450280%2C753660985&adpicid=0&lpn=0&ln=794&fr=&fmq=1603262060531_R&fm=result&ic=&s=undefined&hd=&latest=&copyright=&se=&sme=&tab=0&width=&height=&face=undefined&ist=&jit=&cg=&bdtype=0&oriquery=&objurl=http%3A%2F%2Fimg1.cache.netease.com%2Fcatchpic%2FC%2FCB%2FCBFA4A040907B972B6604449EC875E10.jpg&fromurl=ippr_z2C%24qAzdH3FAzdH3F45gjy_z%26e3B8mn_z%26e3Bv54AzdH3F8mAzdH3Fac80AzdH3F88AzdH3FBNbVGcBmaadcnBaH_z%26e3Bip4s%23u654%3D6jsjewgp&gsm=20&rpstart=0&rpnum=0&islist=&querylist=&force=undefined");

        CheckResult oooll = client.imageCheck(com.example.rtvdemo.TestClass.piccontent);
        mylog.log("imageCheck sync checkResult result " + oooll.result);

        client.imageCheck(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(" imageCheck " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("imageCheck async checkResult result " + checkResult.result);
                }
            }
        }, com.example.rtvdemo.TestClass.piccontent);


        CheckResult hehe = client.audioCheckURL("https://emumo.xiami.com/play?ids=/song/playlist/id/1795782490/object_name/default/object_id/0#loaded", TranscribeLang.EN_US.getName());
        mylog.log("sync checkResult result " + hehe.getErrInfo());

        client.audioCheckURL(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(" audioCheckURL " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("audioCheckURL async checkResult result " + checkResult.result);
                }
            }
        },"https://emumo.xiami.com/play?ids=/song/playlist/id/1795782490/object_name/default/object_id/0#loaded", TranscribeLang.EN_US.getName());


        ooohh = client.audioCheck(com.example.rtvdemo.TestClass.rtmAudioData, TranscribeLang.EN_US.getName());
        mylog.log("audioCheck sync checkResult result " + ooohh.result);

        client.audioCheck(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(" audioCheck " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("audioCheck async checkResult result " + checkResult.result);
                }
            }
        }, com.example.rtvdemo.TestClass.rtmAudioData, TranscribeLang.EN_US.getName());


        CheckResult hehe1 = client.videoCheckURL("https://www.bilibili.com/video/BV1ry4y1r7zV?spm_id_from=333.851.b_7265706f7274466972737431.7");
        mylog.log("videoCheckURL sync checkResult result " + hehe1.result);

        client.videoCheckURL(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(" audioCheck " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("videoCheckURL async checkResult result " + checkResult.result);
                }
            }
        },"https://www.bilibili.com/video/BV1ry4y1r7zV?spm_id_from=333.851.b_7265706f7274466972737431.7");
        com.example.rtvdemo.TestClass.mySleep(2);


        CheckResult ooohhtt = client.videoCheck(com.example.rtvdemo.TestClass.videoData,"videoDemo.mp4");
        mylog.log("audioCheck sync checkResult result " + ooohhtt.result);

        client.videoCheck(new IRTMCallback<CheckResult>() {
            @Override
            public void onResult(CheckResult checkResult, RTMAnswer answer) {
                mylog.log(" videoCheck " + answer.getErrInfo());
                if (answer.errorCode == 0){
                    mylog.log("videoCheck async checkResult result " + checkResult.result);
                }
            }
        }, com.example.rtvdemo.TestClass.videoData,"videoDemo.mp4");


        iikk = client.audioToText(com.example.rtvdemo.TestClass.audioData, TranscribeLang.ZH_CN.getName(),"rtm",16000);
        mylog.log("audioToText sync checkResult result " + iikk.text);

        client.audioToText(new IRTMCallback<AudioTextStruct>() {
            @Override
            public void onResult(AudioTextStruct audioTextStruct, RTMAnswer answer) {
                mylog.log("audioToText sync checkResult result " + answer.getErrInfo());

            }
        }, com.example.rtvdemo.TestClass.audioData, TranscribeLang.ZH_CN.getName(),"rtm",16000);

        com.example.rtvdemo.TestClass.mySleep(5);

        HashSet<Long> gids = new HashSet<Long>(){{
            add(200L);
            add(300L);
            add(400L);
        }};

        HashSet<Long> uids = new HashSet<Long>(){{
            add(100L);
            add(101L);
        }};

        client.setUserInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set user100 info " + answer.getErrInfo());
            }
        },"100 publicinfo","100 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);

        com.example.rtvdemo.TestClass.pushClients.get(101L).setUserInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set user101 info " + answer.getErrInfo());
            }
        },"101 publicinfo","101 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);


        client.getUserPublicInfo(new IRTMCallback<Map<String, String>>() {
            @Override
            public void onResult(Map<String, String> infoMap, RTMAnswer answer) {
                mylog.log("getUserPublicInfo " + answer.getErrInfo());
                if (answer.errorCode  ==0){
                    JSONObject kk = new JSONObject(infoMap);
                    mylog.log("users info is " + kk.toString());
                }
            }
        },uids);
        com.example.rtvdemo.TestClass.mySleep(1);

        client.setGroupInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set group 200 info " + answer.getErrInfo());
            }
        },200,"group 200 publicinfo","group 200 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);

        client.setGroupInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set group 300 info " + answer.getErrInfo());
            }
        },300,"group 300 publicinfo","group 300 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);

        client.setGroupInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set group 400 info " + answer.getErrInfo());
            }
        },400,"group 400 publicinfo"," group 400 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);


//        client.getGroupsOpeninfo(new IRTMCallback<Map<String, String>>() {
//            @Override
//            public void onResult(Map<String, String> infoMap, RTMAnswer answer) {
//                mylog.log("getGroupsOpeninfo " + answer.getErrInfo());
//                if (answer.errorCode  ==0){
//                    JSONObject kk = new JSONObject(infoMap);
//                    mylog.log("groups info is " + kk.toString());
//                }
//            }
//        },gids);

        com.example.rtvdemo.TestClass.mySleep(1);

        client.setRoomInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set room 200 info " + answer.getErrInfo());
            }
        },200,"room 200 publicinfo","room 200 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);

        client.setRoomInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set room 300 info " + answer.getErrInfo());
            }
        },300,"room 300 publicinfo","room 300 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);

        client.setRoomInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("set room 400 info " + answer.getErrInfo());
            }
        },400,"room 400 publicinfo","room 400 privateinfo");
        com.example.rtvdemo.TestClass.mySleep(1);


        client.getRoomsOpeninfo(new IRTMCallback<Map<String, String>>() {
            @Override
            public void onResult(Map<String, String> infoMap, RTMAnswer answer) {
                mylog.log("getRoomsOpeninfo " + answer.getErrInfo());
                if (answer.errorCode  ==0){
                    JSONObject kk = new JSONObject(infoMap);
                    mylog.log("rooms info is " + kk.toString());
                }
            }
        },gids);

        com.example.rtvdemo.TestClass.mySleep(5);
    }

    void blackListSendTest() {
        final String beizhu = "to user " + com.example.rtvdemo.TestClass.loginUid;
        long toUid = com.example.rtvdemo.TestClass.loginUid;
        RTMClient client = com.example.rtvdemo.TestClass.pushClients.get(com.example.rtvdemo.TestClass.peerUid);
        client.sendChat(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendchat", beizhu, mtime, messageId,false);
            }
        }, toUid, textMessage);


        client.sendCmd(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendCmd", beizhu, mtime, messageId,false);
            }
        }, toUid, textMessage);

        client.sendMessage(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage", beizhu, mtime, messageId,false);
            }
        }, toUid, com.example.rtvdemo.TestClass.sendMessgeType, textMessage);


//        client.sendAudio(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                TestClass.outPutMsg(answer, "sendAudio", beizhu, mtime, messageId,false);
//            }
//        }, toUid, TestClass.audioFile);
    }
}

interface CaseInterface {
    void start() throws InterruptedException;
}

class DataCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;

    public void start() {
        IRTMEmptyCallback codeback = new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                mylog.log("answer");
            }
        };
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }

            mylog.log("=========== Begin set user data ===========");

            Setdata("key 1", "value 1");
            mylog.log("=========== Begin get user data ===========");
            getData("key 1");
            com.example.rtvdemo.TestClass.mySleep(1);
            getData("key 2");

            mylog.log("=========== Begin delete one of user data ===========");

            deleteData("key 2");

            mylog.log("=========== Begin get user data after delete action ===========");

            getData("key 1");
            com.example.rtvdemo.TestClass.mySleep(1);
            getData("key 2");

            mylog.log("=========== User logout ===========");

            client.bye();

            mylog.log("=========== User relogin ===========");
            com.example.rtvdemo.TestClass.mySleep(1);

            client.login(com.example.rtvdemo.TestClass.token);

            mylog.log("=========== Begin get user data after relogin ===========");

            getData("key 1");
            com.example.rtvdemo.TestClass.mySleep(1);
            getData("key 2");
    }


    void Setdata(String key, String value){
        RTMAnswer answer = client.dataSet(key, value);
        com.example.rtvdemo.TestClass.outPutMsg(answer, "dataSet", com.example.rtvdemo.TestClass.userBeizhu);

        client.dataSet(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
                    mylog.log("dataSet async success ");
                else
                    mylog.log("dataSet async failed answer:" + answer.getErrInfo());
            }
        },key, value);
        com.example.rtvdemo.TestClass.mySleep(1);
    }

    void getData(String key){

        RTMAnswer answer = client.dataGet(key);
        com.example.rtvdemo.TestClass.outPutMsg(answer, "dataGet", com.example.rtvdemo.TestClass.userBeizhu);

        client.dataGet(new IRTMCallback<String>() {
            @Override
            public void onResult(String value, RTMAnswer answer) {
                if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
                    mylog.log("dataGet async success value is:" + value);
                else
                    mylog.log("dataGet async failed answer:" + answer.getErrInfo());
            }
        },key);
    }

    void deleteData(String key){
        RTMAnswer answer = client.dataDelete(key);
        com.example.rtvdemo.TestClass.outPutMsg(answer, "dataDelete", com.example.rtvdemo.TestClass.userBeizhu);
        client.dataDelete(key, new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
                    mylog.log("dataDelete async success ");
                else
                    mylog.log("dataDelete async failed answer:" + answer.getErrInfo());
            }
        });
    }
}

class FriendCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
    HashSet<Long> uids = new HashSet<Long>() {{
        add(123456L);
        add(234567L);
    }};

    final HashSet<Long> blacks = new HashSet<Long>() {{
        add(101L);
    }};

    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }

        mylog.log("start friend case\n");
        syncFriendTest();
        asyncFriendTest();
        mylog.log("end friend case\n");

    }

    void syncFriendTest(){
            RTMAnswer answer = client.addFriends(uids);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "addFriends", uids.toString());

            MembersStruct answer1 = client.getFriends();
            com.example.rtvdemo.TestClass.outPutMsg(answer, "getFriends", answer1.toString());

            answer = client.deleteFriends(uids);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "deleteFriends", uids.toString());

            answer1 = client.getFriends();
            com.example.rtvdemo.TestClass.outPutMsg(answer, "getFriends", answer1.toString());

            //黑名单
            answer = client.addBlacklist(blacks);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "addBlacklist", blacks.toString());

            answer1 = client.getBlacklist();
            com.example.rtvdemo.TestClass.outPutMsg(answer, "getBlacklist", answer1.toString());

            answer = client.delBlacklist(blacks);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "delBlacklist", blacks.toString());

            answer1 = client.getBlacklist();
            com.example.rtvdemo.TestClass.outPutMsg(answer, "getBlacklist", answer1.toString());
    }

    void asyncFriendTest(){
        client.addFriends(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "addFriends", uids.toString());
            }
        },uids);

        client.getFriends(new IRTMCallback<HashSet<Long>>() {
            @Override
            public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "getFriends", longs!=null?longs.toString():"");
            }
        });

        client.deleteFriends(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "deleteFriends", uids.toString());
            }
        },uids);

        client.getFriends(new IRTMCallback<HashSet<Long>>() {
            @Override
            public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "getFriends", longs!=null?longs.toString():"");
            }
        });

        //黑名单
        client.addBlacklist(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "addBlacklist", blacks.toString());
            }
        },blacks);

        client.getBlacklist(new IRTMCallback<HashSet<Long>>() {
            @Override
            public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "getBlacklist", longs!=null?longs.toString():"");
            }
        });

        client.delBlacklist(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "delBlacklist", blacks.toString());
            }
        },blacks);
        com.example.rtvdemo.TestClass.mySleep(3);

        client.getBlacklist(new IRTMCallback<HashSet<Long>>() {
            @Override
            public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer, "getBlacklist", longs!=null?longs.toString():"");
            }
        });
    }
}

class GroupCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
    long groupId = com.example.rtvdemo.TestClass.groupId;
    final HashSet<Long> uids = new HashSet<Long>() {{
        add(9988678L);
        add(9988789L);
    }};

    final HashSet<Long> gids = new HashSet<Long>() {{
        add(100L);
        add(200L);
    }};

    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        mylog.log("Begin group test case\n");
        syncGroupTest();
        asyncGroupTest();
        com.example.rtvdemo.TestClass.mySleep(5);
        mylog.log("End group test case\n");
    }

    void syncGroupTest(){
            MembersStruct answer = client.getGroupMembers(groupId);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "getGroupMembers", answer.toString());

            RTMAnswer ret = client.addGroupMembers(groupId, uids);
            com.example.rtvdemo.TestClass.outPutMsg(ret, "addGroupMembers");

            ret = client.deleteGroupMembers(groupId, uids);
            com.example.rtvdemo.TestClass.outPutMsg(ret, "deleteGroupMembers");

            GroupInfoStruct info = client.getGroupPublicInfo(groupId);
            com.example.rtvdemo.TestClass.outPutMsg(info, "getGroupPublicInfo", info.toString());

            PublicInfo ll= client.getGroupsOpeninfo(gids);

            client.getGroupPublicInfo(groupId);

            ret = client.setGroupInfo(groupId, "hehe", "haha");
            com.example.rtvdemo.TestClass.outPutMsg(ret, "setGroupInfo");

            GroupInfoStruct groupInfo = client.getGroupInfo(groupId);
            com.example.rtvdemo.TestClass.outPutMsg(groupInfo, "getGroupInfo", groupInfo.toString());

            answer = client.getUserGroups();
            com.example.rtvdemo.TestClass.outPutMsg(answer, "getUserGroups", answer.toString());
    }

    void asyncGroupTest(){
        client.getGroupMembers(new IRTMCallback<HashSet<Long>>() {
            @Override
            public void onResult(HashSet<Long> uids, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getGroupMembers",uids!=null?uids.toString():"");
            }
        },groupId);

        client.addGroupMembers(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"addGroupMembers");
            }
        },groupId,uids);


        client.deleteGroupMembers(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"deleteGroupMembers");
            }
        },groupId,uids);


        client.getGroupPublicInfo(new IRTMCallback<String>() {
            @Override
            public void onResult(String s, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getGroupPublicInfo",s);
            }
        },groupId);


        client.setGroupInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"setGroupInfo");
            }
        },groupId,"hehe","haa");

        client.getGroupInfo(new IRTMCallback<GroupInfoStruct>() {
            @Override
            public void onResult(GroupInfoStruct groupInfoStruct, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getGroupInfo", groupInfoStruct!=null?groupInfoStruct.toString():"");
            }
        },groupId);

        client.getUserGroups(new IRTMCallback<HashSet<Long>>() {
            @Override
            public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getUserGroups");
            }
        });
    }
}

class MessageCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
    long roomId = com.example.rtvdemo.TestClass.roomId;
    long groupId = com.example.rtvdemo.TestClass.groupId;

    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        com.example.rtvdemo.TestClass.enterRoomAsync();
        com.example.rtvdemo.TestClass.enterRoomSync(300);
        mylog.log("======== Begin message test case =========\n");
//        syncRoomTest();
        asyncRoomTest();
        mylog.log("======== End message test case =========\n");

    }

    void displayHistoryMessages(List<HistoryMessage> messages) {
        for (HistoryMessage hm : messages) {
            String str = "";
            if (hm.binaryMessage != null) {
                str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d,  binary message length :%d, attrs:%s, mtime:%d",
                        hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.binaryMessage.length, hm.attrs, hm.modifiedTime);
            } else {
                if (hm.messageType >= MessageType.IMAGEFILE && hm.messageType <= MessageType.NORMALFILE)
                    str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d, fileinfo :%s, attrs:%s, mtime:%d",
                            hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.fileInfo.fileSize +" " + hm.fileInfo.url , hm.attrs, hm.modifiedTime);
                else
                    str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d,  message :%s, attrs:%s, mtime:%d",
                            hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.stringMessage, hm.attrs, hm.modifiedTime);
            }
            mylog.log(str);
        }
    }

    void syncRoomTest(){
        String textMessage = "lala nihao";
        List<Byte> types = new ArrayList<Byte>() {{
            add((byte) 66);
        }};

            client.leaveRoom(roomId);

            MembersStruct answer1 = client.getUserRooms();
            com.example.rtvdemo.TestClass.outPutMsg(answer1, "getUserRooms", answer1.toString());


            GroupInfoStruct info = client.getRoomPublicInfo(roomId);
            com.example.rtvdemo.TestClass.outPutMsg(info, "getRoomPublicInfo", info.publicInfo);
            if (true)
                return;

            RTMAnswer hehe = client.setRoomInfo(roomId, "hehe", "haha");
            com.example.rtvdemo.TestClass.outPutMsg(hehe, "setRoomInfo");

            GroupInfoStruct groupInfo = client.getRoomInfo(roomId);
            com.example.rtvdemo.TestClass.outPutMsg(groupInfo, "getRoomInfo", groupInfo.toString());

            //test roommessage
            ModifyTimeStruct answer = client.sendRoomChat(com.example.rtvdemo.TestClass.roomId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomChat", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendRoomCmd(com.example.rtvdemo.TestClass.roomId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomCmd", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);
//
//            answer = client.sendRoomAudio(TestClass.roomId, TestClass.audioFile);
//            TestClass.outPutMsg(answer, "sendRoomAudio", TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

        answer = client.sendRoomMessage(com.example.rtvdemo.TestClass.roomId, (byte)80, "bugaoxing");
        com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

        int fetchTotalCount = 10;
        int count = fetchTotalCount;
        long beginMsec = 0;
        long endMsec = 0;
        long lastId = 0;
        HistoryMessageResult hisresult;
        mylog.log("\n================[ get Room History Chat " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;

//        while (count >= 0) {
//            hisresult = client.getRoomHistoryChat(TestClass.roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, 0);
//            count = hisresult.count;
//
//            count -= fetchTotalCount;
//
//            displayHistoryMessages(hisresult.messages);
//            beginMsec = hisresult.beginMsec;
//            endMsec = hisresult.endMsec;
//            lastId = hisresult.lastId;
//        }



        mylog.log("\n================[ get Room History Message " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;

        while (count >= 0) {
            hisresult = client.getRoomHistoryMessage(com.example.rtvdemo.TestClass.roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, types);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }
        //test roommessage*/

//            answer = client.getUserGroups();
//            TestClass.outPutMsg(answer, "getUserGroups", answer.uids.toString());
    }

    void asyncRoomTest(){
//        client.leaveRoom(new IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMAnswer answer) {
//                TestClass.asyncOutPutMsg(answer,"leaveRoom");
//            }
//        },roomId);
//

        client.enterRoom(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                if (answer.errorCode == 0){
                    client.getUserRooms(new IRTMCallback<HashSet<Long>>() {
                        @Override
                        public void onResult(HashSet<Long> rooms, RTMAnswer answer) {
                            String desc = "";
                            if (rooms != null)
                            {
                                for (long tt: rooms)
                                {
                                    desc = desc + tt + " ";
                                }
                            }
                            com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getUserRooms", desc);
                        }
                    });
                }

            }
        },roomId);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.setRoomInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"setRoomInfo");
            }
        },roomId,"hello","world");

        client.getRoomPublicInfo(new IRTMCallback<String>() {
            @Override
            public void onResult(String s, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getRoomPublicInfo",s);
            }
        },roomId);


        client.getRoomInfo(new IRTMCallback<GroupInfoStruct>() {
            @Override
            public void onResult(GroupInfoStruct groupInfoStruct, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getRoomInfo", groupInfoStruct!=null?groupInfoStruct.toString():"");
            }
        },roomId);
    }
}

class RoomCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
    long roomId = com.example.rtvdemo.TestClass.roomId;

    final HashSet<Long> rids = new HashSet<Long>() {{
        add(900L);
    }};


    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        com.example.rtvdemo.TestClass.enterRoomAsync();
//        TestClass.enterRoomSync(300);
        mylog.log("======== Begin room test case =========\n");
        syncRoomTest();
//        asyncRoomTest();
        mylog.log("======== End room test case =========\n");

    }

    void displayHistoryMessages(List<HistoryMessage> messages) {
        for (HistoryMessage hm : messages) {
            String str = "";
            if (hm.binaryMessage != null) {
                str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d,  binary message length :%d, attrs:%s, mtime:%d",
                        hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.binaryMessage.length, hm.attrs, hm.modifiedTime);
            } else {
                if (hm.messageType >= MessageType.IMAGEFILE && hm.messageType <= MessageType.NORMALFILE)
                    str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d, fileinfo :%s, attrs:%s, mtime:%d",
                            hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.fileInfo.fileSize +" " + hm.fileInfo.url , hm.attrs, hm.modifiedTime);
                else
                    str = String.format("-- Fetched: ID:%d, from:%d, mtype:%d, mid:%d,  message :%s, attrs:%s, mtime:%d",
                            hm.cursorId, hm.fromUid, hm.messageType, hm.messageId, hm.stringMessage, hm.attrs, hm.modifiedTime);
            }
            mylog.log(str);
        }
    }

    void syncRoomTest(){
        String textMessage = "lala nihao";
        List<Byte> types = new ArrayList<Byte>() {{
            add((byte) 66);
        }};


            client.leaveRoom(roomId);

            MembersStruct answer1 = client.getUserRooms();
            com.example.rtvdemo.TestClass.outPutMsg(answer1, "getUserRooms", answer1.toString());


        GroupInfoStruct info = client.getRoomPublicInfo(roomId);
            com.example.rtvdemo.TestClass.outPutMsg(info, "getRoomPublicInfo", info.toString());

            RTMAnswer hehe = client.setRoomInfo(roomId, "hehe", "haha");
            com.example.rtvdemo.TestClass.outPutMsg(hehe, "setRoomInfo");

            PublicInfo kkkl = client.getRoomsOpeninfo(rids);

            GroupInfoStruct groupInfo = client.getRoomInfo(roomId);
            com.example.rtvdemo.TestClass.outPutMsg(groupInfo, "getRoomInfo", groupInfo.toString());

            //test roommessage
            ModifyTimeStruct answer = client.sendRoomChat(com.example.rtvdemo.TestClass.roomId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomChat", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

            answer = client.sendRoomCmd(com.example.rtvdemo.TestClass.roomId, textMessage);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomCmd", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

         answer = client.sendRoomMessage(com.example.rtvdemo.TestClass.roomId, (byte)80, "bugaoxing");
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendMessage", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime,answer.messageId);

        int fetchTotalCount = 10;
        int count = fetchTotalCount;
        long beginMsec = 0;
        long endMsec = 0;
        long lastId = 0;
        HistoryMessageResult hisresult;
        mylog.log("\n================[ get Room History Chat " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;

//        while (count >= 0) {
//            hisresult = client.getRoomHistoryChat(TestClass.roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, 0);
//            count = hisresult.count;
//
//            count -= fetchTotalCount;
//
//            displayHistoryMessages(hisresult.messages);
//            beginMsec = hisresult.beginMsec;
//            endMsec = hisresult.endMsec;
//            lastId = hisresult.lastId;
//        }



        mylog.log("\n================[ get Room History Message " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;

        while (count >= 0) {
            hisresult = client.getRoomHistoryMessage(com.example.rtvdemo.TestClass.roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, types);
            if (hisresult.errorCode !=0)
                break;
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }
        //test roommessage*/

//            answer = client.getUserGroups();
//            TestClass.outPutMsg(answer, "getUserGroups", answer.uids.toString());
    }

    void asyncRoomTest(){
//        client.leaveRoom(new IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMAnswer answer) {
//                TestClass.asyncOutPutMsg(answer,"leaveRoom");
//            }
//        },roomId);
//

        client.enterRoom(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                if (answer.errorCode == 0){
                    client.getUserRooms(new IRTMCallback<HashSet<Long>>() {
                        @Override
                        public void onResult(HashSet<Long> rooms, RTMAnswer answer) {
                            String desc = "";
                            if (rooms != null)
                            {
                                for (long tt: rooms)
                                {
                                    desc = desc + tt + " ";
                                }
                            }
                            com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getUserRooms", desc);
                        }
                    });
                }

            }
        },roomId);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.setRoomInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"setRoomInfo");
            }
        },roomId,"hello","world");

        client.getRoomPublicInfo(new IRTMCallback<String>() {
            @Override
            public void onResult(String s, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getRoomPublicInfo",s);
            }
        },roomId);


        client.getRoomInfo(new IRTMCallback<GroupInfoStruct>() {
            @Override
            public void onResult(GroupInfoStruct groupInfoStruct, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getRoomInfo", groupInfoStruct!=null?groupInfoStruct.toString():"");
            }
        },roomId);
    }
}

class FileCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
    JSONObject fileattrs = new JSONObject(){{
        try {
            put("mykey", "1111");
            put("mykey1", "2222");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }};

//    private FileMessageType fileMType = FileMessageType.NORMALFILE;
    private String filename = "demo.bin";
    private byte[] fileContent = com.example.rtvdemo.TestClass.piccontent;
    private String picName = "testpicc.jpeg";


    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        com.example.rtvdemo.TestClass.enterRoomSync();
        com.example.rtvdemo.TestClass.mySleep(1);

        mylog.log("======== Begin file test case =========\n");
//        syncFileTest();
        asyncFileTest();
        mylog.log("======== End file test case =========\n");
    }

    //--------------[ send files Demo ]---------------------//
    void syncFileTest(){
        ModifyTimeStruct answer;
            answer = client.sendFile(com.example.rtvdemo.TestClass.peerUid, FileMessageType.IMAGEFILE, com.example.rtvdemo.TestClass.piccontent, picName,fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendFile image", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendGroupFile(com.example.rtvdemo.TestClass.groupId, FileMessageType.IMAGEFILE, com.example.rtvdemo.TestClass.piccontent, picName,fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupFile image", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendRoomFile(com.example.rtvdemo.TestClass.roomId, FileMessageType.IMAGEFILE, com.example.rtvdemo.TestClass.piccontent, picName,fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomFile image", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime, answer.messageId);
            if (true)
                return;


            answer = client.sendFile(com.example.rtvdemo.TestClass.peerUid, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioData, "taishan.wav",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendFile audio", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendGroupFile(com.example.rtvdemo.TestClass.groupId, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioData, "taishan.wav",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupFile audio", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendRoomFile(com.example.rtvdemo.TestClass.roomId, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioData, "taishan.wav",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomFile audio", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime, answer.messageId);


            answer = client.sendFile(com.example.rtvdemo.TestClass.peerUid, FileMessageType.VIDEOFILE, com.example.rtvdemo.TestClass.videoData, "videoDemo.mp4",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendFile video", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendGroupFile(com.example.rtvdemo.TestClass.groupId, FileMessageType.VIDEOFILE, com.example.rtvdemo.TestClass.videoData, "videoDemo.mp4",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupFile video", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendRoomFile(com.example.rtvdemo.TestClass.roomId, FileMessageType.VIDEOFILE, com.example.rtvdemo.TestClass.videoData, "videoDemo.mp4",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomFile video", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime, answer.messageId);


            answer = client.sendFile(com.example.rtvdemo.TestClass.peerUid, FileMessageType.NORMALFILE, com.example.rtvdemo.TestClass.fileData, "normalFile.unity",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendFile normal file", com.example.rtvdemo.TestClass.userBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendGroupFile(com.example.rtvdemo.TestClass.groupId,  FileMessageType.NORMALFILE, com.example.rtvdemo.TestClass.fileData, "normalFile.unity",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendGroupFile normal file", com.example.rtvdemo.TestClass.groupBeizhu, answer.modifyTime, answer.messageId);

            answer = client.sendRoomFile(com.example.rtvdemo.TestClass.roomId,  FileMessageType.NORMALFILE, com.example.rtvdemo.TestClass.fileData, "normalFile.unity",fileattrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "sendRoomFile normal file", com.example.rtvdemo.TestClass.roomBeizhu, answer.modifyTime, answer.messageId);

    }

    void asyncFileTest(){
//        client.sendGroupFile(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long time,Long messageId, RTMAnswer answer) {
//                TestClass.asyncOutPutMsg(answer,"sendGroupFile image", TestClass.groupBeizhu, time,messageId);
//            }
//        },TestClass.groupId, FileMessageType.IMAGEFILE, fileContent, picName,null);
//
//        client.sendRoomFile(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long time,Long messageId, RTMAnswer answer) {
//                TestClass.asyncOutPutMsg(answer,"sendRoomFile image", TestClass.roomBeizhu,time,messageId);
//            }
//        },TestClass.roomId, FileMessageType.IMAGEFILE, fileContent, picName,null);
//        if (true)
//            return;
//        client.sendFile(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long time,Long messageId, RTMAnswer answer) {
//                TestClass.asyncOutPutMsg(answer,"sendFile audio", TestClass.userBeizhu,time, messageId);
//            }
//        },TestClass.peerUid, FileMessageType.AUDIOFILE, TestClass.audioData, "taishan.wav",fileattrs);
//
//        if (true)
//            return;

        client.sendFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendFile image", com.example.rtvdemo.TestClass.userBeizhu,time, messageId);
            }
        }, com.example.rtvdemo.TestClass.peerUid, FileMessageType.IMAGEFILE, fileContent, picName,fileattrs);

        client.sendGroupFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendGroupFile image", com.example.rtvdemo.TestClass.groupBeizhu, time,messageId);
            }
        }, com.example.rtvdemo.TestClass.groupId, FileMessageType.IMAGEFILE, fileContent, picName,fileattrs);

        client.sendRoomFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendRoomFile image", com.example.rtvdemo.TestClass.roomBeizhu,time,messageId);
            }
        }, com.example.rtvdemo.TestClass.roomId, FileMessageType.IMAGEFILE, fileContent, picName,fileattrs);

        com.example.rtvdemo.TestClass.mySleep(1);
        client.sendFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendFile audio", com.example.rtvdemo.TestClass.userBeizhu,time, messageId);
            }
        }, com.example.rtvdemo.TestClass.peerUid, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioData, "taishan.wav",fileattrs);

        client.sendGroupFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendGroupFile audio", com.example.rtvdemo.TestClass.groupBeizhu, time,messageId);
            }
        }, com.example.rtvdemo.TestClass.groupId, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioData, "taishan.wav",fileattrs);

        client.sendRoomFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendRoomFile audio", com.example.rtvdemo.TestClass.roomBeizhu,time,messageId);
            }
        }, com.example.rtvdemo.TestClass.roomId, FileMessageType.AUDIOFILE, com.example.rtvdemo.TestClass.audioData, "taishan.wav",fileattrs);

        com.example.rtvdemo.TestClass.mySleep(1);

        client.sendFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendFile video", com.example.rtvdemo.TestClass.userBeizhu,time, messageId);
            }
        }, com.example.rtvdemo.TestClass.peerUid, FileMessageType.VIDEOFILE, com.example.rtvdemo.TestClass.videoData, "videoDemo.mp4",fileattrs);

        client.sendGroupFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendGroupFile video", com.example.rtvdemo.TestClass.groupBeizhu, time,messageId);
            }
        }, com.example.rtvdemo.TestClass.groupId, FileMessageType.VIDEOFILE, com.example.rtvdemo.TestClass.videoData, "videoDemo.mp4",fileattrs);

        client.sendRoomFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendRoomFile video", com.example.rtvdemo.TestClass.roomBeizhu,time,messageId);
            }
        }, com.example.rtvdemo.TestClass.roomId, FileMessageType.VIDEOFILE, com.example.rtvdemo.TestClass.videoData, "videoDemo.mp4",fileattrs);


        com.example.rtvdemo.TestClass.mySleep(1);

        client.sendFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendFile normal file", com.example.rtvdemo.TestClass.userBeizhu,time, messageId);
            }
        }, com.example.rtvdemo.TestClass.peerUid, FileMessageType.NORMALFILE, com.example.rtvdemo.TestClass.fileData, "normalFile.unity",fileattrs);

        client.sendGroupFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendGroupFile normal file", com.example.rtvdemo.TestClass.groupBeizhu, time,messageId);
            }
        }, com.example.rtvdemo.TestClass.groupId, FileMessageType.NORMALFILE, com.example.rtvdemo.TestClass.fileData, "normalFile.unity",fileattrs);

        client.sendRoomFile(new IRTMDoubleValueCallback<Long, Long>() {
            @Override
            public void onResult(Long time, Long messageId, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"sendRoomFile normal file", com.example.rtvdemo.TestClass.roomBeizhu,time,messageId);
            }
        }, com.example.rtvdemo.TestClass.roomId, FileMessageType.NORMALFILE, com.example.rtvdemo.TestClass.fileData, "normalFile.unity",fileattrs);

    }
}

class SystemCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
    Map<String, String> attrs = new HashMap<String, String>(){{
        put("name","tome");
        put("age","18");
    }};

    void systemTest(){

            RTMAnswer answer = client.addAttributes(attrs);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "addAttributes", attrs.toString());

            AttrsStruct ret = client.getAttributes();
            com.example.rtvdemo.TestClass.outPutMsg(ret, "getAttributes", ret.toString());

        client.addAttributes(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"addAttributes", attrs.toString());
            }
        },attrs);

        client.getAttributes(new IRTMCallback<List<Map<String, String>>>() {
            @Override
            public void onResult(List<Map<String, String>> maps, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getAttributes", maps!=null?maps.toString():"");
            }
        });
    }


    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        mylog.log("======== Begin system test case =========\n");
        systemTest();
        mylog.log("======== End system test case =========\n");
    }
}

class UserCase implements com.example.rtvdemo.CaseInterface {
    RTMClient client = com.example.rtvdemo.TestClass.client;
    HashSet<Long> onlineUsers = new HashSet<Long>(){
        {
            add(100L);
            add(101L);
            add(102L);
        }
    };
    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        mylog.log("======== Begin user test case =========\n");
        userTest();
        mylog.log("======== End user test case =========\n");

    }

    void userTest(){
        PublicInfo ll = client.getUserPublicInfo(onlineUsers);
            MembersStruct answer = client.getOnlineUsers(onlineUsers);
            com.example.rtvdemo.TestClass.outPutMsg(answer, "getOnlineUsers", answer.toString());

            RTMAnswer ret = client.setUserInfo("hehe", "haha");
            com.example.rtvdemo.TestClass.outPutMsg(ret, "setUserInfo");

            GroupInfoStruct userInfo = client.getUserInfo();
            com.example.rtvdemo.TestClass.outPutMsg(userInfo, "getUserInfo", userInfo.privateInfo + " " + userInfo.publicInfo);


        client.getOnlineUsers(new IRTMCallback<HashSet<Long>>() {
            @Override
            public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getOnlineUsers", longs!=null?longs.toString():"");
            }
        },onlineUsers);

        client.setUserInfo(new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"setUserInfo");
            }
        },"hehe","haa");

        client.getUserInfo(new IRTMCallback<GroupInfoStruct>() {
            @Override
            public void onResult(GroupInfoStruct groupInfoStruct, RTMAnswer answer) {
                com.example.rtvdemo.TestClass.asyncOutPutMsg(answer,"getUserInfo", groupInfoStruct.privateInfo + " " + groupInfoStruct.publicInfo);
            }
        });
    }
}

class HistoryCase implements com.example.rtvdemo.CaseInterface {
    List<Byte> types = new ArrayList<Byte>() {{
        add((byte) 66);
    }};
    private static int fetchTotalCount = 1;

    RTMClient client = com.example.rtvdemo.TestClass.client;

    public void start() {
        if (client == null) {
            mylog.log("not available rtmclient");
            return;
        }
        mylog.log("Begin History test case\n");
        com.example.rtvdemo.TestClass.enterRoomSync();
        syncHistoryTest();
//        asyncHistoryTest();

        mylog.log("End History test case\n");
    }

    //------------------------[ Desplay Histories Message ]-------------------------//
    void syncHistoryTest() {
        mylog.log("\n================[ get P2P History Chat " + fetchTotalCount + " items ]==================");
        int count = fetchTotalCount;
        long beginMsec = 0;
        long endMsec = 0;
        long lastId = 0;

        HistoryMessageResult hisresult;

        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getP2PHistoryChat(com.example.rtvdemo.TestClass.peerUid, true, fetchTotalCount, beginMsec, endMsec, lastId);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }

  /*      while (count >= 0) {
            hisresult = client.getBroadcastHistoryChat(true, fetchTotalCount, beginMsec, endMsec, lastId, 0);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }

        while (count >= 0) {
            hisresult = client.getP2PHistoryChat(TestClass.peerUid, true, fetchTotalCount, beginMsec, endMsec, lastId, 0);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }*/
        mylog.log("\n================[ get Group History Chat " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getGroupHistoryChat(com.example.rtvdemo.TestClass.groupId, true, fetchTotalCount, beginMsec, endMsec, lastId);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }

        mylog.log("\n================[ get Room History Chat " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getRoomHistoryChat(com.example.rtvdemo.TestClass.roomId, true, fetchTotalCount, beginMsec, endMsec, lastId);
            count = hisresult.count;

            count -= fetchTotalCount;
            if (hisresult.errorCode != 0)
                break;
            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }

        mylog.log("\n================[ get Broadcast History Chat " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getBroadcastHistoryChat(true, fetchTotalCount, beginMsec, endMsec, lastId);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }


        mylog.log("\n================[ get P2P History Message " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getP2PHistoryMessage(com.example.rtvdemo.TestClass.peerUid, true, fetchTotalCount, beginMsec, endMsec, lastId, types);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }


        mylog.log("\n================[ get Group History Message " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getGroupHistoryMessage(com.example.rtvdemo.TestClass.groupId, true, fetchTotalCount, beginMsec, endMsec, lastId, types);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }


        mylog.log("\n================[ get Room History Message " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getRoomHistoryMessage(com.example.rtvdemo.TestClass.roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, types);
            count = hisresult.count;

            count -= fetchTotalCount;
            if (hisresult.errorCode != 0 )
            {
                mylog.log("chucuol " + hisresult.getErrInfo());
                break;
            }
            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }

        mylog.log("\n================[ get Broadcast History Message " + fetchTotalCount + " items ]==================");
        beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

        while (count >= 0) {
            hisresult = client.getBroadcastHistoryMessage(true, fetchTotalCount, beginMsec, endMsec, lastId, types);
            count = hisresult.count;

            count -= fetchTotalCount;

            displayHistoryMessages(hisresult.messages);
            beginMsec = hisresult.beginMsec;
            endMsec = hisresult.endMsec;
            lastId = hisresult.lastId;
        }
    }

    void asyncHistoryTest(){
        mylog.log("\n================[ get P2P History Chat " + fetchTotalCount + " items ]==================");
        client.getP2PHistoryChat(new IRTMCallback<HistoryMessageResult>() {
           @Override
           public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
               if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                   mylog.log("getP2PHistoryChat in async return error:" + answer.getErrInfo());
                   return;
               }
               displayHistoryMessages(ret.messages);
           }
       }, com.example.rtvdemo.TestClass.peerUid, true, fetchTotalCount, 0, 0, 0);
       com.example.rtvdemo.TestClass.mySleep(1);

        mylog.log("\n================[ get GROUP History Chat " + fetchTotalCount + " items ]==================");
        client.getGroupHistoryChat(new IRTMCallback<HistoryMessageResult>() {
            @Override
            public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                    mylog.log("getGroupHistoryChat in async return error:" + answer.getErrInfo());
                    return;
                }
                displayHistoryMessages(ret.messages);
            }
        }, com.example.rtvdemo.TestClass.groupId, true, fetchTotalCount, 0, 0, 0);
        com.example.rtvdemo.TestClass.mySleep(1);

        mylog.log("\n================[ get ROOM History Chat " + fetchTotalCount + " items ]==================");
        client.getRoomHistoryChat(new IRTMCallback<HistoryMessageResult>() {
            @Override
            public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                    mylog.log("getRoomHistoryChat in async return error:" + answer.getErrInfo());
                    return;
                }
                displayHistoryMessages(ret.messages);
            }
        }, com.example.rtvdemo.TestClass.roomId, true, fetchTotalCount, 0, 0, 0);
        com.example.rtvdemo.TestClass.mySleep(1);

        mylog.log("\n================[ get P2P History Message " + fetchTotalCount + " items ]==================");
        client.getP2PHistoryMessage(new IRTMCallback<HistoryMessageResult>() {
            @Override
            public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                    mylog.log("getP2PHistoryMessage in async return error:" + answer.getErrInfo());
                    return;
                }
                displayHistoryMessages(ret.messages);
            }
        }, com.example.rtvdemo.TestClass.peerUid, true, fetchTotalCount, 0, 0, 0, types);
        com.example.rtvdemo.TestClass.mySleep(1);

        mylog.log("\n================[ get GROUP History Message " + fetchTotalCount + " items ]==================");
        client.getGroupHistoryMessage(new IRTMCallback<HistoryMessageResult>() {
            @Override
            public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                    mylog.log("getGroupHistoryMessage in async return error:" + answer.getErrInfo());
                    return;
                }
                displayHistoryMessages(ret.messages);
            }
        }, com.example.rtvdemo.TestClass.groupId, true, fetchTotalCount, 0, 0, 0, types);
        com.example.rtvdemo.TestClass.mySleep(1);


        mylog.log("\n================[ get ROOM History Message " + fetchTotalCount + " items ]==================");
        client.getRoomHistoryMessage(new IRTMCallback<HistoryMessageResult>() {
            @Override
            public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                    mylog.log("getRoomHistoryMessage in async return error:" + answer.getErrInfo());
                    return;
                }
                displayHistoryMessages(ret.messages);
            }
        }, com.example.rtvdemo.TestClass.roomId, true, fetchTotalCount, 0, 0, 0, types);
        com.example.rtvdemo.TestClass.mySleep(1);
    }

    void displayHistoryMessages(List<HistoryMessage> messages) {
        for (HistoryMessage hm : messages) {
            mylog.log(hm.getInfo());
        }
    }
}

