package application.transplant.pimtree;


import static application.transplant.pimtree.ApplicationConfiguration.DB_SIZE;

public class mdbptr {
    public len_addr la;
    Object[] data_blocks;


    public static class data_block {
        public len_addr la;
        Object[] data;
        public data_block(){
            data = new Object[DB_SIZE];
        }
    }

    public static mdbptr InvalidPtr(){
        mdbptr p = new mdbptr();
        return p;
    }
}
