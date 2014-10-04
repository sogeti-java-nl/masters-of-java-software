package nl.ctrlaltdev.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Replacement class for LightweightPopup which cannot have keyboard focus.
 */

public class FocusablePopup extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2719030957872862843L;
	private JFrame myParent;
	private JComponent myContents;

    protected FocusablePopup(JFrame parent,JComponent contents) {
        super(new BorderLayout());
        this.add(contents,BorderLayout.CENTER);
		Dimension d=contents.getSize();
		if (d.width==0) d.width=256;
		if (d.height==0) d.height=128;
		this.setSize(d);
        myParent=parent;
        myContents=contents;
    }
    
    public JComponent getContents() {
    	return myContents;
    }

	public void closePopup() {
		myParent.getLayeredPane().remove(this);
		myParent.repaint();
	}
		  
	/** 
	 * creates a new (lightweight) popup at the specified coordinates.
	 * @param owner the owning component.
	 * @param contents the contents of the popup
	 * @param x the x coordinate of the popup relative to the frame 
	 * @param y the y coordinate of the popup relative to the frame 
	 */   
    public static FocusablePopup getPopup(JComponent owner,JComponent contents,int x,int y) {
		JFrame parentFrame=(JFrame)JOptionPane.getFrameForComponent(owner);
		FocusablePopup fp=new FocusablePopup(parentFrame,contents);
		parentFrame.getLayeredPane().add(fp,JLayeredPane.POPUP_LAYER);
		fp.setLocation(x,y);
		return fp;    	
    }

/*	public static void main(String[] args) throws Throwable {
		final JFrame tmp=new JFrame("POPUPTest");
		final JPanel editor=new JPanel();
		tmp.setSize(512,512);
		tmp.getContentPane().add(editor);
		tmp.show();
		tmp.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
            }
		});
		final FocusablePopup p=getPopup(editor,new JTextArea(),256,256);
		//
		Thread.sleep(2000);
		//
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//
				p.closePopup();
				//
			}
		});
	}*/

}
