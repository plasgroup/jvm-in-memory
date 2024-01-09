package framework.lang.struct.dist.redis;

public class SArrayBitmap extends SBitmap{

    byte[] bitmap;
    public SArrayBitmap(int bitCount){
        bitmap = new byte[bitCount];
    }

    public int getiThBit(int i){
        int byteLocation = i / 8;
        return (bitmap[byteLocation] >> (7 - i % 8)) & 1;
    }

    public void setiThBit(int i){
        int byteLocation = i / 8;
        bitmap[byteLocation] |= (1 << (7 - i % 8));
    }

    public void cleariThBit(int i){
        int byteLocation = i / 8;
        bitmap[byteLocation] &= ~(1 << (7 - i % 8));
    }

}
