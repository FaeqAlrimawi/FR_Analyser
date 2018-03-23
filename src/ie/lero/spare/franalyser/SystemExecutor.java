package ie.lero.spare.franalyser;

import java.util.HashMap;

import ie.lero.spare.franalyser.utility.TransitionSystem;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Signature;

public interface SystemExecutor {
	
	public String execute();
	public TransitionSystem getTransitionSystem();
	public Signature getBigraphSignature();
	public HashMap<Integer, Bigraph> getStates();

}
