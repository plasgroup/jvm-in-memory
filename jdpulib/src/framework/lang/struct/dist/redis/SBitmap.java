package framework.lang.struct.dist.redis;

public abstract class SBitmap extends RObject{
    public abstract int getiThBit(int i);
    public abstract void setiThBit(int i);
    public abstract void cleariThBit(int i);

}
