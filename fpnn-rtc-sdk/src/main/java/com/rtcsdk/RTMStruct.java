package com.rtcsdk;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class RTMStruct {
    public static class RTMAudioStruct {
        public  int duration; //语音时长
        public String lang; //语种
        public byte[] audioData;// 语音内容
        public File file;//语音文件路径
    }

    public enum TranslateType {
        Chat,
        Mail
    }

    public enum RTMModel{
        Normal(0),  //普通RTM
        VOICE(1),  //试试音频
        VIDEO(2);  //实时视频
        private int value;

        RTMModel(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }


    public enum CaptureLevle
    {
        Normal(1),  //一般 320 * 240 15fps 300kbps(默认)
        MIddle(2),  //中等质量 320 * 240 30fps 500kbps
        High(3);    //高质量 640*480 30fps 500kbps
        private int value;

        CaptureLevle(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    //敏感词过滤类型
    public enum ProfanityType {
        Off, //不进行敏感词过滤
        Censor //如果有敏感词 结果用*号代替
    }


    //P2PRTC event
    public enum P2PRTCEvent {
        CancelRequest(1), //对方取消请求
        RingOff(2), //对方挂断
        Accept(3),//对方接受请求
        Refuse(4),//对方拒绝请求
        NoResponse(5);//对方无应答

        private int value;

        P2PRTCEvent(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static P2PRTCEvent intToEnum(int type){
            switch (type) {
                case 1:
                    return CancelRequest;
                case 2:
                    return RingOff;
                case 3:
                    return Accept;
                case 4:
                    return Refuse;
                case 5:
                    return NoResponse;
                default:
                    return NoResponse;
            }
        }
    }


    public enum FileMessageType
    {
        IMAGEFILE(40),  //图片
        AUDIOFILE(41),  //音频文件
        VIDEOFILE(42),  //视频文件
        NORMALFILE(50); //一般文件
        private int value;

        FileMessageType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    //errorCode==0为成功 非0错误 错误信息详见errorMsg字段
    public static class RTMAnswer
    {
        public int errorCode = -1;
        public String errorMsg = "";
        public RTMAnswer(){}
        public RTMAnswer(int _code, String msg){
            errorCode = _code;
            errorMsg = msg;
        }
        public String getErrInfo(){
            return  " " + errorCode + "-" + errorMsg;
        }
    }

    //未读消息结构
    public static class Unread extends RTMAnswer
    {
        public List<Long> p2pList; //uid集合
        public List<Long> groupList;//群组id集合
    }

    //未读消息条目数结构
    public static class UnreadNum extends RTMAnswer
    {
        public HashMap<String, Integer> unreadInfo; //key-uid/groupid value-unreadnumber
        public HashMap<String, Integer> latestTime; //每个session的的最新一条消息时间
    }


    public static class MessageType
    {
        public static final int WITHDRAW = 1;
        public static final int GEO = 2;
        public static final int MULTILOGIN = 7; //多点登陆
        public static final int CHAT = 30; //聊天
        public static final int CMD = 32;//命令
        public static final int REALAUDIO = 35;//实时语音
        public static final int REALVIDEO = 36;//实时视频
        public static final int IMAGEFILE = 40;//图片
        public static final int AUDIOFILE = 41;//音频文件
        public static final int VIDEOFILE = 42;//视频文件
        public static final int NORMALFILE = 50;//一般文件
    }

    //serverpush 消息结构
    public static class RTMMessage
    {
        public long fromUid;    //发送者id 若等于自己uid 说明发送者是自己
        public long toId;       //目标id 根据messagetype 有可能是uid/groupid/roomid
        public byte messageType;  //消息类型 常规聊天类型见 RTMcore enum MessageType 用户可以自定义messagetype 51-127
        public long messageId;  //消息id
        public String stringMessage; //字符串消息
        public byte[] binaryMessage; //二进制消息
        public String attrs;        //客户端发送消息自定义的附加信息
        public long modifiedTime;   //服务器处理返回时间
        public FileStruct fileInfo; //文件结构信息(语音的信息也存在这里)
        public TranslatedInfo translatedInfo = new TranslatedInfo(); //聊天信息结构(push)

        public String getInfo()
        {
            String info ="";
            StringBuilder kk = new StringBuilder();
            JSONObject all = new JSONObject();
            JSONObject file = new JSONObject();
            JSONObject trans = new JSONObject();
            try {

                all.put("fromUid", fromUid);
                all.put("toId", toId);
                all.put("messageType", messageType);
                all.put("messageId", messageId);
                all.put("stringMessage", stringMessage);
                all.put("binaryMessage", binaryMessage==null?0:binaryMessage.length);
                all.put("attrs", attrs);
                all.put("modifiedTime", modifiedTime);

                if (fileInfo!=null) {
                    file.put("url", fileInfo.url);
                    file.put("duration", fileInfo.duration);
                    file.put("fileSize", fileInfo.fileSize);
                    file.put("lang", fileInfo.lang);
                    file.put("surl", fileInfo.surl);
                    file.put("isrtmaudio", fileInfo.isRTMaudio);
                    all.put("fileInfo", file);
                }
                if (translatedInfo != null) {
                    trans.put("source", translatedInfo.source);
                    trans.put("target", translatedInfo.target);
                    trans.put("sourceText", translatedInfo.sourceText);
                    trans.put("targetText", translatedInfo.targetText);
                    all.put("transInfo", trans);
                }

            info = all.toString(10);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return info;
        }
    }

    public static class HistoryMessage extends RTMMessage //历史消息结构
    {
        public long cursorId;       //历史消息的索引id
    }

    //getmsg单条消息结构
    public static class SingleMessage extends RTMAnswer{
        public long cusorId; //消息的索引id
        public byte messageType;  ////消息类型 常规聊天类型见 RTMcore enum MessageType 用户可以自定义messagetype 51-127
        public String stringMessage; //文本数据
        public byte[] binaryMessage;//二进制数据
        public String attrs;    //消息的附加属性信息(客户端自定义)
        public long modifiedTime;   //服务器应答时间
        public FileStruct fileInfo; //文件类型结构 messageType为FileMessageType的都存在这里

        public String getInfo()
        {
            String info ="";
            JSONObject all = new JSONObject();
            JSONObject file = new JSONObject();
            try {

                all.put("cusorId", cusorId);
                all.put("messageType", messageType);
                all.put("stringMessage", stringMessage);
                all.put("binaryMessage", binaryMessage==null?0:binaryMessage.length);
                all.put("attrs", attrs);
                all.put("modifiedTime", modifiedTime);

                if (fileInfo!=null) {
                    file.put("url", fileInfo.url);
                    file.put("duration", fileInfo.duration);
                    file.put("fileSize", fileInfo.fileSize);
                    file.put("lang", fileInfo.lang);
                    file.put("surl", fileInfo.surl);
                    all.put("fileInfo", file);
                }
                info = all.toString(10);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return info;
        }
    }

    public static class TranslatedInfo extends RTMAnswer//聊天消息结构
    {
        public String source = ""; //原语言
        public String target = ""; //翻译的目标语言
        public String sourceText = ""; //原文本
        public String targetText = ""; //设置自动翻译后的目标文本
    }

    public static class CheckResult extends RTMAnswer{ //文本/语音/图片/视频检测结构
        public int result = -1;      //检测结果 0-通过 2-不通过
        public String text;     //(只对文本检测接口)返回text，文本内容,含有的敏感词会被替换为*，如果检测通过,则无此字段
        public List<String> wlist; //(只对文本检测接口)敏感词列表
        public List<Integer> tags; //触发的分类，比如涉黄涉政等等，(只有审核结果不通过才有此值)
    }

    public static class GroupInfoStruct extends RTMAnswer{
        public String  publicInfo = ""; //群组/房间公开信息
        public String  privateInfo = ""; //群组/房间私有信息
    }


    public static class AudioTextStruct extends RTMAnswer{
        public String  text = ""; //语音转文字的结果
        public String  lang = ""; //语音转文字的语言
    }


    public static class ModifyTimeStruct extends RTMAnswer{
        public long  modifyTime; //服务器返回时间
        public long  messageId = 0; //消息id
    }

    //历史消息结果 需要循环调用
    public static class HistoryMessageResult extends RTMAnswer{
        public int count;   //实际返回消息条数
        public long lastId; //最后一条消息的索引id
        public long beginMsec; //开始时间戳(毫秒)
        public long endMsec;    //结束时间戳(毫秒)
        public List<HistoryMessage> messages; //历史消息详细信息结构集合
    }

    public static class MembersStruct extends RTMAnswer{
        public  HashSet<Long> uids = new HashSet<>(); //用户列表
        public  HashSet<Long> onlineUids = new HashSet<>(); //在线用户列表
    }

    public static class DataInfo extends RTMAnswer{
        public String info = "";
    }

    public static class MemberCount extends RTMAnswer{
        public Map<Long,Integer>  memberCounts;
    }

    public static class RoomMemberCount extends RTMAnswer{
        public int count;
    }

    public static class GroupCount extends RTMAnswer{
        public int totalCount; //总人数
        public int onlineCount; //在线人数
    }

    public static class RoomInfo extends RTMAnswer{
        public int roomTyppe; //房间类型 1-voice 2-video 3-语聊房
        public long roomId; //房间id
        public long owner;//房主
        public HashSet<Long> uids; //房间的成员uid
        public HashSet<Long> managers; //房间的管理员id
    }


    public static class AttrsStruct extends RTMAnswer{
        public List<Map<String, String>> attrs;
        //  map中自动添加如下几个参数：
        //  ce：链接的endpoint，需要让其下线可以调用kickout
        //  login：登录时间，utc时间戳
        //  my：当前链接的attrs
    }

    public static class PublicInfo extends RTMAnswer{
        public Map<String, String> publicInfos; //群组/房间/个人的公开信息 key -用户id/群组id/房间id
    }


    public static class DevicePushOption extends RTMAnswer{
        public Map<Long, HashSet<Integer>> p2pPushOptions; //p2p的不推送设置 key-uid value-messagetypes （如果集合里有0 表示所有type均不推送）
        public Map<Long, HashSet<Integer>> groupPushOptions; //group的不推送设置 key-groupid value-messagetypes（如果集合里有0 表示所有type均不推送）
    }

    public static class FileStruct{ //serverpush的文件结构
        public String url = "";     //文件的url地址 图片/语音/视频/普通文件
        public long fileSize = 0;  //文件大小(字节)
        public String surl = "";    //缩略图的url地址 如果是图片类型 会有此值
        public boolean isRTMaudio = false; //是否是rtm语音消息
        public String lang = "";    //语言 如果是rtm语音消息 会有此值
        public int duration = 0;   //语音长度(毫秒) 如果是rtm语音消息 会有此值
        public String codec = "";   //语音编码 如果是rtm语音消息 会有此值
        public int srate = 0;       //语音采样率 如果是rtm语音消息 会有此值
    }

    //new protocol
    public  static class ConversationInfo{
        public long toId;       //目标id uid/groupid
        public int unreadNum; //未读数量
        public HistoryMessage lastHistortMessage = new HistoryMessage();//最后一条消息
    }


    //new protocol
    public  static class UnreadConversationInfo{
        public ArrayList<ConversationInfo> groupUnreads = new ArrayList<>(); //群组未读消息
        public ArrayList<ConversationInfo> p2pUnreads = new ArrayList<>();   //p2p未读消息
    }


    public enum MessageTypes {
        P2PMessage          (1), //P2P消息
        GroupMessage        (2), //群组消息
        RoomMessage         (3), //房间消息
        BroadcastMessage    (4); //广播消息
        private int value;
        MessageTypes (int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
    }
}
