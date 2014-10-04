package nl.moj.client.codecompletion;

import java.util.ArrayList;
import java.util.List;

import nl.ctrlaltdev.util.Tool;
import nl.moj.client.codecompletion.statement.Declaration;
import nl.moj.client.codecompletion.statement.DeclarationStatement;
import nl.moj.client.codecompletion.statement.Statement;
import nl.moj.client.codecompletion.statement.StatementException;

/**
 * 
 */
public class CodeCompletion {

	private List<SourceCodeCompletion> mySources;
	private CodeNodeFactory myCNF;

	public CodeCompletion() {
		super();
		mySources = new ArrayList<SourceCodeCompletion>();
		myCNF = new CodeNodeFactory();
	}

	/**
	 * Adds a piece of static (non editable) source code for future reference.
	 */
	public void addStaticSource(String source) throws StatementException {
		mySources.add(new SourceCodeCompletion(source));
	}

	public String removeCorresponding(String l, String start, String end) {
		int sx = l.indexOf(start);
		int ex = l.indexOf(end);
		if (((sx >= 0) && (ex == -1)) || ((sx >= 0) && (ex >= 0) && (sx < ex))) {
			String first = l.substring(0, sx);
			String tmp = l.substring(sx + 1);
			tmp = removeCorresponding(tmp, start, end);
			int se = tmp.indexOf(end);
			if (se >= 0) {
				String tmp2 = tmp.substring(se + 1);
				l = first + removeCorresponding(tmp2, start, end);
			}
		}
		return l;
	}

	/**
	 * Identifies the part of the source that needs to be completed.
	 */
	public PartialCode getPartToComplete(String line) {
		if (line.length() == 0)
			return new PartialCode("", false, false);
		int idx2 = line.lastIndexOf(",");
		if (idx2 >= 0)
			line = line.substring(idx2 + 1);
		int idx3 = line.lastIndexOf("=");
		if (idx3 >= 0)
			line = line.substring(idx3 + 1);
		int idx4 = line.lastIndexOf("<");
		if (idx4 >= 0)
			line = line.substring(idx4 + 1);
		int idx5 = line.lastIndexOf(">");
		if (idx5 >= 0)
			line = line.substring(idx5 + 1);
		int idx6 = line.lastIndexOf(";");
		if (idx6 >= 0)
			line = line.substring(idx6 + 1);
		//
		line = removePadding(line.trim(), " ");
		line = removeCorresponding(line, "(", ")");
		boolean hasArrayBrackets = (line.indexOf('[') >= 0) && (line.indexOf(']') >= 0);
		line = removeCorresponding(line, "[", "]");
		//
		String[] words = Tool.cut(line, " ");
		if (words.length == 0)
			return new PartialCode("", false, false);
		boolean isNewInstance = ((words.length > 1) && (words[words.length - 2].equals("new")));
		//
		return new PartialCode(words[words.length - 1], isNewInstance, hasArrayBrackets);
		//
	}

	public String removePadding(String l, String p) {
		while (l.indexOf(p) >= 0) {
			l = l.substring(0, l.indexOf(p)) + l.substring(l.indexOf(p) + p.length());
		}
		return l;
	}

	public String[] getCompletions(String contextSource, int contextPosition, PartialCode wordToComplete) {
		CodeNode root = myCNF.createRoot();
		//
		// Parse the current source and add it to the root node.
		//
		SourceCodeParser parse = null;
		try {
			parse = new SourceCodeParser(contextSource);
			new SourceCodeCompletion(parse).addToCodeTree(myCNF, root);
		} catch (StatementException ex) {
			// System.out.println(ex);
		}
		//
		// Add static sources to the root node.
		//
		for (int t = 0; t < mySources.size(); t++) {
			SourceCodeCompletion scc = mySources.get(t);
			scc.addToCodeTree(myCNF, root);
		}
		//
		if (parse != null) {
			//
			Statement stm = parse.getStatementAt(contextPosition);
			wordToComplete.checkStatic(stm);
			//
			List<Declaration> varList = new ArrayList<Declaration>();
			List<DeclarationStatement> rootList = new ArrayList<DeclarationStatement>();
			parse.getJavaFile().findAccessibleDeclarations(stm, varList);
			parse.getJavaFile().findAccessibleRoots(stm, rootList);
			Declaration[] vars = varList.toArray(new Declaration[varList.size()]);
			CodeNode[] roots = new CodeNode[rootList.size() + 1];
			for (int t = 0; t < rootList.size(); t++) {
				roots[t] = toCodeNode(root, rootList.get(t));
			}
			roots[rootList.size()] = root;
			//
			// Add known variables for this context.
			//
			wordToComplete.setContext(roots, vars);
			//
			wordToComplete.resolve();
			//
		} else {
			wordToComplete.setContext(new CodeNode[] { root }, new Declaration[0]);
		}
		//
		return wordToComplete.findCompletions();
		//
	}

	public CodeNode toCodeNode(CodeNode root, DeclarationStatement st) {
		String[] parts = Tool.cut(st.getFullyQualifiedName(), ".");
		for (int t = 0; t < parts.length; t++) {
			root = root.contains(parts[t]);
			if (root == null)
				return null;
		}
		return root;
	}

}
