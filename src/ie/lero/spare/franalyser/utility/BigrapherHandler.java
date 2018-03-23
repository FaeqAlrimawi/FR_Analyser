package ie.lero.spare.franalyser.utility;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ie.lero.spare.franalyser.JSONTerms;
import ie.lero.spare.franalyser.SystemExecutor;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;

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

	/**
	 * creates a signature from the Bigrapher file provided (i.e. fileName set
	 * by method setFileName)
	 * 
	 * @return The signature of the Bigrapher as a Signature object from the
	 *         LibBig library
	 */
	public  Signature createSignatureFromBRS(String fileName) {
		SignatureBuilder sigBuilder = new SignatureBuilder();

		String[] lines = FileManipulator.readFileNewLine(fileName);
		String tmp;

		for (int i = 0; i < lines.length; i++) {
			// if there are functions in the control then creating the signature
			// should be done in alternative
			// way i.e. by looking into all states and extracting the controls
			if ((lines[i].startsWith("fun") && lines[i].contains(" ctrl "))
					|| (lines[i].startsWith("atomic") && lines[i].contains(" fun "))) {
				return null;
			}
		}
		// determine the last time the keyword ctrl is used as predicates
		for (int i = 0; i < lines.length; i++) {
			tmp = lines[i];
			if (tmp.startsWith("ctrl") || (tmp.startsWith("atomic") || tmp.contains(" ctrl "))) {
				if (!tmp.contains(";")) {
					for (int j = i + 1; j < lines.length; j++) {
						tmp += lines[j];
						if (lines[j].contains(";")) {
							i = j;
							break;
						}
					}
				}
				// remove comments
				if (tmp.contains("#")) {
					tmp = tmp.split("#")[0];

				}
				// remove semicolon
				tmp = tmp.replace(";", "");
				tmp = tmp.trim();
				String[] tmp2 = tmp.split("=");

				// get control arity
				String controlArity = tmp2[1].trim();

				// get control name
				String[] tmp3 = tmp2[0].split(" ");
				String controlName = tmp3[tmp3.length - 1];

				// if control holds brackets i.e. () then create a global
				// signature from all other states
				if (controlName.contains("(")) {
					controlName = controlName.substring(0, controlName.indexOf("("));
				}
				controlName.trim();

				// create signature
				sigBuilder.add(controlName, true, Integer.parseInt(controlArity));

			}
		}

		return sigBuilder.makeSignature();

	}



}
