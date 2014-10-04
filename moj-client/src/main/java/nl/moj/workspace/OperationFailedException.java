package nl.moj.workspace;

/**
 * Thrown whenever an operation fails on the RemoteWorkspaceClient
 * 
 * @author E.Hooijmeijer
 */

public class OperationFailedException extends RuntimeException
{

   /**
    * <code>serialVersionUID</code> indicates/is used for.
    */
   private static final long serialVersionUID = -8802711606040413527L;

   public OperationFailedException()
   {
      super();
   }

   public OperationFailedException(String msg)
   {
      super(msg);
   }

   public OperationFailedException(String msg, Throwable t)
   {
      super(msg, t);
   }

}
