package simulator;

public class PIMRemoteJVMConfiguration {
    public static int threadCount = 1;
    public static int JVMCount = 16;

    final public static int heapSize = 48 * 1024 * 1024;
    final public static int maxParameterSpaceSize = 24 * 4 * 1024;
    final public static int maxMetaspaceSize = 12 * 1024 * 1024;

}
