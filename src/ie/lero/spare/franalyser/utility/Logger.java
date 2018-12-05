package ie.lero.spare.franalyser.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ie.lero.spare.pattern_instantiation.IncidentPatternInstantiationListener;

public class Logger implements Runnable {

	private boolean isPrintToScreen = true;
	private boolean isSaveLog = false;
	private IncidentPatternInstantiationListener listener;
	private String logFolder = ".";
	private String logFileName;
	private BlockingQueue<String> msgQ = new ArrayBlockingQueue<String>(4000);
	private BufferedWriter bufferWriter;
	private LocalDateTime timeNow;

	private DateTimeFormatter dtfTime = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

	private Thread thread;
	public static final int MSG_INFO = 0;
	public static final int MSG_ERROR = 1;
	public static final int MSG_WARNING = 2;
	private static String msgTypeDelimiter = "&&";

	private static String terminatingString = "LoggingDone";
	private static String terminatingStringWithDelimiter = terminatingString + msgTypeDelimiter + MSG_INFO;

	private static final int DAY = 86400000;
	private static final String SEPARATOR = "*=========================================================================================================*";

	// private ScheduledExecutorService scheduledExecutor =
	// Executors.newSingleThreadScheduledExecutor();

	public static final String SEPARATOR_BTW_INSTANCES = ">>";

	public Logger() {

		timeNow = LocalDateTime.now();
		// set log file name
		logFileName = "log" + timeNow.getHour() + timeNow.getMinute() + timeNow.getSecond() + "_"
				+ timeNow.toLocalDate() + ".txt";

		// createLogFile();
	}

	public Logger(String fileName) {

		// set log file name
		logFileName = fileName;

		// createLogFile();
	}

	// public static Logger getInstance() {
	//
	// if (logger == null) {
	// logger = new Logger();
	// }
	//
	// return logger;
	// }

	public BufferedWriter createLogFile() {

		if (!isSaveLog) {
			return null;
		}

		bufferWriter = null;

		// create folder if it does not exist
		boolean isFolderCreated = true;
		File file = null;

		try {
			File folder = new File(logFolder);

			if (!folder.exists()) {
				isFolderCreated = folder.mkdir();
			}

			if (isFolderCreated) {
				if (!logFileName.endsWith(".txt")) {
					logFileName = logFileName + ".txt";
				}

				file = new File(logFolder + "/" + logFileName);
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

			// schedule a task to change format of timing after one day
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {

					dtfTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
				}
			}, DAY);

			String msg = (String) this.msgQ.take();

			// terminating message ends wirting and closes the file
			while (!msg.equals(Logger.terminatingStringWithDelimiter)) {

				if (isSaveLog || isPrintToScreen) {

					print(msg);

					// next msg
					msg = msgQ.take();
				} else {
					msgQ.take();
				}

			}

			if (isSaveLog) {
				bufferWriter.close();
			}

		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void print(String msg) {

		String timeStamp = "[" + dtfTime.format(LocalDateTime.now()) + "]";

		int index = msg.lastIndexOf("&&");
		int msgType = -1;

		if (index != -1) {
			msgType = Character.getNumericValue(msg.charAt(index + 2)); // get
																		// the
																		// type
			msg = msg.substring(0, index); // remove the type
		}

		msg = timeStamp + ": " + msg;

		switch (msgType) {
		case MSG_INFO:
			if (isPrintToScreen) {
				System.out.println(msg);

			}
			break;
		case MSG_WARNING:
			if (isPrintToScreen) {
				System.out.println("[Warning] " + msg);
			}
			break;
		case MSG_ERROR:
			if (isPrintToScreen) {
				System.err.println(msg);
			}
			break;
		default:
			if (isPrintToScreen) {
				System.out.println(msg);
			}
		}

		if (listener != null) {
			listener.updateLogger(msg);
		}

		try {
			if (isSaveLog) {
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

	/*
	 * public BlockingQueue<String> getMsgQ() { return msgQ; }
	 */

	public synchronized void putMessage(String msg) {

		putMessage(msg, MSG_INFO);

	}

	public synchronized void putError(String msg) {

		putMessage(msg, MSG_ERROR);

	}

	public synchronized void putWarning(String msg) {

		putMessage(msg, MSG_WARNING);

	}

	public void putSeparator() {

		putMessage(SEPARATOR);

	}

	public synchronized void putMessage(String msg, int msgType) {
		try {

			msg = msg + msgTypeDelimiter + msgType;
			msgQ.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized void terminateLogging() {

		putMessage(Logger.terminatingString);

	}

	public void start() {

		thread = new Thread(this);
		thread.start();
	}

	// public static void setInstanceNull() {
	//
	// logger = null;
	// }
}
