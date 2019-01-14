package ie.lero.spare.pattern_instantiation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceEuclidian;
import ca.pfv.spmf.algorithms.clustering.distanceFunctions.DistanceFunction;
import ca.pfv.spmf.algorithms.clustering.kmeans.AlgoKMeans;
import ca.pfv.spmf.patterns.cluster.ClusterWithMean;
import ie.lero.spare.franalyser.utility.FileManipulator;

public class IncidentInstancesClusterGenerator {

	List<GraphPath> instances;
	String instanceFileName;
	String convertedInstancesFileName;
	int numberOFClusters = 3;

	public final static String DATA_SEPARATOR = " "; // space is the separator
	public final static int PADDING_STATE = -1;
	
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

		System.out.println("converting instances to miner format...");
		convertedInstancesFileName = convertInstancesToMinerFormat();

		// apply cluster algorithm (K-mean)

		AlgoKMeans kmean = new AlgoKMeans();
		DistanceFunction distanceFun = new DistanceEuclidian();

		try {
			System.out.println("Generating clusters using K-mean algorithm with K = " + numberOFClusters
					+ ", distance function is " + distanceFun.getName());
			List<ClusterWithMean> outputClusters = kmean.runAlgorithm(convertedInstancesFileName, numberOFClusters,
					distanceFun, DATA_SEPARATOR);

			if (outputClusters != null) {
				System.out.println(outputClusters.size());
			}

			// for(ClusterWithMean cluster: outputClusters) {
			//// System.out.println(cluster.);
			// }
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void generateClusters(String fileName) {

		instanceFileName = fileName;

		generateClusters();

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
		String instancesFolder = instanceFileName.substring(0, instanceFileName.lastIndexOf("/"));
		String outputFileName = instancesFolder + "/convertedInstances.txt";

		String fileLinSeparator = System.getProperty("line.separator");

		StringBuilder builder = new StringBuilder();

		int longestTransition = -1;

		for (GraphPath path : instances) {
			List<Integer> tmp = path.getStateTransitions();

			if (tmp.size() > longestTransition) {
				longestTransition = tmp.size();
			}
		}

		for (GraphPath path : instances) {

			// set instance name to be the instance id
			builder.append(instanceName).append(path.getInstanceID());
			builder.append(fileLinSeparator);

			// set data to be the state transitions
			List<Integer> states = path.getStateTransitions();

			for (int i = 0; i < states.size() - 1; i++) {
				builder.append(states.get(i)).append(DATA_SEPARATOR);
			}
			// add last state
			builder.append(states.get(states.size() - 1));
			
			//pad the transition with -1 
			if(states.size()<longestTransition) {
				
				builder.append(DATA_SEPARATOR);
				
				for (int i = 0; i < longestTransition- states.size()-1; i++) {
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

		String fileName = "D:/Bigrapher data/lero/output/2_4003.json";

		tester.generateClusters(fileName);
	}

}
