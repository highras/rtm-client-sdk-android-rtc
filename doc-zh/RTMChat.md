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
     * @param callback 
     * @param uid       目标用户id
     * @param message   聊天消息
     * @param attrs     用户自定义附加信息(可空)
     * @param messageTypes   消息类别
     */
    public void sendChat(long uid, String message, MessageTypes messageTypes, String attrs, UserInterface.ISendMsgCallBack<Long,Long> callback) 


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
     * @param callback 
     * @param uid       目标用户id
     * @param message   指令消息
     * @param attrs     附加信息
     * @param messageTypes   消息类别
     */
    public void sendCmd(long uid, String message, MessageTypes messageTypes,String attrs, UserInterface.ISendMsgCallBack<Long,Long> callback) 

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
    public void sendMessage(long uid, int mtype,  String message, MessageTypes messageTypes, String attrs,  ISendMsgCallBack callback) 

    /**
     *发送指定类型消息(sync)
     * @param uid       目标用户id
     * @param mtype     消息类型
     * @param message   消息内容
     * @param messageTypes   消息类别
     * @param attrs     客户端自定义信息
     * @return          ModifyTimeStruct结构
     */
    public ModifyTimeStruct sendMessage(long uid, int mtype,  String message, MessageTypes messageTypes,String attrs)


    /**
     *发送指定类型消息(二进制数据)
     * @param uid       目标用户id
     * @param mtype     消息类型
     * @param message   消息内容
     * @param attrs     客户端自定义信息(可空)
     * @param callback
     */
    public void sendMessage(long uid, int mtype,  byte[] message, MessageTypes messageTypes, String attrs,  ISendMsgCallBack callback) 

    /**
     *发送指定类型消息(二进制数据)(sync)
     * @param uid       目标用户id
     * @param mtype     消息类型
     * @param message   消息内容
     * @param attrs     客户端自定义信息
     * @return          ModifyTimeStruct结构
     */
    public ModifyTimeStruct sendMessage(long uid, int mtype,  byte[] message, MessageTypes messageTypes,String attrs)


    /**
     *分页获得历史聊天消息(async)
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginMsec 开始时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.beginMsec)
     * @param endMsec   结束时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.endMsec)
     * @param lastId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果HistoryMessageResult的lastId)
     * @param messageTypes   消息类别
     * @param callback  HistoryMessageResult类型回调
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
     */
    public void getHistoryChat(long toUid, boolean desc, int count, long beginMsec, long endMsec, long lastId, MessageTypes messageTypes, UserInterface.IRTMCallback<RTMStruct.HistoryMessageResult> callback) 


    /**
     *分页获得历史聊天消息(sync)
     * @param targetId   目标id(对于Broadcast类消息 可传任意值)
     * @param desc      是否按时间倒叙排列
     * @param count     获取条数(后台配置 最多建议20条)
     * @param beginMsec 开始时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.beginMsec)
     * @param endMsec   结束时间戳(毫秒)(可选默认0,第二次查询传入上次结果的HistoryMessageResult.endMsec)
     * @param lastId    索引id(可选默认0，第一次获取传入0 第二次查询传入上次结果HistoryMessageResult的lastId)
     * @param messageTypes   消息类别
     * 注意: 建议时间和id不要同时传入 通过时间查询是左右闭区间(beginMsec<=x<=endMsec)
     *       通过id查询是开区间lastId<x
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
    public void sendFile(long peerUid, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioInfo,IRTMDoubleValueCallback<Long,Long> callback )

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
    public void  sendGroupFile(long groupId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs,RTMAudioStruct audioInfo,IRTMDoubleValueCallback<Long,Long> callback)

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
    public void  sendRoomFile(long roomId, FileMessageType mtype, byte[] fileContent, String filename, JSONObject attrs, RTMAudioStruct audioInfo, IRTMDoubleValueCallback<Long,Long> callback)


    /**
     * 发送文件并返回url
     * @param mtype 文件类型
     * @param fileName 文件名字(需要带后缀)
     * @param fileContent  文件内容
     * @param audioInfo  RTM录音完成返回的结构
     * @param callback IRTMDoubleValueCallback<文件url,文件大小>回调
     */
    public void uploadFile(final FileMessageType mtype, final String fileName, final byte[] fileContent, final RTMAudioStruct audioInfo, final IRTMDoubleValueCallback<String,Long> callback)
    
~~~