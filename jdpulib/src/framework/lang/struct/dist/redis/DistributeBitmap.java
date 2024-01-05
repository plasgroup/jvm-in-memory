package framework.lang.struct.dist.redis;

public class DistributeBitmap extends SBitmap{
    SBitmap[] bitmaps;
    int averageBitCount = 0;
    int remainsBitCount = 0;
    DistributeBitmap(int partitionAmount, int totalBitCount){
        bitmaps = new SArrayBitmap[partitionAmount];
        int avgBitCount = totalBitCount / partitionAmount;
        int remainsBitCount = totalBitCount % partitionAmount;
        for(int i = 0; i < partitionAmount - 1; i++){
            bitmaps[i] = new SArrayBitmap(avgBitCount);
        }
        bitmaps[partitionAmount - 1] = new SArrayBitmap(avgBitCount + remainsBitCount);
        averageBitCount = avgBitCount;
        this.remainsBitCount = remainsBitCount;
    }

    @Override
    public int getiThBit(int i) {
        int partitionLocation = i / averageBitCount;
        int offset = i % averageBitCount;
        return bitmaps[partitionLocation].getiThBit(offset);
    }

    @Override
    public void setiThBit(int i) {
        int partitionLocation = i / averageBitCount;
        int offset = i % averageBitCount;
        bitmaps[partitionLocation].setiThBit(offset);
    }

    @Override
    public void cleariThBit(int i) {
        int partitionLocation = i / averageBitCount;
        int offset = i % averageBitCount;
        bitmaps[partitionLocation].cleariThBit(offset);
    }



}
