package com.highras.voiceDemo.model;

/**
 * @author fengzi
 * @date 2022/2/22 16:38
 */
public class Member {
    public long uid;
    public Boolean subscribe = true;

    public Member(long uid, Boolean subscribe) {
        this.uid = uid;
        this.subscribe = subscribe;
    }
}
