package application.transplant.sparsep.spmv.one.types.data.share;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.dpu.RPCHelper;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class ArrayListProxy extends ArrayList implements IDPUProxyObject {
    public Integer address;
    public Integer dpuID;
    @Override
    public int getAddr() {
        return address;
    }

    @Override
    public int getDpuID() {
        return dpuID;
    }

    public ArrayListProxy(int initialCapacity) {
        super(initialCapacity);
    }

    public ArrayListProxy() {
        super();
    }

    public ArrayListProxy(Collection c) {
        super(c);
    }

    @Override
    public void trimToSize() {
       RPCHelper.invokeMethod(dpuID,  address,"java/util/ArrayList", "trimToSize:()V");
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","ensureCapacity:()V", minCapacity);
    }

    @Override
    public int size() {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","size:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public boolean isEmpty() {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","isEmpty:()Z");
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public boolean contains(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","contains:()Z", o);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public int indexOf(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","indexOf:()I", o);
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public int lastIndexOf(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","lastIndexOf:(Ljava/lang/Object;)I", o);
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public Object clone() {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","clone:()Ljava/lang/Object;");
        return RPCHelper.getAReturnValue(dpuID, ArrayListProxy.class);
    }

    @Override
    public Object[] toArray() {
//        RPCHelper.invokeMethod(dpuID, address, "ArrayList","clone():Ljava/lang/Object;");
//        return RPCHelper.ArrayHandlerFromAddress(RPCHelper.getAReturnValue(dpuID));
        throw new RuntimeException("Unsupport Operation");
    }

    @Override
    public Object[] toArray(Object[] a) {
        throw new RuntimeException("Unsupport Operation");
    }

    @Override
    public Object get(int index) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","get:(I)Ljava/lang/Object;", index);
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public Object set(int index, Object element) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","set:(ILjava/lang/Object;)Ljava/lang/Object;", index, element);
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public boolean add(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","add:(Ljava/lang/Object;)Z", o);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public void add(int index, Object element) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","add:(ILjava/lang/Object;)V", index, element);
    }

    @Override
    public Object remove(int index) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","remove:(I)Ljava/lang/Object;", index);
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public boolean equals(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","equals:(Ljava/lang/Object;)Z", o);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public int hashCode() {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","hashCode:()I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public boolean remove(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","remove:(Ljava/lang/Object;)Z", o);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public void clear() {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","clear:()V");
    }

    @Override
    public boolean addAll(Collection c) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","addAll:(Ljava.util.Collection;)Z", c);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","addAll:(ILjava.util.Collection;)Z", index, c);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","removeRange:(II)V", fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection c) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","removeAll:(Ljava.util.Collection;)Z", c);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public boolean retainAll(Collection c) {
        RPCHelper.invokeMethod(dpuID, address, "java/util/ArrayList","retainAll:(Ljava.util.Collection;)Z", c);
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public ListIterator listIterator(int index) {
//        RPCHelper.invokeMethod(dpuID, address, "ArrayList","listIterator:(I)Z");
//        return RPCHelper.getAReturnValue(dpuID);
        throw new RuntimeException("Unsupport Operation");
    }

    @Override
    public ListIterator listIterator() {
        throw new RuntimeException("Unsupport Operation");
    }

    @Override
    public Iterator iterator() {
        return super.iterator();
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public void forEach(Consumer action) {
        super.forEach(action);
    }

    @Override
    public Spliterator spliterator() {
        return super.spliterator();
    }

    @Override
    public boolean removeIf(Predicate filter) {
        return super.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator operator) {
        super.replaceAll(operator);
    }

    @Override
    public void sort(Comparator c) {
        super.sort(c);
    }

    @Override
    public boolean containsAll(Collection c) {
        return super.containsAll(c);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Object[] toArray(IntFunction generator) {
        return super.toArray(generator);
    }

    @Override
    public Stream stream() {
        return super.stream();
    }

    @Override
    public Stream parallelStream() {
        return super.parallelStream();
    }
}