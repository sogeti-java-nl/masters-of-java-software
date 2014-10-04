package nl.moj.client.codecompletion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Uses reflection to determine possible completions. Only useful for known classes.
 */

public class TypeCodeCompletion implements CompletionSource {

    private Class<?> myType;

    public TypeCodeCompletion(String type) {
        try {
            myType = Class.forName(type);
        } catch (ClassNotFoundException ex) {
            myType = null;
        }
    }

    public TypeCodeCompletion(Class<?> type) {
        myType = type;
    }

    /**
     * returns the possible completions for the specified type.
     * @param cng
     * @param root
     */
    public CodeNode addToCodeTree(CodeNodeFactory cng, CodeNode root) {
        if (myType == null) return null;
        //
        root = cng.addDotSeparated(myType.getName(), root);
        //
        Field[] f = myType.getFields();
        for (int t = 0; t < f.length; t++) {
            cng.addReferenceIfNotExist(root, f[t].getName(), f[t].getType().getName(), Modifier.isStatic(f[t].getModifiers()),f[t].getType().isArray());
        }
        //
        Method[] m = myType.getMethods();
        for (int t = 0; t < m.length; t++) {
            cng.addReferenceIfNotExist(root, m[t].getName(), m[t].getReturnType().getName(), Modifier.isStatic(m[t].getModifiers()),m[t].getReturnType().isArray());
        }
        if (myType.equals(Enum.class)) {
            cng.addReferenceIfNotExist(root, "values", "java.lang.Object" , true,false);
        }
        //
        Constructor<?>[] c = myType.getConstructors();
        for (int t = 0; t < c.length; t++) {
            String name = c[t].getName();
            if (name.lastIndexOf('.') >= 0) name = name.substring(name.lastIndexOf('.') + 1);
            cng.addReferenceIfNotExist(root, name, myType.getName(), true, false);
        }
        //
        return root;
    }

}
