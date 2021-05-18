package com.mirea.bsbo0419.latexdroid.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.BroadcastReceiver;
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
    Uri currentPhotoUri;
    BroadcastReceiver broadcastReceiver = null;

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

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void dispatchBroadcastIntent() {
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void onCalculateClick(View view) {
        HideVirtualKeyboard();
        answerText.setText("");
        loadingBar.setVisibility(View.VISIBLE);

        if (equationText.getText() != null && !equationText.getText().toString().equals("")) {
            answerText.setEnabled(true);
            if (NetworkAPI.GetNetworkStatus(this)) {
                QueryWolframAPI();
            } else {
                List<String> expression = RPN_Parser.getParsedStr(equationText.getText().toString());

                if (expression.size() != 0 && !expression.contains("Error")) {
                    answerText.setText(String.valueOf(RPN_Core.calc(expression)));
                }
                else {
                    HandleErrors();
                }
            }
        } else {
            HandleErrors();
        }
        
        loadingBar.setVisibility(View.GONE);
    }

    public void onGalleryClick(View view) {
        answerText.setEnabled(true);
        selectImageInGallery();
    }

    public void onCameraClick(View view) {
        answerText.setEnabled(true);
        dispatchTakePictureIntent();
    }

    public void onClearClick(View view) {
        equationText.setText("");
    }

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
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                HandleErrors();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        answerText.setText("");
        loadingBar.setVisibility(View.VISIBLE);

        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    currentPhotoUri = data.getData();

                    QueryLatexAPI();
                    QueryWolframAPI();
                } else {
                    answerText.setEnabled(false);
                }

                loadingBar.setVisibility(View.GONE);
                break;
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    CropImage.activity(currentPhotoUri)
                            .start(this);
                } else {
                    answerText.setEnabled(false);
                }

                loadingBar.setVisibility(View.GONE);
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                loadingBar.setVisibility(View.VISIBLE);

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    currentPhotoUri = result.getUri();

                    QueryLatexAPI();
                    QueryWolframAPI();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    HandleErrors();
                } else {
                    answerText.setEnabled(false);
                }

                loadingBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void QueryLatexAPI() {
        LaTeX_OCR_API laTeXApiInstance = new LaTeX_OCR_API(this, currentPhotoUri);

        String response = laTeXApiInstance.GetResponseFromServer();
        if (response != null) {
            equationText.setText(response);
        } else {
            HandleErrors();
        }
    }

    private void QueryWolframAPI() {
        ArrayList<String> result = WolframAPI.SendQuery(equationText.getText().toString(), this);

        String resultFormatted = "";
        if (result != null && result.size() != 0) {
            if (!result.get(0).equals("")) {
                for (String line : result) {
                    resultFormatted += line + "\n";
                }
            } else {
                HandleErrors();
                return;
            }
        } else {
            HandleErrors();
            return;
        }

        if (!resultFormatted.equals("")) {
            resultFormatted = resultFormatted.substring(0, resultFormatted.length() - 1);
            answerText.setText(resultFormatted);
        } else {
            HandleErrors();
        }
    }

    private void HandleErrors() {
        loadingBar.setVisibility(View.GONE);
        answerText.setText(R.string.error_text, TextView.BufferType.EDITABLE);
        answerText.setEnabled(false);
    }

    private void HideVirtualKeyboard() {
        View focusedView = this.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
        if (focusedView != null) {
            focusedView.clearFocus();
        }
    }
}
