package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class UnimplementedStatement extends AbstractCompoundStatement {

	private AbstractStatement myParent;

    public UnimplementedStatement(AbstractStatement parent,int pos,String[] words) {
        super(words,pos);
        myParent=parent;
    }
    
    public Statement getParent() {
        return myParent;
    }
    
	public String getName() {
		return "";
	}


}
