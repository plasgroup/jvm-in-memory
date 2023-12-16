package application.transplant.index.search;

import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;

import java.io.*;
import java.util.List;

public class IndexSearchMain {
    public static void main(String[] args) throws ClassNotFoundException {
        IndexSearchDatabaseBuilder dm = new IndexSearchDatabaseBuilder();

        UPMEM.initialize(new UPMEMConfigurator().setThreadPerDPU(1).setDpuInUseCount(64).setUseSimulator(true).setPackageSearchPath("application.transplant.index.search."));
        try {
            String basePath = (System.getProperty("user.dir"));

            IndexSearchDatabase indexSearchDatabase = dm.initialize().buildDictionary(basePath + "/src/application/transplant/index/search/database/dict.txt")
                    .buildIndexes(basePath + "/src/application/transplant/index/search/database/files").buildDatabase();


            indexSearchDatabase.search("document", "in");

//            IndexSearchDatabase indexSearchDatabase =
//                    dm.buildDictionary("./database/dict.txt")
//                        .buildIndexes("./database/files/")
//                        .buildDatabase();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

