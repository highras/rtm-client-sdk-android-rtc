~~~ c++
    //---------sync start-------------//
    /**
     * set activity room
     * @param roomId
     */
    public RTMAnswer setActivityRoom(long roomId)

    /**
     * set micphone status(default false)
     * @param status //true-can speak false-can not speak
     */
    public RTMAnswer canSpeak(boolean status)

    /**
     * set voice stat(default true)
     * @param status
     */
    public RTMAnswer setVoiceStat(boolean status)


    /**
     * create voice room and enter
     * @roomId
     * return RoomInfo
     */
    public RTMAnswer createVoiceRoom(long roomId)


    /**
     * invite User Into VoiceRoom(need the other side confirm)
     * @param roomId
     * @param uids     user ids
     * return RTMAnswer
     */
    public RTMAnswer inviteUserIntoVoiceRoom(long roomId, HashSet<Long> uids)


    /**leave voice room
     * @param roomId
     * return RTMAnswer
     */
    public RTMAnswer leaveVoiceRoom(final long roomId)xw


    /**
     * get voice room members
     * @param roomId
     * return RoomInfo
     */
    public RoomInfo getVoiceRoomMembers(long roomId)

    /**
    * @param roomId
     * get Voice RoomMemberCount
     *  return MemberCount
     */
    public VoiceMemberCount getVoiceRoomMemberCount(long roomId)


    /**
     * block User voice In VoiceRoom
     * @param roomId
     * @param uids     user ids
     * return        RTMAnswer
     */
    public RTMAnswer blockUserInVoiceRoom(long roomId, HashSet<Long> uids)


    /**
     * enter voice room
     * @param roomId
     */
    public RTMAnswer enterVoiceRoom(final long roomId)


    /**
     * unblock User voice In VoiceRoom
     * @param roomId
     * @param uids     user ids
     * return        RTMAnswer
     */
    public RTMAnswer unblockUserInVoiceRoom(long roomId, HashSet<Long> uids)
    //-------------------sync end----------------//



    //-----------async start-------------//
    /**

    public void createVoiceRoom(@NonNull final IRTMEmptyCallback callback, long roomId)


    public void enterVoiceRoom(@NonNull final IRTMEmptyCallback callback, final long roomId)



    public void inviteUserIntoVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids)



    public void leaveVoiceRoom(final IRTMEmptyCallback callback, final long roomId)


    public void getVoiceRoomMembers(@NonNull final IRTMDoubleValueCallback<HashSet<Long>, HashSet<Long>> callback, long roomId)



    public void getVoiceRoomMemberCount(@NonNull final IRTMCallback<Integer> callback, long roomId)



    public void blockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids)


    public void unblockUserInVoiceRoom(IRTMEmptyCallback callback, long roomId, HashSet<Long> uids)


    /**
     * close rtm voice(release source)
     */
    public void closeRTMVoice()
~~~