package com.mirea.bsbo0419.latexdroid.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mirea.bsbo0419.latexdroid.R;
import com.mirea.bsbo0419.latexdroid.apis.LaTeX_OCR_API;
import com.mirea.bsbo0419.latexdroid.apis.WolframAPI;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT_IMAGE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    EditText answerText, equationText;
    Button calculateButton;
    ImageButton galleryButton, cameraButton;

    Uri currentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        equationText = findViewById(R.id.editTextEquation);
        answerText = findViewById(R.id.answerText);
        answerText.setEnabled(false);

        calculateButton = findViewById(R.id.calculateButton);
        galleryButton = findViewById(R.id.galleryButton);
        cameraButton = findViewById(R.id.cameraButton);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HideVirtualKeyboard();
                answerText.setText("");

                if (equationText.getText() != null && !equationText.getText().toString().equals("")) {
                    answerText.setEnabled(true);
                    QueryWolframAPI();
                } else {
                    HandleErrors();
                }
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageInGallery();
                answerText.setEnabled(true);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerText.setEnabled(true);
                dispatchTakePictureIntent();
            }
        });
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

    public void selectImageInGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        answerText.setText("");

        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    currentPhotoUri = data.getData();
                }
                
                QueryLatexAPI();
                QueryWolframAPI();
                break;
            case REQUEST_IMAGE_CAPTURE:
                CropImage.activity(currentPhotoUri)
                        .start(this);
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    currentPhotoUri = result.getUri();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    HandleErrors();
                }

                QueryLatexAPI();
                QueryWolframAPI();
                break;
            default:
                break;
        }
    }

    private void QueryWolframAPI() {
        ArrayList<String> result = WolframAPI.SendQuery(equationText.getText().toString());

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

    private void QueryLatexAPI() {
        LaTeX_OCR_API laTeXApiInstance = new LaTeX_OCR_API(this, currentPhotoUri);

        String response = laTeXApiInstance.GetResponseFromServer();
        if (response != null) {
            equationText.setText(response);
        } else {
            HandleErrors();
        }
    }

    private void HandleErrors() {
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
