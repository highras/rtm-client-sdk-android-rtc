#### RTM语音
用户可以继承IAudioAction接口 自定义开始录音,结束录音,开始播放,结束播放,显示录音的音量等操作
- 接口
~~~c++
       void startRecord();
        void stopRecord();
        void broadAudio();
        void broadFinish();
        void listenVolume();
~~~

#### API
public void broadAudio(byte[] amrData) {
public void startRecord();
public RTMAudioStruct stopRecord()


#### 使用
    RTMAudio audioManage = RTMAudio.getinstance();
    public void init(File file, String lang, IAudioAction audioAction) { //lang, action可空
    audioManage.startRecord(); //开始录音
    audioManage.stopRecord();  //结束录音
    rtmclient.sendfile/sendgroufile/sendroomfile
    
    audioManage.broadAudio(data)


