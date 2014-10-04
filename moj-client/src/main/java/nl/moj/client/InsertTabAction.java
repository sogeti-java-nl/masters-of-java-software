package nl.moj.client;

import java.awt.event.ActionEvent;

import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

/**
 * Custom InsertTabAction in order to insert space characters instead of tabs.
 * @author E.Hooijmeijer
 */
public class InsertTabAction extends TextAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5955302645199745952L;
	private String tabChars;
    /**
     * Creates this object with the appropriate identifier.
     */
    public InsertTabAction(String tabChars) {
        super(DefaultEditorKit.insertTabAction);
        this.tabChars=tabChars;
    }

    /**
     * The operation to perform when this action is triggered.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
            if ((!target.isEditable()) || (!target.isEnabled())) {
                UIManager.getLookAndFeel().provideErrorFeedback(target);
                return;
            }
            target.replaceSelection(tabChars);
        }
    }
}