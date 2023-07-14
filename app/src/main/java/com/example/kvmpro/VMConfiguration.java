package com.example.kvmpro;

import java.io.Serializable;

public class VMConfiguration implements Serializable {
    public String friendlyName;
    public String kernelImageFilename;
    public String diskFileName;
    public String appendedBootargs;

    public VMConfiguration()
    {
    }

    public VMConfiguration(String friendlyName, String kernelImage, String diskName)
    {
        this.friendlyName = friendlyName;
        this.kernelImageFilename = kernelImage;
        this.diskFileName = diskName;
    }
}
