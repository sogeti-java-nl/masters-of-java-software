package nl.moj.client;

import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.undo.UndoManager;

/**
 *
 */
public class JavaUndoManager extends UndoManager {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7942890392724400831L;

	public JavaUndoManager() {
        super();
        setLimit(16);
    }
    
    public boolean isInsertOrDelete() {
    	AbstractDocument.DefaultDocumentEvent ev=(AbstractDocument.DefaultDocumentEvent)editToBeRedone();
    	if (ev==null) return false;
		return (DocumentEvent.EventType.INSERT.equals(ev.getType()))||(DocumentEvent.EventType.REMOVE.equals(ev.getType()));
    }

}
