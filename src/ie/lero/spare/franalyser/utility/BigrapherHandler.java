package ie.lero.spare.franalyser.utility;

import java.io.IOException;
import java.util.Scanner;

import ie.lero.spare.franalyser.SystemExecutor;

public class BigrapherHandler implements SystemExecutor {

	private String bigrapherFileName;
	private String bigrapherValidateCmd = "bigrapher validate -n ";
	private String bigrapherExecutionOutputFolder;
	private String bigrapherOutputFormat = "json";
	private int maximumNumberOfStates = 1000;
	private String validBigrapherString = "model file parsed correctly";

	public String executeBigraph(String BRSFileName, String outputFolder) {
		bigrapherExecutionOutputFolder = outputFolder;
		return execute(BRSFileName);
	}

	public String execute(String BRSFileName) {
		
		bigrapherFileName = BRSFileName;
		bigrapherExecutionOutputFolder = bigrapherFileName.split("\\.")[0] + "_output";

		if (validateBigraph()) {

			Process proc;
			String cmd = createDefaultBigrapherExecutionCmd();

			Runtime r = Runtime.getRuntime();
			try {
				r.exec("mkdir " + bigrapherExecutionOutputFolder);

				// for future development this could run in own thread for
				// multiprocessing
				proc = r.exec(cmd);

				// check the output of the command, if it has something then
				// there
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

	private boolean validateBigraph() {
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

	private String createDefaultBigrapherExecutionCmd() {
		StringBuilder res = new StringBuilder();
		res.append("bigrapher full -q -M ").append(maximumNumberOfStates).append(" -t ")
				.append(bigrapherExecutionOutputFolder).append("/transitionSystem -s ")
				.append(bigrapherExecutionOutputFolder).append(" -l ").append(bigrapherExecutionOutputFolder)
				.append("/predicates -p ").append(bigrapherExecutionOutputFolder).append("/transitions -f ")
				.append(bigrapherOutputFormat).append(" ").append(bigrapherFileName);

		return res.toString();
	}

	public int getMaximumNumberOfStates() {
		return maximumNumberOfStates;
	}

	public void setMaximumNumberOfStates(int maxStates) {
		maximumNumberOfStates = maxStates;
	}

	public String getBigrapherOutputFormat() {
		return bigrapherOutputFormat;
	}

	public void setBigrapherOutputFormat(String format) {
		bigrapherOutputFormat = format;
	}

	public String getBigrapherExecutionOutputFolder() {
		return bigrapherExecutionOutputFolder;
	}

	public void setBigrapherExecutionOutputFolder(String outputFolder) {
		bigrapherExecutionOutputFolder = outputFolder;
	}

}
