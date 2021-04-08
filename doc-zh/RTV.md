~~~ c++

      //初始化实时语音(需要rtm登陆成功以后才能调用)
      initVoice()

    //---------sync start-------------//
    /**
     * 设置目前活跃的房间(需要先进入房间;设置活跃房间后次才能接收音频信息)
     * @param roomId
     */
    public RTMAnswer setActivityRoom(long roomId)


    /**
     * 设置麦克风讲话状态(需要先设置活跃房间;可以设置只听不说 备注:初始默认关闭)
     * @param status //true-可以说话 false-不可以说话
     */
    public RTMAnswer canSpeak(boolean status)

    /**
     * 设置语音开关(需要先设置活跃房间;开启语音或者关闭语音(不能听也不能说) 备注:默认开启)
     * @param status
     */
    public RTMAnswer setVoiceStat(boolean status)


    /**
     * 创建并进入语音房间
     * @roomId 房间id
     * return RoomInfo
     */
    public RTMAnswer createVoiceRoom(long roomId)


    /**
     * 邀请用户加入语音房间(非强制，需要对端确认)(发送成功仅代表收到该请求，至于用户最终是否进入房间结果未知)
     * @param roomId   房间id
     * @param uids     需要邀请的用户列表
     * return RTMAnswer
     */
    public RTMAnswer inviteUserIntoVoiceRoom(long roomId, HashSet<Long> uids)


    /**离开语音房间
     * @param roomId   房间id
     * return RTMAnswer
     */
    public RTMAnswer leaveVoiceRoom(final long roomId)


    /**
     * 获取语音房间成员列表
     * return RoomInfo
     */
    public RoomInfo getVoiceRoomMembers(long roomId)

    /**
     * 获取语音房间成员个数
     *  return MemberCount
     */
    public VoiceMemberCount getVoiceRoomMemberCount(long roomId)


    /**
     * 屏蔽房间某些人的语音
     * @param roomId   房间id
     * @param uids     屏蔽语音的用户列表
     * return        RTMAnswer
     */
    public RTMAnswer blockUserInVoiceRoom(long roomId, HashSet<Long> uids)


    /**
     * 解除屏蔽房间某些人的语音
     * @param roomId   房间id
     * @param uids     解除屏蔽语音的用户列表
     * return        RTMAnswer
     */
    public RTMAnswer unblockUserInVoiceRoom(long roomId, HashSet<Long> uids)
    //-------------------sync end----------------//



    //-----------async start-------------//
    /**
     * 创建语音房间 async
     * @roomId 房间id
     * @param callback 回调
     */
    public void createVoiceRoom(@NonNull final IRTMEmptyCallback callback, long roomId)


    /**
     * 进入语音房间
     * @param roomId   房间id
     */
    public RTMAnswer enterVoiceRoom(final long roomId)


    /**
     * 进入语音房间 async
     * @param callback 回调
     * @param roomId   房间id
     */
    public void enterVoiceRoom(@NonNull final IRTMEmptyCallback callback, final long roomId)


    /**
     * 邀请用户加入语音房间(非强制，需要对端确认)(发送成功仅代表收到该请求，至于用户最终是否进入房间结果未知)
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     需要邀请的用户列表
     */
    public void inviteUserIntoVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids)


    /**离开语音房间
     * @param callback 回调
     * @param roomId   房间id
     */
    public void leaveVoiceRoom(final IRTMEmptyCallback callback, final long roomId)

    /**
     * 获取语音房间成员列表 async
     * @param callback 回调HashSet<Long>, HashSet<Long> 房间用户id 管理员id
     */
    public void getVoiceRoomMembers(@NonNull final IRTMDoubleValueCallback<HashSet<Long>, HashSet<Long>> callback, long roomId)


    /**
     * 获取语音房间成员个数 async
     * @param callback 回调
     */
    public void getVoiceRoomMemberCount(@NonNull final IRTMCallback<Integer> callback, long roomId)


    /**
     * 屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     屏蔽语音的用户列表
     */
    public void blockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids)


    /**
     * 解除屏蔽房间某些人的语音
     * @param callback 回调
     * @param roomId   房间id
     * @param uids     解除屏蔽语音的用户列表
     */
    public void unblockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids)


    /**
     * 彻底RTM关闭语音(释放资源 如果需要重新使用 需要再次调用initvoice)
     */
    public void closeRTMVoice()
~~~
