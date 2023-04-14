package com.rtcsdk;

import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;

import java.util.ArrayList;
import java.util.List;
import com.rtcsdk.RTMStruct.*;


class RTMChat extends RTMRoom {
    private String defaultCodec = "AMR_WB";
    private int sample_rate = 16000;

    private List<Integer> chatMTypes = new ArrayList<Integer>() {
        {
            add(RTMStruct.MessageType.CHAT);
            add(RTMStruct.MessageType.CMD);
            add(RTMStruct.MessageType.IMAGEFILE);
            add(RTMStruct.MessageType.AUDIOFILE);
            add(RTMStruct.MessageType.VIDEOFILE);
            add(RTMStruct.MessageType.NORMALFILE);
        }
    };
    private enum CheckType {
        PIC,
        AUDIO,
        VIDEO
    }

    //图片/音频/视频检测类型
    private enum CheckSourceType {
        URL, //url地址
        CONTENT //二进制内容
    }

    /**图片检测 async(调用此接口需在管理系统启用图片审核系统)
     * @param callback  IRTMCallback<CheckResult>回调
     * @param url       url地址
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void imageCheckURL( String url, String strategyId, UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) {
        checkContentAsync(callback, url, CheckSourceType.URL, CheckType.PIC, "", null,"",0, strategyId);
    }


    /**图片检测 async
     * @param callback  IRTMCallback<CheckResult>回调
     * @param content   图片内容
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/)
     */
    public void imageCheck( byte[] content, String strategyId,  UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) {
        checkContentAsync(callback, content, CheckSourceType.CONTENT, CheckType.PIC, "", null,"",0, strategyId);
    }


    /**语音检测 (调用此接口需在管理系统启用语音审核系统)
     * @param callback  IRTMCallback<CheckResult>回调
     * @param url       语音url地址
     * @param lang      语言(详见TranscribeLang.java枚举列表)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void audioCheckURL(String url, String lang, String codec, int srate, String strategyId,UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) {
        checkContentAsync(callback, url, CheckSourceType.URL, CheckType.AUDIO, "", lang,codec, srate, strategyId);
    }

    /**语音检测 
     * @param callback  IRTMCallback<CheckResult>回调
     * @param content   语音内容
     * @param lang      语言(详见TranscribeLang.java枚举列表)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void audioCheck(byte[] content, String lang, String codec, int srate, String strategyId,UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) {
        checkContentAsync(callback, content, CheckSourceType.CONTENT, CheckType.AUDIO, "", lang, codec, srate, strategyId);
    }


    /**视频检测 async(调用此接口需在管理系统启用视频审核系统)
     * @param callback  IRTMCallback<CheckResult>回调
     * @param url       视频url地址
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void videoCheckURL(String url,String strategyId, UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) {
        checkContentAsync(callback, url, CheckSourceType.URL, CheckType.VIDEO, "", null,"",0, strategyId);
    }

    /**视频检测
     * @param callback  IRTMCallback<CheckResult>回调
     * @param content   视频内容
     * @param videoName   视频名称
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void videoCheck(byte[] content, String videoName, String strategyId,UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) {
        checkContentAsync(callback, content, CheckSourceType.CONTENT, CheckType.VIDEO, videoName, null, "",0, strategyId);
    }

    /**语音转文字 (调用此接口需在管理系统启用语音识别系统) codec为空则默认为AMR_WB,srate为0或者空则默认为16000
     * @param callback  IRTMCallback<AudioTextStruct>回调
     * @param url       语音url地址
     * @param lang      语言(详见TranscribeLang.java枚举值)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     */
    public void audioToTextURL(String url, String lang, String codec, int srate, UserInterface.IRTMCallback<RTMStruct.AudioTextStruct> callback) {
        audioToTextAsync(callback, url,CheckSourceType.URL, lang, codec, srate);
    }


    /**语音转文字
     * @param callback  IRTMCallback<AudioTextStruct>
     * @param content   语音内容
     * @param lang      语言(详见TranscribeLang.java枚举值)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     */
    public void audioToText(byte[] content, String lang, String codec, int srate, UserInterface.IRTMCallback<RTMStruct.AudioTextStruct> callback) {
        audioToTextAsync(callback, content, CheckSourceType.CONTENT, lang, codec, srate);
    }


