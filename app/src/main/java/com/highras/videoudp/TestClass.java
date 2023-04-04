package com.highras.videoudp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.fpnn.sdk.ErrorCode;
import com.rtcsdk.RTMCenter;
import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.RTMStruct.*;
import com.rtcsdk.TranscribeLang;
import com.rtcsdk.TranslateLang;
import com.rtcsdk.UserInterface.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestClass {
    public long peerUid = 333;
    public long roomId = 777;
    public long groupId = 777;
    public int sendMessgeType = 81;

    public ArrayList<Integer> savetypes = new ArrayList<Integer>(){{add(sendMessgeType);}};
    public String setLang = "no";
    public Map<Long, RTMClient> pushClients = new HashMap<>();
    public Map<Long, String> pushUserTokens;
    public File audioSave;
    public String textMessage = "chat test";

    JSONObject fileattrs = new JSONObject(){{
        try {
            put("mykey", "1111");
            put("mykey1", "2222");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }};


    TestErrorRecorder mylogRecoder = new TestErrorRecorder();
    TestErrorRecorder1 mylogRecoder1 = new TestErrorRecorder1();
//    public long loginUid = 220;
    public long loginUid = 997;
//    public String loginToken = "17041DDE6C73511D97114250A46DC974";
    public String loginToken = "0DD2BA78F005804153E3155619E24797";
//    public String dispatchEndpoint = "161.189.171.91:9999";
//    public String dispatchEndpoint = "161.189.171.91:13321";
//    public String dispatchEndpoint = "rtm-intl-frontgate.ilivedata.com:80";
//    public String dispatchEndpoint = "rtm-nx-front.ilivedata.com:13321";
    public String dispatchEndpoint = "rtm-ms-frontgate.ilivedata.com:13321";
//    public String dispatchEndpoint = "";
//    public long pid = 80000071;
//    public long pid = 11000002;
//    public long pid = 90000033;

    public RTMClient client = null;
    Random rand = new Random();
    public Map<String, CaseInterface> testMap;
    public String roomBeizhu = " to room " + roomId;
    public String groupBeizhu = " to group " + groupId;
    public String userBeizhu = " to user " + peerUid;
//    public byte[] audioData = null;
    public File audioFile = null;
    public byte[] rtmAudioData = null;
    public byte[] audioData = null;
    public byte[] videoData = null;
    public byte[] piccontent = null;
    private String picName = "testpicc.jpeg";
    public byte[] fileData = null;
    public RTMAudioStruct audioStruct = null;
    public long lastCloseTime = 0;
    private WeakReference<Activity> activityWeakReference ;

    private boolean isFileType(int type){
        return (type >= FileMessageType.IMAGEFILE.value() && type<= FileMessageType.NORMALFILE.value());
    }

    class ChatCase implements CaseInterface {
        //    RTMClient client = pushClients.get(101L);
//    public String textMessage = "{\"user\":{\"name\":\"alex\",\"age\":\"18\",\"isMan\":true}}";
        public String translateMessage = "fuck you";
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

            RTMAnswer answer = client.enterRoom(roomId);
            outPutMsg(answer, "enterRoom", "enterroom " + roomId);

            if (answer.errorCode !=0)
                return;
            String realp2pbeizhu = " to user " + peerUid;
            String val = fileattrs.toString();

            client.dataSet("hehe", val);
            mySleep(2);

            DataInfo info = client.dataGet("hehe");
            try {
                JSONObject jj  = new JSONObject(info.info);
                mylog.log(jj.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }


            /***************************发送聊天类************************************/
            ModifyTimeStruct modifyTimeStruct = client.sendChat(peerUid, textMessage,MessageTypes.P2PMessage,"");
            outPutMsg(modifyTimeStruct, "sendChat", realp2pbeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendChat(peerUid, textMessage, MessageTypes.P2PMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendChat", realp2pbeizhu, aLong, aLong2);
                }
            });

            modifyTimeStruct = client.sendChat(groupId, textMessage,MessageTypes.GroupMessage,"");
            outPutMsg(modifyTimeStruct, "sendChat", groupBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendChat(groupId, textMessage, MessageTypes.GroupMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendChat", groupBeizhu, aLong, aLong2);
                }
            });


            modifyTimeStruct = client.sendChat(roomId, textMessage,MessageTypes.RoomMessage,"");
            outPutMsg(modifyTimeStruct, "sendChat", roomBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendChat(roomId, textMessage, MessageTypes.RoomMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendChat", roomBeizhu, aLong, aLong2);
                }
            });


            /***************************发送CMD类************************************/
            modifyTimeStruct = client.sendCmd(peerUid, textMessage,MessageTypes.P2PMessage,"");
            outPutMsg(modifyTimeStruct, "sendcmd", realp2pbeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendCmd(peerUid, textMessage, MessageTypes.P2PMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendcmd", realp2pbeizhu, aLong, aLong2);
                }
            });

            modifyTimeStruct = client.sendCmd(groupId, textMessage,MessageTypes.GroupMessage,"");
            outPutMsg(modifyTimeStruct, "sendcmd", groupBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendCmd(groupId, textMessage, MessageTypes.GroupMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendcmd", groupBeizhu, aLong, aLong2);
                }
            });


            modifyTimeStruct = client.sendCmd(roomId, textMessage,MessageTypes.RoomMessage,"");
            outPutMsg(modifyTimeStruct, "sendcmd", roomBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendCmd(roomId, textMessage, MessageTypes.RoomMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendcmd", roomBeizhu, aLong, aLong2);
                }
            });



            /***************************发送Message类************************************/
            modifyTimeStruct = client.sendMessage(peerUid, sendMessgeType, textMessage,MessageTypes.P2PMessage,"");
            outPutMsg(modifyTimeStruct, "sendMessage", realp2pbeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendMessage(peerUid, sendMessgeType, textMessage, MessageTypes.P2PMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendMessage", realp2pbeizhu, aLong, aLong2);
                }
            });

            modifyTimeStruct = client.sendMessage(groupId, sendMessgeType, textMessage,MessageTypes.GroupMessage,"");
            outPutMsg(modifyTimeStruct, "sendMessage", groupBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendMessage(groupId, sendMessgeType, textMessage, MessageTypes.GroupMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendMessage", groupBeizhu, aLong, aLong2);
                }
            });


            modifyTimeStruct = client.sendMessage(roomId, sendMessgeType, textMessage,MessageTypes.RoomMessage,"");
            outPutMsg(modifyTimeStruct, "sendMessage", roomBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendMessage(roomId, sendMessgeType, textMessage, MessageTypes.RoomMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendMessage", roomBeizhu, aLong, aLong2);
                }
            });


            /***************************发送Message二进制类************************************/
            modifyTimeStruct = client.sendMessage(peerUid, sendMessgeType, binaryData,MessageTypes.P2PMessage,"");
            outPutMsg(modifyTimeStruct, "sendMessage", realp2pbeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendMessage(peerUid, sendMessgeType, binaryData, MessageTypes.P2PMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendMessage", realp2pbeizhu, aLong, aLong2);
                }
            });

            modifyTimeStruct = client.sendMessage(groupId, sendMessgeType, binaryData,MessageTypes.GroupMessage,"");
            outPutMsg(modifyTimeStruct, "sendMessage", groupBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendMessage(groupId, sendMessgeType, binaryData, MessageTypes.GroupMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendMessage", groupBeizhu, aLong, aLong2);
                }
            });


            modifyTimeStruct = client.sendMessage(roomId, sendMessgeType, binaryData,MessageTypes.RoomMessage,"");
            outPutMsg(modifyTimeStruct, "sendMessage", roomBeizhu, modifyTimeStruct.modifyTime, modifyTimeStruct.messageId);

            client.sendMessage(roomId, sendMessgeType, binaryData , MessageTypes.RoomMessage, "", new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendMessage", roomBeizhu, aLong, aLong2);
                }
            });
        }



        void newInterface(){
            HashSet<Long> p2puids = new HashSet<>();
            p2puids.add(100L);

            for (int i = 0; i<200;i++) {
                for (final long uid : pushClients.keySet()) {
                    pushClients.get(uid).bye();
                }
                mySleep1(500);
                for (final long uid : pushClients.keySet()) {
                    pushClients.get(uid).login(pushUserTokens.get(uid),"zh",null,new IRTMEmptyCallback() {
                        @Override
                        public void onResult(RTMAnswer answer) {
                            if (answer.errorCode == ErrorCode.FPNN_EC_OK.value())
                                mylog.log("user " + uid + " login success ");
                        }
                    });
                }
                mySleep1(500);
                enterRoomSync();
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
//        mySleep(1);
//
//       client.getDevicePushOption(new IRTMCallback<DevicePushOption>() {
//           @Override
//           public void onResult(DevicePushOption devicePushOption, RTMAnswer answer) {
//               mylog.log("getDevicePushOption result " + devicePushOption.toString());
//           }
//       });
////
//        mySleep(5);
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
//        mySleep(1);
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
//        CheckResult lloo = client.imageCheck(piccontent);
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
//        },piccontent);

//        HashSet rids = new HashSet<Long>(){{add(roomId);add(100L);add(200L);}};
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
//        },roomId);

            if (true)
                return;

            client.audioToTextURL("https://s3.cn-northwest-1.amazonaws.com.cn/rtm-filegated-test-cn-northwest-1/90000033/20201022/100/a92adc98ec315c2a52b51f248c53c233.amr", TranscribeLang.ZH_CN.getName(),null,0,new IRTMCallback<AudioTextStruct>() {
                @Override
                public void onResult(AudioTextStruct audioTextStruct, RTMAnswer answer) {
                    mylog.log("audioToTextURL async checkResult result " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("audioToTextURL async text " + audioTextStruct.text + " " + audioTextStruct.lang);
                    }

                }
            });


            client.audioToText(rtmAudioData, TranscribeLang.ZH_CN.getName(),null, 0, new IRTMCallback<AudioTextStruct>() {
                @Override
                public void onResult(AudioTextStruct audioTextStruct, RTMAnswer answer) {
                    mylog.log("audioToText async checkResult result " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("audioToText async text " + audioTextStruct.text + " " + audioTextStruct.lang);
                    }

                }
            });

            mySleep(5);

            client.textCheck(new IRTMCallback<CheckResult>() {
                @Override
                public void onResult(CheckResult checkResult, RTMAnswer answer) {
                    mylog.log(answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log(("textCheck async checkResult text " + checkResult.text));
                    }
                }
            },"bitch you");


            client.imageCheckURL("https://image.baidu.com/search/detail?ct=503316480&z=0&ipn=d&word=%E8%94%A1%E8%8B%B1%E6%96%87&step_word=&hs=0&pn=31&spn=0&di=100870&pi=0&rn=1&tn=baiduimagedetail&is=0%2C0&istype=2&ie=utf-8&oe=utf-8&in=&cl=2&lm=-1&st=-1&cs=3211985769%2C881571615&os=2143750784%2C4271208400&simid=4224450280%2C753660985&adpicid=0&lpn=0&ln=794&fr=&fmq=1603262060531_R&fm=result&ic=&s=undefined&hd=&latest=&copyright=&se=&sme=&tab=0&width=&height=&face=undefined&ist=&jit=&cg=&bdtype=0&oriquery=&objurl=http%3A%2F%2Fimg1.cache.netease.com%2Fcatchpic%2FC%2FCB%2FCBFA4A040907B972B6604449EC875E10.jpg&fromurl=ippr_z2C%24qAzdH3FAzdH3F45gjy_z%26e3B8mn_z%26e3Bv54AzdH3F8mAzdH3Fac80AzdH3F88AzdH3FBNbVGcBmaadcnBaH_z%26e3Bip4s%23u654%3D6jsjewgp&gsm=20&rpstart=0&rpnum=0&islist=&querylist=&force=undefined",null, new IRTMCallback<CheckResult>() {
                @Override
                public void onResult(CheckResult checkResult, RTMAnswer answer) {
                    mylog.log(" imageCheckURL " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("imageCheckURL async checkResult result " + checkResult.result);
                    }
                }
            });


            client.imageCheck(piccontent, null, new IRTMCallback<CheckResult>() {
                @Override
                public void onResult(CheckResult checkResult, RTMAnswer answer) {
                    mylog.log(" imageCheck " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("imageCheck async checkResult result " + checkResult.result);
                    }
                }
            });



            client.audioCheckURL("https://emumo.xiami.com/play?ids=/song/playlist/id/1795782490/object_name/default/object_id/0#loaded",TranscribeLang.EN_US.getName(),null, 0, null, new IRTMCallback<CheckResult>() {
                @Override
                public void onResult(CheckResult checkResult, RTMAnswer answer) {
                    mylog.log(" audioCheckURL " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("audioCheckURL async checkResult result " + checkResult.result);
                    }
                }
            });


            client.audioCheck(rtmAudioData,TranscribeLang.EN_US.getName(),null, 0, null, new IRTMCallback<CheckResult>() {
                @Override
                public void onResult(CheckResult checkResult, RTMAnswer answer) {
                    mylog.log(" audioCheck " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("audioCheck async checkResult result " + checkResult.result);
                    }
                }
            });

            client.videoCheckURL("https://www.bilibili.com/video/BV1ry4y1r7zV?spm_id_from=333.851.b_7265706f7274466972737431.7", null, new IRTMCallback<CheckResult>() {
                @Override
                public void onResult(CheckResult checkResult, RTMAnswer answer) {
                    mylog.log(" audioCheck " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("videoCheckURL async checkResult result " + checkResult.result);
                    }
                }
            });
            mySleep(2);


            client.videoCheck(videoData,"videoDemo.mp4", null, new IRTMCallback<CheckResult>() {
                @Override
                public void onResult(CheckResult checkResult, RTMAnswer answer) {
                    mylog.log(" videoCheck " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        mylog.log("videoCheck async checkResult result " + checkResult.result);
                    }
                }
            });


            client.audioToText(audioData, TranscribeLang.ZH_CN.getName(),"rtm",16000,new IRTMCallback<AudioTextStruct>() {
                @Override
                public void onResult(AudioTextStruct audioTextStruct, RTMAnswer answer) {
                    mylog.log("audioToText sync checkResult result " + answer.getErrInfo());

                }
            });

            mySleep(5);

            HashSet<Long> gids = new HashSet<Long>(){{
                add(200L);
                add(300L);
                add(400L);
            }};

            HashSet<Long> uids = new HashSet<Long>(){{
                add(100L);
                add(101L);
            }};

            client.setUserInfo("100 publicinfo","100 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set user100 info " + answer.getErrInfo());
                }
            });
            mySleep(1);

            pushClients.get(101L).setUserInfo("101 publicinfo","101 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set user101 info " + answer.getErrInfo());
                }
            });
            mySleep(1);


            client.getUserPublicInfo(uids,new IRTMCallback<Map<String, String>>() {
                @Override
                public void onResult(Map<String, String> infoMap, RTMAnswer answer) {
                    mylog.log("getUserPublicInfo " + answer.getErrInfo());
                    if (answer.errorCode  ==0){
                        JSONObject kk = new JSONObject(infoMap);
                        mylog.log("users info is " + kk.toString());
                    }
                }
            });
            mySleep(1);

            client.setGroupInfo(200,"group 200 publicinfo","group 200 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set group 200 info " + answer.getErrInfo());
                }
            });
            mySleep(1);

            client.setGroupInfo(300,"group 300 publicinfo","group 300 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set group 300 info " + answer.getErrInfo());
                }
            });
            mySleep(1);

            client.setGroupInfo(400,"group 400 publicinfo"," group 400 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set group 400 info " + answer.getErrInfo());
                }
            });
            mySleep(1);


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

            mySleep(1);

            client.setRoomInfo(200,"room 200 publicinfo","room 200 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set room 200 info " + answer.getErrInfo());
                }
            });
            mySleep(1);

            client.setRoomInfo(300,"room 300 publicinfo","room 300 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set room 300 info " + answer.getErrInfo());
                }
            });
            mySleep(1);

            client.setRoomInfo(400,"room 400 publicinfo","room 400 privateinfo",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    mylog.log("set room 400 info " + answer.getErrInfo());
                }
            });
            mySleep(1);


            client.getRoomsOpeninfo(gids,new IRTMCallback<Map<String, String>>() {
                @Override
                public void onResult(Map<String, String> infoMap, RTMAnswer answer) {
                    mylog.log("getRoomsOpeninfo " + answer.getErrInfo());
                    if (answer.errorCode  ==0){
                        JSONObject kk = new JSONObject(infoMap);
                        mylog.log("rooms info is " + kk.toString());
                    }
                }
            });

            mySleep(5);
        }

        void blackListSendTest() {
            final String beizhu = "to user " + loginUid;
            long toUid = loginUid;
            RTMClient client = pushClients.get(peerUid);
            client.sendChat(toUid, textMessage,MessageTypes.P2PMessage,"",new IRTMDoubleValueCallback<Long,Long>() {
                @Override
                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                    outPutMsg(answer, "sendchat", beizhu, mtime, messageId,false);
                }
            });


            client.sendCmd(toUid, textMessage,MessageTypes.P2PMessage, "",new IRTMDoubleValueCallback<Long,Long>() {
                @Override
                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                    outPutMsg(answer, "sendCmd", beizhu, mtime, messageId,false);
                }
            });

            client.sendMessage(toUid, sendMessgeType, textMessage,MessageTypes.P2PMessage, "", new IRTMDoubleValueCallback<Long,Long>() {
                @Override
                public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                    outPutMsg(answer, "sendMessage", beizhu, mtime, messageId,false);
                }
            });


