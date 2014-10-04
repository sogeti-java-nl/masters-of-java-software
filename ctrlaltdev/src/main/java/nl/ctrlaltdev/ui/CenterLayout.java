package nl.ctrlaltdev.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import java.awt.LayoutManager2;

/**
 * Positions the Center panel in the center of the parent container. If the
 * parent container is bigger than the centers preferred size, space is allocated
 * to north, south, east and west.
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */
public class CenterLayout implements LayoutManager2 {
	
	public static final String NORTH="NORTH";
	public static final String SOUTH="SOUTH";
	public static final String CENTER="CENTER";
	public static final String WEST="WEST";
	public static final String EAST="EAST";
	
	private Component n,s,c,w,e; 
	
	public void addLayoutComponent(String name, Component comp) {
		if (name.equals(NORTH)) { n=comp;return ;} 
		if (name.equals(SOUTH)) { s=comp;return; } 
		if (name.equals(WEST)) { w=comp;return; } 
		if (name.equals(EAST)) { e=comp;return; } 
		c=comp;
	}
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			int pw=parent.getWidth();
			int ph=parent.getHeight();
			if (c==null) return;
			Dimension cs=c.getPreferredSize();
			int vh=(ph-cs.height)/2;
			int hw=(pw-cs.width)/2;
			if (vh<0) vh=0; 
			if (hw<0) hw=0;
			//
			if (n!=null) n.setBounds(0,0,pw,vh);
			if (s!=null) s.setBounds(0,vh+cs.height,pw,vh);
			c.setBounds(hw,vh,cs.width,cs.height);			
			if (w!=null) w.setBounds(0,vh,hw,cs.height);
			if (e!=null) e.setBounds(hw+cs.width,vh,hw,cs.height);
			//
		}		
	}
	public Dimension minimumLayoutSize(Container parent) {
		return c.getPreferredSize();
	}
	public Dimension preferredLayoutSize(Container parent) {		
		return c.getPreferredSize();
	}
	public void removeLayoutComponent(Component comp) {
		if (comp==n) n=null;
		if (comp==e) e=null;
		if (comp==s) s=null;
		if (comp==w) w=null;
		if (comp==c) c=null;
	}

	public void addLayoutComponent(Component comp, Object constraints) {
		addLayoutComponent(constraints.toString(),comp);
	}
	
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }
	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}
	/**
     * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
     */
    public void invalidateLayout(Container target) {
    }
	/**
     * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
     */
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);
    }



}
