package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class StatementException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7689055625224177326L;
	protected StatementException() {
		super();
	}
	
    public StatementException(String msg) {
        super(msg);
    }
    public StatementException(String msg,Throwable t) {
    	super(msg,t);
    }

}
