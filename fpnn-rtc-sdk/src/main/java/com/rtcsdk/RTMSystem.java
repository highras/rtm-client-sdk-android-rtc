package com.rtcsdk;

import android.util.Log;

import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.rtcsdk.RTMStruct.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class RTMSystem extends RTMUser {
        /** 用户下线
         */
    public void bye() {
        bye(true);
    }

    /**
     * @param async 用户下线
     */
    public void bye(boolean async) {
        sayBye(async);
    }

    /**
     *添加key_value形式的变量（例如设置客户端信息，会保存在当前链接中） async
     * @param callback IRTMEmptyCallback回调
     * @param attrs     客户端自定义属性值
     */
    public void addAttributes(Map<String, String> attrs,  UserInterface.IRTMEmptyCallback callback) {
        Quest quest = new Quest("addattrs");
        quest.param("attrs", attrs);
        sendQuestEmptyCallback(callback,quest);
    }

    /**
     *添加key_value形式的变量（例如设置客户端信息，会保存在当前链接中） async
     * @param attrs     客户端自定义属性值
     */
    public RTMAnswer addAttributes(Map<String, String> attrs){
        Quest quest = new Quest("addattrs");
        quest.param("attrs", attrs);
        return sendQuestEmptyResult(quest);
    }

    /**
     * 获取用户属性 async
     * @param callback  用户属性回调 其中map的key
     *                  map中自动添加如下几个参数：
     *                  login：登录时间，utc时间戳
     *                  my：当前链接的attrs
     */
    public void getAttributes( final UserInterface.IRTMCallback<List<Map<String, String>>> callback) {
        Quest quest = new Quest("getattrs");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                List<Map<String, String>> attributes = new ArrayList<>();
                if (errorCode == okRet)
                    attributes = rtmUtils.wantListHashMap(answer, "attrs");
                callback.onResult(attributes, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     *获取用户属性 async
     * @return      AttrsStruct
     */
    public AttrsStruct getAttributes(){
        Quest quest = new Quest("getattrs");
        Answer answer = sendQuest(quest);
        RTMAnswer result = genRTMAnswer(answer);
        AttrsStruct ret = new AttrsStruct();
        ret.errorCode = result.errorCode;
        ret.errorMsg = result.errorMsg;
        if (ret.errorCode == RTMErrorCode.RTM_EC_OK.value())
            ret.attrs = rtmUtils.wantListHashMap(answer,"attrs");

        return ret;
    }

    /**
     * 添加debug日志
     * @param callback  IRTMEmptyCallback回调(notnull)
     * @param message   消息内容
     * @param attrs     消息属性信息
     */
    public void addDebugLog(String message, String attrs,  UserInterface.IRTMEmptyCallback callback) {
        Quest quest = new Quest("adddebuglog");
        quest.param("msg", message);
        quest.param("attrs", attrs);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 添加debug日志
     * @param message   消息内容
     * @param attrs     消息属性信息
     * @return          RTMAnswer
     */
    public RTMAnswer addDebugLog( String message,  String attrs){
        Quest quest = new Quest("adddebuglog");
        quest.param("msg", message);
        quest.param("attrs", attrs);
        return sendQuestEmptyResult(quest);
    }

    /**
     * 添加设备，应用信息 async
     * @param  callback  IRTMEmptyCallback回调
     * @param appType     应用类型 fcm(android) 或者 apns(ios)
     * @param deviceToken   设备推送token
     */
    public void addDevice( String appType,  String deviceToken,  UserInterface.IRTMEmptyCallback callback) {
        Quest quest = new Quest("adddevice");
        quest.param("apptype", appType);
        quest.param("devicetoken", deviceToken);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 删除设备， async
     * @param  callback  IRTMEmptyCallback回调
     * @param deviceToken   设备推送token
     */
    public void removeDevice( String deviceToken,  UserInterface.IRTMEmptyCallback callback) {
        Quest quest = new Quest("removedevice");
        quest.param("devicetoken", deviceToken);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 删除设备， async
     * @param deviceToken   设备推送token
     */
    public RTMAnswer removeDevice(String deviceToken){
        Quest quest = new Quest("removedevice");
        quest.param("devicetoken", deviceToken);
        return sendQuestEmptyResult(quest);
    }


    /**设置设备推送属性(注意此接口是设置个人或群组某个类型的type不推送的设置) async
     * @param callback      IRTMEmptyCallback回调
     * @param type  type=0, 设置某个p2p 不推送；type=1, 设置某个group不推送
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes (为空，则所有mtype均不推送;否则表示指定mtype不推送)
     */
    public void addDevicePushOption( int type,  long xid, HashSet<Integer> messageTypes,  UserInterface.IRTMEmptyCallback callback){
        Quest quest = new Quest("addoption");
        quest.param("type", type);
        quest.param("xid", xid);
        if (messageTypes != null)
            quest.param("mtypes", messageTypes);
        sendQuestEmptyCallback(callback, quest);
    }

    /**取消设备推送属性(和addDevicePushOption对应) async
     * @param callback      IRTMEmptyCallback回调
     * @param type  type=0, 取消p2p推送属性；type=1, 取消group推送属性
     * @param xid   当type =0 时 表示userid；当type =1时 表示groupId
     * @param messageTypes  需要取消设置的messagetype集合(如果为空表示什么都不做)
     */
    public void removeDevicePushOption(UserInterface.IRTMEmptyCallback callback,  int type,  long xid, HashSet<Integer> messageTypes){
        Quest quest = new Quest("removeoption");
        quest.param("type", type);
        quest.param("xid", xid);
        if (messageTypes != null)
            quest.param("mtypes", messageTypes);
        sendQuestEmptyCallback(callback, quest);
    }

    /**获取设备推送属性(addDevicePushOption的结果) async
     * @param callback 回调
     */
    public void getDevicePushOption(final UserInterface.IRTMCallback<DevicePushOption> callback) {
        Quest quest = new Quest("getoption");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                DevicePushOption ret = new DevicePushOption();
                if (errorCode == okRet) {
                    ret.p2pPushOptions = rtmUtils.wantDeviceOption(answer,"p2p");
                    ret.groupPushOptions = rtmUtils.wantDeviceOption(answer,"group");
                }
                callback.onResult(ret, genRTMAnswer(answer,errorCode));
            }
        });
    }


    /**
     * 获取存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节) async
     * @param key      key值
     * @param callback  获取value回调
     */
    public void dataGet( final UserInterface.IRTMCallback<String> callback,  String key) {
        Quest quest = new Quest("dataget");
        quest.param("key", key);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                String value = "";
                if (errorCode == okRet)
                    value = answer.getString("val");
                callback.onResult(value, genRTMAnswer(answer,errorCode));
            }
        });
    }


    /**
     * 获取存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节) sync
     * @param key      key值
     * @return    存储的数据信息
     */
    public DataInfo dataGet( String key) {
        Quest quest = new Quest("dataget");
        quest.param("key", key);

        Answer answer = sendQuest(quest);
        DataInfo result = new DataInfo();
        RTMAnswer ret = genRTMAnswer(answer);
        result.errorCode = ret.errorCode;
        result.errorMsg = ret.errorMsg;
        if (answer != null)
            result.info = answer.getString("val");
        return  result;
    }

    /**
     * 设置存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节) async
     * @param key      key值
     * @param callback  IRTMEmptyCallback接口回调
     */
    public void dataSet( final UserInterface.IRTMEmptyCallback callback,  String key,  String value) {
        Quest quest = new Quest("dataset");
        quest.param("key", key);
        quest.param("val", value);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 设置存储的数据信息（仅能操作自己信息）(key:最长128字节，val：最长65535字节) async
     * @param key      key值
     * @param value     设置的value值
     *  return          RTMAnswer
     */
    public RTMAnswer dataSet( String key,  String value) {
        Quest quest = new Quest("dataset");
        quest.param("key", key);
        quest.param("val", value);
        return sendQuestEmptyResult(quest);
    }

    /**
     * 删除存储的数据信息 async
     * @param key      key值
     * @param callback  IRTMEmptyCallback接口回调
     */
    public void dataDelete( String key,  final UserInterface.IRTMEmptyCallback callback) {
        Quest quest = new Quest("datadel");
        quest.param("key", key);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 删除存储的数据信息 async
     * @param key      key值
     * @return    RTMAnswer
     */
    public RTMStruct.RTMAnswer dataDelete( String key){
        Quest quest = new Quest("datadel");
        quest.param("key", key);
        return sendQuestEmptyResult(quest);
    }
}
