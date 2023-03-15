package com.example.ocrtest2;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MyService extends Service implements OnSuccessListener<Text>, OnFailureListener {
    private static final String TAG = "OCR_SERVICE";

    private TextRecognizer textRecognizer;
    private Handler handler;
    private boolean isScanning;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the text recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Initialize the handler to scan periodically
        handler = new Handler();

        // Set the initial state of the scanner
        isScanning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Set the service to run in the foreground
        Notification notification = createNotification();
        startForeground(1, notification);

        // Start the text recognition process
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanCurrentScreen();
                handler.postDelayed(this, 1000); // Scan every 1 second
            }
        }, 0);

        // Return START_STICKY to ensure that the service continues running
        return START_STICKY;
    }

    private Notification createNotification() {
        // Create a notification to show that the service is running in the foreground
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("OCR Service")
                .setContentText("Scanning for matching strings...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create a pending intent to open the app when the notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // Build the notification and return it
        return builder.build();
    }

    private Bitmap getScreenBitmap() {
        // Get the root view of the current window
        View rootView = getWindow().getDecorView().getRootView();

        // Create a bitmap with the same dimensions as the root view
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);

        // Draw the root view into the bitmap
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);

        return bitmap;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSuccess(Text text) {
        // Convert the extracted text to a string
        String extractedText = text.getText();

        // Define the string you want to match
        String matchString = "example string";

        // Check if the extracted text contains the match string
        if (extractedText.contains(matchString)) {
            // Update the status of the app to show that the string has been found
            Log.i(TAG, "Match found: " + matchString);

            // Trigger the desired action
            //performAction();
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        // Handle failure
    }


    private void scanCurrentScreen() {
        // Get the current screen as a bitmap
        View viewToRead = findViewById(R.id.imageToRead);
        Bitmap screenBitmap = getScreenBitmap(viewToRead);

        // Check for errors in the bitmap
        if (screenBitmap == null) {
            Log.e(TAG, "Screen bitmap is null");
            return;
        }
        if (screenBitmap.getWidth() == 0 || screenBitmap.getHeight() == 0) {
            Log.e(TAG, "Screen bitmap has invalid dimensions: " + screenBitmap.getWidth() + "x" + screenBitmap.getHeight());
            return;
        }

        // Create an ML Kit InputImage from the bitmap
        InputImage inputImage = InputImage.fromBitmap(screenBitmap, 0);

        // Run the text recognition on the image
        Task<Text> result =
                textRecognizer.process(inputImage)
                        .addOnSuccessListener(
                                texts -> {
                                    // Get the recognized text
                                    String recognizedText = texts.getText();

                                    // Check if the recognized text contains the desired string
                                    String desiredString = "example string";
                                    if (recognizedText.contains(desiredString)) {
                                        // Do something here if the string is found
                                        Log.i(TAG, "Match found: " + desiredString);
                                    }
                                })
                        .addOnFailureListener(
                                e -> {
                                    // Handle any errors
                                    Log.e(TAG, "Text recognition failed: " + e.getMessage(), e);
                                });
    }



}