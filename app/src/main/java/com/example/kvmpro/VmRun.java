package com.example.kvmpro;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.kvmpro.databinding.ActivityVmRunBinding;

public class VmRun extends AppCompatActivity {

    private ActivityVmRunBinding binding;

    private String currentConsoleBuffer = "";

    private TextView vmOutputTextView;

    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vm_run);

        vmOutputTextView = findViewById(R.id.vm_output_console);
        scrollView = findViewById(R.id.scrollviewconsole);

        VMConfiguration vmCfg = getIntent().getParcelableExtra("VM_CONFIGURATION", VMConfiguration.class);
        runLoggingThread();
        startVMJni(vmCfg);
    }

    public void notifyConsoleUpdate(char c)
    {
        currentConsoleBuffer += c;
        System.out.print(c);

        if (c == '\n') {
            try {
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (vmOutputTextView != null) {
                            vmOutputTextView.setText(currentConsoleBuffer);
                            scrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    }
                });
                System.out.println("Should update text");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public native int startVMJni(VMConfiguration vm);

    public native int runLoggingThread();
}