package transplant.index.search;

import transplant.index.search.pojo.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class Searcher {
    IndexTable table;
    public List<Document> documents; // proxy

    public Searcher(IndexTable table){
        this.table = table;
    }

    public IndexTable getTable() {
        return table;
    }

    public SearchResult searchDocumentIds(int[] keywordIDs) {
        SearchResult searchResult = new SearchResult();
        int matchedCount = 0;
        int lastDocumentId = -1;
        int currentDocumentId = -1;
        int firstMatchDocumentId = -1;
        int firstMatchLocation = -1;

        if(keywordIDs.length == 0) return null;
        List<Match> matchedLocations = table.getMappingTable().get(keywordIDs[0]);
        for(int i = 0; i < matchedLocations.size(); i++){
            Match m = matchedLocations.get(i);
            Document d = documents.get(m.documentLocationInList);
            boolean matched = true;
            lastDocumentId = currentDocumentId;
            currentDocumentId = m.documentID;
            if(lastDocumentId == currentDocumentId) continue;
            for(int j = m.wordLocation; j < m.wordLocation + keywordIDs.length; i++){
                if(j >= d.context.size() || d.context.get(j) != keywordIDs[j - m.wordLocation]){
                    matched = false;
                    break;
                }
            }
            if(matched){
                if(firstMatchDocumentId != -1){
                    firstMatchDocumentId = currentDocumentId;
                    firstMatchLocation = m.wordLocation;
                }
                searchResult.firstMatchedDocumentId = firstMatchDocumentId;
                searchResult.firstMatchedLocations = firstMatchLocation;
                matchedCount++;
            }
        }
        searchResult.totalMatchedDocuments = matchedCount;
        return searchResult;
    }
}
