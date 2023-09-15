package pim.dpu.java_strut;

public class VirtualTableItem {
    public int methodReferenceAddress;
    public int classReferenceAddress;
    public String className;
    public String descriptor;
    public VirtualTableItem(String className, String descriptor){
        this.className = className;
        this.descriptor = descriptor;
    }
}
