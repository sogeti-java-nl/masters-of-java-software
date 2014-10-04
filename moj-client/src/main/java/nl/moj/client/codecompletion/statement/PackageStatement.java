package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class PackageStatement extends AbstractStatement {

	private JavaFile myParent;

    public PackageStatement(JavaFile parent,int pos,String[] words) {
        super(words,pos);
        myParent=parent;
    }
    
    public String getPackage() {
    	return myWords[1];
    }
    
	public String getName() {
		return myWords[1];
	}
    
    public static boolean qualifies(String[] words) {
    	if (words.length!=2) return false;
    	if (!words[0].equals(K_PACKAGE)) return false;
    	return true;
    }

	public Statement getParent() {
		return myParent;
	}

}
