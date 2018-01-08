package com.manfacerecognition;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.recognizerdemo.R;
import com.wordrecognition.FileUtil;
import com.wordrecognition.ui.camera.CameraActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckManFaceActivity extends AppCompatActivity {
    public boolean flag = true;
    String pic_Path = Environment.getExternalStorageDirectory() + "/test.jpg";
    Button face_check;
    String token = "";
    TextView face_check_result;
    double age = 0.0;    //年龄
    double isMan = 0.0;  //人脸置信度
    String xxxx = "";
    double beauty = 0.00; //美丑评分
    int expression = 0;   //表情，0，不笑；1，微笑；2，大笑
    String gender = "";   //性别 male、female
    int glasses = 0;    //是否带眼镜，0-无眼镜，1-普通眼镜，2-墨镜
    String race = "";     //人种
    NumberFormat nf = NumberFormat.getPercentInstance();

    public void test() {
        nf.setMaximumFractionDigits(2);
        final long start = System.currentTimeMillis();
        byte[] imgData;
        try {
            imgData = com.withoutBaiduAPI.util.Util.readFileByBytes(pic_Path);
            String base64Content = com.withoutBaiduAPI.util.Util.encode(imgData);
//            HashMap<String, String> options = new HashMap<String, String>();
//            options.put("face_fields", "age");
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("image", base64Content)
                    .add("face_fields", "age,beauty,expression,gender,glasses,race")
                    .add("aipSdk", "java")
                    .add("access_token", token)
                    .build();
            Request request = new Request.Builder()
                    .url("https://aip.baidubce.com/rest/2.0/face/v2/detect")
                    .post(body)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CheckManFaceActivity.this, "网络连接异常", Toast.LENGTH_LONG).show();
                            face_check.setText("选择图片");
                            face_check.setEnabled(true);
                        }
                    });

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = new String(response.body().bytes(), "UTF-8");
                    JSONObject json = null;
                    try {
                        json = new JSONObject(res);
                        Log.e("111", json.toString(2));
                        int re = json.getInt("result_num");
                        if (re == 0)
                            xxxx = "亲，您上传的图片不是人脸\n" +
                                    "[识别耗费时间为：" +
                                    (System.currentTimeMillis() - start) +
                                    "ms]";
                        else {
                            JSONArray jsonArr = json.getJSONArray("result");
                            JSONObject ss = jsonArr.getJSONObject(0);
                            StringBuilder qq = new StringBuilder();
                            qq.append("亲，您上传的图片是人脸\n");
                            age = ss.getDouble("age");
                            isMan = ss.getDouble("face_probability");
                            qq.append("人脸置信度：" + nf.format(isMan) + "\n");
                            qq.append("年龄：").append((int) age).append("岁\n");
                            race = ss.getString("race");
                            switch (race) {
                                case "yellow":
                                    race = "黄种人";
                                    break;
                                case "white":
                                    race = "白种人";
                                    break;
                                case "black":
                                    race = "黑种人";
                                    break;
                                case "arabs":
                                    race = "阿拉伯人";
                                    break;
                            }
                            qq.append("人种：").append(race + "\n");
                            gender = ss.getString("gender");
                            if (gender.equals("male"))
                                qq.append("性别：").append("男性" + "\n");
                            else
                                qq.append("性别：").append("女性" + "\n");
                            beauty = ss.getDouble("beauty");
                            expression = ss.getInt("expression");
                            switch (expression) {
                                case 0:
                                    qq.append("表情：不笑\n");
                                    break;
                                case 1:
                                    qq.append("表情：" +
                                            "微笑\n");
                                    break;
                                case 2:
                                    qq.append("表情：" +
                                            "大笑\n");
                                    break;
                            }
                            glasses = ss.getInt("glasses");
                            switch (glasses) {
                                case 0:
                                    qq.append("眼镜：无眼镜\n");
                                    break;
                                case 1:
                                    qq.append("眼镜：普通眼镜\n");
                                    break;
                                case 2:
                                    qq.append("眼镜：墨镜\n");
                                    break;
                            }
                            int a = 0;
                            if (beauty >= 80.00)
                                a = 5;
                            else if (beauty < 80 && beauty >= 70) {
                                a = 4;
                            } else if (beauty < 70 && beauty >= 60)
                                a = 3;
                            else if (beauty < 60 && beauty >= 50)
                                a = 2;
                            else
                                a = 1;
                            String piaoliang = "";
                            switch (a) {
                                case 5:
                                    piaoliang = "☆☆☆☆☆";
                                    break;
                                case 4:
                                    piaoliang = "☆☆☆☆";
                                    break;
                                case 3:
                                    piaoliang = "☆☆☆";
                                    break;
                                case 2:
                                    piaoliang = "☆☆";
                                    break;
                                case 1:
                                    piaoliang = "☆";
                                    break;
                            }
                            qq.append("该人脸美丽指数(满分5星)：").append(piaoliang + "。\n");
                            qq.append("[识别耗费时间为：" + (System.currentTimeMillis() - start) + "ms]");
                            xxxx = qq.toString();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("111", xxxx);
                                face_check_result.setText(xxxx);
                                face_check.setText("选择图片");
                                face_check.setEnabled(true);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CheckManFaceActivity.this, "网络连接异常", Toast.LENGTH_LONG).show();
                        face_check.setText("选择图片");
                        face_check.setEnabled(true);
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
        flag = true;
        initPermission();
        face_check_result = (TextView) findViewById(R.id.face_check_result);
        face_check = (Button) findViewById(R.id.face_check);
        face_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag){
                    face_check_result.setText("");
                    Intent intent = new Intent(CheckManFaceActivity.this, CameraActivity.class);
                    intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                            FileUtil.getSaveFile2(getApplication()).getAbsolutePath());
                    intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                            CameraActivity.CONTENT_TYPE_GENERAL);
                    startActivityForResult(intent, 111);
                }
               else
                    Toast.makeText(CheckManFaceActivity.this,"请先授予权限",Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 111 && resultCode == Activity.RESULT_OK) {
            getToken();
            face_check.setText("图片正在识别请稍后");
            face_check.setEnabled(false);
        } else {

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
                    flag = true;
                } else {
                    Toast.makeText(CheckManFaceActivity.this, "缺少权限！", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void initPermission() {
        String permissions[] = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
            flag = false;
        }

    }
}
