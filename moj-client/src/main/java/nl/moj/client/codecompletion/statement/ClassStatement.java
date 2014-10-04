package nl.moj.client.codecompletion.statement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ClassStatement extends DeclarationStatement {

    private String myName;
    private String mySuperClass;
    private String[] myInterfaces;
    private CompoundStatement myParent;

    public ClassStatement(CompoundStatement parent, int pos, String[] words) {
        super(words, pos);
        myName = null;
        mySuperClass = null;
        myParent = parent;
        //
        boolean isImplements = false;
        boolean isClass = false;
        boolean isExtends = false;
        List<String> interfaces = new ArrayList<String>();
        for (int t = 0; t < words.length; t++) {
            if (isModifier(words[t])) {
                //
                // Ok. Go on.
                //
            } else if (getClassWord().equals(words[t])) {
                isClass = true;
            } else if (K_EXTENDS.equals(words[t])) {
                isExtends = true;
                isClass = false;
            } else if (K_IMPLEMENTS.equals(words[t])) {
                isImplements = true;
                isExtends = false;
                isClass = false;
            } else {
                if (isClass) {
                    if (myName != null) throw new StatementException("Double Class name : " + this);
                    myName = words[t];
                } else if (isExtends) {
                    if (mySuperClass != null) throw new StatementException("Two Parent Classes : " + this);
                    mySuperClass = words[t];
                } else if (isImplements) {
                    interfaces.add(words[t]);
                } else
                    throw new StatementException("Incorrectly formatted Class Statement : " + this);
            }
        }
        myInterfaces = interfaces.toArray(new String[interfaces.size()]);
        //
        if (myName == null) throw new StatementException("Missing name : " + this);
        if (mySuperClass == null) mySuperClass = "java.lang.Object";
        //      
    }

    protected void setSuperClass(String s) {
        if (s == null) s = "java.lang.Object";
        mySuperClass = s;
    }

    protected String getClassWord() {
        return K_CLASS;
    }

    public boolean isInnerClass() {
        return getParent() instanceof ClassStatement;
    }

    public String getName() {
        return myName;
    }

    public String getSuperClassName() {
        return getJavaFile().expandImport(mySuperClass);
    }

    public String[] getInterfaceNames() {
        String[] tmp = new String[myInterfaces.length];
        for (int t = 0; t < myInterfaces.length; t++) {
            tmp[t] = getJavaFile().expandImport(myInterfaces[t]);
        }
        return tmp;
    }

    private JavaFile getJavaFile() {
        Statement p = this;
        while (p.getParent() != null) {
            p = p.getParent();
        }
        return (JavaFile) p;
    }

    public ClassStatement[] getClasses() {
        List<ClassStatement> results = new ArrayList<ClassStatement>();
        scan(results, ClassStatement.class);
        return results.toArray(new ClassStatement[results.size()]);
    }

    public InterfaceStatement[] getInterfaces() {
        List<InterfaceStatement> results = new ArrayList<InterfaceStatement>();
        scan(results, InterfaceStatement.class);
        return results.toArray(new InterfaceStatement[results.size()]);
    }

    public DeclarationStatement[] getTypes() {
        List<DeclarationStatement> results = new ArrayList<DeclarationStatement>();
        scan(results, ClassStatement.class);
        scan(results, InterfaceStatement.class);
        return results.toArray(new DeclarationStatement[results.size()]);
    }

    public static boolean qualifies(String[] words) {
        return contains(words, K_CLASS);
    }

    public Statement getParent() {
        return myParent;
    }

    public Declaration getDeclaration() {
        return new Declaration(getFullyQualifiedName(), getName(),false);
    }

}
