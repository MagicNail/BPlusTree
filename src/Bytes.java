import java.util.*;

/** Utilities for converting to and from arrays of bytes */



public class Bytes{

    public static byte[] intToBytes(int number){
	byte[] ans = new byte[4];
	ans[0] = (byte) ((number>>24) & 0xff);
	ans[1] = (byte) ((number>>16) & 0xff);
	ans[2] = (byte) ((number>>8) & 0xff);
	ans[3] = (byte) (number & 0xff);
	return ans;
    }

    public static void intToBytes(int number, byte[] block, int pos){
	block[pos]  =  (byte) ((number>>24) & 0xff);
	block[pos+1] = (byte) ((number>>16) & 0xff);
	block[pos+2] = (byte) ((number>>8) & 0xff);
	block[pos+3] = (byte) (number & 0xff);
    }

    public static int bytesToInt(byte[] bytes){
	return ((bytes[0] & 0xff)<<24) ^ ((bytes[1] & 0xff)<<16) ^ ((bytes[2] & 0xff)<<8) ^ (bytes[3] & 0xff);
    }
    
    public static int bytesToInt(byte[] bytes, int offset){
	return (bytes[offset]<<24) ^ (bytes[offset+1]<<16) ^ (bytes[offset+2]<<8) ^ bytes[offset+3];
    }
    
    public static int bytesToInt(byte b1, byte b2, byte b3, byte b4){
	return ((int)b1<<24) ^ ((int)b2<<16) ^ ((int)b3<<8) ^ (int)b4;
    }

    public static class ByteArrayComparator implements Comparator<byte[]>{
	public int compare(byte[] a, byte[] b){
	    int shared = a.length;
	    if (b.length<shared) shared = b.length;
	    for (int i=0; i<shared; i++){
		if (a[i]+128 < b[i]+128) return -1;
		if (a[i]+128 > b[i]+128) return 1;
	    }
	    // they are the same up to index shared
	    if (a.length < b.length) return -1;
	    if (a.length > b.length) return 1;
	    return 0;
	}
    }
    
    // TEST CODE. Illustrates how the methods could be used

    public static void main(String[] args){
	byte[] a = new byte[]{(byte)0134, (byte)0345, (byte)0456, (byte)0753};
	byte[] b = new byte[]{(byte)0134, (byte)0345, (byte)0546, (byte)0753};
	byte[] c = new byte[]{(byte)0134, (byte)0345, (byte)0546};

	Comparator<byte[]> comp = new ByteArrayComparator();
	
	System.out.printf("a: %o | %o | %o | %o\n", a[0], a[1], a[2], a[3]);
	System.out.printf("b: %o | %o | %o | %o\n", b[0], b[1], b[2], b[3]);
	System.out.printf("c: %o | %o | %o \n", c[0], c[1], c[2]);
	System.out.printf("a?a : %d\na?b : %d\na?c : %d\nb?c : %d\n",
			   comp.compare(a, a),
			   comp.compare(a, b),
			   comp.compare(a, c),
			   comp.compare(b, c));
	System.out.printf("b?b : %d\nb?a : %d\nc?a : %d\nc?b : %d\n",
			   comp.compare(b, b),
			   comp.compare(b, a),
			   comp.compare(c, a),
			   comp.compare(c, b));
	System.out.println(bytesToInt((byte)0134, (byte)0345, (byte)0456, (byte)0753));

	int n = 024567654321;
	System.out.printf("intToBytes(%o):\n",n);
	System.out.printf("intToBytes(%d):\n",n);
	b = intToBytes(n);
	
	System.out.println(b[0]+"."+b[1]+"."+b[2]+"."+b[3]);
    }


}
