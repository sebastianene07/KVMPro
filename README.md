# KVMPro

### Description

Android application which allows you to run virtual machines on your Android device. This uses kvmtool to interact with the `/dev/kvm` which is built inside the app as a native library.

### Requirements

!! You need root access to be able to patch the SELinux policy on your device to give kvm-device access to this app. The script `scripts/patch_selinux.sh` can be used to patch the SELinux policy on your device.\
!! Kernel cmdline patching **optionaly** - from fastboot to be able to enable VHE mode otherwise it will run in **nVHE Protected** mode which will slow down things.

Running this should put your device in VHE mode:\
`fastboot oem cmdline add id_aa64mmfr1.vh=1` 

### Known issues

1. App returns `KVM_RUN with no memory`.

This is because you are not running in VHE mode. Untrusted application have a restriction on the number of memlocks identified by `RLIMIT_MEMLOCK`. To overcome this, the application should be able to overwrite this limit but this would require the app to have `CAP_SYS_RESOURCE` capability. Another option would be for the app to run with `CAP_IPC_LOCK`.

2. pci__init error -17

Kvmtool doesn't clean the registered MMIO devices upon restart using kvm__deregister_iotrap. This happens when you didn't kill the app after returning from a current VM session. The **easiest** temporary solution is just to kill the app if you want to start another VM.



