# 1. Prepare Environment

## 1.1 UPMEM SDK

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