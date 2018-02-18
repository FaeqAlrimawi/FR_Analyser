package Testing;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import it.uniud.mads.jlibbig.core.std.BigraphBuilder;
import it.uniud.mads.jlibbig.core.std.SignatureBuilder;

public class Converter {
	
	public void convertJSONtoLibBiG(){
		JSONParser parser = new JSONParser();
		LinkedList<String> controls = new LinkedList<String>();
		String tmp;
		String tmpArity;
		JSONObject tmpObj;
		SignatureBuilder sigBuilder = new SignatureBuilder();
		
		try {
			JSONObject state = (JSONObject) parser.parse(new FileReader("output/0.json"));
			
			JSONArray ary = (JSONArray) state.get("nodes");
			Iterator<JSONObject> it = ary.iterator();
			
			//get controls & their arity [defines signature]
			while(it.hasNext()) {
				tmpObj = (JSONObject) it.next().get("control");
				tmp = tmpObj.get("control_id").toString();
				tmpArity = tmpObj.get("control_arity").toString();
			
				if(!controls.contains(tmp)) {
					controls.add(tmp); //to avoid duplicates
					sigBuilder.add(tmp,true, Integer.parseInt(tmpArity));
				}
				
			}
			
			BigraphBuilder biBuilder = new BigraphBuilder(sigBuilder.makeSignature());
			
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void main(String [] args) {
		
		Converter c = new Converter();
		
		c.convertJSONtoLibBiG();
	}
}
