package nl.moj.client.codecompletion;

import java.util.ArrayList;
import java.util.List;

import nl.moj.client.codecompletion.statement.AbstractCompoundStatement;
import nl.moj.client.codecompletion.statement.AbstractStatement;
import nl.moj.client.codecompletion.statement.CompoundStatement;
import nl.moj.client.codecompletion.statement.JavaFile;
import nl.moj.client.codecompletion.statement.Statement;
import nl.moj.client.codecompletion.statement.StatementException;
import nl.moj.client.codecompletion.statement.StatementFactory;
import nl.moj.client.codecompletion.statement.StatementVisitor;

/**
 * Parses a Java source file into a hierarchy of statements.
 * @author E.Hooijmeijer
 */

public class SourceCodeParser {

    private JavaFile root;

    public SourceCodeParser(String source) throws StatementException {
        this(source, false);
    }

    public SourceCodeParser(String source, boolean faultTolerant) throws StatementException {
        root = new JavaFile();
        try {
            makeStatements(root, source, faultTolerant, 0);
        } catch (StatementException ex) {
            if (!faultTolerant) throw ex;
        }
    }

    public void visit(StatementVisitor v) {
        root.visit(v, 0, false);
    }

    public JavaFile getJavaFile() {
        return root;
    }

    /** determines the statement at the specified cursor position */
    public Statement getStatementAt(final int position) {
        final List<AbstractStatement> bestMatches = new ArrayList<>();
        visit(new StatementVisitor() {
            public void begin() {
            }

            public void end() {
            }

            public void beginStatement(AbstractStatement st, int indent, boolean isLast) {
                if (st.getPosition() < position) {
                    bestMatches.add(st);
                }
            }

            public void onStatement(AbstractStatement st, int indent, boolean isLast) {
                if (st.getPosition() < position) {
                    bestMatches.add(st);
                }
            }

            public void endStatement(AbstractStatement st, int indent, boolean isLast) {
            }
        });
        //
        if (bestMatches.size() == 0) return null;
        return (Statement) bestMatches.get(bestMatches.size() - 1);
    }

    /**
     * parses the source file and creates statements based on the following rules : - compound statements start with { and end with } - normal statements end
     * with ;
     */

    protected int makeStatements(CompoundStatement parent, String s, boolean tolerant, int offset) {
        try {
            int startPos = 0;
            int roundBr = 0;
            char inString = 0;
            StringBuffer sb = new StringBuffer();
            for (int t = 0; t < s.length(); t++) {
                char c = s.charAt(t);
                //
                if (c < 32) c = 32;
                //
                if (c == '/') {
                    //
                    // Skip any comments.
                    //
                    if (s.substring(t, t + 2).equals("//")) {
                        int end = s.substring(t).indexOf("\n");
                        if (end < 0) throw new StatementException("Missing end of line.");
                        t = t + end;
                        continue;
                    } else if (s.substring(t, t + 2).equals("/*")) {
                        int end = s.substring(t).indexOf("*/") + 1;
                        if (end < 0) throw new StatementException("Missing end of comment.");
                        t = t + end;
                        continue;
                    }
                    //
                    // Not a comment..
                    //
                    sb.append(c);
                    //
                } else if ((c == '"') || (c == '\'')) {
                    if (inString == 0) {
                        inString = c;
                    } else if (c == inString) {
                        inString = 0;
                    }
                    sb.append(c);
                } else if ((c == '(') && (inString == 0)) {
                    roundBr++;
                    sb.append(c);
                } else if ((c == ')') && (inString == 0)) {
                    roundBr--;
                    sb.append(c);
                } else if ((c == ';') && (roundBr == 0) && (inString == 0)) {
                    //
                    // ; indicates end of statement.
                    //
                    if (sb.length() != 0) {
                        AbstractStatement st = StatementFactory.createStatement(parent, offset + startPos, sb.toString().trim());
                        if (st != null) parent.addStatement(st);
                        sb.delete(0, sb.length());
                        startPos = t + 1;
                    }
                    //				
                } else if ((c == '{') && (inString == 0)) {
                    //
                    // { indicates the beginning of a block statement.
                    //
                    String tmp = sb.toString().trim();
                    if (tmp.length() > 0) {
                        AbstractCompoundStatement sub = (AbstractCompoundStatement) StatementFactory.createStatement(parent, offset + startPos, tmp);
                        parent.addStatement(sub);
                        t = t + makeStatements(sub, s.substring(t + 1), tolerant, offset + t + 1);
                        startPos = t + 1;
                    } else
                        throw new StatementException("Cannot start a statement with a {");
                    //
                    sb.delete(0, sb.length());
                    //
                } else if ((c == '}') && (inString == 0)) {
                    //
                    // } indicates the end of a block statement.
                    //
                    String tmp = sb.toString().trim();
                    if (tmp.length() > 0) parent.addStatement(StatementFactory.createStatement(parent, offset + startPos, tmp));
                    sb.delete(0, sb.length());
                    return t + 1;
                    //
                } else {
                    sb.append(c);
                }
            }
            //
            if (sb.toString().trim().length() > 0) {
                parent.addStatement(StatementFactory.createStatement(parent, startPos, sb.toString().trim()));
            }
            //
            return s.length();
        } catch (StatementException ex) {
            if (!tolerant) throw ex;
            //
            // Find some point to continue.
            //
            int idx = s.indexOf("}");
            return (idx > -1 ? idx : 1);
        }
    }

}