//        client.sendAudio(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                outPutMsg(answer, "sendAudio", beizhu, mtime, messageId,false);
//            }
//        }, toUid, audioFile);
        }
    }

    class DataCase implements CaseInterface {
        

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
            mySleep(1);
            getData("key 2");

            mylog.log("=========== Begin delete one of user data ===========");

            deleteData("key 2");

            mylog.log("=========== Begin get user data after delete action ===========");

            getData("key 1");
            mySleep(1);
            getData("key 2");

            mylog.log("=========== User logout ===========");

            client.bye();

            mylog.log("=========== User relogin ===========");
            mySleep(1);

            client.login(loginToken,"zh",null);

            mylog.log("=========== Begin get user data after relogin ===========");

            getData("key 1");
            mySleep(1);
            getData("key 2");
        }


        void Setdata(String key, String value){
            RTMAnswer answer = client.dataSet(key, value);
            outPutMsg(answer, "dataSet", userBeizhu);

            client.dataSet(new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    if (answer.errorCode ==ErrorCode.FPNN_EC_OK.value())
                        mylog.log("dataSet async success ");
                    else
                        mylog.log("dataSet async failed answer:" + answer.getErrInfo());
                }
            },key, value);
            mySleep(1);
        }

        void getData(String key){

            RTMAnswer answer = client.dataGet(key);
            outPutMsg(answer, "dataGet", userBeizhu);

            client.dataGet(new IRTMCallback<String>() {
                @Override
                public void onResult(String value, RTMAnswer answer) {
                    if (answer.errorCode ==ErrorCode.FPNN_EC_OK.value())
                        mylog.log("dataGet async success value is:" + value);
                    else
                        mylog.log("dataGet async failed answer:" + answer.getErrInfo());
                }
            },key);
        }

        void deleteData(String key){
            RTMAnswer answer = client.dataDelete(key);
            outPutMsg(answer, "dataDelete", userBeizhu);
            client.dataDelete(key, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    if (answer.errorCode ==ErrorCode.FPNN_EC_OK.value())
                        mylog.log("dataDelete async success ");
                    else
                        mylog.log("dataDelete async failed answer:" + answer.getErrInfo());
                }
            });
        }
    }

    class FriendCase implements CaseInterface {
        
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
            outPutMsg(answer, "addFriends", uids.toString());

            MembersStruct answer1 = client.getFriends();
            outPutMsg(answer, "getFriends", answer1.toString());

            answer = client.deleteFriends(uids);
            outPutMsg(answer, "deleteFriends", uids.toString());

            answer1 = client.getFriends();
            outPutMsg(answer, "getFriends", answer1.toString());

            //黑名单
            answer = client.addBlacklist(blacks);
            outPutMsg(answer, "addBlacklist", blacks.toString());

            answer1 = client.getBlacklist();
            outPutMsg(answer, "getBlacklist", answer1.toString());

            answer = client.delBlacklist(blacks);
            outPutMsg(answer, "delBlacklist", blacks.toString());

            answer1 = client.getBlacklist();
            outPutMsg(answer, "getBlacklist", answer1.toString());
        }

        void asyncFriendTest(){
            client.addFriends(uids, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer, "addFriends", uids.toString());
                }
            });

            client.getFriends(new IRTMCallback<HashSet<Long>>() {
                @Override
                public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "getFriends", longs!=null?longs.toString():"");
                }
            });

            client.deleteFriends(uids, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer, "deleteFriends", uids.toString());
                }
            });

            client.getFriends(new IRTMCallback<HashSet<Long>>() {
                @Override
                public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "getFriends", longs!=null?longs.toString():"");
                }
            });

            //黑名单
            client.addBlacklist(blacks, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer, "addBlacklist", blacks.toString());
                }
            });

            client.getBlacklist(new IRTMCallback<HashSet<Long>>() {
                @Override
                public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "getBlacklist", longs!=null?longs.toString():"");
                }
            });

            client.delBlacklist(blacks, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer, "delBlacklist", blacks.toString());
                }
            });
            mySleep(3);

            client.getBlacklist(new IRTMCallback<HashSet<Long>>() {
                @Override
                public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "getBlacklist", longs!=null?longs.toString():"");
                }
            });
        }
    }

    class GroupCase implements CaseInterface {
        
        
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
            mySleep(5);
            mylog.log("End group test case\n");
        }

        void syncGroupTest(){
            MembersStruct answer = client.getGroupMembers(groupId);
            outPutMsg(answer, "getGroupMembers", answer.onlineUids.toString() + " " + answer.uids.toString());


            GroupInfoStruct info = client.getGroupPublicInfo(groupId);
            outPutMsg(info, "getGroupPublicInfo", info.toString());

            PublicInfo ll= client.getGroupsOpeninfo(gids);

            client.getGroupPublicInfo(groupId);

            GroupInfoStruct groupInfo = client.getGroupInfo(groupId);
            outPutMsg(groupInfo, "getGroupInfo", groupInfo.toString());

            answer = client.getUserGroups();
            outPutMsg(answer, "getUserGroups", answer.toString());
        }

        void asyncGroupTest(){
            client.getGroupMembers(new IRTMCallback<MembersStruct>() {
                @Override
                public void onResult(MembersStruct uidInfos, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getGroupMembers",uidInfos.uids.toString() + " " + uidInfos.onlineUids.toString());
                }
            },groupId);

            client.getGroupCount(groupId,new IRTMCallback<GroupCount>() {
                @Override
                public void onResult(GroupCount groupCount, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getGroupCount",groupCount.totalCount + " " + groupCount.onlineCount);

                }
            });
            client.addGroupMembers(groupId,uids, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer,"addGroupMembers");
                }
            });


            client.deleteGroupMembers(groupId,uids, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer,"deleteGroupMembers");
                }
            });


            client.getGroupPublicInfo(groupId,new IRTMCallback<String>() {
                @Override
                public void onResult(String s, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getGroupPublicInfo",s);
                }
            });


            client.setGroupInfo(groupId, "hehe","haa", new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer,"setGroupInfo");
                }
            });

            client.getGroupInfo(groupId, new IRTMCallback<GroupInfoStruct>() {
                @Override
                public void onResult(GroupInfoStruct groupInfoStruct, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getGroupInfo", groupInfoStruct!=null?groupInfoStruct.toString():"");
                }
            });

            client.getUserGroups(new IRTMCallback<HashSet<Long>>() {
                @Override
                public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getUserGroups");
                }
            });
        }
    }

    class RoomCase implements CaseInterface {

        final HashSet<Long> rids = new HashSet<Long>() {{
            add(900L);
        }};


        public void start() {
            if (client == null) {
                mylog.log("not available rtmclient");
                return;
            }
            enterRoomAsync();
//        enterRoomSync(300);
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
            List<Integer> types = new ArrayList<Integer>() {{
                add( 66);
            }};


            client.leaveRoom(roomId);

            MembersStruct answer1 = client.getUserRooms();
            outPutMsg(answer1, "getUserRooms", answer1.toString());


            GroupInfoStruct info = client.getRoomPublicInfo(roomId);
            outPutMsg(info, "getRoomPublicInfo", info.toString());

            RTMAnswer hehe = client.setRoomInfo(roomId, textMessage, "haha");
            outPutMsg(hehe, "setRoomInfo");


            GroupInfoStruct groupInfo = client.getRoomInfo(roomId);
            outPutMsg(groupInfo, "getRoomInfo", groupInfo.toString());

            int fetchTotalCount = 10;
            int count = fetchTotalCount;
            long beginMsec = 0;
            long endMsec = 0;
            long lastId = 0;
            HistoryMessageResult hisresult;
            mylog.log("\n================[ get Room History Chat " + fetchTotalCount + " items ]==================");
            beginMsec = 0;endMsec = 0;lastId = 0;

//        while (count >= 0) {
//            hisresult = client.getRoomHistoryChat(roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, 0);
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
                hisresult = client.getHistoryMessage(roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, types, MessageTypes.RoomMessage);
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
//            outPutMsg(answer, "getUserGroups", answer.uids.toString());
        }

        void asyncRoomTest(){
//        client.leaveRoom(new IRTMEmptyCallback() {
//            @Override
//            public void onResult(RTMAnswer answer) {
//                asyncOutPutMsg(answer,"leaveRoom");
//            }
//        },roomId);
//

            client.enterRoom(roomId,new IRTMEmptyCallback() {
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
                                asyncOutPutMsg(answer,"getUserRooms", desc);
                            }
                        });
                    }

                }
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            client.setRoomInfo(roomId,"hello","world",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer,"setRoomInfo");
                }
            });

            client.getRoomPublicInfo(roomId,new IRTMCallback<String>() {
                @Override
                public void onResult(String s, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getRoomPublicInfo",s);
                }
            });


            client.getRoomInfo(roomId, new IRTMCallback<GroupInfoStruct>() {
                @Override
                public void onResult(GroupInfoStruct groupInfoStruct, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getRoomInfo", groupInfoStruct!=null?groupInfoStruct.toString():"");
                }
            });
        }
    }

    class FileCase implements CaseInterface {
        public void start() {
            if (client == null) {
                mylog.log("not available rtmclient");
                return;
            }

            RTMAnswer answer = client.enterRoom(roomId);
            outPutMsg(answer, "enterRoom", "enterroom " + roomId);

            if (answer.errorCode !=0)
                return;
            String realp2pbeizhu = " to user " + peerUid;

/*            long sendtime = System.currentTimeMillis();
            client.uploadFile( FileMessageType.VIDEOFILE,"ct4.jpeg", piccontent, null, new IRTMDoubleValueCallback<String, Long>() {
                @Override
                public void onResult(String s, Long aLong, RTMAnswer answer) {
                    long recievetime  = System.currentTimeMillis();
                    mylog.log("sendFileOnlySave ret " + answer.getErrInfo() + " url is " + s + " size is " + aLong + " costtime " + (recievetime-sendtime));

                }
            });

            if (true)
                return;*/
            /***************************发送file类************************************/
            client.sendFile(peerUid, FileMessageType.NORMALFILE, piccontent, picName, fileattrs, null, new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendFile", realp2pbeizhu, aLong, aLong2);
                }
            });

