package pim;

public class ExperimentConfigurator {
    public static int totalNodeCount = 10000000;
    public static int queryCount = 100000;
    public static int dpuInUse = 64;
    public static int cpuLayerCount = 18;
    public static String experimentType = "CPU";
    public static boolean serializeToFile = false;
    public static boolean buildFromSerializedData = false;
    public static boolean noSearch = false;
    public static String imagesPath = "./";
    public static boolean writeKeyValue = false;
    public static int writeKeyValueCount = 10000000;
}
