~~~c++

    /**图片检测 async(调用此接口需在管理系统启用图片审核系统)
     * @param callback  IRTMCallback<CheckResult>回调
     * @param url       url地址
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void imageCheckURL( String url, String strategyId, UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) 


    /**图片检测 async
     * @param callback  IRTMCallback<CheckResult>回调
     * @param content   图片内容
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/)
     */
    public void imageCheck( byte[] content, String strategyId,  UserInterface.IRTMCallback<RTMStruct.CheckResult> callback)


    /**语音检测 (调用此接口需在管理系统启用语音审核系统)
     * @param callback  IRTMCallback<CheckResult>回调
     * @param url       语音url地址
     * @param lang      语言(详见TranscribeLang.java枚举列表)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void audioCheckURL(String url, String lang, String codec, int srate, String strategyId,UserInterface.IRTMCallback<RTMStruct.CheckResult> callback)


    /**语音检测 
     * @param callback  IRTMCallback<CheckResult>回调
     * @param content   语音内容
     * @param lang      语言(详见TranscribeLang.java枚举列表)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void audioCheck(byte[] content, String lang, String codec, int srate, String strategyId,UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) 


    /**视频检测 async(调用此接口需在管理系统启用视频审核系统)
     * @param callback  IRTMCallback<CheckResult>回调
     * @param url       视频url地址
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void videoCheckURL(String url,String strategyId, UserInterface.IRTMCallback<RTMStruct.CheckResult> callback)


    /**视频检测
     * @param callback  IRTMCallback<CheckResult>回调
     * @param content   视频内容
     * @param videoName   视频名称
     * @param strategyId 对应文本审核的策略编号(https://docs.ilivedata.com/textcheck/technologydocument/introduction/ 可空)
     */
    public void videoCheck(byte[] content, String videoName, String strategyId,UserInterface.IRTMCallback<RTMStruct.CheckResult> callback) 


    /**语音转文字 (调用此接口需在管理系统启用语音识别系统) codec为空则默认为AMR_WB,srate为0或者空则默认为16000
     * @param callback  IRTMCallback<AudioTextStruct>回调
     * @param url       语音url地址
     * @param lang      语言(详见TranscribeLang.java枚举值)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     */
    public void audioToTextURL(String url, String lang, String codec, int srate, UserInterface.IRTMCallback<RTMStruct.AudioTextStruct> callback)



    /**语音转文字
     * @param callback  IRTMCallback<AudioTextStruct>
     * @param content   语音内容
     * @param lang      语言(详见TranscribeLang.java枚举值)
     * @param codec     音频格式(传空默认"AMR_WB")
     * @param srate     采样率(传0默认16000)
     */
    public void audioToText(byte[] content, String lang, String codec, int srate, UserInterface.IRTMCallback<RTMStruct.AudioTextStruct> callback)



    /**
     *发送聊天消息
     * @param callback  IRTMDoubleValueCallback<服务器返回时间,消息id>回调
     * @param uid       目标用户id
     * @param message   聊天消息
     * @param attrs     用户自定义附加信息(可空)
     * @param messageTypes   消息类别
     */
    public void sendChat(long uid, String message, MessageTypes messageTypes, String attrs, UserInterface.IRTMDoubleValueCallback<Long,Long> callback) 


    /**
     *发送聊天消息
     * @param uid       目标用户id
     * @param message   聊天消息
     * @param attrs     用户自定义附加信息(可空)
     * @param messageTypes   消息类别
     * return  ModifyTimeStruct
     */
    public ModifyTimeStruct sendChat(long uid, String message, MessageTypes messageTypes, String attrs)


    /**
     *发送指令消息
     * @param callback  IRTMDoubleValueCallback<服务器返回时间,消息id>回调(服务器返回时间,消息id)
     * @param uid       目标用户id
     * @param message   指令消息
     * @param attrs     附加信息
     * @param messageTypes   消息类别
     */
    public void sendCmd(long uid, String message, MessageTypes messageTypes,String attrs, UserInterface.IRTMDoubleValueCallback<Long,Long> callback) 

    /**
     *发送指令消息
     * @param uid       目标用户id
     * @param message   指令消息
     * @param attrs     附加信息
     * @param messageTypes   消息类别
     * return  ModifyTimeStruct
     */
    public ModifyTimeStruct sendCmd(long uid, String message, MessageTypes messageTypes,String attrs) 


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
    public void getHistoryChat(long toUid, boolean desc, int count, long beginMsec, long endMsec, long lastId, MessageTypes messageTypes, UserInterface.IRTMCallback<RTMStruct.HistoryMessageResult> callback) 


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
    public HistoryMessageResult getHistoryChat(long toUid, boolean desc, int count, long beginMsec, long endMsec, long lastId, MessageTypes messageTypes) 


    /**
     *获取服务器未读消息
     * @param callback  IRTMCallback<Unread> 回调
     * @param clear     是否清除离线提醒(如果不传 默认为true)
     */
    public void getUnread(boolean clear, final UserInterface.IRTMCallback<RTMStruct.Unread> callback)


    /**
     * 获取所有未读会话列表
     * @param clear 是否清除会话未读状态
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     * @param callback
     */
    public void getUnreadConversationList(boolean clear, long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<UnreadConversationInfo> callback) 


    /**
     * 获取群组未读会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getGroupUnreadConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback)


    /**
     * 获取p2p未读会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getP2PUnreadConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback)



    /**
     * 获取所有p2p会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getp2pConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback)


    /**
     * 获取所有group会话列表
     * @param mtime 毫秒级时间戳，大于该时间戳的消息被计为未读消息，传0则默认取上次离线时间。
     * mtypes   消息类型列表，传null 则默认查询mtype为30-50的消息
     */
    public void getGroupConversationList(long mtime, List<Integer> mtypes, final UserInterface.IRTMCallback<List<ConversationInfo>> callback)


    /**
     * 删除p2p会话
     * @param id
     */
    public void removeSession(long id, UserInterface.IRTMEmptyCallback callback)


    /**
     *清除离线提醒 async
     * @param callback EmptyCallback回调
     */
    public void clearUnread(UserInterface.IRTMEmptyCallback callback)



    //===========================[ 翻译,语音识别,敏感词过滤相关 ]=========================//
    /**
     *设置目标翻译语言
     * @param callback  IRTMEmptyCallback回调
     * @param targetLanguage    目标语言(详见TranslateLang.java语言列表)
     */
    public void setTranslatedLanguage(UserInterface.IRTMEmptyCallback callback, String targetLanguage)


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
                          RTMStruct.TranslateType type, RTMStruct.ProfanityType profanity,final UserInterface.IRTMCallback<RTMStruct.TranslatedInfo> callback) 


    /**
     *文本检测 async(调用此接口需在管理系统启用文本审核系统）
     * @param callback      IRTMCallback<CheckResult>回调
     * @param text          需要检测的文本
     */
    public void textCheck(final UserInterface.IRTMCallback<RTMStruct.CheckResult> callback, String text)
~~~