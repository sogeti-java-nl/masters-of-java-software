package nl.moj.model;

/**
 * Provides a {@link Workspace.Internal} property. 
 * To have your {@link Tester.Testable}s access the `current' workspace
 * simply implement this interface also.
 * @author geenenju
 */
public interface HasWorkspace {
    
    Workspace.Internal getWorkspace();

    void setWorkspace(Workspace.Internal ws);
    
}
