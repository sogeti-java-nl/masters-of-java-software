package nl.ctrlaltdev.util;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/** 
 * Generic string utils
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */

public class Tool {

	/** reads a text file into a string. Optionally trims the lines. */
	public static String read(File f,boolean trim) throws IOException {
		BufferedReader r=new BufferedReader(new FileReader(f));
		return read(r,trim);
	}
	
	public static void write(File f,String text) throws IOException {
		FileWriter out=new FileWriter(f);
		try {
			out.write(text);
		} finally {
			out.close();
		}
	}
	
	/** reads a text file into a string. Optionally trims the lines. */
	public static String read(BufferedReader r,boolean trim) throws IOException {
		StringBuffer sb=new StringBuffer();		
		try {
			String s=r.readLine();
			while (s!=null) {
				if (trim) sb.append(s.trim());
					 else sb.append(s);
				sb.append("\n");
				s=r.readLine();
			}
		} finally {
			r.close();
		}
		//
		return sb.toString();		
	}	
	
	/** reads the contents of a InputStream into a byte array. Closes the Inputstream when done. */
	public static byte[] readBinary(InputStream f,int length) throws IOException {
		BufferedInputStream in=new BufferedInputStream(f);
		byte[] myData=new byte[length];
		try {
			int r=0;
			do {
				int c=in.read(myData,r,myData.length-r);
				if (c<0) throw new IOException("Missing data.");
				r=r+c;
			} while (r<myData.length);
		} finally {
			in.close();
		}
		return myData;		
	}		

	/** 
	 * cuts a string into substrings.
	 */

	public static String[] cut(String s,String sep) {
		List<String> l=new ArrayList<>();
		//
		StringTokenizer st=new StringTokenizer(s,sep);
		while (st.hasMoreTokens()) {
			l.add(st.nextToken());
		}
		//
		return l.toArray(new String[l.size()]);
	}

	/** 
	 * cuts a string into substrings, returns an empty string for double seps.
	 */
	
	public static String[] cutForEach(String s,String sep) {
		List<String> l=new ArrayList<>();
		//
		StringBuffer sb=new StringBuffer();
		for (int t=0;t<s.length();t++) {
			char c=s.charAt(t);
			if (sep.indexOf(c)>=0) {
				l.add(sb.toString());
				sb.delete(0,sb.length());
			} else {
				sb.append(c);
			}
		}
		//
		l.add(sb.toString());
		//
		return l.toArray(new String[l.size()]);
	}
	
	/**
	 * concats an array of strings separated by comma's
	 */
	
	public static String arrayToString(Object[] o) {
		StringBuffer sb=new StringBuffer();
		for (int t=0;t<o.length;t++) {
			if(t>0) sb.append(',');
			sb.append(o[t].toString());
		}		
		return sb.toString();
	}
	
	/**
	 * replaces in the source string 'from' with 'to'
	 * @param src the source string.
	 * @param from the from string.
	 * @param to the to string.
	 */
	
	public static String replace(String src,String from,String to) {
		int idx=src.indexOf(from);
		if (idx<0) return src;
		StringBuffer sb=new StringBuffer();
		while (idx>=0) {
			sb.append(src.substring(0,idx));
			sb.append(to);
			src=src.substring(idx+from.length());
			idx=src.indexOf(from);
		}
		sb.append(src);
		return sb.toString();
	}
	
	/**
	 * counts the number of times a char from the string chars are inside string src.
	 */
	
	public static int count(String src,String chars) {
		int cnt=0;
		for (int t=0;t<src.length();t++) {
			if (chars.indexOf(src.charAt(t))>=0) cnt++;
		}
		return cnt;
	}
	
	/** concats a string array with the specified delimiter. */
	
	public static String concat(String[] s,String sep) {
		if (s==null) return "";
		StringBuffer sb=new StringBuffer();
		for (int t=0;t<s.length;t++) {
			if (t!=0) sb.append(sep);
			sb.append(s[t]);
		}
		return sb.toString();
	}
	
	/** turns a crlf delimited message into a String[] */
	public static String[] arrayify(String message) {
		List<String> l=new ArrayList<>(); 
		int i=-1;
		//
		// Break up message.
		//
		while ((i=message.indexOf("\n"))>=0) {
			String sub=message.substring(0,i);
			if (sub.endsWith("\r")) sub=sub.substring(0,i-1);
			l.add(sub);
			message=message.substring(i+1);
			if (message.startsWith("\r")) message=message.substring(1);
		}
		//
		return l.toArray(new String[l.size()]);
	}	
	
