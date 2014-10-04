package nl.moj.workspace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import nl.ctrlaltdev.io.OutputRedirector;
import nl.ctrlaltdev.io.OutputRedirector.Target;
import nl.moj.model.Assignment;
import nl.moj.model.Operation;
import nl.moj.model.Operation.Context;
import nl.moj.model.Workspace;
import nl.moj.process.ProcessPool.ProcessListener;

/**
 * Local Workspace resides on the machine it runs on. All Operations are executed using that machine. This works nicely up to 32 concurrent workspaces. After that compilation and testing will probably
 * exceed available CPU and memory resources.
 * 
 * @author E.Hooijmeijer
 */

public class LocalWorkspace implements Workspace.Internal, ProcessListener
{

   private static final Logger log = Logger.getLogger("LocalWorkspace");

   private File projectRoot;
   private File sourceRoot;
   private File binRoot;
   private String myName;
   private Map<String, String> currentState;
   private boolean isCompiled;
   private boolean textFileIsMonospaced;
   private Operation[] operations;
   private OutputRedirector.Target outputTarget;
   private ProcessListener processListener;
   private Operation currentOperation;
   private String[] editableFiles = new String[0];

   /**
    * creates the named workspace and any directories needed.
    */
   public LocalWorkspace(String name, OutputRedirector.Target tm, ProcessListener lst) throws IOException
   {
      super();
      //
      myName = name;
      currentState = new HashMap<>();
      operations = new Operation[0];
      outputTarget = tm;
      processListener = lst;
      //
      projectRoot = new File("./workspace/" + name);
      if (!projectRoot.exists())
      {
         if (!projectRoot.mkdirs())
            throw new IOException("Failed to create project root.");
      }
      sourceRoot = new File(projectRoot, "src");
      if (!sourceRoot.exists())
      {
         if (!sourceRoot.mkdirs())
            throw new IOException("Failed to create source root.");
      }
      binRoot = new File(projectRoot, "bin");
      if (!binRoot.exists())
      {
         if (!binRoot.mkdirs())
            throw new IOException("Failed to create bin root.");
      }
      //
   }

   public Operation[] getAllOperations()
   {
      return operations;
   }

   public Operation getOperationByName(String name)
   {
      for (int t = 0; t < operations.length; t++)
      {
         if (operations[t].getName().equals(name))
            return operations[t];
      }
      return null;
   }

   public synchronized void perform(Operation op, Context ctx)
   {
      if (currentOperation == null)
      {
         currentOperation = getOperationByName(op.getName());
         if (currentOperation == null)
            throw new NullPointerException("Unsupported operation '" + op.getName() + "'");
         currentOperation.perform(this, this, ctx);
      }
      else
         outputTarget.append(null, "Busy.");
   }

   public synchronized boolean isPerforming()
   {
      return (currentOperation != null);
   }

   public void update() throws IOException
   {
      // No point of doing something here. All direct access.
   }

   //
   // ProcessListener
   //

   public void complete(Runnable r)
   {
      currentOperation = null;
      processListener.complete(r);
   }

   public void executing(Runnable r)
   {
      processListener.executing(r);
   }

   public void queued(Runnable r)
   {
      processListener.queued(r);
   }

   //
   // OutputRedirector
   // 
   public Target getTarget()
   {
      return outputTarget;
   }

   public String getName()
   {
      return myName;
   }

   /**
    * loads an assignment into the workspace.
    * 
    * @param assignment the assignment to load.
    * @param resume if true, workspace contents are not deleted and rewritten so an existing assignment can continue.
    * @throws IOException
    */
   public void loadAssignment(Assignment assignment, boolean resume) throws IOException
   {
      //
      editableFiles = assignment.getEditableFileNames();
      textFileIsMonospaced = assignment.isDescriptionRenderedInMonospaceFont();
      operations = assignment.getOperations();
      //
      // Wipeout !
      //
      if (!resume)
         delTree(projectRoot);
      //
      // Description files.
      //
      String[] name = assignment.getDescriptionFileNames();
      for (int t = 0; t < name.length; t++)
      {
         InputStream in = assignment.getAssignmentFileData(name[t]);
         File f = new File(projectRoot, name[t]);
         // (re)create missing files in resume mode.
         if ((resume) && (f.exists()))
            continue;
         // overwrite always in non-resume mode.
         if (!f.exists())
            if (!f.createNewFile())
               throw new IOException("Unable to create :" + f);
         OutputStream out = new FileOutputStream(f);
         copyStream(in, out);
      }
      //
      // Source Files.
      //		
      name = assignment.getSourceCodeFileNames();
      for (int t = 0; t < name.length; t++)
      {
         InputStream in = assignment.getAssignmentFileData(name[t]);
         File f = new File(sourceRoot, name[t]);
         // (re)create missing files in resume mode.
         if ((resume) && (f.exists()))
            continue;
         // overwrite always in non-resume mode.
         if (!f.exists())
            if (!f.createNewFile())
               throw new IOException("Unable to create :" + f);
         if (resume)
            log.warning("Writing '" + f + "' - its missing");
         OutputStream out = new FileOutputStream(f);
         copyStream(in, out);
      }
      //
   }

   public void dispose()
   {
      try
      {
         delTree(projectRoot);
      }
      catch (IOException ex)
      {
         throw new RuntimeException("Unable to dispose of workspace files : " + ex.getMessage());
      }
   }

