package com.example.rtvdemo;

import com.rtmsdk.RTMAudio;
import com.rtmsdk.RTMPushProcessor;
import com.rtmsdk.RTMStruct.RTMAnswer;
import com.rtmsdk.RTMStruct.RTMMessage;
import com.rtmsdk.RTMStruct.SingleMessage;
import com.rtmsdk.UserInterface;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;


public class RTMExampleQuestProcessor extends RTMPushProcessor {
    private Object interlock;
    AtomicInteger allCount = new AtomicInteger();

    AtomicInteger roomChatCount = new AtomicInteger();
    AtomicInteger roomCmdCount = new AtomicInteger();
    AtomicInteger roomMessageCount = new AtomicInteger();
    AtomicInteger roomFileCount = new AtomicInteger();
    AtomicInteger groupChatCount = new AtomicInteger();
    AtomicInteger groupCmdCount = new AtomicInteger();
    AtomicInteger groupMessageCount = new AtomicInteger();
    AtomicInteger groupFileCount = new AtomicInteger();

    public RTMExampleQuestProcessor() {
        interlock =  new Object();
    }


    public boolean reloginWillStart(long uid, RTMAnswer answer, int reloginCount) {
//            if (reloginCount >= 6) {return false;}
        mylog.log(uid + " 开始重连第 " + reloginCount + "次");
//            TestClass.mySleep(1);
        return true;
    }

    public void reloginCompleted(long uid, boolean successful, RTMAnswer answer, int reloginCount) {
        mylog.log(uid + " 重连结束 结果 " + answer.getErrInfo() + " 重连次数 " + reloginCount);
//        if (successful)
//        {
//            TestClass.pushClients.get(101).enterRoom(TestClass.roomId);
//        }
    }

    public void rtmConnectClose(long uid) {
        synchronized (interlock) {
            TestClass.lastCloseTime = System.currentTimeMillis();
            mylog.log(uid + " rtmconnect closed ");
        }
    }

    public void kickout() {
        synchronized (interlock) {
            mylog.log("Received kickout.");
        }
    }

    public void kickoutRoom(long roomId) {
        synchronized (interlock) {
            mylog.log("Kickout from room " + roomId);
        }
    }

    void getMessageSync(String sourceMethod, int type, long fromId, long toid, long messageId)
    {
//        try {
//            Thread.sleep(100);
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
        String showmethod = "";
        SingleMessage ll = null;
        if (type == 0){
            showmethod = "getP2PMessage";
//            ll = TestClass.pushClients.get(TestClass.peerUid).getP2PMessage(fromId, toid, messageId);
            ll = TestClass.pushClients.get(TestClass.peerUid).getP2PMessage(fromId, toid, messageId);
        }
        else if (type == 1) {
            showmethod = "getGroupMessage";
            ll = TestClass.client.getGroupMessage(fromId, toid, messageId);
        }
        else if (type == 2) {
            showmethod = "getRoomMessage";
            ll = TestClass.client.getRoomMessage(fromId, toid, messageId);
        }
        else if (type == 3) {
            showmethod = "getBroadcastMessage";
            ll = TestClass.client.getBroadcastMessage(messageId);
        }

        if (ll.errorCode == 0)
            mylog.log(sourceMethod + " "+showmethod+ " "+ ll.getInfo());
        else
            mylog.log(sourceMethod + " "+showmethod+ " error " + ll.getErrInfo());
    }

    void getMessageAsync(final String sourceMethod, int type, long fromId, long toid, long messageId)
    {
        try {
            Thread.sleep(100);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        SingleMessage ll = null;
        if (type == 0){
            TestClass.client.getP2PMessage(new UserInterface.IRTMCallback<SingleMessage>() {
                @Override
                public void onResult(SingleMessage singleMessage, RTMAnswer answer) {
                    if (answer.errorCode == 0)
                        mylog.log(sourceMethod + " getP2PMessage "+ singleMessage.getInfo());
                    else
                        mylog.log(sourceMethod + " getP2PMessage error " + answer.getErrInfo());
                }
            },fromId, toid, messageId);
        }
        else if (type == 1) {
            TestClass.client.getGroupMessage(new UserInterface.IRTMCallback<SingleMessage>() {
                @Override
                public void onResult(SingleMessage singleMessage, RTMAnswer answer) {
                    if (answer.errorCode == 0)
                        mylog.log(sourceMethod + " getGroupMessage "+ singleMessage.getInfo());
                    else
                        mylog.log(sourceMethod + " getGroupMessage error " + answer.getErrInfo());
                }
            },fromId, toid, messageId);
        }
        else if (type == 2) {
            TestClass.client.getGroupMessage(new UserInterface.IRTMCallback<SingleMessage>() {
                @Override
                public void onResult(SingleMessage singleMessage, RTMAnswer answer) {
                    if (answer.errorCode == 0)
                        mylog.log(sourceMethod + " getRoomMessage "+ singleMessage.getInfo());
                    else
                        mylog.log(sourceMethod + " getRoomMessage error " + answer.getErrInfo());
                }
            },fromId, toid, messageId);
        }
        else if (type == 3) {
            TestClass.client.getBroadcastMessage(new UserInterface.IRTMCallback<SingleMessage>() {
                @Override
                public void onResult(SingleMessage singleMessage, RTMAnswer answer) {
                    if (answer.errorCode == 0)
                        mylog.log(sourceMethod + " getBroadcastMessage "+ singleMessage.getInfo());
                    else
                        mylog.log(sourceMethod + " getBroadcastMessage error " + answer.getErrInfo());
                }
            },messageId);
        }
    }

    //-- message for String format
    public void pushMessage(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive pushMessage " + message.getInfo());
/*            getMessageSync("pushMessage", 0,message.fromUid,message.toId,message.messageId);
            getMessageAsync("pushMessage", 0,message.fromUid,message.toId,message.messageId);*/
        }
    }

