package nl.moj.client.codecompletion;

/**
 *
 */
public interface CompletionSource {

    /**
     * adds this completion source to the root code node.
     * @param cnf the code node factory
     * @param root the root of the codenodes.
     * @return the code node representing the root of this source.
     */
    public CodeNode addToCodeTree(CodeNodeFactory cnf, CodeNode root);

}
