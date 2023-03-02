package com.rtcsdk;

import android.app.Activity;
import java.util.HashMap;

public class RTMCenter {
    static HashMap<String, RTMClient> clients = new HashMap<>();
    public  static RTMClient initRTMClient(String rtmEndpoint, String rtcEndpoint, long pid, long uid, RTMPushProcessor serverPushProcessor, Activity currentActivity){
        synchronized (clients){
            String findkey = pid + ":" + uid;
            if (clients.containsKey(findkey)){
                return clients.get(findkey);
            }
        }
        RTMClient client = new RTMClient( rtmEndpoint, rtcEndpoint,pid, uid, serverPushProcessor,currentActivity,null);
        return client;
    }


    public  static RTMClient initRTMClient(String rtmEndpoint, String rtcEndpoint, long pid, long uid, RTMPushProcessor serverPushProcessor, Activity currentActivity, RTMConfig config){
        synchronized (clients){
            String findkey = pid + ":" + uid;
            if (clients.containsKey(findkey)){
                return clients.get(findkey);
            }
        }
        RTMClient client = new RTMClient( rtmEndpoint, rtcEndpoint,pid, uid, serverPushProcessor,currentActivity,config);
        return client;
    }

    static void closeRTM(long pid, long uid){
        synchronized (clients){
            String findkey = pid + ":" + uid;
            clients.remove(findkey);
        }
    }
}
