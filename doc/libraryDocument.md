# Library Documentation

# I. Proxy Class

> [!NOTE]
>
> Currently, provides a generator for automatically generating proxy class. But it is untested.





## Create A Proxy Class

``` Java
class A {
    public T1 f1(PT11 p1, PT12 p2, p13 p3, ...){...}
    public T2 f2(){...}
    ...
    public Tn fn(){...}
}

class AProxy extends A implements IDPUProxyObject{
    int address;
    int dpuID;
    // Override all overridable methods
    @Override
    T1 f1(PT11 p1, PT12 p2, p13 p3, ...){
        ProxyHelper.invokeMethod(A.class, "f1(....):LT1;", p1, p2, p3);
    }
    
}
```



### ProxyHelper.invokeMethod()

+ **1st** parameter is a class.
+ **2nd** parameter is a descriptor of the method.
  + For more information, please refer to the JVM specification.
    + https://docs.oracle.com/javase/specs/jvms/se8/jvms8.pdf section 4.4.

+ Rest parameters are arguments for method execution.

+ It may need to notice the **2nd** parameter, as methods with the same descriptor may exist in the inherence chain. We need specific the class' should the library call.



