package application.transplant.index.search;

import java.util.*;

public class IndexTable {
    // word id -> (int x int)+
    Dictionary<Integer, List<Match>> mappingTable = new Hashtable<>();

    private int recordCount = 0;
    public int getSize(){
        return mappingTable.size() + recordCount;
    }
    public void insert(int wordID, int documentId, int location) {
        List<Match> matched = mappingTable.get(wordID);
        if (matched == null) {
            matched = new ArrayList<>();
            matched.add(new Match(documentId, location));
        }
        recordCount++;
    }


    public Dictionary<Integer, List<Match>> getMappingTable() {
        return mappingTable;
    }

    public int getRecordCount() {
        return recordCount;
    }
}
