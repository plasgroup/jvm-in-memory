package application.transplant.index.search;

import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;

import java.io.*;

public class IndexSearchMain {
    public static void main(String[] args){
        IndexSearchDatabaseBuilder dm = new IndexSearchDatabaseBuilder();

        UPMEM.initialize(new UPMEMConfigurator().setThreadPerDPU(1).setDpuInUseCount(64).setUseSimulator(true));
        try {
            String basePath = (System.getProperty("user.dir"));

            dm.buildDictionary( basePath + "/src/application/transplant/index/search/database/dict.txt")
                    .buildIndexes("./database/files/");



//            IndexSearchDatabase indexSearchDatabase =
//                    dm.buildDictionary("./database/dict.txt")
//                        .buildIndexes("./database/files/")
//                        .buildDatabase();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

