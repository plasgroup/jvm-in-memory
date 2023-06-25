package pim;

public class BytesUtils {
    public static int readU4BigEndian(byte[] bs, int offset){
        return ((0xFF & bs[offset]) << 24) | ((0xFF & bs[offset + 1]) << 16) |
                ((0xFF & bs[offset + 2]) << 8) | (0xFF & bs[offset + 3]);
    }
    public static int readU2BigEndian(byte[] bs, int offset){
        return ((0xFF & bs[offset]) << 8) | ((0xFF & bs[offset + 1]));
    }
    public static int readU4LittleEndian(byte[] bs, int offset){
        return ((0xFF & bs[offset + 3]) << 24) | ((0xFF & bs[offset + 2]) << 16) |
                ((0xFF & bs[offset + 1]) << 8) | (0xFF & bs[offset]);
    }
    public static int readU2LittleEndian(byte[] bs, int offset){
        return ((0xFF & (bs[offset]) << 8)) | ((0xFF & (bs[offset + 1])));
    }

    public static void writeU4LittleEndian(byte[] bs, int num, int offset){
        bs[offset + 3] = (byte) ((num >> 24) & 0xFF);
        bs[offset + 2] = (byte) ((num >> 16) & 0xFF);
        bs[offset + 1] = (byte) ((num >> 8) & 0xFF);
        bs[offset] = (byte) (num & 0xFF);
    }
    public static void writeU2LittleEndian(byte[] bs, int num, int offset){
        bs[offset + 1] = (byte) ((num >> 8) & 0xFF);
        bs[offset] = (byte) (num & 0xFF);
    }

    public static void writeU4BigEndian(byte[] bs, int num, int offset){
        bs[offset] = (byte) ((num >> 24) & 0xFF);
        bs[offset + 1] = (byte) ((num >> 16) & 0xFF);
        bs[offset + 2] = (byte) ((num >> 8) & 0xFF);
        bs[offset + 3] = (byte) (num & 0xFF);
    }
    public static void writeU2BigEndian(byte[] bs, int num, int offset){
        bs[offset] = (byte) ((num >> 8) & 0xFF);
        bs[offset + 1] = (byte) (num & 0xFF);
    }

    public static long readU8BigEndian(byte[] bs, int offset){
        long s = ((long) BytesUtils.readU4BigEndian(bs, offset) << 32) | (BytesUtils.readU4BigEndian(bs, offset + 4));
        return s;
    }

    public static long readU8LittleEndian(byte[] bs, int offset){
        long s = (BytesUtils.readU4LittleEndian(bs, offset)) | ((long) BytesUtils.readU4LittleEndian(bs, offset + 4) << 32);
        return s;
    }


}
