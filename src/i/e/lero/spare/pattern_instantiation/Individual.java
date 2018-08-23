package i.e.lero.spare.pattern_instantiation;


import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cyberPhysical_Incident.IncidentEntity;
import cyberPhysical_Incident.Knowledge;
import cyberPhysical_Incident.Location;
import environment.Asset;

public class Individual {

	//DNA representation, each position in the array holds the index to an asset in the assets array
	int [] assets;
	
	//assets properties values corresponding to the rules of general assets
	int [] assetsProperties;
	int maxConcreteAssetNum;
	
	//scores related to comparing properties of an entity
	protected static int typeMatchScore = 30;
	protected static int parentTypeMatchScore = 20;
	protected static int connectionsMatchScore = 15;
	protected static int containedAssetsMatchScore = 25;
	protected static int propertyMatchScore = 15;
	
	//scores related to relations between entities
	protected static double matchRelationScoreAddedPercentage = 0.05;
	protected static double misMatchRelationScoreDeductedPercentage = 0.05;
	protected static int isChildMatchScore = 5;
	protected static int isConnectionMatchScore = 5;
	

	public Individual() {
		this(GeneticMain.gNumofAssets);
		
	}
	
	public Individual(int assetsNum){
		maxConcreteAssetNum = GeneticMain.cAssetsNum;
		assetsProperties = new int[GeneticMain.rules.length];
		
		createDNA(assetsNum);
	}
	
	public void createDNA(int assetsNum){
		assets = new int[assetsNum];
		Random r = new Random();
		
		for(int i=0;i<assetsNum;i++){			
			assets[i] = r.nextInt(maxConcreteAssetNum);
		}
	}
	
	public double  measurefitness(){
		double score=0.1;
		
		for(int i=0;i<assets.length;i++){
			score+=match(i,assets[i]);
		}
		
		//generate proterties between assets of this indiviual
		generateProperties();

		score +=evaluateProperties();

		//really bad individual
		if(score<=0) {
			score = 0.0001;
		}
		
		return score*score; //magnify small differences between solutions
		
	}
	
	/*double compareGenConAssets(int indexGen, int indexCon) {
		double score = 0;
		cyberPhysical_Incident.IncidentEntity genEntity;
		environment.Asset conAsset;
		
		//load assets
		genEntity = GeneticMain.generalEntities.get(indexGen);
		conAsset = GeneticMain.concreteAssets.get(indexCon);
		
		//comparison based on criteria should ha[[en here
		
		//match type
		if(genEntity.getType() == conAsset.getType()) {//1st level (most specific)
			score += 3; 
		} 
		else if(genEntity.getType() == conAsset.getType().getParent()) { //2nd level
			score += 2; 
		
		} else if(genEntity.getType() == conAsset.getType().getParent().getParent()) {//3rd level (least specific)
			score += 1; 
		
		} else if(genEntity.getType().getParent() == conAsset.getType().getParent()) { //more loose comparison (parent of general asset with parent of concrete asset)
			score += 0.75; 
		} else if(genEntity.getType().getParent() == conAsset.getType().getParent().getParent()) { //parent of gen asset with grandparent of con asset
			score += 0.5; 
		}
		
		return score;
	}*/
	
