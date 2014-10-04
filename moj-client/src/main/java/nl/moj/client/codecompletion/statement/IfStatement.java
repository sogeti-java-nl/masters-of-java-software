package nl.moj.client.codecompletion.statement;

/**
 * 
 */
public class IfStatement extends AbstractCompoundStatement {

	private Statement parent;

	public IfStatement(Statement parent,int pos,String[] words) {
		super(words,pos);
		this.parent=parent;
	}

	public String getName() {
		return null;
	}

	public Statement getParent() {
		return parent;
	}

	public static boolean qualifies(String[] words) {
		if (words.length<2) return false;
		return "if".equals(words[0]);
	}

}
