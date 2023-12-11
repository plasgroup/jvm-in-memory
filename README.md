# Part I Documents

## I.1 Documentations

1. [Project Documents](./doc/ProjectDocument.md)
2. [Library Documents](./doc/libraryDocument.md)

3. [In Memory JVM Documents](./doc/InMemoryJVMDocument.md)



## I.2 Repository Structure

``` java
---------------------------------------------------------------------------------------------------
-- classloader
|  // A java class parser write in C (It is not used in experiment. But may be useful as an util)
-- doc
|  // Documents
-- dpu_jvm
|  // in-memory jvm project
-- jdpulib
|  // java PIM framework project
-- lib
|  // libs that required by java framework
---------------------------------------------------------------------------------------------------


```



# Part II Compilation

> [!CAUTION]
> **This part is in preparing.... More specific version can currently refer to repository **[paper-jssst2023-huang](https://github.com/plasgroup/paper-jssst2023-huang)



## II.1 Prepare Environment

### II.1.1 Compile Extended UPMEM Java Library

1. Download UPMEM SDK (**2023.1.0 version**)

   > It may need change the url. It may refer to https://sdk.upmem.com/ find the version that fit to the target system.

   In `workspace`

   ``` bash
   #$ curl http://sdk-releases.upmem.com/2023.1.0/debian_10/upmem-2023.1.0-Linux-x86_64.tar.gz -o upmem-2023.1.0-Linux-x86_64.tar.gz
   
   # ubuntu 18.04
   $ curl http://sdk-releases.upmem.com/2023.1.0/ubuntu_18.04/upmem-2023.1.0-Linux-x86_64.tar.gz -o ~/upmem-2023.1.0-Linux-x86_64.tar.gz
   $ cd ~
   $ tar -xvf ~/upmem-2023.1.0-Linux-x86_64.tar.gz
   # Some problems may happen if upmem not unzip to  ~/
   ```

2. Download UPMEM src (**2023.1.0 version**)

   > It may need change the url. It can refer to https://sdk.upmem.com/ find the version that fit to the target system.

   In workspace

   ``` bash
   $ curl http://sdk-releases.upmem.com/2023.1.0/upmem-src-2023.1.0-Linux-x86_64.tar.gz -o ~/upmem-src-2023.1.0-Linux-x86_64.tar.gz
   $ cd ~
   $ tar -xvf ~/upmem-src-2023.1.0-Linux-x86_64.tar.gz
   ```


3. Insert new code in the file `~/upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c`

   ``` bash
   $ vi ~/upmem-2023.1.0-Linux-x86_64/src/backends/api/src/api/dpu_jni.c
   ...
   ```

> It is a JNI method that can transfer data to a given DPU variable (name is given by `jsymbol`) with a specific offset of this variable's memory.

+ Place it **523 th** line of the `dpu_jni.c` file (523 line should be an empty line)

```
JNIEXPORT void JNICALL
Java_com_upmem_dpu_NativeDpuSet_copy__Ljava_lang_String_2_3_3B_3IZZ(JNIEnv *env,
    jobject this,
    jstring jsymbol,
    jobjectArray jarrays, 
    jintArray offsets, 
    jboolean to_dpu,
    jboolean async){
    //dpu-related
    struct dpu_set_t set = build_native_set(env, this);
    dpu_xfer_t xfer = to_dpu ? DPU_XFER_TO_DPU : DPU_XFER_FROM_DPU;
    dpu_xfer_flags_t flags = async ? DPU_XFER_ASYNC : DPU_XFER_DEFAULT;
    struct jarray_release_ctx *ctx = NULL;

    // parameter process
    uint32_t nr_buffers = (*env)->GetArrayLength(env, jarrays);
    uint32_t nr_offsets = (*env)->GetArrayLength(env, offsets);
    jbyteArray arrays[nr_buffers];
    jbyte *buffers[nr_buffers];
    jint* offset;
    uint32_t nr_dpus;

    // get symbol string
    const char *symbol = (*env)->GetStringUTFChars(env, jsymbol, 0);
    THROW_ON_ERROR_L(dpu_get_nr_dpus(set, &nr_dpus), end);

    // check whether buffer count equal to dpuset length
    if (nr_buffers != nr_dpus || nr_offsets != nr_dpus) {
        throw_dpu_exception(env, "the number of buffers should match the numer of DPUs");
        goto end;
    }

    // read bytes
    for (uint32_t each_buffer = 0; each_buffer < nr_buffers; ++each_buffer) {
        jbyteArray jarray = (*env)->GetObjectArrayElement(env, jarrays, each_buffer);
        jbyte *jbuffer = (*env)->GetByteArrayElements(env, jarray, 0);
       
        arrays[each_buffer] = jarray;
        buffers[each_buffer] = jbuffer;
        offset =  (*env)->GetIntArrayElements(env, offsets, 0);
    }

    bool length_initialized = false;
    jsize length;

    struct dpu_set_t dpu;
    uint32_t each_dpu;
    DPU_FOREACH (set, dpu, each_dpu) {
        jbyteArray jarray = arrays[each_dpu];
        jbyte *jbuffer = buffers[each_dpu];
        jsize buffer_length = (*env)->GetArrayLength(env, jarray);

        if (!length_initialized) {
            length_initialized = true;
            length = buffer_length;
        } else if (length != buffer_length) {
            throw_dpu_exception(env, "all buffers should have the same length");
            return;
        }

        THROW_ON_ERROR_L(dpu_prepare_xfer(dpu, jbuffer), error);
    }

    if (length_initialized) {
        for (each_dpu = 0; each_dpu < nr_dpus; each_dpu++){
            THROW_ON_ERROR_L(dpu_push_xfer(set, xfer, symbol, offset[each_dpu], length, flags), error);
        }
        
    }

    if (async) {
        THROW_ON_ERROR_L(prepare_release_callback(env, arrays, buffers, nr_buffers, &ctx), end);
        THROW_ON_ERROR_L(
            dpu_callback(
                set, callback_release_jarray, ctx, DPU_CALLBACK_ASYNC | DPU_CALLBACK_NONBLOCKING | DPU_CALLBACK_SINGLE_CALL),
            end);
    } else {
    error:
        for (uint32_t each_buffer = 0; each_buffer < nr_buffers; ++each_buffer) {
            (*env)->ReleaseByteArrayElements(env, arrays[each_buffer], buffers[each_buffer], 0);
        }
    }

end:
    free_release_ctx(ctx);
    (*env)->ReleaseStringUTFChars(env, jsymbol, symbol);

    }
```

1. Make UPMEM library

   ``` bash
   $ cd ~/upmem-2023.1.0-Linux-x86_64/src/backends
   $ cmake .
   $ make
   ```

   > Some errors may happen according the platform. But in UPMEM, it should success without any errors.
   >
   > It may need some effort to solve errors, if in other hardware platform.

2. Copy the generated `.so` files and `.so.xx` files to UPMEM SDK

   ``` bash
   $ cp api/libdpu.so ~/upmem-2023.1.0-Linux-x86_64/lib/libdpu.so
   $ cp api/libdpu.so.0.0 ~/upmem-2023.1.0-Linux-x86_64/lib/libdpu.so.0.0
   $ cp api/libdpu.so.0.0 ~/upmem-2023.1.0-Linux-x86_64/lib/libdpu.so.2023.1
   $ cp api/libdpujni.so ~/upmem-2023.1.0-Linux-x86_64/lib/libdpujni.so
   $ cp api/libdpujni.so.0.0 ~/upmem-2023.1.0-Linux-x86_64/lib/libdpujni.so.0.0
   $ cp api/libdpujni.so.0.0 ~/upmem-2023.1.0-Linux-x86_64/lib/libdpujni.so.2023.1
   
   ```

3. Compile DPU Java API Library (This step could be ignored; we provide a pre-compiled `Dpu.jar` file in the root of `jvm-in-memory`)

   + In the file `~/upmem-2023.1.0-Linux-x86_64/src/backends/java/src/main/java/com/upmem/dpu/Dpu.java`, 

   Insert follow code

   ```java
   public void copy(String dpuDstSymbol, byte[] dstBuffer, int offset) throws DpuException {
           this.copy(dpuDstSymbol, new byte[][]{dstBuffer}, new int[]{offset});
   }
   ```

   + In the file `~/upmem-2023.1.0-Linux-x86_64/src/backends/java/src/main/java/com/upmem/dpu/DpuSet.java`, 

   Insert follow code

   ``` java
   void copy(String dpuDstSymbol, byte[][] dstBuffer, int[] offset) throws DpuException;
   ```

   + In the file `~/upmem-2023.1.0-Linux-x86_64/src/backends/java/src/main/java/com/upmem/dpu/DpuSetBase.java`

   Insert follow code

   ``` java
   public void copy(String dpuDstSymbol, byte[][] dstBuffer, int[] offset) throws DpuException{
        this.set.copy(dpuDstSymbol, dstBuffer, offset);
   }
   ```

   + In the file `~/upmem-2023.1.0-Linux-x86_64/src/backends/java/src/main/java/com/upmem/dpu/NativeDpuSet.java`

   Insert follow code

   ``` java
   native void copy(String dpuDstSymbol, byte[][] dstBuffer, int[] offset) throws DpuException;
   ```

   compile

   ``` bash
   $ cd ~/upmem-2023.1.0-Linux-x86_64/src/backends/java/src/main/java/com/upmem/dpu/
   $ javac *.java
   $ jar cf dpu.jar *.class 
   ```

   

4. compiler the UPMEM java API project and get a `dpu.jar`, and copy to evaluation folder

   ``` bash
   # copy to jvm-in-memory
   $ mkdir -p <path-to-workspace>/jvm-in-memory/evaluation
   $ cp <path-to-dpu.jar> ~/upmem-2023.1.0-Linux-x86_64/share/java/dpu.jar
   
   # It can also use the dpu.jar provided in the repository
   $ cp <path-to-jvm-in-memory>/dpu.jar ~/upmem-2023.1.0-Linux-x86_64/share/java/dpu.jar
   ```

   

## II.1.3 Compile DPU JVM

1. Set up the environment for UPMEM

   - This can be done by `source ~/upmem-2023.1.0-Linux-x86_64/upmem_env.sh`

   - A more convenient way is add these codes to the `~/.bashrc`

     ```
     export UPMEM_HOME="<path-to-upmem-sdk>"
     export LD_LIBRARY_PATH="${UPMEM_HOME}/lib${LD_LIBRARY_PATH+:$LD_LIBRARY_PATH}"
     export PATH="${UPMEM_HOME}/bin:${PATH}"
     ```

2. It may need install clang and related libraries

   ``` bash
   $ sudo apt-get install clang
   $ sudo apt install libncurses5
   ```

3. In  `/jvm-in-memory/dpu_jvm`

   - `rm dpuslave; make dpuslave;`

4. Copy the `dpuslave` binary to the root of `evaluation`

``` bash
$ cp dpuslave <path-to-workspace>/jvm-in-memory/evaluation/
```

5. Compile the `Main.java` to `bst-latest.jar` (the jvm-in-memory folder also provided a pre-compiled jar. This step can be ignored if use the jar provided.)

   **Note that, JDK version should >= 17**

   ``` bash
   $ cp ~/upmem-2023.1.0-Linux-x86_64/share/java/dpu.jar <path-to-workspace>/jvm-in-memory/jdpulib/src
   $ javac -sourcepath ./src -d ./out2 ./src/Main.java -classpath ./src/dpu.jar
   $ cd out2
   $ cp ../src/META-INF/MANIFEST.MF ./
   $ cp <path-to-workspace>/jvm-in-memory/jdpulib/src/dpu.jar ./
   $ jar cvfm bst-latest.jar MANIFEST.MF . 
   
   ```

   

6. Copy generated jar to evaluation folder

   ``` bash
   $ cp <path-to-bst-latest-jar> <path-to-evaluation-folder>
   
   ## or use pre-compiled version
   $ cp <path-to-jvm-in-memory>/bst-latest.jar <path-to-jvm-in-memory>/evaluation
   ```



### II.1.4 Prepare JDK 17

+ In the UPMEM computer, the system version may cannot install openjdk17 by `apt-get` directly. It may need download from internet manually

  ``` bash
  $ curl https://download.java.net/java/GA/jdk17/0d483333a00540d886896bac774ff48b/35/GPL/openjdk-17_linux-x64_bin.tar.gz -o ~/jdk-17.0.1.tar.gz
  $ cd ~/
  $ tar -xvf jdk-17.0.1.tar.gz
  ```



## II.2 Compile DPU JVM

1. It need set up the environment for UPMEM

   + This can be done by `source <path-to-upmem-sdk>/upmem_env.sh`

   + A more convenient way is add these lines to the  `~/.bashrc`

     ``` bash
     export UPMEM_HOME="<path-to-upmem-sdk>"
     export LD_LIBRARY_PATH="${UPMEM_HOME}/lib${LD_LIBRARY_PATH+:$LD_LIBRARY_PATH}"
     export PATH="${UPMEM_HOME}/bin:${PATH}"
     ```

2. In the root of `dpu_jvm`
   + `rm dpuslave; make dpuslave;`
3. Move the `dpuslave` binary to the root of `jdpulib`



# PART III Some Future Directions



