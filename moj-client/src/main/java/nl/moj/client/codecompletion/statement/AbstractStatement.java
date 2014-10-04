package nl.moj.client.codecompletion.statement;

/**
 *
 */
public abstract class AbstractStatement implements Statement {


    protected String[] myWords;
    protected int myPos;

    public AbstractStatement(String[] words,int pos) {
        super();
        myWords=words;
        myPos=pos;
    }
    public int getPosition() {
        return /*getParent().getPosition()*/myPos;
    }
    
    public String getStatement() { 
        StringBuffer sb=new StringBuffer();
        for (int t=0;t<myWords.length;t++) {
            if ((t>0)&&(!contains(O_OPERATORS,myWords[t])&&(!contains(O_OPERATORS,myWords[t-1])))) sb.append(" ");
            sb.append(myWords[t]);
        }
        return sb.toString();
    }
    
    /** returns true if any of the words in the statement exacty matches the word */
    protected boolean contains(String word) {
        return contains(myWords,word);

    }
    /** returns true if any of the words in the statement exacty matches any of the words in the array. */
    protected boolean containsAny(String[] anyOf) {
        for (int y=0;y<anyOf.length;y++) {
            for (int t=0;t<myWords.length;t++) {
                if (myWords[t].equals(anyOf[y])) return true;
            }
        }       
        return false;
    }
    /** returns true if the specified word is a modifier */
    protected static boolean isModifier(String word) {
        for (int t=0;t<MODIFIERS.length;t++) {
            if (MODIFIERS[t].equals(word)) return true;
        }       
        return false;
    }
    /** returns true if the specified word is a primitive */
    protected static boolean isPrimitive(String word) {
        for (int t=0;t<PRIMITIVES.length;t++) {
            if (PRIMITIVES[t].equals(word)) return true;
        }       
        return false;
    }    
    /** returns true if the specified word is a primitive */
    protected static boolean isCommand(String word) {
        for (int t=0;t<COMMANDS.length;t++) {
            if (COMMANDS[t].equals(word)) return true;
        }       
        return false;
    }   
    protected static boolean contains(String[] words,String word) {
        for (int t=0;t<words.length;t++) {
            if (words[t].equals(word)) return true;
        }       
        return false;
    }
    
    public abstract String getName();

    public boolean isCompound() { return this instanceof AbstractCompoundStatement; }   
    public boolean isClassStatement() { return this instanceof ClassStatement; }
    public boolean isInterfaceStatement() { return this instanceof InterfaceStatement; }
    public boolean isConstructorStatement() { return this instanceof ConstructorStatement;} 
    public boolean isMethodStatement() { return this instanceof MethodStatement; }
    public boolean isFieldStatement() { return this instanceof FieldStatement; }
    public boolean isPackageStatement() { return this instanceof PackageStatement; }
    public boolean isImportStatement() { return this instanceof ImportStatement; }
    public boolean isVariableDeclarationStatement() { return this instanceof VariableDeclarationStatement; }
    public boolean isReturnStatement() { return this instanceof ReturnStatement; }
    public boolean isIfStatement() { return this instanceof IfStatement; }
    public boolean isEnumStatement() { return this instanceof EnumStatement; }
    
    public void visit(StatementVisitor v,int indent,boolean isLast) {
        v.onStatement(this,indent,isLast);
    }
    
    public String toString() {
        StringBuffer sb=new StringBuffer();
        sb.append("Statement(");
        for (int t=0;t<myWords.length;t++) {
            sb.append(myWords[t]);
            sb.append(" ");
        }
        sb.append(")");
        return sb.toString();
    }
    
    public DeclarationStatement getOwningDeclaration() {
        Statement st=getParent();
        if (st==null) return null;
        if (st.isMethodStatement()) return (DeclarationStatement)st;
        if (st.isClassStatement()) return (DeclarationStatement)st;
        if (st.isInterfaceStatement()) return (DeclarationStatement)st;
        return st.getOwningDeclaration();
    }

}
