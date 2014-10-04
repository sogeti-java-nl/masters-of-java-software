package nl.moj.client.codecompletion.statement;

/**
 *
 */
public class ConstructorStatement extends DeclarationStatement {

	private ClassStatement myParent;

    public ConstructorStatement(ClassStatement parent,int pos,String[] words) {
        super(words,pos);
        myParent=parent;
    }

	public static boolean qualifies(String[] words) {
		int cnt=0;
		// Skip any modifiers.
		while ((cnt<words.length)&&(isModifier(words[cnt]))) cnt++;
		if (cnt>words.length-1) return false;
		// Next should not be a type and not a primitive.
		if (isPrimitive(words[cnt])) return false;
		//
		// Check for opening and closing brackets.
		// 
		String constructor=words[cnt];
		if (constructor.indexOf("(")<0) return false;    	
		if (constructor.indexOf(")")<0) return false;
		//
		return true;		
	}	
	
	public Statement getParent() {
		return myParent;
	}	
	
	public String getName() {
		return ((ClassStatement)getParent()).getName();
	}

}
