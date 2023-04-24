### android-rtm-sdk-RTC 使用文档

- [版本支持](#版本支持)
- [依赖集成](#依赖集成)
- [使用说明](#使用说明)
- [使用示例](#使用示例)
- [接口说明](#接口说明)


### 版本支持
- RTM最低支持Android版本为5.0(api-21) 带有RTC(实时音视频)功能最低支持Android版本为7.0(api-24)

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
    使用RTC:
    dependencies {
        implementation 'com.github.highras:rtc-android:2.7.7'
    }
    使用RTM:
        dependencies {
        implementation 'com.github.highras:rtm-android:2.7.7'
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
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    ~~~
  
- RTM说明:
  - 服务器push消息:请继承RTMPushProcessor类,重写自己需要的push系列函数(所有push函数在子线程执行，如需更新ui请自行切回主线程 RTM的push回调函数和收发线程在一起 如果用户在push的回调函数中有耗时操作 建议请独开启线程处理)
  - RTM的各项服务配置和增值服务可以在后台配置，请登陆管理后台预览详细的配置参数
  - 所有同步和异步接口都会返回 RTMAnswer结构，请优先判断answer中的errorCode 如果为0正常
  - RTM的room和group的区别 group在服务端会持久化 room是非持久化(用户下线或者RTM链接断开会自动离开room)
  - room默认不支持多房间（当用户进入第二个房间会自动退出第一个房间） 用户可以在控制台开启支持多房间配置
  - RTMConfig是RTM的全局配置参数，所有配置均已有默认值，使用者如需要重新设置默认值，请在初始化RTMclient调用带RTMConfig的构造函数。
  
- RTC说明:
  - 开启RTC功能需要先登陆成功
  - 可以进入多个实时语音房间 但必须只有一个当前活跃的房间(必须调用setActivityRoom设置当前活跃房间才能正常接收和发送语音)
  - 视频房间和实时翻译语音房间只能进入一个
  - 需要订阅才能正常接收对方视频流
  - 链接断开，进入的实时音视频房间会自动退出，需要在重连完成后再次进入房间 订阅的视频流需要重新订阅
- 用户可以重写日志类 收集和获取sdk内部的错误信息(强烈建议重载日志类) 例如
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
 
    RTMClient client = RTMCenter.initRTMClient(rtmEndpoint,info.pid, uid, new RTMExampleQuestProcessor(), currentActivity);
    
    rtmclient.setErrorRecoder(new TestErrorRecorder())
    client.login(String token)
    
    login成功后可以正常调用rtm聊天相关接口
    client.sendChat/ client.sendMessage.....

实时音频房间
    client.enterRTCRoom(100);
    client.setActivityRoom(100);
    client.canSpeak(boolean status) //打开或关闭麦克风
实时视频房间
    client.enterRTCRoom(100);
    client.setPreview(previewSurfaceView);//设置预览view
    client.opencamera() //打开摄像头
    client.subscribeVideos(100, new HashMap<Long, SurfaceView>() ) //订阅对方视频流
实时翻译语音房间
    client.createTranslateRTCRoom
发起p2p请求
    client.requestP2PRTC(1, 12345, null, callback)
~~~

##  接口说明
### [RTM接口]
- [服务端push接口](doc-zh/RTMPush.md)
- [发送聊天以及消息类接口](doc-zh/RTMChat.md)
- [房间/群组/好友接口](doc-zh/RTMRelationship.md)
- [用户系统命令接口](doc-zh/RTMUserSystem.md)
- [语音接口](doc-zh/RTMAudio.md)
- [RTM错误码](doc-zh/ErrorCode.md)
### [RTC接口](doc-zh/RTC-zh.md)
