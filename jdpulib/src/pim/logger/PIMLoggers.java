package pim.logger;

public class PIMLoggers {
    public static Logger pimProxy = AppendLogger("pim:proxy");
    public static Logger cpuTreeNodeLogger = AppendLogger("tree:cpu-node");
    public static Logger bstTestLogger = AppendLogger("bst:testing");
    public static Logger bstBuildingLogger = AppendLogger("bst:building");
    static{
        disableLoggers( "tree:cpu-node", "bst:testing", "bst:building");
        Logger.disableAllBeginWith("pim");
    }

    private static Logger AppendLogger(String loggerName){
        Logger logger = Logger.getLogger(loggerName);
        return logger;
    }

    public static void disableLoggers(String... names){
        for(String name : names){
            Logger.getLogger(name).setEnable(false);
        }
    }
}
