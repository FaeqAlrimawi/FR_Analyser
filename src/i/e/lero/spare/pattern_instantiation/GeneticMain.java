package i.e.lero.spare.pattern_instantiation;


import java.util.ArrayList;

import cyberPhysical_Incident.Activity;


public class GeneticMain {
	
	public int assetsMaxNum;
	
	/*for now the assets pool is created and maintained in an arraylist of assets
	the assumptions: there are 4 assets in general incidents, and 10 assets in an environment
	*/
	public static ArrayList<environment.Asset> concreteAssets;
	public static ArrayList<cyberPhysical_Incident.Entity> generalEntities;
	public static ArrayList<Activity> activities;
	
	static int cAssetsNum = 6;
	//for general incidents
	static int  gNumofAssets = 3;
	int numofActivities=4;
	
	//number of rules
	//rules for each asset are 4 currently: isSame? 0 or 1, hasConnectivity? 0 or Type, isParent? 0 or 1, isChild? 0 or 1
	static int rulesNum = 4;
	//rules for each general asset in relation to other general assets in the generalEntities array
	static int [] rules;
	//how far a general asset looks in the array
	static int neighbourhood; //max = number of general assets - 1, min = 1
	
	public GeneticMain(){
		concreteAssets = new ArrayList<environment.Asset>();
		generalEntities = new ArrayList<cyberPhysical_Incident.Entity>();	
		activities = new ArrayList<Activity>();
	}
	
	public void loadConcreteAssets(){
		
	//	int assetsNum = 9;
		
		//need a method to read and convert assets from xmo files to objects
		//thenconverted objects are then added to the concreteAssets array
		
		//[method]
		environment.Asset ast = null;//getAsset() [fake method];
		
		concreteAssets.add(ast);
		
		cAssetsNum = concreteAssets.size();
		
	}
	

	public void loadGeneralAssets(){
		
		cyberPhysical_Incident.Entity ent = null; //which could be asset, resource or actor
		
		generalEntities.add(ent);
		
		gNumofAssets = generalEntities.size();
		
		neighbourhood = gNumofAssets-1;
		
	}
	
/*	public void createGeneralAssetsRules() {
		//rules for each asset are 4 currently: isSame? 0 or 1, hasConnectivity? 0 or 1, isParent? 0 or 1, isChild? 0 or 1
		//the size of the rules array = rulesNum*neighbourhood*size of generalAsset array (i.e. number of general assets)  
		int arraySize = generalEntities.size();
		int sum =0;
		int ind=0;
		GeneralAsset src, des;
		
		//determine array size required
		if (rules == null){
			int in=0;
			int it = 0;
			for( ;in<arraySize-1;in++){
				it = 0;
				for(int j=in+1;j<arraySize;j++){
					sum+=4;
					it++;
					if (it>=neighbourhood){
						break;
					}
				}
			}
		//create array
		rules = new int[sum];
			
		}
		//initialise array
		for(int i=0;i<rules.length;i++){
			rules[i] =-1;
		}
		
		//determine properties
		for (int i=0;i<arraySize;i++){
			for(int j=1;j<=neighbourhood;j++){
				if (i+j < arraySize){//make sure that it does not go out of index
					src = generalEntities.get(i);
					des = generalEntities.get(i+j);
					//ind = (i+j-1)*rulesNum;
					//[1] isSame
					if(src.getId() == des.getId()){
						rules[ind] = 1;
					} else {
						rules[ind] = 0;
					}
					//[2] isConnected
					for(GeneralConnection con: src.getConnections()) {
						if (con.getAssetDes() == des){
							rules[ind+1] = 1;//con.getType().ordinal();
							break;
						} 
					}
					if(rules[ind+1] != 1) {
						rules[ind+1] = 0;
					}
					//[3] is the destination parent of source
					if(src.getParent() == des) {
						rules[ind+2] = 1;
					} else {
						rules[ind+2] = 0;
					}
					//[4] is destination a child in source
					if(src.getContainedAssets().contains(des)){
						rules[ind+3] = 1;
					} else {
						rules[ind+3] = 0;
					}
					ind+=rulesNum;
				}
			}
		}
	}
*/
	
	public static void main(String [] args) {
		
		//loadIncidentFromFile();
	}
}
