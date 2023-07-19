package pim.dpu;

public class VirtualTableItem {
    int methodReferenceAddress;
    int classReferenceAddress;
    String className;
    String descriptor;
    public VirtualTableItem(String className, String descriptor){
        this.className = className;
        this.descriptor = descriptor;
    }
}
