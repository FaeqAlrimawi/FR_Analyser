package ie.lero.spare.franalyser.utility;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class FileManipulator {
	
	public synchronized static boolean openFolder(String folderName) {
		boolean isOpened = false;
		
		Runtime run = Runtime.getRuntime();
	    String lcOSName = System.getProperty("os.name").toLowerCase();
	    
	    File myfile = new File(folderName);
		String path = myfile.getAbsolutePath();
		
	    boolean MAC_OS_X = lcOSName.startsWith("mac os x");
	    if (MAC_OS_X) {
	        try {
				run.exec("open " + path);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    } else if (lcOSName.startsWith("windows")) {
	        try {
	        	run.exec("explorer "+path);
	        	return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    } else if(lcOSName.startsWith("linux")) {
	    	 try {
					run.exec("xdg-open " + path);
					return true;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    }
	    
	    return isOpened;
	}
	
	public synchronized static boolean openFile(String fileName) {
		boolean isOpened = false;
		
		Runtime run = Runtime.getRuntime();
	
	    String lcOSName = System.getProperty("os.name").toLowerCase();
	    
	    boolean MAC_OS_X = lcOSName.startsWith("mac os x");
	    if (MAC_OS_X) {
	        try {
				run.exec("open " + fileName);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    } else if (lcOSName.startsWith("windows")) {
	        try {
	        
				 //run.exec("rundll32 url.dll, FileProtocolHandler " +fileName);
	        	Desktop.getDesktop().open(new File(fileName)); 
	        	return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    } else if(lcOSName.startsWith("linux")) {
	    	 try {
					run.exec("gedit " + fileName);
					return true;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	    }
	    
	    return isOpened;
	}
	
	public synchronized static String[] readFile(String fileName) {
		StringBuilder result = new StringBuilder();
		  
		    String tmp;
		    try {
		    	BufferedReader reader = new BufferedReader(new FileReader(fileName));
				while ((tmp = reader.readLine()) != null) {
					result.append(tmp);
				}
				  
			    reader.close();
			}  catch(FileNotFoundException e){
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  
		
		return result.toString().split(";");
	}
	
	public synchronized static String[] readFileNewLine(String fileName) {
		StringBuilder result = new StringBuilder();
		  
		    String tmp;
		    try {
		    	BufferedReader reader = new BufferedReader(new FileReader(fileName));
				while ((tmp = reader.readLine()) != null) {
					result.append(tmp).append("\n");
				}
				  
			    reader.close();
			}  catch(FileNotFoundException e){
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  
		
		return result.toString().split("\n");
	}
	
	
}
