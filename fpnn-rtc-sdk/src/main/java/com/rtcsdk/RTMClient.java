package com.rtcsdk;

import android.app.Activity;
import android.view.SurfaceView;

import java.util.Map;

import com.rtcsdk.UserInterface.IRTMEmptyCallback;

public class RTMClient extends RTMRTC {
    /**
     *
     * @param rtmEndpoint RTM网关地址
     * @param rtmEndpoint RTC实时视音频网关地址
     * @param pid      项目id
     * @param uid       用户id
     * @param serverPushProcessor serverpush类
     */
    protected RTMClient(String rtmEndpoint, long pid, long uid, RTMPushProcessor serverPushProcessor, Activity currentActivity) {
        RTMInit(rtmEndpoint,pid, uid, serverPushProcessor,currentActivity,null);
    }

    /**
     *
     * @param rtmEndpoint RTM网关地址
     * @param rtmEndpoint RTC实时视音频网关地址
     * @param pid      项目id
     * @param uid       用户id
     * @param serverPushProcessor serverpush类
     * @param config 自定义配置项
     */
    protected RTMClient(String rtmEndpoint,long pid, long uid, RTMPushProcessor serverPushProcessor,Activity currentActivity,RTMConfig config){
        RTMInit(rtmEndpoint,pid, uid, serverPushProcessor,currentActivity,config);
    }

    /** 用户下线(单纯的用户下线)
     */
    public void bye() {
        bye(true);
    }

    /** 切换账号或者完全退出时候调用(释放资源,网络广播监听会持有RTMClient对象 如果不调用RTMClient对象会一直持有不释放)
     */
    public void closeRTM(){
        realClose();
        RTMCenter.closeRTM(getPid(), getUid());
    }


    public long getPid() {
        return super.getPid();
    }

    public long getUid() {
        return super.getUid();
    }

    /**获取用户登录状态
     */
    public boolean isOnline() {
        return getClientStatus() == ClientStatus.Connected?true:false;
    }

    /**
     *rtm登陆  sync
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public RTMStruct.RTMAnswer login(String token, String lang, Map<String, String> attr) {
        return super.login(token, lang, attr, 0);
    }

    /**
     *rtm登陆(新的验签方式)  sync
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public RTMStruct.RTMAnswer login(String token, String lang, Map<String, String> attr, long ts) {
        return super.login(token, lang, attr, ts);
    }



    /**
     *rtm登陆  async
     * @param callback  登陆结果回调
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public void login(String token, String lang, Map<String, String> attr, IRTMEmptyCallback callback) {
        super.login(callback, token, lang, attr, 0);
    }


    /**
     *rtm登陆(新的验签方式)  async
     * @param callback  登陆结果回调
     * @param token     用户token
     * @param lang      用户语言(详见TranslateLang.java枚举列表)(当项目启用了自动翻译  如果客户端设置了语言会收到翻译后的结果 不设置语言或者为空则不会自动翻译,后续可以通过setTranslatedLanguage设置)
     * @param attr      登陆附加信息
     */
    public void login(String token, String lang, Map<String, String> attr, long ts, IRTMEmptyCallback callback) {
        super.login(callback, token, lang, attr, ts);
    }
}