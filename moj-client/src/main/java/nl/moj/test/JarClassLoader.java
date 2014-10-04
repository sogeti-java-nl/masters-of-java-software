package nl.moj.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Loads classes from a jar file.
 */
public class JarClassLoader extends ClassLoader
{

   private JarFile myJar;

   public JarClassLoader(JarFile j)
   {
      myJar = j;
   }

   public JarClassLoader(JarFile j, ClassLoader parent)
   {
      super(parent);
      myJar = j;
   }

   public Class< ? > loadClass(String name) throws ClassNotFoundException
   {
      checkPermissionToLoad(name);
      return super.loadClass(name);
   }

   protected synchronized Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException
   {
      checkPermissionToLoad(name);
      return super.loadClass(name, resolve);
   }

   public Class< ? > findClass(String name) throws ClassNotFoundException
   {
      try
      {
         JarEntry e = findJarEntry(name);
         if (e == null)
            throw new ClassNotFoundException(name);
         byte[] b = loadClassData(e);
         return defineClass(name, b, 0, b.length);
      }
      catch (IOException ex)
      {
         throw new ClassNotFoundException(name);
      }
   }

   private JarEntry findJarEntry(String name)
   {
      name = name.replace('.', '/') + ".class";
      return (JarEntry) myJar.getEntry(name);
   }

   private byte[] loadClassData(JarEntry e) throws IOException
   {
      InputStream in = myJar.getInputStream(e);
      byte[] data = new byte[(int) e.getSize()];
      int pos = 0;
      do
      {
         int cnt = in.read(data, pos, data.length - pos);
         if (cnt < 0)
            throw new IOException("Not enough bytes.");
         pos += cnt;

      }
      while (pos < data.length);
      //
      return data;
   }

   /** override to disallow access to certain classes. */
   public void checkPermissionToLoad(String name)
   {
   }

}
