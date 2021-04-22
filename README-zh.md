### android-rtm-sdk 使用文档
- [版本支持](#版本支持)
- [依赖集成](#依赖集成)
- [接口说明](#使用说明)
- [使用示例](#使用示例)
- [接口说明](#接口说明)
- [测试案例](#测试案例)

### 版本支持
- 最低支持Android版本为4.1(api16)
- 支持fpnn ecc加密(secp192r1,secp224r1,secp256r1,secp256r1)

### 依赖集成
 dependency in Gradle
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
        implementation 'com.github.highras:rtm-android:2.0.0'
    }
### 使用说明
- RTM需要的权限
  ~~~
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET"/>
    ~~~
- RTM默认支持自动重连(请继承RTMPushProcessor类的reloginWillStart和reloginCompleted方法) 
- 服务器push消息:请继承RTMPushProcessor类,重写自己需要的push系列函数(RTM的push回调函数和收发线程在一起 如果用户在push的回调函数中有耗时操作 建议请独开启线程处理)
- RTM的各项服务配置和增值服务可以在后台配置，请登陆管理后台预览详细的配置参数
- 所有同步和异步接口都会返回 RTMAnswer结构，请优先判断answer中的errorCode 如果为0正常
- RTM的room和group的区别 group在服务端会持久化 room是非持久化(用户下线或者RTM链接断开会自动离开room)
  - room默认不支持多房间（当用户进入第二个房间会自动退出第一个房间） 用户可以在控制台开启支持多房间配置
- RTMConfig是RTM的全局配置参数，所有配置均已有默认值，使用者如需要重新设置默认值，请在初始化RTMclient调用带RTMConfig的构造函数。
- RTM实时语音说明:
  - 如果使用实时语音功能请在任意activity的oncreate主线程中 初始化RTMClient对象
  - 开启实时语音功能需要RTM先登陆成功，然后调用initRTMVoice初始化实时语音
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
    client.initRTMVoice()/initRTMVoiceWithStereo();
    client.canSpeak(boolean status) //打开或关闭麦克风
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
- [实时语音](doc-zh/RTC.md)
- [RTM错误码](doc-zh/ErrorCode.md)


#### 测试案例
- [详见测试案例](app/src/main/java/com/rtm)
