package nl.moj.workspace;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.moj.assignment.JarFileAssignment;
import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.model.Operation.Context;
import nl.moj.model.Team;
import nl.moj.model.Tester;
import nl.moj.model.Tester.TestResult;
import nl.moj.model.Workspace;
import nl.moj.operation.ContextImpl;
import nl.moj.process.ProcessPool.ProcessListener;
import nl.moj.workspace.io.AssignmentMessage;
import nl.moj.workspace.io.ContentsRequest;
import nl.moj.workspace.io.GoodbyeMessage;
import nl.moj.workspace.io.Message;
import nl.moj.workspace.io.PerformMessage;
import nl.moj.workspace.io.WorkspaceMessageFactory;

/**
 * Workspace Client which connects to a RemoteWorkspace. Only those calls that need to be executed remotely are executed remotely. A copy of the editable file(s) is kept so that if the server fails
 * the workspace can be recreated elsewhere.
 * 
 * @author E.Hooijmeijer
 */

public class RemoteWorkspaceClient implements Workspace
{

   public static class ProcessResults implements Runnable, Team.Results
   {
      private Operation op;

      public ProcessResults(Operation op)
      {
         this.op = op;
      }

      public Operation getOperation()
      {
         return op;
      }

      public void run()
      {
      }
   }

   public static class TestResults extends ProcessResults implements Team.TestResults
   {
      private Tester.TestResult tr;

      public TestResults(Operation op, Tester.TestResult tr)
      {
         super(op);
         this.tr = tr;
      }

      public TestResult getTestResults()
      {
         return tr;
      }

      public void run()
      {
      }
   }

   public static class CompileResults extends ProcessResults implements Team.CompileResults
   {
      private boolean success;

      public CompileResults(Operation op, boolean success)
      {
         super(op);
         this.success = success;
      }

      public boolean wasSuccess()
      {
         return success;
      }

      public void run()
      {
      }
   }

   private Assignment assignment;
   private Operation[] ops;

   private Socket socket;
   private DataInputStream in;
   private DataOutputStream out;
   private LoadBalancer loadBalancer;

   private String team;
   private WorkspaceMessageFactory factory = new WorkspaceMessageFactory();
   private OutputRedirector.Target target;
   private ProcessListener pListener;

   public RemoteWorkspaceClient(String team, LoadBalancer lb, OutputRedirector.Target target, ProcessListener pListener)
   {
      //
      if (lb == null)
         throw new NullPointerException("NULL LoadBalancer");
      if (team == null)
         throw new NullPointerException("NULL Team Name");
      if (target == null)
         throw new NullPointerException("NULL OutputRedirector.Target");
      if (pListener == null)
         throw new NullPointerException("NULL ProcessListener");
      //
      this.team = team;
      this.target = target;
      this.pListener = pListener;
      //
      loadBalancer = lb;
      //
   }

   public synchronized void connect() throws IOException
   {
      connect(false);
   }

   public synchronized void connect(boolean resume) throws IOException
   {
      if ((socket == null) || (socket.isClosed() || (!socket.isConnected())))
      {
         //
         socket = loadBalancer.getWorkspaceServerConnection(team, resume);
         //
         in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
         out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
         //
         target.append(null, "(re)Connected to '" + socket.getRemoteSocketAddress() + "'\n");
         //
         if (assignment != null)
         {
            target.append(null, "Loading assignment\n");
            loadAssignment(assignment, true);
            target.append(null, "Restoring previous state\n");
            performBlocked(getOperationByName("Save"), getCachedContext());
            target.append(null, "Ready.\n");
         }
      }
   }

   public synchronized void forceClose()
   {
      if (socket != null)
         try
         {
            target.append(null, "Connection closed.\n");
            loadBalancer.reportClosing(socket);
            socket.close();
            socket = null;
         }
         catch (IOException ex)
         {
            throw new OperationFailedException("Error closing Socket", ex);
         }
   }

   public String getName()
   {
      return team;
   }

   public void loadAssignment(Assignment a, boolean resume) throws IOException
   {
      //
      if (a == null)
         throw new NullPointerException("Assignment is NULL");
      //
      // First, make a connection.
      //
      connect(resume);
      //
      // Then set and load the assignment.
      //
      assignment = a;
      ops = assignment.getOperations();
      //
      try
      {
         new AssignmentMessage(team, resume, (JarFileAssignment) a).write(out);
         out.flush();
      }
      catch (IOException ex)
      {
         loadBalancer.reportFailure(socket);
         forceClose();
      }
      //
   }

   //
   // Methods that must be handled remotely.
   //	

   public void perform(Operation op, Context ctx)
   {
      try
      {
         connect();
         //
         storeInCache(ctx);
         //
         new PerformMessage(op.getName(), ctx).write(out);
         out.flush();
      }
      catch (IOException ex)
      {
         loadBalancer.reportFailure(socket);
         forceClose();
         throw new OperationFailedException("Failed sending perform-msg", ex);
      }
   }

   /**
    * blocked version of the perform. Waits until the operation signals 'completed' Note that update() may not be called during this time as only one thread can read from the input stream !
    */
   public synchronized void performBlocked(Operation op, Context ctx)
   {
      try
      {
         connect();
         //
         storeInCache(ctx);
         //
         new PerformMessage(op.getName(), ctx).write(out);
         out.flush();
         //
         Message.ProcessState result = null;
         while (result == null)
            try
            {
               Message msg = factory.createMessage(in);
               if (msg instanceof Message.ProcessState)
               {
                  result = (Message.ProcessState) msg;
                  if (!result.isFinished())
                     result = null;
               }
               else
               {
                  dispatch(msg);
               }
            }
            catch (SocketTimeoutException ex)
            {
               // Ignore
            }
         //
      }
      catch (IOException ex)
      {
         loadBalancer.reportFailure(socket);
         forceClose();
         throw new OperationFailedException("Failed sending perform-msg", ex);
      }
   }