/*
            client.sendGroupFile(groupId, FileMessageType.NORMALFILE, piccontent, picName, fileattrs, null, new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendFile", groupBeizhu, aLong, aLong2);
                }
            });


            client.sendRoomFile(roomId, FileMessageType.NORMALFILE, piccontent, picName, fileattrs, null, new IRTMDoubleValueCallback<Long, Long>() {
                @Override
                public void onResult(Long aLong, Long aLong2, RTMAnswer answer) {
                    asyncOutPutMsg(answer, "sendFile", roomBeizhu, aLong, aLong2);
                }
            });
*/

            mylog.log("======== End file test case =========\n");
        }
    }

    class SystemCase implements CaseInterface {
        
        Map<String, String> attrs = new HashMap<String, String>(){{
            put("name","tome");
            put("age","18");
        }};

        void systemTest(){

            RTMAnswer answer = client.addAttributes(attrs);
            outPutMsg(answer, "addAttributes", attrs.toString());

            AttrsStruct ret = client.getAttributes();
            outPutMsg(ret, "getAttributes", ret.toString());

            client.addAttributes(attrs, new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer,"addAttributes", attrs.toString());
                }
            });

            client.getAttributes(new IRTMCallback<List<Map<String, String>>>() {
                @Override
                public void onResult(List<Map<String, String>> maps, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getAttributes", maps!=null?maps.toString():"");
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

    class UserCase implements CaseInterface {
        
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
            outPutMsg(answer, "getOnlineUsers", answer.toString());

            RTMAnswer ret = client.setUserInfo(textMessage, "haha");
            outPutMsg(ret, "setUserInfo");

            GroupInfoStruct userInfo = client.getUserInfo();
            outPutMsg(userInfo, "getUserInfo", userInfo.privateInfo + " " + userInfo.publicInfo);


            client.getOnlineUsers(onlineUsers, new IRTMCallback<HashSet<Long>>() {
                @Override
                public void onResult(HashSet<Long> longs, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getOnlineUsers", longs!=null?longs.toString():"");
                }
            });

            client.setUserInfo(textMessage,"haa",new IRTMEmptyCallback() {
                @Override
                public void onResult(RTMAnswer answer) {
                    asyncOutPutMsg(answer,"setUserInfo");
                }
            });

            client.getUserInfo(new IRTMCallback<GroupInfoStruct>() {
                @Override
                public void onResult(GroupInfoStruct groupInfoStruct, RTMAnswer answer) {
                    asyncOutPutMsg(answer,"getUserInfo", groupInfoStruct.privateInfo + " " + groupInfoStruct.publicInfo);
                }
            });
        }
    }


    class HistoryCase implements CaseInterface {
        List<Integer> types = new ArrayList<Integer>() {{
            add( 66);
        }};
        private int fetchTotalCount = 1;

        

        public void start() {
            if (client == null) {
                mylog.log("not available rtmclient");
                return;
            }
            mylog.log("Begin History test case\n");
            RTMAnswer answer = client.enterRoom(roomId);
            outPutMsg(answer, "enterRoom", "enterroom " + roomId);

            if (answer.errorCode !=0)
                return;

            syncHistoryTest();
//            getconversationtest();

            mylog.log("End History test case\n");
        }


        void getconversationtest(){
            client.getp2pConversationList(0, null, new IRTMCallback<List<ConversationInfo>>() {
                @Override
                public void onResult(List<ConversationInfo> conversationInfos, RTMAnswer answer) {
                    mylog.log("getp2pConversationList result " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        for (ConversationInfo conversationInfo: conversationInfos){
                            if (isFileType(conversationInfo.lastHistortMessage.messageType) )
                                mylog.log("getp2pConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.fileInfo.url);
                            else
                                mylog.log("getp2pConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.stringMessage);
                        }
                    }
                }
            });

            client.getGroupConversationList(0, null, new IRTMCallback<List<ConversationInfo>>() {
                @Override
                public void onResult(List<ConversationInfo> conversationInfos, RTMAnswer answer) {
                    mylog.log("getGroupConversationList result " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        for (ConversationInfo conversationInfo: conversationInfos){
                            if (isFileType(conversationInfo.lastHistortMessage.messageType) )
                                mylog.log("getGroupConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.fileInfo.url);
                            else
                                mylog.log("getGroupConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.stringMessage);
                        }
                    }
                }
            });


            client.getP2PUnreadConversationList(0, null, new IRTMCallback<List<ConversationInfo>>() {
                @Override
                public void onResult(List<ConversationInfo> conversationInfos, RTMAnswer answer) {
                    mylog.log("getP2PUnreadConversationList result " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        for (ConversationInfo conversationInfo: conversationInfos){
                            if (isFileType(conversationInfo.lastHistortMessage.messageType) )
                                mylog.log("getP2PUnreadConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.fileInfo.url);
                            else
                                mylog.log("getP2PUnreadConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.stringMessage);
                        }
                    }

                }
            });

            client.getGroupUnreadConversationList(0, null, new IRTMCallback<List<ConversationInfo>>() {
                @Override
                public void onResult(List<ConversationInfo> conversationInfos, RTMAnswer answer) {
                    mylog.log("getGroupUnreadConversationList result " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        for (ConversationInfo conversationInfo: conversationInfos){
                            if (isFileType(conversationInfo.lastHistortMessage.messageType) )
                                mylog.log("getGroupUnreadConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.fileInfo.url);
                            else
                                mylog.log("getGroupUnreadConversationList uid:"  + conversationInfo.toId + " unreadnum:" + conversationInfo.unreadNum + " lastmsg:" + conversationInfo.lastHistortMessage.stringMessage);
                        }
                    }
                }
            });

            client.getUnreadConversationList(false, 0, null, new IRTMCallback<UnreadConversationInfo>() {
                @Override
                public void onResult(UnreadConversationInfo unreadConversationInfo, RTMAnswer answer) {
                    mylog.log("getUnreadConversationList result " + answer.getErrInfo());
                    if (answer.errorCode == 0){
                        for (ConversationInfo p2punread: unreadConversationInfo.p2pUnreads){
                            if (isFileType(p2punread.lastHistortMessage.messageType) )
                                mylog.log("getP2PUnreadConversationList uid:"  + p2punread.toId + " unreadnum:" + p2punread.unreadNum + " lastmsg:" + p2punread.lastHistortMessage.fileInfo.url);
                            else
                                mylog.log("getP2PUnreadConversationList uid:"  + p2punread.toId + " unreadnum:" + p2punread.unreadNum + " lastmsg:" + p2punread.lastHistortMessage.stringMessage);

                        }

                        for (ConversationInfo groupunread: unreadConversationInfo.groupUnreads){
                            if (isFileType(groupunread.lastHistortMessage.messageType) )
                                mylog.log("getP2PUnreadConversationList uid:"  + groupunread.toId + " unreadnum:" + groupunread.unreadNum + " lastmsg:" + groupunread.lastHistortMessage.fileInfo.url);
                            else
                                mylog.log("getP2PUnreadConversationList uid:"  + groupunread.toId + " unreadnum:" + groupunread.unreadNum + " lastmsg:" + groupunread.lastHistortMessage.stringMessage);

                        }
                    }
                }
            });
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
                hisresult = client.getHistoryChat(peerUid, true, fetchTotalCount, beginMsec, endMsec, lastId, MessageTypes.P2PMessage);
                count = hisresult.count;

                count -= fetchTotalCount;

                displayHistoryMessages(hisresult.messages);
                beginMsec = hisresult.beginMsec;
                endMsec = hisresult.endMsec;
                lastId = hisresult.lastId;
            }

            mylog.log("\n================[ get Group History Chat " + fetchTotalCount + " items ]==================");
            beginMsec = 0;endMsec = 0;lastId = 0;count = fetchTotalCount;

            while (count >= 0) {
                hisresult = client.getHistoryChat(groupId, true, fetchTotalCount, beginMsec, endMsec, lastId, MessageTypes.GroupMessage);
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
                hisresult = client.getHistoryChat(roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, MessageTypes.RoomMessage);
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
                hisresult = client.getHistoryChat(-1,true, fetchTotalCount, beginMsec, endMsec, lastId, MessageTypes.BroadcastMessage);
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
                hisresult = client.getHistoryMessage(peerUid, true, fetchTotalCount, beginMsec, endMsec, lastId, types, MessageTypes.P2PMessage);
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
                hisresult = client.getHistoryMessage(groupId, true, fetchTotalCount, beginMsec, endMsec, lastId, types,MessageTypes.GroupMessage);
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
                hisresult = client.getHistoryMessage(roomId, true, fetchTotalCount, beginMsec, endMsec, lastId, types,MessageTypes.RoomMessage);
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
                hisresult = client.getHistoryMessage(-1,true, fetchTotalCount, beginMsec, endMsec, lastId, types,MessageTypes.BroadcastMessage);
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
            client.getHistoryChat(peerUid, true, fetchTotalCount, 0, 0, 0,MessageTypes.P2PMessage,new IRTMCallback<HistoryMessageResult>() {
                @Override
                public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                    if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        mylog.log("getP2PHistoryChat in async return error:" + answer.getErrInfo());
                        return;
                    }
                    displayHistoryMessages(ret.messages);
                }
            });
            mySleep(1);

            mylog.log("\n================[ get GROUP History Chat " + fetchTotalCount + " items ]==================");
            client.getHistoryChat(groupId, true, fetchTotalCount, 0, 0, 0, MessageTypes.GroupMessage,new IRTMCallback<HistoryMessageResult>() {
                @Override
                public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                    if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        mylog.log("getGroupHistoryChat in async return error:" + answer.getErrInfo());
                        return;
                    }
                    displayHistoryMessages(ret.messages);
                }
            });
            mySleep(1);

            mylog.log("\n================[ get ROOM History Chat " + fetchTotalCount + " items ]==================");
            client.getHistoryChat(roomId, true, fetchTotalCount, 0, 0, 0, MessageTypes.RoomMessage,new IRTMCallback<HistoryMessageResult>() {
                @Override
                public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                    if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        mylog.log("getRoomHistoryChat in async return error:" + answer.getErrInfo());
                        return;
                    }
                    displayHistoryMessages(ret.messages);
                }
            });
            mySleep(1);

            mylog.log("\n================[ get P2P History Message " + fetchTotalCount + " items ]==================");
            client.getHistoryMessage(peerUid, true, fetchTotalCount, 0, 0, 0, types, MessageTypes.P2PMessage, new IRTMCallback<HistoryMessageResult>() {
                @Override
                public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                    if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        mylog.log("getP2PHistoryMessage in async return error:" + answer.getErrInfo());
                        return;
                    }
                    displayHistoryMessages(ret.messages);
                }
            });
            mySleep(1);

            mylog.log("\n================[ get GROUP History Message " + fetchTotalCount + " items ]==================");
            client.getHistoryMessage(groupId, true, fetchTotalCount, 0, 0, 0, types, MessageTypes.GroupMessage, new IRTMCallback<HistoryMessageResult>() {
                @Override
                public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                    if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        mylog.log("getGroupHistoryMessage in async return error:" + answer.getErrInfo());
                        return;
                    }
                    displayHistoryMessages(ret.messages);
                }
            });
            mySleep(1);


            mylog.log("\n================[ get ROOM History Message " + fetchTotalCount + " items ]==================");
            client.getHistoryMessage(roomId, true, fetchTotalCount, 0, 0, 0, types, MessageTypes.RoomMessage, new IRTMCallback<HistoryMessageResult>() {
                @Override
                public void onResult(HistoryMessageResult ret, RTMAnswer answer) {
                    if (answer.errorCode != ErrorCode.FPNN_EC_OK.value()) {
                        mylog.log("getRoomHistoryMessage in async return error:" + answer.getErrInfo());
                        return;
                    }
                    displayHistoryMessages(ret.messages);
                }
            });
            mySleep(1);
        }

        void displayHistoryMessages(List<HistoryMessage> messages) {
            if (messages == null)
                return;
            for (HistoryMessage hm : messages) {
                mylog.log(hm.getInfo());
            }
        }
    }


    int getuid(){
        return rand.nextInt(20000 - 1 + 1) + 1;
    }

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

    public void displayHistoryMessages(List<HistoryMessage> messages) {
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
//        enterRoomSync();
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
//                outPutMsg(answer, "sendFile audio", userBeizhu, mtime, messageId,false);
//            }
//        }, roomId, FileMessageType.AUDIOFILE,audioStruct.audioData,"",lll,audioStruct);



        if (audioStruct == null)
            return;

//        client.sendFile(new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                outPutMsg(answer, "sendGroupFile audio", groupBeizhu, mtime, messageId,false);
//            }
//        }, 123456, FileMessageType.AUDIOFILE, audioStruct.audioData,"",lll, audioStruct);

//        client.sendGroupFile(100, FileMessageType.AUDIOFILE, audioStruct.audioData,"",lll, audioStruct,new IRTMDoubleValueCallback<Long,Long>() {
//            @Override
//            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
//                outPutMsg(answer, "sendGroupFile audio", groupBeizhu, mtime, messageId,false);
//            }
//        });

        client.uploadFile( FileMessageType.AUDIOFILE,"hehe.amr", audioStruct.audioData, audioStruct, new IRTMDoubleValueCallback<String, Long>() {
            @Override
            public void onResult(String s, Long aLong, RTMAnswer answer) {
                mylog.log("uploadFile audiorecord ret " + answer.getErrInfo() + " url is " + s + " size is " + aLong);

            }
        });


        if(true)
            return;

        client.sendRoomFile(roomId, FileMessageType.AUDIOFILE, audioStruct.audioData,"",lll, audioStruct,new IRTMDoubleValueCallback<Long,Long>() {
            @Override
            public void onResult(Long mtime, Long messageId, RTMAnswer answer) {
                outPutMsg(answer, "sendFile audio", userBeizhu, mtime, messageId,false);
            }
        });

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
        for (CaseInterface key : testMap.values())
            key.start();
    }

    public void writeFile(byte[] data, File file)
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


