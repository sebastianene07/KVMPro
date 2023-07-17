package com.example.kvmpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileNotFoundException;

public class VmActivity extends AppCompatActivity {

    private static final int PICKFILE_KERNEL_IMAGE_REQ_CODE = 8778;
    private static final int PICKFILE_DISK_IMAGE_REQ_CODE = 8779;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vm);

        Button kImageButton = findViewById(R.id.kernelImageButtonId);
        kImageButton.setOnClickListener(view -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            startActivityForResult(
                    Intent.createChooser(chooseFile, "Choose a file"),
                    PICKFILE_KERNEL_IMAGE_REQ_CODE
            );
        });

        Button diskImageButton = findViewById(R.id.diskImageButtonId);
        diskImageButton.setOnClickListener(view -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            startActivityForResult(
                    Intent.createChooser(chooseFile, "Choose a file"),
                    PICKFILE_DISK_IMAGE_REQ_CODE
            );
        });

        Button doneButton = findViewById(R.id.doneButtonId);
        doneButton.setOnClickListener(view -> {
            // Put the String to pass back into an Intent and close this activity
            TextView kImgTextView = findViewById(R.id.kernerlImageNameId);
            TextView diskTextView = findViewById(R.id.diskImageNameId);
            TextInputEditText textInputView = findViewById(R.id.cfgNameTextInputId);

            Intent intent = new Intent();
            intent.putExtra(String.valueOf(R.string.KEYNAME_IMAGE_NAME), kImgTextView.getText().toString());
            intent.putExtra(String.valueOf(R.string.KEYNAME_DISK_NAME), diskTextView.getText().toString());
            intent.putExtra(String.valueOf(R.string.KEYNAME_CFG_NAME), textInputView.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private String getFileNameThatICanUseInNativeCode(Uri uri) throws FileNotFoundException {
        ParcelFileDescriptor mParcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");
        if (mParcelFileDescriptor != null) {
            int fd = mParcelFileDescriptor.getFd();
            File file = new File("/proc/self/fd/" + fd);
            String path = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    path = Os.readlink(file.getAbsolutePath()).toString();
                }
            } catch (ErrnoException e) {
                e.printStackTrace();
            }

            return path;
        }
        else{
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        // If the selection didn't work
        super.onActivityResult(requestCode, resultCode, returnIntent);

        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            return;
        } else if (requestCode == PICKFILE_KERNEL_IMAGE_REQ_CODE){
            Uri returnUri = returnIntent.getData();
            try {
                String filePath = getFileNameThatICanUseInNativeCode(returnUri);
                TextView kImageTextView = findViewById(R.id.kernerlImageNameId);
                kImageTextView.setText(filePath);
            } catch (FileNotFoundException e) {
                Log.e("kernelImage", e.toString());
            }
        } else if (requestCode == PICKFILE_DISK_IMAGE_REQ_CODE){
            Uri returnUri = returnIntent.getData();
            try {
                String filePath = getFileNameThatICanUseInNativeCode(returnUri);
                TextView kImageTextView = findViewById(R.id.diskImageNameId);
                kImageTextView.setText(filePath);
            } catch (FileNotFoundException e) {

            }
        }
    }
}