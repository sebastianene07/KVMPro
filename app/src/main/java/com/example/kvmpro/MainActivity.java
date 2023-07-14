package com.example.kvmpro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String STRING_NO_VM_CONFIGS = "No VM config. Tap to add one";
    private static final String STRING_ADD_NEW_VM = "Tap to add a new one";
    private KVMSettingsConfiguration _appCfg;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("app");
        System.loadLibrary("kvmtool");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read the saved config and populate the number of saved vms
        _appCfg = KVMSettingsConfiguration.getInstance(this);
        List<VMConfiguration> vmsCfg = _appCfg.getVMAvailableConfigs();

        ListView vmListView = findViewById(R.id.listview);
        final List<String> vmArrayList = new ArrayList<String>();
        if (vmsCfg.isEmpty()) {
            vmArrayList.add(STRING_NO_VM_CONFIGS);
        } else {
            for (VMConfiguration vmCfg : vmsCfg ) {
                vmArrayList.add(vmCfg.friendlyName);
            }

            vmArrayList.add(STRING_ADD_NEW_VM);
        }

        ArrayAdapter vmListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, vmArrayList);
        vmListView.setAdapter(vmListAdapter);
        vmListView.setOnItemLongClickListener((parent, view, position, id) -> {
            final String item = (String) parent.getItemAtPosition(position);
            if (item.equals(STRING_NO_VM_CONFIGS) || item.equals(STRING_ADD_NEW_VM)) {
                return false;
            }
            _appCfg.removeConfig(vmsCfg.get(position));
            vmArrayList.remove(item);
            vmListAdapter.notifyDataSetChanged();
            return true;
        })
        ;
        vmListView.setOnItemClickListener((parent, view, position, id) -> {
            final String item = (String) parent.getItemAtPosition(position);
            if (item.equals(STRING_NO_VM_CONFIGS) || item.equals(STRING_ADD_NEW_VM)) {
                // Spawn a view to configure a new VM
                // This will have to notify the vmListAdapter for object addition & modification
                _appCfg.writeVMConfig(new VMConfiguration("seb test VM", "Image", "disk.img"));
                vmArrayList.remove(item);
                vmArrayList.add("seb");
                vmArrayList.add(STRING_ADD_NEW_VM);
                vmListAdapter.notifyDataSetChanged();
            } else {
                // Spawn the VM >>> On another thread to make sure that we don't lockup
                // the poor UI thread.

                startVMJni(vmsCfg.get(position));
            }
        });

    }

    public native int startVMJni(VMConfiguration vm);
}
