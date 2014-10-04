package nl.moj.client.codecompletion.statement;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class StatementFactory {

	private static final String OPERATORS = "=+-*/%&|<>";

	public static AbstractStatement createStatement(Object parent,
			int position, String statement) {
		//
		statement = statement.trim();
		//
		if (statement.length() == 0) {
			throw new StatementException("Empty Statement in " + parent);
		}
		//
		int inBracketCnt = 0;
		List<String> words = new ArrayList<>();
		StringBuffer sb = new StringBuffer();
		for (int t = 0; t < statement.length(); t++) {
			char c = statement.charAt(t);
			if (c == '(')
				inBracketCnt++;
			if (c == ')')
				inBracketCnt--;
			if ((c == ' ')) {
				if (inBracketCnt == 0) {
					if (sb.length() > 0) {
						words.add(sb.toString());
						sb.delete(0, sb.length());
					}
				} else
					sb.append(' ');
			}
			if (OPERATORS.indexOf(c) >= 0) {
				if (sb.length() > 0) {
					words.add(sb.toString());
					sb.delete(0, sb.length());
				}
				sb.append(c);
				words.add(sb.toString());
				sb.delete(0, sb.length());
			} else {
				if (c != ' ') {
					sb.append(c);
				}
			}
		}
		//
		if (sb.length() != 0) {
			words.add(sb.toString());
		}
		//
		String[] warr = words.toArray(new String[words.size()]);
		//
		if (parent instanceof JavaFile) {
			if (PackageStatement.qualifies(warr))
				return new PackageStatement((JavaFile) parent, position, warr);
			if (ImportStatement.qualifies(warr))
				return new ImportStatement((JavaFile) parent, position, warr);
		}
		//
		if (ClassStatement.qualifies(warr))
			return new ClassStatement((CompoundStatement) parent, position,
					warr);
		if (EnumStatement.qualifies(warr))
			return new EnumStatement((CompoundStatement) parent, position, warr);
		if (InterfaceStatement.qualifies(warr))
			return new InterfaceStatement((CompoundStatement) parent, position,
					warr);
		//
		if (parent instanceof ClassStatement) {
			if (parent instanceof EnumStatement) {
				EnumStatement e = (EnumStatement) parent;
				if (e.getSubStatements().length == 0) {
					e.makeEnumList(warr, position);
					return null;
				}
			}
			//
			if (StaticStatement.qualifies(warr))
				return new StaticStatement((ClassStatement) parent, position,
						warr);
			if (MethodStatement.qualifies(warr))
				return new MethodStatement((ClassStatement) parent, position,
						warr);
			if (FieldStatement.qualifies(warr))
				return new FieldStatement((ClassStatement) parent, position,
						warr);
			if (ConstructorStatement.qualifies(warr))
				return new ConstructorStatement((ClassStatement) parent,
						position, warr);
			//
		} else if (parent instanceof InterfaceStatement) {
			if (MethodStatement.qualifies(warr))
				return new MethodStatement((InterfaceStatement) parent,
						position, warr);
			if (FieldStatement.qualifies(warr))
				return new FieldStatement((InterfaceStatement) parent,
						position, warr);
		}
		if ((parent instanceof FieldStatement)
				|| (parent instanceof ConstructorStatement)
				|| (parent instanceof MethodStatement)
				|| (parent instanceof UnimplementedStatement)
				|| (parent instanceof IfStatement)) {
			if (VariableDeclarationStatement.qualifies(warr))
				return new VariableDeclarationStatement(
						(AbstractStatement) parent, position, warr);
			if ((parent instanceof MethodStatement)
					&& (ReturnStatement.qualifies(warr)))
				return new ReturnStatement((AbstractStatement) parent,
						position, warr);
			if (IfStatement.qualifies(warr))
				return new IfStatement((AbstractStatement) parent, position,
						warr);
			return new UnimplementedStatement((AbstractStatement) parent,
					position, warr);
		}
		//
		throw new StatementException("Not recognized : " + statement);
	}

}
