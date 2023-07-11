package pim.dpu;

public class VirtualTableItem {
    int methodReferenceAddress;
    String className;
    String description;
    public VirtualTableItem(String className, String description){
        this.className = className;
        this.description = description;
    }
}
