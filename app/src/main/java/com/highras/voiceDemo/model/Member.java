package com.highras.voiceDemo.model;

import android.view.Surface;
import android.view.SurfaceView;

/**
 * @author fengzi
 * @date 2022/2/22 16:38
 */
public class Member {
    public long uid;
    public String nickName;

    public Member(long uid, String nickName) {
        this.uid = uid;
        this.nickName = nickName;
    }
}
