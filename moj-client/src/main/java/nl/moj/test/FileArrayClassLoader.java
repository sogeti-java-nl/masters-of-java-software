package nl.moj.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 */
public class FileArrayClassLoader extends ClassLoader
{

   private File[] myFiles;
   private byte[][] prefetchData;

   public FileArrayClassLoader(File[] f)
   {
      myFiles = f;
   }

   public FileArrayClassLoader(File[] f, ClassLoader parent)
   {
      super(parent);
      myFiles = f;
   }

   public FileArrayClassLoader(File[] f, ClassLoader parent, boolean prefetch)
   {
      super(parent);
      myFiles = f;
      if (prefetch)
         prefetch();
   }

   protected void prefetch()
   {
      prefetchData = new byte[myFiles.length][];
      for (int t = 0; t < myFiles.length; t++)
         try
         {
            if (myFiles[t] != null)
            {
               prefetchData[t] = loadClassData(myFiles[t]);
            }
         }
         catch (IOException ex)
         {
            throw new RuntimeException("Prefetch Failed for : " + myFiles[t]);
         }
   }

   public Class< ? > findClass(String name) throws ClassNotFoundException
   {
      try
      {
         File f = findFileEntry(name);
         if (f == null)
            throw new ClassNotFoundException(name);
         byte[] b = loadClassData(f);
         return defineClass(name, b, 0, b.length);
      }
      catch (IOException ex)
      {
         throw new ClassNotFoundException(name);
      }
   }

   private File findFileEntry(String name)
   {
      name = name.replace('.', '/') + ".class";
      for (int t = 0; t < myFiles.length; t++)
      {
         if (myFiles[t] == null)
            continue;
         if (myFiles[t].getAbsolutePath().endsWith(name))
            return myFiles[t];
      }
      return null;
   }

   private byte[] loadClassData(File f) throws IOException
   {
      //
      if (prefetchData != null)
      {
         for (int t = 0; t < myFiles.length; t++)
         {
            if (f.equals(myFiles[t]))
            {
               if (prefetchData[t] != null)
               {
                  return prefetchData[t];
               }
            }
         }
      }
      //
      byte[] data = new byte[(int) f.length()];
      //
      InputStream in = new FileInputStream(f);
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

}
