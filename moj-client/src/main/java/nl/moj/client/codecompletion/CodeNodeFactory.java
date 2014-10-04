package nl.moj.client.codecompletion;

import nl.ctrlaltdev.util.Tool;

/**
 *
 */
public class CodeNodeFactory {

    public CodeNodeFactory() {
        super();
    }
    
    public CodeNode createRoot() {
    	return new CodeNode.BranchNode(null,"",true);
    }
    
    public CodeNode addIfNotExist(CodeNode parent,String name,boolean isStatic) {
    	CodeNode sub=parent.contains(name);
    	if (sub!=null) return sub;
    	CodeNode n=new CodeNode.BranchNode(parent,name,isStatic);
    	((CodeNode.BranchNode)parent).addNode(n);
    	return n;
    }
    
    public CodeNode addIfNotExistLazy(CodeNode parent,String name,boolean isStatic) {
        CodeNode sub=parent.contains(name);
        if (sub!=null) return sub;
        CodeNode n=new CodeNode.BranchNode(parent,name,isStatic,true,this);
        ((CodeNode.BranchNode)parent).addNode(n);
        return n;
    }
    
    public CodeNode addReferenceIfNotExist(CodeNode parent,String name,String reference,boolean isStatic,boolean isArray) {
		CodeNode sub=parent.contains(name);
		if (sub!=null) return sub;
		CodeNode root=this.getRoot(parent);
		CodeNode target=addDotSeparatedLazy(reference,root);
		CodeNode n=new CodeNode.ReferenceNode(parent,name,target,isStatic,isArray);
		((CodeNode.BranchNode)parent).addNode(n);
		return n;    
	}
    
    public CodeNode addDotSeparated(String name,CodeNode root) {
    	String[] pack=Tool.cut(name,".$");
    	for (int t=0;t<pack.length;t++) {
    		root=addIfNotExist(root,pack[t],true);
    	}
    	return root;
    }
    
    public CodeNode addDotSeparatedLazy(String name,CodeNode root) {
        String[] pack=Tool.cut(name,".$");
        for (int t=0;t<pack.length;t++) {
            root=addIfNotExistLazy(root,pack[t],true);
        }
        return root;
    }
    
    public CodeNode getRoot(CodeNode child) {
    	while (child.getParent()!=null) {
    		child=child.getParent();
    	}
    	return child;
    }
    
}
