package ie.lero.spare.franalyser.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import i.e.lero.spare.pattern_instantiation.IncidentPatternInstantiationListener;

public class Logger implements Runnable{

	//private BlockingQueue msgQ;
	private boolean isPrintToScreen = true;
	private boolean isSaveLog = false;
	private IncidentPatternInstantiationListener listener;
	private String logFolder = "etc/scenario1/log";
	private String logFileName;
	private BlockingQueue<String> msgQ = new ArrayBlockingQueue<String>(2000);
	private BufferedWriter bufferWriter;
	private LocalDateTime timeNow;
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"); 
	private DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("HH:mm:ss:SSS"); 
	private static Logger logger = new Logger();
	public static String terminatingString = "LoggingDone";
	
	private Logger() {
		
		timeNow = LocalDateTime.now();
		//set log file name
		logFileName = "log"+timeNow.getHour()+timeNow.getMinute()+timeNow.getSecond()+"_"+timeNow.toLocalDate()+".txt";	
		createLogFile();
	}
	
	public static Logger getInstance() {
		
		if(logger == null) {
			logger = new Logger();
		}
		
		return logger;
	}
	
	private BufferedWriter createLogFile() {
		
		if(!isSaveLog) {
			return null;
		}
		
		bufferWriter = null;
		
		//create folder if it does not exist
		boolean isFolderCreated = true;
		File file = null;
		
		try {
		File folder = new File(logFolder);
		
		
		
		if(!folder.exists()) {
			isFolderCreated = folder.mkdir();
		}
		
		if(isFolderCreated) {
			if(!logFileName.endsWith(".txt")) {
				logFileName = logFileName+".txt";
			}
			
			file = new File(logFolder+"/"+logFileName);
			if (!file.exists()) {
				file.createNewFile();	
	        }	
		}
		
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		bufferWriter = new BufferedWriter(fw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bufferWriter;
	}

	@Override
	public void run() {
		
		try {
			
			String msg = (String) logger.msgQ.take();
			
			//message "done" ends wirting and closes the file
			while(!msg.equalsIgnoreCase(Logger.terminatingString)) {
				
				if(isSaveLog || isPrintToScreen) {
					print(msg);
					msg = msgQ.take();
				} else {
					msgQ.take();
				}
				
				
			}
			
			if(isSaveLog) {
				bufferWriter.close();	
			}
			
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void print(String msg) {
		
		String timeStamp = "["+dtfTime.format(LocalDateTime.now())+"]";
		msg = timeStamp+" : "+msg;
		
		if(isPrintToScreen) {
			System.out.println(msg);
		}
		
		if(listener != null) {
		listener.updateLogger(msg);
		}
		
		try {
			if(isSaveLog) {
				bufferWriter.write(msg);
				bufferWriter.newLine();	
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			print(e.getMessage());
		}
	}

	public boolean isPrintToScreen() {
		return isPrintToScreen;
	}

	public void setPrintToScreen(boolean isPrintToScreen) {
		this.isPrintToScreen = isPrintToScreen;
	}

	public boolean isSaveLog() {
		return isSaveLog;
	}

	public void setSaveLog(boolean isSaveLog) {
		this.isSaveLog = isSaveLog;
	}

	public IncidentPatternInstantiationListener getListener() {
		return listener;
	}

	public void setListener(IncidentPatternInstantiationListener listener) {
		this.listener = listener;
	}

	public String getLogFolder() {
		return logFolder;
	}

	public void setLogFolder(String logFolder) {
		this.logFolder = logFolder;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}


	public  BlockingQueue<String> getMsgQ() {
		return msgQ;
	}
	
	
}
