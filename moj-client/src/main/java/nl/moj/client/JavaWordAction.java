package nl.moj.client;

import java.awt.event.ActionEvent;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

public class JavaWordAction extends TextAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8168389780104382649L;
	private String seps;
    private boolean select;
    private boolean forward;
	
    public JavaWordAction(String name,boolean select,boolean forward) {
        super(name);
        this.select = select;
        this.forward=forward;
        seps=" .,;=()\n'\"";
    }

    /** The operation to perform when this action is triggered. */
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
            try {
                int offs = target.getCaretPosition();
                int begOffs = (forward?getWordStart(target, offs):getWordEnd(target, offs));
                if (select) {
                    target.moveCaretPosition(begOffs);
                } else {
                    target.setCaretPosition(begOffs);
                }
            } catch (BadLocationException bl) {
            	UIManager.getLookAndFeel().provideErrorFeedback(target);
            }
        }
    }

    public int getWordStart(JTextComponent c, int offs) throws BadLocationException {
    	Document doc = c.getDocument();
    	//
    	String text=doc.getText(0,doc.getLength());
    	//
        int wordPosition = offs;
        boolean isEnd=false;
        if (wordPosition<text.length()-1) do {
       		wordPosition++;
       		//
       		char cc=text.charAt(wordPosition);
       		char pc=text.charAt(wordPosition-1);
       		boolean csc=isSeparatorChar(cc);
       		boolean psc=isSeparatorChar(pc);
       		//
       		isEnd=(!csc&&psc)||(csc&&psc&&(cc!=pc)||(csc&&!psc));
       		//
        } while ((wordPosition<text.length()-1)&&(!isEnd));
        //
        return wordPosition;
    }

    public int getWordEnd(JTextComponent c, int offs) throws BadLocationException {
    	Document doc = c.getDocument();
    	//
    	String text=doc.getText(0,doc.getLength());
    	//
        int wordPosition = offs;
        boolean isEnd=false;
        while ((wordPosition>0)&&(!isEnd)) {
       		wordPosition--;
       		//
      		char pc=(wordPosition>0?text.charAt(wordPosition-1):'\n');
       		char cc=text.charAt(wordPosition);
       		boolean csc=isSeparatorChar(pc);
       		boolean psc=isSeparatorChar(cc);
       		//
       		isEnd=(!csc&&psc)||(csc&&psc&&(pc!=cc)||(csc&&!psc));
       		//
        } 
        //
    	return wordPosition;
    }

    protected boolean isSeparatorChar(char c) {
    	return seps.indexOf(c)>=0;
    }
    
}