    /**
     *发送聊天消息
     * @param callback  IRTMDoubleValueCallback<服务器返回时间,消息id>回调
     * @param uid       目标用户id
     * @param message   聊天消息
     * @param attrs     用户自定义附加信息(可空)
     * @param messageTypes   消息类别
     */
    public void sendChat(long uid, String message, MessageTypes messageTypes, String attrs, UserInterface.IRTMDoubleValueCallback<Long,Long> callback) {
        internalSendChat(callback, uid, RTMStruct.MessageType.CHAT, message, attrs, messageTypes);
    }

    /**
     *发送聊天消息
     * @param uid       目标用户id
     * @param message   聊天消息
     * @param attrs     用户自定义附加信息(可空)
     * @param messageTypes   消息类别
     * return  ModifyTimeStruct
     */
    public ModifyTimeStruct sendChat(long uid, String message, MessageTypes messageTypes, String attrs){
        return internalSendChat( uid, RTMStruct.MessageType.CHAT, message, attrs, messageTypes);
    }

    /**
     *发送指令消息
     * @param callback  IRTMDoubleValueCallback<服务器返回时间,消息id>回调(服务器返回时间,消息id)
     * @param uid       目标用户id
     * @param message   指令消息
     * @param attrs     附加信息
     * @param messageTypes   消息类别
     */
    public void sendCmd(long uid, String message, MessageTypes messageTypes,String attrs, UserInterface.IRTMDoubleValueCallback<Long,Long> callback) {
        internalSendChat(callback, uid, RTMStruct.MessageType.CMD, message, attrs, messageTypes);
    }

    /**
     *发送指令消息
     * @param uid       目标用户id
     * @param message   指令消息
     * @param attrs     附加信息
     * @param messageTypes   消息类别
     * return  ModifyTimeStruct
     */
    public ModifyTimeStruct sendCmd(long uid, String message, MessageTypes messageTypes,String attrs) {
        return internalSendChat(uid, RTMStruct.MessageType.CMD, message, attrs, messageTypes);
    }


    /**
     *获得历史聊天消息(async)
     * @param toUid   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     显示条目数
     * @param beginMsec 开始时间戳(毫秒)
     * @param endMsec   结束时间戳(毫秒)
     * @param lastId    最后一条消息索引id(第一次默认填0)
     * @param messageTypes   消息类别
     * @param callback  HistoryMessageResult类型回调
     */
    public void getHistoryChat(long toUid, boolean desc, int count, long beginMsec, long endMsec, long lastId, MessageTypes messageTypes, UserInterface.IRTMCallback<RTMStruct.HistoryMessageResult> callback) {
        getHistoryMessages(callback, toUid, desc, count, beginMsec, endMsec, lastId, chatMTypes, messageTypes);
    }


    /**
     * 获得历史聊天消息(sync)
     * @param toUid   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     显示条目数
     * @param beginMsec 开始时间戳(毫秒)
     * @param endMsec   结束时间戳(毫秒)
     * @param lastId    最后一条消息索引id(第一次默认填0)
     * return   HistoryMessageResult
     */
    public HistoryMessageResult getHistoryChat(long toUid, boolean desc, int count, long beginMsec, long endMsec, long lastId, MessageTypes messageTypes) {
        return getHistoryMessage( toUid, desc, count, beginMsec, endMsec, lastId, chatMTypes, messageTypes);
    }

