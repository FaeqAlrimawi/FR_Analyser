package i.e.lero.spare.pattern_instantiation;


import java.util.Random;

public class Individual {

	//DNA representation, each position in the array holds the index to an asset in the assets array
	int [] assets;
	
	//assets properties values corresponding to the rules of general assets
	int [] assetsProperties;
	int maxConcreteAssetNum;
	
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
			score+=compareGenConAssets(i,assets[i]);
		}
		generateProperties();

		score +=evaluateProperties();

		//really bad individual
		if(score<=0) {
			score = 0.0001;
		}
		
		return score*score; //magnify small differences between solutions
		
	}
	
	double compareGenConAssets(int indexGen, int indexCon) {
		double score = 0;
		cyberPhysical_Incident.Entity genEntity;
		environment.Asset conAsset;
		
		//load assets
		genEntity = GeneticMain.generalEntities.get(indexGen);
		conAsset = GeneticMain.concreteAssets.get(indexCon);
		
		//comparison based on criteria should ha[[en here
		
		/*//match type
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
		}*/
		
		return score;
	}
	
	/**
	 * looks into the different assets and defines the relatonships between them (containment and connectivitiy)
	 * 
	 */
	void generateProperties(){
	/*	int arraySize = assets.length;
		ConcreteAsset src, des;
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
					if(src.getId() == des.getId()){
						assetsProperties[ind] = 1;
					}
					//hasConnectivity
					for(Connection con: src.getConnections()) {
						if (con.getAssetDes() == des){
							assetsProperties[ind+1] = 1;//con.getType().ordinal();
							break;
						}
					}
					//isParent
					if(src.getParent() == des) {
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
*/
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
			//if they both match (currently if it is not 0 then it is 1
			if (assetsProperties[i] ==1 && GeneticMain.rules[i] == 1){
				score +=3;
				
			}  else //more deducted if something is missing from the concrete
				if (assetsProperties[i] ==0 && GeneticMain.rules[i] == 1){
				score -=2;
			} else //in case general asset has this property but not the concrete then the score should be deducted
				if (assetsProperties[i] ==1 && GeneticMain.rules[i] == 0){
				score -=1;
			} 
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
