package ie.lero.spare.franalyser.utility;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import ie.lero.spare.franalyser.GraphPath;
import ie.lero.spare.franalyser.PredicateHandler;

public class BigrapherHandler {
	
	private static String bigrapherFileName;
	private static String bigrapherValidateCmd = "bigrapher validate -n ";
	private static String bigrapherExecutionOutputFolder;
	private static String bigrapherOutputFormat = "json";
	private static int maximumNumberOfStates = 1000;
	private static String validBigrapherString = "model file parsed correctly";
			

	public static String executeBigraph(String BRSFileName, String outputFolder){
		bigrapherExecutionOutputFolder = outputFolder;
		return executeBigraph(BRSFileName);
	}
	
	
	public static String executeBigraph(String BRSFileName){
		
		bigrapherFileName = BRSFileName;
		
		if(bigrapherExecutionOutputFolder == null) {
			bigrapherExecutionOutputFolder = bigrapherFileName.split("\\.")[0]+"_output";
		}
		
		
			if (validateBigraph()) {
				
				Process proc;
				String cmd = createDefaultBigrapherExecutionCmd();

				Runtime r = Runtime.getRuntime();
				try {
					r.exec("mkdir " + bigrapherExecutionOutputFolder);

					// for future development this could run in own thread for
					// multiprocessing
					proc = r.exec(cmd);

					// check the output of the command, if it has something then there
					// are errors otherwise its ok
					Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\A");
					String result = s.hasNext() ? s.next() : "";

					if (result != null) {
						if (!result.toLowerCase().isEmpty()) {
							System.out.println("Execution could not be completed. Please see possible issues below:");
							System.out.println(result);
						} else {
							System.out.println("Execution is Done");

							// should be a step taken by the main program
							// createDigraph();

						}
					}

					return bigrapherExecutionOutputFolder;
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return null;
		}
	
	private static boolean validateBigraph() {
		boolean isValid = false;
		Process proc;
		Runtime r = Runtime.getRuntime();
		try {

			proc = r.exec(bigrapherValidateCmd + bigrapherFileName);

			Scanner s = new Scanner(proc.getInputStream()).useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";

			if (result != null) {
				if (result.toLowerCase().contains(validBigrapherString)) {
					System.out.println(bigrapherFileName + " is valid");
					isValid = true;
				} else {
					System.out.println(bigrapherFileName + " is not valid. Please see possible issues below:");
					System.out.println(result + "");
					isValid = false;

				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			isValid = false;
		}
		return isValid;
	}

	
	private static String createDefaultBigrapherExecutionCmd() {
		StringBuilder res = new StringBuilder();
		//bigrapherExecutionOutputFolder = bigrapherFileName.split("\\.")[0] + "_Execution_Output";
		res.append("bigrapher full -q -M ").append(maximumNumberOfStates).append(" -t ")
				.append(bigrapherExecutionOutputFolder).append("/transitionSystem -s ")
				.append(bigrapherExecutionOutputFolder).append(" -l ").append(bigrapherExecutionOutputFolder)
				.append("/predicates -p ").append(bigrapherExecutionOutputFolder).append("/transitions -f ")
				.append(bigrapherOutputFormat).append(" ").append(bigrapherFileName);

		return res.toString();
	}

	public static int getMaximumNumberOfStates() {
		return maximumNumberOfStates;
	}

	public static void setMaximumNumberOfStates(int maximumNumberOfStates) {
		BigrapherHandler.maximumNumberOfStates = maximumNumberOfStates;
	}

	public static String getBigrapherOutputFormat() {
		return bigrapherOutputFormat;
	}

	public static void setBigrapherOutputFormat(String bigrapherOutputFormat) {
		BigrapherHandler.bigrapherOutputFormat = bigrapherOutputFormat;
	}


	public static String getBigrapherExecutionOutputFolder() {
		return bigrapherExecutionOutputFolder;
	}


	public static void setBigrapherExecutionOutputFolder(String bigrapherExecutionOutputFolder) {
		BigrapherHandler.bigrapherExecutionOutputFolder = bigrapherExecutionOutputFolder;
	}
	
	


}