//    public void repeatLogin(Context context){
//        loginUid = 100;
//        loginToken = getToken();
//        client  = new RTMClient(dispatchEndpoint, pid, loginUid, new RTMExampleQuestProcessor(loginUid),context);
//        client.setErrorRecoder(mylogRecoder);
//    }


    public void exist(){
        if (client == null)
            return;;
        client.bye();
        client.closeRTM();
        client= null;

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (client == null)
//                    return;;
//                client.bye();
//                client.closeRTM();
//            }
//        }).start();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (client == null)
//                    return;;
////                client.closeRTM();
//                client= null;
//            }
//        }).start();

    }

    public void loginRTM() {
        String addr = Utils.INSTANCE.address;
        RTMProjectInfo info = testAddress.get(addr);
//        loginUid = getuid();
//        loginToken = getToken(addr);

        long ts = System.currentTimeMillis()/1000;
        loginToken = ApiSecurityExample.genHMACToken(info.pid, loginUid, ts,"cXdlcnR5");
//        loginToken = "279206AC83D0617079954CC9C7E12E5A1B394585E5CA78F776BEF7A";


        client  = RTMCenter.initRTMClient(info.host, info.pid, loginUid, new RTMExampleQuestProcessor(loginUid),activityWeakReference.get());
        client.setErrorRecoder(mylogRecoder);

//        for(int i= 0;i<1;i++) {
//            peerUid = getuid();
//            userBeizhu = " to user " + peerUid;
//
//            pushUserTokens.put(peerUid,getToken(peerUid));
//            RTMClient rtmUser = new RTMClient(dispatchEndpoint, pid, peerUid, new RTMExampleQuestProcessor(peerUid),context);
//            rtmUser.setErrorRecoder(mylogRecoder1);
//
//            rtmUser.setErrorRecoder(mylogRecoder1);
//            pushClients.put(peerUid, rtmUser);
//        }


//        RTMAnswer answer1 = client.login(loginToken);
//
//        if (answer1.errorCode ==ErrorCode.FPNN_EC_OK.value())
//            mylog.log("user " + loginUid + " login RTM success");
//        else
//            mylog.log("user " + loginUid + " login RTM error:" + answer1.getErrInfo());

        client.login(loginToken,"zh",null,ts, new IRTMEmptyCallback() {
            @Override
            public void onResult(RTMAnswer answer) {
                if (answer.errorCode ==ErrorCode.FPNN_EC_OK.value()) {
                    activityWeakReference.get().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activityWeakReference.get(), "登录成功", Toast.LENGTH_SHORT).show();

                        }
                    });
                    mylog.log("user " + loginUid + " login RTM success");
/*                    for (int i =0;i<100;i++) {
                        long sendTime = System.currentTimeMillis();
                        RTMAnswer kkk = client.setUserInfo("publicinfo", "privateinfo");
//                        RTMAnswer kkk = client.getUserInfo();
//                        RTMAnswer kkk = client.sendChat(peerUid,"nihao");
                        long recieveTime = System.currentTimeMillis();
                        mylog.log("setUserInfo " + kkk.getErrInfo() + " cost time:" + (recieveTime -sendTime) + "ms");
                        mySleep1(200);
                    }*/
                }
                else {
                    Utils.alertDialog(activityWeakReference.get(), "登录失败"+ answer.getErrInfo());
                    mylog.log("user " + loginUid + " login RTM error:" + answer.getErrInfo());
                }
            }
        });