   /**
    * Blocking call that retrieves the contents of a file. Note that update() may not be called during this time as only one thread can read from the input stream !
    */
   public synchronized String getContents(String file) throws IOException
   {
      try
      {
         connect();
         new ContentsRequest(file).write(out);
         out.flush();
         //
         Message.ContentsReply result = null;
         while (result == null)
            try
            {
               Message msg = factory.createMessage(in);
               if (msg instanceof Message.ContentsReply)
               {
                  result = (Message.ContentsReply) msg;
                  if (!isReadOnly(file))
                  {
                     storeInCache(file, result.getContents());
                  }
               }
               else
               {
                  dispatch(msg);
               }
            }
            catch (SocketTimeoutException ex)
            {
               // Ignore
            }
         //
         return result.getContents();
         //
      }
      catch (IOException ex)
      {
         loadBalancer.reportFailure(socket);
         forceClose();
         throw new OperationFailedException("Failed sending getContents-msg", ex);
      }
   }

   public void dispose()
   {
      try
      {
         connect();
         new GoodbyeMessage(true).write(out);
         out.flush();
      }
      catch (IOException ex)
      {
         loadBalancer.reportFailure(socket);
         forceClose();
         throw new OperationFailedException("Failed disposing of workspace.");
      }
   }

   public void suspend()
   {
      try
      {
         connect();
         new GoodbyeMessage(false).write(out);
         out.flush();
      }
      catch (IOException ex)
      {
         loadBalancer.reportFailure(socket);
         forceClose();
         throw new OperationFailedException("Failed suspending of workspace.");
      }
   }

   /**
    * Reads and dispatches any incoming messages. No blocking calls can be executed while this method is running.
    */
   public synchronized void update() throws IOException
   {
      //
      try
      {
         connect();
         while (in.available() > 0)
         {
            Message msg = factory.createMessage(in);
            dispatch(msg);
         }
      }
      catch (SocketTimeoutException ex)
      {
         // Ignore
      }
      catch (IOException ex)
      {
         loadBalancer.reportFailure(socket);
         forceClose();
      }
   }

   protected void dispatch(Message msg)
   {
      switch (msg.getType())
      {
         case Message.MSG_CONSOLE:
            target.append(((Message.Console) msg).getContext(), ((Message.Console) msg).getContent());
            break;
         case Message.MSG_PROCESSSTATE:
            Message.ProcessState pst = (Message.ProcessState) msg;
            Operation op = this.getOperationByName(pst.getOperationName());
            Runnable r = null;
            //
            if (op != null)
            {
               if (pst.getTestResults() != null)
               {
                  r = new TestResults(op, pst.getTestResults());
               }
               else
               {
                  r = new CompileResults(op, pst.wasSuccess());
               }
            }
            else
               r = new ProcessResults(op);
            //
            if (pst.isQueued())
            {
               pListener.queued(r);
            }
            else if (pst.isExecuting())
            {
               pListener.executing(r);
            }
            else if (pst.isFinished())
            {
               pListener.complete(r);
            }
            //
            break;
      }
   }

   //
   // Methods that can be handled locally.
   //

   public Operation[] getAllOperations()
   {
      return ops;
   }

   public Operation getOperationByName(String name)
   {
      for (int t = 0; t < ops.length; t++)
      {
         if (ops[t].getName().equals(name))
            return ops[t];
      }
      return null;
   }

   public boolean isJava(String file)
   {
      if (file == null)
         return false;
      return file.endsWith(".java");
   }

   public boolean isMonospaced(String file)
   {
      if (file == null)
         return false;
      if (!file.endsWith(".txt"))
         return false;
      return assignment.isDescriptionRenderedInMonospaceFont();
   }

   public boolean isReadOnly(String file)
   {
      String[] names = assignment.getEditableFileNames();
      for (int t = 0; t < names.length; t++)
      {
         if (names[t].equals(file))
            return false;
      }
      return true;
   }

   public String[] getEditorFiles()
   {
      //
      List<String> r = new ArrayList<>();
      //
      r.addAll(Arrays.asList(assignment.getDescriptionFileNames()));
      r.addAll(Arrays.asList(assignment.getSourceCodeFileNames()));
      //
      return r.toArray(new String[r.size()]);
   }

   //
   // Local caching for failover to other server.
   //

   private Map<String, String> localCache = new HashMap<>();

   protected synchronized void storeInCache(String file, String contents)
   {
      localCache.put(file, contents);
   }

   protected synchronized void storeInCache(Operation.Context ctx)
   {
      String[] names = ctx.getNames();
      for (int t = 0; t < names.length; t++)
      {
         String content = ctx.getContents(names[t]);
         storeInCache(names[t], content);
      }
   }

   protected synchronized Operation.Context getCachedContext()
   {
      String[] names = new String[localCache.size()];
      String[] values = new String[localCache.size()];
      names = localCache.keySet().toArray(names);
      for (int t = 0; t < names.length; t++)
      {
         values[t] = localCache.get(names[t]);
      }
      return new ContextImpl(names, values, -1);
   }

}
