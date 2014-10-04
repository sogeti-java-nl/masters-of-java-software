package nl.ctrlaltdev.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Some silly hashing and encoding functions.
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */
public class Encoder {

	public static String hash(String source) {
		if (source==null) return null;
		try {
			MessageDigest md=MessageDigest.getInstance("SHA");
			byte buf[]=source.getBytes();
			md.update(buf);		  
			return hexString(intArray(md.digest()));
		} catch (NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static String hexString(int[] d) {
		StringBuffer sb=new StringBuffer();
		for (int t=0;t<d.length;t++) {
			sb.append((char)((d[t]>>4)<10 ? '0'+(d[t]>>4) : 'A'+((d[t]>>4)-10))); 
			sb.append((char)((d[t]&0x0F)<10 ? '0'+(d[t]&0x0F) : 'A'+((d[t]&0x0F)-10))); 
		}
		return sb.toString();
	}
	
	public static int[] intArray(byte[] d) {
		int[] r=new int[d.length];
		for (int t=0;t<d.length;t++) {
			r[t]=d[t]&0xFF;
		}
		return r; 
	}

}
