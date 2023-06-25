package pim;

public class StringUtils {
    public static String getStringFromBuffer(byte[] bs, int offset, int len){
        byte[] sb = new byte[len];
        for(int i = 0; i < len; i++) sb[i] = bs[offset + i];
        return new String(sb);
    }
}
