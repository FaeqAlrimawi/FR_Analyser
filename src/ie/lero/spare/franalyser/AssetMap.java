package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.util.Arrays;
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
		
		LinkedList<String> list = new LinkedList<String>();

		for(String key: strs) {
			 if (list.contains(key)) {
	               return true;

	           } 
			 list.add(key);
		}
		return false;
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
	
public LinkedList<String[]> generateUniqueCombinations2() {
		
		//multi-threading is requried to speed up the process of finding all possible combinations
		//LinkedList<String> [] arys = new LinkedList<String>()[5];
		LinkedList<String> ary1 = new LinkedList<String>();
		LinkedList<String> ary2 = new LinkedList<String>();
		int num = 2;
		int columns = 2;
		String [][][] seq1 = new String [num][columns][];
		
		for(int i=0;i<num;i++) {
			for(int j=0;j<columns;j++) {
				seq1[i][j] = new String[spaceAssetMatches[j+(i*columns)].length];
				seq1[i][j] = spaceAssetMatches[j+(i*columns)];
			}
			
		}
		
		//create threads
		SetsGeneratorThread [] setsGenerators = new SetsGeneratorThread [num];
		
		for(int i=0;i<num;i++) {
			setsGenerators[i] = new SetsGeneratorThread(seq1[i], ary1);
		}
		
		
		return uniqueCombinations;
	}

	public static void main(String [] args){
		
		AssetMap m = new AssetMap();
		
		//represents number of system assets that match each incident asset assuming
		int rows = 3;
		//represents number of incident assets
		int columns = 5;
		String [] a = {"a", "b", "c"};
		System.out.println(Arrays.toString(a));
		String [][] tst = new String[rows][columns];
		
		//generate dummy array assuming they are all unique
		for(int i = 0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				tst[i][j] = "this is test [" +i+""+j+"]";
			}
		}
		
		m.setSpaceAssetMatches(tst);
		LinkedList<String[]> seq = m.generateUniqueCombinations();
		
		//size (if all unique) = columns^rows
		System.out.println(seq.size());
		
	}	 
}

class SetsGeneratorThread implements Runnable {

	private int threadID;
	private Thread t;
	private String [][] array;
	private LinkedList<String> resultArray;
	
	public SetsGeneratorThread(String [][] ary, LinkedList<String> result) {
		// TODO Auto-generated constructor stub
		array = ary;
		resultArray = result;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Iterable<String[]> it = () -> new CartesianIterator<>(array, String[]::new);
		int i=0;
		for (String[] s : it) {
				if(!containsDuplicate(s)) {
					resultArray.add( Arrays.toString(s));
		
					//uniqueCombinations.add(s);	
				}
		}	
	}
	
	public void start() {
		System.out.println("Starting " + threadID);
		if (t == null) {
			t = new Thread(this, "" + threadID);
			t.start();
		}
	}
	
private  boolean containsDuplicate(String [] strs) {
		
		LinkedList<String> list = new LinkedList<String>();

		for(String key: strs) {
			 if (list.contains(key)) {
	               return true;

	           } 
			 list.add(key);
		}
		return false;
	}

}
