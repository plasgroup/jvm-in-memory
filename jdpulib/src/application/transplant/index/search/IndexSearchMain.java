package application.transplant.index.search;

import java.io.*;

public class IndexSearchMain {
    public static void main(String[] args){
        IndexSearchDatabaseBuilder dm = new IndexSearchDatabaseBuilder();
        try {
            IndexSearchDatabase indexSearchDatabase =
                    dm.buildDictionary("./database/dict.txt")
                    .buildIndexes("./database/files/")
                    .buildDatabase();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

