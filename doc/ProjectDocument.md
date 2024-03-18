# Project Document

## PART I. Project Structure and Introduction

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
-- lib
|  // dependency jars
-- 
|  //  
---------------------------------------------------------------------------------------------------
```



### I.2 Introduction

+ **Objectives:** Simply the development of a processing-in-memory instance - UPMEM。
+ **System Components** [for more details, see the correspondent documents]
  + **Java framework**
  + **In memory JVM**

+ **Defectiveness in PIM Development**

> [!CAUTION]
>
> This part is in preparing

+ Computational Site Transition (CST)
+ Computation Kernel Transition (CKT)
+ Introduction of Task Structure (ITS)
+ Data Location Management (DLM)
+ Data Structure Encoding (DSE)
+ Data Structure Decoding (DSD)
+ Broadcast Data Structure (BDS)
+ Distributed Array (DA)
+ Task Structure (TS)
+ Response Structure (RS)
+ Size Computation (SC)
+ Parallel Task Dispatching (PTD)
+ Parallel Task Appending (PTA)
+ Task Synchronization (TS)
+ Tasklet-specific Dispatching (TSD)
+ Task Ordering (TO)
+ Accessing by Scratchpad Memory (ASM)
+ Exception Handling (EXPT)
+ DPU Memory Allocation (DMemA)



### I.3  **Java framework**

+ Proxy type and its generator (incompleted)	
  + A proxy class of class A is created by extending the class A, and overriding all class A's methods that dispatch the method execution to a DPU


+ Distributed Collections
+ Primitives



### I.4  **DPU JVM**

+ An execution engine.
+ Use a switch loop-based interpretation of Java bytecode


