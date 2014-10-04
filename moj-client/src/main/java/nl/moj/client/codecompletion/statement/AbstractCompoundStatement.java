package nl.moj.client.codecompletion.statement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractCompoundStatement extends AbstractStatement implements CompoundStatement{

	protected List<Statement> sub;
	
    public AbstractCompoundStatement(String[] words,int pos) {
        super(words,pos);
    }
    
	public void visit(StatementVisitor v,int indent,boolean isLast) {
		if (sub==null) {
			v.onStatement(this,indent,isLast);
		} else {
			v.beginStatement(this,indent,isLast);
			for (int t=0;t<sub.size();t++) {
				((AbstractStatement)sub.get(t)).visit(v,(indent+1),t==sub.size()-1);
			}
			v.endStatement(this,indent,isLast);
		}
	} 
	
	public void addStatement(AbstractStatement stm) {
		if (sub==null) sub=new ArrayList<Statement>();
		sub.add(stm);
	} 
	
	public Statement[] getSubStatements() {
		if (sub==null) return new Statement[0];
		return sub.toArray(new Statement[sub.size()]);
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends Statement> void scan(List<T> results,Class<? extends T> type) {
		for (int t=0;t<sub.size();t++) {
			if (type.isAssignableFrom(sub.get(t).getClass())) results.add((T)sub.get(t));
		}
	} 

}