	/** 
	 * parses double-quoted arguments in a string-array.
	 * Useful for command line entries that allow spaces.
	 * @param src the source string array.
	 * @return the resulting array in which the strings surrounded by quotes are returned as one. 
	 * @throws RuntimeException if a quote is missing.
	 */
	public static String[] parseArgs(String[] src) {
		List<String> myResults=new ArrayList<>();
		boolean inQuote=false;
		StringBuffer concat=new StringBuffer();
		for (int t=0;t<src.length;t++) {
			if (src[t].startsWith("\"")) {
				inQuote=true;
				concat.delete(0,concat.length());
				concat.append(src[t].substring(1));				
			} else if (inQuote) {
				if (src[t].endsWith("\"")) {
					inQuote=false;
					concat.append(' ');
					concat.append(src[t].substring(0,src[t].length()-1));
					myResults.add(concat.toString());
				} else {
					concat.append(' ');
					concat.append(src[t]);
				}
			} else {
				myResults.add(src[t]);
			}
		}
		if (inQuote) throw new RuntimeException("Missing closing quote.");
		return myResults.toArray(new String[myResults.size()]);
	}
	
	/**
	 * Tokenises a string with ( ) { } and spaces. Quoted strings cannot be split.  
	 */
	
	public static String[] parseStringWithQuotes(String s) {
		List<String> myResults=new ArrayList<>();
		boolean inQuote=false;
		char what=0;
		StringBuffer sb=new StringBuffer();
		//
		for (int t=0;t<s.length();t++) {
			char c=s.charAt(t);
			if (!inQuote) {
				if (c=='"') {
					if (sb.length()>0) {
						myResults.add(sb.toString());
						sb.delete(0,sb.length());
					}
					sb.append(c);
					inQuote=true;
					what='"';						
				} else if (c=='\'') {
					if (sb.length()>0) {
						myResults.add(sb.toString());
						sb.delete(0,sb.length());
					}
					sb.append(c);
					inQuote=true;
					what='\'';						
				} else if (c==' ') {
					myResults.add(sb.toString());
					sb.delete(0,sb.length());
					sb.append(c);
					myResults.add(sb.toString());
					sb.delete(0,sb.length());
				} else if ((c=='(')||(c==')')||(c=='{')||(c=='}')) {
					if (sb.length()>0) {
						myResults.add(sb.toString());
						sb.delete(0,sb.length());
					}
					sb.append(c);
					myResults.add(sb.toString());
					sb.delete(0,sb.length());
				} else {
					sb.append(c);
				}
			} else {
				if (c==what) {
					sb.append(c);
					inQuote=false;
					what=0;
					if (sb.length()>0) {
						myResults.add(sb.toString());
						sb.delete(0,sb.length());
					}
				} else {
					sb.append(c);
				}
			}
		}
		//		
		if (inQuote) throw new RuntimeException("Missing closing quote.");
		//
		if (sb.length()>0) {
			myResults.add(sb.toString());
			sb.delete(0,sb.length());
		}
		//
		return myResults.toArray(new String[myResults.size()]);
	}
	
	public static String decodeEscape(String s,char escapeID,String srcChars,String targetChars) {
		if (srcChars.length()!=targetChars.length()) throw new RuntimeException("srcChars does match targetChars length.");
		StringBuffer sb=new StringBuffer();
		boolean inEscape=false;
		for (int t=0;t<s.length();t++) {
			char c=s.charAt(t);
			if ((c==escapeID)&&(!inEscape)) {
				inEscape=true;
			} else if (!inEscape) {
				sb.append(c);
			} else {
				int i=srcChars.indexOf(c);
				if (i<0) throw new RuntimeException("Illegal Escape sequence char "+c+" in String '"+s+"' at position "+t);
				sb.append(targetChars.charAt(i));
				inEscape=false;
			}
		}
		return sb.toString();
	} 	
	
	public static String encodeEscape(String s,char escapeID,String srcChars,String targetChars) {
		StringBuffer sb=new StringBuffer();
		for (int t=0;t<s.length();t++) {
			char c=s.charAt(t);
			int i=srcChars.indexOf(c);
			if (i>=0) {
				sb.append(escapeID);
				sb.append(targetChars.charAt(i));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	public static String dequote(String s) {
		if (s==null) return null;
		if ((s.startsWith("'"))&&(s.endsWith("'"))) {
			return s.substring(1,s.length()-1);
		} else if ((s.startsWith("\""))&&(s.endsWith("\""))) {
			return s.substring(1,s.length()-1);
		}
		return s;
	}
	
	public static String quote(String s) {
		return "\""+s+"\"";
	}

	public static Object[] copy(Object[] src) {
		if (src==null) return null;
		Object[] dst=(Object[])Array.newInstance(src.getClass().getComponentType(),src.length);
		for (int t=0;t<src.length;t++) dst[t]=src[t];
		return dst;
	}

}

