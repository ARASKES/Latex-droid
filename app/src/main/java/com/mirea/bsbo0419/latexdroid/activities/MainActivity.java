package com.mirea.bsbo0419.latexdroid.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mirea.bsbo0419.latexdroid.R;
import com.mirea.bsbo0419.latexdroid.apis.LaTeX_OCR_API;
import com.mirea.bsbo0419.latexdroid.apis.RPN.RPN_Core;
import com.mirea.bsbo0419.latexdroid.apis.RPN.RPN_Parser;
import com.mirea.bsbo0419.latexdroid.apis.WolframAPI;
import com.mirea.bsbo0419.latexdroid.apis.network.NetworkAPI;
import com.mirea.bsbo0419.latexdroid.apis.network.NetworkReceiver;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // Request IDs
    private static final int REQUEST_SELECT_IMAGE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    //  Views
    EditText answerText, equationText;
    ProgressBar loadingBar;

    // Data
    private Uri currentPhotoUri;
    private BroadcastReceiver broadcastReceiver = null;
    private boolean isReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        broadcastReceiver = new NetworkReceiver();

        loadingBar = findViewById(R.id.loadingBar);
        loadingBar.setVisibility(View.GONE);

        equationText = findViewById(R.id.editTextEquation);
        answerText = findViewById(R.id.answerText);
        answerText.setEnabled(false);

        dispatchBroadcastIntent();
    }

    //  onClick methods
    public void onCalculateClick(View view) {
        answerText.setText("");
        answerText.setEnabled(false);
        HideVirtualKeyboard();
        loadingBar.setVisibility(View.VISIBLE);

        new Thread(() -> CalculateFromInput(this)).start();
    }

    public void onGalleryClick(View view) {
        selectImageInGallery();
    }

    public void onCameraClick(View view) {
        dispatchTakePictureIntent();
    }

    public void onClearClick(View view) {
        equationText.setText("");
    }

    //  Intent preparation methods
    private void selectImageInGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                DisplayError();
                return;
            }

            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
            currentPhotoUri = photoURI;
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //  Activity result catching
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loadingBar.setVisibility(View.VISIBLE);
        answerText.setEnabled(false);

        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                new Thread(() -> {
                    if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                        runOnUiThread(() -> answerText.setText(""));

                        currentPhotoUri = data.getData();

                        QueryLatexAPI(this);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        QueryWolframAPI(this);
                    }

                    HideLoadingBar();
                }).start();
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    CropImage.activity(currentPhotoUri)
                            .start(this);
                } else {
                    HideLoadingBar();
                }

                return;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                new Thread(() -> {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == RESULT_OK) {
                        runOnUiThread(() -> answerText.setText(""));

                        currentPhotoUri = result.getUri();

                        QueryLatexAPI(this);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        QueryWolframAPI(this);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        DisplayError();
                    }

                    HideLoadingBar();
                }).start();
                break;
            default:
                HideLoadingBar();
                break;
        }
    }

    //  Calculation methods
    private void QueryLatexAPI(Context context) {
        String response = LaTeX_OCR_API.GetResponseFromServer(context, currentPhotoUri);
        if (response != null) {
            SetEquation(response);
        } else {
            DisplayError();
        }
    }

    private void QueryWolframAPI(Context context) {
        ArrayList<String> result = WolframAPI.SendQuery(equationText.getText().toString(), context);

        StringBuilder resultFormatted = new StringBuilder();
        if (result != null && result.size() != 0) {
            if (!result.get(0).equals("")) {
                for (String line : result) {
                    resultFormatted.append(line).append("\n");
                }
            } else {
                DisplayError();
                return;
            }
        } else {
            DisplayError();
            return;
        }

        if (!resultFormatted.toString().equals("")) {
            resultFormatted = new StringBuilder(resultFormatted.substring(0, resultFormatted.length() - 1));
            SetAnswer(resultFormatted.toString());
        } else {
            DisplayError();
        }
    }

    private void CalculateFromInput(Context context) {
        if (equationText.getText() != null && !equationText.getText().toString().equals("")) {
            if (NetworkAPI.GetNetworkStatus(context)) {
                QueryWolframAPI(context);
            } else {
                List<String> expression = RPN_Parser.getParsedStr(equationText.getText().toString());

                if (expression.size() != 0 && !expression.contains("Error")) {
                    String answer = String.valueOf(RPN_Core.calc(expression));
                    SetAnswer(answer);
                }
                else {
                    DisplayError();
                }
            }
        } else {
            DisplayError();
        }

        HideLoadingBar();
    }

    //  UI methods
    private void DisplayError() {
        runOnUiThread(() -> {
            loadingBar.setVisibility(View.GONE);
            answerText.setText(R.string.error_text, TextView.BufferType.EDITABLE);
            answerText.setEnabled(false);
        });
    }

    private void SetEquation(String equation) {
        runOnUiThread(() -> equationText.setText(equation));
    }

    private void SetAnswer(String answer) {
        runOnUiThread(() -> {
            answerText.setText(answer);
            answerText.setEnabled(true);
        });
    }

    private void HideLoadingBar() {
        runOnUiThread(() -> loadingBar.setVisibility(View.GONE));
    }

    private void HideVirtualKeyboard() {
        View focusedView = this.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }

    //  Broadcast receiver (un-)registering
    public void dispatchBroadcastIntent() {
        if (!isReceiverRegistered) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            if (isReceiverRegistered) {
                unregisterReceiver(broadcastReceiver);
                isReceiverRegistered = false;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        NetworkReceiver.isReferredFirstTime = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        dispatchBroadcastIntent();
    }
}
