package ie.lero.spare.franalyser;

import java.util.LinkedList;

import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.Control;
import it.uniud.mads.jlibbig.core.std.InnerName;
import it.uniud.mads.jlibbig.core.std.Match;
import it.uniud.mads.jlibbig.core.std.Matcher;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.OuterName;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Signature;
import it.uniud.mads.jlibbig.core.std.Site;

public class TestBigLib {

	public static void main(String[] args) {
		Control room = new Control("Room", true, 1);
		Control hallway = new Control("Hallway", true, 1);
		Control server = new Control("Server", false, 1);
		LinkedList<Control> ctrls = new LinkedList<Control>();
		ctrls.add(room);
		ctrls.add(hallway);
		ctrls.add(server);

		// used to define the controls..it is immutable
		Signature sig = new Signature(ctrls);

		BigraphBuilder bi = new BigraphBuilder(sig);

		// define outer names
		OuterName a1 = bi.addOuterName("p1");
		OuterName a2 = bi.addOuterName("p2");
		OuterName serv = bi.addOuterName("serv");

		// define inner names
		InnerName i1 = bi.addInnerName("in1");

		// define regions (or roots)
		Root root = bi.addRoot();

		// define sites
		Site site = bi.addSite(root);

		// add nodes (based on defined controls). Defining nodes includes
		// defining containment and connectivity
		// i.e. addNode("Control name" [should exist in the signature], parent
		// node, [outerNames it could be connected to])
		Node r2 = bi.addNode("Room", root, a1);

		Node r4 = bi.addNode("Hallway", root, a1);
		Node r3 = bi.addNode("Server", r2, serv);
		Node r5 = bi.addNode("Server", r4);
		bi.addNode("Room", root, a1);
		bi.addNode("Room", root, a1);
		bi.ground();
		// create bigraph
		Bigraph p = bi.makeBigraph();

		// bi.addNode("Server", ro, bi.addOuterName());
		// bi.ground();

		// match bigraph to a redex

		// redex definition
		bi = new BigraphBuilder(sig);
		Node ro = bi.addNode("Room", bi.addRoot(0), bi.addOuterName("p1"));
		bi.addSite(ro);
		bi.addNode("Server", ro, bi.addOuterName());
		Bigraph redex = bi.makeBigraph();

		// match
		Matcher matcher = new Matcher();

		// finds all possible matches in a given bigraph. We might not need
		// this, only if there exists
		// just one match in a state then it is enough. The function could be
		// modified to return
		// just a true false instead of an iterator
		for (Match ma : matcher.match(p, redex)) {
			// System.out.println(ma.toString());
			System.out.println("entered");
		}

		// System.out.println("redex: \n"+redex.toString());
		// System.out.println("bigraph: \n"+p.toString());

		// System.out.println(p.getSignature().toString());

	}

}
