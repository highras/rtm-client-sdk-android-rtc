~~~ java
    /**
     * 添加群组用户 async(注意 调用接口的用户必须在群组里)
     * @param callback  IRTMEmptyCallback回调
     * @param groupId   群组id
     * @param uids      用户id集合
     * */
    public void addGroupMembers(long groupId,HashSet<Long> uids,final IRTMEmptyCallback callback) 

    /**
     * 删除群组用户   async
     * @param callback  IRTMEmptyCallback回调
     * @param groupId   群组id
     * @param uids      用户id集合
     * */
    public void deleteGroupMembers(long groupId,HashSet<Long> uids,  final IRTMEmptyCallback callback) 


    /**
     * 获取群组人数   async
     * @param callback  IRTMCallback回调
     * @param groupId   群组id
     * */
    public void getGroupCount(long groupId,final IRTMCallback<GroupCount> callback) 


    /**
     * 获取群组用户   async
     * @param callback  IRTMCallback回调
     * @param groupId   群组id
     * */
    public void getGroupMembers( final IRTMCallback<MembersStruct> callback, long groupId) 


    /**
     * 获取群组用户   sync
     * @param groupId   群组id
     * return  MembersStruct用户id集合
     * */
    public MembersStruct getGroupMembers(long groupId)

    /**
     * 获取群组用户人数   sync
     * @param groupId   群组id
     * return  GroupCount
     * */
    public GroupCount getGroupCount(long groupId)


    /**
     * 获取其他用户的公开信息，每次最多获取100个群组
     * @param callback IRTMCallback<Map<String, String>>回调
     * @param gids     房间id集合
     */
    public void getGroupsOpeninfo( HashSet<Long> gids,final IRTMCallback<Map<String, String>> callback) 

    /**
     * 获取群组的公开信息，每次最多获取100个群组
     * @param gids        群组id集合
     *return              PublicInfo 结构
     */
    public PublicInfo getGroupsOpeninfo( HashSet<Long> gids)

    /**
     * 获取用户所在的群组   async
     * @param callback  IRTMCallback回调
     * */
    public void getUserGroups( final IRTMCallback<HashSet<Long>> callback) 

    /**
     * 获取用户所在的群组   sync
     * @return  MembersStruct
     * */
    public MembersStruct getUserGroups()

    /**
     * 设置群组的公开信息或者私有信息 async
     * @param callback  IRTMEmptyCallback回调
     * @param groupId   群组id
     * @param publicInfo    群组公开信息
     * @param privateInfo   群组 私有信息
     */
    public void setGroupInfo(long groupId, String publicInfo, String privateInfo,IRTMEmptyCallback callback) 


    /**
     * 设置群组的公开信息或者私有信息 sync
     * @param groupId   群组id
     * @param publicInfo    群组公开信息
     * @param privateInfo   群组 私有信息
     */
    public RTMAnswer setGroupInfo(long groupId, String publicInfo, String privateInfo)


    /**
     * 获取群组的公开信息或者私有信息 async
     * @param callback  IRTMCallback回调
     * @param groupId   群组id
     */
    public void getGroupInfo(final long groupId,final IRTMCallback<GroupInfoStruct> callback)


    /**
     * 获取群组的公开信息或者私有信息 sync
     * @param groupId   群组id
     * @return  GroupInfoStruct结构
     */
    public GroupInfoStruct getGroupInfo(long groupId)


    /**
     * 获取群组的公开信息 async
     * @param callback  MessageCallback回调
     * @param groupId   群组id
     */
    public void getGroupPublicInfo(long groupId,final IRTMCallback<String>  callback ) 

    /**
     * 获取群组的公开信息 sync
     * @param groupId   群组id
     * @return      GroupInfoStruct 群组公开信息
     */
    public GroupInfoStruct getGroupPublicInfo(long groupId)


    //ROOM interface
    /**
     * 进入房间 async
     * @param callback IRTMEmptyCallback回调
     * @param roomId   房间id
     */
    public void enterRoom(long roomId,  IRTMEmptyCallback callback) 

    /**
     * 进入房间 sync
     * @param roomId  房间id
     */
    public RTMAnswer enterRoom(long roomId)


    /**
     * 离开房间 async
     * @param callback IRTMEmptyCallback回调
     * @param roomId   房间id
     */
    public void leaveRoom(long roomId,  IRTMEmptyCallback callback)


    /**
     * 获取房间中的所有member sync(由于分布式系统，房间的人数会有几秒同步间隔)
     * @param roomId  房间id
     */
    public MembersStruct getRoomMembers(long roomId)


    /**
     * 获取房间中的所有人数 async(由于分布式系统，房间的人数会有几秒同步间隔)
     * @param callback IRTMEmptyCallback回调
     * @param rids   房间id
     */
    public void getRoomCount(HashSet<Long> rids,  final IRTMCallback<Map<Long,Integer>> callback)



    /**
     * 获取房间中的所有人数 sync(由于分布式系统，房间的人数会有几秒同步间隔)
     * @param rids  房间id
     */
    public MemberCount getRoomCount(HashSet<Long> rids)


    /**
     * 获取房间中的所有member async(由于分布式系统，房间的人数会有几秒同步间隔)
     * @param callback IRTMEmptyCallback回调
     * @param roomId   房间id
     */
    public void getRoomMembers(long roomId,  final IRTMCallback<HashSet<Long>> callback)


    /**
     * 离开房间 sync
     * @param roomId  房间id
     */
    public RTMAnswer leaveRoom(long roomId)


    /**
     * 获取用户所在的房间   async
     * @param callback IRTMCallback回调
     */
    public void getUserRooms( final IRTMCallback<HashSet<Long>> callback)


    /**
     * 获取用户所在的房间   sync
     * @return  用户所在房间集合
     * */
    public MembersStruct getUserRooms()


    /**
     * 设置房间的公开信息或者私有信息 async
     * @param callback  IRTMEmptyCallback回调
     * @param roomId   房间id
     * @param publicInfo    房间公开信息
     * @param privateInfo   房间 私有信息
     */
    public void setRoomInfo(long roomId, String publicInfo, String privateInfo,  IRTMEmptyCallback callback)


    /**
     * 设置房间的公开信息或者私有信息 sync
     * @param roomId   房间id
     * @param publicInfo    房间公开信息
     * @param privateInfo   房间 私有信息
     */
    public RTMAnswer setRoomInfo(long roomId, String publicInfo, String privateInfo)



    /**
     * 获取房间的公开信息或者私有信息 async
     * @param callback  IRTMCallback回调
     * @param roomId   房间id
     */
    public void getRoomInfo(final long roomId,  final IRTMCallback<GroupInfoStruct> callback)



    /**
     * 获取房间的公开信息或者私有信息 sync
     * @param roomId   房间id
     * @return  GroupInfoStruct结构
     */
    public GroupInfoStruct getRoomInfo(long roomId)



    /**
     * 获取房间的公开信息 async
     * @param callback  IRTMCallback回调
     * @param roomId   房间id
     */
    public void getRoomPublicInfo(long roomId,  final IRTMCallback<String>  callback)



    /**
     * 获取房间的公开信息 sync
     * @param roomId   房间id
     * @return  GroupInfoStruct
     */
    public GroupInfoStruct getRoomPublicInfo(long roomId)


    /**
     * 获取房间的公开信息，每次最多获取100个
     * @param callback IRTMCallback<Map<String, String>>回调
     * @param rids     房间id集合
     */
    public void getRoomsOpeninfo(HashSet<Long> rids,  final IRTMCallback<Map<String, String>> callback)


    
    
    --Friend Interface---
    /**
     * 添加好友 async
     * @param callback IRTMEmptyCallback回调
     * @param uids   用户id集合
     */
    public void addFriends(  HashSet<Long> uids,  IRTMEmptyCallback callback)

    

    /**
     * 添加好友 sync
     * @param uids   用户id集合
     */
    public RTMAnswer addFriends( HashSet<Long> uids)


    /**
     * 删除好友 async
     * @param callback IRTMEmptyCallback回调
     * @param uids   用户id集合
     */
    public void deleteFriends( HashSet<Long> uids,  IRTMEmptyCallback callback)


    /**
     * 删除好友 sync
     * @param uids   用户id集合
     */
    public RTMAnswer deleteFriends( HashSet<Long> uids)


    /**
     * 查询自己好友 async
     * @param callback MembersCallback回调
     */
    public void getFriends( final IRTMCallback<HashSet<Long>> callback)


    /**
     * 查询自己好友 sync
     * @return 好友id集合
     */
    public MembersStruct getFriends()


    /**
     * 添加黑名单 async
     * @param callback IRTMEmptyCallback回调
     * @param uids   用户id集合
     */
    public void addBlacklist( HashSet<Long> uids,  IRTMEmptyCallback callback)


    /**
     * 添加黑名单 sync
     * @param uids   用户id集合
     */
    public RTMAnswer addBlacklist( HashSet<Long> uids)


    /**
     * 删除黑名单用户 async
     * @param callback IRTMEmptyCallback回调
     * @param uids   用户id集合
     */
    public void delBlacklist



    /**
     * 删除黑名单用户 sync
     * @param uids   用户id集合
     */
    public RTMAnswer delBlacklist( HashSet<Long> uids)


    /**
     * 查询黑名单 async
     * @param callback MembersCallback回调
     */
    public void getBlacklist( final IRTMCallback<HashSet<Long>> callback)


    /**
     * 查询黑名单 sync
     * @return 黑名单id集合
     */
    public MembersStruct getBlacklist()

~~~