#### RTM语音
用户可以继承IAudioAction接口 自定义开始录音,结束录音,开始播放,结束播放,显示录音的音量等操作
- 接口
~~~c++
        void startRecord(boolean success, String errMsg);
        void stopRecord();
        void startBroad(boolean success);
        void broadFinish();
        void listenVolume(double db);//录音分贝的回调
~~~

#### API
~~~
    /**
     *
     * @param file 录音文件默认存储的地址
     * @param lang 语言(详见TranslateLang列表)
     * @param audioAction 用户自定义回调
     */
    public void init(Context appcontext, File file, String lang, IAudioAction audioAction)

    //播放录音
    public void broadAudio(byte[] amrData)

    //开始录音
    public void startRecord();

    //停止录音
    public RTMAudioStruct stopRecord()

    //播放录音
    public void broadAudio(byte[] amrData) 

    public void broadAudio(File file) 

    //停止播放
    public void stopAudio()
~~~


#### 使用
        RTMAudio.getInstance().init(this.getApplicationContext(), rtmaudiocache, TranscribeLang.EN_US.getName(),new IAudioAction());


    RTMAudio audioManage = RTMAudio.getinstance();
    .init(this.getApplicationContext(), rtmaudiocache, TranscribeLang.EN_US.getName(),new IAudioAction());

    audioManage.startRecord(); //开始录音
    audioManage.stopRecord();  //结束录音
    rtmclient.sendfile/sendgroufile/sendroomfile
    
    audioManage.broadAudio(data)

