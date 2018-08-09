package i.e.lero.spare.pattern_instantiation;


import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import javax.xml.xquery.XQException;

import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.IncidentEntity;
import cyberPhysical_Incident.Knowledge;
import environment.EnvironmentDiagram;
import i.e.lero.spare.pattern_instantiation.BigraphAnalyser.BigraphMatcher;
import ie.lero.spare.franalyser.utility.IncidentModelHandler;
import ie.lero.spare.franalyser.utility.SystemModelHandler;
import ie.lero.spare.franalyser.utility.XqueryExecuter;
import it.uniud.mads.jlibbig.core.std.Bigraph;

public class Mapper {

	private ForkJoinPool mainPool;
	private String xqueryFilePath;
	private List<environment.Asset> systemAssets;
	private LinkedList<IncidentEntity> incidentEntities;
	private int incidentEntitiesThreshold = 10;
	private int systemAssetsThreshold = 100;
	
	public Mapper() {

		mainPool = new ForkJoinPool();
	}

	public Mapper(String xqueryFilePath) {
		this();
		this.xqueryFilePath = xqueryFilePath;
	}

	public AssetMap findMatches() {
		String res = null;
		
		try {
			res = XqueryExecuter.executeQueryFromFile(xqueryFilePath);
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(res == null) {
			return null;
		}
		
		//removes the tags <>
		res = res.substring(res.indexOf('>')+1, res.lastIndexOf('<'));
		
		String[] incidentAssetsandMatches = res.split(" ");
		String[] incidentAssetNames = new String[incidentAssetsandMatches.length];
		String[][] matches = new String[incidentAssetsandMatches.length][];
		String[] tmp;
		String[] tmp2;
		AssetMap map = new AssetMap();
		int i = 0;

		for (i = 0; i < incidentAssetsandMatches.length; i++) {
			tmp = incidentAssetsandMatches[i].split(":");
			if (tmp.length > 1) //if there are matches for the incident asset
				tmp2 = tmp[1].split("-"); //tmp[1] contains the space asset matched to an incident asset
			else {
				tmp2 = new String[1]; //if there are no matches create one empty string [""] 
				tmp2[0] = null;
			}

			incidentAssetNames[i] = tmp[0]; //tmp[0] contains the incident asset name
			matches[i] = tmp2;

		}

		map.setIncidentEntityNames(incidentAssetNames);
		map.setSpaceAssetMatches(matches);

		return map;
	}


	public AssetMap findMatches2(String incidentPatternFile, String systemModelFile) {
		
		//read models
		//incident pattern
		IncidentDiagram incidentPattern = IncidentModelHandler.loadIncidentFromFile(incidentPatternFile);
		EnvironmentDiagram systemModel = SystemModelHandler.loadSystemFromFile(systemModelFile);
		
		systemAssets = systemModel.getAsset();
		incidentEntities = new LinkedList<IncidentEntity>();
		
		incidentEntities.addAll(incidentPattern.getAsset());
		incidentEntities.addAll(incidentPattern.getActor());
		incidentEntities.addAll(incidentPattern.getResource());
		AssetMap map = new AssetMap();
		
		HashMap<String, List<String>> result = mainPool.invoke(new EntityMatcher(0, incidentEntities.size(), incidentEntities));
		
		map.setMatchedSystemAssets(result);
		
		return map;
	}
	
	public String getXquery() {
		return xqueryFilePath;
	}

	public void setXquery(String xquery) {
		this.xqueryFilePath = xquery;
	}

	class EntityMatcher extends RecursiveTask<HashMap<String, List<String>>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private List<IncidentEntity> incidentEntities;
		private HashMap<String, List<String>> matchedSystemAssets;

		public EntityMatcher(int startIndex, int endIndex, List<IncidentEntity> incidentEntities) {
			this.indexStart = startIndex;
			this.indexEnd = endIndex;
			matchedSystemAssets = new HashMap<String, List<String>>();
			this.incidentEntities = incidentEntities;
		}

		@Override
		protected HashMap<String, List<String>> compute() {
			
			if((indexEnd-indexStart) > incidentEntitiesThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks())
						.stream()
						.map(new Function<EntityMatcher, HashMap<String, List<String>>>() {

							@Override
							public HashMap<String, List<String>> apply(EntityMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedSystemAssets;
							}
							
						}).reduce(matchedSystemAssets, new BinaryOperator<HashMap<String, List<String>>>() {

							@Override
							public  HashMap<String, List<String>> apply(HashMap<String, List<String>> arg0, 
									HashMap<String, List<String>> arg1) {
								// TODO Auto-generated method stub
								arg0.putAll(arg1);
								return arg0;
							}
							
						});
						
			} else {
				
				//do the matching by slicing Assets to match to into different pieces
				for(int i =indexStart; i<indexEnd;i++) {
					IncidentEntity entity = incidentEntities.get(i);
					List<String> result = mainPool.invoke(new EntityAssetMatcher(0, systemAssets.size(), systemAssets, entity));
					matchedSystemAssets.put(entity.getName(), result);
				}
				
				return matchedSystemAssets;
			}
			
		}
		
