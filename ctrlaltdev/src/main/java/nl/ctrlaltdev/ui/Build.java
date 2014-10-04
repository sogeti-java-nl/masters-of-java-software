package nl.ctrlaltdev.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Fast UI Composition classes.
 * @author E.Hooijmeijer / (C) 2003-2004 E.Hooijmeijer / Licence : LGPL 2.1   
 */
public class Build {
	
	public interface Visitor {
		public void visit(JComponent c);
	}
	
	public static class Spacer extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5056315307105251714L;

		public Spacer(int dx,int dy) {
			super();
			this.setPreferredSize(new Dimension(dx,dy));
		}
	}

	/** left flow panel */
	public static class LFP extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4984320821837550630L;
		public LFP(JComponent c) {
			super(new FlowLayout(FlowLayout.LEFT));
			this.add(c);
		}
		public LFP(JComponent c,Color background) {
			this(c);
			setBackground(background);
		}		
		public LFP(JComponent c1,JComponent c2) {
			super(new FlowLayout(FlowLayout.LEFT));
			this.add(c1);
			this.add(c2);
		}
		public LFP(JComponent[] c) {
			super(new FlowLayout(FlowLayout.LEFT));
			for (int t=0;t<c.length;t++) this.add(c[t]);
		}
		public LFP(JComponent[] c,Color background) {
			super(new FlowLayout(FlowLayout.LEFT));
			for (int t=0;t<c.length;t++) this.add(c[t]);
			setBackground(background);
		}		
	}
	
	/** right flow panel */
	public static class RFP extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9059487918276024977L;
		public RFP(JComponent c) {
			super(new FlowLayout(FlowLayout.RIGHT));
			this.add(c);
		}
		public RFP(JComponent c,Color background) {
			this(c);
			setBackground(background);
		}
		public RFP(JComponent c1,JComponent c2) {
			super(new FlowLayout(FlowLayout.RIGHT));
			this.add(c1);
			this.add(c2);
		}
		public RFP(JComponent[] c) {
			super(new FlowLayout(FlowLayout.RIGHT));
			for (int t=0;t<c.length;t++) this.add(c[t]);
		}
		public RFP(JComponent[] c,Color background) {
			super(new FlowLayout(FlowLayout.RIGHT));
			for (int t=0;t<c.length;t++) this.add(c[t]);
			setBackground(background);
		}
	}

	/** center flow panel */
	public static class CFP extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1000341092082051892L;
		public CFP(JComponent c) {
			super(new FlowLayout(FlowLayout.CENTER));
			this.add(c);
		}
		public CFP(JComponent c,Color background) {
			this(c);
			setBackground(background);
		}
		public CFP(JComponent c1,JComponent c2) {
			super(new FlowLayout(FlowLayout.CENTER));
			this.add(c1);
			this.add(c2);
		}
		public CFP(JComponent[] c) {
			super(new FlowLayout(FlowLayout.CENTER));
			for (int t=0;t<c.length;t++) this.add(c[t]);
		}
		public CFP(JComponent[] c,Color background) {
			super(new FlowLayout(FlowLayout.CENTER));
			for (int t=0;t<c.length;t++) this.add(c[t]);
			setBackground(background);
		}
	}

	/** North positioned Box layout */
	public static class NBOXY extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 970106037671107959L;

		public NBOXY(JComponent[] c) {
			super(new BorderLayout());
			Box p=new Box(BoxLayout.Y_AXIS);
			this.add(p,BorderLayout.NORTH);
			for (int t=0;t<c.length;t++) p.add(c[t]);
		}
	}
	
	/** Box layout with X align */
	public static class BOXX extends Box {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7726688275632058063L;

		public BOXX(JComponent[] c) {
			super(BoxLayout.X_AXIS);
			for (int t=0;t<c.length;t++) { 			
				if (c[t]!=null) this.add(c[t]);
				else this.add(Box.createHorizontalStrut(8));
			}
		}	
	}

	/** Box layout with Y align */
	public static class BOXY extends Box {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6885710657764951070L;

		public BOXY(JComponent[] c) {
			super(BoxLayout.Y_AXIS);
			for (int t=0;t<c.length;t++) { 			
				if (c[t]!=null) this.add(c[t]);
				else this.add(Box.createVerticalStrut(8));
			}
		}	
	}

	/** North,Center,South panel */	
	public static class NCS extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3148875538206119447L;
		public NCS(JComponent n,JComponent c) {
			this(n,c,null);
		}
		public NCS(JComponent n,JComponent c,JComponent s) {
			super(new BorderLayout());
			if (n!=null) this.add(n,BorderLayout.NORTH);
			if (c!=null) this.add(c,BorderLayout.CENTER);
			if (s!=null) this.add(s,BorderLayout.SOUTH);
		}
	}
	
	/** West,Center,East panel */
	public static class WCE extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 5629516814177067439L;
		public WCE(JComponent w,JComponent c) {
			this(w,c,null);
		}
		public WCE(JComponent w,JComponent c,JComponent e) {
			super(new BorderLayout());
			if (w!=null) this.add(w,BorderLayout.WEST);
			if (c!=null) this.add(c,BorderLayout.CENTER);
			if (e!=null) this.add(e,BorderLayout.EAST);
		}
	}
	
	/** Grid Layout */
	public static class Grid extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5794631736064773651L;

		public Grid(int dx,int dy,JComponent[] c) {
			super(new GridLayout(dx,dy));
			for (int t=0;t<c.length;t++) { 			
				if (c[t]!=null) this.add(c[t]);
				else this.add(new JLabel());
			}
		}
	}
	
	public static class NWCES extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1236521582285138614L;

		public NWCES(JComponent n,JComponent w,JComponent c,JComponent e,JComponent s) {
			super(new BorderLayout());
			if (n!=null) this.add(n,BorderLayout.NORTH);
			if (w!=null) this.add(w,BorderLayout.WEST);
			if (c!=null) this.add(c,BorderLayout.CENTER);
			if (e!=null) this.add(e,BorderLayout.EAST);
			if (s!=null) this.add(s,BorderLayout.SOUTH);
		}
	}

	public static class CP extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1314392745731402102L;

		public CP(JComponent n,JComponent w,JComponent c,JComponent e,JComponent s) {
			super(new CenterLayout());
			if (n!=null) add(n,CenterLayout.NORTH);
			if (w!=null) add(w,CenterLayout.WEST);
			if (c!=null) add(c,CenterLayout.CENTER);
			if (e!=null) add(e,CenterLayout.EAST);
			if (s!=null) add(s,CenterLayout.SOUTH);
		}
	}
	
	/** visitor for component trees */
	public static void visit(JComponent root,Visitor v) {
		v.visit(root);
		Component[] c=root.getComponents();
		for (int t=0;t<c.length;t++) {
			if (c[t] instanceof JComponent) {
				visit((JComponent)c[t],v);
			}
		}
	}

}
