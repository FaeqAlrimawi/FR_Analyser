package Testing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ie.lero.spare.franalyser.utility.Digraph;
import ie.lero.spare.franalyser.utility.JSONTerms;

public class BigrapherStatesDivider {

	private String bigraherType = JSONTerms.TRANSITIONS_BRS;
	private String originalInputFolder;
	private String outputFolderName = "divided_states";

	public void divideStates(String folderName, int divider) {

		if (folderName == null || folderName.isEmpty()) {
			System.out.println("Folder name is not set");
			return;
		}

		if (divider == 0) {
			System.out.println("Divider cannot be 0");
			return;
		}

		originalInputFolder = folderName;

		// load transition file from folder (should be named transitions.json)
		// and convert it to digraph
		String transitionsFile = folderName + "/transitions.json";

		System.out.println("====== Create Transition digraph");
		Digraph<Integer> transitionsDigraph = createDigraphFromJSON(transitionsFile);

		if (transitionsDigraph == null) {
			System.out.println("Something went wrong in Loading states transitions. Execution is terminated");
			return;
		}

		int numberOfStates = transitionsDigraph.getNumberOfNodes();
		System.out.println("Number of states: " + numberOfStates);
		System.out.println("Number of transitions: " + transitionsDigraph.getNumberOfEdges());
		
		// create partitions based on the given divider. limits of transitions
		// between [startIndex and endIndex)
		int numberOfPartitions = numberOfStates / divider;

		if (numberOfPartitions == 0) {
			System.out.println(
					"No partitioning is needed since the given divider is greater than the number of original states");
			return;
		}
		
		System.out.println("Partition size = " + divider);
		System.out.println("Number of partitions = " + numberOfPartitions);
		
		String outputFolder = folderName + "/" + outputFolderName;

		File outputFolderFile = new File(outputFolder);

		if (!outputFolderFile.exists()) {
			outputFolderFile.mkdir();
		}

		List<String> outputFolders = new LinkedList<String>();

		// create a list of digraphs for each parition
		List<Digraph<Integer>> digraphers = new LinkedList<Digraph<Integer>>();
		String outFolder = null;

		// create output folders for partitions. last
		// partition is not needed as it is the original
		for (int i = 0; i < numberOfPartitions; i++) {

			if (i < numberOfPartitions - 1) {
				outFolder = outputFolder + "/states_" + divider * (i + 1);
			} else {
				outFolder = outputFolder + "/states_" + numberOfStates;
			}

			File outputFile = new File(outFolder);

			if (!outputFile.exists()) {
				outputFile.mkdir();
			}

			outputFolders.add(outFolder);
			digraphers.add(new Digraph<Integer>());
		}

		int statePartition = 0;
		int biggerState = 0;
		double probability = -1;
		String label = "";

		System.out.println("\n===== Create Transition digraphs for each partition");
		
		for (Integer srcState : transitionsDigraph.getNodes()) {

			// for all destination states for the source state
			List<Integer> neighbors = transitionsDigraph.outboundNeighbors(srcState);

			// the the source node has no neighbors then determine state
			// partition based on the source only
			if (neighbors == null || neighbors.isEmpty()) {

				biggerState = srcState;
				statePartition = biggerState / divider;

				for (; statePartition <= digraphers.size(); statePartition++) {

					if (statePartition < digraphers.size()) {
						for (; statePartition < digraphers.size(); statePartition++) {
							digraphers.get(statePartition).add(srcState);
						}
					} else {
						digraphers.get(digraphers.size() - 1).add(srcState);
					}
				}

			} else {
				for (Integer desState : neighbors) {

					label = transitionsDigraph.getLabel(srcState, desState);
					probability = transitionsDigraph.getProbability(srcState, desState);

					biggerState = srcState >= desState ? srcState : desState;

					// determin start state partition. Equals
					// stateNumber/divider
					statePartition = biggerState / divider;

					// add the new state transition to all digraphs that have
					// index greater or equal to the state partition

					if (statePartition < digraphers.size()) {
						for (; statePartition < digraphers.size(); statePartition++) {
							digraphers.get(statePartition).add(srcState, desState, probability, label);
						}
					} else {
						digraphers.get(digraphers.size() - 1).add(srcState, desState, probability, label);
					}
				}
			}
		}

		System.out.println("Done");
		
		System.out.println("\n===== Store new partitions");
		// for each generated digraph create a new transition file and copy
		// states to their output folder
		for (int i = 0; i < digraphers.size()-1; i++) {
			System.out.print("Partition[" + i+"]: ");
			storeDigraphAndStates(digraphers.get(i), outputFolders.get(i));
		}

	}

