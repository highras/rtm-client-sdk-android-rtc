package com.highras.videoudp;

import android.provider.SyncStateContract;
import android.util.Log;

import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.RTMStruct;

import java.util.Iterator;

/**
 * @author fengzi
 * @date 2022/3/31 16:04
 */
public class MyRTMPushProcessor extends RTMPushProcessor {
    @Override
    public boolean reloginWillStart(long uid, int reloginCount) {
        mylog.log(uid + " 开始重连第 " + reloginCount + "次");
        return true;
    }

    @Override
    public void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount) {
        mylog.log(uid + " 重连完成 " + answer.getErrInfo());

        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().reloginCompleted(uid, successful, answer, reloginCount);
        }
    }

    @Override
    public void rtmConnectClose(long uid) {
        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().rtmConnectClose(uid);
        }
    }

    @Override
    public void kickout() {
        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().kickout();
        }
    }

    @Override
    public void pushEnterRTCRoom(long roomId, long userId, long time) {
        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().pushEnterRTCRoom(roomId, userId, time);
        }
    }

    @Override
    public void pushExitRTCRoom(long roomId, long userId, long time) {
        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().pushExitRTCRoom(roomId, userId, time);
        }
    }

    @Override
    public void pushVoiceTranslate(String text, String slang, long uid) {
        Log.d("fengzi", "pushVoiceTranslate: uid = " + uid + "  text = " + text);
        Iterator<OnProcessorListener> iterator = Constants.onProcessorListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().pushVoiceTranslate(text, slang, uid);
        }
    }
}
