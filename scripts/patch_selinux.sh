#!/bin/bash
#
# Patch the SePolicy *cil file on device to allow untrusted android apps to
# access the /dev/kvm device node
#
# Author: Sebastian Ene sebastian.ene07@gmail.com

# Make sure we are runnind with adb root, disable verity with remount and reboot
adb root
adb remount
adb reboot

adb wait-for-device
adb root

# Remount RW the system_ext partition
adb shell mount -o remount,rw /system_ext
echo "[*] Remount /system_ext RW success"

# Pull the sepolicy *cil
adb pull /system_ext/etc/selinux/system_ext_sepolicy.cil

# Patch it locally to allow access to kvm_device for untrusted_app_32
echo  '(allow untrusted_app_32 kvm_device (chr_file (read write ioctl open)))' >> system_ext_sepolicy.cil
echo "[*] Done patching system_ext_sepolicy.cil"

# Push it on the device
adb push system_ext_sepolicy.cil /system_ext/etc/selinux/system_ext_sepolicy.cil
echo "[*] Pushing to device successfully" 

# Regenerate a new sha256 for the policiy to triggere policy compilation afer reboot
adb shell 'echo miu | sha256sum > /system_ext/etc/selinux/system_ext_sepolicy_and_mapping.sha256 '
echo "[*] Updating the hash"

# Reboot to take effect
adb unroot
adb reboot
echo "Done"
