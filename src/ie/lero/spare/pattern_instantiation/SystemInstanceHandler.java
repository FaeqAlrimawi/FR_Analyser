package ie.lero.spare.pattern_instantiation;

import java.util.HashMap;

import ie.lero.spare.franalyser.utility.Logger;
import ie.lero.spare.franalyser.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Signature;

public class SystemInstanceHandler {

	private static String outputFolder;
	private static SystemExecutor executor;
	private static TransitionSystem transitionSystem;
	private static HashMap<Integer, Bigraph> states;
	private static Signature globalBigraphSignature;
	private static boolean isDebugging = true;
	private static String errorSign = "## ";
	
	public static boolean analyseBRS() {
		
		boolean isDone = false;
//		BlockingQueue<String> msgQ = Logger.getInstance().getMsgQ();
		Logger logger = Logger.getInstance();

		if (executor == null) {
			logger.putMessage("BRSHandler>> Bigraph System Executor is not set");
			isDone = false;
		} else {
			logger.putMessage("BRSHandler>> Executing the Bigraphical Reactive System (BRS)...");
			outputFolder = executor.execute();

			if (outputFolder != null) {
				logger.putMessage("BRSHandler>> Creating Bigraph Signature...");

				// get the signature
				globalBigraphSignature = executor.getBigraphSignature();

				if (globalBigraphSignature != null) {
				} else {
					logger.putMessage("BRSHandler>> " + errorSign + "Something went wrong creating the Bigraph signature");
					isDone = false;
				}

				logger.putMessage("BRSHandler>> Creating Bigraph transition system...");
				// get the transition system
				transitionSystem = executor.getTransitionSystem();

				if (transitionSystem != null) {
				} else {
					logger.putMessage("BRSHandler>> " + errorSign
							+ "something went wrong while creating the Bigraph transition system");
					isDone = false;
				}

				logger.putMessage("BRSHandler>> Loading states...");
				// gete states as Bigraph objects
				states = executor.getStates();

				if (states != null) {
				} else {
					logger.putMessage("BRSHandler>> " + errorSign
							+ "something went wrong while loading the Bigraph system states");
					isDone = false;
				}

				isDone = true;
			} else {
				logger.putMessage("BRSHandler>> " + errorSign + "something went wrong while executing the BRS");
				isDone = false;
			}
		}

		return isDone;
	}
	
/*	public static boolean analyseBRS() {
		
		boolean isDone = false;
		
		if (executor == null) {
			isDone = false;
		} else {
			outputFolder = executor.execute();
			if (outputFolder != null) {

				// get the signature
				globalBigraphSignature = executor.getBigraphSignature();

				if (globalBigraphSignature != null) {
				} else {
					isDone = false;
				}
				// get the transition system
				transitionSystem = executor.getTransitionSystem();

				if (transitionSystem != null) {
				} else {
					isDone = false;
				}
				// gete states as Bigraph objects
				states = executor.getStates();

				if (states != null) {
				} else {
					isDone = false;
				}

				isDone = true;
			} else {
				isDone = false;
			}
		}

		return isDone;
	}*/

/*	public static boolean analyseSystem(String fileName, SystemExecutor exec) {
		executor = exec;
		return analyseSystem(fileName);
	}*/

	public static String getOutputFolder() {
		return outputFolder;
	}

	public static void setOutputFolder(String outputFolder) {
		SystemInstanceHandler.outputFolder = outputFolder;
	}

	public static SystemExecutor getExecutor() {
		return executor;
	}

	public static void setExecutor(SystemExecutor executor) {
		SystemInstanceHandler.executor = executor;
	}

	public static TransitionSystem getTransitionSystem() {
		return transitionSystem;
	}

	public static HashMap<Integer, Bigraph> getStates() {
		return states;
	}

	public static Signature getGlobalBigraphSignature() {
		return globalBigraphSignature;
	}

	public static void setBigraphSignature(Signature bigraphSignature) {
		SystemInstanceHandler.globalBigraphSignature = bigraphSignature;
	}

	public static void print(String msg) {

		if (isDebugging) {
			System.out.println(msg);
		}
	}
	
	public static void main(String[] args) {

	/*	String fileName = "sav/savannah-general.big";
		outputFolder = "sav/output10000";
		//fileName = "sb3.big";
		//outputFolder = "sb3_output";
		Matcher matcher = new Matcher();
		JSONParser parser = new JSONParser();

		Bigraph redex;
//		createSignatureFromStates();
		//loadStates();
		//print(""+getTransitionSystem().loadNumberOfStates());

		try {
			if (loadStates() == null) {
				return;
			}
			redex = convertJSONtoBigraph((JSONObject) parser.parse(new FileReader(outputFolder + "/99.json")));
			for (int i = 0; i < states.size(); i++) {
				if (matcher.match(states.get(i), redex).iterator().hasNext()) {
					print("state " + i + " matched");
				}
			}
			int numberOFThreads = 10;
			int size = 10000/numberOFThreads;
			BigraphMatcherThread [] threads = new BigraphMatcherThread[numberOFThreads];
			print("matching started at " + dtf.format(LocalDateTime.now()));
			for(int i=0;i<numberOFThreads;i++) {
//				print((i*size)+ " "+((i*size)+size));
				threads[i] = new BigraphMatcherThread(i*size, (i*size)+size, redex);
				threads[i].start();
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}*/

	}

}

