package framework;

import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import framework.primitive.control.ControlPrimitives;

public class PrimitiveTest {
    public static void main(String[] args){
        UPMEM.initialize(new UPMEMConfigurator().setDpuInUseCount(64).setThreadPerDPU(1));

        ControlPrimitives.dispatchFunction(0,
                (a, b) -> (int) a + (int) b,
                1, 2);

        ControlPrimitives.dispatchFunction(0,
                (a, b, c) -> (int)a + (int) b - (int) c,
                5,6,7
        );
    }
}