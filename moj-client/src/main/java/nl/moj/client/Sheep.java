package nl.moj.client;

import java.awt.Color;

/**
 * Some Sheep to play with.
 */

public class Sheep {

	public static Color[] whiteSheepColors=new Color[] {
		null,new Color(0xE0E0E0),new Color(0x000000),new Color(0xD0D0D0),new Color(0xFFFFFF)
	};
	public static Color[] blackSheepColors=new Color[] {
		null,new Color(0xE0E0E0),new Color(0x000000),new Color(0x202020),new Color(0x303030)
	};
	
	public static byte[][] pixels=new byte[][] {
		{ 0,0,1,2,2,1,0,0 },
		{ 0,0,2,4,4,2,0,0 },
		{ 0,1,2,4,4,2,0,0 },
		{ 1,2,3,4,4,3,2,0 },
		{ 2,3,4,4,4,4,3,2 },
		{ 2,3,4,4,4,4,3,2 },
		{ 1,2,3,4,4,3,2,1 },
		{ 0,1,2,3,2,2,1,0 },
		{ 0,0,1,2,1,0,0,0 }
	};
	
}
