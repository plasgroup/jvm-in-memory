package application.transplant.pimtree;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class dpu_control {
    public static Lock dpu_mutex = new ReentrantLock();

}
