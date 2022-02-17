### android-rtm-sdk-RTC-audio 使用文档

## <font color="#660000">备注</font>:rtm-rtc-voice基于RTM，RTM的使用文档详见[https://github.com/highras/rtm-client-sdk-android/blob/master/README-zh.md]

- [版本支持](#版本支持)
- [依赖集成](#依赖集成)
- [使用说明](#使用说明)
- [使用示例](#使用示例)
- [接口说明](#接口说明)


### 版本支持
- 最低支持Android版本为4.4

### 依赖集成
- Add maventral as your repository in project's build.gradle:
    ~~~
    allprojects {
            repositories {
                maventral()
            }
        }
    ~~~
- Add dependency in your module's build.gradle:
    ~~~
    dependencies {
        implementation 'com.github.highras:rtc-android-audio:2.7.3'
    }
    ~~~

### 使用说明
- 需要的权限
  ~~~
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    ~~~
- RTC-audio说明:
  - 如果使用实时语音功能请在任意activity的oncreate主线程中 初始化RTMClient对象
  - 开启RTC功能需要RTM先登陆成功，然后调用initRTMVoice初始化实时音频
  - 可以进入多个实时语音房间 但必须只有一个当前活跃的房间(必须调用setActivityRoom设置当前活跃房间才能正常接收和发送语音)
  - RTM链接断开，进入的实时语音房间会自动退出，需要在重连完成后再次进入房间
- 用户可以重写RTM的日志类 收集和获取sdk内部的错误信息(强烈建议重载日志类) 例如
    ~~~
     public class TestErrorRecorder extends ErrorRecorder {
        public TestErrorRecorder(){
            super.setErrorRecorder(this);
        }
    
        public void recordError(Exception e) {
            Log.i("log","Exception:" + e);
        }
    
        public void recordError(String message) {
            Log.i("log","Error:" + message);
        }
    
        public void recordError(String message, Exception e) {
            Log.i("log",String.format("Error: %s, exception: %s", message, e));
        }
    }
    ~~~

### 使用示例
 ~~~
    public class RTMExampleQuestProcessor extends RTMPushProcessor {
        ....//重写自己需要处理的业务接口
    }
        @Override
    protected void onCreate(Bundle savedInstanceState) {
    RTMClient rtmclient  = new RTMClient(rtmEndpoint,rtcEndpoint, pid, uid, new RTMExampleQuestProcessor(), currentActivity);
    
    rtmclient.setErrorRecoder(new TestErrorRecorder())
    //-- sync
    client.login(String token)
    //-- Async
    client.login(loginCallback callback, String token)
    
    login成功后可以正常调用rtm聊天相关接口
    client.sendChat/ client.sendMessage.....

    实时语音
    client.initRTMVoice();
    client.canSpeak(boolean status) //打开或关闭麦克风
~~~

##  接口说明
- [RTC接口](doc-zh/RTC.md)