	private double match(int indexGen, int indexCon) {
		
		double score = 0;
		cyberPhysical_Incident.IncidentEntity entity;
		environment.Asset asset;
		
		//load assets
		entity = GeneticMain.generalEntities.get(indexGen);
		asset = GeneticMain.concreteAssets.get(indexCon);
		
		/**
		 * matching criteria
		 * 1-Type of entity which is taken from the tag type in the model. This compared to the type 
		 * of an asset as a class (e.g., entity has type "Room" returns all assets that are instances of
		 * Room class or its subclasses 
		 * 2-Type of parent (container of asset/entity)
		 * 3-Number & type of contained assets.
		 * 4-Number & type of connections. All incident entity connections should be subset of the
		 * connections of an asset if knowledge is partial or exact if knowledge is exact. Type can be 
		 * of the same class or subclass.
		 * 5-Status if found in the entity and entity is an Asset (not implemented as not all assets hold status)
		 * 6-Properities if found in the entity
		 */
		
		/** matching Type **/
		
		Class<?> potentialClass = null;
		
		if(entity.getType() != null ) {
		String typeName = entity.getType().getName();
		try {
			String potentialClassName = "environment.impl."+typeName;
			
			if(!typeName.endsWith("Impl")) {
				potentialClassName +="Impl";
			}
			
			potentialClass = Class.forName(potentialClassName);
			
		} catch (ClassNotFoundException e) {
			//type mismatch i.e. there is no type available in the system model 
			//currently return false
			//return false;
//			score += 3; 
		}
		
		//if the current asset object is not of the same class or subclass of the potential class
		//then return false (type mismatch)
		if(potentialClass.isInstance(asset)) {
			score += typeMatchScore; 
		}
		
		}
		
		
		/** matching Parent type**/
		IncidentEntity parent = (IncidentEntity)entity.getParentEntity();
		
		if(parent != null) {
			if(parent.getType() != null ) {
				String typeName = parent.getType().getName();
				try {
					String potentialClassName = "environment.impl."+typeName;
					
					if(!typeName.endsWith("Impl")) {
						potentialClassName +="Impl";
					}
					
					potentialClass = Class.forName(potentialClassName);
					
				} catch (ClassNotFoundException e) {
					//type mismatch i.e. there is no type available in the system model 
					//currently return false (maybe loosened a little like ignore)
					//return false;
				}
				
				//if the current asset object is not of the same class or subclass of the potential class
				//then return false (type mismatch)
				environment.Asset parentAsset = null;
				
				if(environment.DigitalAsset.class.isInstance(asset)) {
					parentAsset = ((environment.DigitalAsset)asset).getParentAsset();
				} else if(environment.PhysicalAsset.class.isInstance(asset)) {
					parentAsset = ((environment.PhysicalAsset)asset).getParentAsset();
				}
				
				//if the asset has no parent but the incident entity has then return false (no match)
				if(parentAsset == null) {
//					return false;
				}
				
				if(potentialClass.isInstance(parentAsset)) {
					score += parentTypeMatchScore;
				}
				
			}
		}
		
		/** matching contained assets (number & type) **/
		//if knowledge is exact then both should have the same number of connections
		//otherwise there's no match
/*		if(entity.getContainedAssetsKnowledge().compareTo(Knowledge.EXACT) == 0) {
			if(entity.getContainedEntities().size() != entity.getContainedEntities().size()) {
			
//				return false;
			}
		}
		*/
		//if the incident entity has more connections then it cannot be subset of the asset connections
		//thus there's no match
		if(entity.getContainedEntities().size() < entity.getContainedEntities().size()) {
		
		//compare connection type (simialr to asset type)
		LinkedList<Integer> matchedcontainedAssets = new LinkedList<Integer>();
		
		for(Location ent : entity.getContainedEntities()) {
			
			IncidentEntity containedEntity = (IncidentEntity)ent;
			
			if(containedEntity.getType() == null) {
				continue;//ignored
			}
			
			String typeName = containedEntity.getType().getName();
			
			try {
			String potentialClassName = "environment.impl."+typeName;
			
			if(!typeName.endsWith("Impl")) {
				potentialClassName +="Impl";
			}
			
			potentialClass = Class.forName(potentialClassName);
			
			} catch (ClassNotFoundException e) {
				//type mismatch i.e. there is no type available in the system model 
				//currently returns false
//				return false;
				break; //very strict
			}
			
			//if the current asset connection object is not of the same class or subclass of the potential class
			//then return false (connection type mismatch)
			boolean iscontainedEntityMatched = false;
			
			List<environment.Asset> containedAssets = (List<Asset>)asset.getContainedAssets();
			
			environment.Asset containedAsset = null;
			
			for(int i=0;i<containedAssets.size();i++) {
				
				if(matchedcontainedAssets.contains(i)) {
					continue;
				}
				
				containedAsset = containedAssets.get(i);
				
				if(potentialClass.isInstance(containedAsset)) {
					matchedcontainedAssets.add(i);
					iscontainedEntityMatched = true;
					break;
				} 
			}
			
			//if none of the asset connections match the incident connection then it is a mismatch
			if(!iscontainedEntityMatched) {
//				return false;
				break;
			}
			
			iscontainedEntityMatched = false;
			
		}
		
		if(entity.getContainedEntities().size() == matchedcontainedAssets.size()) {
			score += containedAssetsMatchScore;
		}
		
		}
		
		/** matching connections (number & type) **/
		//if knowledge is exact then both should have the same number of connections
		//otherwise there's no match
		if(entity.getConnectionsKnowledge().compareTo(Knowledge.EXACT) == 0) {
			if(entity.getConnections().size() != asset.getConnections().size()) {
			
//				return false;
			}
		}
		
		//if the incident entity has more connections then it cannot be subset of the asset connections
		//thus there's no match
		if(entity.getConnections().size() < asset.getConnections().size()) {
	
		//compare connection type (simialr to asset type)
		LinkedList<Integer> matchedAssetCons = new LinkedList<Integer>();
		
		for(cyberPhysical_Incident.Connection entityCon : entity.getConnections()) {
			
			if(entityCon.getType() == null) {
				continue;//ignored
			}
			
			String typeName = entityCon.getType().getName();
			
			try {
			String potentialClassName = "environment.impl."+typeName;
			
			if(!typeName.endsWith("Impl")) {
				potentialClassName +="Impl";
			}
			
			potentialClass = Class.forName(potentialClassName);
			
			} catch (ClassNotFoundException e) {
				//type mismatch i.e. there is no type available in the system model 
				//currently returns false
//				return false;
				break;
			}
			
			//if the current asset connection object is not of the same class or subclass of the potential class
			//then return false (connection type mismatch)
			boolean isConnectionMatched = false;
			
			List<environment.Connection> assetCons = asset.getConnections();
			environment.Connection assetCon = null;
			
			for(int i=0;i<assetCons.size();i++) {
				
				if(matchedAssetCons.contains(i)) {
					continue;
				}
				
				assetCon = assetCons.get(i);
				
				if(potentialClass.isInstance(assetCon)) {
					matchedAssetCons.add(i);
					isConnectionMatched = true;
					break;
				} 
			}
			
			//if none of the asset connections match the incident connection then it is a mismatch
			if(!isConnectionMatched) {
				break;
			}
			
			isConnectionMatched = false;
			
		}
		
		if(entity.getConnections().size() == matchedAssetCons.size()) {
			score += connectionsMatchScore;
		}
		
		}
		
		
		/** matching status **/
	/*	if(cyberPhysical_Incident.Asset.class.isInstance(entity)) {
			String entityStatus = ((cyberPhysical_Incident.Asset)entity).getStatus();
			
			if(entityStatus != null || !entityStatus.isEmpty()) {
				String assetStatus = asset.gets
			}
		}*/
		
		/** matching properities**/
		LinkedList<Integer> properitiesMatched = new LinkedList<Integer>();
		List<environment.Property> assetProperities = asset.getProperty();
		boolean isPropertyMatched = false;
		environment.Property assetProp = null;
		
		for(cyberPhysical_Incident.Property entityProp : entity.getProperties()) {
			for(int i=0;i<assetProperities.size();i++) {
				
				if(properitiesMatched.contains(i)) {
					continue;
				}
				
			assetProp = assetProperities.get(i);
			
			if(entityProp.getName() != null &&
					entityProp.getName().equalsIgnoreCase(assetProp.getName()) &&
						entityProp.getValue() != null &&
						entityProp.getValue().equalsIgnoreCase(assetProp.getValue())) {
				properitiesMatched.add(i);
				isPropertyMatched=  true;
			}
			}
			
			if(!isPropertyMatched) {
				break;
			}
			
			isPropertyMatched = false;
		}
		
		if(entity.getProperties().size() == properitiesMatched.size()) {
			score += propertyMatchScore;
		}
		
		return score;
	}
	
