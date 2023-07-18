#include <jni.h>
#include <string>
#include <android/log.h>
#include <pthread.h>
#include <unistd.h>

extern "C" int handle_command(struct cmd_struct *command, int argc, const char **argv);
extern "C" struct cmd_struct kvm_commands[];
extern "C" void kvm__set_dir(const char *fmt, ...);

struct vm_args_s {
    int argc;
    void *argv;
};

static int pfd[2];
static pthread_t loggingThread, vmThread;
static const char *LOG_TAG = "[native]";
static struct vm_args_s vm_args;

static void *startVmThread(void *args)
{
    handle_command(kvm_commands, vm_args.argc, (const char **)vm_args.argv);
}

extern "C"
JNIEXPORT int JNICALL
Java_com_example_kvmpro_MainActivity_startVMJni(JNIEnv *env, jobject thiz, jobject vm) {
    static const char *args[80];
    int argc = 0;
    jclass cls = env->GetObjectClass(vm);

    args[argc++] = "run";

    // Extract the disk name:
    args[argc++] = "-d";
    jfieldID diskFid = env->GetFieldID(cls, "diskFileName", "Ljava/lang/String;");
    jstring diskImageS = (jstring)env->GetObjectField(vm, diskFid);
    args[argc++] = env->GetStringUTFChars(diskImageS, NULL);

    // Add memory attribute
//    args[argc++] = "-m";
//    args[argc++] = "128";

    // Add memory attribute
    args[argc++] = "-c";
    args[argc++] = "1";

    // Extract the kernel image name:
    args[argc++] = "-k";
    jfieldID kernelImageFid = env->GetFieldID(cls, "kernelImageFilename", "Ljava/lang/String;");
    jstring kernelImageS = (jstring)env->GetObjectField(vm, kernelImageFid);
    args[argc++] = env->GetStringUTFChars(kernelImageS, NULL);

    jfieldID homeFid = env->GetFieldID(cls, "homePath", "Ljava/lang/String;");
    jstring homeS = (jstring)env->GetObjectField(vm, homeFid);

    vm_args.argc = argc;
    vm_args.argv = args;

    kvm__set_dir("%s", env->GetStringUTFChars(homeS, NULL));
    if (pthread_create(&vmThread, 0, startVmThread, 0) == -1) {
        return -1;
    }

    pthread_detach(vmThread);
    return 0;
}

static void *loggingFunction(void*)
{
    ssize_t readSize;
    char buf[128];

    while((readSize = read(pfd[0], buf, sizeof buf - 1)) > 0) {
        if(buf[readSize - 1] == '\n') {
            --readSize;
        }

        buf[readSize] = 0;  // add null-terminator

        __android_log_write(ANDROID_LOG_DEBUG, LOG_TAG, buf); // Set any log level you want
    }

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_kvmpro_MainActivity_runLoggingThread(JNIEnv *env, jobject thiz)
{
    setvbuf(stdout, 0, _IOLBF, 0); // make stdout line-buffered
    setvbuf(stderr, 0, _IONBF, 0); // make stderr unbuffered

    /* create the pipe and redirect stdout and stderr */
    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    /* spawn the logging thread */
    if(pthread_create(&loggingThread, 0, loggingFunction, 0) == -1) {
        return -1;
    }

    pthread_detach(loggingThread);
    return 0;
}