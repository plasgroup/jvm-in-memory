package application.nbody;

import framework.pim.BatchDispatcher;
import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import application.nbody.NBodySystem;
import application.nbody.NBodySystemProxy;
import application.nbody.Body;
import application.nbody.Sqrt;

import framework.pim.dpu.classloader.ClassWriter;
import framework.primitive.control.ControlPrimitives;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import static framework.pim.ExperimentConfigurator.*;

/**
 * =======================================================================
 * This is a program for evaluating N-Body Simulation (NBody) application
 ** =======================================================================
 **/

public class Main {
    public static UPMEMConfigurator upmemConfigurator = new UPMEMConfigurator();

    public static void main(String[] args) throws RemoteException {
        // Get the number of iterations
        int iterations = Integer.parseInt(args[0]);

        // Configure UPMEM
        upmemConfigurator
                .setDpuInUseCount(1)
                .setThreadPerDPU(1)
                .setUseSimulator(false)
                .setEnableProfilingRPCDataMovement(false)
                // .setPrintStream(System.out);
                .setPrintStream(null);

        // UPMEM initialization
        UPMEM.initialize(upmemConfigurator);
        UPMEM.setPackageSearchPath("application.nbody.");

        // Test DPUTreeNode
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Body.class);
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Sqrt.class);
        NBodySystemProxy nbs = (NBodySystemProxy) UPMEM.getInstance().createObject(0, NBodySystem.class);
        System.out.println("[INFO] Start DPU advance");
        for (int i = 0; i < iterations; i++) {
            nbs.advance(0.01f);
        }
        System.out.println("[INFO] End DPU advance");
        float result = nbs.energy();
        System.out.println("\tResult of " + iterations + " iterations is: " + result);
    }
}