	/**
	 * looks into the different assets and defines the relatonships between them (containment and connectivitiy)
	 * 
	 */
	void generateProperties(){
		int arraySize = assets.length;
		environment.Asset src, des;
		int ind=0;
		for(int i=0;i<assetsProperties.length;i++){
			assetsProperties[i] =0;
		}
		
		for (int i=0;i<arraySize;i++){
			for(int j=1;j<=GeneticMain.neighbourhood;j++){
				if (i+j < arraySize){//make sure that it does not go out of index
					src = GeneticMain.concreteAssets.get(assets[i]);
					des = GeneticMain.concreteAssets.get(assets[i+j]);
					
					//isSame
					if(src.getName().equals(des.getName())){
						assetsProperties[ind] = 1;
					}
					
					//hasConnectivity
					for(environment.Connection con: src.getConnections()) {
						if (con.getAsset1().getName().equals(des.getName())
								|| con.getAsset2().getName().equals(des.getName())){
							assetsProperties[ind+1] = 1;//con.getType().ordinal();
							break;
						}
					}
					
					//isParent
					environment.Asset parentAsset = null;
					
					if(environment.DigitalAsset.class.isInstance(src)) {
						parentAsset = ((environment.DigitalAsset)src).getParentAsset();
					} else if(environment.PhysicalAsset.class.isInstance(src)) {
						parentAsset = ((environment.PhysicalAsset)src).getParentAsset();
					} 
					
					if(parentAsset != null && parentAsset.getName().equals(des.getName())) {
						assetsProperties[ind+2] = 1;
					}
					
					//isChild
					if(src.getContainedAssets().contains(des)){
						assetsProperties[ind+3] = 1;
					}
					
					ind+=GeneticMain.rulesNum;
				}
			}
		}

	}

