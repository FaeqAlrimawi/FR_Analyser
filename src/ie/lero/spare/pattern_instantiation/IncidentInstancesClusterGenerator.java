package ie.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceCorrelation;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceCosine;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceEuclidian;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoBisectingKMeans;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.patterns.cluster.ClusterWithMean;
import ca.pfv.spmf.patterns.cluster.DoubleArray;
import ie.lero.spare.franalyser.utility.FileManipulator;

public class IncidentInstancesClusterGenerator {

	List<GraphPath> instances;
	String instanceFileName;
	String convertedInstancesFileName;
	int numberOFClusters =10;
	DistanceFunction distanceFunction;
	AlgoKMeans kmean;

	public final static String DATA_SEPARATOR = " "; // space is the separator
	public final static int PADDING_STATE = -10000; // cloud be the number of
													// states

	String clusterFolder = "clusters generated";
	String clustersOutputFileName = "clustersGenerated.txt";
	String convertedInstancesFile = "convertedInstances.txt";
	String clustersOutputFolder;
	String attributeState = "state-";

	int longestTransition = -1;
	int shortestTransition = -1;
	
	// holds clusters generated
	List<ClusterWithMean> clusters;

	void generateClusters() {

		System.out.println("reading instances from: " + instanceFileName);

		instances = FileManipulator.readInstantiatorGeneratedIncidentInstancesFile(instanceFileName);

		if (instances == null) {
			System.out.println("Instances are null! Exiting");
			return;
		}

		// for (GraphPath p : instances) {
		// System.out.println(p.getInstanceID());
		// System.out.println(p.getStateTransitions() + "\n");
		// }

		System.out.println(">>Converting instances to miner format...");
		convertedInstancesFileName = convertInstancesToMinerFormat();

		distanceFunction = new DistanceEuclidian();

		// apply cluster algorithm (K-mean)
		generateClustersUsingKMean();
		// printClusters();
		kmean.printStatistics();

		clustersOutputFileName = clustersOutputFolder + "/" + clustersOutputFileName;
		try {
			kmean.saveToFile(clustersOutputFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// apply cluster algorithm (BiSect implementation)
		// generateClustersUsingKMeanUsingBiSect();
		 printClusters();

		System.out.println("\n>>DONE");

	}

	void generateClusters(String fileName) {

		instanceFileName = fileName;

		clustersOutputFolder = instanceFileName.substring(0, instanceFileName.lastIndexOf("/")) + "/" + clusterFolder;

		File outputFolder = new File(clustersOutputFolder);

		if (!outputFolder.exists()) {
			outputFolder.mkdir();

		}

		generateClusters();

	}

	public void generateClustersUsingKMean() {

		kmean = new AlgoKMeans();

		try {
			System.out.println(">>Generating clusters using K-mean algorithm" + " with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					DATA_SEPARATOR);

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void generateClustersUsingKMeanUsingBiSect() {

		kmean = new AlgoBisectingKMeans();

		try {
			System.out.println(">>Generating clusters using K-mean algorithm with K = " + numberOFClusters
					+ ", distance function is " + distanceFunction.getName());
			clusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters, distanceFunction,
					DATA_SEPARATOR);

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void printClusters() {

		int id = 0;
		
		//used to print a few sets
		int length = 5;
		
		for (ClusterWithMean cluster : clusters) {
			System.out.println("Cluster " + id++);
			// For each data point: [first entry is the instance name]
			List<DoubleArray> dataPoints = cluster.getVectors();
			System.out.println("  number of instances = " + dataPoints.size());
			
			for (int i=0;i<length && i<dataPoints.size();i++) {
				System.out.println("   " + dataPoints.get(i));
			}
		}

	}

	public String convertInstancesToMinerFormat() {

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

		String outputFileName = clustersOutputFolder + "/" + convertedInstancesFile;

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();
	
		if(instances!= null && !instances.isEmpty()) {
			shortestTransition = instances.get(0).getStateTransitions().size();
		}
		
		//find longest and shortest transitions
		for (GraphPath path : instances) {
			List<Integer> tmp = path.getStateTransitions();

			if (tmp.size() > longestTransition) {
				longestTransition = tmp.size();
			} else if (tmp.size() < shortestTransition) {
				shortestTransition = tmp.size();
			}
		}

		numberOFClusters = longestTransition - shortestTransition +1;
		
		//set attribute names
		for (int i = 0; i < longestTransition; i++) {

			// add attribute name e.g., "state-0 state-1 ..."
			builder.append(attributeName).append(attributeState).append(i).append(fileLinSeparator);
		}
		
		//set data
		for (GraphPath path : instances) {

			// set instance name to be the instance id
			builder.append(instanceName).append(path.getInstanceID()).append(fileLinSeparator);

			// set data to be the state transitions
			List<Integer> states = path.getStateTransitions();


			for (int i = 0; i < states.size() - 1; i++) {
				// add state
				builder.append(states.get(i)).append(DATA_SEPARATOR);
			}
			// add last state
			builder.append(states.get(states.size() - 1));

			// pad the transition with -1
			if (states.size() < longestTransition) {

				builder.append(DATA_SEPARATOR);

				for (int i = 0; i < longestTransition - states.size() - 1; i++) {

					builder.append(PADDING_STATE).append(DATA_SEPARATOR);
				}

				builder.append(PADDING_STATE);
			}

			builder.append(fileLinSeparator);
		}

		// save string to file

		try {
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFileName), "utf-8"));

			writer.write(builder.toString());

			writer.close();

			return outputFileName;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) {

		IncidentInstancesClusterGenerator tester = new IncidentInstancesClusterGenerator();

		String fileName = "D:/Bigrapher data/lero/output/2_10000.json";

		tester.generateClusters(fileName);
	}

}
