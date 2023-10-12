//import transplant.index.search.IndexSearch;
//
//import java.io.*;
//
//public class Parser {
//    static int MAX_LINE_SIZE = (1024 * 1024 * 4);
//
//    static int word_hash(String word_name) {
//        int size = (word_name).length();
//        hash_t hash = new hash_t(0);
//        for (int i = 0; i < size; i++) {
//            hash.add(word_name.charAt(i));
//        }
//        return hash.hash % WORDS_HASHTABLE_SIZE;
//    }
//
//    static int word_in_dictionnary(String word_name, word_dictionnary dict) {
//        IndexSearch.word_t head = dict.dict[word_hash(word_name)];
//        IndexSearch.word_t word = head.next;
//        if(word == null) return -1;
//        while (word != null){
//            if(word.word_name.equals(word_name)) return word.word_id;
//            word = word.next;
//        }
//
//        return -1;
//    }
//
//    static int WORDS_HASHTABLE_SIZE = (1 << (8 * 1));
//
//    static class SList {
//
//        static void init(IndexSearch.word_t headWordsT) {
//            if(headWordsT == null) return;
//            headWordsT.next = null;
//        }
//
//        static void insert_head(IndexSearch.word_t word_hashtable, IndexSearch.word_t word) {
//            // 已经获取了某个值的hash, 现在存储到哈希表中
//            if(word_hashtable == null) return;
//            IndexSearch.word_t w = word_hashtable;
//            while(w.next != null){
//                w = w.next;
//            }
//            w.next= word;
//        }
//    }
//
//    public static word_dictionnary parse_dictionnary(String dictionnary_file_name, File words_map_file) throws IOException {
//        word_dictionnary dict = new word_dictionnary();
//        dict.nb_words = 0;
//        dict.dict = new IndexSearch.word_t[WORDS_HASHTABLE_SIZE];
//
//        IndexSearch.word_t[] words_hashtable = dict.dict;
//
//        //初始化一个哈希表，每个哈希表是一个链表。存储相关的记录。
//        for (int i = 0; i < WORDS_HASHTABLE_SIZE; i++) {
//            words_hashtable[i] = new IndexSearch.word_t();
//            SList.init(words_hashtable[i]);
//        }
//
//        BufferedReader br = new BufferedReader(new FileReader(dictionnary_file_name));
//
//        String str_read;
//        StringBuilder str;
//        while ((str_read = br.readLine()) != null) {
//            str = new StringBuilder(str_read);
//            int str_len = str.length() - 1;
//            assert (str_len < MAX_LINE_SIZE - 1);
//            boolean skip_word = false;
//            for (int each_char = 0; each_char < str_len; each_char++) {
//                char curr_char = (char) str.charAt(each_char);
//                if ((curr_char < 'a' || curr_char > 'z') && (curr_char < 'A' || curr_char > 'Z')) {
//                    skip_word = true;
//                    continue;
//                } else if (curr_char >= 'A' && curr_char <= 'Z') {
//                    str.setCharAt(each_char, (char) ('a' + curr_char - 'A'));
//                }
//            }
//            if (skip_word) {
//                continue;
//            }
//            IndexSearch.word_t word = new IndexSearch.word_t();
//            word.word_id = dict.nb_words++;
//            word.word_name = str.toString();
//            if (word.word_name == null) throw new RuntimeException();
//
//            SList.insert_head(words_hashtable[word_hash((String) word.word_name)], word);
//            System.out.printf("%d %s\n", word.word_id, word.word_name);
//
//            if (words_map_file != null)
//                System.out.printf("%u %s\n", word.word_id, word.word_name);
//        }
//
//        return dict;
//    }
//
//    void clearList(IndexSearch.word_t wt){
//        if(wt == null) return;
//        clearList(wt.next);
//        wt.next = null;
//    }
//    void free_dictionnary(word_dictionnary dict) {
//        for(int i = 0; i < dict.dict.length; i++){
//            clearList(dict.dict[i]);
//        }
//    }
//
//    private static class hash_t {
//        int hash = 0;
//
//        public hash_t(int i) {
//            this.hash = i;
//        }
//
//        public void add(char c) {
//            this.hash += c;
//        }
//    }
//
//
//    public static class word_dictionnary {
//        public IndexSearch.word_t[] dict;
//        public int nb_words;
//    }
//}
