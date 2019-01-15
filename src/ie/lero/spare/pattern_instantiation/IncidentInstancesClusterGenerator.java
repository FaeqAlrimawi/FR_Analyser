package ie.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ca.pfv.spmf.algorithms.clustering.dbscan.AlgoDBSCAN;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceCorrelation;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceCosine;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceEuclidian;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceJaccard;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoBisectingKMeans;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.algorithms.clustering.optics.AlgoOPTICS;
import ca.pfv.spmf.algorithms.clustering.optics.DoubleArrayOPTICS;
import ca.pfv.spmf.algorithms.clustering.text_clusterer.TextClusterAlgo;
import ca.pfv.spmf.patterns.cluster.Cluster;
import ca.pfv.spmf.patterns.cluster.ClusterWithMean;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ie.lero.spare.franalyser.utility.FileManipulator;

public class IncidentInstancesClusterGenerator {

	List<GraphPath> instances;
	String instanceFileName;
	String convertedInstancesFileName = "convertedInstances.txt";
	int numberOFClusters = 10;
	DistanceFunction distanceFunction;
	// AlgoKMeans kmean;

	// space is the separator
	public final static String DATA_SEPARATOR = " ";
	// cloud be the number of states
	public final static int PADDING_STATE = -1;
	// what value to give? probably larger would be better to get a noticable
	// difference
	public final static int ACTION_PERFORMED = 1;
	public final static int ACTION_NOT_PERFORMED = 0;

	String clusterFolder = "clusters generated";
	String clustersOutputFileName = "clustersGenerated.txt";
	String clustersOutputFolder;
	String attributeState = "state-";

	int longestTransition = -1;
	int shortestTransition = -1;

	List<String> systemActions;

	// prints a number of instances for each cluster
	int lengthToPrint = 5;

	public IncidentInstancesClusterGenerator() {

		systemActions = new LinkedList<String>();

		// some actions
		systemActions.add("EnterRoom");
		systemActions.add("ConnectIPDevice");
		systemActions.add("DisconnectIPDevice");
		systemActions.add("ConnectBusDevice");
		systemActions.add("DisconnectBusDevice");
		systemActions.add("SendData");
		systemActions.add("SendMalware");
		systemActions.add("DisableHVAC");
		systemActions.add("EnterRoomWithoutCardReader");
		systemActions.add("ChangeAccessToCardRequired");
		systemActions.add("ChangeAccessToCardNotRequired");
		systemActions.add("ChangeContextToOutSideWorkingHours");
		systemActions.add("ChangeContextToWorkingHours");
		systemActions.add("TurnOnHVAC");
		systemActions.add("TurnOffHVAC");
		systemActions.add("TurnOnSmartTV");
		systemActions.add("TurnOffSmartTV");
		systemActions.add("GenerateData");
		systemActions.add("CollectData");

	}

	// holds clusters generated
	List<ClusterWithMean> clusters;

	void generateClusters() {

		System.out.println("reading instances from: " + instanceFileName);

		// load instances from file
		instances = FileManipulator.readInstantiatorInstancesFile(instanceFileName);

		System.out.println(instances.size());
		// System.out.println(instances.get(0).getTransitionActions());
		if (instances == null) {
			System.out.println("Instances are null! Exiting");
			return;
		}

		System.out.println(">>Converting instances to data mining tech format...");
		convertedInstancesFileName = convertInstancesToMiningFormat();

		// jaccard distance function is used for vectors that has only 0,1
		// values
		distanceFunction = new DistanceCosine();

		// apply cluster algorithm (K-mean)
		// clusters = generateClustersUsingKMean();
		// printClusters(clusters);

		// apply cluster algorithm (BiSect implementation)
		// generateClustersUsingKMeanUsingBiSect();
		// printClusters();

		// using OPTIC algorithm to find clusters
		// List<Cluster> clus = generateClustersUsingOPTICS();
		// printClustersOPTIC(clus);

		// using DBSCAN algorithm
		// List<Cluster> clus = generateClustersUsingDBSCAN();
		// printClustersOPTIC(clus);

		// ======text based clustering
//		convertedInstancesFileName = convertInstancesToTextMiningFormat();
		
//		generateClustersUsingTextMining();

		System.out.println("\n>>DONE");

	}

	void generateClusters(String fileName) {

		instanceFileName = fileName;

		clustersOutputFolder = instanceFileName.substring(0, instanceFileName.lastIndexOf("/")) + "/" + clusterFolder;

		File outputFolder = new File(clustersOutputFolder);

		if (!outputFolder.exists()) {
			outputFolder.mkdir();

		}

		clustersOutputFileName = clustersOutputFolder + "/" + clustersOutputFileName;
		convertedInstancesFileName = clustersOutputFolder + "/" + convertedInstancesFileName;

		generateClusters();

	}

