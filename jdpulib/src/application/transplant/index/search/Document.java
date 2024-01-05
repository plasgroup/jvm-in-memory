package application.transplant.index.search;

import java.util.ArrayList;
import java.util.List;

public class Document {
    int did;
    List<Integer> context;

    public Document(int id) {
        this.did = id;
        this.context = new ArrayList<>();
    }

    public Document(int id, List<Integer> context){
        this.did = id;
        this.context = context;
    }

    public void addWord(int wid){
        context.add(wid);
    }

    public int getDid() {
        return did;
    }

    public List<Integer> getContext() {
        return context;
    }

    public void setContext(List<Integer> context) {
        this.context = context;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public void pushWord(int wordID) {
        this.context.add(wordID);
    }

}
