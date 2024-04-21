package com.example.kvmpro;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.kvmpro.databinding.ActivityVmRunBinding;
import com.google.android.material.textfield.TextInputEditText;

public class VmRun extends AppCompatActivity {

    private ActivityVmRunBinding binding;

    private String currentConsoleBuffer = "";

    private TextView vmOutputTextView;

    private ScrollView scrollView;

    private TextInputEditText inputConsoleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vm_run);

        vmOutputTextView = findViewById(R.id.vm_output_console);
        scrollView = findViewById(R.id.scrollviewconsole);
        inputConsoleView = findViewById(R.id.inputConsoleView);
        inputConsoleView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    //do what you want on the press of 'done'
                    System.out.println(v.getText().toString());
                    sendConsoleCharacters(v.getText().toString() + "\n");
                    v.setText("");
                    scrollView.fullScroll(View.FOCUS_DOWN);
                    scrollView.setFocusable(false);
                    return true;
                }
                return false;
            }
        });

        VMConfiguration vmCfg = getIntent().getParcelableExtra("VM_CONFIGURATION", VMConfiguration.class);
        runLoggingThread();
        startVMJni(vmCfg);
    }

    private String unicodeColor;
    private enum unicode_color_parser {
        NOPE,
        START,
        MATCHED,
        END,
    };

    unicode_color_parser _state;

    public void notifyConsoleUpdate(char c)
    {
        if (c == '') {
            unicodeColor = "";
            _state = unicode_color_parser.START;
            return;
        } else if (c == '[' && _state != unicode_color_parser.NOPE) {
            /* may be the start of a unicode color */
            unicodeColor += c;
            _state = unicode_color_parser.MATCHED;
            return;
        } else if ((Character.isDigit(c) || (c == ';'))  && _state == unicode_color_parser.MATCHED) {
            unicodeColor += c;
            return;
        } else if ((c == 'm') && (_state == unicode_color_parser.MATCHED)) {
            _state = unicode_color_parser.NOPE;
            unicodeColor = "";
            return;
        }

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
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public native void sendConsoleCharacters(String characters);

    public native int startVMJni(VMConfiguration vm);

    public native int runLoggingThread();
}