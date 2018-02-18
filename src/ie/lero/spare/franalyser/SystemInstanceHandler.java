package ie.lero.spare.franalyser;

import ie.lero.spare.franalyser.utility.TransitionSystem;

public class SystemInstanceHandler {

	private static String outputFolder;
	private static SystemExecutor executor;
	private static TransitionSystem transitionSystem;
	private static String fileName;
	private static boolean isSystemAnalysed = false;
	
	public static boolean analyseSystem(String fileName) {
		

		if(executor == null || fileName == null) {
			return isSystemAnalysed = false;
		}
		
		if(isSystemAnalysed && fileName.equals(SystemInstanceHandler.fileName)) {
			return true;
		}
		
		SystemInstanceHandler.fileName = fileName;
		
		outputFolder = executor.execute(fileName);
		
		if(outputFolder != null) {
			return isSystemAnalysed = true;
		}
		
		return isSystemAnalysed = false;
	}
	
	public static boolean analyseSystem(String fileName, SystemExecutor exec) {
		executor = exec;
		return analyseSystem(fileName);
	}

	public static String getOutputFolder() {
		return outputFolder;
	}

	public static void setOutputFolder(String outputFolder) {
		SystemInstanceHandler.outputFolder = outputFolder;
	}

	public static SystemExecutor getExecutor() {
		return executor;
	}

	public static void setExecutor(SystemExecutor executor) {
		SystemInstanceHandler.executor = executor;
	}

	public static TransitionSystem getTransitionSystem() {
		if(transitionSystem == null) {
			if(outputFolder != null) {
				TransitionSystem.setFileName(outputFolder+"/transitions");
				transitionSystem = TransitionSystem.getTransitionSystemInstance();
			}
		}
		
		return transitionSystem;
	}

	public static void setTransitionSystem(TransitionSystem transitionsystem) {
		SystemInstanceHandler.transitionSystem = transitionsystem;
	}

	public static String getFileName() {
		return fileName;
	}

	public static void setFileName(String fileName) {
		SystemInstanceHandler.fileName = fileName;
		isSystemAnalysed = false;
		outputFolder = null;
	}

	public static boolean isSystemAnalysed() {
		return isSystemAnalysed;
	}

	public static void setSystemAnalysed(boolean isSystemAnalysed) {
		SystemInstanceHandler.isSystemAnalysed = isSystemAnalysed;
	}
	
	
		
}
