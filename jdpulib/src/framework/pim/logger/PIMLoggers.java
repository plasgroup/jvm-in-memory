package framework.pim.logger;

public class PIMLoggers {
    public static Logger pimProxy = appendLogger("framework.pim:proxy");
    public static Logger cpuTreeNodeLogger = appendLogger("tree:cpu-node");
    public static Logger bstTestLogger = appendLogger("bst:testing");
    public static Logger bstBuildingLogger = appendLogger("bst:building");
    public static Logger classfileAnalyzerLogger = appendLogger("framework.pim:class-file-analyzer");
    public static Logger pimCacheLogger = appendLogger("framework.pim:cache");
    public static Logger pimManagerLogger = appendLogger("framework.pim:framework.pim-manager");
    public static Logger dpuManagerLogger = appendLogger("framework.pim:dpu-manager");
    public static Logger gcLogger = appendLogger("framework.pim:gc");
    public static Logger batchDispatchLogger = appendLogger("framework.pim:batch-dispatch");
    public static Logger classfileLogger = appendLogger("framework.pim:classfile");
    public static Logger jvmSimulatorLogger = appendLogger("simulator:jvm");
    public static Logger pimTreeLogger = appendLogger("pimtree:appmain");

    static {
        // disableLoggers("tree:cpu-node", "bst:testing", "bst:building",
        // "framework.pim:class-file-analyzer", "framework.pim:cache",
        // "framework.pim:framework.pim-manager",
        // "framework.pim:dpu-manager",
        // "framework.pim:gc", "framework.pim:classfile" , "simulator:jvm");
        // Logger.disableAllBeginWith("pim");
    }

    private static Logger appendLogger(String loggerName) {
        Logger logger = Logger.getLogger(loggerName);
        return logger;
    }

    public static void disableLoggers(String... names) {
        for (String name : names) {
            Logger.getLogger(name).setEnable(false);
        }
    }
}
