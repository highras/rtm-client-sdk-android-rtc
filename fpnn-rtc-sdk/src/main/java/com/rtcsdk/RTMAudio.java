package com.rtcsdk;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.livedata.audioConvert.AudioConvert;
import com.rtcsdk.RTMStruct.RTMAudioStruct;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

class AmrBroad implements Runnable {
    private Thread mDecodeThread;
    private AudioTrack mAudioTrack;
    private  int playerBufferSize = 0;
    private static final int SAMPLE_RATE = 16000;
    private short[] pcmData;
//    boolean isRunning = false;

    public void start(short[] data) {
        if (data== null)
            return;

        if (mAudioTrack != null && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
            release();

        pcmData = data;

        playerBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                playerBufferSize, AudioTrack.MODE_STREAM);

        mAudioTrack.play();
        mDecodeThread = new Thread(this);
        mDecodeThread.start();
    }

    public void stop() {
        if (mDecodeThread != null)
            mDecodeThread.interrupt();
//        isRunning = false;
    }

    private void release()
    {
        if(mAudioTrack == null)
            return;
        try {
            mAudioTrack.stop();
        }
        catch (IllegalStateException e) {
            Log.e("TAG","stopRecord error " + e.getMessage());
        }
        mAudioTrack.release();
        mAudioTrack = null;
    }

