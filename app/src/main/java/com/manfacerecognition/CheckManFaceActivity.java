package com.manfacerecognition;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.speech.recognizerdemo.R;
import com.wordrecognition.FileUtil;
import com.wordrecognition.ui.camera.CameraActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckManFaceActivity extends AppCompatActivity {
    String pic_Path = Environment.getExternalStorageDirectory() + "/test.jpg";
    Button face_check;
    String token = "";
    TextView face_check_result;
    String age="";
    String isMan="";
    String xxxx="";

    public void test() {
        byte[] imgData;
        try {
            imgData = com.withoutBaiduAPI.util.Util.readFileByBytes(pic_Path);
            String base64Content = com.withoutBaiduAPI.util.Util.encode(imgData);
//            HashMap<String, String> options = new HashMap<String, String>();
//            options.put("face_fields", "age");
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("image", base64Content)
                    .add("face_fields", "age")
                    .add("aipSdk","java")
                    .add("access_token",token)
                    .build();
            Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/rest/2.0/face/v2/detect")
                    .post(body)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = new String(response.body().bytes(), "UTF-8");
                    JSONObject json = null;
                    try {
                        json = new JSONObject(res);
                        Log.e("111", json.toString(2));
                        JSONArray jsonArr = json.getJSONArray("result");
                        JSONObject ss = jsonArr.getJSONObject(0);
                        age = ss.getString("age");
                        isMan=ss.getString("face_probability");
                        StringBuilder qq = new StringBuilder();
                        if (isMan.equals("1")){
                            qq.append("亲，您上传的图片是人。\n");
                        }
                        else{
                            qq.append("亲，您上传的图片不是人。\n");
                        }
                        qq.append("年龄约为：").append(age).append("岁");
                        xxxx=qq.toString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("111", xxxx);
                                face_check_result.setText(xxxx);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = new String(response.body().bytes(), "UTF-8");
                try {
                    JSONObject json = new JSONObject(res);
                    Log.e("111", json.toString(2));
                    Log.e("111", "验证完毕");
                    token = json.getString("access_token");
                    test();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return token;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_man_face);
        face_check_result = (TextView) findViewById(R.id.face_check_result);
        face_check = (Button) findViewById(R.id.face_check);
        face_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckManFaceActivity.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getSaveFile2(getApplication()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                        CameraActivity.CONTENT_TYPE_GENERAL);
                startActivityForResult(intent, 111);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            getToken();


        }
    }
}
