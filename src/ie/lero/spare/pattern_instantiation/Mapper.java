package ie.lero.spare.pattern_instantiation;

import java.io.FileNotFoundException;
import java.util.Arrays;
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
import cyberPhysical_Incident.Location;
import cyberPhysical_Incident.Mobility;
import environment.Asset;
import environment.ComputingDevice;
import environment.DigitalAsset;
import environment.DigitalNetwork;
import environment.EnvironmentDiagram;
import environment.PhysicalAsset;
import environment.PhysicalStructure;
import ie.lero.spare.franalyser.utility.IncidentModelHandler;
import ie.lero.spare.franalyser.utility.ModelsHandler;
import ie.lero.spare.franalyser.utility.SystemModelHandler;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

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

	/*
	 * public Mapper(String xqueryFilePath) { this(); this.xqueryFilePath =
	 * xqueryFilePath; }
	 */
	public AssetMap findMatchesUsingXquery(String xqueryFilePath) {

		String res = null;
		HashMap<String, List<String>> result = null;

		try {
			res = XqueryExecuter.executeQueryFromFile(xqueryFilePath);
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (res == null) {
			return null;
		}

		// removes the tags <>
		res = res.substring(res.indexOf('>') + 1, res.lastIndexOf('<'));

		result = new HashMap<String, List<String>>();

		String[] incidentAssetsandMatches = res.split(" ");
		String[] incidentAssetNames = new String[incidentAssetsandMatches.length];
		String[][] matches = new String[incidentAssetsandMatches.length][];
		String[] tmp;
		String[] tmp2;
		AssetMap map = new AssetMap();
		int i = 0;

		for (i = 0; i < incidentAssetsandMatches.length; i++) {
			tmp = incidentAssetsandMatches[i].split(":");
			if (tmp.length > 1) // if there are matches for the incident asset
				tmp2 = tmp[1].split("-"); // tmp[1] contains the space asset
											// matched to an incident asset
			else {
				tmp2 = new String[1]; // if there are no matches create one
										// empty string [""]
				tmp2[0] = null;
			}

			incidentAssetNames[i] = tmp[0]; // tmp[0] contains the incident
											// asset name
			matches[i] = tmp2;

			result.put(incidentAssetNames[i], Arrays.asList(matches[i]));
		}

		map.setMatchedSystemAssets(result);

		return map;
	}

	/**
	 * Finds All system assets that match each incident entity. The matching is
	 * done based on the criteria: 1-Type of entity which is taken from the tag
	 * type in the model. This compared to the type of an asset as a class
	 * (e.g., entity has type "Room" returns all assets that are instances of
	 * Room class or its subclasses 2-Type of parent (container of asset/entity)
	 * 3-Number & type of contained assets. 4-Number & type of connections. All
	 * incident entity connections should be subset of the connections of an
	 * asset if knowledge is partial or exact if knowledge is exact. Type can be
	 * of the same class or subclass. 5-Properities if found in the entity
	 * 
	 * @param incidentPatternFile
	 *            incident pattern model file path
	 * @param systemModelFile
	 *            system model file path
	 * @return
	 */
	public AssetMap findMatches() {

		// read models
		// incident pattern
		IncidentDiagram incidentPattern = ModelsHandler.getCurrentIncidentModel();
		EnvironmentDiagram systemModel = ModelsHandler.getCurrentSystemModel();

		if (systemModel == null || incidentPattern == null) {
			return null;
		}

		systemAssets = systemModel.getAsset();
		incidentEntities = new LinkedList<IncidentEntity>();

		incidentEntities.addAll(incidentPattern.getEntity());
		// incidentEntities.addAll(incidentPattern.getAsset());
		// incidentEntities.addAll(incidentPattern.getActor());
		// incidentEntities.addAll(incidentPattern.getResource());

		AssetMap map = new AssetMap();

		HashMap<String, List<String>> result = mainPool
				.invoke(new EntityMatcher(0, incidentEntities.size(), incidentEntities));

		mainPool.shutdown();

		map.setMatchedSystemAssets(result);

		return map;
	}

	/*
	 * public String getXquery() { return xqueryFilePath; }
	 * 
	 * public void setXquery(String xquery) { this.xqueryFilePath = xquery; }
	 */
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

			if ((indexEnd - indexStart) > incidentEntitiesThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<EntityMatcher, HashMap<String, List<String>>>() {

							@Override
							public HashMap<String, List<String>> apply(EntityMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedSystemAssets;
							}

						}).reduce(matchedSystemAssets, new BinaryOperator<HashMap<String, List<String>>>() {

							@Override
							public HashMap<String, List<String>> apply(HashMap<String, List<String>> arg0,
									HashMap<String, List<String>> arg1) {
								// TODO Auto-generated method stub
								arg0.putAll(arg1);
								return arg0;
							}

						});

			} else {

				// do the matching by slicing Assets to match to into different
				// pieces
				for (int i = indexStart; i < indexEnd; i++) {
					IncidentEntity entity = incidentEntities.get(i);
					List<String> result = mainPool
							.invoke(new EntityAssetMatcher(0, systemAssets.size(), systemAssets, entity));
					matchedSystemAssets.put(entity.getName(), result);
				}

				return matchedSystemAssets;
			}

		}

		private Collection<EntityMatcher> createSubTasks() {
			List<EntityMatcher> dividedTasks = new LinkedList<EntityMatcher>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

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

		public EntityAssetMatcher(int startIndex, int endIndex, List<environment.Asset> systemAssets,
				IncidentEntity entity) {
			this.indexStart = startIndex;
			this.indexEnd = endIndex;
			matchedSystemAssets = new LinkedList<String>();
			this.systemAssets = systemAssets;
			incidentEntity = entity;

		}

		@Override
		protected List<String> compute() {

			if ((indexEnd - indexStart) > systemAssetsThreshold) {
				return ForkJoinTask.invokeAll(createSubTasks()).stream()
						.map(new Function<EntityAssetMatcher, List<String>>() {

							@Override
							public List<String> apply(EntityAssetMatcher arg0) {
								// TODO Auto-generated method stub
								return arg0.matchedSystemAssets;
							}

						}).reduce(matchedSystemAssets, new BinaryOperator<List<String>>() {

							@Override
							public List<String> apply(List<String> arg0, List<String> arg1) {
								// TODO Auto-generated method stub
								arg0.addAll(arg1);
								return arg0;
							}

						});

			} else {

				// match according to criteria
				environment.Asset tmpAst = null;

				for (int i = indexStart; i < indexEnd; i++) {
					tmpAst = systemAssets.get(i);
					if (isMatch(tmpAst, incidentEntity)) {
						matchedSystemAssets.add(tmpAst.getName());
					}
				}

				return matchedSystemAssets;
			}

		}

		private Collection<EntityAssetMatcher> createSubTasks() {
			List<EntityAssetMatcher> dividedTasks = new LinkedList<EntityAssetMatcher>();

			int mid = (indexStart + indexEnd) / 2;
			// int startInd = indexEnd - endInd1;

			dividedTasks.add(new EntityAssetMatcher(indexStart, mid, systemAssets, incidentEntity));
			dividedTasks.add(new EntityAssetMatcher(mid, indexEnd, systemAssets, incidentEntity));

			return dividedTasks;
		}

		private boolean isMatch(environment.Asset asset, IncidentEntity entity) {

			/**
			 * matching criteria 1-Type of entity which is taken from the tag
			 * type in the model. This compared to the type of an asset as a
			 * class (e.g., entity has type "Room" returns all assets that are
			 * instances of Room class or its subclasses 2-Type of parent
			 * (container of asset/entity) 3-Number & type of contained assets.
			 * 4-Number & type of connections. All incident entity connections
			 * should be subset of the connections of an asset if knowledge is
			 * partial or exact if knowledge is exact. Type can be of the same
			 * class or subclass. 5-Status if found in the entity and entity is
			 * an Asset (not implemented as not all assets hold status)
			 * 6-Properities if found in the entity
			 */

			/** matching Type **/
			Class<?> potentialClass = null;

			if (entity.getType() != null) {
				String typeName = entity.getType().getName();
				try {
					String potentialClassName = "environment.impl." + typeName;

					if (!typeName.endsWith("Impl")) {
						potentialClassName += "Impl";
					}

					potentialClass = Class.forName(potentialClassName);

				} catch (ClassNotFoundException e) {
					// type mismatch i.e. there is no type available in the
					// system model
					// currently return false
					return false;
				}

				// if the current asset object is not of the same class or
				// subclass of the potential class
				// then return false (type mismatch)
				if (!potentialClass.isInstance(asset)) {
					return false;
				}

			}

			/** matching Parent type **/
			IncidentEntity parent = (IncidentEntity) entity.getParentEntity();

			if (parent != null) {

				if (parent.getType() != null) {
					String typeName = parent.getType().getName();
					try {
						String potentialClassName = "environment.impl." + typeName;

						if (!typeName.endsWith("Impl")) {
							potentialClassName += "Impl";
						}

						potentialClass = Class.forName(potentialClassName);

					} catch (ClassNotFoundException e) {
						// type mismatch i.e. there is no type available in the
						// system model
						// currently return false (maybe loosened a little like
						// ignore)
						return false;
					}

					// if the current asset object is not of the same class or
					// subclass of the potential class
					// then return false (type mismatch)
					environment.Asset parentAsset = null;

					if (environment.DigitalAsset.class.isInstance(asset)) {
						parentAsset = ((environment.DigitalAsset) asset).getParentAsset();
					} else if (environment.PhysicalAsset.class.isInstance(asset)) {
						parentAsset = ((environment.PhysicalAsset) asset).getParentAsset();
					}

					// if the asset has no parent but the incident entity
					// has
					// then return false (no match)
					if (parentAsset == null) {
						return false;
					}

					// if the entity is fixed at that location, then both entity
					// and asset should have the same class type
					if (entity.getMobility() == Mobility.FIXED) {

						if (!potentialClass.isInstance(parentAsset)) {
							return false;
						}

						// else if entity is movable (i.e. can change location
					} else if (entity.getMobility() == Mobility.MOVABLE) {

						// if the entity parent is of type physical structure,
						// then
						// asset parent should be of type physical structure
						if (PhysicalStructure.class.isInstance(parent)) {
							if (!PhysicalStructure.class.isInstance(parentAsset)) {
								return false;
							}
						}

						// if the entity parent is of type computing device,
						// then
						// asset parent should be of type computing device
						if (ComputingDevice.class.isInstance(parent)) {
							if (!ComputingDevice.class.isInstance(parentAsset)) {
								return false;
							}
						}

						// if the entity parent is of type digital network, then
						// asset parent should be of type digital network
						if (DigitalNetwork.class.isInstance(parent)) {
							if (!DigitalNetwork.class.isInstance(parentAsset)) {
								return false;
							}
						}

						// general case (other cases could be added as the ones
						// before)
						if (DigitalAsset.class.isInstance(parent)) {
							if (!DigitalAsset.class.isInstance(parentAsset)) {
								return false;
							}
						}
						// general case (other cases could be added as the ones
						// before)
						if (PhysicalAsset.class.isInstance(parent)) {
							if (!PhysicalAsset.class.isInstance(parentAsset)) {
								return false;
							}
						}

					} else if (entity.getMobility() == Mobility.UNKNOWN) {
						// if mobility is unknown, then parents should match on
						// the level of physical or digital
						// general case (other cases could be added as the ones
						// before)
						if (DigitalAsset.class.isInstance(parent)) {
							if (!DigitalAsset.class.isInstance(parentAsset)) {
								return false;
							}
						}
						// general case (other cases could be added as the ones
						// before)
						if (PhysicalAsset.class.isInstance(parent)) {
							if (!PhysicalAsset.class.isInstance(parentAsset)) {
								return false;
							}
						}
					}

				}
			}

			/** matching contained assets (number & type) **/
			// if knowledge is exact then both should have the same number of
			// connections
			// otherwise there's no match
			if (entity.getContainedAssetsKnowledge().compareTo(Knowledge.EXACT) == 0) {
				if (entity.getContainedEntities().size() != entity.getContainedEntities().size()) {

					return false;
				}
			}

			// if the incident entity has more connections then it cannot be
			// subset of the asset connections
			// thus there's no match
			if (entity.getContainedEntities().size() > entity.getContainedEntities().size()) {
				return false;
			}

			// compare contained assets type (simialr to asset type)
			LinkedList<Integer> matchedcontainedAssets = new LinkedList<Integer>();

			for (Location ent : entity.getContainedEntities()) {

				IncidentEntity containedEntity = (IncidentEntity) ent;

				if (containedEntity.getType() == null) {
					continue;// ignored
				}

				// if contained entity mobility is movable then it is also
				// ignored
				// too loose
				// if(containedEntity.getMobility() == Mobility.MOVABLE) {
				// continue;
				// }

				String typeName = containedEntity.getType().getName();

				try {
					String potentialClassName = "environment.impl." + typeName;

					if (!typeName.endsWith("Impl")) {
						potentialClassName += "Impl";
					}

					potentialClass = Class.forName(potentialClassName);

				} catch (ClassNotFoundException e) {
					// type mismatch i.e. there is no type available in the
					// system model
					// currently returns false
					return false;
				}

				// if the current asset connection object is not of the same
				// class or subclass of the potential class
				// then return false (connection type mismatch)
				boolean iscontainedEntityMatched = false;

				List<environment.Asset> containedAssets = (List<Asset>) asset.getContainedAssets();

				environment.Asset containedAsset = null;

				for (int i = 0; i < containedAssets.size(); i++) {

					if (matchedcontainedAssets.contains(i)) {
						continue;
					}

					containedAsset = containedAssets.get(i);

					if (potentialClass.isInstance(containedAsset)) {
						matchedcontainedAssets.add(i);
						iscontainedEntityMatched = true;
						break;
					}
				}

				// if the contained entity is fixed and none of the contained
				// assets match the incident contained entity, then return false
				if (containedEntity.getMobility() == Mobility.FIXED && !iscontainedEntityMatched) {
					return false;
				}

				iscontainedEntityMatched = false;

			}

			/** matching connections (number & type) **/
			// if knowledge is exact then both should have the same number of
			// connections
			// otherwise there's no match
			if (entity.getConnectionsKnowledge().compareTo(Knowledge.EXACT) == 0) {
				if (entity.getConnections().size() != asset.getConnections().size()) {

					return false;
				}
			}

			// if the incident entity has more connections then it cannot be
			// subset of the asset connections
			// thus there's no match
			if (entity.getConnections().size() > asset.getConnections().size()) {
				return false;
			}

			// compare connection type (simialr to asset type)
			LinkedList<Integer> matchedAssetCons = new LinkedList<Integer>();

			for (cyberPhysical_Incident.Connection entityCon : entity.getConnections()) {

				if (entityCon.getType() == null) {
					continue;// ignored
				}

				String typeName = entityCon.getType().getName();

				try {
					String potentialClassName = "environment.impl." + typeName;

					if (!typeName.endsWith("Impl")) {
						potentialClassName += "Impl";
					}

					potentialClass = Class.forName(potentialClassName);

				} catch (ClassNotFoundException e) {
					// type mismatch i.e. there is no type available in the
					// system model
					// currently returns false
					return false;
				}

				// if the current asset connection object is not of the same
				// class or subclass of the potential class
				// then return false (connection type mismatch)
				boolean isConnectionMatched = false;

				List<environment.Connection> assetCons = asset.getConnections();
				environment.Connection assetCon = null;

				for (int i = 0; i < assetCons.size(); i++) {

					if (matchedAssetCons.contains(i)) {
						continue;
					}

					assetCon = assetCons.get(i);

					if (potentialClass.isInstance(assetCon)) {
						matchedAssetCons.add(i);
						isConnectionMatched = true;
						break;
					}
				}

				// if none of the asset connections match the incident
				// connection then it is a mismatch
				if (!isConnectionMatched) {
					return false;
				}

				isConnectionMatched = false;

			}

			/** matching status **/
			/*
			 * if(cyberPhysical_Incident.Asset.class.isInstance(entity)) {
			 * String entityStatus =
			 * ((cyberPhysical_Incident.Asset)entity).getStatus();
			 * 
			 * if(entityStatus != null || !entityStatus.isEmpty()) { String
			 * assetStatus = asset.gets } }
			 */

			/** matching properities **/
			LinkedList<Integer> properitiesMatched = new LinkedList<Integer>();
			List<environment.Property> assetProperities = asset.getProperty();
			boolean isPropertyMatched = false;
			environment.Property assetProp = null;

			for (cyberPhysical_Incident.Property entityProp : entity.getProperties()) {
				for (int i = 0; i < assetProperities.size(); i++) {

					if (properitiesMatched.contains(i)) {
						continue;
					}

					assetProp = assetProperities.get(i);

					if (entityProp.getName() != null && entityProp.getName().equalsIgnoreCase(assetProp.getName())
							&& entityProp.getValue() != null
							&& entityProp.getValue().equalsIgnoreCase(assetProp.getValue())) {
						properitiesMatched.add(i);
						isPropertyMatched = true;
					}
				}

				if (!isPropertyMatched) {
					return false;
				}

				isPropertyMatched = false;
			}

			return true;
		}
	}
}