	void printProp(){
		System.out.print("asset prop: ");
		for(int i=0;i<assetsProperties.length;i++){
			System.out.print(assetsProperties[i]+"-");
			
		}
		System.out.println("  " +toString());
	}
	
	//evaluate properties in comparison to the general rules generated in the geneticMain class.
	double evaluateProperties(){
		double score = 0;
		
		for(int i=0;i<assetsProperties.length;i++) {
			
			//if they both match
			if (assetsProperties[i] ==1 && GeneticMain.rules[i] == 1){
				score = score + score*matchRelationScoreAddedPercentage;
				
			}  else //more deducted if something is missing from the system asset and exists in the incident entity
				if (assetsProperties[i] ==0 && GeneticMain.rules[i] == 1){
				score = score - score*misMatchRelationScoreDeductedPercentage;
			} 
				/*else //in case general asset has this property but not the concrete then the score should be deducted
				if (assetsProperties[i] ==1 && GeneticMain.rules[i] == 0){
				score -=1;
			} */
		}
		
		return score;
	}
	
	//returns 1 if this individual has better qualities compared to the general than the other individual (ind)
		//returns 2 if it is the other way around, and returns 0 if they are equal
		int compareAssetsProperties(int index, Individual ind) {
			
			double score1= 0,score2=0;
			int in =index*GeneticMain.rulesNum*GeneticMain.neighbourhood;
			int end = in+(GeneticMain.rulesNum*GeneticMain.neighbourhood)-1;
			
			for(;in<assetsProperties.length && in<=end;in++){
					
				if (assetsProperties[in] + GeneticMain.rules[in] == 2){
					score1 +=2;
					
				} else //in case general asset has this property but not the concrete (and vice-versa) then the score should be deducted
					if (assetsProperties[in] + GeneticMain.rules[in] == 1){
					score1 -=2;
				}
				
				if (ind.assetsProperties[in] + GeneticMain.rules[in] == 2){
					score2 +=2;
					
				} else //in case general asset has this property but not the concrete (and vice-versa) then the score should be deducted
					if (ind.assetsProperties[in] + GeneticMain.rules[in] == 1){
					score2 -=2;
				}
				
				//if the asset in the array is ended
				
			}
			
			if (score1 > score2) {
				return 1;
				
			} else //if the 2nd individual has a better score 
				if (score2 > score1){
				return 2;
			}
			
			return 0;
			
		}
		
	//crossover done through swapping better genes from this ind to the new child or the other ind in case the first is not suitable
	public Individual crossOver(Individual ind){
		
		Individual child = new Individual();
		
		/*for (int i=0;i<assets.length;i++){
			
			if (compareAssetsProperties(i, ind) < 2){
			child.assets[i] = assets[i];
			} else {
				child.assets[i] = ind.assets[i];
			}
			
		}*/
		
		//first half from this individual
		int index =0;
		for (;index<assets.length/2;index++){			
			child.assets[index] = assets[index];
		}
		//second half from the other individual
		for (;index<assets.length;index++){			
			child.assets[index] = ind.assets[index];
		}
		
		return child;
	}
	
	public void mutate(){
		Random r = new Random();
		
		int index = r.nextInt(assets.length);
		assets[index] = r.nextInt(maxConcreteAssetNum);
		
	}
	
	public String toString() {
		String value="";
		int i = 0;
		value +=assets[i];
		i++;
		for(;i<assets.length;i++){
			value += "-"+assets[i];
		}
		return value;
	}
	
	//returns 1 if match 0 if not
	@Override
	public boolean equals(Object o) {
		
		if (o == null) return false;
	    if (o == this) return true;
	    if (!(o instanceof Individual))return false;
		
		for(int i=0;i<assets.length;i++) {
			if(assets[i] != ((Individual)(o)).assets[i]){
				return false;
			}
		}
		
		return true;
	}
}
