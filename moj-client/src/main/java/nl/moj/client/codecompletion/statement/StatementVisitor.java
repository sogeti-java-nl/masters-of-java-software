package nl.moj.client.codecompletion.statement;

/**
 *
 */
public interface StatementVisitor {
	public void begin();
	public void beginStatement(AbstractStatement st,int indent,boolean isLast);
	public void onStatement(AbstractStatement st,int indent,boolean isLast);
	public void endStatement(AbstractStatement st,int indent,boolean isLast);
	public void end();
}