////        mySleep(3);
//        RTMAnswer kkk = client.setUserInfo("publicinfo","privateinfo");
////        RTMAnswer kkk = client.getUserInfo();
//        mylog.log("setUserInfo " + kkk.getErrInfo());
        if(true)
            return;
        mylog.log("nihao send  login");
        RTMAnswer answer = client.login(loginToken,"zh",null);

        if (answer.errorCode ==ErrorCode.FPNN_EC_OK.value())
            mylog.log("user " + loginUid + " login RTM success");
        else
            mylog.log("user " + loginUid + " login RTM error:" + answer.getErrInfo());

        for (final long uid : pushClients.keySet()) {
            final RTMClient loginClient = pushClients.get(uid);

            loginClient.login(pushUserTokens.get(uid),"zh",null,new IRTMEmptyCallback() {
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
            });
        }
    }

//    public void loginRTM1() {
//        client  = new RTMClient(dispatchEndpoint, pid, loginUid, new RTMExampleQuestProcessor1(),appContext);
//        client.setErrorRecoder(mylogRecoder1);
//
//
//        RTMAnswer answer = client.login(loginToken);
//
//        if (answer.errorCode ==ErrorCode.FPNN_EC_OK.value())
//            mylog.log("user " + loginUid + " login RTM success");
//        else
//            mylog.log("user " + loginUid + " login RTM error:" + answer.getErrInfo());
//    }

    public byte[] fileToByteArray(File file) {
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

    class RTMProjectInfo{
        long pid;
        String host;
        RTMProjectInfo(long _pid, String _host){
            pid = _pid;
            host = _host;
        }
    }


    public void mySleep1(int millsecond) {
        try {
            Thread.sleep(millsecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void okhttpRequest(){
        OkHttpClient okHttpClient = new OkHttpClient();
//                 final Request request = new Request.Builder()
//                         .url(url)
//                         .get()//默认就是GET请求，可以不写
//                         .build();
//        String currTime = System.currentTimeMillis();
//        String autotoken = getAuth(url, currTime, postBody, isPost).trim();

        String geturl = "https://rtm-intl-frontgate-test.ilivedata.com/service/tcp-13321-fail-tcp-80-fail-11000002-100";

//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), String.valueOf(postBody));
        final Request request = new Request.Builder()
                .url(geturl)
//                .addHeader("Authorization", autotoken)
//                .addHeader("X-TimeStamp", currTime)
//                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("https 443", "onFailure: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                             Log.d("customsdk", "onResponse: " + response.body().string());
                Log.d("https 443", "ok: ");
            }
        });
    }

    final HashMap<String, RTMProjectInfo> testAddress = new HashMap(){{
        put("test", new RTMProjectInfo(11000001,"161.189.171.91:13321"));
//        put("nx",new RTMProjectInfo(80000071,"rtm-nx-front.ilivedata.com:13321"));
        put("nx",new RTMProjectInfo(80000071,"52.83.245.22:13092"));
        put("intl",new RTMProjectInfo(80000087,"rtm-ms-frontgate.ilivedata.com:13321"));
    }};

    public String getToken(String address) {
        int port = 0;
        if (address.equals("test"))
            port = 8099;
        else if (address.equals("nx"))
            port = 8090;
        else if (address.equals("intl"))
            port = 8098;

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String tourl = "http://161.189.171.91:" + port + "?uid=" + loginUid;

        try {
            URL url = new URL(tourl);
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
                return "";
            }
        } catch (Exception e) {
            mylog.log("gettoken error :" + e.getMessage());
        }
        return output.toString();
    }


    public TestClass(Activity activity) {

        activityWeakReference = new WeakReference<>(activity);
        pushUserTokens = new HashMap<>();
        testMap = new HashMap<String, CaseInterface>() {
            {
                put("chat", new ChatCase());
                put("data", new DataCase());
                put("group", new GroupCase());
                put("friend", new FriendCase());
                put("room", new RoomCase());
                put("file", new FileCase());
                put("system", new SystemCase());
                put("user", new UserCase());
                put("history", new HistoryCase());
            }
        };
    }

    public void leaveRoomSync() {
        enterRoomSync(roomId);
    }

    public void leaveRoomSync(final long roomId) {
        client.leaveRoom(roomId, new IRTMEmptyCallback(){
            @Override
            public void onResult(RTMAnswer answer) {
                outPutMsg(answer,"leaveroom" + roomId,"uid is " + loginUid);
            }
        });
        for (final long uid:pushClients.keySet()) {
            pushClients.get(uid).leaveRoom(roomId, new IRTMEmptyCallback(){
                @Override
                public void onResult(RTMAnswer answer) {
                    outPutMsg(answer,"leaveroom" + roomId,"uid is " + uid);
                }
            });
        }
    }


    public void sayAllbye() {
        client.bye();
        for (final long uid:pushClients.keySet()) {
            pushClients.get(uid).bye();
        }
    }


    public void enterRoomSync() {
        enterRoomSync(roomId);
    }

    public void enterRoomSync(long roomid) {

            RTMAnswer answer = client.enterRoom(roomid);
            outPutMsg(answer,"enterroom" + roomid,"uid is " + loginUid);
            for (long uid:pushClients.keySet()) {
                answer = pushClients.get(uid).enterRoom(roomid);
                outPutMsg(answer,"enterroom" + roomid,"uid is " + uid);
            }
    }


    public void enterRoomAsync() {
        enterRoomAsync(roomId);
    }

    public void enterRoomAsync(final long roomid) {
        client.enterRoom(roomid, new IRTMEmptyCallback(){
            @Override
            public void onResult(RTMAnswer answer) {
                outPutMsg(answer,"enterroom" + roomid,"uid is " + loginUid);
            }
        });
        for (final long uid:pushClients.keySet()) {
            pushClients.get(uid).enterRoom(roomid, new IRTMEmptyCallback(){
                @Override
                public void onResult(RTMAnswer answer) {
                    outPutMsg(answer,"enterroom" + roomid,"uid is " + uid);
                }
            });
        }
    }

    public void asyncOutPutMsg(RTMAnswer answer, String method) {
        outPutMsg(answer, method, "", 0, 0L,false);
    }

    public void asyncOutPutMsg(RTMAnswer answer, String method, String beizhu) {
        outPutMsg(answer, method, beizhu, 0, 0L,false);
    }

    public void asyncOutPutMsg(RTMAnswer answer, String method, String beizhu,long mtime, long messageId) {
        outPutMsg(answer, method, beizhu, mtime, messageId,false);
    }


    public void outPutMsg(RTMAnswer answer, String method) {
        outPutMsg(answer, method, "", 0, 0L,true);
    }

    public void outPutMsg(RTMAnswer answer ,String method, String beizhu) {
        outPutMsg(answer, method, beizhu, 0, 0L,true);
    }

    public void outPutMsg(RTMAnswer answer, String method, String beizhu, long mtime, long messageId) {
        outPutMsg(answer, method, beizhu, mtime, messageId,true);
    }

    public void outPutMsg(RTMAnswer answer, String method, String beizhu, long mtime, long messageId, boolean sync) {
        String syncType = "sync", msg = "";
        long xid = 0;
        if (!sync)
            syncType = "async";

        if (answer.errorCode ==ErrorCode.FPNN_EC_OK.value()) {
            if (mtime > 0)
                msg = String.format("%s %s in %s successed, mtime is:%d, messageId is :%d", method, beizhu, syncType, mtime, messageId);
            else
                msg = String.format("%s %s in %s successed", method, beizhu, syncType);
        } else
            msg = String.format("%s %s in %s failed, errordes:%s", method, beizhu, syncType, answer.getErrInfo());
        mylog.log(msg);
    }
}

interface CaseInterface {
    void start() throws InterruptedException;
}