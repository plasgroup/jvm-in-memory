package transplant.index.search;

import java.util.ArrayList;
import java.util.List;

public class Document {
    int did;
    List<Integer> context = new ArrayList<>();

    public Document(int id) {
        this.did = id;
    }

    public void addWord(int wid){
        context.add(wid);
    }


}
