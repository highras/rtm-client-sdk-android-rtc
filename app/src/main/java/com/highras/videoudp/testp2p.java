package com.highras.videoudp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rtcsdk.RTMClient;
import com.rtcsdk.RTMPushProcessor;
import com.rtcsdk.RTMStruct;
import com.rtcsdk.UserInterface;

public class testp2p extends AppCompatActivity implements View.OnClickListener {

    RTMClient client;
    Utils utils;

    class p2pquestprocessor extends RTMPushProcessor{
        public void pushRequestP2PRTC(long uid, int type){
            testp2p.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(testp2p.this);
                    builder.setTitle("请求p2p rtc");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            client.acceptP2PRTC(null, null, new UserInterface.IRTMEmptyCallback() {
                                @Override
                                public void onResult(RTMStruct.RTMAnswer answer) {

                                }
                            });
                        }
                    });
                    builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            client.refuseP2PRTC(new UserInterface.IRTMEmptyCallback() {
                                @Override
                                public void onResult(RTMStruct.RTMAnswer answer) {

                                }
                            });
                        }
                    });
                    builder.show();
                }
            });

        }

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testp2p);
        findViewById(R.id.requestp2p).setOnClickListener(this);
        findViewById(R.id.cancelp2p).setOnClickListener(this);
        findViewById(R.id.closep2p).setOnClickListener(this);

        utils = Utils.INSTANCE;
        client = utils.client;
        client.setServerPush(new p2pquestprocessor());

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.requestp2p){
            client.requestP2PRTC(1, 666, null, new UserInterface.IRTMEmptyCallback() {
                @Override
                public void onResult(RTMStruct.RTMAnswer answer) {
                    mylog.log("requestP2PRTC ret " + answer.getErrInfo());
                }
            });
        }
        else if (v.getId() == R.id.cancelp2p){
            client.cancelP2PRTC(new UserInterface.IRTMEmptyCallback() {
                @Override
                public void onResult(RTMStruct.RTMAnswer answer) {
                    mylog.log("cancelP2PRTC ret " + answer.getErrInfo());

                }
            });
        }
        else if (v.getId() == R.id.closep2p){
            client.closeP2PRTC(new UserInterface.IRTMEmptyCallback() {
                @Override
                public void onResult(RTMStruct.RTMAnswer answer) {
                    mylog.log("closeP2PRTC ret " + answer.getErrInfo());

                }
            });
        }
    }
}