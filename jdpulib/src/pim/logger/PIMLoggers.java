package pim.logger;

public class PIMLoggers {
    public static Logger pimProxy = AppendLogger("pim:proxy");
    static{
        disableLoggers("pim:proxy");
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
