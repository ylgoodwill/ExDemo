package com.withBaiduAPI.android.voicedemo.activity;

import com.withBaiduAPI.android.voicedemo.recognization.CommonRecogParams;
import com.withBaiduAPI.android.voicedemo.recognization.online.OnlineRecogParams;
public class ActivityOnline extends ActivityRecog{
    {
        DESC_TEXT = "请点击开始录音按钮录音\n" ;
    }
    public ActivityOnline(){
        super();
    }

    @Override
    protected CommonRecogParams getApiParams() {
        return new OnlineRecogParams(this);
    }


}