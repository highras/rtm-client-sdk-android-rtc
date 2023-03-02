package com.rtcsdk;

import com.fpnn.sdk.ErrorCode;
import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.Quest;
import com.rtcsdk.UserInterface.*;
import com.rtcsdk.RTMStruct.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class RTMGroup extends RTMFile {
    /**
     * 添加群组用户 async(注意 调用接口的用户必须在群组里)
     * @param callback  IRTMEmptyCallback回调
     * @param groupId   群组id
     * @param uids      用户id集合
     * */
    public void addGroupMembers(long groupId,HashSet<Long> uids,final IRTMEmptyCallback callback) {
        Quest quest = new Quest("addgroupmembers");
        quest.param("gid", groupId);
        quest.param("uids", uids);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 删除群组用户   async
     * @param callback  IRTMEmptyCallback回调
     * @param groupId   群组id
     * @param uids      用户id集合
     * */
    public void deleteGroupMembers(long groupId,HashSet<Long> uids,  final IRTMEmptyCallback callback) {
        Quest quest = new Quest("delgroupmembers");
        quest.param("gid", groupId);
        quest.param("uids", uids);
        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 获取群组人数   async
     * @param callback  IRTMCallback回调
     * @param groupId   群组id
     * */
    public void getGroupCount(long groupId,final IRTMCallback<GroupCount> callback) {
        Quest quest = new Quest("getgroupcount");
        quest.param("gid", groupId);
        quest.param("online", true);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                GroupCount retgroup = new GroupCount();
                int totalCount = 0,  onlineCount = 0;
                if (errorCode == ErrorCode.FPNN_EC_OK.value()) {
                    retgroup.totalCount = rtmUtils.wantInt(answer, "cn");
                    retgroup.onlineCount = rtmUtils.wantInt(answer, "online");
                }
                callback.onResult(retgroup,genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取群组用户   async
     * @param callback  IRTMCallback回调
     * @param groupId   群组id
     * */
    public void getGroupMembers( final IRTMCallback<MembersStruct> callback, long groupId) {
        Quest quest = new Quest("getgroupmembers");
        quest.param("gid", groupId);
        quest.param("online", true);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                MembersStruct retmap = new MembersStruct();
                if (errorCode == RTMErrorCode.RTM_EC_OK.value()) {
                    retmap.uids = rtmUtils.wantLongHashSet(answer, "uids");
                    retmap.onlineUids = rtmUtils.wantLongHashSet(answer, "onlines");
                }
                callback.onResult(retmap,genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取群组用户   sync
     * @param groupId   群组id
     * return  MembersStruct用户id集合
     * */
    public MembersStruct getGroupMembers(long groupId){
        Quest quest = new Quest("getgroupmembers");
        quest.param("gid", groupId);
        quest.param("online", true);

        Answer answer = sendQuest(quest);
        RTMAnswer result = genRTMAnswer(answer);
        MembersStruct ret = new MembersStruct();
        ret.errorCode = result.errorCode;
        ret.errorMsg = result.errorMsg;
        if (ret.errorCode == RTMErrorCode.RTM_EC_OK.value()) {
            ret.uids = rtmUtils.wantLongHashSet(answer, "uids");
            ret.onlineUids = rtmUtils.wantLongHashSet(answer, "onlines");
        }
        return ret;
    }

    /**
     * 获取群组用户人数   sync
     * @param groupId   群组id
     * return  GroupCount
     * */
    public GroupCount getGroupCount(long groupId){
        Quest quest = new Quest("getgroupcount");
        quest.param("gid", groupId);
        quest.param("online", true);

        Answer answer = sendQuest(quest);
        RTMAnswer result = genRTMAnswer(answer);
        GroupCount ret = new GroupCount();
        ret.errorCode = result.errorCode;
        ret.errorMsg = result.errorMsg;
        if (ret.errorCode == RTMErrorCode.RTM_EC_OK.value()) {
            ret.totalCount = rtmUtils.wantInt(answer, "cn");
            ret.onlineCount = rtmUtils.wantInt(answer, "online");
        }
        return ret;
    }


    /**
     * 获取其他用户的公开信息，每次最多获取100个群组
     * @param callback IRTMCallback<Map<String, String>>回调
     * @param gids     房间id集合
     */
    public void getGroupsOpeninfo( HashSet<Long> gids,final IRTMCallback<Map<String, String>> callback) {
        Quest quest = new Quest("getgroupsopeninfo");
        quest.param("gids",gids);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                Map<String, String> attributes = new HashMap<>();
                if (errorCode == okRet) {
                    attributes = rtmUtils.wantStringMap(answer, "info");
                }
                callback.onResult(attributes, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取群组的公开信息，每次最多获取100个群组
     * @param gids        群组id集合
     *return              PublicInfo 结构
     */
    public PublicInfo getGroupsOpeninfo( HashSet<Long> gids) {
        Quest quest = new Quest("getgroupsopeninfo");
        quest.param("gids", gids);

        Answer answer = sendQuest(quest);
        RTMAnswer result = genRTMAnswer(answer);
        PublicInfo ret = new PublicInfo();
        ret.errorCode = result.errorCode;
        ret.errorMsg = result.errorMsg;
        if (ret.errorCode == RTMErrorCode.RTM_EC_OK.value())
            ret.publicInfos = rtmUtils.wantStringMap(answer, "info");

        return ret;
    }

    /**
     * 获取用户所在的群组   async
     * @param callback  IRTMCallback回调
     * */
    public void getUserGroups( final IRTMCallback<HashSet<Long>> callback) {
        Quest quest = new Quest("getusergroups");

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                HashSet<Long> groupIds =new HashSet<>();
                if (errorCode == okRet)
                    groupIds = rtmUtils.wantLongHashSet(answer,"gids");
                callback.onResult(groupIds, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取用户所在的群组   sync
     * @return  MembersStruct
     * */
    public MembersStruct getUserGroups(){
        Quest quest = new Quest("getusergroups");

        Answer answer = sendQuest(quest);
        RTMAnswer result = genRTMAnswer(answer);
        MembersStruct ret = new MembersStruct();
        ret.errorCode = result.errorCode;
        ret.errorMsg = result.errorMsg;
        if (ret.errorCode == RTMErrorCode.RTM_EC_OK.value())
            ret.uids = rtmUtils.wantLongHashSet(answer,"gids");

        return ret;
    }

    /**
     * 设置群组的公开信息或者私有信息 async
     * @param callback  IRTMEmptyCallback回调
     * @param groupId   群组id
     * @param publicInfo    群组公开信息
     * @param privateInfo   群组 私有信息
     */
    public void setGroupInfo(long groupId, String publicInfo, String privateInfo,IRTMEmptyCallback callback) {
        Quest quest = new Quest("setgroupinfo");
        quest.param("gid", groupId);
        if (publicInfo != null)
            quest.param("oinfo", publicInfo);
        if (privateInfo != null)
            quest.param("pinfo", privateInfo);

        sendQuestEmptyCallback(callback, quest);
    }

    /**
     * 设置群组的公开信息或者私有信息 sync
     * @param groupId   群组id
     * @param publicInfo    群组公开信息
     * @param privateInfo   群组 私有信息
     */
    public RTMAnswer setGroupInfo(long groupId, String publicInfo, String privateInfo){
        Quest quest = new Quest("setgroupinfo");
        quest.param("gid", groupId);
        if (publicInfo != null)
            quest.param("oinfo", publicInfo);
        if (privateInfo != null)
            quest.param("pinfo", privateInfo);

        return sendQuestEmptyResult(quest);
    }

    /**
     * 获取群组的公开信息或者私有信息 async
     * @param callback  IRTMCallback回调
     * @param groupId   群组id
     */
    public void getGroupInfo(final long groupId,final IRTMCallback<GroupInfoStruct> callback) {
        Quest quest = new Quest("getgroupinfo");
        quest.param("gid", groupId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                GroupInfoStruct groupInfo = new GroupInfoStruct();
                if (errorCode == okRet) {
                    groupInfo.publicInfo = rtmUtils.wantString(answer,"oinfo");
                    groupInfo.privateInfo = rtmUtils.wantString(answer,"pinfo");
                }
                callback.onResult(groupInfo, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取群组的公开信息或者私有信息 sync
     * @param groupId   群组id
     * @return  GroupInfoStruct结构
     */
    public GroupInfoStruct getGroupInfo(long groupId){
        Quest quest = new Quest("getgroupinfo");
        quest.param("gid", groupId);

        Answer answer = sendQuest(quest);
        RTMAnswer result = genRTMAnswer(answer);
        GroupInfoStruct ret = new GroupInfoStruct();
        if (result.errorCode == RTMErrorCode.RTM_EC_OK.value()) {
            ret.publicInfo = rtmUtils.wantString(answer,"oinfo");
            ret.privateInfo = rtmUtils.wantString(answer,"pinfo");
        }
        ret.errorCode = result.errorCode;
        ret.errorMsg = result.errorMsg;
        return ret;
    }


    /**
     * 获取群组的公开信息 async
     * @param callback  MessageCallback回调
     * @param groupId   群组id
     */
    public void getGroupPublicInfo(long groupId,final IRTMCallback<String>  callback ) {
        Quest quest = new Quest("getgroupopeninfo");
        quest.param("gid", groupId);

        sendQuest(quest, new FunctionalAnswerCallback() {
            @Override
            public void onAnswer(Answer answer, int errorCode) {
                String publicInfo = "";
                if (errorCode == okRet)
                    publicInfo = rtmUtils.wantString(answer,"oinfo");

                callback.onResult(publicInfo, genRTMAnswer(answer,errorCode));
            }
        });
    }

    /**
     * 获取群组的公开信息 sync
     * @param groupId   群组id
     * @return      GroupInfoStruct 群组公开信息
     */
    public GroupInfoStruct getGroupPublicInfo(long groupId){
        Quest quest = new Quest("getgroupopeninfo");
        quest.param("gid", groupId);

        Answer answer = sendQuest(quest);
        RTMAnswer result = genRTMAnswer(answer);
        GroupInfoStruct ret = new GroupInfoStruct();
        ret.errorCode = result.errorCode;
        ret.errorMsg = result.errorMsg;
        if (result.errorCode == RTMErrorCode.RTM_EC_OK.value())
            ret.publicInfo = rtmUtils.wantString(answer,"oinfo");
        return ret;
    }
}
