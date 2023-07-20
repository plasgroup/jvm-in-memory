package pim.logger;

public class PIMLoggers {
    public static Logger pimProxy = appendLogger("pim:proxy");
    public static Logger cpuTreeNodeLogger = appendLogger("tree:cpu-node");
    public static Logger bstTestLogger = appendLogger("bst:testing");
    public static Logger bstBuildingLogger = appendLogger("bst:building");
    public static Logger classfileAnalyzerLogger = appendLogger("pim:class-file-analyzer");
    public static Logger pimCacheLogger = appendLogger("pim:cache");
    public static Logger pimManagerLogger = appendLogger("pim:pim-manager");
    public static Logger dpuManagerLogger = appendLogger("pim:dpu-manager");
    public static Logger gcLogger = appendLogger("pim:gc");

    public static Logger classfileLogger = appendLogger("pim:classfile");
    static{
        disableLoggers( "tree:cpu-node", "bst:testing", "bst:building",
                "pim:class-file-analyzer",   "pim:cache", "pim:pim-manager", "pim:dpu-manager",
                "pim:gc", "pim:classfile"
                );
        Logger.disableAllBeginWith("pim");
    }

    private static Logger appendLogger(String loggerName){
        Logger logger = Logger.getLogger(loggerName);
        return logger;
    }

    public static void disableLoggers(String... names){
        for(String name : names){
            Logger.getLogger(name).setEnable(false);
        }
    }
}
