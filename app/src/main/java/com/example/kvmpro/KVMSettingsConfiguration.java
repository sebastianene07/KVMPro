package com.example.kvmpro;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KVMSettingsConfiguration {
    private static KVMSettingsConfiguration _cfg = null;
    private ArrayList<VMConfiguration> _vmConfigs = null;
    private static final String FILENAME_KVM_SETTINGS = "kvmSettings.txt";
    private Context _appCtxt;

    private KVMSettingsConfiguration(Context appCtxt) {
        _appCtxt = appCtxt;
        _vmConfigs = new ArrayList<VMConfiguration>();

        // Try and open the configuration file
        try (FileInputStream fis = appCtxt.openFileInput(FILENAME_KVM_SETTINGS);
             ObjectInputStream ois = new ObjectInputStream(fis);) {
            _vmConfigs = (ArrayList) ois.readObject();
        }
        catch (FileNotFoundException e) {
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized KVMSettingsConfiguration getInstance(Context appCtxt)
    {
        if (_cfg == null)
            _cfg = new KVMSettingsConfiguration(appCtxt);

        return _cfg;
    }

    private void updateStorageConfig()
    {
        // Update it on the disk
        try (FileOutputStream fos = _appCtxt.openFileOutput(FILENAME_KVM_SETTINGS, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos);) {
            oos.writeObject(_vmConfigs);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<VMConfiguration> getVMAvailableConfigs()
    {
        return _vmConfigs;
    }

    public void removeConfig(VMConfiguration cfg)
    {
        synchronized (_vmConfigs)
        {
            if (_vmConfigs.remove(cfg)) {
                updateStorageConfig();
            }
        }
    }

    public void writeVMConfig(VMConfiguration cfg)
    {
        synchronized (_vmConfigs)
        {
            _vmConfigs.add(cfg);
            updateStorageConfig();
        }
    }
}
