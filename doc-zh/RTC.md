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
        public void adminCommand (IRTMEmptyCallback callback, long roomId, HashSet<Long> uids,  int command);
    
    
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
         * 创建RTC房间
         * @roomId 房间id
         * @param callback 回调
         */
        public void createRTCRoom(@NonNull final IRTMCallback<RoomInfo> callback, final long roomId);
    
        /**
         * 进入RTC房间
         * @param callback 回调
         * @param roomId   房间id
         */
        public void enterRTCRoom(@NonNull final IRTMCallback<RoomInfo> callback, final long roomId);


        /**
         * 邀请用户加入RTC房间(非强制，需要对端确认)(发送成功仅代表收到该请求，至于用户最终是否进入房间结果未知)
         * @param callback 回调
         * @param roomId   房间id
         * @param uids     需要邀请的用户列表
         */
        public void inviteUserIntoRTCRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids);
    
        /**
         * 设置目前活跃的房间(仅对语音房间有效)
         * @param roomId
         */
        public RTMAnswer setActivityRoom(long roomId);
    
        /**
         * 切换扬声器听筒(耳机状态下不操作)(默认扬声器)
         * @param usespeaker true-使用扬声器 false-使用听筒
         */
        public void switchOutput(boolean usespeaker);
    
        /**
         * 设置语音开关(开启语音功能或者关闭语音功能(如果为语音功能关闭则麦克风自动关闭)
         * @param status
         */
        public RTMAnswer setVoiceStat(boolean status);
    
        /**离开RTC房间
         * @param roomId   房间id
         */
        public void leaveRTCRoom(final long roomId);


        /**
         * 屏蔽房间某些人的语音
         * @param callback 回调
         * @param roomId   房间id
         * @param uids     屏蔽语音的用户列表
         */
        public void blockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids);
    
        /**
         * 解除屏蔽房间某些人的语音
         * @param callback 回调
         * @param roomId   房间id
         * @param uids     解除屏蔽语音的用户列表
         */
        public void unblockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids);
    
        /**
         * 获取语RTC房间成员列表
         * @param callback 回调<RoomInfo>
         */
        public void getRTCRoomMembers(@NonNull final IRTMCallback<RoomInfo> callback, long roomId);


        /**
         * 获取RTC房间成员个数
         * @param callback 回调
         */
        public void getRTCRoomMemberCount(@NonNull final IRTMCallback<Integer> callback, long roomId);
~~~