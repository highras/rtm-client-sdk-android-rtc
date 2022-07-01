package com.livedata.rtc;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceView;

public enum RTCEngine {
    INSTANCE;

    // Load native library
    static {
        System.loadLibrary("rtcEngineNative");
    }
    static int sessionID = -1;

    // Native methods
//    public static native String create(Object object, int osversion,boolean stereo, Object view);
    public static native String create(Object object, String rtcEndpoint, int videoLevel, long pid, long uid, Context mcontext, Object audiofocusobject);
    public static native void switchCamera(boolean front);
    public static native String switchVideoCapture(int level);
    public static native String setCameraFlag(boolean flag);
    public static native void canSpeak(boolean flag);
    public static native void userLeave(long uid);
    public static native void setMicphoneGain(int level);
    public static native void delete();
    public static native void unsubscribeUser(long uid);
    public static native void switchVoiceOutput(boolean useSpeaker);
    public static native byte[] enterRTCRoom(String token, long rid, int type, String nickName, int scene);
    public static native void leaveRTCRoom(long rid);
    public static native void setBackground(boolean flag);
    public static native void setRotation(int rotation);
    public static native void RTCClear();
    public static native void headsetStat(int flag);
    public static native String setActivityRoom(long rid);
    public static native String setVoiceStat(boolean flag);
    public static native String resumeAudioFocus();
    public static native String lossAudioFocus();
    public static native void setdiscardable(boolean flag);
    public static native void setpreview(Surface view);
    public static native int isInRTCRoom();
    public static native void audioOutputFlag (boolean flag);//开启或者关闭音频输出
    public static native void bindDecodeSurface(long uid, Surface surface);
    public static native String startP2P(int type, long toUid, long callid);
    public static native String requestP2PVideo(Surface view);
    public static native void closeP2P();
    public static native long getRTTTime();
}
