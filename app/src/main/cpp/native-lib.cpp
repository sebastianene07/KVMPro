#include <jni.h>
#include <string>
#include <android/log.h>
#include <pthread.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <termios.h>

extern "C" int handle_command(struct cmd_struct *command, int argc, const char **argv);
extern "C" struct cmd_struct kvm_commands[];
extern "C" void kvm__set_dir(const char *fmt, ...);

struct vm_args_s {
    int argc;
    void *argv;
};

static int pfd_vm_output[2];
static int pfd_vm_input[2];
static pthread_t loggingThread, vmThread;
static struct vm_args_s vm_args;

static void *startVmThread(void *args)
{
    int ret;

    ret = handle_command(kvm_commands, vm_args.argc, (const char **)vm_args.argv);
    if (ret < 0) {
        fprintf(stderr, "Error %d starting VM\n", ret);
    }

    return NULL;
}

extern "C"
JNIEXPORT int JNICALL
Java_com_example_kvmpro_VmRun_startVMJni(JNIEnv *env, jobject thiz, jobject vm) {
    static const char *args[80];
    int argc = 0;
    jclass cls = env->GetObjectClass(vm);

    args[argc++] = "run";

    // Extract the disk name:
    jfieldID diskFid = env->GetFieldID(cls, "diskFileName", "Ljava/lang/String;");
    jstring diskImageS = (jstring)env->GetObjectField(vm, diskFid);
    if (diskImageS) {
        args[argc++] = "-d";
        args[argc++] = env->GetStringUTFChars(diskImageS, NULL);
    }

    // Extract the initrd name:
    jfieldID initRdFid = env->GetFieldID(cls, "initrdFileName", "Ljava/lang/String;");
    jstring initrdImageS = (jstring)env->GetObjectField(vm, initRdFid);
    if (initrdImageS) {
        args[argc++] = "-i";
        args[argc++] = env->GetStringUTFChars(initrdImageS, NULL);
    }

    // Add memory attribute
    args[argc++] = "-m";
    args[argc++] = "1024";

    // Add cpu attribute
    args[argc++] = "-c";
    args[argc++] = "2";

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

struct vm_run_jobjects {
    JNIEnv *env;
    jobject thiz;
    jmethodID methodId;
};
JavaVM *g_vm;

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    g_vm = vm;
    return JNI_VERSION_1_6;
}

static void *loggingFunction(void *_arg) {
    ssize_t readSize = 0;
    char buf[128];
    char c;
    struct vm_run_jobjects *args = (struct vm_run_jobjects *) _arg;
    JNIEnv *env = args->env;
    jmethodID notifyConsoleUpdate = args->methodId;
    jint status;

    status = g_vm->AttachCurrentThread(&env, NULL);
    if (status == JNI_OK) {
        while (read(pfd_vm_output[0], &c, 1) > 0) {
            env->CallVoidMethod(args->thiz, notifyConsoleUpdate, c);
            env->ExceptionClear();
        }
    }

    env->DeleteGlobalRef(args->thiz);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_kvmpro_VmRun_runLoggingThread(JNIEnv *env, jobject thiz)
{
    static struct vm_run_jobjects vm_run_objs = {
            .env = env,
    };

    jclass cls = env->FindClass("com/example/kvmpro/VmRun");
    vm_run_objs.methodId = env->GetMethodID(cls,"notifyConsoleUpdate", "(C)V");
    vm_run_objs.thiz = env->NewGlobalRef(thiz);


    setvbuf(stdout, 0, _IOLBF, 0); // make stdout line-buffered
    setvbuf(stderr, 0, _IONBF, 0); // make stderr unbuffered

    /* create the pipe and redirect stdout and stderr */
    pipe(pfd_vm_output);
    dup2(pfd_vm_output[1], 1);
    dup2(pfd_vm_output[1], 2);

    pipe(pfd_vm_input);
    dup2(pfd_vm_input[0], 0);

    /* spawn the logging thread */
    if (pthread_create(&loggingThread, 0, loggingFunction, &vm_run_objs) == -1) {
        return -1;
    }

    pthread_detach(loggingThread);
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_kvmpro_VmRun_sendConsoleCharacters(JNIEnv *env, jobject thiz, jstring characters) {
    const char *consoleChar = env->GetStringUTFChars(characters, NULL);
    write(pfd_vm_input[1], consoleChar, strlen(consoleChar));
    env->ReleaseStringUTFChars(characters, consoleChar);
}