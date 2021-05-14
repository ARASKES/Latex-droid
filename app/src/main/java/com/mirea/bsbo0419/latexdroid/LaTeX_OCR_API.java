package com.mirea.bsbo0419.latexdroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LaTeX_OCR_API {
    public static final String IP = "34.118.83.161";

    public String result = "";
    private Context context = null;
    private Bitmap bitmap;

    public LaTeX_OCR_API(Context context, Uri imageUri)
    {
        this.context = context;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String connectToServer()
    {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path
        if (bitmap == null)
            return "ERROR!!!";
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "temp.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        postRequest(postBodyImage);
        return result;
    }

    void postRequest(RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://" + IP +":" + "9090" + "/upload_image")
                .post(postBody)
                .build();


        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    try {
                        Response response = client.newCall(request).execute();
                        result = response.body().string();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // Cancel the post on failure.
//                call.cancel();
//                System.out.println(Arrays.toString(e.getStackTrace()));
//                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
//                result = "Error!";
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) throws IOException {
//                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
//                try {
//                    //responseText.setText(response.body().string());
//                    result = response.body().string();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

}

