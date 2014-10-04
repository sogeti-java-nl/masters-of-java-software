package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class StaticStatement extends AbstractCompoundStatement {

	private ClassStatement myParent; 

    public StaticStatement(ClassStatement stm,int pos,String[] words) {
        super(words,pos);
        myParent=stm;
    }

	public static boolean qualifies(String[] words) {
		if (words.length!=1) return false;
		return words[0].equals(M_STATIC);
	}

	public Statement getParent() {
        return myParent;
    }

	public String getName() {
		return "";
	}



}
