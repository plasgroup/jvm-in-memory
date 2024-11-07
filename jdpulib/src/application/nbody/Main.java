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
import java.util.concurrent.TimeUnit;

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

        // Create proxy object
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Body.class);
        UPMEM.getInstance().getDPUManager(0).dpuClassFileManager.loadClassToDPU(Sqrt.class);
        NBodySystemProxy nbs = (NBodySystemProxy) UPMEM.getInstance().createObject(0, NBodySystem.class);
        System.out.println("[INFO] Start DPU advance");

        // Run the N-Body simulation (executions time is measured)
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            nbs.advance(0.01f);
        }
        long end = System.nanoTime();

        long durationInMicros = TimeUnit.NANOSECONDS.toMicros(end - start);
        long durationInMillis = TimeUnit.NANOSECONDS.toMillis(end - start);
        System.out.println("[INFO] Duration of " + iterations + " iterations: " + durationInMicros + " us");
        System.out.println("[INFO] Duration of " + iterations + " iterations: " + durationInMillis + " ms");

        System.out.println("[INFO] End DPU advance");
        float energy = nbs.energy();
        System.out.println("\tResult of " + iterations + " iterations is: " + energy);
    }
}
