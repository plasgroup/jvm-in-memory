# Project Document

## 1. Introduction

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





## 2.  **Java framework**

+ Proxy type and its generator (incompleted)	
  + A proxy class of class A is created by extends the class A, and override all class A's method that dispatch the method execution to a DPU


+ Distributed Collections
+ Primitives



## 3.  **DPU JVM**

+ An execution engine.
+ Use a switch loop-based interpretation of Java bytecode



