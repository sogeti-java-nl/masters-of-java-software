package nl.moj.client.codecompletion;

import java.util.Set;
import java.util.TreeSet;

import nl.ctrlaltdev.util.Tool;
import nl.moj.client.codecompletion.CodeNode.BranchNode;
import nl.moj.client.codecompletion.CodeNode.ReferenceNode;
import nl.moj.client.codecompletion.statement.Declaration;
import nl.moj.client.codecompletion.statement.MethodStatement;
import nl.moj.client.codecompletion.statement.Statement;

/**
 * Holds the code to complete
 */

public class PartialCode {

    private boolean newInstance;
	private boolean hasArrayBrackets;
    private boolean variable;
    private boolean inStaticMethod;
    private String codeToComplete;
    private String[] dotSeparated;
    private Declaration[] knownDeclarations;
    private CodeNode[] myRoots;
    private CodeNode foundRoot;

    /**
     * @param codeToComplete
     *            the code to complete
     * @param newInstance
     *            true if the code is linked to the new keyword.
     */
    public PartialCode(String codeToComplete, boolean newInstance,boolean hasArrayBrackets) {
        this.codeToComplete = codeToComplete;
        this.newInstance = newInstance;
        // System.out.println("Code to Complete : "+codeToComplete);
        dotSeparated = Tool.cutForEach(codeToComplete, ".");
        this.hasArrayBrackets=hasArrayBrackets;
    }

    public void setContext(CodeNode[] roots, Declaration[] decl) {
        myRoots = roots;
        knownDeclarations = decl;
    }

    public int length() {
        return codeToComplete.length();
    }

    public String toString() {
        return codeToComplete;
    }
    
    public void checkStatic(Statement stm) {
    	boolean st=true;
    	while (stm!=null) {
    		if (stm.isMethodStatement()) {
    			st=((MethodStatement)stm).isStatic();
    		}
    		stm=stm.getParent();
    	}
    	this.inStaticMethod=st;
    }

    /**
     * Resolve attempts to resolve the various parts before the actual
     */
    public void resolve() {
        //
        if (dotSeparated.length <= 1) return;
        //
        boolean sni=newInstance;
        boolean st=inStaticMethod;
        boolean sv=variable;
        //
        for (int ridx=0;ridx<myRoots.length;ridx++) {
            //
            // See if there is a complete type def.
            //
        	newInstance=sni;
        	inStaticMethod=st;
        	variable=sv;
            CodeNode root = myRoots[ridx];
            // System.out.println(myRoot.render());
            //
            for (int t = 0; t < dotSeparated.length - 1; t++) {
                // System.out.println("CURRENT : "+dotSeparated[t]);
                CodeNode newRoot = root.contains(dotSeparated[t]);
                // System.out.println("newRoot = "+newRoot);
                if (newRoot == null) {
                    for (int y = 0; y < knownDeclarations.length; y++) {
                        if (dotSeparated[t].equals(knownDeclarations[y].getName())) {
                            // System.out.println("declaration =
                            // "+knownDeclarations[y]);
                            variable = true;
                            String[] type = Tool.cutForEach(knownDeclarations[y].getType(), ".");
                            CodeNode troot = myRoots[ridx];
                            for (int z = 0; z < type.length; z++) {
                                troot = troot.contains(type[z]);
                                if (troot == null) break;
                            }
                        	newRoot = troot;
                            if (knownDeclarations[y].isArray()) {
                            	if (newRoot instanceof ReferenceNode) {
                            		newRoot=((ReferenceNode)newRoot).asArrayNode();
                            	} else if (newRoot instanceof BranchNode) {
                            		newRoot=((BranchNode)newRoot).asArrayNode();
                            	}
                            }
                        }
                    }
                } else {
                	inStaticMethod=newRoot.isStatic();
                	// Mmm.. we use code conventions to determine if its a static or not...
                	if (isStatic()) {
                		boolean mayBeClass=Character.isUpperCase(newRoot.getName().charAt(0));
                		boolean constant=newRoot.getName().toUpperCase().equals(newRoot.getName());
                		variable=!mayBeClass||constant;
                	}
                }
                root = newRoot;
                // System.out.println("root = "+root);
                if (root != null) {
                	foundRoot=root;
                } else {
                	break;
                }
            }
            //
            if (foundRoot!=null) {
            	return;
            }
            //
        }
        //
    }

    public String[] findCompletions() {
        Set<String> result=new TreeSet<String>();
        Set<CodeNode> tmp = new TreeSet<CodeNode>();
        //
        if (getRoot().isArray()) {
        	if (hasArrayBrackets) {
            	getRoot().findChildren(getRemainderToComplete(), true, isStatic(), tmp);
        	} else {
        		tmp.add(new CodeNode.LeafNode(null,"length",false));
        	}
        } else {
        	getRoot().findChildren(getRemainderToComplete(), true, isStatic(), tmp);
        }
        //
        CodeNode[] cna = tmp.toArray(new CodeNode[tmp.size()]);
        for (int t = 0; t < cna.length; t++) {
            if (cna[t].getName().length() > 0) {
                result.add(getPrefix() + cna[t].getName());
            }            
        }
        //
        return result.toArray(new String[result.size()]);
    }

    public boolean isStatic() {
        return ((newInstance)||(inStaticMethod))&&(!variable);
    }

    public CodeNode getRoot() {
        if (foundRoot == null) {
            return myRoots[0];
        }
        return foundRoot;
    }

    public String getRemainderToComplete() {
        return dotSeparated[dotSeparated.length - 1];
    }

    public String getPrefix() {
        StringBuffer sb = new StringBuffer();
        for (int t = 0; t < dotSeparated.length - 1; t++) {
            sb.append(dotSeparated[t]);
            sb.append(".");
        }
        return sb.toString();
    }

}
