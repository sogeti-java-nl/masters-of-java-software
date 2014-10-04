package nl.moj.client.codecompletion.statement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JavaFile implements CompoundStatement {

	public static final PackageStatement DEFAULTPACKAGE = new PackageStatement(
			null, 0, new String[] { "package", "" });

	private List<Statement> sub = new ArrayList<Statement>();

	public JavaFile() {
		super();
	}

	public void visit(StatementVisitor v, int indent, boolean isLast) {
		v.begin();
		for (int t = 0; t < sub.size(); t++) {
			((AbstractStatement) sub.get(t)).visit(v, (indent + 1),
					t == sub.size() - 1);
		}
		v.end();
	}

	public void addStatement(AbstractStatement stm) {
		sub.add(stm);
	}

	public PackageStatement getPackage() {
		for (int t = 0; t < sub.size(); t++) {
			if (sub.get(t) instanceof PackageStatement)
				return (PackageStatement) sub.get(t);
		}
		return DEFAULTPACKAGE;
	}

	public ImportStatement[] getImports() {
		List<ImportStatement> results = new ArrayList<ImportStatement>();
		for (int t = 0; t < sub.size(); t++) {
			if (sub.get(t) instanceof ImportStatement)
				results.add((ImportStatement) sub.get(t));
		}
		return results.toArray(new ImportStatement[results.size()]);
	}

	public ClassStatement[] getClasses() {
		List<ClassStatement> results = new ArrayList<ClassStatement>();
		for (int t = 0; t < sub.size(); t++) {
			if (sub.get(t) instanceof ClassStatement)
				results.add((ClassStatement) sub.get(t));
		}
		return results.toArray(new ClassStatement[results.size()]);
	}

	/**
	 * returns true if the fully qualified name is imported into the source
	 * file.
	 */
	public boolean containsImport(String importType) {
		for (int t = 0; t < sub.size(); t++) {
			if (sub.get(t) instanceof ImportStatement) {
				ImportStatement stm = (ImportStatement) sub.get(t);
				//
				// Exact match.
				//
				if (stm.getImport().equals(importType))
					return true;
				//
				// WildCard Match
				//
				if (stm.isWildcard()) {
					int idx = importType.lastIndexOf(".");
					if (idx < 0)
						continue;
					if (stm.getImport()
							.substring(0, stm.getImport().length() - 2)
							.equals(importType.substring(0, idx)))
						return true;
				}
			}
		}
		return false;
	}

	public String expandImport(String shortName) {
		//
		if (shortName.endsWith("[]")) {
			shortName = shortName.substring(0, shortName.length() - 2);
		}
		//
		for (int t = 0; t < sub.size(); t++) {
			if (sub.get(t) instanceof ImportStatement) {
				ImportStatement stm = (ImportStatement) sub.get(t);
				int idx = stm.getImport().lastIndexOf(".");
				if (idx < 0)
					continue;
				if (stm.getImport().substring(idx + 1).equals(shortName)) {
					return stm.getImport();
				}
			}
		}
		//
		if (shortName.indexOf(".") < 0) {
			String tmp = "java.lang." + shortName;
			try {
				return Class.forName(tmp).getName();
			} catch (ClassNotFoundException ex) {
				// Ignore.
			}
		}
		//
		return shortName;
	}

	public void findAccessibleRoots(Statement start,
			List<DeclarationStatement> roots) {
		do {
			if (start instanceof ClassStatement) {
				roots.add((DeclarationStatement) start);
			} else if (start instanceof InterfaceStatement) {
				roots.add((DeclarationStatement) start);
			}
			start = start.getParent();
		} while (start != null);
	}

	public void findAccessibleDeclarations(Statement start,
			List<Declaration> results) {
		//
		// Should add any method parameters.
		//
		if (start instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) start;
			Declaration[] vr = ms.getParams();
			for (int t = 0; t < vr.length; t++) {
				results.add(vr[t]);
			}
		}
		CompoundStatement parent = (CompoundStatement) start.getParent();
		if (parent == null)
			return;
		Statement[] sub = parent.getSubStatements();
		for (int t = 0; t < sub.length; t++) {
			//
			// Only add stuff till where we are for variables.
			//
			if (sub[t].getPosition() < start.getPosition()) {
				if (sub[t] instanceof FieldStatement) {
					results.add(((FieldStatement) sub[t]).getDeclaration());
				}
				if (sub[t] instanceof VariableDeclarationStatement) {
					results.add(((VariableDeclarationStatement) sub[t])
							.getDeclaration());
				}
			} else {
				// if (sub[t] instanceof MethodStatement) {
				// results.add(((MethodStatement) sub[t]).getDeclaration());
				// }
				// if (sub[t] instanceof ClassStatement) {
				// results.add(((ClassStatement) sub[t]).getDeclaration());
				// }
			}
		}
		//
		findAccessibleDeclarations(parent, results);
		//
	}

	public ClassStatement[] findClassDeclarations(Statement start) {
		List<ClassStatement> l = new ArrayList<ClassStatement>();
		while (start.getParent() != null) {
			if (start instanceof ClassStatement) {
				l.add((ClassStatement) start);
			}
			start = start.getParent();
		}
		return l.toArray(new ClassStatement[l.size()]);
	}

	public Statement[] getSubStatements() {
		return sub.toArray(new Statement[sub.size()]);
	}

	public Statement getParent() {
		return null;
	}

	public DeclarationStatement getOwningDeclaration() {
		return null;
	}

	public boolean isClassStatement() {
		return false;
	}

	public boolean isCompound() {
		return false;
	}

	public boolean isConstructorStatement() {
		return false;
	}

	public boolean isFieldStatement() {
		return false;
	}

	public boolean isImportStatement() {
		return false;
	}

	public boolean isInterfaceStatement() {
		return false;
	}

	public boolean isMethodStatement() {
		return false;
	}

	public boolean isPackageStatement() {
		return false;
	}

	public boolean isEnumStatement() {
		return false;
	}

	public boolean isIfStatement() {
		return false;
	}

	public int getPosition() {
		return 0;
	}

}
