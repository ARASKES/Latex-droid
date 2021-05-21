package com.mirea.bsbo0419.latexdroid.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.mirea.bsbo0419.latexdroid.R;
import com.mirea.bsbo0419.latexdroid.apis.LaTeX_OCR_API;
import com.mirea.bsbo0419.latexdroid.apis.WolframAPI;

import java.util.Objects;

public class ConfigDialog extends AppCompatDialogFragment {
    private EditText editTextIp, editTextAppId;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));

        View view = requireActivity().getLayoutInflater().inflate(R.layout.config_dialog, null);

        builder.setView(view)
                .setTitle(R.string.config_dialog_title)
                .setPositiveButton(R.string.dialog_positive_button, (dialog, which) -> {
                    LaTeX_OCR_API.SetIP(editTextIp.getText().toString());
                    WolframAPI.SetAppID(editTextAppId.getText().toString());
                })
                .setNegativeButton(R.string.dialog_negative_button, null);

        editTextIp = view.findViewById(R.id.ip_address_edittext);
        if (LaTeX_OCR_API.GetIP() == null) {
            editTextIp.setText(R.string.server_ip_address);
        } else {
            editTextIp.setText(LaTeX_OCR_API.GetIP());
        }

        editTextAppId = view.findViewById(R.id.app_id_edittext);
        if (WolframAPI.GetAppID() == null) {
            editTextAppId.setText(R.string.wolfram_app_id);
        }  else {
            editTextAppId.setText(WolframAPI.GetAppID());
        }

        return builder.create();
    }
}
