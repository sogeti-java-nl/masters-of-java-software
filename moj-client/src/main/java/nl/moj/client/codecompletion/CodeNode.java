package nl.moj.client.codecompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import nl.ctrlaltdev.util.Tool;

/**
 * Code Node : Tree like construct to store types and identifiers with their type to
 *             ease code completion lookup.
 * @author E.Hooijmeijer 
 */

public abstract class CodeNode implements Comparable<CodeNode> {

	private static final CodeNode[] NOTHING=new CodeNode[0];

	/** End point. */
	public static class LeafNode extends CodeNode {
		public LeafNode(CodeNode parent,String name,boolean isStatic) {
			super(parent,name,isStatic);
		}
		public CodeNode[] getLeafNodes() {
            return NOTHING;
        }
	}
	
	/** BranchNode : stores package names, types and subtypes. */
	public static class BranchNode extends CodeNode {
		private List<CodeNode> mySubNodes=new ArrayList<CodeNode>();
		private Set<CodeNode> myLinkedNodes=new TreeSet<CodeNode>();
		private CodeNodeFactory cnf;
		public BranchNode(CodeNode parent,String name,boolean isStatic) {
			super(parent,name,isStatic);			
		}
        public BranchNode(CodeNode parent,String name,boolean isStatic,boolean isLazy,CodeNodeFactory cnf) {
            super(parent,name,isStatic,isLazy,false);
            this.cnf=cnf;
        }
        public BranchNode(CodeNode parent,String name,boolean isStatic,boolean isLazy,CodeNodeFactory cnf,boolean isArray) {
            super(parent,name,isStatic,isLazy,isArray);
            this.cnf=cnf;
        }        
		public void addNode(CodeNode n) {
			mySubNodes.add(n);
		}
		public CodeNode[] getLeafNodes() {
		    if (isLazy()) {
                clearLazy();
		        new TypeCodeCompletion(getFullPath()).addToCodeTree(cnf,getRoot());
		        cnf=null;
		    }
		    List<CodeNode> tmp=new ArrayList<CodeNode>();
		    tmp.addAll(mySubNodes);
            for (CodeNode c:myLinkedNodes) {
                tmp.addAll(Arrays.asList(c.getLeafNodes()));
            }
            return tmp.toArray(new CodeNode[tmp.size()]);
        }
		public void addLink(CodeNode link) {
		    if (link==null) return;
		    if (this.equals(link)) throw new RuntimeException("Cannot be one's own child.");
		    myLinkedNodes.add(link);
		}
		public CodeNode asArrayNode() {
			BranchNode bn=new BranchNode(getParent(),getName(),isStatic(),isLazy(),cnf,true);
			bn.myLinkedNodes=myLinkedNodes;
			bn.mySubNodes=mySubNodes;
			return bn;
		}

	}
	
	/** ReferenceNode : something that has a name and refers to another type. Fields, Methods and such. */ 	
	public static class ReferenceNode extends CodeNode {
		private CodeNode reference;
		public ReferenceNode(CodeNode parent,String name,CodeNode node,boolean isStatic,boolean isArray) {
			super(parent,name,isStatic,false,isArray);
			reference=node;
		}
		public CodeNode[] getLeafNodes() {
            return reference.getLeafNodes();
        }
		public void find(String nodeName,boolean partial,boolean isStatic,Set<CodeNode> results) {
			//
			if (match(nodeName,partial,isStatic)) {
				results.add(this);
			}
			//
			// Do not traverse subnodes.
			// These are already somewhere else in the list.
			//
		}
		protected void render(StringBuffer sb, int indent) {
			for (int t=0;t<indent;t++) sb.append("| ");
			sb.append("+-");
			sb.append(getName()+" --> "+reference.getFullPath()+(isArray()?"[]":""));
			sb.append("\n");
        }
		public CodeNode asArrayNode() {
			return new ReferenceNode(getParent(),getName(),reference,isStatic(),true);
		}
	}


	private String myName;
	private boolean myIsStatic;
	private boolean isLazy;
	private boolean isArray;
	private CodeNode myParent;	

	public CodeNode(CodeNode parent,String name,boolean isStatic) {
	    this(parent,name,isStatic,false,false);
	}
    public CodeNode(CodeNode parent,String name,boolean isStatic,boolean isLazy,boolean isArray) {
        myName=name;
        myIsStatic=isStatic;
        myParent=parent;
        this.isLazy=isLazy;
        this.isArray=isArray;
    }

    public boolean isLazy() {
        return isLazy;
    }
    
    protected void clearLazy() {
        isLazy=false;
    }
    
    public boolean isArray() {
    	return isArray;
    }
    
