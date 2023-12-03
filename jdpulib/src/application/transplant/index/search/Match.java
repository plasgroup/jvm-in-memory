package application.transplant.index.search;

public class Match {
    int documentID;
    int documentLocationInList;
    int wordLocation;

    public Match(int documentId, int location) {
        this.documentID = documentId;
        this.wordLocation = location;
    }

    public int getDocumentID() {
        return documentID;
    }

    public int getDocumentLocationInList() {
        return documentLocationInList;
    }

    public int getWordLocation() {
        return wordLocation;
    }
}
