package simulator;

import java.io.Serializable;

public class JVMSimulatorResult implements Serializable {
    public int taskID;
    public Object value;
    public JVMSimulatorResult(int tid, Object val){
        this.taskID = tid;
        this.value = val;
    }
}
