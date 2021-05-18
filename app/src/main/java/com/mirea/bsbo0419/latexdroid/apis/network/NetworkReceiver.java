package com.mirea.bsbo0419.latexdroid.apis.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mirea.bsbo0419.latexdroid.R;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status;
        ImageButton galleryButton = ((Activity) context).findViewById(R.id.galleryButton),
                    cameraButton = ((Activity) context).findViewById(R.id.cameraButton);

        if (NetworkAPI.GetNetworkStatus(context)) {
            status = context.getResources().getString(R.string.network_established_message);

            galleryButton.setEnabled(true);
            cameraButton.setEnabled(true);
        } else {

            status = context.getResources().getString(R.string.network_not_found_message);

            galleryButton.setEnabled(false);
            cameraButton.setEnabled(false);
        }

        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
    }
}
