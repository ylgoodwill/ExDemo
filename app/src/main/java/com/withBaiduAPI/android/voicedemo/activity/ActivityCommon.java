package com.withBaiduAPI.android.voicedemo.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.recognizerdemo.R;
import com.withBaiduAPI.android.voicedemo.recognization.online.InFileStream;
import com.withBaiduAPI.android.voicedemo.util.Logger;

import java.util.ArrayList;


public abstract class ActivityCommon extends AppCompatActivity {
    protected TextView txtLog;
    protected Button btn;
    protected TextView txtResult;

    protected Handler handler;

    protected String DESC_TEXT;

    protected int layout = R.layout.common;
    public boolean flag = true;
    protected boolean running = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStrictMode();
        InFileStream.setContext(this);
        setContentView(layout);
        initView();
        handler = new Handler() {

            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }

        };
        Logger.setHandler(handler);
        initPermission();
        if (flag)
            initRecog();
        else {
            Toast.makeText(ActivityCommon.this, "缺少权限", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }


    protected abstract void initRecog();

    protected void handleMsg(Message msg) {
        if (txtLog != null && msg.obj != null) {
            String[] content = msg.obj.toString().split("原始json");
            if (!content[0].contains("INFO")&& !content[0].contains("ERROR"))
                txtLog.append(content[0] + "\n");
        }
    }

    protected void initView() {
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        btn = (Button) findViewById(R.id.btn);
        txtLog.setText(DESC_TEXT + "\n");

    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm :permissions){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()){
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
            flag = false;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    flag =true;


                } else {
                    Toast.makeText(ActivityCommon.this,"缺少权限！", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
