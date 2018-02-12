package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.xquery.XQException;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class AssetMap {
	private String[] incidentAssetNames;
	private String[][] spaceAssetMatches;
	private StringBuilder sets;
	public int numberOfSets;
	
	public AssetMap(){
		numberOfSets=0;
		sets = new StringBuilder();
		incidentAssetNames = null;
		spaceAssetMatches = null;
	}
	
	public AssetMap(String [] incidentAssetNames, String[][] spaceAssetMatches) {
		this();
		this.incidentAssetNames = incidentAssetNames;
		this.spaceAssetMatches = spaceAssetMatches;
	}

	public String[] getIncidentAssetNames() {
		return incidentAssetNames;
	}

	public void setIncidentAssetNames(String[] incidentAssetNames) {
		this.incidentAssetNames = incidentAssetNames;
	}

	public String[][] getSpaceAssetMatches() {
		return spaceAssetMatches;  
	}
 
	public void setSpaceAssetMatches(String[][] spaceAssetMatches) {
		this.spaceAssetMatches = spaceAssetMatches;
	}
	
	public String[] getSpaceAssetMatches(int incidentAssetIndex) {
		String [] matches=null;

		if(incidentAssetIndex > 0 && incidentAssetIndex<incidentAssetNames.length) {
			matches = spaceAssetMatches[incidentAssetIndex];
		} 
		
		return matches;
	}
	
	public String[] getSpaceAssetMatched(String incidentAssetName) {
		String [] matches=null;
		int index=-1;
		incidentAssetName = incidentAssetName.toLowerCase();
		for(int i=0;i<incidentAssetName.length();i++) {
			if(incidentAssetNames[i].toLowerCase().contentEquals(incidentAssetName)){
				index = i;
				break;
			}
		}
		if(index != -1) {
			matches = spaceAssetMatches[index];
		}
		return matches;
	}
	
	public String[][] getSpaceAssetsMatched(String[] incidentAssets) {
		String[][] result = new String[incidentAssets.length][];
		
		for(int i=0; i<incidentAssets.length; i++) {
			result[i] = getSpaceAssetMatched(incidentAssets[i]);
		}
		
		return result;
	}
	
	public String toString() { 
		StringBuilder result = new StringBuilder("");
		
		for(int i=0;i<incidentAssetNames.length;i++) {
			result.append(incidentAssetNames[i]).append(":");
			for(int j=0;j<spaceAssetMatches[i].length;j++) {
				result.append(spaceAssetMatches[i][j]).append(",");
			}
			result.deleteCharAt(result.length()-1);
			result.append("\n");
		}
		
		return result.toString();
	}

	public String getCombinations(){
		StringBuilder sets = new StringBuilder();
		//sets.setLength(0);
		//numberOfSets=0;
		//add incident asset names as the first set {incidentAsset1, ..., incidentAssetN}
		sets.append("{");
		for(String e: incidentAssetNames) {
			sets.append(e).append(",");
		}
		sets.deleteCharAt(sets.length()-1);
		sets.append("}\n");
		
		findCombinations(spaceAssetMatches, 0, "",sets);

		return sets.toString();
	}
	
	private void findCombinations(String[][] sets, int indexLower, String prefix, StringBuilder str){
        if(indexLower >= sets.length){
        	String set = prefix.substring(0,prefix.length()-1);
        	if(!containsDuplicate(set)){
        	//add found set {spaceAsset1, ..., spaceAsset2}
             str.append("{").append(set).append("}\n");
             numberOfSets++;
        	}
            return;
        }
        for(String s : sets[indexLower]) {
        
            findCombinations(sets, indexLower+1, prefix+s+",", str);
        }
    }
	
	private  boolean containsDuplicate(String str) {
		boolean isDuplicate=false;
		Map<String, Boolean> charMap = new HashMap<String, Boolean>();
		str = str.trim();
		String [] strs = str.split(",");
	
		for(String key: strs) {
			 if (charMap.containsKey(key)) {
	               return true;

	           } else {
	               charMap.put(key, true);
	           }
		}
		return isDuplicate;
	}
	
	public boolean hasAssetsWithNoMatch(){
		boolean isNotMatched = false;
		
		for(int i=0;i<spaceAssetMatches.length;i++) {
			if(spaceAssetMatches[i][0] == null) {
				return true;
			}
		}
		
		return isNotMatched;
	}
	
	public String[] getIncidentAssetsWithNoMatch() {
		String [] assetNames;
		StringBuilder names = new StringBuilder("");
		
		if(!hasAssetsWithNoMatch()) {
			return null;
		}
		
		for(int i=0;i<spaceAssetMatches.length;i++) {
			if(spaceAssetMatches[i][0] == null || spaceAssetMatches[i][0] == "" ) {
				names.append(incidentAssetNames[i]).append(",");
			}
		}
		names.deleteCharAt(names.length()-1);
		assetNames = names.toString().split(",");
		
		return assetNames;
	}
	
	public String getIncidentAssetInfo(String assetName) {
		String info="";
		String query = XqueryExecuter.NS_DECELERATION + "doc(\""+XqueryExecuter.INCIDENT_DOC+"\")//"+XqueryExecuter.INCIDENT_ROOT_ELEMENT+"/asset[@name=\""+assetName+"\"]";
		
		try {
			info = XqueryExecuter.executeQuery(query);
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return info;
	}
	
	public String getIncidentAssetInfo(String [] assetNames) {
		String info="";
		StringBuilder names = new StringBuilder("(");
		
		for(String e: assetNames) {
			names.append("\"").append(e).append("\",");
		}
		names.deleteCharAt(names.length()-1);
		names.append(")");
		
		System.out.println(names.toString());
		String query = XqueryExecuter.NS_DECELERATION + "doc(\""+XqueryExecuter.INCIDENT_DOC+"\")//"+XqueryExecuter.INCIDENT_ROOT_ELEMENT+"/asset[@name="+names.toString()+"]";
		
		try {
			info = XqueryExecuter.executeQuery(query);
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return info;
	}
	
	public String[] getRandomSpaceAssetMatches() {
		String [] result = new String[spaceAssetMatches.length];
		boolean isUnique = false;
		Random rand = new Random();
		String set="";
		int stopLimit = 0;
		
		while(!isUnique & stopLimit <100000) {
		for(int i=0;i<result.length;i++) {
		//	System.out.println(r);
			result[i] = spaceAssetMatches[i][rand.nextInt(spaceAssetMatches[i].length)];
			set+=result[i]+",";
		}
		set = set.substring(0, set.length()-1);
		if(!containsDuplicate(set)) {
			isUnique = true;
		}
			stopLimit++;
			set="";
		}
		
		//failed to get a unique set 
		if(stopLimit == 100000) {
			System.out.println("AssetMap:getRandomSapceAssetMatches() = no sequence found");
			return null;
		}

		
		return result;
	}
	
}
