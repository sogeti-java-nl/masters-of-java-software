package nl.moj.client.codecompletion.statement;

import java.util.StringTokenizer;

/**
 *
 */
public class MethodStatement extends DeclarationStatement {

    private String myType;
    private String myName;
    private String myParams;
    private DeclarationStatement myParent;

    public MethodStatement(DeclarationStatement parent, int pos, String[] words) {
        super(words, pos);
        int cnt = 0;
        // Skip any modifiers.
        while ((cnt < words.length) && (isModifier(words[cnt])))
            cnt++;
        // Next is Type
        myType = words[cnt];
        cnt++;
        if (words[cnt].equals("[]")) {
        	myType=myType+"[]";
        	cnt++;
        }
        myName = words[cnt].substring(0, words[cnt].indexOf("("));
        myParams = words[cnt].substring(words[cnt].indexOf("(") + 1, words[cnt].indexOf(")"));
        //
        myParent = parent;
    }

    public String getType() {
        return expandImport(myType);
    }
    
    public boolean isArray() {
    	return myType.endsWith("[]");
    }

    protected String expandImport(String type) {
        Statement p = this;
        while (p.getParent() != null) {
            p = p.getParent();
        }
        return ((JavaFile) p).expandImport(type);
    }

    public String getName() {
        return myName;
    }

    public Declaration[] getParams() {
        StringTokenizer st = new StringTokenizer(myParams, ",");
        Declaration[] result = new Declaration[st.countTokens()];
        for (int t = 0; t < result.length; t++) {
            String combined = st.nextToken();
            String tp = combined.substring(0, combined.indexOf(' '));
            String nm = combined.substring(combined.indexOf(' ') + 1);
            result[t] = new Declaration(expandImport(tp), nm,tp.endsWith("[]"));
        }
        return result;
    }

    public static boolean qualifies(String[] words) {
        int cnt = 0;
        // Skip any modifiers.
        while ((cnt < words.length) && (isModifier(words[cnt])))
            cnt++;
        if (cnt > words.length - 2) return false;
        //
        if (words[cnt+1].equals("[]")) {
        	cnt++;
        }        
        if (cnt > words.length - 2) return false;
        // Next should be a type.
        if (!isPrimitive(words[cnt])) {
            //
            // Must be a class then..
            //
        }
        //
        // Check for opening and closing brackets.
        //
        String method = words[cnt + 1];
        if (method.indexOf("(") < 0) return false;
        if (method.indexOf(")") < 0) return false;
        //    	
        return true;
    }

    public Declaration getDeclaration() {
        return new Declaration(getType(), getName(),isArray());
    }

    public String toString() {
        return "Method(" + myName + " returns " + myType + " with args " + myParams + ")";
    }

    public Statement getParent() {
        return myParent;
    }

}
