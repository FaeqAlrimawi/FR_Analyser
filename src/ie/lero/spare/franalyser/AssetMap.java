package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import javax.xml.xquery.XQException;

import ie.lero.spare.franalyser.utility.CartesianIterator;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class AssetMap {
	private String[] incidentAssetNames;
	private String[][] spaceAssetMatches;
	private LinkedList<String[]> uniqueCombinations;
	public int numberOfSets;
	
	public AssetMap(){
		numberOfSets=0;
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
	
	public LinkedList<String[]> getUniqueCombinations() {
		return uniqueCombinations;
	}

	public void setUniqueCombinations(LinkedList<String[]> uniqueCombinations) {
		this.uniqueCombinations = uniqueCombinations;
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
	
	/**
	 * Checks if there are any duplicate names in the given array
	 * @param strs String array containing the system assets
	 * @return true if two strings in the array are equal
	 */
	private  boolean containsDuplicate(String [] strs) {
		boolean isDuplicate=false;
		Map<String, Boolean> charMap = new HashMap<String, Boolean>();
	
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
	
	/**
	 * Returns all incident entities that have no matches
	 * @return array of strings that hold the names of the incident entities
	 */
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
	
	/**
	 * Returns a random unique sequence of system assets
	 * @return array of strings holding the names of the system assets
	 */
	public String[] getRandomSpaceAssetMatches() {

		if(uniqueCombinations == null) {
			generateUniqueCombinations();
		}
		Random rand = new Random();
		int index = rand.nextInt(uniqueCombinations.size());
		return uniqueCombinations.get(index);
		
	}

	/**
	 * Generates all unique combinations of system assets that correspond to the set of incident assets
	 *@return LinkedList<String[]> containing all unique combinations
	 */
	public LinkedList<String[]> generateUniqueCombinations() {
		
		Iterable<String[]> it = () -> new CartesianIterator<>(spaceAssetMatches, String[]::new);
		uniqueCombinations = new LinkedList<String[]>();
		
		for (String[] s : it) {
				if(!containsDuplicate(s)) {
					uniqueCombinations.add(s);	
				}
		}
		
		return uniqueCombinations;
	}


	
	
	 
}
