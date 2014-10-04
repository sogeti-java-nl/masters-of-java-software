package nl.moj.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import nl.ctrlaltdev.util.Tool;

/**
 * Some utility code as a workaround for a Socket feature in JDK 1.5
 * @author E.Hooijmeijer
 */

public class InetAddressUtil {

	/**
	 * Fix for a JDK 1.5 feature which always tries to do a host lookup even if you just
	 * specify the IP address. Which takes a lot of time especially when the host is unknown 
	 * as is the case with a local address. A work-around is to specify both the name and the 
	 * IP address in bytes in which case the host-lookup does not take place.
	 * 
	 * This method does the String->byte[] parsing and returns a InetAddress with both the ip
	 * number and the host name filled in, if you specify a string with three dots.
	 * 
	 * @param address an IP address in string format.
	 * @return InetAddress
	 * @throws UnknownHostException
	 */
	
	public static InetAddress makeInetAddress(String address) throws UnknownHostException {
		//
		String[] parts=Tool.cut(address,".");
		if (parts.length!=4) return InetAddress.getByName(address);
		byte[] digits=new byte[4];
		for (int t=0;t<4;t++) try {
			digits[t]=(byte)Integer.parseInt(parts[t]);
		} catch (NumberFormatException ex) {
			// Not a number. Then it must a name :-)
			return InetAddress.getByName(address);
		}
		//
		return InetAddress.getByAddress(address,digits);
		//
	}
	
}
