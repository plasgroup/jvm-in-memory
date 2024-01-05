package application.transplant.index.search;

import application.transplant.index.search.pojo.SearchResult;
import framework.pim.UPMEM;
import application.transplant.index.search.pojo.Word;
import simulator.PIMRemoteJVMConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class IndexSearchDatabase {
    public IndexTable[] tables; // proxy
    public List<Document>[] documents; // proxy
    public Searcher[] dpuSearchers; // proxy
    final static int dpuInUse = 64;
    Dictionary<String, Integer> dictionary = new Hashtable<>();
    Dictionary<Integer, String> documentIDMap = new Hashtable<>();

    public IndexSearchDatabase(){
//        tables = new IndexTable[dpuInUse];
//        documents = new ArrayList[dpuInUse];
//        for(int i = 0; i < dpuInUse; i++){
//            tables[i] = (IndexTable) UPMEM.getInstance().createObject(i, IndexTable.class);
//            documents[i] = (List<Document>) UPMEM.getInstance().createObject(i, ArrayList.class);
//        }
    }



    public void insertDocument(int dpuID, Document doc) {
        documents[dpuID].add(doc);
    }
    public void insertWordIndexRecord(int dpuID, int wordID, int did, int i) {
        tables[dpuID].insert(wordID, did, i);
    }

    public void search(String... words) {
        int[] collect = Arrays.stream(words).map(s -> dictionary.get(s.replace(".", "").toLowerCase())).mapToInt(e -> e).toArray();
        int totalMatch = 0;
        int firstMatchLocation = -1;

        int firstMatchDocumentID = Integer.MAX_VALUE;
        for(int i = 0; i < PIMRemoteJVMConfiguration.JVMCount; i++){
            SearchResult searchResult;
            if(words.length == 1){
                searchResult = dpuSearchers[i].search(collect[0]);
            }else if(words.length == 2){
                searchResult = dpuSearchers[i].search(collect[0], collect[1]);
            }else if(words.length == 3){
                searchResult = dpuSearchers[i].search(collect[0], collect[1], collect[2]);
            }else if(words.length == 4){
                searchResult = dpuSearchers[i].search(collect[0], collect[1], collect[2], collect[3]);
            }else if(words.length == 5){
                searchResult = dpuSearchers[i].search(collect[0], collect[1], collect[2], collect[3], collect[4]);
            }else{
                throw new RuntimeException("Keywords' count should not more than 5.");
            }
            if(searchResult == null) continue;
            totalMatch += searchResult.getTotalMatch();
            int firstDid = searchResult.getFirstMatchedDocumentId();
            if(firstDid < firstMatchDocumentID){
                firstMatchDocumentID = firstDid;
                firstMatchLocation = searchResult.getFirstMatchedLocations();
            }
        }
        System.out.println("search finish.., total matched = " + totalMatch + ", first matched did = " + firstMatchDocumentID
                + ", first matched location = " + firstMatchLocation);

    }


}

