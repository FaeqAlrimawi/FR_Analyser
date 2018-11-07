package ie.lero.spare.franalyser.utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class LogFileAnalyser {

	protected void extractActivityTiming(String logFileName) {

		String[] fileNames = null;
//		String analysisOutputFolder = "";
		String outputFolder = "";
//		String analysisOutputFile = ".";

		File file = new File(logFileName);

		boolean isDirectory = false;

		if (file.isDirectory()) {
			fileNames = file.list();
			isDirectory = true;
		} else if (file.isFile()) {
			fileNames = new String[1];
			fileNames[0] = file.getName();
		} else {
			return;
		}

		if (isDirectory) {
			outputFolder = logFileName;
		} else {
			outputFolder = logFileName.substring(0, logFileName.lastIndexOf("/"));
		}

//		analysisOutputFolder = outputFolder + "/analysis";
//		// output folder
//		File analysisOutput = new File(analysisOutputFolder);

		String statesString = "Number of States=";
		String act1_preString = "BigraphAnalyser>>Condition [activity1_Precondition] matching time:";
		String act2_preString = "BigraphAnalyser>>Condition [activity2_Precondition] matching time:";
		String act3_preString = "BigraphAnalyser>>Condition [activity3_Precondition] matching time:";

		String act1_postString = "BigraphAnalyser>>Condition [activity1_Postcondition] matching time:";
		String act2_postString = "BigraphAnalyser>>Condition [activity2_Postcondition] matching time:";
		String act3_postString = "BigraphAnalyser>>Condition [activity3_Postcondition] matching time:";

		// String act1_preString = "BigraphAnalyser>>Condition
		// [activity1_Precondition] matching time:";

		List<Long> preConditionTiming = new LinkedList<Long>();
		List<Long> postConditionTiming = new LinkedList<Long>();
		List<Long> actTiming = new LinkedList<Long>();
		// List<Integer> act1Timing = new LinkedList<Integer>();
		String states = "1";

		
		StringBuilder str = new StringBuilder();
		str.append(outputFolder).append("\n\n");
		str.append("states\nactivity1-timing; activity2-timing; activity3-timing\n\n");
		
		final int ACTIVITY_NUMBER = 3;
		final int INSTANCES_NUMBER = 5;

		for (String fileName : fileNames) {

			preConditionTiming.clear();
			postConditionTiming.clear();
			actTiming.clear();
			
			System.out.println("analysing "+fileName);
			
			//read only text files
			if(!fileName.startsWith("log") || !fileName.endsWith(".txt")) {
				System.out.println(fileName + " NOT a file");
				continue;
			}
			
		
			fileName = outputFolder + "/" + fileName;
			
			String[] lines = FileManipulator.readFileNewLine(fileName);
			long preTiming = 0;
			long postTiming = 0;

			for (String line : lines) {

				// get states numbers
				if (line.contains(statesString)) {
					states = line.split("=")[1].trim();
//					analysisOutputFile = analysisOutputFolder + "/" + states + ".txt";
					// System.out.println(outputFile);
				}

				// get precondition activity
				if (line.contains(act1_preString) || line.contains(act2_preString) || line.contains(act3_preString)) {
					// System.out.println(line);
					String tmp = line.split(":")[5].trim().split(" ")[0].replace("ms", "");
					preTiming = Long.parseLong(tmp);
					preConditionTiming.add(preTiming);
				}

				// get postcondition activity
				if (line.contains(act1_postString) || line.contains(act2_postString)
						|| line.contains(act3_postString)) {
					String tmp = line.split(":")[5].trim().split(" ")[0].replace("ms", "");
					postTiming = Long.parseLong(tmp);
					postConditionTiming.add(postTiming);
				}

				// set activity time after getting pre and post timings.
				// Activity time is the condition with higher timing
				if (preTiming != 0 && postTiming != 0) {
					if (preTiming > postTiming) {
						// str.append("act-").append(actNum).append(",").append(preTiming).append("\n");
						actTiming.add(preTiming);
					} else {
						// str.append("act-").append(actNum).append(",").append(postTiming).append("\n");
						actTiming.add(postTiming);
					}

					preTiming = 0;
					postTiming = 0;
				}

			}

			str.append(states).append("\n");
			for (int i = 0; i < actTiming.size(); i = i + ACTIVITY_NUMBER) {
				for (int j = 0; j < ACTIVITY_NUMBER; j++) {
					str.append(actTiming.get(i + j)).append(";");
				}
				str.deleteCharAt(str.lastIndexOf(";"));
				str.append("\n");
			}

			str.append("\n\n");
		}
		
		try {

			FileWriter analysisFile = new FileWriter(outputFolder+"/activity-Time-analysis.txt");

			// //create analysis output folder
//			if (!analysisOutput.exists()) {
//				analysisOutput.mkdir();
//			}

			BufferedWriter writer = new BufferedWriter(analysisFile);

			writer.write(str.toString());

			writer.close();
			
			System.out.println("=========analysis complete=========");
			System.out.println(str.toString());
			System.out.println("\noutput file save to: " + outputFolder+"/activity-Time-analysis.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {

		LogFileAnalyser analyser = new LogFileAnalyser();

		String outputFolderVM16 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-16/log";
		String outputFolderVM8 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-8/log";
		String outputFolderVM4 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-4/log";
		String outputFolderVM2 = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/CPU-2/log";
		String outputFolderVMNoThreads = "D:/Bigrapher data/lero/instantiation data/VM ubuntu data/No threads/log";
		
		String outputFolderUbuntu8 = "D:/Bigrapher data/lero/instantiation data/ubuntu data/CPU-8/log";
		String outputFolderUbuntu4 = "D:/Bigrapher data/lero/instantiation data/ubuntu data/CPU-4/log";
		String outputFolderUbuntu2 = "D:/Bigrapher data/lero/instantiation data/ubuntu data/CPU-2/log";
		String outputFolderUbuntuNoThreads = "D:/Bigrapher data/lero/instantiation data/ubuntu data/No threads/log";

		analyser.extractActivityTiming(outputFolderUbuntuNoThreads);
	}
}
