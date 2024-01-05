package application.transplant.index.search;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.UPMEM;
import simulator.PIMRemoteJVMConfiguration;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


public class IndexSearchDatabaseBuilder {
    final static int dpuInUse = 4;
    IndexTable[] tables;
    Dictionary<String, Integer> dictionary;
    Dictionary<Integer, String> documentIDMap;
    List<Document>[] documents;
    Searcher[] dpuSearchers;

    public static void serializeIndexTablesToFolder(IndexTable[] tables, String outputPath){

    }

    public IndexSearchDatabaseBuilder makeIndexFromDocumentsFiles(String documentsPath, String serializePath) throws IOException{
        buildIndexes(documentsPath);
        return this;
    }

    public IndexSearchDatabase buildDatabase(){
        IndexSearchDatabase database = new IndexSearchDatabase();
        database.tables = tables;
        database.dictionary = dictionary;
        database.documentIDMap = documentIDMap;
        database.documents = documents;
        database.dpuSearchers = dpuSearchers;
        return database;
    }


    public IndexSearchDatabaseBuilder buildIndexes(String docPath) throws IOException{


        File f = new File(docPath);
        if (!f.exists() || f.isFile()) return this;
        File[] fs = f.listFiles();
        int did = 1;
        int lastDPU = 0;

        for (int i = 0; i < fs.length; i++) {
            File file = fs[i];
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
            String content = new String(fis.readAllBytes());
            int dpuID = lastDPU;
            if(getSize(dpuID) >= 10000000){
                dpuID = ++lastDPU;
            }

            Document doc = (Document) UPMEM.getInstance().createObject(dpuID, Document.class, did);

            // insert words to doc
            List<Integer> splitedContent = Arrays.stream(content.split(System.lineSeparator())).map(e -> e.split(" ")).map(e -> Arrays.stream(e).map(w -> dictionary.get(w.replace(".","").toLowerCase())).collect(Collectors.toList())).collect(Collectors.toList()).stream().flatMap(List::stream).collect(Collectors.toList());
            pushContent(doc, splitedContent);

            documentIDMap.put(did, f.getPath());
            insertDocument(dpuID, doc);


            String[] words = content.split(" ");

            int location = 0;
            for (int wid : splitedContent) {
                insertWordIndexRecord(dpuID, wid, did, location++);
            }

            did++;
        }
        return this;
    }

    private String formalWord(String w) {
        return w.replace(";","").toLowerCase();
    }

    private void pushContent(Document doc, List<Integer> splitedContent) {

        for(Integer wordID : splitedContent){
               doc.pushWord(wordID);
        }
    }

    public void insertDocument(int dpuID, Document doc) {
        documents[dpuID].add(doc);
    }

    public void insertWordIndexRecord(int dpuID, int wordID, int did, int i) {
        tables[dpuID].insert(wordID, did, i);
    }

    static int sizes[] = new int[dpuInUse];
    private int getSize(int dpuID) {
        int s = 0;
        for (int i = 0; i < dpuInUse; i++) {
            s += sizes[i];
        }

        return tables[dpuID].getSize() * 4 + s;

    }

    private int getWordID(String w) {
        if (dictionary.get(w) == null) throw new RuntimeException();
        return dictionary.get(w);
    }

    public IndexSearchDatabaseBuilder buildDictionary(String dictionaryFilePath) throws IOException {
        File f = new File(dictionaryFilePath);

        int wid = 0;
        if(!f.exists()) return this;
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        if(dictionary == null) dictionary = new Hashtable<>();
        while((line = br.readLine()) != null){
            if("".equals(line)) continue;
            //Word w = new Word(wid, line);
            dictionary.put(line, wid);
            wid++;
        }
        return this;
    }


    public IndexSearchDatabaseBuilder initialize() throws IOException {
        documents = new List[PIMRemoteJVMConfiguration.JVMCount];
        dpuSearchers = new Searcher[PIMRemoteJVMConfiguration.JVMCount];
        tables = new IndexTable[PIMRemoteJVMConfiguration.JVMCount];

        for(int i = 0; i < PIMRemoteJVMConfiguration.JVMCount; i++){
            tables[i] = (IndexTable) (IDPUProxyObject) UPMEM.getInstance().createObject(i, IndexTable.class);
            documents[i] = (List<Document>) UPMEM.getInstance().createObject(i, ArrayList.class);
            String descriptor = "<init>:(Lapplication/transplant/index/search/IndexTable;Ljava/util/List;)V";
            if (dpuSearchers[i] == null)
                dpuSearchers[i] =
                        (Searcher) UPMEM.getInstance().getDPUManager(i).
                                createObjectSpecific(
                                        Searcher.class,
                                        descriptor,
                                        ((IDPUProxyObject)tables[i]).getAddr(),
                                        ((IDPUProxyObject)documents[i]).getAddr());

        }

        documentIDMap = new Hashtable<>();
        return this;
    }
}