    @Override
    public void run() {
        try {
            int idx = 0;
            while (mAudioTrack != null && !Thread.currentThread().isInterrupted()) {
                short[] tempBuffer = Arrays.copyOfRange(pcmData, idx, idx + playerBufferSize);
                // 播放
                if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
                    return;
                mAudioTrack.write(tempBuffer, 0, playerBufferSize);
                idx += playerBufferSize;
                if (idx >= pcmData.length)
                    break;
            }
            release();
            if (RTMAudio.getInstance().audioAction != null){
                RTMAudio.getInstance().audioAction.broadFinish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class RTMAudio {
    BluetoothUtil bluetoothUtil;
    AudioManager mAudioManager;

    private static RTMAudio instance = null;
    private Handler handler=new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };
    IAudioAction audioAction = null;
    private String lang = TranscribeLang.EN_US.getName();
    private int gatherInterval= 200;
    //    private AudioTrack mPlayer;
    private MediaRecorder mRecorder = null;
    private File recordFile;
    private int minSampleRate = 16000;
    private int maxDurSeconds = 300;
    private int defaultBitRate = 16000;
    private int audioChannel = 1;
    private AmrBroad play = new AmrBroad();
    private Context appcontext;


    class BluetoothUtil {

        private String TAG = "rtcsdk";

        //第一次打开sco没成功的情况，持续连接的次数
        private static final int SCO_CONNECT_TIME = 5;
        private int mConnectIndex = 0;

        public void openSco() {
            if (!mAudioManager.isBluetoothScoAvailableOffCall()) {
                Log.e(TAG, "系统不支持蓝牙录音");
                if (audioAction!= null)
                    audioAction.startRecord(false,"Your device no support bluetooth record!");
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    //蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
                    mAudioManager.stopBluetoothSco();
                    mAudioManager.startBluetoothSco();
                    //蓝牙SCO连接建立需要时间，连接建立后会发出ACTION_SCO_AUDIO_STATE_CHANGED消息，通过接收该消息而进入后续逻辑。
                    //也有可能此时SCO已经建立，则不会收到上述消息，可以startBluetoothSco()前先
                    //stopBluetoothSco()
                    mConnectIndex = 0;
                    appcontext.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                            boolean bluetoothScoOn = mAudioManager.isBluetoothScoOn();
                            Log.i(TAG, "onReceive state=" + state + ",bluetoothScoOn=" + bluetoothScoOn);
                            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) { // 判断值是否是：1
                                Log.e(TAG, "onReceive success!");
                                mAudioManager.setBluetoothScoOn(true);  //打开SCO
                                realStartRecord();
                                appcontext.unregisterReceiver(this);  //取消广播，别遗漏
                            } else {//等待一秒后再尝试启动SCO
                                Log.e(TAG, "onReceive failed index=" + mConnectIndex);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (mConnectIndex < SCO_CONNECT_TIME) {
                                    mAudioManager.startBluetoothSco();//再次尝试连接
                                } else {
                                    if (audioAction != null)
                                        audioAction.startRecord(false,"open sco failed!");
                                    appcontext.unregisterReceiver(this);  //取消广播，别遗漏
                                }
                                mConnectIndex++;
                            }
                        }
                    }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
                }
            }).start();

        }


        public void closeSco() {
            boolean bluetoothScoOn = mAudioManager.isBluetoothScoOn();
            if (bluetoothScoOn) {
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.stopBluetoothSco();
            }
        }

    }

    public static RTMAudio getInstance() {
        if (instance == null) {
            synchronized (RTMAudio.class) {
                if (instance == null) {
                    instance = new RTMAudio();
                }
            }
        }
        return instance;
    }


    private RTMAudio(){

    }
    public void setMaxDurSeconds(int millTime){
        maxDurSeconds = millTime;
    }

    public short[] getRawData(byte[] amrSrc) {
        if (amrSrc == null)
            return null;

        int[] status = new int[1];
        int[] wavsize = new int[1];

        byte[] wavBuffer = AudioConvert.convertAmrwbToWav(amrSrc, status, wavsize);
        if (wavBuffer == null || status[0] != 0)
            return null;

        int channelCount = wavBuffer[22];
        int pos = 12;

        while (!(wavBuffer[pos] == 100 && wavBuffer[pos + 1] == 97 && wavBuffer[pos + 2] == 116 && wavBuffer[pos + 3] == 97)) {
            pos += 4;
            int chunkSize = wavBuffer[pos] + wavBuffer[pos + 1] * 256 + wavBuffer[pos + 2] * 65536 + wavBuffer[pos + 3] * 16777216;
            pos += 4 + chunkSize;
        }
        pos += 8;

        int samples = (wavBuffer.length - pos) / 2;
        short[] pcmData = new short[samples];

        int idx = 0;
        while (pos < wavBuffer.length) {
            pcmData[idx] = getShort(wavBuffer[pos], wavBuffer[pos + 1]);
            pos += 2;
            idx++;
        }
        return pcmData;
    }

    private  short getShort(byte firstByte, byte secondByte) {
        return (short) ((0xff & firstByte) | (0xff00 & (secondByte << 8)));
    }

    public File getRecordFile(){
        return recordFile;
    }

    public interface IAudioAction {
        void startRecord(boolean success, String errMsg);

        void stopRecord();

        void startBroad(boolean success);

        void broadFinish();

        void listenVolume(double db);//录音分贝的回调
    }


    /**
     *
     * @param file 录音文件默认存储的地址
     * @param lang 语言(详见TranslateLang列表)
     * @param audioAction 用户自定义回调
     */
    public void init(Context appcontext, File file, String lang, IAudioAction audioAction) {
        this.audioAction = audioAction;
        if (!lang.isEmpty())
            this.lang = lang;
        this.appcontext = appcontext;
        recordFile = file;
        bluetoothUtil = new BluetoothUtil();
        mAudioManager = (AudioManager) appcontext.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * @param lang 语言(详见TranslateLang列表)
     */
    public void setLang(String lang){
        this.lang = lang;
    }

    public void setGatherInterva(int time){
        gatherInterval = time;
    }

    private int getAudioTime(File file) {
        int length;
        MediaPlayer tmp = new MediaPlayer();
        try {
            tmp.setDataSource(file.getPath());
            tmp.prepare();
            length = tmp.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
            tmp.release();
            return 0;
        }
        tmp.release();
        return length;
    }


    public RTMAudioStruct getAudioInfo() {
        return getAudioInfo(recordFile);
    }

    public boolean checkAudio(byte[] data)
    {
        if (data ==null)
            return false;
        byte[] amrHeader = "#!AMR-WB\n".getBytes();
        byte[] result = new byte[amrHeader.length];
        System.arraycopy(data ,0,result,0,amrHeader.length);
        if(!Arrays.equals(result,amrHeader))
            return false;
        return  true;
    }

    public RTMAudioStruct getAudioInfo(File file) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        RTMAudioStruct tt = new RTMAudioStruct();
        byte[] audio = fileToByteArray(file);
        if (audio == null)
            return null;
        byte[] amrHeader = "#!AMR-WB\n".getBytes();
        byte[] result = new byte[amrHeader.length];
        System.arraycopy(audio ,0,result,0,amrHeader.length);
        if(!Arrays.equals(result,amrHeader))
            return null;

        int audioDur = getAudioTime(file);
        if (audioDur == 0)
            return null;
        tt.file = file;
        tt.audioData = audio;
        tt.duration = audioDur;
        tt.lang = lang;
        return tt;
    }

    private byte[] fileToByteArray(File file) {
        byte[] data;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            data = baos.toByteArray();
            fis.close();
            baos.close();
        } catch (Exception e) {
            Log.e("TAG","fileToByteArray error " + e.getMessage());
            return null;
        }
        return data;
    }

    private void updateMicStatus() {
        if (mRecorder != null) {
            try {
                double ratio = (double) mRecorder.getMaxAmplitude() / 1;
                double db = 0;// 分贝
                if (ratio > 1)
                    db = 20 * Math.log10(ratio);
                if (audioAction != null) {
                    audioAction.listenVolume(db);
                }
                handler.postDelayed(mUpdateMicStatusTimer, gatherInterval);
            }
            catch (Exception e){
                return;
            }
        }
    }

    private boolean isBlutoothOn(){
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int state = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        if (state != BluetoothAdapter.STATE_CONNECTED){
            return false;
        }
        return true;
    }

    void listenVolum(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRecorder != null) {
                    try {
                        double ratio = (double) mRecorder.getMaxAmplitude() / 32767;
                        double db = 20 * Math.log10(ratio);
                        if (audioAction != null) {
                            audioAction.listenVolume(db);
                        }
                        Thread.sleep(gatherInterval);
                    }
                    catch (Exception e){
                        return;
                    }
                }
            }
        }).start();
    }

    public void startRecord() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (appcontext.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                if (audioAction != null)
                    audioAction.startRecord(false, "not Record Permission");
                return;
            }
        }
        if (recordFile.exists())
            recordFile.delete();
        try {
            recordFile.createNewFile();
        } catch (IOException e) {
            if (audioAction != null)
                audioAction.startRecord(true, e.getMessage());
            return;
        }
        if (mRecorder != null){
            try {
                mRecorder.stop();
                mRecorder.setOnErrorListener(null);
                mRecorder.setOnInfoListener(null);
                mRecorder.setPreviewDisplay(null);
            } catch (IllegalStateException e) {
                Log.e("TAG","stopRecord error " + e.getMessage());
            }
            catch (RuntimeException e) {
                Log.e("TAG","stopRecord error " + e.getMessage());
            } catch (Exception e) {
                Log.e("TAG","stopRecord error " + e.getMessage());
            }
            mRecorder.release();
            mRecorder = null;
        }

        if (isBlutoothOn()){
            bluetoothUtil.openSco();
        }
        else{
            realStartRecord();
        }
    }


    private void realStartRecord(){
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
            mRecorder.setMaxDuration(maxDurSeconds * 1000);//设置录音最大时长
            mRecorder.setAudioSamplingRate(minSampleRate);  //采样率16K
            mRecorder.setAudioChannels(audioChannel);//单声道
            mRecorder.setAudioEncodingBitRate(defaultBitRate);//比特率16bit
            mRecorder.setOutputFile(recordFile.getPath());

            mRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
                        mr.stop();
                }
            });
            mRecorder.prepare();
            mRecorder.start();
            if (audioAction != null)
                audioAction.startRecord(true, "");
            //            updateMicStatus();
            listenVolum();
        }
        catch (IllegalStateException ex){
            if (audioAction != null)
                audioAction.startRecord(false,"mediarecord start error + " +ex.getMessage());
            Log.e("TAG","startRecord error " + ex);
        }
        catch (Exception e) {
            if (audioAction != null)
                audioAction.startRecord(false,"mediarecord start error + " +e.getMessage());
            Log.e("TAG","startRecord error " + e);
        }
    }
    public RTMAudioStruct stopRecord() {
        if (mRecorder == null)
            return null;
        try {
            mRecorder.setOnErrorListener(null);
            mRecorder.setOnInfoListener(null);
            mRecorder.setPreviewDisplay(null);
            mRecorder.stop();
        } catch (IllegalStateException e) {
            Log.e("TAG","stopRecord error " + e.getMessage());
        }
        catch (RuntimeException e) {
            Log.e("TAG","stopRecord error " + e.getMessage());
        } catch (Exception e) {
            Log.e("TAG","stopRecord error " + e.getMessage());
        }

        mRecorder.release();
        mRecorder = null;
        if (audioAction != null)
            audioAction.stopRecord();

        return getAudioInfo();
//        audioDur = System.currentTimeMillis() - audioDur;
    }

    public void writeWavFile(byte[] amrData, File file)
    {
        int[] status = new int[1];
        int[] wavsize = new int[1];
        byte[] wavBuffer = AudioConvert.convertAmrwbToWav(amrData, status, wavsize);
        if (wavBuffer == null)
            return;
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(wavBuffer);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void broadAudio(byte[] amrData) {
        short[] data = getRawData(amrData);
        play.start(data);
    }

    public void broadAudio() {
        broadAudio(recordFile);
    }

    public void broadAudio(File file) {
        broadAudio(fileToByteArray(file));
    }

    public void stopAudio() {
        play.stop();
    }
}
