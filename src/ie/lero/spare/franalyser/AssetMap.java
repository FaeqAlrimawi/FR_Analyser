package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import javax.xml.xquery.XQException;

import ie.lero.spare.franalyser.utility.CartesianIterator;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class AssetMap {
	private String[] incidentAssetNames;
	private String[][] spaceAssetMatches;
	private LinkedList<String[]> uniqueCombinations;
	public int numberOfSets;
	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	private static LocalDateTime now;
	
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
		
		System.out.println("size "+uniqueCombinations.size());
		return uniqueCombinations;
	}
	
public LinkedList<String[]> generateUniqueCombinations2() {
		
		//multi-threading is requried to speed up the process of finding all possible combinations
		//LinkedList<String> [] arys = new LinkedList<String>()[5];
		//number of segments required
		int num = 2;
		//number of space assets each segment should take
		int size = 4;
		
		//segments
		String [][][] segments = new String [num][size][];
		LinkedList<String> [] results = new LinkedList[num];
		
		//segments the spaceAssets (or system assets array) into several 2d arrays
		//where each 2d aray points to a segment in system array
		for(int i=0;i<num;i++) {
			for(int j=0;j<size;j++) {
				segments[i][j] = new String[spaceAssetMatches[j+(i*size)].length];
				segments[i][j] = spaceAssetMatches[j+(i*size)];
			}
			
			//create lists to hold results from each segment
			results[i] = new LinkedList<String>();	
		}
		
		//used to wait for the threads to end before proceeding with result
		CountDownLatch latch = new CountDownLatch(num);
		
		//create threads
		SetsGeneratorThread [] setsGenerators = new SetsGeneratorThread [num];
		
		for(int i=0;i<num;i++) {
			setsGenerators[i] = new SetsGeneratorThread(segments[i], results[i], latch, i);
			setsGenerators[i].start();
		}
		
		//wait for threads to finish execution
		try {
			latch.await();
			String [][] res = new String [2][];
			res[0] = results[0].toArray(new String[0]);
			res[1] = results[1].toArray(new String[0]);
			LinkedList<String> finalResult = new LinkedList<String>();
			
			Iterable<String[]> it = () -> new CartesianIterator<>(res, String[]::new);
			for (String[] s : it) {
					if(!containsDuplicate(s)) {
						finalResult.add( Arrays.toString(s));
						//uniqueCombinations.add(s);	
					}
			}
			
			System.out.println("size: "+finalResult.size());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/*		for(int i=0;i<num;i++) {
			System.out.println("size: "+results[i].size()+"\n"+results[i]);
		}
		*/
		return uniqueCombinations;
	}

	public static void main(String [] args){
		
		AssetMap m = new AssetMap();
		AssetMap m2 = new AssetMap();
		//represents number of system assets that match each incident asset assuming
		int rows = 8;
		//represents number of incident assets
		int columns = 10;
//		String [] a = {"a", "b", "c"};
//		System.out.println(Arrays.toString(a));
		String [][] tst = new String[rows][columns];
		int cnt = 0;
		//generate dummy array assuming they are all unique
		for(int i = 0;i<rows;i++) {
			for(int j=0;j<columns;j++) {
				tst[i][j] = ""+cnt;
				cnt++;
			}
		}
		
		m.setSpaceAssetMatches(tst);
		m2.setSpaceAssetMatches(tst);
		
		/*System.out.println("Testing [The generation of unqiue sequences USING threads] using a "+rows+""
				+ "*"+columns+ "\nstatring time [" + dtf.format(LocalDateTime.now())+"]");
		LinkedList<String[]> seq = m.generateUniqueCombinations2();
		System.out.println("Finished [" + dtf.format(LocalDateTime.now())+"]\n\n");*/
		
		System.out.println("Testing [The generation of unqiue sequences WITHOUT threads] using a "+rows+""
				+ "*"+columns+ "\nstatring time [" + dtf.format(LocalDateTime.now())+"]");
		LinkedList<String[]> seq2 = m2.generateUniqueCombinations();
		System.out.println("Finished [" + dtf.format(LocalDateTime.now())+"]");
		
		//size (if all unique) = columns^rows
		//System.out.println(seq.size());
		
	}	 
}

class SetsGeneratorThread implements Runnable {

	private int threadID;
	private Thread t;
	private String [][] array;
	private LinkedList<String> resultArray;
	private CountDownLatch latch;
	
	public SetsGeneratorThread(String [][] ary, LinkedList<String> result, CountDownLatch latch, int id) {
		// TODO Auto-generated constructor stub
		array = ary;
		resultArray = result;
		this.latch = latch;
		threadID = id;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		Iterable<String[]> it = () -> new CartesianIterator<>(array, String[]::new);
		for (String[] s : it) {
				if(!containsDuplicate(s)) {
					resultArray.add( Arrays.toString(s));
					//uniqueCombinations.add(s);	
				}
		}
		
		latch.countDown();
		/*if(resultArray != null)
		System.out.println(threadID+" size:"+resultArray.size());
		System.out.println(resultArray.toString());*/
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
