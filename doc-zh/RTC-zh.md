~~~ c++
       /**
        *管理员权限操作
        * @param roomId 房间id
        * @param uids
        * @param command
        * 0 赋予管理员权
        * 1 剥夺管理员权限
        * 2 禁止发送音频数据
        * 3 允许发送音频数据
        * 4 禁止发送视频数据
        * 5 允许发送视频数据
        * 6 关闭他人麦克风
        * 7 关闭他人摄像头
        * @return
        */
       public void adminCommand (long roomId, HashSet<Long> uids,  int command, IRTMEmptyCallback callback);

   
       /**
        * 开启摄像头
        */
       public RTMAnswer openCamera();

   
       /**
        * 关闭摄像头
        */
       public void closeCamera();

   
       /**
        * 摄像头切换
        * @param front true-使用前置  false-使用后置
        */
       public void switchCamera(boolean front);

   
       /**
        * 打开麦克风(音频模式进入房间初始默认关闭  视频模式进入房间默认开启)
        */
       public void openMic();

   
       /**
        * 关闭麦克风
        */
       public void closeMic();

   
   
       /**
        * 设置麦克风增益等级(声音自动增益 取值 范围0-10)
        */
       public void setMicphoneLevel(int level);

   
       /**
        * 取消订阅视频流
        * @param roomId 房间id
        * @param uids 取消订阅的成员列表
        * @return
        */
       public void unsubscribeVideo(long roomId, HashSet<Long> uids);

   
    /**
     * 创建RTC房间
     * @roomId 房间id
     * @roomType 1-voice 2-video(视频房间摄像头默认关闭 麦克风默认开启)
     * @param callback 回调
     */
    public void createRTCRoom( final long roomId, final int roomType, @NonNull final IRTMCallback<RoomInfo> callback) 

   
   /**
     * 创建实时翻译RTC房间
     * @roomId 房间id
     * @param language 自己的语言
     * @param callback 回调
     */
    public void createTranslateRTCRoom( final long roomId,final String language, @NonNull final IRTMCallback<RoomInfo> callback) 
    
    
   /**
     * 进入RTC房间
     * @param callback 回调
     * @param roomId   房间id
     * @param lang 自己的语言(当为实时语音翻译房间必传)
     */
    public void enterRTCRoom(final long roomId, String lang, @NonNull final IRTMCallback<RoomInfo> callback) 


       /**
        * 订阅视频流
        * @param roomId 房间id
        * @param userViews  key-订阅的用户id value-显示用户的surfaceview()
        */
       public RTMAnswer subscribeVideos(long roomId, HashMap<Long, SurfaceView> userViews);

       
       /**
        * 邀请用户加入RTC房间(非强制，需要对端确认)(发送成功仅代表收到该请求，至于用户最终是否进入房间结果未知)
        * @param callback 回调
        * @param roomId   房间id
        * @param uids     需要邀请的用户列表
        */
       public void inviteUserIntoRTCRoom(long roomId, HashSet<Long> uids, IRTMEmptyCallback callback);

   
       /**
        * 设置目前活跃的房间(仅对语音房间有效)
        * @param roomId
        */
       public RTMAnswer setActivityRoom(long roomId);

   
       /**
        * 切换扬声器听筒(耳机状态下不操作)(默认扬声器)
        * @param usespeaker true-使用扬声器 false-使用听筒
        */
       public void switchOutput(final boolean usespeaker);

   
       /**
        * 设置语音开关(开启语音功能或者关闭语音功能(备注:默认开启 如果为语音功能关闭则麦克风自动关闭)
        * @param status
        */
       public RTMAnswer setVoiceStat(boolean status);

   
       /**离开RTC房间
        * @param roomId   房间id
        */
       public void leaveRTCRoom(final long roomId, final IRTMEmptyCallback callback);

   
       /**
        * 屏蔽房间某些人的语音
        * @param callback 回调
        * @param roomId   房间id
        * @param uids     屏蔽语音的用户列表
        */
       public void blockUserInVoiceRoom(long roomId, HashSet<Long> uids, IRTMEmptyCallback callback);

       /**
        * 解除屏蔽房间某些人的语音
        * @param callback 回调
        * @param roomId   房间id
        * @param uids     解除屏蔽语音的用户列表
        */
       public void unblockUserInVoiceRoom(long roomId, HashSet<Long> uids, IRTMEmptyCallback callback);

   
   
       /**
        * 获取语RTC房间成员列表
        * @param callback 回调<RoomInfo>
        */
       public void getRTCRoomMembers(long roomId, @NonNull final IRTMCallback<RoomInfo> callback);


       /**
        * 获取RTC房间成员个数
        * @param callback 回调
        */
       public void getRTCRoomMemberCount(long roomId, @NonNull final IRTMCallback<Integer> callback);

   
       /**
        * 切换视频质量
        * @level 视频质量详见RTMStruct.CaptureLevle
        * @return
        */
       public RTMAnswer switchVideoQuality(int level);

   
   
       /**
        * 设置预览view(需要传入的view 真正建立完成)
        * @return
        */
       public void setPreview(SurfaceView view);


  /**
     *发起p2p音视频请求(对方是否回应通过 pushP2PRTCEvent回调接口返回)
     * @param type 1-语音  2-视频
     * @SurfaceView view(如果为实时频频 自己预览的view 需要view创建完成并可用)
     * @param toUid 对方id
     */
    public void requestP2PRTC(final int type , final long toUid, final SurfaceView view, final IRTMEmptyCallback callback)


    /**
     * 取消p2p RTC请求
     * @param callback
     */
    public void cancelP2PRTC(final IRTMEmptyCallback callback)



    /**
     * 关闭p2p 会话
     * @param callback
     */
    public void closeP2PRTC(final IRTMEmptyCallback callback)


    /**
     * 接受p2p 会话
     * @param callback
     * @param preview 自己预览的view(需要view创建完成并可用)
     * @param bindview 对方的view(需要view创建完成并可用)
     */
    public void acceptP2PRTC(final SurfaceView preview, final SurfaceView bindview, final IRTMEmptyCallback callback)


    /**
     * 拒绝p2p 会话
     * @param callback
     */
    public void refuseP2PRTC(final IRTMEmptyCallback callback)

~~~