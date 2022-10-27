package com.highras.videoudp;

import com.rtcsdk.RTMStruct;

/**
 * @author fengzi
 * @date 2022/3/31 16:05
 */
public interface OnProcessorListener {
    boolean reloginWillStart(long uid, int reloginCount);

    void reloginCompleted(long uid, boolean successful, RTMStruct.RTMAnswer answer, int reloginCount);

    void rtmConnectClose(long uid);

    void kickout();

    void pushVoiceTranslate(String text, String slang, long uid);

    void pushEnterRTCRoom(long roomId, long userId, long time);

    void pushExitRTCRoom(long roomId, long userId, long time);
}