    /**
     *获取服务器未读消息
     * @param callback  IRTMCallback<Unread> 回调
     * @param clear     是否清除离线提醒(如果不传 默认为true)
     */
    public void getUnread(boolean clear, final UserInterface.IRTMCallback<RTMStruct.Unread> callback) {
        Quest quest = new Quest("getunread");
        quest.param("clear", clear);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTMStruct.Unread ret = new RTMStruct.Unread();
                if (errorCode == okRet) {
                    List<Long> p2pList = new ArrayList<>();
                    List<Long> groupList = new ArrayList<>();
                    rtmUtils.wantLongList(answer,"p2p", p2pList);
                    rtmUtils.wantLongList(answer,"group", groupList);
                    ret.p2pList = p2pList;
                    ret.groupList = groupList;
                }
                callback.onResult(ret, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取所有未读会话列表
     * @param clear 是否清除会话未读状态
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     * @param callback
     */
    public void getUnreadConversationList(boolean clear, long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<UnreadConversationInfo> callback) {
        Quest quest = new Quest("getunreadconversationlist");
        quest.param("clear", clear);
        if (mtime > 0)
            quest.param("mtime", mtime);
        if (mtypes != null)
            quest.param("mtypes", mtypes);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                UnreadConversationInfo unreadConversationInfo = new UnreadConversationInfo();
                if (errorCode == ErrorCode.FPNN_EC_OK.value()) {
                    ArrayList<List<Object>> groupmsgs = (ArrayList<List<Object>>) answer.want("groupMsgs");
                    ArrayList<Long> groupconversations = new ArrayList<>();
                    ArrayList<Integer> groupUnreads = new ArrayList<>();
                    rtmUtils.wantLongList(answer, "groupConversations", groupconversations);
                    rtmUtils.wantIntList(answer, "groupUnreads", groupUnreads);
                    unreadConversationInfo.groupUnreads = parseHis(groupmsgs, groupconversations, groupUnreads);

                    ArrayList<List<Object>> p2pMsgs = (ArrayList<List<Object>>) answer.want("p2pMsgs");
                    ArrayList<Long> p2pConversations = new ArrayList<>();
                    ArrayList<Integer> p2pUnreads = new ArrayList<>();
                    rtmUtils.wantLongList(answer, "p2pConversations", p2pConversations);
                    rtmUtils.wantIntList(answer, "p2pUnreads", p2pUnreads);
                    unreadConversationInfo.p2pUnreads = parseHis(p2pMsgs, p2pConversations, p2pUnreads);
                }
                callback.onResult(unreadConversationInfo, genRTMAnswer(answer,errorCode));
            }
        });

    }

    /**
     * 获取群组未读会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getGroupUnreadConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback){
        Quest quest = new Quest("getgroupunreadconversationlist");
        if (mtime > 0)
            quest.param("mtime", mtime);
        if (mtypes != null)
            quest.param("mtypes", mtypes);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                ArrayList<ConversationInfo> Conversationinfos = new ArrayList<>();
                if (errorCode == ErrorCode.FPNN_EC_OK.value()) {
                    ArrayList<List<Object>> msgs = (ArrayList<List<Object>>) answer.want("msgs");
                    ArrayList<Long> conversations = new ArrayList<>();
                    ArrayList<Integer> unreadNums = new ArrayList<>();
                    rtmUtils.wantLongList(answer, "conversations", conversations);
                    rtmUtils.wantIntList(answer, "unreads", unreadNums);

                    Conversationinfos = parseHis(msgs, conversations, unreadNums);
                }
                callback.onResult(Conversationinfos, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取p2p未读会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getP2PUnreadConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback){
        Quest quest = new Quest("getp2punreadconversationlist");
        if (mtime > 0)
            quest.param("mtime", mtime);
        if (mtypes != null)
            quest.param("mtypes", mtypes);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                ArrayList<ConversationInfo> Conversationinfos = new ArrayList<>();
                if (errorCode == ErrorCode.FPNN_EC_OK.value()) {
                    ArrayList<List<Object>> msgs = (ArrayList<List<Object>>) answer.want("msgs");
                    ArrayList<Long> conversations = new ArrayList<>();
                    ArrayList<Integer> unreadNums = new ArrayList<>();
                    rtmUtils.wantLongList(answer, "conversations", conversations);
                    rtmUtils.wantIntList(answer, "unreads", unreadNums);

                    Conversationinfos = parseHis(msgs, conversations, unreadNums);
                }
                callback.onResult(Conversationinfos, genRTMAnswer(answer,errorCode));
            }
        });
    }


    /**
     * 获取所有p2p会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getp2pConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback){
        Quest quest = new Quest("getp2pconversationlist");
        if (mtime > 0)
            quest.param("mtime", mtime);
        if (mtypes != null)
            quest.param("mtypes", mtypes);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                ArrayList<ConversationInfo> Conversationinfos = new ArrayList<>();
                if (errorCode == ErrorCode.FPNN_EC_OK.value()) {
                    ArrayList<List<Object>> msgs = (ArrayList<List<Object>>) answer.want("msgs");
                    ArrayList<Long> conversations = new ArrayList<>();
                    ArrayList<Integer> unreadNums = new ArrayList<>();
                    rtmUtils.wantLongList(answer, "conversations", conversations);
                    rtmUtils.wantIntList(answer, "unreads", unreadNums);

                    Conversationinfos = parseHis(msgs, conversations, unreadNums);
                }
                callback.onResult(Conversationinfos, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取所有group会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getGroupConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback){
        Quest quest = new Quest("getgroupconversationlist");
        if (mtime > 0)
            quest.param("mtime", mtime);
        if (mtypes != null)
            quest.param("mtypes", mtypes);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                ArrayList<ConversationInfo> Conversationinfos = new ArrayList<>();
                if (errorCode == ErrorCode.FPNN_EC_OK.value()) {
                    ArrayList<List<Object>> msgs = (ArrayList<List<Object>>) answer.want("msgs");
                    ArrayList<Long> conversations = new ArrayList<>();
                    ArrayList<Integer> unreadNums = new ArrayList<>();
                    rtmUtils.wantLongList(answer, "conversations", conversations);
                    rtmUtils.wantIntList(answer, "unreads", unreadNums);

                    Conversationinfos = parseHis(msgs, conversations, unreadNums);
                }
                callback.onResult(Conversationinfos, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 删除p2p会话
     * @param id
     */
    public void removeSession(long id, UserInterface.IRTMEmptyCallback callback){
        Quest quest = new Quest("removesession");
        quest.param("toid", id);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     *清除离线提醒 async
     * @param callback EmptyCallback回调
     */
    public void clearUnread(UserInterface.IRTMEmptyCallback callback) {
        Quest quest = new Quest("cleanunread");
        sendQuestEmptyCallback(callback, quest);
    }


    //===========================[ 翻译,语音识别,敏感词过滤相关 ]=========================//
    /**
     *设置目标翻译语言
     * @param callback  IRTMEmptyCallback回调
     * @param targetLanguage    目标语言(详见TranslateLang.java语言列表)
     */
    public void setTranslatedLanguage(UserInterface.IRTMEmptyCallback callback, String targetLanguage) {
        String slang ="";
        if (targetLanguage!=null)
            slang = targetLanguage;
        Quest quest = new Quest("setlang");
        quest.param("lang", slang);
        sendQuestEmptyCallback(callback, quest);
    }


    /**
     *文本翻译 (调用此接口需在管理系统启用翻译系统）
     * @param callback      IRTMCallback<TranslatedInfo>回调
     * @param text          需要翻译的内容
     * @param destinationLanguage   目标语言
     * @param sourceLanguage        源文本语言
     * @param type                  可选值为chat或mail。如未指定，则默认使用'chat'
     * @param profanity             对翻译结果进行敏感语过滤。设置为以下2项之一: off, censor，默认：off
     */
    public void translate( String text, String destinationLanguage, String sourceLanguage,
                          RTMStruct.TranslateType type, RTMStruct.ProfanityType profanity,final UserInterface.IRTMCallback<RTMStruct.TranslatedInfo> callback) {
        Quest quest = new Quest("translate");
        quest.param("text", text);
        quest.param("dst", destinationLanguage);

        if (sourceLanguage.length() > 0)
            quest.param("src", sourceLanguage);

        if (type == RTMStruct.TranslateType.Mail)
            quest.param("type", "mail");
        else
            quest.param("type", "chat");

        if (profanity != null) {
            switch (profanity) {
                case Censor:
                    quest.param("profanity", "censor");
                    break;
                case Off:
                    quest.param("profanity", "off");
                    break;
            }
        }

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTMStruct.TranslatedInfo tm = new RTMStruct.TranslatedInfo();
                if (errorCode == okRet) {
                    tm.source = rtmUtils.wantString(answer,"source");
                    tm.target = rtmUtils.wantString(answer,"target");
                    tm.sourceText = rtmUtils.wantString(answer,"sourceText");
                    tm.targetText = rtmUtils.wantString(answer,"targetText");
                }
                callback.onResult(tm, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     *文本检测 async(调用此接口需在管理系统启用文本审核系统）
     * @param callback      IRTMCallback<CheckResult>回调
     * @param text          需要检测的文本
     */
    public void textCheck(final UserInterface.IRTMCallback<RTMStruct.CheckResult> callback, String text){
        Quest quest = new Quest("tcheck");
        quest.param("text", text);

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTMStruct.CheckResult checkResult = new RTMStruct.CheckResult();
                if (errorCode == okRet) {
                    List<Integer> tags = new ArrayList<>();
                    List<String> wlist = new ArrayList<>();
                    checkResult.text = answer.getString("text");
                    rtmUtils.getIntList(answer,"tags",tags);
                    rtmUtils.getStringList(answer,"wlist",wlist);
                    checkResult.tags = tags;
                    checkResult.wlist = wlist;
                }
                callback.onResult(checkResult, genRTMAnswer(answer,errorCode));
            }
        });
    }

    private void audioToTextAsync(final UserInterface.IRTMCallback<RTMStruct.AudioTextStruct> callback, Object content, CheckSourceType type, String lang, String codec, int srate)
    {
        String sendcodec = codec==null?defaultCodec:codec;
        int  sendsrate = srate==0?sample_rate:srate;

        Quest quest = new Quest("speech2text");
        quest.param("audio", content);
        if (type == CheckSourceType.URL)
            quest.param("type", 1);
        else if (type == CheckSourceType.CONTENT)
            quest.param("type", 2);
        quest.param("lang", lang);
        quest.param("codec", sendcodec);
        quest.param("srate", sendsrate);

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTMStruct.AudioTextStruct audioTextStruct = new RTMStruct.AudioTextStruct();
                if (errorCode == okRet) {
                    audioTextStruct.text = answer.getString("text");
                    audioTextStruct.lang = answer.getString("lang");
                }
                callback.onResult(audioTextStruct, genRTMAnswer(answer,errorCode));
            }
        });
    }

    private void checkContentAsync(final UserInterface.IRTMCallback<RTMStruct.CheckResult> callback, Object content, CheckSourceType type, CheckType checkType, String videoName, String lang, String codec, int srate
            ,String strategyId)
    {
        String method = "", rucankey = "";
        String sendcodec = codec==null?defaultCodec:codec;
        int  sendsrate = srate==0?sample_rate:srate;

        int sourfeType = 1;
        if (checkType == CheckType.PIC) {
            method = "icheck";
            rucankey = "image";
        }
        else if (checkType == CheckType.AUDIO) {
            method = "acheck";
            rucankey = "audio";
        }
        else if (checkType == CheckType.VIDEO) {
            method = "vcheck";
            rucankey = "video";
        }

        if (type == CheckSourceType.CONTENT)
            sourfeType = 2;

        Quest quest = new Quest(method);
        quest.param("type", sourfeType);
        if (strategyId!=null){
            quest.param("strategyId", strategyId);
        }

        quest.param(rucankey, content);
        if (checkType == CheckType.VIDEO) {
            quest.param("videoName", videoName);
        }
        else if (checkType == CheckType.AUDIO) {
            quest.param("lang", lang);
            quest.param("codec", sendcodec);
            quest.param("srate", sendsrate);
        }

        sendFileQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                RTMStruct.CheckResult checkResult = new RTMStruct.CheckResult();
                if (errorCode == okRet) {
                    checkResult.result = rtmUtils.wantInt(answer,"result");
                    if (checkResult.result == 2){
                        List<Integer> tags = new ArrayList<>();
                        rtmUtils.wantIntList(answer,"tags",tags);
                        checkResult.tags = tags;
                    }
                }
                callback.onResult(checkResult, genRTMAnswer(answer,errorCode));
            }
        });
    }
}
