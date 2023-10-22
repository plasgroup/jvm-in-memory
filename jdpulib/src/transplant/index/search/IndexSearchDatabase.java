package transplant.index.search;

import pim.UPMEM;
import transplant.index.search.pojo.Word;


import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class IndexSearchDatabase {
    public IndexTable[] tables; // proxy
    public List<Document>[] documents; // proxy
    public Searcher[] dpuSearchers; // proxy

    final static int dpuInUse = 64;
    Dictionary<Word, Integer> dictionary = new Hashtable<>();
    Dictionary<Integer, String> documentIDMap = new Hashtable<>();


    public IndexSearchDatabase(int dpuInUse){
        tables = new IndexTable[dpuInUse];
        documents = new ArrayList[dpuInUse];
        for(int i = 0; i < dpuInUse; i++){
            tables[i] = (IndexTable) UPMEM.getInstance().createObject(i, IndexTable.class);
            documents[i] = (List<Document>) UPMEM.getInstance().createObject(i, ArrayList.class);
        }
    }


    public void insertDocument(int dpuID, Document doc) {
        documents[dpuID].add(doc);
    }

    public void insertWordIndexRecord(int dpuID, int wordID, int did, int i) {
        tables[dpuID].insert(wordID, did, i);
    }
}