		private Collection<EntityMatcher> createSubTasks() {
			List<EntityMatcher> dividedTasks = new LinkedList<EntityMatcher>();
			
			int mid = (indexStart+indexEnd)/2;
			//int startInd = indexEnd - endInd1;
			
			dividedTasks.add(new EntityMatcher(indexStart, mid, incidentEntities));
			dividedTasks.add(new EntityMatcher(mid, indexEnd, incidentEntities));
			
			return dividedTasks;
		}
	}
	
	class EntityAssetMatcher extends RecursiveTask<List<String>> {

		private static final long serialVersionUID = 1L;
		private int indexStart;
		private int indexEnd;
		private List<String> matchedSystemAssets;
		private List<environment.Asset> systemAssets;
		private IncidentEntity incidentEntity;
		
		public EntityAssetMatcher(int startIndex, int endIndex, List<environment.Asset> systemAssets, IncidentEntity entity) {
			this.indexStart = startIndex;
			this.indexEnd = endIndex;
			matchedSystemAssets = new LinkedList<String>();
			this.systemAssets = systemAssets;
			incidentEntity = entity;
			
		}

		@Override
		protected List<String> compute() {
			
			if((indexEnd-indexStart) > systemAssetsThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks())
						.stream()
						.map(new Function<EntityAssetMatcher, List<String>>() {

							@Override
							public List<String> apply(EntityAssetMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedSystemAssets;
							}
							
						}).reduce(matchedSystemAssets, new BinaryOperator<List<String>>() {

							@Override
							public   List<String> apply(List<String> arg0, 
									List<String> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}
							
						});
						
			} else {
				
				//match according to criteria
				environment.Asset tmpAst = null;
				
				for(int i =indexStart; i<indexEnd;i++) {
					tmpAst = systemAssets.get(i);
					if(isMatch(tmpAst, incidentEntity)) {
						matchedSystemAssets.add(tmpAst.getName());
					}
				}
				
				return matchedSystemAssets;
			}
			
		}
		
		private Collection<EntityAssetMatcher> createSubTasks() {
			List<EntityAssetMatcher> dividedTasks = new LinkedList<EntityAssetMatcher>();
			
			int mid = (indexStart+indexEnd)/2;
			//int startInd = indexEnd - endInd1;
			
			dividedTasks.add(new EntityAssetMatcher(indexStart, mid, systemAssets, incidentEntity));
			dividedTasks.add(new EntityAssetMatcher(mid, indexEnd, systemAssets, incidentEntity));
			
			return dividedTasks;
		}
		
		private boolean isMatch(environment.Asset asset, IncidentEntity entity) {
			
			/**
			 * matching criteria
			 * 1- type of entity which is taken from the tag type in the model. This compared to the type 
			 * of an asset as a class (e.g., entity has type "Room" returns all assets that are instances of
			 * Room class or its subclasses 
			 * 2- Number & type of connections. All incident entity connections should be subset of the
			 * connections of an asset if knowledge is partial or exact if knowledge is exact. Type can be 
			 * of the same class or subclass
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
				return false;
			}
			
			//if the current asset object is not of the same class or subclass of the potential class
			//then return false (type mismatch)
			if(!potentialClass.isInstance(asset)) {
				return false;
			}
			
			}
			
			/** matching connections **/
			//if knowledge is exact then both should have the same number of connections
			//otherwise there's no match
			if(entity.getConnectionsKnowledge().compareTo(Knowledge.EXACT) == 0) {
				if(entity.getConnections().size() != asset.getConnections().size()) {
				
					return false;
				}
			}
			
			//if the incident entity has more connections then it cannot be subset of the asset connections
			//thus there's no match
			System.out.println("entity: "+entity.getName()+" cons: "+entity.getConnections().size());
			if(entity.getConnections().size() > asset.getConnections().size()) {
				return false;
			}
			
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
					return false;
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
					return false;
				}
				
				isConnectionMatched = false;
				
			}
			
			return true;
		}
	}
}
