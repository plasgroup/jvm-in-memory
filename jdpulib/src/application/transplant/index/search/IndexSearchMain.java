package application.transplant.index.search;

import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import transplant.index.search.pojo.Word;

import java.io.*;
import java.util.List;

public class IndexSearchMain {
    public static void main(String[] args) throws ClassNotFoundException {
        IndexSearchDatabaseBuilder dm = new IndexSearchDatabaseBuilder();

        UPMEM.initialize(new UPMEMConfigurator().setThreadPerDPU(1).setDpuInUseCount(64)
                .setUseSimulator(true).setPackageSearchPath("application.transplant.index.search.")
                .setUseAllowSet(true).addClassesAllow(
                        "java.lang.Object", "java.util.HashTable",
                        "application.transplant.index.search.IndexTable",
                        "application.transplant.index.search.Document",
                        "java.util.ArrayList",
                        "application.transplant.index.search.Searcher",
                        "application.transplant.index.search.pojo.SearchResult")
                .setEnableProfilingRPCDataMovement(true));

        int requestCount = 1000000;

        try {
            String basePath = (System.getProperty("user.dir"));

            IndexSearchDatabase indexSearchDatabase =
                    dm.initialize()
                            .buildDictionary(basePath + "/src/application/transplant/index/search/database/dict.txt")
                            .buildIndexes(basePath + "/src/application/transplant/index/search/database/files", 200)
                            .buildDatabase();

            System.out.println("build database Finished");


            File requestFile =
                    new File(basePath + "/src/application/transplant/index/search/database/request10000000.txt");


            FileReader fileReader = new FileReader(requestFile);
            BufferedReader br = new BufferedReader(fileReader);
            String s;
            while((s = br.readLine()) != null){
                String[] words = s.split(" ");
                switch (words.length){
                    case 1:
                        indexSearchDatabase.search(words[0]);
                        break;
                    case 2:
                        indexSearchDatabase.search(words[0], words[1]);
                        break;
                    case 3:
                        indexSearchDatabase.search(words[0], words[1], words[2]);
                        break;
                    case 4:
                        indexSearchDatabase.search(words[0], words[1], words[2], words[3]);
                        break;
                    case 5:
                        indexSearchDatabase.search(words[0], words[1], words[2], words[3], words[4]);
                        break;
                }
            }
            br.close();
            fileReader.close();

            indexSearchDatabase.search("document", "in");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

