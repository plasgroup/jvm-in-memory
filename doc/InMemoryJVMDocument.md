# In-Memory JVM Documentation

## PART I. Project Structure and Files Introduction

### I.1 Project Structure

``` java
---------------------------------------------------------------------------------------------------
-- assembly
|  // current is not used. It a folder for develop JIT compiler experimentally.
-- ir
|  // intermediate language
-- utils
|
-- jdpulib
|  // java PIM framework project
-- core
|  // core src
-- 
---------------------------------------------------------------------------------------------------
```



## PART II. Compilation

``` bash
$ source <path-to-upmem-sdk>/upmem_env.sh
$ cd <path-to-dpu_jvm>
$ make dpuslave
```

+ The compilation will generate a binary file "**dpuslave**"
+ The Java framework need this binary file. This binary file can be placed to the root of framework project (./jdpulib).



 