	public List<ClusterWithMean> generateClustersUsingKMean() {

		AlgoKMeans kmean = new AlgoKMeans();

		try {
			System.out.println(">>Generating clusters using K-mean algorithm" + " with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());

			// generate clusters
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					DATA_SEPARATOR);

			// store clusters (each line is a cluster in the output file)
			kmean.saveToFile(clustersOutputFileName);

			kmean.printStatistics();

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<ClusterWithMean> generateClustersUsingKMeanUsingBiSect() {

		AlgoBisectingKMeans kmean = new AlgoBisectingKMeans();

		int iteratorForSplit = numberOFClusters * 2;

		try {
			System.out.println(">>Generating clusters using K-mean algorithm with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					iteratorForSplit, DATA_SEPARATOR);

			kmean.saveToFile(clustersOutputFileName);

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<Cluster> generateClustersUsingDBSCAN() {

		AlgoDBSCAN algo = new AlgoDBSCAN();

		// minimum number of points/instances in a cluster
		int minPoints = 10;
		// distance between points/instances in a cluster
		double epsilon = 10d;
		// double epsilonPrime = epsilon;

		try {
			System.out.println(">>Generating clusters using DBSCAN algorithm");

			// generate clusters
			List<Cluster> clusters = algo.runAlgorithm(convertedInstancesFileName, minPoints, epsilon, DATA_SEPARATOR);

			// store clusters (each line is a cluster in the output file)
			algo.saveToFile(clustersOutputFileName);

			algo.printStatistics();

			return clusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public List<Cluster> generateClustersUsingOPTICS() {

		AlgoOPTICS algo = new AlgoOPTICS();

		// minimum number of points/instances in a cluster
		int minPoints = 10;
		// distance between points/instances in a cluster
		double epsilon = 2d;
		double epsilonPrime = epsilon;

		try {
			System.out.println(">>Generating clusters using OPTIC algorithm");

			// generate clusters
			List<DoubleArrayOPTICS> clusters = algo.computerClusterOrdering(convertedInstancesFileName, minPoints,
					epsilon, DATA_SEPARATOR);

			// generate dbscan clusters from the cluster ordering:
			List<Cluster> dbScanClusters = algo.extractDBScan(minPoints, epsilonPrime);

			// store clusters (each line is a cluster in the output file)
			algo.saveToFile(clustersOutputFileName);

			algo.printStatistics();

			return dbScanClusters;
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void generateClustersUsingTextMining() {

		TextClusterAlgo algo = new TextClusterAlgo();

		boolean stemFlag = false;
		boolean stopWordFlag = false;

		System.out.println(">>Generating clusters using Text Clustering algorithm");

		// generate clusters
		algo.runAlgorithm(convertedInstancesFileName, clustersOutputFileName, stemFlag, stopWordFlag);

		algo.printStatistics();

	}

	public String convertInstancesToMiningFormat() {

		// convert instances to a format compatible with that of the data mining
		// library used (i.e. SPMF)
		// line format could have:
		// @NAME="instance name"
		String instanceName = "@NAME=";
		// @ATTRIBUTEDEF="attribute name"
		String attributeName = "@ATTRIBUTEDEF=";
		// #, % are used for comments and any meta-data respectively
		// data1 [separator] data2 [separator] data3 ... (actual data treated as
		// double array)

		/**
		 * all data array should be of the same length so states that are short
		 * than the longest are padded with -1
		 **/
		// create a text file to hold the data

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		if (instances != null && !instances.isEmpty()) {
			shortestTransition = instances.get(0).getStateTransitions().size();
		}

		// find longest and shortest transitions
		for (GraphPath path : instances) {
			List<Integer> tmp = path.getStateTransitions();

			if (tmp.size() > longestTransition) {
				longestTransition = tmp.size();
			} else if (tmp.size() < shortestTransition) {
				shortestTransition = tmp.size();
			}
		}

		numberOFClusters = longestTransition - shortestTransition + 1;

		// ========states attributes (state-0, state-1, number of maximum
		// states)
		for (int i = 0; i < longestTransition; i++) {

			// add attribute name e.g., "state-0 state-1 ..."
			builder.append(attributeName).append(attributeState).append(i).append(fileLinSeparator);
		}

		// ========actions attribute (actions names)
		for (String action : systemActions) {
			builder.append(attributeName).append(action).append(fileLinSeparator);
		}

		// ========set data
		for (GraphPath path : instances) {

			// set instance name to be the instance id
			builder.append(instanceName).append(path.getInstanceID()).append(fileLinSeparator);

			// set data to be the state transitions
			List<Integer> states = path.getStateTransitions();

			int i = 0;
			for (i = 0; i < states.size() - 1; i++) {
				// add state
				builder.append(states.get(i)).append(DATA_SEPARATOR);
			}
			// add last state
			builder.append(states.get(states.size() - 1));

			// pad the transition with -1
			if (states.size() < longestTransition) {

				builder.append(DATA_SEPARATOR);

				for (i = 0; i < longestTransition - states.size() - 1; i++) {

					builder.append(PADDING_STATE).append(DATA_SEPARATOR);
				}

				builder.append(PADDING_STATE);
			}

			// add action data
			// 0 for missing the action from the transition actions. 1 if it
			// exists
			List<String> transitionActions = path.getTransitionActions();

			if (transitionActions != null && !transitionActions.isEmpty()) {

				builder.append(DATA_SEPARATOR);

				for (i = 0; i < systemActions.size() - 1; i++) {

					if (transitionActions.contains(systemActions.get(i))) {
						builder.append(ACTION_PERFORMED).append(DATA_SEPARATOR);
					} else {
						builder.append(ACTION_NOT_PERFORMED).append(DATA_SEPARATOR);
					}
				}

				// check last action
				if (transitionActions.contains(systemActions.get(i))) {
					builder.append(ACTION_PERFORMED);
				} else {
					builder.append(ACTION_NOT_PERFORMED);
				}
			}

			builder.append(fileLinSeparator);
		}

		// save string to file

		try {
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(convertedInstancesFileName), "utf-8"));

			writer.write(builder.toString());

			writer.close();

			return convertedInstancesFileName;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public String convertInstancesToTextMiningFormat() {

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		// ========set data
		for (GraphPath path : instances) {

			//=== get states as string
//			String statesStr = path.getStateTransitions().toString();
//			// remove brackets
//			statesStr = statesStr.replaceAll("\\[", "");
//			statesStr = statesStr.replaceAll("\\]", "");
//			// remove commas
//			statesStr = statesStr.replaceAll(",", "");
//			statesStr = statesStr.trim();

			//=== get actions as string
			String actionsStr = path.getTransitionActions().toString();
			actionsStr = actionsStr.replaceAll("\\[", "");
			actionsStr = actionsStr.replaceAll("\\]", "");
			actionsStr = actionsStr.replaceAll(",", "");
			actionsStr = actionsStr.trim();

			//=== set record(instance_id [states (1 2 3) actions (enterRoom)]
			builder.append(path.getInstanceID()).append("\t")
//			.append(statesStr)
//			.append(" ")
			.append(actionsStr)
					.append(fileLinSeparator);

		}
		try {
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(convertedInstancesFileName), "utf-8"));

			writer.write(builder.toString());

			writer.close();

			return convertedInstancesFileName;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/******* printers *********/

	public void printClusters(List<Cluster> clusters) {

		int id = 0;

		// used to print a few sets
		// int length = 5;
		Random rand = new Random();

		for (Cluster cluster : clusters) {
			System.out.println("Cluster " + id++);
			// For each data point: [first entry is the instance name]
			List<DoubleArray> dataPoints = cluster.getVectors();
			System.out.println("  number of instances = " + dataPoints.size());

			for (int i = 0; i < lengthToPrint && i < dataPoints.size(); i++) {
				System.out.println("   " + dataPoints.get(rand.nextInt(dataPoints.size())));
			}

			if (dataPoints.size() > lengthToPrint) {
				System.out.println("   ...");
			}
		}

	}

	public void printClustersOPTIC(List<Cluster> clusters) {

		int id = 0;

		// used to print a few sets
		// int length = 5;
		Random rand = new Random();

		for (Cluster cluster : clusters) {
			System.out.println("Cluster " + id++);
			// For each data point: [first entry is the instance name]
			List<DoubleArray> dataPoints = cluster.getVectors();
			System.out.println("  number of instances = " + dataPoints.size());

			for (int i = 0; i < lengthToPrint && i < dataPoints.size(); i++) {
				System.out.println("   " + dataPoints.get(rand.nextInt(dataPoints.size())));
			}

			if (dataPoints.size() > lengthToPrint) {
				System.out.println("   ...");
			}
		}

	}

	public static void main(String[] args) {

		IncidentInstancesClusterGenerator tester = new IncidentInstancesClusterGenerator();

		String fileName = "D:/Bigrapher data/lero/output/2_10000.json";

		tester.generateClusters(fileName);
	}

}
