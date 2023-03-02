package com.rtcsdk;

import com.rtcsdk.DuplicatedMessageFilter.MessageCategories;
import com.rtcsdk.RTMStruct.*;
import com.rtcsdk.UserInterface.*;
import java.util.List;

class RTMessage extends RTMMessageCore {
    /**
     * mtype MUST 51-127, else this interface will return false or erroeCode-RTM_EC_INVALID_MTYPE.
     */
    /**
     *发送指定类型消息(async)
     * @param uid       目标用户id
     * @param mtype     消息类型
     * @param message   消息内容
     * @param messageTypes   消息类别
     * @param attrs     客户端自定义信息(可空)
     * @param callback  IRTMCallback<服务器处理时间，消息id>接口回调
     */
    public void sendMessage(long uid, int mtype,  String message, MessageTypes messageTypes, String attrs,  IRTMDoubleValueCallback<Long,Long> callback) {
        internalSendMessage(callback, uid, mtype, message, attrs,  messageTypes);
    }

    /**
     *发送消息(sync)
     * @param uid       目标用户id
     * @param mtype     消息类型
     * @param message   消息内容
     * @param messageTypes   消息类别
     * @param attrs     客户端自定义信息
     * @return          ModifyTimeStruct结构
     */
    public ModifyTimeStruct sendMessage(long uid, int mtype,  String message, MessageTypes messageTypes,String attrs){
        return internalSendMessage(uid, mtype, message, attrs, messageTypes);
    }


    /**
     *发送指定类型消息(二进制数据)
     * @param uid       目标用户id
     * @param mtype     消息类型
     * @param message   消息内容
     * @param attrs     客户端自定义信息(可空)
     * @param callback  IRTMCallback<服务器处理时间，消息id>
     */
    public void sendMessage(long uid, int mtype,  byte[] message, MessageTypes messageTypes, String attrs,  IRTMDoubleValueCallback<Long,Long> callback) {
        internalSendMessage(callback, uid, mtype, message, attrs,  messageTypes);
    }

    /**
     *发送指定类型消息(二进制数据)(sync)
     * @param uid       目标用户id
     * @param mtype     消息类型
     * @param message   消息内容
     * @param attrs     客户端自定义信息
     * @return          ModifyTimeStruct结构
     */
    public ModifyTimeStruct sendMessage(long uid, int mtype,  byte[] message, MessageTypes messageTypes,String attrs){
        return internalSendMessage(uid, mtype, message, attrs, messageTypes);
    }



    //===========================[ History Messages ]=========================//
    /**
     *获取指定类型消息的历史记录(async)
     * @param toId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     显示条目数
     * @param beginMsec 开始时间戳(毫秒)
     * @param endMsec   结束时间戳(毫秒)
     * @param lastId    最后一条消息索引id(第一次默认填0)
     * @param mtypes    查询的消息类型
     * @param messageTypes    消息类别
     * @param callback  IRTMCallback<HistoryMessageResult> 回调
     */
    public void getHistoryMessage(long toId, boolean desc, int count, long beginMsec, long endMsec, long lastId,  List<Integer> mtypes, MessageTypes messageTypes, IRTMCallback<HistoryMessageResult> callback) {
        getHistoryMessages(callback, toId, desc, count, beginMsec, endMsec, lastId, mtypes,  messageTypes);
    }

    /**
     *获取指定类型消息的历史记录(sync)
     * @param toId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     显示条目数
     * @param beginMsec 开始时间戳(毫秒)
     * @param endMsec   结束时间戳(毫秒)
     * @param lastId    最后一条消息索引id(第一次默认填0)
     * @param mtypes    查询的消息类型
     * @return          HistoryMessageResult
     */
    public HistoryMessageResult getHistoryMessage(long toId, boolean desc, int count, long beginMsec, long endMsec, long lastId,  List<Integer> mtypes, MessageTypes messageTypes){
        return getHistoryMessages(toId, desc, count, beginMsec, endMsec, lastId, mtypes,  messageTypes);
    }

    //===========================[ 获取单条历史记录 ]=========================//
    /**
     *获取单条聊天消息 async
     * @param fromUid   发送者id
     * @param toUid     接收者id
     * @param messageId   消息id
     * @messageTypes 消息类别
     * @param callback IRTMCallback<SingleMessage>回调
     */
    public void getSingleMessage(long fromUid, long toUid, long messageId, MessageTypes messageTypes, IRTMCallback<SingleMessage> callback) {
        getMessage(callback, fromUid, toUid, messageId, messageTypes.value());
    }

    /**获取单条消息 sync
     * @param fromUid   发送者id
     * @param toUid     接收者id
     * @param messageId   消息id
     * @messageTypes 消息类别
     */
    public SingleMessage getSingleMessage(long fromUid, long toUid, long messageId, MessageTypes messageTypes){
        return getMessage(fromUid, toUid, messageId, messageTypes.value());
    }

    /**
     *删除单条消息 async
     * @param messageId   消息id
     * @param fromUid   发送者id
     * @param toUid     接收者id
     * @messageTypes 消息类别
     */
    public void deleteP2PMessage(long fromUid, long toUid, long messageId, MessageTypes messageTypes, IRTMEmptyCallback callback) {
        delMessage(callback,fromUid, toUid, messageId, messageTypes.value());
    }

    /**
     * 删除单条消息 sync
     * @param messageId   消息id
     * @param fromUid   发送者id
     * @param toUid     接收者id
     * @messageTypes 消息类别
     */
    public RTMAnswer deleteP2PMessage(long fromUid, long toUid, long messageId, MessageTypes messageTypes){
        return delMessage(fromUid, toUid, messageId, messageTypes.value());
    }
}
