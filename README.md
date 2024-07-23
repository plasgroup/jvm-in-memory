# 1. Prepare Environment

## 1.1 UPMEM SDK


実験のソースコードは `jdpulib/src/Main`

実験レポジトリ https://github.com/plasgroup/paper-jssst2023-huang/tree/main

共有ライブラリのビルドの指示は従わなきゃいけないが、dpu.jar は jvm-in-memory 直下にあるものをコピーしてしまえばいい
実行時に共有ライブラリロードのエラーが出るときは、`export LD_LIBRARY_PATH=/home/ichinose/jvm-in-memory/upmem-2023.1.0-Linux-x86_64/lib:$LD_LIBRARY_PATH`
などとして共有ライブラリを探すパスを設定する。Java がネイティブライブラリを探す場所は -Djava.library.path= で指定するが、共有ライブラリが依存する別のライブラリは LD_LIBRARY_PATH を見るらしい

1. 改造済み DPU ライブラリの upmem_env.sh を実行
2. jdpulib の中で ./compile-source-code.sh してから ../evaluation/bst-performance-eval.sh
3. key_value ペアを作るには、java -cp generate-key-values.jar Main してから ../evaluation/bst-serialize.sh をする。大きさが 10000000で固定なので head -n とかで適切な大きさに切ってから jdpulib にコピーする

### 実行の準備
- `wget http://sdk-releases.upmem.com/2023.1.0/debian_10/upmem-2023.1.0-Linux-x86_64.tar.gz`
- `wget http://sdk-releases.upmem.com/2023.1.0/upmem-src-2023.1.0-Linux-x86_64.tar.gz`
- `tar xzvf upmem-2023.1.0-Linux-x86_64.tar.gz`
- `tar xzvf upmem-src-2023.1.0-Linux-x86_64.tar.gz`
- `src/backends/api/src/api/dpu_jni.c` に対する指示をやる（ビルドと共有ライブラリの入れ替え）
- `source upmem-2023.1.0-Linux-x86_64/upmem_env.sh`
- `export LD_LIBRARY_PATH=/home/ichinose/jvm-in-memory/upmem-2023.1.0-Linux-x86_64/lib:$LD_LIBRARY_PATH`

### 解読
- bst-serialize
  - TYPE=PIM, PERF_MODE 指定なし
  - main() で BSTTester.evaluatePIMBST が呼ばれる
  - evaluatePIMBST の中で、buildFromSerializedData がオフなら BSTBuilder.buildPIMTree が呼ばれる
  - buildPIMTree の中では、クラスのロードをして TreeWriter.convertCPUTreeToPIMTree を呼ぶ
  - TreeWriter.convertCPUTreeToPIMTree の中で DPU の中に木を作る処理をしている
  - TreeWriter.writeSubTreeBytes() でヒープバイト配列に DPU 用の木を書き込んでいる
- bst-performance-evaluation
  - Main.performanceEvaluation()

### 改造
- Main.main()
- Main.performanceEvaluation()
- BSTBuilder.buildPIMTreeDirect()
- TreeWriter.convertCPUTreeToPIMTreeDirect()
- TreeWriter.createSubTree()

## 1.2 Compile Extended UPMEM Java Library

1.  Download UPMEM SDK Source code
2. Insert new JNI code in the file `src/backends/api/src/api/dpu_jni.c`

> It is a JNI method that can transfer data to a given DPU variable (name is given by `jsymbol`) from a specific offset of this variable's memory.

``` c

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
        printf("* transit data to offset %d, symbol = %s\n",
         offset[each_buffer], symbol);
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

3. In the root of `./backend`

   + `cmake .`
   + `make`

   > Some errors may happen

4. Replace the generated `.so` files and `.so.xx` files to UPMEM SDK

5. In the java library of UPMEM's `Dpu.java`

   Insert

   ``` java
   public void copy(String dpuDstSymbol, byte[] dstBuffer, int offset) throws DpuException {
           this.copy(dpuDstSymbol, new byte[][]{dstBuffer}, new int[]{offset});
   }
   ```

6. compiler the java project and get a `Dpu.jar`

7. copy the `Dpu.jar` to `./lib/` of this repository



## 1.3 Compile DPU JVM

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





