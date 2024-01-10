package application.transplant.index.search.database;

import java.io.*;
import java.util.*;

public class Preprocessing {
    static String dictionaryFilePath =
            System.getProperty("user.dir") + "/src/application/transplant/index/search/database/dict.txt";
    static HashSet<String> buildDictionary() throws IOException {
        File f = new File(dictionaryFilePath);
        HashSet<String> wordSet = new HashSet<>();
        if(!f.exists()) return null;
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while((line = br.readLine()) != null){
            if(line.isEmpty()) continue;
            wordSet.add(line);
        }
        return wordSet;
    }

    public static void generateRequest(int requestCount) throws IOException {
        String basePath = (System.getProperty("user.dir"));

        System.out.println("generate request, count = " + requestCount);
        File requestFile =
                new File(basePath +
                        "/src/application/transplant/index/search/database/request" + requestCount + ".txt");
        HashSet<String> wordsSet = buildDictionary();

        Random r = new Random();

        File filesDictionary =
                new File(basePath + "/src/application/transplant/index/search/database/files");
        File[] files = filesDictionary.listFiles();

        int filesCount = files.length;
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(requestFile));
        int generatedCount = 0;
        int generateEnsuredSequenceCount = requestCount;
        while(generatedCount < generateEnsuredSequenceCount){
            String s = new String(new FileInputStream(files[r.nextInt(0, filesCount)])
                    .readAllBytes());
            int takes = r.nextInt(1, 6);

            String[] words = s.split(" ");
            int skipatable = words.length - takes;
            if(skipatable < 2) continue;

            String requestLine = Arrays.stream(words)
                    .skip(r.nextInt(1, skipatable))
                    .filter(p -> wordsSet.contains(p))
                    .reduce((w1, w2) -> w1 + " " + w2)
                    .get();

            bufferedWriter.append(requestLine);
            bufferedWriter.newLine();
            generatedCount++;
        }
        bufferedWriter.close();
    }

    public static void main(String[] args) throws IOException {
        if(args.length < 1) return;
        String mode = args[0].strip().toUpperCase();
        if("NORM".equals(mode)){
            String basePath = (System.getProperty("user.dir"));
            File filesDictionary = new File(basePath + "/src/application/transplant/index/search/database/files");
            File[] files = filesDictionary.listFiles();
            HashSet<String> wordsSet = buildDictionary();
            for(int i = 0; i < Objects.requireNonNull(files).length; i++){
                File f = files[i];
                System.out.println("Process " + f.getPath());
                FileInputStream fis = new FileInputStream(f);
                String s = new String(fis.readAllBytes()).replaceAll("[^a-zA-Z]", " ");
                String reduce = Arrays.stream(s.split(" "))
                        .map(word -> word.strip().toLowerCase())
                        .filter(w -> wordsSet.contains(w))
                        .reduce((a, b) -> (a + " " + b))
                        .get();

                fis.close();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(reduce.getBytes());
                fos.close();
            }
        }else if("GEN_REQ".equals(mode)){
            if(args.length < 2) return;
            generateRequest(Integer.parseInt(args[1]));
        }
    }
}