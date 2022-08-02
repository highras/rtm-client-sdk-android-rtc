package com.rtcsdk;

import com.fpnn.sdk.FunctionalAnswerCallback;
import com.fpnn.sdk.TCPClient;
import com.fpnn.sdk.proto.Answer;
import com.fpnn.sdk.proto.MessagePayloadUnpacker;
import com.fpnn.sdk.proto.Quest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RTMDATALink {

    long pid;
    long uid;

    public RTMDATALink(long pid, long uid) {
        this.pid = pid;
        this.uid = uid;
    }

    protected boolean isDatalinkTest = false;
    boolean datalinkTest = false;
    boolean datalinkTestStop = false;

    public static String getIP(long ipaddr) {
        long y = ipaddr % 256;
        long m = (ipaddr - y) / (256 * 256 * 256);
        long n = (ipaddr - 256 * 256 *256 * m - y) / (256 * 256);
        long x = (ipaddr - 256 * 256 *256 * m - 256 * 256 *n - y) / 256;
        return m + "." + n + "." + x + "." + y;
    }

    public static final byte[] hex2byte(String hex)
            throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }
    void startDataLinkTest(final String token){
        if (token.isEmpty())
            return;
        if (!isDatalinkTest)
            return;
        if (!datalinkTest){
            datalinkTestStop = false;
            datalinkTest = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final HashMap<String, TCPClient> testClient = new HashMap<>();
                    try{
                        ArrayList<String> ips = new ArrayList<>();
                        byte[] realToken = token.substring(0,33).getBytes();
                        int version = Integer.parseInt(token.substring(32,33));
                        String ipstring = token.substring(33);
                        byte[] backtoken = hex2byte(ipstring);
                        //            String lala1 = hexStringToString(ipstring);
                        byte[] ret = new byte[backtoken.length];
                        int count = 0;
                        for (int i =0; i < backtoken.length; i++) {
                            if(count >= 33)
                            {
                                count = 0;
                            }
                            ret[i] = (byte) (backtoken[i] ^ realToken[count]);
                            count++;
                        }
                        MessagePayloadUnpacker unpacker = new MessagePayloadUnpacker(ret, 0, ret.length);
                        Map payload = unpacker.unpack();
                        HashMap<String, Object> retMap = new HashMap<>(payload);
                        String sendC = "";
                        for (String region: retMap.keySet()) {
                            ArrayList<Long> kk = new ArrayList<>();
                            List<Object> attrsList = (List<Object>) retMap.get(region);
                            for (Object value : attrsList) {
                                if (value instanceof Integer)
                                    kk.add((((Integer) value).longValue()));
                                else if (value instanceof Long)
                                    kk.add(((Long) value).longValue());
                                else if (value instanceof BigInteger)
                                    kk.add(((BigInteger) value).longValue());
                                else
                                    kk.add(Long.valueOf(String.valueOf(value)));
                            }
                            for (Long ipint: kk){
                                String ip = getIP(ipint);
                                sendC += ":" + region + "-" + ip;
                                testClient.put(region, new TCPClient(ip ,13702));
                            }

//                    rtmUtils.wantLongList();
//                    testClient.put(region, )
                        }
                        final String stringparm = sendC.substring(1);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                RTCEngine.startUDPTest(stringparm,pid,uid);
                            }
                        }).start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (!datalinkTestStop){
                        Quest quest = new Quest("adjustTime");
                        for (final String region: testClient.keySet()){
                            final TCPClient jjk = testClient.get(region);
                            final long sendTime = System.currentTimeMillis();
                            jjk.sendQuest(quest, new FunctionalAnswerCallback() {
                                @Override
                                public void onAnswer(Answer answer, int errorCode) {
                                    if (errorCode == 0) {
                                        long rttTime = System.currentTimeMillis() - sendTime;
                                        Quest addLog = new Quest("adddebuglog");
                                        JSONObject sendmsg = new JSONObject();
                                        try {
                                            sendmsg.put("pid",pid);
                                            sendmsg.put("uid",uid);
                                            sendmsg.put("region",region);
                                            sendmsg.put("ip",jjk.endpoint());
                                            sendmsg.put("model","tcp");
                                            sendmsg.put("RTT",rttTime);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
//                                        mylog.log("1111 " + sendmsg.toString());
                                        addLog.param("msg", sendmsg.toString());
                                        addLog.param("attrs","");
                                        jjk.sendQuest(addLog, new FunctionalAnswerCallback() {
                                            @Override
                                            public void onAnswer(Answer answer, int errorCode) {
//                                                Log.i("sdktest", " code " + errorCode);
                                            }
                                        });
                                    }
                                }
                            }, 10);
                        }
                        try {
                            int cycle = 100;
                            while (!datalinkTestStop && cycle-- >0) {
                                Thread.sleep(100);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    datalinkTest =false;
                }
            }).start();
        }
    }
}
