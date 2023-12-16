package application.transplant.index.search;

import application.transplant.index.search.pojo.SearchResult;

import java.util.List;

public class Searcher {
    public IndexTable table;
    public List<Document> documents; // proxy

    public Searcher(IndexTable table, List<Document> documents){
        this.table = table;
        this.documents = documents;
    }

    public IndexTable getTable() {
        return table;
    }

    public SearchResult searchDocumentIds(int... keywordIDs) {
        SearchResult searchResult = new SearchResult();
        int matchedCount = 0;
        int lastDocumentId = -1;
        int currentDocumentId = -1;
        int firstMatchDocumentId = -1;
        int firstMatchLocation = -1;

        if(keywordIDs.length == 0) return null;
        List<Match> matchedLocations = table.getMappingTable().get(keywordIDs[0]);
        System.out.println(matchedLocations);
        if(matchedLocations == null) return null;
        for(int i = 0; i < matchedLocations.size(); i++){
            Match m = matchedLocations.get(i);
            Document d = documents.get(m.documentLocationInList);
            boolean matched = true;
            lastDocumentId = currentDocumentId;
            currentDocumentId = m.documentID;
            if(lastDocumentId == currentDocumentId) continue;
            for(int j = m.wordLocation; j < m.wordLocation + keywordIDs.length; j++){
                if(j >= d.context.size() || d.context.get(j) != keywordIDs[(j - m.wordLocation)]){
                    matched = false;
                    break;
                }
            }
            if(matched){
                if(firstMatchDocumentId == -1){
                    firstMatchDocumentId = currentDocumentId;
                    firstMatchLocation = m.wordLocation;

                    searchResult.firstMatchedDocumentId = firstMatchDocumentId;
                    searchResult.firstMatchedLocations = firstMatchLocation;
                }
                matchedCount++;
            }
        }
        searchResult.totalMatch = matchedCount;
        return searchResult;
    }


    public SearchResult search(int w0){
        return searchDocumentIds(w0);
    }

    public SearchResult search(int w0, int w1){
        return searchDocumentIds(w0, w1);
    }

    public SearchResult search(int w0, int w1, int w2){
        return searchDocumentIds(w0, w1, w2);
    }

    public SearchResult search(int w0, int w1, int w2, int w3){
        return searchDocumentIds(w0, w1, w2, w3);
    }

    public SearchResult search(int w0, int w1, int w2, int w3, int w4){
        return searchDocumentIds(w0, w1, w2, w3, w4);
    }
}
