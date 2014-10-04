package nl.moj.client.codecompletion.statement;

import nl.ctrlaltdev.util.Tool;

public class EnumStatement extends ClassStatement {

	public EnumStatement(CompoundStatement parent,int pos,String[] words) {
		super(parent,pos,words);
		setSuperClass("java.lang.Enum");
	}
	
	public static boolean qualifies(String[] words) {		
		return contains(words,K_ENUM);
	}    
	
    protected String getClassWord() {
    	return K_ENUM;
    }
	
    public void makeEnumList(String[] warr,int pos) {
    	String[] ident=Tool.cut(warr[0],",");
    	for (int t=0;t<ident.length;t++) {
    		//
    		String name=ident[t];
    		if (name.indexOf("(")>=0) {
    			name=name.substring(0,name.indexOf("("));
    		}
    		if (name.indexOf(")")>=0) continue;
    		//
    		this.addStatement(
    				new FieldStatement(this,pos,new String[] { "public","static","final",getName(),name })
    		);
    	}
    }
    
}
