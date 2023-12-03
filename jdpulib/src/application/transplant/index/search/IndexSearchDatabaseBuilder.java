package application.transplant.index.search;

import framework.lang.struct.IDPUProxyObject;
import framework.pim.UPMEM;
import application.transplant.index.search.pojo.Word;

import java.io.*;
import java.util.Dictionary;
import java.util.List;


public class IndexSearchDatabaseBuilder {
    final static int dpuInUse = 64;
    IndexTable[] tables;
    Dictionary<Word, Integer> dictionary;
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
        IndexSearchDatabase database = new IndexSearchDatabase(dpuInUse);
        database.tables = tables;
        database.dictionary = dictionary;
        database.documentIDMap = documentIDMap;
        database.documents = documents;
        return database;
    }

    public IndexSearchDatabaseBuilder buildIndexes(String docPath) throws IOException{

        tables = new IndexTable[dpuInUse];

        for(int i = 0; i < dpuInUse; i++){
            tables[i] = (IndexTable) UPMEM.getInstance().createObject(i, IndexTable.class);
        }

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

            Document doc = (Document) UPMEM.getInstance().createObject(dpuID, Document.class, did, content);
            documentIDMap.put(did, f.getPath());
            insertDocument(dpuID, doc);

            String[] words = content.split(" ");
            if (dpuSearchers[dpuID] == null)
                dpuSearchers[dpuID] =
                        (Searcher) UPMEM.getInstance()
                                .createObject(i, Searcher.class, ((IDPUProxyObject)tables[i]).getAddr(),
                                        ((IDPUProxyObject)documents[i]).getAddr());

            int location = 1;
            for (String w : words) {
                insertWordIndexRecord(dpuID, getWordID(w), did, location++);
            }

            did++;
        }
        return this;
    }

    public void insertDocument(int dpuID, Document doc) {
        documents[dpuID].add(doc);
    }

    public void insertWordIndexRecord(int dpuID, int wordID, int did, int i) {
        tables[dpuID].insert(wordID, did, i);
    }

    private int getSize(int dpuID) {
        return tables[dpuID].getSize() * 4 + documents[dpuID].stream().map(e -> e.context.size() * 4 + 4).reduce(0, Integer::sum);

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
        while((line = br.readLine()) != null){
            if("".equals(line)) continue;
            Word w = new Word(wid, line);
            dictionary.put(w, wid);
            wid++;
        }
        return this;
    }


}
