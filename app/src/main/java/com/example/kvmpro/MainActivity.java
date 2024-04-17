package com.example.kvmpro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String STRING_NO_VM_CONFIGS = "No VM config. Tap to add one";
    private static final String STRING_ADD_NEW_VM = "Tap to add a new one";

    public static final int INT_CONFIGURE_VM = 1;
    public static final int INT_END_CONFIGURE_VM = 2;
    public static final int INT_START_VM = 3;
    public static final int INT_END_START_VM = 4;

    private ArrayAdapter vmListAdapter;
    private List<String> vmArrayList;

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
        vmArrayList = new ArrayList<String>();
        if (vmsCfg.isEmpty()) {
            vmArrayList.add(STRING_NO_VM_CONFIGS);
        } else {
            for (VMConfiguration vmCfg : vmsCfg ) {
                vmArrayList.add(vmCfg.friendlyName);
            }

            vmArrayList.add(STRING_ADD_NEW_VM);
        }

        vmListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, vmArrayList);
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
        });
        vmListView.setOnItemClickListener((parent, view, position, id) -> {
            final String item = (String) parent.getItemAtPosition(position);
            if (item.equals(STRING_NO_VM_CONFIGS) || item.equals(STRING_ADD_NEW_VM)) {
                // Spawn a view to configure a new VM
                vmArrayList.remove(item);
                vmListAdapter.notifyDataSetChanged();
                startActivityForResult(new Intent(this, VmActivity.class), INT_CONFIGURE_VM);

            } else {
                // Spawn the VM >>> On another thread to make sure that we don't lockup
                // the poor UI thread.

                VMConfiguration cfg = vmsCfg.get(position);
                String home_path = cfg.kernelImageFilename;

                try {
                    cfg.homePath = home_path.substring(0, home_path.lastIndexOf("/"));
                    Os.setenv("HOME", home_path.substring(0, home_path.lastIndexOf("/")), true);
                } catch (ErrnoException e) {
                    throw new RuntimeException(e);
                }

                VMConfiguration vmCfg = vmsCfg.get(position);
                Intent vmRunIntent = new Intent(this, VmRun.class);
                vmRunIntent.putExtra("VM_CONFIGURATION", vmCfg);
                startActivityForResult(vmRunIntent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INT_CONFIGURE_VM && resultCode == RESULT_OK && data != null) {
            String kernelImagePath = data.getStringExtra(String.valueOf(R.string.KEYNAME_IMAGE_NAME));
            String diskImagePath = data.getStringExtra(String.valueOf(R.string.KEYNAME_DISK_NAME));
            String initrdImagePath = data.getStringExtra(String.valueOf(R.string.KEYNAME_INITRD_NAME));
            String cfgName = data.getStringExtra(String.valueOf(R.string.KEYNAME_CFG_NAME));

            // This will have to notify the vmListAdapter for object addition & modification
            _appCfg.writeVMConfig(new VMConfiguration(cfgName, kernelImagePath, diskImagePath, initrdImagePath));
            vmArrayList.add(cfgName);
            vmArrayList.add(STRING_ADD_NEW_VM);
            vmListAdapter.notifyDataSetChanged();
        }
    }
}
