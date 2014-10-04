package nl.moj.client.codecompletion.statement;

/**
 * Base class for Class,Field,Method and Constructor declarations.
 */
public abstract class DeclarationStatement extends AbstractCompoundStatement {

    public DeclarationStatement(String[] words, int pos) {
        super(words, pos);
    }

    /** returns true if the declaration is public */
    public boolean isPublic() {
        return contains(M_PUBLIC);
    }

    /** returns true if the declaration is protected */
    public boolean isProtected() {
        return contains(M_PROTECTED);
    }

    /** returns true if the declaration has package access */
    public boolean isPackage() {
        return (!isPublic()) && (!isPrivate()) && (!isProtected());
    }

    /** returns true if the declaration is private */
    public boolean isPrivate() {
        return contains(M_PRIVATE);
    }

    /** returns true if the declaration is static */
    public boolean isStatic() {
        return contains(M_STATIC);
    }

    public abstract String getName();

    /**
     * returns the fully declared name of the type declared by this statement.
     * @return the fully declared name.
     */
    public String getFullyQualifiedName() {
        String fqname = getName();
        // 
        // Find JavaFile
        //	
        Statement p = this;
        while (p.getParent() != null) {
            p = p.getParent();
            if (p instanceof ClassStatement) {
                fqname = ((ClassStatement) p).getName() + "." + fqname;
            } else if (p instanceof InterfaceStatement) {
                fqname = ((InterfaceStatement) p).getName() + "." + fqname;
            }
        }
        //
        String pkg = ((JavaFile) p).getPackage().getPackage();
        return (pkg.length() == 0 ? fqname : pkg + "." + fqname);
        //	
    }

    /**
     * returns the fully qualified name of a type used by this statement.
     * @param name the name to look for.
     * @return the fully qualified name.
     */
    public String findFullyQualifiedName(String name) {
        if (name.indexOf('<') >= 0) name = name.substring(0, name.indexOf('<'));
        Statement p = this;
        while (p.getParent() != null) {
            p = p.getParent();
            if (p instanceof ClassStatement) {
                ClassStatement cs = (ClassStatement) p;
                if (cs.getName().equals(name)) {
                    return cs.getFullyQualifiedName();
                }
                DeclarationStatement[] ds = cs.getTypes();
                for (int t = 0; t < ds.length; t++) {
                    if (ds[t].getName().equals(name)) {
                        return ds[t].getFullyQualifiedName();
                    }
                }
            } else if (p instanceof InterfaceStatement) {
                InterfaceStatement cs = (InterfaceStatement) p;
                if (cs.getName().equals(name)) {
                    return cs.getFullyQualifiedName();
                }
                DeclarationStatement[] ds = cs.getTypes();
                for (int t = 0; t < ds.length; t++) {
                    if (ds[t].getName().equals(name)) {
                        return ds[t].getFullyQualifiedName();
                    }
                }
            }
        }
        //
        // String pkg=((JavaFile)p).getPackage().getPackage();
        // return (pkg.length()==0 ? name : pkg+"."+name);
        return ((JavaFile) p).expandImport(name);
        //  	    
    }

}