    public void pushGroupMessage(RTMMessage message) {
            mylog.log("receive  pushGroupMessage " + message.getInfo()+ " " + groupMessageCount.incrementAndGet());
/*            getMessageSync("pushGroupMessage", 1,message.fromUid,message.toId,message.messageId);
            getMessageAsync("pushGroupMessage", 1,message.fromUid,message.toId,message.messageId);*/

    }

    public void pushRoomMessage(RTMMessage message) {
            mylog.log("receive  pushRoomMessage " + message.getInfo() + " " + roomMessageCount.incrementAndGet());
//            getMessageSync("pushRoomMessage", 2,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushRoomMessage", 2,message.fromUid,message.toId,message.messageId);

    }

    public void pushBroadcastMessage(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive  pushBroadcastMessage " + message.getInfo());
//            getMessageSync("pushBroadcastMessage", 3,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushBroadcastMessage", 3,message.fromUid,message.toId,message.messageId);
        }
    }

    public void pushChat(RTMMessage message) {
            mylog.log("receive  pushChat time " + System.currentTimeMillis()+ " info "+ message.getInfo());
//            RTMAnswer ll = TestClass.client.deleteP2PMessage(message.fromUid,message.toId,message.messageId);
//            mylog.log(ll.getErrInfo());
//            getMessageSync("pushChat", 0,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushChat", 0,message.fromUid,message.toId,message.messageId);
    }

    public void pushGroupChat(RTMMessage message) {
            mylog.log("receive  pushGroupChat " + message.getInfo() + " " + groupChatCount.incrementAndGet());
//            getMessageSync("pushGroupChat", 1,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushGroupChat", 1,message.fromUid,message.toId,message.messageId);

    }

    public void pushRoomChat(RTMMessage message) {
        JSONObject ll = new JSONObject();
        ll = null;
        ll.has("el");
            mylog.log("receive  pushRoomChat " + message.getInfo() + " " + roomChatCount.incrementAndGet());
//            getMessageSync("pushRoomChat", 2,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushRoomChat", 2,message.fromUid,message.toId,message.messageId);

    }

    public void pushBroadcastChat(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive  pushBroadcastChat " + message.getInfo());
//            getMessageSync("pushBroadcastChat", 3,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushBroadcastChat", 3,message.fromUid,message.toId,message.messageId);
        }
    }

    public void pushBroadcastAudio(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive  pushBroadcastAudio " + message.getInfo());
//            getMessageSync("pushBroadcastAudio", 3,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushBroadcastAudio", 3,message.fromUid,message.toId,message.messageId);
        }
    }

    public void pushCmd(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive  pushCmd " + message.getInfo());
//            getMessageSync("pushCmd", 0,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushCmd", 0,message.fromUid,message.toId,message.messageId);
        }
    }

    public void pushGroupCmd(RTMMessage message) {
            mylog.log("receive  pushGroupCmd " + message.getInfo()+ " " + groupCmdCount.incrementAndGet());
//            getMessageSync("pushGroupCmd", 1,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushGroupCmd", 1,message.fromUid,message.toId,message.messageId);

    }

    public void pushRoomCmd(RTMMessage message) {
            mylog.log("receive  pushRoomCmd " + message.getInfo() + " " + roomCmdCount.incrementAndGet());
//            getMessageSync("pushRoomCmd", 2,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushRoomCmd", 2,message.fromUid,message.toId,message.messageId);

    }

    public void pushBroadcastCmd(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive  pushBroadcastCmd " + message.getInfo());
//            getMessageSync("pushBroadcastCmd", 3,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushBroadcastCmd", 3,message.fromUid,message.toId,message.messageId);
        }
    }

    public void pushFile(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive  pushFile " + message.getInfo());
            if(message.fileInfo.isRTMaudio)
            {
                byte[] jj = TestClass.httpGetFile(message.fileInfo.url);
                RTMAudio.getInstance().broadAudio(jj);
            }
//            getMessageSync("pushFile", 0,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushFile", 0,message.fromUid,message.toId,message.messageId);
        }
    }

    public void pushGroupFile(RTMMessage message) {
            mylog.log("receive  pushGroupFile " + message.getInfo()+ " " + groupFileCount.incrementAndGet());
//            if(message.fileInfo.isRTMaudio)
//            {
//                byte[] jj = TestClass.httpGetFile(message.fileInfo.url);
//                RTMAudio.getInstance().broadAduio(jj);
//            }
//            getMessageSync("pushGroupFile", 1,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushGroupFile", 1,message.fromUid,message.toId,message.messageId);

    }

    public void pushRoomFile(RTMMessage message) {
            mylog.log("receive  pushRoomFile " + message.getInfo() + " " + roomFileCount.incrementAndGet());
//            if(message.fileInfo.isRTMaudio)
//            {
//                byte[] jj = TestClass.httpGetFile(message.fileInfo.url);
//                RTMAudio.getInstance().broadAduio(jj);
//            }
//            getMessageSync("pushRoomFile", 2,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushRoomFile", 2,message.fromUid,message.toId,message.messageId);

    }

    public void pushBroadcastFile(RTMMessage message) {
        synchronized (interlock) {
            mylog.log("receive  pushBroadcastFile " + message.getInfo());
//            getMessageSync("pushBroadcastFile", 3,message.fromUid,message.toId,message.messageId);
//            getMessageAsync("pushBroadcastFile", 3,message.fromUid,message.toId,message.messageId);
        }
    }
}
