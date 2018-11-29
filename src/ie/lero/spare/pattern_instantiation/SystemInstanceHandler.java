package ie.lero.spare.pattern_instantiation;

import java.util.HashMap;

import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Signature;

public class SystemInstanceHandler {

	private String outputFolder;
	private SystemExecutor executor;
	private TransitionSystem transitionSystem;
	private HashMap<Integer, Bigraph> states;
	private Signature globalBigraphSignature;
	private boolean isDebugging = true;
	private String errorSign = "## ";
	private Logger logger;
	private long sysID;

	public boolean analyseBRS() {

		boolean isDone = false;
		// BlockingQueue<String> msgQ = Logger.getInstance().getMsgQ();
		// Logger logger = Logger.getInstance();

		if (executor == null) {
			logger.putError("SystemInstanceHandler>> Bigraph System Executor is not set");
			isDone = false;
		} else {
			logger.putMessage("SystemInstanceHandler>> Executing the Bigraphical Reactive System (BRS)...");
			outputFolder = executor.execute();

			if (outputFolder != null) {
				logger.putMessage("SystemInstanceHandler>> Creating Bigraph Signature...");

				// get the signature
				globalBigraphSignature = executor.getBigraphSignature();

				if (globalBigraphSignature != null) {
				} else {
					logger.putMessage("SystemInstanceHandler>> " + errorSign
							+ "Something went wrong creating the Bigraph signature");
					isDone = false;
				}

				logger.putMessage("SystemInstanceHandler>> Creating Bigraph transition system...");
				// get the transition system
				transitionSystem = executor.getTransitionSystem();

				if (transitionSystem != null) {
				} else {
					logger.putMessage("SystemInstanceHandler>> " + errorSign
							+ "something went wrong while creating the Bigraph transition system");
					isDone = false;
				}

				logger.putMessage("SystemInstanceHandler>> Loading states...");
				// gete states as Bigraph objects
				states = executor.getStates();

				if (states != null) {
				} else {
					logger.putMessage("SystemInstanceHandler>> " + errorSign
							+ "something went wrong while loading the Bigraph system states");
					isDone = false;
				}

				isDone = true;
			} else {
				logger.putMessage(
						"SystemInstanceHandler>> " + errorSign + "something went wrong while executing the BRS");
				isDone = false;
			}
		}

		return isDone;
	}

	/*
	 * public static boolean analyseBRS() {
	 * 
	 * boolean isDone = false;
	 * 
	 * if (executor == null) { isDone = false; } else { outputFolder =
	 * executor.execute(); if (outputFolder != null) {
	 * 
	 * // get the signature globalBigraphSignature =
	 * executor.getBigraphSignature();
	 * 
	 * if (globalBigraphSignature != null) { } else { isDone = false; } // get
	 * the transition system transitionSystem = executor.getTransitionSystem();
	 * 
	 * if (transitionSystem != null) { } else { isDone = false; } // gete states
	 * as Bigraph objects states = executor.getStates();
	 * 
	 * if (states != null) { } else { isDone = false; }
	 * 
	 * isDone = true; } else { isDone = false; } }
	 * 
	 * return isDone; }
	 */

	/*
	 * public static boolean analyseSystem(String fileName, SystemExecutor exec)
	 * { executor = exec; return analyseSystem(fileName); }
	 */

	public void setSysID(long id) {
		sysID = id;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public SystemExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(SystemExecutor executor) {
		this.executor = executor;
	}

	public TransitionSystem getTransitionSystem() {
		return transitionSystem;
	}

	public HashMap<Integer, Bigraph> getStates() {
		return states;
	}

	public Signature getGlobalBigraphSignature() {
		return globalBigraphSignature;
	}

	public void setBigraphSignature(Signature bigraphSignature) {
		this.globalBigraphSignature = bigraphSignature;
	}

	public void print(String msg) {

		if (isDebugging) {
			System.out.println(msg);
		}
	}
	//
	// public static void main(String[] args) {
	//
	// /* String fileName = "sav/savannah-general.big";
	// outputFolder = "sav/output10000";
	// //fileName = "sb3.big";
	// //outputFolder = "sb3_output";
	// Matcher matcher = new Matcher();
	// JSONParser parser = new JSONParser();
	//
	// Bigraph redex;
	//// createSignatureFromStates();
	// //loadStates();
	// //print(""+getTransitionSystem().loadNumberOfStates());
	//
	// try {
	// if (loadStates() == null) {
	// return;
	// }
	// redex = convertJSONtoBigraph((JSONObject) parser.parse(new
	// FileReader(outputFolder + "/99.json")));
	// for (int i = 0; i < states.size(); i++) {
	// if (matcher.match(states.get(i), redex).iterator().hasNext()) {
	// print("state " + i + " matched");
	// }
	// }
	// int numberOFThreads = 10;
	// int size = 10000/numberOFThreads;
	// BigraphMatcherThread [] threads = new
	// BigraphMatcherThread[numberOFThreads];
	// print("matching started at " + dtf.format(LocalDateTime.now()));
	// for(int i=0;i<numberOFThreads;i++) {
	//// print((i*size)+ " "+((i*size)+size));
	// threads[i] = new BigraphMatcherThread(i*size, (i*size)+size, redex);
	// threads[i].start();
	// }
	// } catch (IOException | ParseException e) {
	// e.printStackTrace();
	// }*/
	//
	// }

}
