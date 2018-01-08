package com.ar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.baidu.ar.ARFragment;
import com.baidu.ar.bean.DuMixARConfig;
import com.baidu.ar.plugin.ARCallbackClient;
import com.baidu.ar.util.Constants;
import com.baidu.ar.util.Res;
import com.baidu.speech.recognizerdemo.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ARActivity extends FragmentActivity {

    private ARFragment mARFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // 设置获取资源的上下文Context
        Res.addResource(this);
        // 设置App Id
        DuMixARConfig.setAppId("11597");
        // 设置API Key
        DuMixARConfig.setAPIKey("1429eef92ff5f9ccded6263b1d438360");
        // 设置Secret Key
        DuMixARConfig.setSecretKey("您的Secret Key");

        if (findViewById(R.id.bdar_id_fragment_container) != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Bundle data = new Bundle();
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put(Constants.AR_KEY, 10006416);
                jsonObj.put(Constants.AR_TYPE, 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            data.putString(Constants.AR_VALUE, jsonObj.toString());
            if (mARFragment != null) {
                mARFragment.release();
                mARFragment = null;
            }

            mARFragment = new ARFragment();
            mARFragment.setArguments(data);
            mARFragment.setARCallbackClient(new ARCallbackClient() {
                // 分享接口
                @Override
                public void share(String title, String content, String shareUrl, String iconUrl, int type) {
                    // type = 1 视频，type = 2 图片
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, content);
                    shareIntent.putExtra(Intent.EXTRA_TITLE, title);
                    shareIntent.setType("text/plain");
                    // 设置分享列表的标题，并且每次都显示分享列表
                    startActivity(Intent.createChooser(shareIntent, "分享到"));
                }

                // 透传url接口：当AR Case中需要传出url时通过该接口传出url
                @Override
                public void openUrl(String url) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri contentUrl = Uri.parse(url);
                    intent.setData(contentUrl);
                    startActivity(intent);
                    Log.e("111", "AR功能支持 " );
                    ARActivity.this.finish();
                }

                // AR黑名单回调接口：当手机不支持AR时，通过该接口传入退化H5页面的url
                @Override
                public void nonsupport(String url) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri contentUrl = Uri.parse(url);
                    intent.setData(contentUrl);
                    startActivity(intent);
                    ARActivity.this.finish();
                    Log.e("111", "AR功能不支持 " );
                }
            });
            // 将ARFragment设置到布局上
            fragmentTransaction.replace(R.id.bdar_id_fragment_container, mARFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void onBackPressed() {
        boolean backFlag = false;
        if (mARFragment != null) {
            backFlag = mARFragment.onFragmentBackPressed();
        }
        if (!backFlag) {
            super.onBackPressed();
        }
    }
}