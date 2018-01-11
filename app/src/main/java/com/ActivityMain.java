package com;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ar.ARActivity;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.speech.recognizerdemo.R;
import com.manfacerecognition.CheckManFaceActivity;
import com.withBaiduAPI.android.voicedemo.activity.ActivityOnline;
import com.withoutBaiduAPI.myRecoder.WithoutBaiduAPIMainActivity;
import com.wordrecognition.FileUtil;
import com.wordrecognition.ui.camera.CameraActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivityMain extends AppCompatActivity {
    Button voice_Re;
    Button voice_re2;
    Button word_Re;
    Button bankCard_re;
    Button manface_re;
    Button ar_re;
    private boolean hasGotToken = false;
    private AlertDialog.Builder alertDialog;
    private static final int REQUEST_CODE_GENERAL = 105;
    private static final int REQUEST_CODE_GENERAL_BASIC = 106;
    private static final int REQUEST_CODE_ACCURATE_BASIC = 107;
    private static final int REQUEST_CODE_ACCURATE = 108;
    private static final int REQUEST_CODE_GENERAL_ENHANCED = 109;
    private static final int REQUEST_CODE_GENERAL_WEBIMAGE = 110;
    private static final int REQUEST_CODE_BANKCARD = 111;
    private static final int REQUEST_CODE_VEHICLE_LICENSE = 120;
    private static final int REQUEST_CODE_DRIVING_LICENSE = 121;
    private static final int REQUEST_CODE_LICENSE_PLATE = 122;
    private static final int REQUEST_CODE_BUSINESS_LICENSE = 123;
    private static final int REQUEST_CODE_RECEIPT = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        alertDialog = new AlertDialog.Builder(this);
        voice_Re = (Button) findViewById(R.id.voice_re);
        voice_re2 = (Button) findViewById(R.id.voice_re2);
        word_Re = (Button) findViewById(R.id.word_re);
        bankCard_re = (Button) findViewById(R.id.bankCard_re);
        manface_re = (Button) findViewById(R.id.manface_re);
        ar_re = (Button) findViewById(R.id.ar_re);
        voice_Re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityMain.this, ActivityOnline.class));
            }
        });
        voice_re2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityMain.this, WithoutBaiduAPIMainActivity.class));
            }
        });
        word_Re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkTokenStatus()) {
                    return;
                }
                Intent intent = new Intent(ActivityMain.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                        CameraActivity.CONTENT_TYPE_GENERAL);
                startActivityForResult(intent, REQUEST_CODE_ACCURATE_BASIC);//高精度文字识别
            }

        });
        bankCard_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkTokenStatus()) {
                    return;
                }
                Intent intent = new Intent(ActivityMain.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                        CameraActivity.CONTENT_TYPE_GENERAL);
                startActivityForResult(intent, REQUEST_CODE_BANKCARD);//银行卡识别
            }
        });
        manface_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ActivityMain.this, CheckManFaceActivity.class));
            }
        });
        ar_re.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityMain.this, ARActivity.class);
                intent.putExtra("ar_key", 10006416);
                intent.putExtra("ar_type", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        initAccessTokenWithAkSk();
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放内存资源
        OCR.getInstance().release();
    }

    private void infoPopText(final String result) {
        alertText("识别结果为：", result);
    }

    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
                word_Re.setEnabled(true);
                word_Re.setText("文字识别");
                bankCard_re.setEnabled(true);
                bankCard_re.setText("银行卡识别");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 识别成功回调，通用文字识别（含位置信息）
        if (requestCode == REQUEST_CODE_GENERAL && resultCode == Activity.RESULT_OK) {
            RecognizeService.recGeneral(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText(result);
                        }
                    });
        }

        // 识别成功回调，通用文字识别（含位置信息高精度版）
        if (requestCode == REQUEST_CODE_ACCURATE && resultCode == Activity.RESULT_OK) {
            RecognizeService.recAccurate(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            try {
                                JSONObject json = new JSONObject(result);
                                String xx = json.toString(2);
                                Log.e("111", "得到的json数据为" + xx);
                                StringBuilder resultString = new StringBuilder();
                                JSONArray jsonArr = json.getJSONArray("words_result");
                                for (int i = 0; i < jsonArr.length(); i++) {
                                    JSONObject object = jsonArr.getJSONObject(i);
                                    String re = object.getString("words");
                                    resultString.append(re).append("\n");
                                }
                                String xxx = resultString.toString();
                                infoPopText(xxx);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
        // 识别成功回调，通用文字识别
        if (requestCode == REQUEST_CODE_GENERAL_BASIC && resultCode == Activity.RESULT_OK) {
            RecognizeService.recGeneralBasic(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            Log.e("111", "通用文字识别" + result);
                            //infoPopText(result);
                        }
                    });
        }

        // 识别成功回调，通用文字识别（高精度版）
        if (requestCode == REQUEST_CODE_ACCURATE_BASIC && resultCode == Activity.RESULT_OK) {
            word_Re.setEnabled(false);
            word_Re.setText("文字正在识别请稍后");
            final long startT = System.currentTimeMillis();
            RecognizeService.recAccurateBasic(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            try {
                                JSONObject json = new JSONObject(result);
                                String xx = json.toString(2);
                                Log.e("111", "文字识别得到的json数据为" + xx);
                                StringBuilder resultString = new StringBuilder();
                                int words_result_num = json.getInt("words_result_num");
                                if (words_result_num!=0){
                                    JSONArray jsonArr = json.getJSONArray("words_result");
                                    for (int i = 0; i < jsonArr.length(); i++) {
                                        JSONObject object = jsonArr.getJSONObject(i);
                                        String re = object.getString("words");
                                        resultString.append(re).append("\n");
                                    }
                                    resultString.append("[识别耗费时间为：").append(System.currentTimeMillis()
                                            - startT).append("ms]");
                                    String xxx = resultString.toString();
                                    infoPopText(xxx);
                                }
                                else {
                                    String xx1 = "未识别到文字\n"+
                                            "[识别耗费时间为："+
                                            (System.currentTimeMillis()-startT)+
                                            "ms]";
                                    infoPopText(xx1);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }

        // 识别成功回调，通用文字识别（含生僻字版）
        if (requestCode == REQUEST_CODE_GENERAL_ENHANCED && resultCode == Activity.RESULT_OK) {
            RecognizeService.recGeneralEnhanced(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            Log.e("111", "通用文字识别（含生僻字版）" + result);
                            // infoPopText(result);
                        }
                    });
        }

        // 识别成功回调，网络图片文字识别
        if (requestCode == REQUEST_CODE_GENERAL_WEBIMAGE && resultCode == Activity.RESULT_OK) {
            RecognizeService.recWebimage(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            Log.e("111", "网络图片文字识别" + result);
                            // infoPopText(result);
                        }
                    });
        }

        // 识别成功回调，银行卡识别
        if (requestCode == REQUEST_CODE_BANKCARD && resultCode == Activity.RESULT_OK) {
            bankCard_re.setEnabled(false);
            bankCard_re.setText("银行卡正在识别请稍后");
            final long startTime = System.currentTimeMillis();
            RecognizeService.recBankCard(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            Log.e("111", result );
                            long xx = System.currentTimeMillis() - startTime;
                            StringBuilder b = new StringBuilder();
                            if (!result.startsWith("卡")){
                                b.append("识别错误，请传入正确银行卡。\n")
                                        .append("[识别耗费时间为：").append(xx).append("ms]");
                            }
                            else{
                                b.append(result+"\n")
                                        .append("[识别耗费时间为:").append(xx).append("ms]");
                            }
                            infoPopText(b.toString());
                        }
                    });
        }

        // 识别成功回调，行驶证识别
        if (requestCode == REQUEST_CODE_VEHICLE_LICENSE && resultCode == Activity.RESULT_OK) {
            RecognizeService.recVehicleLicense(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText(result);
                        }
                    });
        }

        // 识别成功回调，驾驶证识别
        if (requestCode == REQUEST_CODE_DRIVING_LICENSE && resultCode == Activity.RESULT_OK) {
            RecognizeService.recDrivingLicense(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText(result);
                        }
                    });
        }

        // 识别成功回调，车牌识别
        if (requestCode == REQUEST_CODE_LICENSE_PLATE && resultCode == Activity.RESULT_OK) {
            final long start =System.currentTimeMillis();
            RecognizeService.recLicensePlate(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            Log.e("111", result );
                            long xx = System.currentTimeMillis() - start;
                            StringBuilder b = new StringBuilder();
                            if (result.startsWith("[282000]")){
                                b.append("识别错误，请重新上传。\n")
                                        .append("[识别耗费时间为：").append(xx).append("ms]");
                            }
                            else{
                                try {
                                    JSONObject kk = new JSONObject(result);
                                    String color = kk.getString("color");
                                    String number = kk.getString("number");
                                    b.append("车牌颜色为："+color+"\n")
                                            .append("车牌号为："+number+"\n")
                                            .append("[识别耗费时间为:").append(xx).append("ms]");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.e("111", b.toString() );

                            }
                            infoPopText(b.toString());
                        }
                    });
        }

        // 识别成功回调，营业执照识别
        if (requestCode == REQUEST_CODE_BUSINESS_LICENSE && resultCode == Activity.RESULT_OK) {
            RecognizeService.recBusinessLicense(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText(result);
                        }
                    });
        }

        // 识别成功回调，通用票据识别
        if (requestCode == REQUEST_CODE_RECEIPT && resultCode == Activity.RESULT_OK) {
            RecognizeService.recReceipt(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(String result) {
                            infoPopText(result);
                        }
                    });
        }

    }

    private void initAccessToken() {
        OCR.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("licence方式获取token失败", error.getMessage());
            }
        }, getApplicationContext());
    }

    private void initAccessTokenWithAkSk() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(), "GXxrqtLF6QEbPXaSY08ClGd5", "cKG4MpT07KHWWUQCQvV5lSAVS9xQGcT6");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initAccessToken();
        } else {
            Toast.makeText(getApplicationContext(), "需要android.permission.READ_PHONE_STATE", Toast.LENGTH_LONG).show();
        }
    }
}

