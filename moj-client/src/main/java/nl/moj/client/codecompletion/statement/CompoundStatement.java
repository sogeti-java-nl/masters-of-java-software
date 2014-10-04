package nl.moj.client.codecompletion.statement;

/**
 *
 */
public interface CompoundStatement extends Statement {

	public void addStatement(AbstractStatement stm);
	public Statement[] getSubStatements();

}
