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
     * @param callback
     */
    public void sendMessage(long uid, int mtype,  String message, MessageTypes messageTypes, String attrs,  ISendMsgCallBack callback) {
        internalSendMessage(callback, uid, mtype, message, attrs,  messageTypes);
    }

    /**
     *发送指定类型消息(sync)
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
     * @param callback
     */
    public void sendMessage(long uid, int mtype,  byte[] message, MessageTypes messageTypes, String attrs,  ISendMsgCallBack callback) {
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
     *分页获得历史聊天消息(async)
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginMsec 开始时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.beginMsec)
     * @param endMsec   结束时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.endMsec)
     * @param lastId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果HistoryMessageResult的lastId)
     * @param mtypes    获取的指定消息类型
     * @param messageTypes   消息类别
     * @param callback  HistoryMessageResult类型回调
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
     */
    public void getHistoryMessage(long targetId, boolean desc, int count, long beginMsec, long endMsec, long lastId,  List<Integer> mtypes, MessageTypes messageTypes, IRTMCallback<HistoryMessageResult> callback) {
        getHistoryMessages(callback, targetId, desc, count, beginMsec, endMsec, lastId, mtypes,  messageTypes);
    }

    /**
     *分页获得历史聊天消息(async)
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginMsec 开始时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.beginMsec)
     * @param endMsec   结束时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.endMsec)
     * @param lastId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果HistoryMessageResult的lastId)
     * @param mtypes    获取的指定消息类型
     * @param messageTypes   消息类别
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
     */
    public HistoryMessageResult getHistoryMessage(long targetId, boolean desc, int count, long beginMsec, long endMsec, long lastId,  List<Integer> mtypes, MessageTypes messageTypes){
        return getHistoryMessages(targetId, desc, count, beginMsec, endMsec, lastId, mtypes,  messageTypes);
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
