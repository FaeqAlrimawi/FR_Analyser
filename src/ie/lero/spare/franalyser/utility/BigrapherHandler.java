package ie.lero.spare.franalyser.utility;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import ie.lero.spare.franalyser.GraphPath;
import ie.lero.spare.franalyser.PredicateHandler;

public class BigrapherHandler {
	
	private static String bigrapherFileName;
	private static String bigrapherValidateCmd;
	private static String bigrapherExecutionOutputFolder;
	private static String bigrapherOutputFormat = "json";
	private static int maximumNumberOfStates = 1000;
	private static String validBigrapherString = "model file parsed correctly";
	
	public BigrapherHandler() {
	
	}		

	public static String executeBigraph(String BRSFileName){
		boolean isExecuted = false;
		
			if (validateBigraph()) {
				bigrapherFileName = BRSFileName;
				bigrapherExecutionOutputFolder = BRSFileName.split("\\.")[0]+"_output";
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
							isExecuted = false;
						} else {
							System.out.println("Execution is Done");
							isExecuted = true;

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
				.append(bigrapherOutputFormat).append(" ").append(bigrapherFileName); //bigrapher file name should be changed to the generated one

		return res.toString();
	}
	


}
