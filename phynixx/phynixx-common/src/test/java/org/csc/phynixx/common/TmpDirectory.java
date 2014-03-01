/**
 * 
 */
package org.csc.phynixx.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;


public class TmpDirectory 
{
	private static final String MY_TMP="de_csc_xaresource";
	private File dir= null;
	

	public TmpDirectory(String relDirectory) 
	{
		String tmpDir= System.getProperty("java.io.tmpdir");
		dir= new File(tmpDir+File.separator+relDirectory);
		if( !dir.exists()) {
			dir.mkdirs();
		} 			
	} 		

	public TmpDirectory() 
	{
		this(MY_TMP);
	} 		
	
	public File getDirectory() {
		return this.dir;
	}
	
	public void clear() {
		
		if( dir==null) {
			return;
		}
		File[] files=dir.listFiles();
		for (int i=0; i <files.length;i++) {
			files[i].delete();
		}
		this.dir.delete();
		this.dir=null; 
	}	
	
	public File assertExitsFile(String filename) throws IOException 
	{
		File parentDir= this.assertExitsDirectory(FilenameUtils.getPath(filename));		

		String name=  FilenameUtils.getName(filename); 		
		String fullname= FilenameUtils.normalize(parentDir.getAbsolutePath()+File.separator+name);
		File file= new File(fullname);		
		file.createNewFile();
		
		return file; 
		
	}
	
	public File assertExitsDirectory(String dirname) throws IOException 
	{
		
		File directory= new File(this.dir.getAbsolutePath()+File.separator+dirname);
		if( directory.exists() && !directory.isDirectory()) {
			throw new IllegalStateException(dirname +" is not a directory");
		}
		if( !directory.exists()) {
			directory.mkdirs();
		}
		return directory;
		
	}
	
	
}