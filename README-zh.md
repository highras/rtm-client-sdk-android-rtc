### android-rtm-sdk-RTC 使用文档
## <font color="#660000">备注</font>:rtm-rtc基于RTM，RTM的使用文档详见[https://github.com/highras/rtm-client-sdk-android/blob/master/README-zh.md]

- [版本支持](#版本支持)
- [依赖集成](#依赖集成)
- [使用说明](#使用说明)
- [使用示例](#使用示例)
- [接口说明](#接口说明)

### 版本支持
- 最低支持Android版本为7.0

### 依赖集成
 dependency in Gradle
    allprojects {
            repositories {
                maventral()
            }
        }
    dependencies {
        implementation 'com.github.highras:rtc-android:2.7.0'
    }
### 使用说明
- RTC需要的权限
  ~~~
     <uses-permission android:name="android.permission.BLUETOOTH" />
      <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
      <uses-permission android:name="android.permission.RECORD_AUDIO"/>
      <uses-permission android:name="android.permission.CAMERA" />
      <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
      <uses-permission android:name="android.permission.INTERNET"/>
      <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    ~~~
  
- RTC说明:
  - 如果使用实时视频功能请在任意activity的oncreate主线程中 初始化RTMClient对象
  - 开启RTC功能需要RTM先登陆成功，然后调用initRTMVideo初始化
  - RTM链接断开，进入的实时视频房间会自动退出，需要在重连完成后再次进入房间并订阅视频流
  
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

  实时视频
    client.initRTMVideo();
    client.openCamera() //打开摄像头
~~~

##  接口说明
- [用户回调接口和数据结构](doc-zh/RTMUserInterface.md)
- [服务端push接口](doc-zh/RTMPush.md)
- [聊天接口](doc-zh/RTMChat.md)
- [消息接口](doc-zh/RTMessage.md)
- [文件接口](doc-zh/RTMFile.md)
- [房间/群组/好友接口](doc-zh/RTMRelationship.md)
- [用户系统命令接口](doc-zh/RTMUserSystem.md)
- [语音接口](doc-zh/RTMAudio.md)
- [RTC](doc-zh/RTC.md)
- [RTM错误码](doc-zh/ErrorCode.md)
