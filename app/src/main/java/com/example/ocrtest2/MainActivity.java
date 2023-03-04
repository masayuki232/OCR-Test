package com.example.ocrtest2;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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


public class MainActivity extends AppCompatActivity implements OnSuccessListener<Text>, OnFailureListener {
    private static final String TAG = "OCR_TEST";

    private TextRecognizer textRecognizer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        // Find the TextView for displaying the status of the app
        TextView statusText = findViewById(R.id.status_text);


        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);


        handler = new Handler();

        // Start the text recognition process
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanCurrentScreen();
                handler.postDelayed(this, 1000); // Scan every 1 second
            }
        }, 0);
    }
    private Bitmap getScreenBitmap(View view) {
        // Get the dimensions of the screen
        int width = view.getWidth();
        int height = view.getHeight();

        // Create a bitmap with the same dimensions
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Draw the view into the bitmap
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scanCurrentScreen();
                handler.postDelayed(this, 1000); // Scan every 1 second
            }
        }, 0);
    }

    private void scanCurrentScreen() {
        // Get the current screen as a bitmap
        View rootView = getWindow().getDecorView().getRootView();
        Bitmap screenBitmap = getScreenBitmap(rootView);

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

                                    // Display the recognized text on the screen
                                    TextView statusText = findViewById(R.id.status_text);
                                    statusText.setText(recognizedText);
                                })
                        .addOnFailureListener(
                                e -> {
                                    // Handle any errors
                                    Log.e(TAG, "Text recognition failed: " + e.getMessage(), e);
                                    TextView statusText = findViewById(R.id.status_text);
                                    statusText.setText("Text recognition failed: " + e.getMessage());
                                });

        // Wait for the text recognition to complete
        while (!result.isComplete()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
            TextView statusText = findViewById(R.id.status_text);
            statusText.setText("Match found: " + matchString);

            // Trigger the desired action
            //performAction();
        }
    }

    @Override
    public void onFailure(Exception e) {
        // Handle the failure
    }




    // ...
}


