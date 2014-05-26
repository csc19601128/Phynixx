/**
 * 
 */
package org.csc.phynixx.connection.reference.scenarios;

import java.io.File;


public class TmpDirectory 
{
	private static final String MY_TMP="scenarios";
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
	
	/**
	 * removes all files but keeps the directories ...
	 */
	public void clear() {
		
		if( dir==null) {
			return;
		}
		File[] files=dir.listFiles();
		for (int i=0; i <files.length;i++) {
			files[i].delete();
		}
	}
	
	/**
	 * removes all files and directories relative to java.io.tmpdir ...
	 */
	public void rmdir() {
		
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
	
	
	
}