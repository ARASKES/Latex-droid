package com.mirea.bsbo0419.latexdroid.apis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.mirea.bsbo0419.latexdroid.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LaTeX_OCR_API {
    public static String IP = null;

    public String result = "";
    private Bitmap bitmap;

    public LaTeX_OCR_API(Context context, Uri imageUri)
    {
        IP = context.getString(R.string.server_ip_address);

        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String GetResponseFromServer()
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path
        if (bitmap == null)
            return null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "temp.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        PostRequest(postBodyImage);
        return result;
    }

    void PostRequest(RequestBody postBody) {

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
    }
}
