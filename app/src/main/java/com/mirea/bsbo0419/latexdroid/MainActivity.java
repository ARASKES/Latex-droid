package com.mirea.bsbo0419.latexdroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT_IMAGE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    EditText answerText, equationText;
    Button calculateButton;
    ImageButton galleryButton, cameraButton;

    Uri currentPhotoUri;

    /*
    * Паша, тебе на строки 139, 143
    * Ангелина, тебе на строки 58, а еще 140, 144
    */

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
                answerText.setEnabled(true);
                if (equationText.getText() != null && !equationText.getText().toString().equals("")) {
                    answerText.setEnabled(true);

                    // Вызов функции Ангелины, Ангелина берет equationText.getText().toString(), а результат пихает в answerText.setText()
                } else {
                    answerText.setText("Error occured", TextView.BufferType.EDITABLE);
                    answerText.setEnabled(false);
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
                dispatchTakePictureIntent();
                answerText.setEnabled(true);
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
                answerText.setText("Error occured", TextView.BufferType.EDITABLE);
                answerText.setEnabled(false);
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                currentPhotoUri = photoURI;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } else {
                answerText.setText("Error occured", TextView.BufferType.EDITABLE);
                answerText.setEnabled(false);
            }
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
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    currentPhotoUri = data.getData();
                }

                // Вызов функции Паши, Паша берет currentPhotoUri, а результат пихает в equationText.setText()
                // После того, как отработала функция Паши, вызывается функция Ангелины (наверное в конце Пашиной)
                break;
            case REQUEST_IMAGE_CAPTURE:
                // Вызов функции Паши, Паша берет currentPhotoUri, а результат пихает в equationText.setText()
                // После того, как отработала функция Паши, вызывается функция Ангелины (наверное в конце Пашиной)
                break;
            default:
                break;
        }
    }
}