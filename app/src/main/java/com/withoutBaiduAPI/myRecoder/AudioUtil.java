package com.withoutBaiduAPI.myRecoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class AudioUtil {
    private OnAudioStatusUpdateListener audioStatusUpdateListener;
    /**
     * 更新话筒状态
     */
    private int BASE = 1;
    private int SPACE = 100;// 间隔取样时间
    private final Handler mHandler = new Handler();
    public double volume = 0.00;
    private static AudioUtil mInstance;
    private AudioRecord recorder;
    //录音源
    private static int audioSource = MediaRecorder.AudioSource.MIC;
    //录音的采样频率
    private static int audioRate = 16000;
    //录音的声道，单声道
    private static int audioChannel = AudioFormat.CHANNEL_IN_MONO;
    //量化的深度
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //缓存的大小
    private static int bufferSize = AudioRecord.getMinBufferSize(audioRate, audioChannel, audioFormat);
    //记录播放状态
    private boolean isRecording = false;
    //数字信号数组
    private byte[] noteArray;
    //PCM文件
    private File pcmFile;
    //文件输出流
    private OutputStream os;
    //文件目录
    private String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test111.pcm";

    private AudioUtil() {
        createFile();//创建文件
        recorder = new AudioRecord(audioSource, audioRate, audioChannel, audioFormat, bufferSize);
    }

    public synchronized static AudioUtil getInstance() {
        if (mInstance == null) {
            mInstance = new AudioUtil();
        }
        return mInstance;
    }

    public interface OnAudioStatusUpdateListener {
        public void onUpdate(double db);
    }

    //读取录音数字数据线程
    class WriteThread implements Runnable {
        public void run() {
            writeData();
        }
    }

    //开始录音
    public void startRecord() {
        isRecording = true;
        recorder.startRecording();
        recordData();
        updateMicStatus();
    }

    //停止录音
    public void stopRecord() {
        isRecording = false;
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
            }
        }
    }

    //将数据写入文件夹,文件的写入没有做优化

    public void writeData() {
        noteArray = new byte[bufferSize];
        //建立文件输出流
        try {
            os = new BufferedOutputStream(new FileOutputStream(pcmFile));
        } catch (IOException e) {

        }
        while (isRecording == true) {
            int recordSize = recorder.read(noteArray, 0, bufferSize);
            if (recordSize > 0) {
                try {
                    os.write(noteArray);
                } catch (IOException e) {

                }
            }

            int v = 0;
            for (int i = 0; i < noteArray.length; i++) {
                v += noteArray[i] * noteArray[i];
            }
            // 平方和除以数据总长度，得到音量大小。
            double mean = v / (double) recordSize;
            volume = 10 * Math.log10(mean);
        }
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {

            }
        }
    }


    public void createFile() {
        pcmFile = new File(basePath);
        if (pcmFile.exists()) {
            pcmFile.delete();
        }
        try {
            pcmFile.createNewFile();
        } catch (IOException e) {
        }
    }

    //记录数据
    public void recordData() {
        new Thread(new WriteThread()).start();
    }

    private void updateMicStatus() {
        if (recorder != null) {
            double ratio = volume;
            double db = 0;// 分贝
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
                if (null != audioStatusUpdateListener) {
                    audioStatusUpdateListener.onUpdate(db);
                }
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, SPACE);
        }
    }

    public void setOnAudioStatusUpdateListener(OnAudioStatusUpdateListener audioStatusUpdateListener) {
        this.audioStatusUpdateListener = audioStatusUpdateListener;
    }

    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };
}
