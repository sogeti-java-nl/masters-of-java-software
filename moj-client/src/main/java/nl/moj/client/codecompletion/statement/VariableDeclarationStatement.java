package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class VariableDeclarationStatement extends AbstractCompoundStatement {

	private AbstractStatement myParent;
	private String myType;
	private String myName;

	public VariableDeclarationStatement(AbstractStatement parent,int pos,String[] words) {
		super(words,pos);
		myParent=parent;
		myType=words[0];
        if (words[1].equals("[]")) {
        	myType=myType+"[]";
        	myName=words[2];
        } else {
        	myName=words[1];
        }
	}
    
	public Statement getParent() {
		return myParent;
	}
	
	public String getName() {
		return myName;
	}
	
    public String getType() {
		Statement p=this;
		while (p.getParent()!=null) {
			p=p.getParent();
		}
		if (isArray()) {
			return ((JavaFile)p).expandImport(myType.substring(0,myType.length()-2))+"[]";
		} else {
			return ((JavaFile)p).expandImport(myType);
		}	
    }

	
    public boolean isArray() {
    	return myType.endsWith("[]");
    }
	
	
	public Declaration getDeclaration() {
		return new Declaration(getType(),getName(),isArray());
	}

	public static boolean qualifies(String[] words) {
		if (words.length<2) return false;
		//
		if (isModifier(words[0])) return false;
		if (isModifier(words[1])) return false;
		if (isPrimitive(words[1])) return false;
		if (isCommand(words[0])) return false;
		if (isCommand(words[1])) return false;
		//
		if (words.length>2) {			
			if (words[1].equals("[]")) {
				if (!words[3].equals("=")) return false;
			} else {
				if (!words[2].equals("=")) return false;
			}
		}
		//
		if (isPrimitive(words[0])) {
			return true;				
		} else {
			// Should be type.
			return true;
		}
	}
	
	public String toString() {
		return "VariableDeclaration("+myName+" of type "+getType()+")";
	}

}
