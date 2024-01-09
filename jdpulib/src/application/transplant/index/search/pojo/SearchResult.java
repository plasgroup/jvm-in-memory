package application.transplant.index.search.pojo;

public class SearchResult {
    public int totalMatchedDocuments = 0;
    public int firstMatchedDocumentId = 0;
    public int firstMatchedLocations = 0;
    public int totalMatch = 0;

    public int getTotalMatchedDocuments() {
        return totalMatchedDocuments;
    }

    public int getFirstMatchedLocations() {
        return firstMatchedLocations;
    }

    public int getFirstMatchedDocumentId() {
        return firstMatchedDocumentId;
    }
    public int getTotalMatch(){
        return totalMatch;
    }
}
