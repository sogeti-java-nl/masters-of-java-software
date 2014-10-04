package nl.moj.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 * Wrapper around an ActionLister to ask the user if he really wants to do this action.
 * @author E.Hooijmeijer
 */
public class ConfirmingActionListener implements ActionListener {
	
	private JComponent myRef;
	private ActionListener myDelegate;
	
	public ConfirmingActionListener(JComponent ref,ActionListener delegate) {
		myRef=ref;
		myDelegate=delegate;
	}	

	public void actionPerformed(ActionEvent e) {
		if (JOptionPane.showConfirmDialog(myRef,"Are you Sure ?",e.getActionCommand(),JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
			myDelegate.actionPerformed(e);
		}
    }

}
