package nl.moj.client.codecompletion.statement;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class InterfaceStatement extends DeclarationStatement {

    private String myName;
    private String[] myInterfaces;
    private CompoundStatement myParent;

    public InterfaceStatement(CompoundStatement parent, int pos, String[] words) {
        super(words, pos);
        myParent = parent;
        //
        boolean isInterface = false;
        boolean isExtends = false;
        List<String> interfaces = new ArrayList<>();
        for (int t = 0; t < words.length; t++) {
            if (isModifier(words[t])) {
                //
                // Ok. Go on.
                //
            } else if (K_INTERFACE.equals(words[t])) {
                isInterface = true;
            } else if (K_EXTENDS.equals(words[t])) {
                isExtends = true;
            } else {
                if (isInterface) {
                    myName = words[t];
                    isInterface = false;
                } else if (isExtends) {
                    interfaces.add(words[t]);
                } else
                    throw new StatementException("Incorrectly formatted Interface Statement : " + this);
            }
        }
        myInterfaces = interfaces.toArray(new String[interfaces.size()]);
        for (int t = 0; t < myInterfaces.length; t++) {
            myInterfaces[t] = getJavaFile().expandImport(myInterfaces[t]);
        }
        //
        if (myName == null)
            throw new StatementException("Missing name : " + this);
        //
    }

    private JavaFile getJavaFile() {
        Statement p = this;
        while (p.getParent() != null) {
            p = p.getParent();
        }
        return (JavaFile) p;
    }

    public String getName() {
        return myName;
    }

    public String[] getExtendedInterfaces() {
        return myInterfaces;
    }

    public static boolean qualifies(String[] words) {
        return contains(words, K_INTERFACE);
    }

    public Statement getParent() {
        return myParent;
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

}
