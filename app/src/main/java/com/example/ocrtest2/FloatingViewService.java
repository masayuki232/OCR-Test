package com.example.ocrtest2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class FloatingViewService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private boolean mIsEnabled;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inflate the floating view layout
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_view, new RelativeLayout(this), false);


        // Set up the click listener for the toggle button
        ImageView toggleButton = mFloatingView.findViewById(R.id.toggle_button);
        toggleButton.setOnClickListener(v -> {
            mIsEnabled = !mIsEnabled;
            if (mIsEnabled) {
                // TODO: Enable the app
                Toast.makeText(FloatingViewService.this, "App enabled", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Disable the app
                Toast.makeText(FloatingViewService.this, "App disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Add the floating view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT);
        mWindowManager.addView(mFloatingView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mFloatingView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