    protected CodeNode getRoot() {
        if (this.getParent()!=null) return getParent().getRoot();
        return this;
    }
	
	/** returns the name of this node */
	public String getName() {
		return myName;
	}
	
	/** returns true if this branch has children */
	public boolean isBranch() {
		return (getLeafNodes().length>0);
	}

	/** returns the leafnodes if any */
	public abstract CodeNode[] getLeafNodes();
	
	public CodeNode contains(String name) {
		CodeNode[] cn=getLeafNodes();
		for (int t=0;t<cn.length;t++) {
			if (cn[t].getName().equals(name)) return cn[t];
		} 
		return null;
	}
	
	/** 
	 * searches the tree from this node onwards doing a complete or partial match on the name.
	 * @param nodeName the nodename you're looking for.
	 * @param partial if partial matches should be returned as well.
	 * @param isStatic if true, only static nodes will be returned. 
	 * @param results a list to store the results in.
	 */
	public void find(String nodeName,boolean partial,boolean isStatic,Set<CodeNode> results) {
		//
		if (match(nodeName,partial,isStatic)) {
			results.add(this);
		}
		//
		findChildren(nodeName, partial, isStatic, results);
		//		
	}
	
	public boolean isRoot() {
	    return myName.length()==0;
	}
	
	public boolean isStatic() {
		return myIsStatic;
	}
	
	public boolean containsPath(String s) {
	    return findPath(Tool.cut(s,"."))!=null;
	}
	
	public CodeNode findPath(String[] s) {
	    if (s.length==0) return this;
	    if (match(s[0],false,true)||match(s[0],false,false)||(isRoot())) {
            if (s.length==1) return this;
	        CodeNode[] cn=getLeafNodes();
	        if (!isRoot()) {
	            String[] ss=new String[s.length-1];
	            System.arraycopy(s,1, ss,0,ss.length);
	            s=ss;
	        }
	        for (int t=0;t<cn.length;t++) {
	            CodeNode found=cn[t].findPath(s);
	            if (found!=null) return found; 
	        }
	    }
	    return null;
	}
	
	public CodeNode findPath(String s) {
	    return findPath(Tool.cut(s,"."));
	}
	
    /** 
     * searches this node children doing a complete or partial match on the name.
     * @param nodeName the nodename you're looking for.
     * @param partial if partial matches should be returned as well.
     * @param isStatic if true, only static nodes will be returned. 
     * @param results a list to store the results in.
     */	
	public void findChildren(String nodeName,boolean partial,boolean isStatic,Set<CodeNode> results) {
        CodeNode[] cn=getLeafNodes();
        for (int t=0;t<cn.length;t++) {
            cn[t].find(nodeName,partial,isStatic,results);
        } 
	}
	
	protected void resolveReference() {
	    throw new UnsupportedOperationException("Not Implemented");
	}

	/**
	 * returns true if the specified nodeName matches the current node name.
	 * @param nodeName the nodename you're looking for.
	 * @param partial if true, partial matches return true as well.
	 * @param isStatic if true, only static nodes will be returned.
	 */
	public boolean match(String nodeName,boolean partial,boolean isStatic) {
		//
		// Only match non static nodes if isStatic is false.  
		//
		if (this.myIsStatic) {
			if (!isStatic) return false;
		} else if (!this.myIsStatic) {
		    if (isStatic) return false;
		}
		//
		// Use the myName property directly, instead of the accessor,
		// which may delegate to somewhere else in the tree.
		//
		if (partial) {
			return myName.startsWith(nodeName);
		} else {
			return myName.equals(nodeName);
		}
	}
	
	public CodeNode getParent() {
		return myParent;
	}
	
	public String toString() {
		return "CodeNode("+myName+")";
	}
	
	public int compareTo(CodeNode o) {		
    	return o.getName().compareTo(myName);
	}
	
	
	public String render() {
		StringBuffer sb=new StringBuffer();
		render(sb,0);
		return sb.toString(); 
	}
	
	protected void render(StringBuffer sb,int indent) {
		for (int t=0;t<indent;t++) sb.append("| ");
		sb.append("+-");
		sb.append(myName);
		sb.append("\n");
		CodeNode[] cn=getLeafNodes();
		for (int t=0;t<cn.length;t++) {
			cn[t].render(sb,indent+1);
		}
	}
	
	public String getFullPath() {
		//
		CodeNode p=this.getParent();
		if (p!=null) {
			String fp=p.getFullPath();
			if (fp.length()==0) return getName();
						   else return fp+"."+getName();
		} else { 		
			return getName(); 
		}
	}
	
	public void addLink(CodeNode cc) {
	    throw new UnsupportedOperationException("");
	} 	
	

}