   public void suspend()
   {
      // Nothing to do here 
   }

   public File getProjectRoot()
   {
      return projectRoot;
   }

   public File[] getProjectFiles()
   {
      return projectRoot.listFiles(new FileFilter()
         {
            public boolean accept(File f)
            {
               if (f.isDirectory())
                  return false;
               return true;
            }
         });
   }

   public File getSourceRoot()
   {
      return sourceRoot;
   }

   public File[] getSourceFiles()
   {
      return sourceRoot.listFiles(new FileFilter()
         {
            public boolean accept(File f)
            {
               if (f.isDirectory())
                  return false;
               return f.getName().endsWith(".java");
            }
         });
   }

   public void clearBinary() throws IOException
   {
      isCompiled = false;
      delTree(binRoot);
   }

   public File getBinaryRoot()
   {
      return binRoot;
   }

   public String[] getEditorFiles()
   {
      List<String> files = new ArrayList<>();
      File[] f = this.getProjectFiles();
      for (int t = 0; t < f.length; t++)
      {
         files.add(f[t].getName());
      }
      f = this.getSourceFiles();
      for (int t = 0; t < f.length; t++)
      {
         files.add(f[t].getName());
      }
      return files.toArray(new String[files.size()]);
   }

   public boolean isJava(String name)
   {
      if (!name.endsWith(".java"))
         return false;
      File f = getAsFile(name);
      return f.exists();
   }

   public boolean isMonospaced(String name)
   {
      if (name.endsWith(".txt"))
      {
         return textFileIsMonospaced;
      }
      return false;
   }

   public boolean isReadOnly(String name)
   {
      if (!isJava(name))
         return true;
      for (int t = 0; t < editableFiles.length; t++)
      {
         if (name.equals(editableFiles[t]))
            return false;
      }
      return true;
   }

   public String getContents(String name) throws IOException
   {
      File f = getAsFile(name);
      if (f == null)
         throw new NullPointerException("Unknown file : " + name);
      return read(new BufferedReader(new FileReader(f)));
   }

   public boolean save(String name, String contents) throws IOException
   {
      //
      if (name == null)
         throw new NullPointerException("Cannot save a NULL file.");
      if (contents == null)
         throw new NullPointerException("Cannot save NULL contents to " + name);
      // Don't allow to save readonly files.
      if (isReadOnly(name))
         throw new IOException("File is readonly.");
      // Check if the file exists in the workspace.
      File f = getAsFile(name);
      if (f == null)
         throw new NullPointerException("Unknown file : " + name);
      //
      // Check if the file is modified.
      //
      String last = currentState.get(name);
      if (last == null)
      {
         write(f, contents);
         currentState.put(name, contents);
         return true;
      }
      else
      {
         if (!last.equals(contents))
         {
            write(f, contents);
            currentState.put(name, contents);
            return true;
         }
         else
         {
            return false;
         }
      }
   }

   protected File getAsFile(String file)
   {
      File[] f = this.getSourceFiles();
      for (int t = 0; t < f.length; t++)
      {
         if (f[t].getName().equals(file))
            return f[t];
      }
      //
      f = this.getProjectFiles();
      for (int t = 0; t < f.length; t++)
      {
         if (f[t].getName().equals(file))
            return f[t];
      }
      //
      return null;
   }

   /** USE WITH CARE ! */
   private void delTree(File f) throws IOException
   {
      // Do not delete files that start with a .
      if (f.getName().startsWith("."))
         return;
      //
      if (f.isDirectory())
      {
         File[] ff = f.listFiles();
         for (int t = 0; t < ff.length; t++)
            delTree(ff[t]);
      }
      //
      if (f.isDirectory())
         return;
      if (!f.delete())
         throw new IOException("Failed to delete " + f);
      //
   }

   /** copies one stream to another and closes the streams. */
   private static void copyStream(InputStream in, OutputStream out) throws IOException
   {
      try
      {
         byte[] buffer = new byte[8192];
         int rd;
         do
         {
            rd = in.read(buffer, 0, buffer.length);
            if (rd > 0)
            {
               out.write(buffer, 0, rd);
            }
         }
         while (rd >= 0);
      }
      finally
      {
         out.close();
         in.close();
      }
   }

   /** reads a text file */
   private static String read(BufferedReader in) throws IOException
   {
      StringBuffer result = new StringBuffer();
      try
      {
         String s = in.readLine();
         while (s != null)
         {
            int idx = -1;
            // Remove any tabs and replace them with spaces.
            while ((idx = s.indexOf('\t')) >= 0)
            {
               s = s.substring(0, idx) + "    " + s.substring(idx + 1);
            }
            result.append(s);
            result.append("\n");
            s = in.readLine();
         }
      }
      finally
      {
         in.close();
      }
      return result.toString();
   }

   /** writes a text file */
   private void write(File f, String data) throws IOException
   {
      BufferedWriter out = new BufferedWriter(new FileWriter(f));
      isCompiled = false;
      try
      {
         out.write(data);
         out.flush();
      }
      finally
      {
         out.close();
      }
   }

   public boolean isCompiled()
   {
      return isCompiled;
   }

   public void markCompiled()
   {
      isCompiled = true;
   }

}
