package pim.dpu;

public class VirtualTableItem {
    int methodReferenceAddress;
    String className;
    String descriptor;
    public VirtualTableItem(String className, String descriptor){
        this.className = className;
        this.descriptor = descriptor;
    }
}
