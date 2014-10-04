package nl.moj.client.codecompletion.statement;

public class ReturnStatement extends AbstractStatement {

	private AbstractStatement myParent;

    public ReturnStatement(AbstractStatement parent,int pos,String[] words) {
        super(words,pos);
        myParent=parent;
    }

	public String getName() {
		return myWords[1];
	}
    
	public static boolean qualifies(String[] words) {
		if (words.length<1) return false;
		if (!words[0].equals(K_RETURN)) return false;
		return true;
	} 
	
	public Statement getParent() {
        return myParent;
    }

}
