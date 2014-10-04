package nl.moj.client.codecompletion.statement;

/**
 *
 */
public interface Statement {

	public static final String M_PUBLIC="public";
	public static final String M_PROTECTED="protected";
	public static final String M_PRIVATE="private";
	public static final String M_STATIC="static";
	public static final String M_FINAL="final";
	public static final String M_ABSTRACT="abstract";
	public static final String M_FPSTRICT="fpstrict";
	public static final String M_VOLATILE="volatile";
	public static final String M_TRANSIENT="transient";
	public static final String M_NATIVE="native";
	
	public static final String P_DOUBLE="double";
	public static final String P_INT="int";
	public static final String P_BOOLEAN="boolean";
	public static final String P_LONG="long";
	public static final String P_BYTE="byte";
	public static final String P_FLOAT="float";
	public static final String P_VOID="void";
	public static final String P_SHORT="short";
	public static final String P_CHAR="char";  
      
	public static final String K_ELSE="else";
	public static final String K_INTERFACE="interface";
	public static final String K_SUPER="super";
	public static final String K_BREAK="break";
	public static final String K_EXTENDS="extends";
	public static final String K_SWITCH="switch";
	public static final String K_SYNCHRONIZED="synchronized";
	public static final String K_CASE="case";
	public static final String K_FINALLY="finally";
	public static final String K_NEW="new";
	public static final String K_THIS="this";
	public static final String K_CATCH="catch";
	public static final String K_PACKAGE="package";
	public static final String K_THROW="throw";
	public static final String K_FOR="for";
	public static final String K_THROWS="throws";
	public static final String K_CLASS="class";
	public static final String K_ENUM="enum";
	public static final String K_GOTO="goto";
	public static final String K_CONST="const";
	public static final String K_IF="if";
	public static final String K_TRY="try";
	public static final String K_CONTINUE="continue";
	public static final String K_IMPLEMENTS="implements";
	public static final String K_RETURN="return";
	public static final String K_DEFAULT="default";
	public static final String K_IMPORT="import";
	public static final String K_DO="do";
	public static final String K_INSTANCEOF="instanceof";
	public static final String K_WHILE="while";

	public static final String[] MODIFIERS = new String[] {
		M_PUBLIC,M_PROTECTED,M_PRIVATE,M_STATIC,M_FINAL,M_FPSTRICT,M_VOLATILE,M_TRANSIENT,M_ABSTRACT,M_NATIVE,K_SYNCHRONIZED
	};
	
	public static final String[] PRIMITIVES = new String[] {
		P_DOUBLE,P_INT,P_BOOLEAN,P_LONG,P_BYTE,P_FLOAT,P_VOID,P_SHORT,P_CHAR
	};
	
	public static final String[] COMMANDS = new String[] {
		K_ELSE,K_INTERFACE,K_SUPER,
		K_BREAK,K_EXTENDS,K_SWITCH,
		K_SYNCHRONIZED,K_CASE,K_FINALLY,
		K_NEW,K_THIS,K_CATCH,K_PACKAGE,K_THROW,K_FOR,K_THROWS,
		K_CLASS,K_GOTO,K_CONST,
		K_IF,K_TRY,K_CONTINUE,K_IMPLEMENTS,
		K_RETURN,K_DEFAULT,K_IMPORT,K_DO,K_INSTANCEOF,K_WHILE
	};

	public static final String[] O_OPERATORS=new String[] {
		"=","+","-","*","/","%","&","|","<",">"
	};

	public void visit(StatementVisitor v,int indent,boolean isLast);
	
	public Statement getParent();
	
	public DeclarationStatement getOwningDeclaration();

	public boolean isCompound();	
	public boolean isClassStatement();
	public boolean isInterfaceStatement();
	public boolean isConstructorStatement(); 
	public boolean isMethodStatement();
	public boolean isFieldStatement();
	public boolean isPackageStatement();
	public boolean isImportStatement();
	public boolean isIfStatement();
	public boolean isEnumStatement();

	public int getPosition();

}
