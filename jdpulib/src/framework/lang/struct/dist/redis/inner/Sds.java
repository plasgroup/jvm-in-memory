package framework.lang.struct.dist.redis.inner;

public class Sds {
    public int free;
    public int length;
    public byte[] buffer;

    public Sds(int length, int free){
        this.length = length;
        this.buffer = new byte[length];
        this.free = free;
    }

}
