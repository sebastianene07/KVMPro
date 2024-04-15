package com.example.kvmpro;

import java.io.Serializable;

public class VMConfiguration implements Serializable {
    public String friendlyName;
    public String kernelImageFilename;
    public String diskFileName;
    public String appendedBootargs;
    public String homePath;

    public String initrdFileName;

    public VMConfiguration()
    {
    }

    public VMConfiguration(String friendlyName, String kernelImage, String diskName)
    {
        this.friendlyName = friendlyName;
        this.kernelImageFilename = kernelImage;
        this.diskFileName = diskName;
    }

    public VMConfiguration(String friendlyName, String kernelImage, String diskName, String initrdFileName)
    {
        this.friendlyName = friendlyName;
        this.kernelImageFilename = kernelImage;
        this.diskFileName = diskName;
        this.initrdFileName = initrdFileName;
    }
}
