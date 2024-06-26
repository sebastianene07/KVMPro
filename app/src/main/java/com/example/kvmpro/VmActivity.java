package com.example.kvmpro;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class VmActivity extends AppCompatActivity {

    private static final int PICKFILE_KERNEL_IMAGE_REQ_CODE = 8778;
    private static final int PICKFILE_DISK_IMAGE_REQ_CODE = 8779;

    private static final int PICKFILE_INITRD_IMAGE_REQ_CODE = 8780;

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

        Button initrdImageButton = findViewById(R.id.initrdImageButtonId);
        initrdImageButton.setOnClickListener(view -> {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            startActivityForResult(
                    Intent.createChooser(chooseFile, "Choose a file"),
                    PICKFILE_INITRD_IMAGE_REQ_CODE
            );
        });

        Button doneButton = findViewById(R.id.doneButtonId);
        doneButton.setOnClickListener(view -> {
            // Put the String to pass back into an Intent and close this activity
            TextView kImgTextView = findViewById(R.id.kernerlImageNameId);
            TextView diskTextView = findViewById(R.id.diskImageNameId);
            TextView initrdTextView = findViewById(R.id.initrdImageNameId);
            TextInputEditText textInputView = findViewById(R.id.cfgNameTextInputId);

            Intent intent = new Intent();
            intent.putExtra(String.valueOf(R.string.KEYNAME_IMAGE_NAME), kImgTextView.getText().toString());
            intent.putExtra(String.valueOf(R.string.KEYNAME_CFG_NAME), textInputView.getText().toString());
            if (!diskTextView.getText().toString().equals(getResources().getString(R.string.disk_image_name))) {
                intent.putExtra(String.valueOf(R.string.KEYNAME_DISK_NAME), diskTextView.getText().toString());
            }

            if (!initrdTextView.getText().toString().equals(getResources().getString(R.string.initrd_image_name))) {
                intent.putExtra(String.valueOf(R.string.KEYNAME_INITRD_NAME), initrdTextView.getText().toString());
            }
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private String getFileNameThatICanUseInNativeCode(Uri uri) throws FileNotFoundException, IOException {
        ParcelFileDescriptor mParcelFileDescriptor = getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");
        if (mParcelFileDescriptor != null) {
            int fd = mParcelFileDescriptor.getFd();
            File file = new File("/proc/self/fd/" + fd);
            String path = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    path = Os.readlink(file.getAbsolutePath()).toString();
                    mParcelFileDescriptor.close();
                }
            } catch (ErrnoException e) {
                e.printStackTrace();
            }

            Log.i("getFileNameThatICanUseInNativeCode", path);
            return path;
        } else {
            return null;
        }
    }
    @SuppressLint("Range")
    public String getRealPathFromURI(Uri uri) throws IOException {
        String result;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor ==null)

        {
            result = uri.getPath();
            cursor.close();
            return result;
        }
        cursor.moveToFirst();
        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        cursor.close();

        File file =  File.createTempFile(result, "");
        FileOutputStream fos = new FileOutputStream(file);
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileUtils.copy(inputStream, fos);
        }

        return file.getAbsolutePath();
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
                filePath = getRealPathFromURI(returnUri);
                TextView kImageTextView = findViewById(R.id.kernerlImageNameId);
                kImageTextView.setText(filePath);
            } catch (IOException e) {
                Log.e("kernelImage", e.toString());
            }
        } else if (requestCode == PICKFILE_DISK_IMAGE_REQ_CODE){
            Uri returnUri = returnIntent.getData();
            try {
                String filePath = getFileNameThatICanUseInNativeCode(returnUri);
                filePath = getRealPathFromURI(returnUri);
                TextView kImageTextView = findViewById(R.id.diskImageNameId);
                kImageTextView.setText(filePath);
            } catch (IOException e) {

            }
        } else if (requestCode == PICKFILE_INITRD_IMAGE_REQ_CODE){
            Uri returnUri = returnIntent.getData();
            try {
                String filePath = getFileNameThatICanUseInNativeCode(returnUri);
                filePath = getRealPathFromURI(returnUri);
                TextView initrdTextView = findViewById(R.id.initrdImageNameId);
                initrdTextView.setText(filePath);
            } catch (IOException e) {

            }
        }
    }
}