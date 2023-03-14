package com.highras.videoudp;

/**
 * @author fengzi
 * @date 2022/2/18 10:25
 */
public class ProjectInfo {
    long pid;
    String host;
    String key;

    public ProjectInfo(long pid, String host, String key) {
        this.pid = pid;
        this.host = host;
        this.key = key;
    }
}
