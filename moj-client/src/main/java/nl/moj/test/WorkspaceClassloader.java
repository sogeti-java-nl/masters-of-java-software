package nl.moj.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.moj.model.Workspace;

/**
 * Loads classes from the specified workspace.
 */

public class WorkspaceClassloader extends ClassLoader
{

   private Workspace.Internal myWorkspace;
   private boolean reverse;

   public WorkspaceClassloader(Workspace.Internal w, boolean reverse)
   {
      myWorkspace = w;
      this.reverse = reverse;
   }

   public WorkspaceClassloader(Workspace.Internal j, ClassLoader parent, boolean reverse)
   {
      super(parent);
      myWorkspace = j;
      this.reverse = reverse;
   }

   public Class< ? > loadClass(String name) throws ClassNotFoundException
   {
      checkPermissionToLoad(name);
      return super.loadClass(name);
   }

   protected synchronized Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      checkPermissionToLoad(name);
      if (reverse)
      {
         return reverseOrderLoadClass(name, resolve);
      }
      else
      {
         return super.loadClass(name, resolve);
      }
   }

   public Class< ? > findClass(String name) throws ClassNotFoundException
   {
      try
      {
         String file = name.replace('.', '/') + ".class";
         File target = new File(myWorkspace.getBinaryRoot(), file);
         if (!target.exists())
            throw new ClassNotFoundException(name);
         byte[] b = loadClassData(target);
         return defineClass(name, b, 0, b.length);
      }
      catch (IOException ex)
      {
         throw new ClassNotFoundException(name);
      }
   }

   private byte[] loadClassData(File f) throws IOException
   {
      InputStream in = new FileInputStream(f);
      byte[] data = new byte[(int) f.length()];
      try
      {
         int pos = 0;
         do
         {
            int cnt = in.read(data, pos, data.length - pos);
            if (cnt < 0)
               throw new IOException("Not enough bytes.");
            pos += cnt;
         }
         while (pos < data.length);
      }
      finally
      {
         in.close();
      }
      //
      return data;
   }

   /** override to disallow access to certain classes. */
   public void checkPermissionToLoad(String name)
   {
   }

   /**
    * reverseOrderLoadClass solves a problem when running the Eclipse plugin. When testing with the plugin there will be two copies of the players classes. One compiled in the MoJ workspace and one in
    * the Eclipse workspace. We want the one in the MoJ workspace, so we need to reverse the class loading.
    * 
    * @param name
    * @param resolve
    * @return
    * @throws ClassNotFoundException
    */
   protected synchronized Class< ? > reverseOrderLoadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      //
      Class< ? > c = findLoadedClass(name);
      //
      if (c == null)
      {
         //
         // If the class is a default package class then use
         // reverse class load order to load it (top classloader first) 
         // Otherwise use normal classloading (bottom classloader first)
         //
         if (name.indexOf('.') < 0)
         {
            c = findClass(name);
         }
         else
            try
            {
               c = super.loadClass(name, resolve);
            }
            catch (ClassNotFoundException e)
            {
               c = findClass(name);
            }
      }
      if (resolve)
      {
         resolveClass(c);
      }
      return c;
   }

}
