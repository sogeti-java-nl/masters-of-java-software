package nl.ctrlaltdev.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileTool {

	/**
	 * performs a recursive copy of the specified file resource.
	 * @param src source folder or file.
	 * @param target folder or file.
	 * @throws IOException if something fails.
	 */
	public static void copy(File src,File target,boolean excludeCVS) throws IOException {
		//
		if (src.getName().equals("CVS")&&excludeCVS) return;
		//
		if (src.isFile()&&target.isFile()) {
			fileCopy(src,target);
		} else if (src.isFile()&&target.isDirectory()) {
			fileCopy(src,new File(target,src.getName()));
		} else if (src.isDirectory()&&target.isFile()) {
			throw new IOException("Unable to copy a dir->file.");
		} else {
			File[] srcFiles=src.listFiles();
			for (int t=0;t<srcFiles.length;t++) {
				File targetFile=new File(target,srcFiles[t].getName());
				if (srcFiles[t].isDirectory()) {
					targetFile.mkdir();
					copy(srcFiles[t],targetFile,excludeCVS);
				} else {
					fileCopy(srcFiles[t],targetFile);
				}
			}
		}
	}
	
	public static void fileCopy(File src,File target) throws IOException {
		long length=src.length();
		byte[] buffer=new byte[8192];
		InputStream fin=new BufferedInputStream(new FileInputStream(src));
		try {
			BufferedOutputStream fout=new BufferedOutputStream(new FileOutputStream(target));
			try {
				int sum=0;
				do {
					int r=fin.read(buffer);
					if (r>=0) {
						fout.write(buffer,0,r);
						sum+=r;
					} else {
						throw new IOException("Read less bytes than expected.");
					}
				} while (sum<length);
			} finally {
				fout.close();
			}
		} finally {
			fin.close();
		}
	}	
	
}
