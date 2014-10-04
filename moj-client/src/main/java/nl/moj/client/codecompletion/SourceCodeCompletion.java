package nl.moj.client.codecompletion;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import nl.moj.client.codecompletion.statement.AbstractStatement;
import nl.moj.client.codecompletion.statement.ClassStatement;
import nl.moj.client.codecompletion.statement.DeclarationStatement;
import nl.moj.client.codecompletion.statement.FieldStatement;
import nl.moj.client.codecompletion.statement.ImportStatement;
import nl.moj.client.codecompletion.statement.InterfaceStatement;
import nl.moj.client.codecompletion.statement.JavaFile;
import nl.moj.client.codecompletion.statement.MethodStatement;
import nl.moj.client.codecompletion.statement.StatementVisitor;
import nl.moj.client.codecompletion.statement.VariableDeclarationStatement;

/**
 * Source Code Completion identifies possible identifiers that can be used
 */
public class SourceCodeCompletion implements CompletionSource {

    private SourceCodeParser mySource;

    public SourceCodeCompletion(String sourceCode) {
        //
        mySource = new SourceCodeParser(sourceCode);
        //
    }

    public SourceCodeCompletion(SourceCodeParser scc) {
        mySource = scc;
    }

    public CodeNode addToCodeTree(final CodeNodeFactory cnf, CodeNode root) {
        JavaFile f = mySource.getJavaFile();
        //
        ImportStatement[] istm = f.getImports();
        for (int t = 0; t < istm.length; t++) {
            if (istm[t].isWildcard()) continue;
            new TypeCodeCompletion(istm[t].getImport()).addToCodeTree(cnf, root);
        }
        //
        String pkg = f.getPackage().getPackage();
        final CodeNode base = cnf.addDotSeparated(pkg, root);
        final Map<CodeNode, String> superClasses = new TreeMap<CodeNode, String>();
        //
        mySource.visit(new StatementVisitor() {
            private Stack<CodeNode> stack = new Stack<CodeNode>();

            public void begin() {
                stack.push(base);
            }

            public void beginStatement(AbstractStatement st, int indent, boolean isLast) {
                CodeNode current = stack.peek();
                if (st.isClassStatement()) {
                    String name = ((ClassStatement) st).getFullyQualifiedName();
                    CodeNode clazz = cnf.addIfNotExist(current, name.substring(name.lastIndexOf('.') + 1), true);
                    stack.push(clazz);
                    superClasses.put(clazz, ((ClassStatement) st).getSuperClassName());
                } else if (st.isInterfaceStatement()) {
                    String name = ((InterfaceStatement) st).getFullyQualifiedName();
                    CodeNode interf = cnf.addIfNotExist(current, name.substring(name.lastIndexOf('.') + 1), true);
                    stack.push(interf);
                    String[] ifs = ((InterfaceStatement) st).getExtendedInterfaces();
                    for (int t = 0; t < ifs.length; t++) {
                        superClasses.put(interf, ifs[t]);
                    }
                } else if (st.isMethodStatement()) {
                    String typeName = ((DeclarationStatement) st.getParent()).findFullyQualifiedName(((MethodStatement) st).getType());
                    cnf.addReferenceIfNotExist(current, st.getName(), typeName, ((MethodStatement) st).isStatic(),((MethodStatement) st).isArray());
                } else if (st.isFieldStatement()) {
                    String typeName = ((DeclarationStatement) st.getParent()).findFullyQualifiedName(((FieldStatement) st).getType());
                    cnf.addReferenceIfNotExist(current, st.getName(), typeName, ((FieldStatement) st).isStatic(),((FieldStatement) st).isArray());
                }
            }

            public void onStatement(AbstractStatement st, int indent, boolean isLast) {
                if (stack.isEmpty()) return;
                CodeNode current = stack.peek();
                if (st.isMethodStatement()) {
                    String fqType = st.getOwningDeclaration().findFullyQualifiedName(((MethodStatement) st).getType());
                    cnf.addReferenceIfNotExist(current, st.getName(), fqType, ((MethodStatement) st).isStatic(),((MethodStatement) st).isArray());
                    MethodStatement m = (MethodStatement) st;
                    for (int t = 0; t < m.getParams().length; t++) {
                        String paramType = st.getOwningDeclaration().findFullyQualifiedName(m.getParams()[t].getType());
                        cnf.addDotSeparatedLazy(paramType, base);
                    }
                } else if (st.isFieldStatement()) {
                    String fqType = st.getOwningDeclaration().findFullyQualifiedName(((FieldStatement) st).getType());
                    cnf.addReferenceIfNotExist(current, st.getName(), fqType, ((FieldStatement) st).isStatic(),((FieldStatement) st).isArray());
                } else if (st.isVariableDeclarationStatement()) {
                    VariableDeclarationStatement v = (VariableDeclarationStatement) st;
                    String fqType = v.getOwningDeclaration().findFullyQualifiedName(v.getType());
                    cnf.addReferenceIfNotExist(current, st.getName(), fqType, false,((VariableDeclarationStatement) st).isArray());
                }
            }

            public void endStatement(AbstractStatement st, int indent, boolean isLast) {
                if (st.isClassStatement()) {
                    stack.pop();
                } else if (st.isInterfaceStatement()) {
                    stack.pop();
                }
            }

            public void end() {
            }
        });
        //
        for (CodeNode cn : superClasses.keySet()) {
            String s = superClasses.get(cn);
            if (!root.containsPath(s)) {
                TypeCodeCompletion cc = new TypeCodeCompletion(s);
                cc.addToCodeTree(cnf, root);
            }
            cn.addLink(root.findPath(s));
        }
        //
        return base;
    }

}
