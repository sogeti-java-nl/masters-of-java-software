package nl.ctrlaltdev.util;
/** 
 * Utility class to compare 2 object references and see if they're equal
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */

public class RefCmp {

	/** 
	 * checks if two references are equal. Which is true if both are NULL, false if one of both is NULL, and true if equals returns true.
	 * @return true if the two object references are equal.
	 */

	public static boolean equals(Object o1,Object o2) {
		if ((o1==null)&&(o2==null)) return true;
		if ((o1==null)||(o2==null)) return false;
		return o1.equals(o2);
	}
	
	/**
	 * Same as above, only then a toString() compare (casesensitive)
	 */
	
	public static boolean equalsStr(Object o1,Object o2) {
		if ((o1==null)&&(o2==null)) return true;
		if ((o1==null)||(o2==null)) return false;
		return o1.toString().equals(o2.toString());
	}
	

}

