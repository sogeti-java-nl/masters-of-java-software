package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class FieldStatement extends DeclarationStatement {

	private String myType;
	private String myName;
	private DeclarationStatement myParent;

    public FieldStatement(DeclarationStatement parent,int pos,String[] words) {
        super(words,pos);
        int cnt=0;
		// Skip any modifiers.
		while ((cnt<words.length)&&(isModifier(words[cnt]))) cnt++;
		//
		myType=words[cnt];
		cnt++;
		if (words[cnt].equals("[]")) {
			myType=myType+"[]";
			cnt++;
		}
		if (words[cnt].equals("<")) {
			myType=myType+"<"+words[cnt+1]+">";
			cnt+=3;
		}
		myName=words[cnt];
		//
		myParent=parent;
    }
    
    public boolean isArray() {
    	return myType.endsWith("[]");
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
    
    public String getName() {
		return myName;    	
    }
    
    public Declaration getDeclaration() {
    	return new Declaration(getType(),getName(),isArray());
    }

	public static boolean qualifies(String[] words) {
		//
		int cnt=0;
		// Skip any modifiers.
		while ((cnt<words.length)&&(isModifier(words[cnt]))) cnt++;
		//
		if (cnt>words.length-2) return false;
		//
		if (!isPrimitive(words[cnt])) {
			//
			// Should be class.
			//
		}
		//
		// Should not contain any brackets.
		//
		if (words[cnt+1].indexOf('(')>=0) return false;
		//
		return true;
	}
	
	public String toString() {
		return "Field("+myName+" of "+myType+")";
	}
	
	public Statement getParent() {
		return myParent;
	}

}
