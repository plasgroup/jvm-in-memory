package pim.logger;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;


public class Logger {
    static Map<String, Logger> loggerDictionary = new HashMap<>();

    int level;
    String name;
    boolean enable = true;

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Logger(String loggerName) {
        this.name = loggerName;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    public static void disableAllBeginWith(String name){
        for(String x : loggerDictionary.keySet()){
            if(x.length() >= name.length() && x.startsWith(name)){
                loggerDictionary.get(x).enable = false;
            }
        }
    }
    public void log(String message){
        log(name, message + "\n");
    }

    public void logf(String format, Object... params){
        logf(name, format, params);
    }

    public void log(int level, String message){
        log(level, name, message);
    }

    public void logf(int level, String format, Object... params){
        logf(level, name, format, params);
    }
    public static void log(int level, String loggerName, String message){
        Logger logger = loggerDictionary.get(loggerName);
        if(logger == null){
            logger = getLogger(loggerName);
            loggerDictionary.put(loggerName, logger);
        }
        if(!logger.enable) return;
        if(level >= logger.level){
            System.out.println(message);
        }
    }
    public static void log(int level, String loggerName, Object message){
        Logger logger = loggerDictionary.get(loggerName);
        if(logger == null){
            logger = getLogger(loggerName);
            loggerDictionary.put(loggerName, logger);
        }
        if(!logger.enable) return;
        if(level >= logger.level){
            System.out.println(message);
        }
    }
    public static void logf(int level, String loggerName, String format, Object... params){
        log(level, loggerName, String.format(format, params));
    }

    public static void log(String loggerName, String message){
        log(0, loggerName, message);
    }
    public static void logf(String loggerName, String format, Object... params){
        logf(0, loggerName, format, params);
    }

    public static Logger getLogger(String loggerName){
        Logger logger = loggerDictionary.get(loggerName);
        if(logger != null){
            return logger;
        }
        logger = new Logger(loggerName);
        loggerDictionary.put(loggerName, logger);
        return logger;
    }


    public void logln(String s) {
        log(s + "\n");
    }

    public void logln(Object obj) {
        log(obj + "\n");
    }
    public void logln() {
        logln("");
    }
}
