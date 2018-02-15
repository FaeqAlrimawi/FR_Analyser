package ie.lero.spare.franalyser;

import java.util.LinkedList;

import it.uniud.mads.jlibbig.core.std.*;


public class TestBigLib {
	
	public static void main(String[] args) {
		Control room = new Control("Room", true, 1);
		Control hallway = new Control("Hallway", true, 1);
		Control server = new Control("Server", false, 1);
		LinkedList<Control> ctrls = new LinkedList<Control>();
		ctrls.add(room);
		ctrls.add(hallway);
		ctrls.add(server);
		
		Signature sig = new Signature(ctrls);
		
		BigraphBuilder bi = new BigraphBuilder(sig);
		OuterName a1 = bi.addOuterName("p1");
		OuterName a2 = bi.addOuterName("p2");
		Root root = bi.addRoot(0);
		Root root1 = bi.addRoot(1);
		Node r2 = bi.addNode("Room", root1, a1);
		Node r3 = bi.addNode("Server", r2);
		Node r4 = bi.addNode("Hallway", root1, a1);
		Bigraph p = bi.makeBigraph();
		
		System.out.println(p.toString());
		//System.out.println(p.getSignature().toString());
	
		
	}

}
