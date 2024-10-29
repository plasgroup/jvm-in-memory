package application.bst;

import framework.pim.BatchDispatcher;
import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import application.bst.NBodySystem;
import application.bst.NBodySystemProxy;
import application.bst.Body;
import application.bst.Sqrt;

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
        UPMEM.setPackageSearchPath("application.bst.");

        // Test DPUTreeNode
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Body.class);
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Sqrt.class);
        NBodySystemProxy nbs = (NBodySystemProxy) UPMEM.getInstance().createObject(0, NBodySystem.class);
        nbs.advance(0.01f);
        System.out.println(nbs.energy());
    }
}
