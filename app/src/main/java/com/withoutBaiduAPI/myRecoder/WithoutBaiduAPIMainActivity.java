package com.withoutBaiduAPI.myRecoder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.recognizerdemo.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WithoutBaiduAPIMainActivity extends AppCompatActivity implements View.OnTouchListener, AudioUtil.OnAudioStatusUpdateListener {
    private AudioRecoderDialog recoderDialog;
    private AudioUtil audioUtil;
    private TextView button;
    private long downT;
    private TextView textView;
    static final String SPEECH_ASR_URL = "http://vop.baidu.com/server_api";
    public RequestBody body;
    public OkHttpClient okHttpClient;
    public String base64Content = "";
    public byte[] imgData;
    public JSONObject jsonqq;
    public String path = "";
    public long startTime = 0;
    public long endTime = 0;
    public boolean flag = true;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private String accessToken = "";

    public String result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.withoutbaiduapiactivity_main);
        flag = true;
        initPermission();
//        //判断SDK版本是否大于等于19，大于就让他显示，小于就要隐藏，不然低版本会多出来一个
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setTranslucentStatus(true);
//            //还有设置View的高度，因为每个型号的手机状态栏高度都不相同
//        }

        button = (TextView) findViewById(android.R.id.button1);
        button.setOnTouchListener(this);
        textView = (TextView) findViewById(R.id.txtLog);
        recoderDialog = new AudioRecoderDialog(this);
        recoderDialog.setShowAlpha(0.98f);
        //recoderUtils = new AudioRecoderUtils(new File(Environment.getExternalStorageDirectory() + "/recoder.amr"));
        audioUtil = AudioUtil.getInstance();
        audioUtil.setOnAudioStatusUpdateListener(this);
        //recoderUtils.setOnAudioStatusUpdateListener(this);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                textView.setText("语音内容为:\n");
                //recoderUtils.startRecord();
                if (flag){
                    audioUtil.startRecord();
                    downT = System.currentTimeMillis();
                    recoderDialog.showAtLocation(view, Gravity.CENTER, 0, 0);
                    button.setBackgroundResource(R.drawable.shape_recoder_btn_recoding);
                }
                else {
                    Toast.makeText(WithoutBaiduAPIMainActivity.this, "缺少权限", Toast.LENGTH_LONG).show();
                    this.finish();
                }
                return true;
            case MotionEvent.ACTION_UP:
                //recoderUtils.stopRecord();
                if (flag) {
                    audioUtil.stopRecord();
                    recoderDialog.dismiss();
                    button.setBackgroundResource(R.drawable.shape_recoder_btn_normal);
                    Toast.makeText(WithoutBaiduAPIMainActivity.this, "录制完成", Toast.LENGTH_LONG).show();
                    test();
                }
                return true;
        }
        return false;
    }

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
                    Toast.makeText(WithoutBaiduAPIMainActivity.this,"缺少权限！", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void test() {
        try {
            //1.读取音频文件,验证id等
            //path = Environment.getExternalStorageDirectory() + "/recoder.amr";
            path = Environment.getExternalStorageDirectory() + "/test111.pcm";
            imgData = com.withoutBaiduAPI.util.Util.readFileByBytes(path);
            base64Content = com.withoutBaiduAPI.util.Util.encode(imgData);
            okHttpClient = new OkHttpClient();
            OkHttpClient okHttpClientYanZheng = new OkHttpClient();
            RequestBody body1 = new FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .add("client_id", "GXxrqtLF6QEbPXaSY08ClGd5")
                    .add("client_secret", "cKG4MpT07KHWWUQCQvV5lSAVS9xQGcT6")
                    .build();
            Request request1 = new Request.Builder()
                    .url("https://aip.baidubce.com/oauth/2.0/token")
                    .post(body1)
                    .build();
            Call call1 = okHttpClientYanZheng.newCall(request1);
            call1.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           Toast.makeText(WithoutBaiduAPIMainActivity.this,"网络连接异常",Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = new String(response.body().bytes(), "UTF-8");
                    try {
                        JSONObject json = new JSONObject(res);
                        Log.e("111", json.toString(2));
                        Log.e("111", "验证完毕");
                        accessToken = json.getString("access_token");
                        //2.将音频文件传输到服务器
                        startTime = System.currentTimeMillis();
                        JSONObject jobject = new JSONObject();
                        try {
                            jobject.put("speech", base64Content);
                            jobject.put("format", "pcm");
                            jobject.put("rate", 16000);
                            jobject.put("channel", 1);
                            jobject.put("token", accessToken);
                            jobject.put("cuid", com.withoutBaiduAPI.util.Util.md5(accessToken, "UTF-8"));
                            jobject.put("len", imgData.length);
                            MediaType mediaType = MediaType.parse("application/json");
                            RequestBody requestBody = RequestBody.create(mediaType, jobject.toString());
                            //创建一个请求对象
                            Request request = new Request.Builder()
                                    .url(SPEECH_ASR_URL)
                                    .post(requestBody)
                                    .build();
                            Call callxx = okHttpClient.newCall(request);
                            callxx.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String xx = textView.getText().toString();
                                            xx += "网络请求错误，请检查网络";
                                            textView.setText(xx);
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    String res = new String(response.body().bytes(), "UTF-8");
                                    try {
                                        jsonqq = new JSONObject(res);
                                        Log.e("111", jsonqq.toString(2));
                                        result = jsonqq.getString("result");
                                        if (jsonqq.getString("err_no").equals("0")) {
                                            Log.e("111", "识别成功");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String xx = textView.getText().toString();
                                                    xx+="\"";
                                                    String y = result.replaceAll("\"", "")
                                                            .replaceAll("\\[", "")
                                                            .replaceAll("\\]", "");          //去掉【】“”符号
                                                    xx += y;
                                                    xx = xx.substring(0,xx.length()-1);    //去掉末尾逗号
                                                    endTime = System.currentTimeMillis();
                                                    xx += "\"；说话结束到识别结束耗时【" + (endTime-startTime) + "ms】";
                                                    textView.setText(xx);
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    String xx = textView.getText().toString();
                                                    xx += "无法识别，请重新录制";
                                                    textView.setText(xx);
                                                }
                                            });
                                        }

                                    } catch (JSONException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                String xx = textView.getText().toString();
                                                xx += "无法识别，请重新录制";
                                                textView.setText(xx);
                                            }
                                        });
                                    }

                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(double db) {
        if (null != recoderDialog) {
            int level = (int) db;
            recoderDialog.setLevel((int) db);
            recoderDialog.setTime(System.currentTimeMillis() - downT);
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
