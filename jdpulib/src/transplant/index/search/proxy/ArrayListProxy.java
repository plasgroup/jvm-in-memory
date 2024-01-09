package transplant.index.search.proxy;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.ProxyHelper;
import framework.pim.dpu.RPCHelper;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

class ArrayListProxy extends ArrayList implements IDPUProxyObject {
    int address;
    int dpuID;
    @Override
    public int getAddr() {
        return 0;
    }

    @Override
    public int getDpuID() {
        return 0;
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
       RPCHelper.invokeMethod(dpuID,  address,"ArrayList", "trimToSize():V");
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","ensureCapacity():V");
    }

    @Override
    public int size() {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","size():I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public boolean isEmpty() {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","isEmpty():Z");
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public boolean contains(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","contains():Z");
        return RPCHelper.getBooleanReturnValue(dpuID);
    }

    @Override
    public int indexOf(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","indexOf():I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public int lastIndexOf(Object o) {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","lastIndexOf(Ljava/lang/Object;):I");
        return RPCHelper.getIReturnValue(dpuID);
    }

    @Override
    public Object clone() {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","clone():Ljava/lang/Object;");
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public Object[] toArray() {
//        ProxyHelper.invokeMethod(dpuID, address, "ArrayList","clone():Ljava/lang/Object;");
//        return ProxyHelper.getAReturnValue(dpuID);
        throw new RuntimeException();
    }

    @Override
    public Object[] toArray(Object[] a) {
        throw new RuntimeException();
    }

    @Override
    public Object get(int index) {
        RPCHelper.invokeMethod(dpuID, address, "ArrayList","get(I):Ljava/lang/Object;");
        return RPCHelper.getAReturnValue(dpuID);
    }

    @Override
    public Object set(int index, Object element) {
        return super.set(index, element);
    }

    @Override
    public boolean add(Object o) {
        return super.add(o);
    }

    @Override
    public void add(int index, Object element) {
        super.add(index, element);
    }

    @Override
    public Object remove(int index) {
        return super.remove(index);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public boolean addAll(Collection c) {
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return super.addAll(index, c);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection c) {
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        return super.retainAll(c);
    }

    @Override
    public ListIterator listIterator(int index) {
        return super.listIterator(index);
    }

    @Override
    public ListIterator listIterator() {
        return super.listIterator();
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