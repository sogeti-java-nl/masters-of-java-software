package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class ImportStatement extends AbstractStatement {

	private JavaFile myParent;

    public ImportStatement(JavaFile parent,int pos,String[] words) {
        super(words,pos);
        myParent=parent;
    }

	public String getImport() {
		return myWords[1]+(isWildcard()?"*":"");
	}
	
	public String getName() {
		return myWords[1]+(isWildcard()?"*":"");
	}
	
	public boolean isWildcard() {
		return (myWords.length==3);
	}
    
	public static boolean qualifies(String[] words) {
		if ((words.length!=2)&&(words.length!=3)) return false;
		if ((words.length==3)&&(!words[2].equals("*"))) return false;
		if (!words[0].equals(K_IMPORT)) return false;
		return true;
	} 
	
	public Statement getParent() {
        return myParent;
    }

}
