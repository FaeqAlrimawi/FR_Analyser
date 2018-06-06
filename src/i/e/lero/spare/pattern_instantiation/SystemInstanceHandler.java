package i.e.lero.spare.pattern_instantiation;

import java.util.HashMap;
import java.util.LinkedList;

import ie.lero.spare.franalyser.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Signature;

public class SystemInstanceHandler {

	private static String outputFolder;
	private static SystemExecutor executor;
	private static TransitionSystem transitionSystem;
	private static HashMap<Integer, Bigraph> states;
	private static Signature globalBigraphSignature;
	private static boolean isDebugging = true;
	private static String errorSign = "## ";
	public static boolean analyseSystem() {

		boolean isDone = false;
		
		if (executor == null) {
			print("SystemHandler>> Bigraph System Executor is not set");
			isDone = false;
		} else {
			print("SystemHandler>> Executing the Bigraphical Reactive System (BRS)...");
			outputFolder = executor.execute();

			if (outputFolder != null) {
				print("SystemHandler>> Creating Bigraph Signature...");

				// get the signature
				globalBigraphSignature = executor.getBigraphSignature();

				if (globalBigraphSignature != null) {
				} else {
					print("SystemHandler>> " + errorSign + "Something went wrong creating the Bigraph signature");
					isDone = false;
				}

				print("SystemHandler>> Creating Bigraph transition system...");
				// get the transition system
				transitionSystem = executor.getTransitionSystem();

				if (transitionSystem != null) {
				} else {
					print("SystemHandler>> " + errorSign
							+ "something went wrong while creating the Bigraph transition system");
					isDone = false;
				}

				print("SystemHandler>> Loading states...");
				// gete states as Bigraph objects
				states = executor.getStates();

				if (states != null) {
				} else {
					print("SystemHandler>> " + errorSign
							+ "something went wrong while loading the Bigraph system states");
					isDone = false;
				}

				isDone = true;
			} else {
				print("SystemHandler>> " + errorSign + "something went wrong while executing the BRS");
				isDone = false;
			}

			if (isDone) {
				print("SystemHandler>> Initialisation completed successfully");
			} else {
				print("SystemHandler>> Initialisation was NOT completed successfully...");
			}
		}

		return isDone;
	}

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

class BigraphMatcherThread implements Runnable {

	private int startIndex;
	private int endIndex;
	private LinkedList<Integer> statesMatched;
	private Bigraph redex;
	private String threadID;
	private Thread t;
	
	 public BigraphMatcherThread(int start, int end, Bigraph redex) {
		startIndex = start;
		endIndex = end;
		statesMatched = new LinkedList<Integer>();		
		this.redex = redex;
		threadID= "bla";
	}
	 
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Matcher matcher = new Matcher();
		
		for(int i=startIndex;i<endIndex;i++) {
			if (matcher.match(SystemInstanceHandler.getStates().get(i), redex).iterator().hasNext()) {
				statesMatched.add(i);
				System.out.println("state " + i + " matched");
		}
		}
	}
	
	public void start() {
		//System.out.println("Starting " + threadID);
		if (t == null) {
			t = new Thread(this, "" + threadID);
			t.start();
		}
	}


}
