package nl.moj.client.codecompletion.statement;

/**
 * 
 */
public class Declaration {

	private String myType;
	private boolean isArray;
	private String myName;

	public Declaration(String type,String name,boolean isArray) {
		myType=type;
		myName=name;
		this.isArray=isArray;
	}
	public Declaration(String combined) {
		myType=combined.substring(0,combined.lastIndexOf(' '));
		myName=combined.substring(combined.lastIndexOf(' ')+1);
		isArray=myType.endsWith("[]");
		if (isArray) myType=myType.substring(0,myType.length()-2).trim();
	}
	
	public boolean isArray() {
		return isArray;
	}
	
	public String getName() { 
		return myName;
	}
	public String getType() {
		return myType;
	}
	
	public String toString() {
		return "Declaration("+myType+(isArray?"[]":"")+" "+myName+")";
	}

}
