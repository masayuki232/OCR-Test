package com.example.ocrtest2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import androidx.appcompat.app.AppCompatActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenCaptureActivity extends AppCompatActivity {
    private MediaProjection.Callback medProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            super.onStop();
            // Stop and release the MediaRecorder object
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }

            // Stop and release the VirtualDisplay object
            if (virtualDisplay != null) {
                virtualDisplay.release();
                virtualDisplay = null;
            }

            // Stop and release the MediaProjection object
            if (mediaProjection != null) {
                mediaProjection.unregisterCallback(this);
                mediaProjection.stop();
                mediaProjection = null;
            }
        }
    };
    private static final String TAG = "ScreenCaptureActivity";
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    private MediaRecorder mediaRecorder;

    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private int screenDensity;

    private int DISPLAY_WIDTH;
    private int DISPLAY_HEIGHT;
    private int RECORD_WIDTH;
    private int RECORD_HEIGHT;
    private int VIDEO_BITRATE = 12000000; // 12Mbps
    private int VIDEO_FPS = 30;

    private String filePath;
    private String fileName = "example.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenDensity = metrics.densityDpi;
        DISPLAY_WIDTH = metrics.widthPixels;
        DISPLAY_HEIGHT = metrics.heightPixels;
        RECORD_WIDTH = DISPLAY_WIDTH;
        RECORD_HEIGHT = DISPLAY_HEIGHT;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkSelfPermission("android.permission.RECORD_AUDIO") == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE);
        } else {
            requestPermissions(new String[]{"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1001);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // This method is called when the activity is destroyed
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE && resultCode == Activity.RESULT_OK) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            mediaProjection.registerCallback(medProjectionCallback, null);
            startRecording();
        }
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(getFilePath());
        mediaRecorder.setVideoSize(RECORD_WIDTH, RECORD_HEIGHT);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncodingBitRate(VIDEO_BITRATE);
        mediaRecorder.setVideoFrameRate(VIDEO_FPS);

        try {
            mediaRecorder.prepare();
        } catch (Exception e) {
            Log.e(TAG, "Exception preparing MediaRecorder: " + e.getMessage());
            return;
        }

        virtualDisplay = mediaProjection.createVirtualDisplay(TAG, RECORD_WIDTH, RECORD_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);

        mediaRecorder.start();
    }

    private String getFilePath() {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "ScreenCaptures");

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        filePath = directory.getAbsolutePath() + File.separator + timestamp + "_" + fileName;

        return filePath;
    }




}