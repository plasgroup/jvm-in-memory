package framework.primitive.linq;

public class LinqFacade {

    public static void join(){

    }
    public static void select(ICondition condition){
        // send anonymous class to DPU
        // set DPUs' "PC" pointer
        // execute (use all tasklets to scan heap)
        // retrieve result from each DPU
        // merge result
        ICondition i = new ICondition() {
            @Override
            public boolean condition(Object object) {
                return false;
            }
        };
        i.getClass();
    }

    public static void where(){

    }

    public static void selectMany(){

    }

    public static void orderBy(){

    }

    public static void filter(){

    }
    public static void main(String[] args){

        ICondition i = new ICondition() {
            @Override
            public boolean condition(Object object) {
                return false;
            }
        };
        System.out.println(i.getClass());



    }
}