	public void storeDigraphAndStates(Digraph<Integer> digraph, String outputFolder) {

		StringBuilder res = new StringBuilder();

		double prob = -1;
		String label = null;
		Path srcFile;
		Path targetFile;

		res.append("{\"").append(bigraherType).append("\":[");

		for (Integer state : digraph.getNodes()) {
			for (Integer stateDes : digraph.outboundNeighbors(state)) {
				prob = digraph.getProbability(state, stateDes);
				label = digraph.getLabel(state, stateDes);
				if (prob != -1) {
					if (label != null && !label.isEmpty()) {
						res.append("{\"source\":").append(state).append(",\"target\":").append(stateDes)
								.append(",\"prob\":").append(prob).append(", \"action\":").append(label).append("},");
					} else {
						res.append("{\"source\":").append(state).append(",\"target\":").append(stateDes)
								.append(",\"prob\":").append(prob).append("},");
					}
				} else {
					if (label != null && !label.isEmpty()) {
						res.append("{\"source\":").append(state).append(",\"target\":").append(stateDes)
								.append(", \"action\":").append(label).append("},");
					} else {
						res.append("{\"source\":").append(state).append(",\"target\":").append(stateDes).append("},");
					}
				}
			}

			// save state to output file
			srcFile = Paths.get(originalInputFolder + "/" + state + ".json");
			targetFile = Paths.get(outputFolder + "/" + state + ".json");

			try {
				Files.copy(srcFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// remove last comma
		res.deleteCharAt(res.length() - 1);

		// close the json object
		res.append("]}");

		org.json.JSONObject objLabelled = new org.json.JSONObject(res.toString());

		File file = new File(outputFolder + "/transitions.json");

		try (final BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
			writer.write(objLabelled.toString(4));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		System.out.println("[" + digraph.getNumberOfNodes() + "] states and their transitions ["
				+ digraph.getNumberOfEdges() + "] are stored in " + outputFolder);

	}

	private Digraph<Integer> createDigraphFromJSON(String fileName) {

		Integer st1;
		Integer st2;
		double probability = -1;
		String label = null;
		Digraph<Integer> transitionGraph = new Digraph<Integer>();

		// transitionsFileLines = FileManipulator.readFileNewLine(fileName);

		JSONParser parser = new JSONParser();
		JSONObject obj;

		try {
			JSONArray ary;

			obj = (JSONObject) parser.parse(new FileReader(fileName));

			// if the transitions come from a brs file
			ary = (JSONArray) obj.get(JSONTerms.TRANSITIONS_BRS);

			// if the transitions come from pbrs
			if (ary == null) {
				ary = (JSONArray) obj.get(JSONTerms.TRANSITIONS__PROP_BRS);
				bigraherType = JSONTerms.TRANSITIONS__PROP_BRS;
			}

			if (ary == null) {
				ary = (JSONArray) obj.get(JSONTerms.TRANSITIONS__STOCHASTIC_BRS);
				bigraherType = JSONTerms.TRANSITIONS__STOCHASTIC_BRS;
			}

			// numberOfStates = new Integer(transitionsFileLines[0].split("
			// ")[0]);
			// //gets the number of states

			Iterator<JSONObject> iter = ary.iterator();
			JSONObject tmpObj = null;
			Object objGeneral = null;

			while (iter.hasNext()) {

				tmpObj = iter.next();

				// source state
				String srcState = tmpObj.get(JSONTerms.TRANSITIONS__SOURCE).toString();
				st1 = srcState != null ? Integer.valueOf(srcState) : -1;

				// destination state
				String desState = tmpObj.get(JSONTerms.TRANSITIONS__TARGET).toString();
				st2 = desState != null ? Integer.valueOf(desState) : -1;

				// probability. If there's no probability then its set to -1
				objGeneral = tmpObj.get(JSONTerms.TRANSITIONS__PROBABILITY);
				probability = objGeneral != null ? Double.parseDouble(objGeneral.toString()) : -1;

				// label for action
				objGeneral = tmpObj.get(JSONTerms.TRANSITIONS__LABEL);
				label = objGeneral != null ? objGeneral.toString() : "";

				// if one of the states is not set to a proper state ( between 0
				// & Max-States-1)
				if (st1 == -1 || st2 == -1) {
					continue;
				}

				transitionGraph.add(st1, st2, probability, label);
			}

			// numberOfStates = transitionGraph.getNumberOfNodes();

		} catch (Exception ie) {
			ie.printStackTrace();
			return null;
		}

		return transitionGraph;
	}

	public static void main(String[] args) {

		BigrapherStatesDivider divider = new BigrapherStatesDivider();

		String folderName = "D:/Bigrapher data/lero/lero100K/states/divided_states/states_100003";
		int dividNumber = 10000;

		divider.divideStates(folderName, dividNumber);

		System.out.println("\n======== Complete ========");
	}

}
