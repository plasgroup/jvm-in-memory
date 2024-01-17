package application.transplant.index.search;

import application.transplant.index.search.database.Preprocessing;
import framework.pim.ExperimentConfigurator;
import framework.pim.UPMEM;
import framework.pim.UPMEMConfigurator;
import transplant.index.search.pojo.Word;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class IndexSearchMain {
    public static void main(String[] args) throws ClassNotFoundException, IOException {
        boolean profileCPUDPUDataMovement = true;
        int docCount = 1000;
        int reqs = 1000;
        int dpuCount = 4;
        int threads = 1;
        String dictPath = "";
        String filesPath = "";
        String reqFilePath = "";
        boolean cpuOnly = false;
        if(args.length != 0){
            for(int i = 0; i < args.length; i++){
                String[] argItem = args[i].split("=");
                System.out.println(args[i]);
                System.out.println("parse arg:" + (argItem.length > 0 ?  argItem[0] : "")
                        + " = " + (argItem.length > 1 ? argItem[1] : ""));
                if ("DOC_COUNT".equals(argItem[0].strip().toUpperCase())){
                    docCount = Integer.parseInt(argItem[1].strip());
                } else if("TSK_N".equals(argItem[0].strip().toUpperCase())){
                    reqs = Integer.parseInt(argItem[1].strip());
                } else if("DPU_COUNT".equals(argItem[0].strip().toUpperCase())){
                    dpuCount = Integer.parseInt(argItem[1].strip());
                } else if("PROF_CPUDPU_DM".equals(argItem[0].strip().toUpperCase())){
                    profileCPUDPUDataMovement = true;
                } else if("THREADS".equals(argItem[0].strip().toUpperCase())){
                    threads = Integer.parseInt(argItem[1].strip());
                } else if("DICT_PATH".equals(argItem[0].strip().toUpperCase())){
                    dictPath = Arrays.stream(argItem).skip(1).reduce("", (a, b) -> a + b);
                    System.out.println("dict path = " + dictPath);

                } else if("FILE_PATH".equals(argItem[0].strip().toUpperCase())){
                    filesPath = Arrays.stream(argItem).skip(1).reduce("", (a, b) -> a + b);
                } else if("REQ_FILE".equals(argItem[0].strip().toUpperCase())){
                    reqFilePath =  Arrays.stream(argItem).skip(1).reduce("", (a, b) -> a + b);
                } else if("CPU_ONLY".equals(argItem[0].strip().toUpperCase())){
                    cpuOnly = true;
                }
            }
        }

        IndexSearchDatabaseBuilder dm = new IndexSearchDatabaseBuilder();
        System.out.println("init " + dpuCount + " DPUs with threads = " + threads);
            UPMEM.initialize(new UPMEMConfigurator().setThreadPerDPU(threads).setDpuInUseCount(dpuCount)
                    .setUseSimulator(true).setPackageSearchPath("application.transplant.index.search.")
                    .setUseAllowSet(true).addClassesAllow(
                            "java.lang.Object", "java.util.HashTable",
                            "application.transplant.index.search.IndexTable",
                            "application.transplant.index.search.Document",
                            "java.util.ArrayList",
                            "application.transplant.index.search.Searcher",
                            "application.transplant.index.search.pojo.SearchResult")
                    .setEnableProfilingRPCDataMovement(profileCPUDPUDataMovement)
                    .setCPUOnly(cpuOnly)
            );

            int requestCount = reqs;

            try {
                String basePath = (System.getProperty("user.dir"));
                if("".equals(dictPath)) {
                    dictPath = basePath + "/src/application/transplant/index/search/database/dict.txt";
                }

                System.out.println("use dict path = " + dictPath);
                if("".equals(filesPath)) filesPath = basePath + "/src/application/transplant/index/search/database/files";
                IndexSearchDatabase indexSearchDatabase =
                        dm.initialize()
                                .buildDictionary(dictPath)
                                .buildIndexes(filesPath, docCount)
                                .buildDatabase();

                System.out.println("build database Finished");


                File requestFile =
                        new File(reqFilePath + "/" +
                                requestCount + ".txt");
                if(!requestFile.exists())
                    System.out.println("generate request amount = " + requestCount);
                    Preprocessing.generateRequest(requestCount, filesPath, dictPath, reqFilePath + "/" +
                            requestCount + ".txt");


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

                UPMEM.reportProfiling();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }


    }
}

