package ie.lero.spare.pattern_extraction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;

import cyberPhysical_Incident.Activity;
import cyberPhysical_Incident.ActivityInitiator;
import cyberPhysical_Incident.ActivityPattern;
import cyberPhysical_Incident.ActivityType;
import cyberPhysical_Incident.Actor;
import cyberPhysical_Incident.ActorLevel;
import cyberPhysical_Incident.ActorRole;
import cyberPhysical_Incident.Asset;
import cyberPhysical_Incident.Behaviour;
import cyberPhysical_Incident.BigraphExpression;
import cyberPhysical_Incident.Connection;
import cyberPhysical_Incident.Entity;
import cyberPhysical_Incident.IncidentDiagram;
import cyberPhysical_Incident.IncidentEntity;
import cyberPhysical_Incident.Knowledge;
import cyberPhysical_Incident.Location;
import cyberPhysical_Incident.Postcondition;
import cyberPhysical_Incident.Precondition;
import cyberPhysical_Incident.Resource;
import cyberPhysical_Incident.Scene;
import cyberPhysical_Incident.Vulnerability;
import environment.EnvironmentDiagram;
import ie.lero.spare.franalyser.utility.ModelsHandler;
import it.uniud.mads.jlibbig.core.std.Bigraph;
import it.uniud.mads.jlibbig.core.std.Matcher;

public class IncidentPatternExtractor {

	protected IncidentDiagram incidentModel;
	protected EnvironmentDiagram systemModel;
	protected static int MaxNumberOfActions = 3;
	protected Map<String, String> entityMap = new HashMap<String, String>();

	public IncidentPatternExtractor() {

	}

	public IncidentDiagram extract(IncidentDiagram incidentModel) {

		String systemFileName = "D:/runtime-EclipseApplication/Scenarios/Scenario1/Research_centre.cps";

		if (incidentModel == null) {
			return null;
		}

		this.incidentModel = incidentModel;

		IncidentDiagram abstractedModel = null;

		// create a copy
		/*
		 * String tmpFileName = "tmpModel.cpi";
		 * IncidentModelHandler.SaveIncidentToFile(incidentModel, tmpFileName);
		 * abstractedModel =
		 * IncidentModelHandler.loadIncidentFromFile(tmpFileName);
		 */
		/*
		 * //remove tmp (i.e. the copy) file File tmpFile = new
		 * File(tmpFileName);
		 * 
		 * if(tmpFile.exists()) { tmpFile.delete(); }
		 */

		// System.out.println("test:
		// "+abstractedModel.getInitialActivity().getConnectionChangesBetweenEntities("offender",
		// "hallway"));
		// abstractedModel.abstractActivities();

		// abstract entities

		/*
		 * for(Asset ast : systemModel.getAsset()) { Asset tmp =
		 * ast.abstractType();
		 * 
		 * if(tmp != null) { System.out.println("o:"+
		 * ast.getClass().getSimpleName() +"  a: " +
		 * tmp.getClass().getSimpleName()); }else { System.out.println("o:"+
		 * ast.getClass().getSimpleName() +"  abstracted asset is NULL"); }
		 * 
		 * }
		 */
		// Random rand = new Random();
		//
		// int tries = 50;

		/*
		 * for(int i = 0;i<tries;i++) { Asset original =
		 * systemModel.getAsset().get(rand.nextInt(systemModel.getAsset().size()
		 * )); Asset abstracted = original.abstractAsset();
		 * 
		 * System.out.println("Original Asset: "+original +
		 * "\nContainedAssets ["+original.getContainedAssets().size()+"]: "
		 * +original.getContainedAssets() +
		 * "\nConnections ["+original.getConnections().size()+"]: "+original.
		 * getConnections() );
		 * 
		 * System.out.println("Abstracted Asset: "+abstracted+
		 * "\nContainedAssets ["+abstracted.getContainedAssets().size()+"]: "
		 * +abstracted.getContainedAssets() +
		 * "\nConnections ["+abstracted.getConnections().size()+"]: "+abstracted
		 * .getConnections()); System.out.println(); }
		 */

		// status: abstraction is done for the basic attributes (type, control,
		// properties) and contained assets
		// next is to implement connections abstraction in assets

		// abstractedModel.abstractActivities();
		// abstractedModel.abstractEntities(systemModel);

		systemModel = ModelsHandler.getSystemModel(systemFileName);

		if (systemModel == null) {
			System.out.println("system model is NULL");
		}

		String collectDataPatternFileName = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/collectDataPattern.cpi";
		String movePhysicallyPatternFileName = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/movePhysicallyPattern.cpi";
		String movePhysicallyPatternFileName2 = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/movePhysicallyPattern2.cpi";
		String connectToNetworkPatternFileName = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/connectToNetworkPattern.cpi";
		String connectToNetworkPatternFileName2 = "D:/runtime-EclipseApplication_design/activityPatterns/activity_patterns/connectToNetworkPattern2.cpi";

		ActivityPattern activityPatternConnectNetwork = ModelsHandler
				.addActivityPattern(connectToNetworkPatternFileName2);
		ActivityPattern activityPatternMove = ModelsHandler.addActivityPattern(movePhysicallyPatternFileName2);

		// create a local signature of the incident model
		incidentModel.createBigraphSignature();

		Map<String, List<String>> result1 = matchActivityPattern(activityPatternConnectNetwork);
		Map<String, List<String>> result2 = matchActivityPattern(activityPatternMove);

		System.out.println(result1);
		System.out.println(result2);
		
		return abstractedModel;
	}

	protected List<int[]> convertPatternResult(Map<String, List<String>> patternMaps) {
	
		List<int[]> result = new LinkedList<int[]>();
		
		for(Entry<String,List<String>> entry  :patternMaps.entrySet()) {
			int [] seq = incidentModel.getActivitySequence(entry.getKey(), entry.getValue().get(0));
			System.out.println(Arrays.toString(seq));		
		}
		
		return result;
		
	}
	
	protected String getMergeRuleName(int rule) {

		switch (rule) {
		case IncidentDiagram.COLLECTDATA_MERGE_RULE:
			return "Collect Data";
		case IncidentDiagram.CONTAINMENT_MERGE_RULE:
			return "Containment";
		case IncidentDiagram.CONNECTIVITY_MERGE_RULE:
			return "Connectivity";
		default:
			return "";
		}
	}

	public IncidentDiagram extract(String fileName) {

		IncidentDiagram model = ModelsHandler.getIncidentModel(fileName);

		return extract(model);
	}

	public static void main(String[] args) {

		IncidentPatternExtractor extractor = new IncidentPatternExtractor();
		String fileName = "D:/runtime-EclipseApplication_design/Examples/Scenario1_B/incidentInstance_steal.cpi";

		extractor.extract(fileName);

	}

	public Map<String, List<String>> matchActivityPattern(ActivityPattern activityPattern) {

		// find all possible matches of the given pattern in the incidentModel
		// sequence
		// assume activity pattern has one activity
		// match precondition to one (initial) activity and then the post to the
		// same or the next

		if (activityPattern == null) {
			return null;
		}

		// EList<Activity> ptrActivities =
		// activityPattern.getAbstractActivity();

		Activity ptrActivity = !activityPattern.getAbstractActivity().isEmpty()
				? activityPattern.getAbstractActivity().get(0) : null;

		if (ptrActivity == null) {
			return null;
		}

		Activity initialActivity = null;
		Activity finalActivity = null;
		Activity currentActivity = null;
		List<String> tmpPreMatchedActivities = new LinkedList<String>();
		List<HashMap<String, String>> entityMaps = new LinkedList<HashMap<String, String>>();
		
		//final result. Key is activity name that satisfies the pattern precondition 
		//and List<String> are the activities that satisfy the postcondition of the pattern
		HashMap<String, List<String>> prePostMappingActivities = new HashMap<String, List<String>>();

		// Activity preMatchedActivity = null;
		// Activity postMatchedActivity = null;

		boolean isptrPreMatched = false;
		boolean isptrPostMatched = false;

		for (Scene scene : incidentModel.getScene()) {
			
			isptrPreMatched = false;
			isptrPostMatched = false;

			initialActivity = scene.getInitialActivity();
			finalActivity = scene.getFinalActivity();
			currentActivity = initialActivity;

			// try to find a match of the pattern precondition (currently finds
			// all matches)
			while (true) {
				entityMap.clear();
				isptrPreMatched = false;
				// System.out.println("-Checking activity [" +
				// currentActivity.getName() + "] for precondition matching");
				// compare precondition of the first activity

				isptrPreMatched = comparePatternIncidentActivities(ptrActivity, currentActivity, true, false);

				// if match found
				if (isptrPreMatched) {

					// System.out.println("-Pattern precondition matched to
					// activity: " + currentActivity.getName());
					entityMaps.add(new HashMap<String, String>(entityMap));

					isptrPostMatched = comparePatternIncidentActivities(ptrActivity, currentActivity, false, true);

					if (isptrPostMatched) {
						
						//add to the result map
						if (!prePostMappingActivities.containsKey(currentActivity.getName())) {
							List<String> matchedActs = new LinkedList<String>();
							matchedActs.add(currentActivity.getName());
							prePostMappingActivities.put(currentActivity.getName(), matchedActs);
						} else {
							prePostMappingActivities.get(currentActivity.getName()).add(currentActivity.getName());
						}

						entityMaps.remove(entityMaps.size() - 1);
					} else {

						// added to the temp to search in next activities
						tmpPreMatchedActivities.add(currentActivity.getName());
					}
				}

				Activity next = !currentActivity.getNextActivities().isEmpty()
						? currentActivity.getNextActivities().get(0) : null;

				// if reached the last activity of the scene
				if (currentActivity.equals(finalActivity)) {
					break;
				}
				// move to check next activity
				currentActivity = next;
			}

			//try to find a match for the pattern postcondition in next activities of the scene
			for (int i = 0; i < tmpPreMatchedActivities.size(); i++) {

				entityMap = entityMaps.get(i);
				isptrPostMatched = false;
				Activity preActivity = scene.getActivity(tmpPreMatchedActivities.get(i));

				currentActivity = preActivity;

				if (currentActivity == null || currentActivity.equals(finalActivity)) {
					continue;
				}

				int cnt = 0;

				// compare the pattern postcondition with the postcondition of next activity
				// until the max number of activities (or actions) is reached or
				// the final activity is reached
				while (cnt < MaxNumberOfActions && !currentActivity.equals(finalActivity)) {

					Activity next = !currentActivity.getNextActivities().isEmpty()
							? currentActivity.getNextActivities().get(0) : null;

					if (next == null) {
						break;
					}

					currentActivity = next;
					
					isptrPostMatched = comparePatternIncidentActivities(ptrActivity, currentActivity, false, true);

					// if there is a match from one of the activities
					if (isptrPostMatched) {
				
						//add to the result map
						if (!prePostMappingActivities.containsKey(preActivity.getName())) {
							List<String> matchedActs = new LinkedList<String>();
							matchedActs.add(currentActivity.getName());
							prePostMappingActivities.put(preActivity.getName(), matchedActs);
						} else {
							prePostMappingActivities.get(preActivity.getName()).add(currentActivity.getName());
						}
						
						//one match is only taken (i.e. first match)
						break;
					}

					cnt++;
				}

			}

		}
		
		return prePostMappingActivities;

	}

	protected boolean comparePatternIncidentActivities(Activity patternActivity, Activity incidentActivity,
			boolean comparePrecondition, boolean comparePostCondition) {

		// compare attributes and references of both activities

		// basic activity attributes:
		// 1-Behaviour Type (e.g., normal, malicious)
		// 2-System action
		// 3-Type (e.g., physical, digital)
		// 4-Duration (to be implemented)
		// 5-Timing (to be implemented)

		// 1- Behaviour Type (e.g., normal, malicious)
		Behaviour incActBehaviour = incidentActivity.getBehaviourType();
		Behaviour ptrActBehaviour = patternActivity.getBehaviourType();

		// if both activities don't have the same behaviour then return false
		if (!ptrActBehaviour.equals(incActBehaviour)) {
			return false;
		}

		// 2-system action
		String incActAction = incidentActivity.getSystemAction();
		String ptrActAction = patternActivity.getSystemAction();

		// if the pattern activity has an action that is not equal to the action
		// in the incident activity then return false
		if (ptrActAction != null && !ptrActAction.equals(incActAction)) {
			return false;
		}

		// 3-Type (e.g., Physical, digital)
		ActivityType incActType = incidentActivity.getType();
		ActivityType ptrActType = patternActivity.getType();

		// if the pattern activity has a type that is different from the
		// incident activity then return false
		if (ptrActType != null && !ptrActType.equals(incActType)) {
			return false;
		}

		// activity references:
		// 1-Initiator
		// 2-Target assets
		// 3-Resources
		// 4-Exploited assets
		// 5-Location
		// 6-Vicitms
		// 7-Condition (Pre or Post)
		// 8-Accomplices (to be implemented)

		// the rule by which certain aspects of an activity should be matched to
		// a pattern is that
		// if it exists in the pattern precondition then it should be matched to
		// an entity of the activity
		// that its precondition is being test otherwise it should be matched to
		// the postcondition

		BigraphExpression ptrBigExp = null;
		BigraphExpression incBigExp = null;

		if (comparePrecondition) {
			Precondition ptrPre = patternActivity.getPrecondition();
			ptrBigExp = ptrPre != null ? (BigraphExpression) ptrPre.getExpression() : null;

			Precondition incPre = incidentActivity.getPrecondition();
			incBigExp = incPre != null ? (BigraphExpression) incPre.getExpression() : null;

		} else if (comparePostCondition) {
			Postcondition ptrPost = patternActivity.getPostcondition();
			ptrBigExp = ptrPost != null ? (BigraphExpression) ptrPost.getExpression() : null;

			Postcondition incPost = incidentActivity.getPostcondition();
			incBigExp = incPost != null ? (BigraphExpression) incPost.getExpression() : null;

		}

		if (ptrBigExp == null) {
			return false;
		}

		boolean canBeApplied = false;

		// 1-Initiator: compare initiator attributes found in the pattern
		// activity to that in the incident activity
		ActivityInitiator ptrInitiator = patternActivity.getInitiator();
		String ptrInitiatorName = ptrInitiator != null ? ((IncidentEntity) ptrInitiator).getName() : null;

		if (ptrBigExp.hasEntity(ptrInitiatorName)) {
			ActivityInitiator incInitiator = incidentActivity.getInitiator();
			canBeApplied = compareInitiators(ptrInitiator, incInitiator);

			if (!canBeApplied) {
				return false;
			}

			if (ptrInitiator != null) {
				if (!entityMap.containsKey(ptrInitiatorName)) {
					entityMap.put(ptrInitiatorName, ((IncidentEntity) incInitiator).getName());
				}
			}
		}

		// 2-Target assets
		Asset ptrTargetAsset = !patternActivity.getTargetedAssets().isEmpty()
				? patternActivity.getTargetedAssets().get(0) : null;
		String ptrTargetAssetName = ptrTargetAsset != null ? ptrTargetAsset.getName() : null;

		if (ptrBigExp.hasEntity(ptrTargetAssetName)) {

			Asset incTargetAsset = !incidentActivity.getTargetedAssets().isEmpty()
					? incidentActivity.getTargetedAssets().get(0) : null;
			canBeApplied = compareAssets(ptrTargetAsset, incTargetAsset);

			if (!canBeApplied) {
				return false;
			}

			if (ptrTargetAsset != null) {
				if (!entityMap.containsKey(ptrTargetAssetName)) {
					entityMap.put(ptrTargetAssetName, incTargetAsset.getName());
				}

			}
		}

		// 3-Resources
		Resource ptrResource = !patternActivity.getResources().isEmpty() ? patternActivity.getResources().get(0) : null;
		String ptrResourceName = ptrResource != null ? ptrResource.getName() : null;

		if (ptrBigExp.hasEntity(ptrResourceName)) {

			Resource incResource = !incidentActivity.getResources().isEmpty() ? incidentActivity.getResources().get(0)
					: null;

			canBeApplied = compareResources(ptrResource, incResource);

			if (!canBeApplied) {
				return false;
			}

			if (ptrResource != null) {
				if (!entityMap.containsKey(ptrResourceName)) {
					entityMap.put(ptrResourceName, incResource.getName());
				}
			}
		}

		// 4-Exploited assets
		Asset ptrExploitedAsset = !patternActivity.getExploitedAssets().isEmpty()
				? patternActivity.getExploitedAssets().get(0) : null;
		String ptrExploitedAssetName = ptrExploitedAsset != null ? ptrExploitedAsset.getName() : null;

		if (ptrBigExp.hasEntity(ptrExploitedAssetName)) {

			Asset incExploitedAsset = !incidentActivity.getExploitedAssets().isEmpty()
					? incidentActivity.getExploitedAssets().get(0) : null;

			canBeApplied = compareAssets(ptrExploitedAsset, incExploitedAsset);

			if (!canBeApplied) {

				// maybe pattern exploited asset could be the target of an
				// activity
				if (comparePrecondition) {
					Asset incTargetAsset = !incidentActivity.getTargetedAssets().isEmpty()
							? incidentActivity.getTargetedAssets().get(0) : null;
					canBeApplied = compareAssets(ptrExploitedAsset, incTargetAsset);

					if (!canBeApplied) {
						return false;
					}

					if (ptrExploitedAsset != null) {
						if (!entityMap.containsKey(ptrExploitedAssetName)) {
							entityMap.put(ptrExploitedAssetName, incTargetAsset.getName());
						}

					}

				}
			} else {
				if (!entityMap.containsKey(ptrExploitedAssetName)) {
					entityMap.put(ptrExploitedAssetName, incExploitedAsset.getName());
				}
			}

		}

		// 5-Locations
		Location ptrLocation = patternActivity.getLocation();
		String ptrLocationName = ptrLocation != null ? ((IncidentEntity) ptrLocation).getName() : null;

		if (ptrBigExp.hasEntity(ptrLocationName)) {

			Location incLocation = incidentActivity.getLocation();

			canBeApplied = compareLocations(ptrLocation, incLocation);

			if (!canBeApplied) {
				return false;
			}

			if (ptrLocation != null) {
				if (!entityMap.containsKey(ptrLocationName)) {
					entityMap.put(ptrLocationName, ((IncidentEntity) incLocation).getName());
				}
			}
		}

		// 6-Vicitms
		Actor ptrVicitm = !patternActivity.getVictims().isEmpty() ? patternActivity.getVictims().get(0) : null;
		String ptrVicitmName = ptrVicitm != null ? ptrVicitm.getName() : null;

		if (ptrBigExp.hasEntity(ptrVicitmName)) {

			Actor incVicitm = !incidentActivity.getVictims().isEmpty() ? incidentActivity.getVictims().get(0) : null;

			canBeApplied = compareActors(ptrVicitm, incVicitm);

			if (!canBeApplied) {
				return false;
			}

			if (ptrVicitm != null) {
				if (!entityMap.containsKey(ptrVicitmName)) {
					entityMap.put(ptrVicitmName, incVicitm.getName());
				}
			}
		}

		// 7-Conditions (pre or post)
		canBeApplied = compareConditions(ptrBigExp, incBigExp);

		if (!canBeApplied) {
			return false;
		}

		return true;
	}

	protected boolean compareInitiators(ActivityInitiator patternActivityInitiator,
			ActivityInitiator incidentActivityInitiator) {

		if (patternActivityInitiator == null) {
			return true;
		}

		// if the pattern activity has an initiator but the incident activity
		// does not then return false
		if (patternActivityInitiator != null && incidentActivityInitiator == null) {
			return false;
		}

		// attributes to compare between initiators
		// 1-Initiator Type (i.e. are they of the same class such as actor or
		// asset)

		// if the class of the pattern activity initiator is the same (or
		// superclass) of the incident activity initiator then return false
		if (!patternActivityInitiator.getClass().isInstance(incidentActivityInitiator)) {
			return false;
		}

		boolean canBeApplied = false;

		if (Actor.class.isInstance(patternActivityInitiator)) {
			canBeApplied = compareActors((Actor) patternActivityInitiator, (Actor) incidentActivityInitiator);
		} else if (Asset.class.isInstance(patternActivityInitiator)) {
			canBeApplied = compareAssets((Asset) patternActivityInitiator, (Asset) incidentActivityInitiator);
		} else if (Resource.class.isInstance(patternActivityInitiator)) {
			canBeApplied = compareResources((Resource) patternActivityInitiator, (Resource) incidentActivityInitiator);
		} else {
			canBeApplied = compareIncidentEntities((IncidentEntity) patternActivityInitiator,
					(IncidentEntity) incidentActivityInitiator);
		}

		if (!canBeApplied) {
			return false;
		}

		return true;
	}

	protected boolean compareIncidentEntities(IncidentEntity patternEntity, IncidentEntity incidentEntity) {

		if (patternEntity != null && incidentEntity == null) {
			return false;
		}

		if (patternEntity == null) {
			return true;
		}

		// Attributes:
		// 1-Type
		// 2-Parent Entity
		// 3-Contained Entities
		// 4-Connections

		// 1-type: compare the types of both entities
		String ptrActType = patternEntity.getType() != null ? patternEntity.getType().getName() : null;
		String incActType = incidentEntity.getType() != null ? incidentEntity.getType().getName() : null;

		if (ptrActType != null && incActType == null) {
			return false;
		}

		if (ptrActType != null && incActType != null) {
			if (!isSameClassOrSuperClass(ptrActType, incActType)) {
				return false;
			}
		}

		// 2-parent entity: find out if the type of the parent entity is of the
		// same type or super type to that of the incident activity
		IncidentEntity ptrActParent = (IncidentEntity) patternEntity.getParentEntity();
		IncidentEntity incActParent = (IncidentEntity) incidentEntity.getParentEntity();

		if (ptrActParent != null && incActParent == null) {
			return false;
		}

		// check parent types (pattern parent type should be same class or super
		// class of incident parent type)
		if (ptrActParent != null && incActParent != null) {

			String ptrParentType = ptrActParent.getType() != null ? ptrActParent.getType().getName() : null;
			String incParentType = incActParent.getType() != null ? incActParent.getType().getName() : null;

			if (!isSameClassOrSuperClass(ptrParentType, incParentType)) {
				return false;
			}
		}

		// 3-Contained Entities: check number & types
		// if knowledge is complete in pattern entity then both should have the
		// same number,
		// and type of pattern contained entities should be same class or
		// superclass
		Knowledge ptrCotnainedEntitiesKnowledge = incidentEntity.getContainedAssetsKnowledge();
		// Knowledge incContainedEntitiesKnowledge =
		// patternEntity.getContainedAssetsKnowledge();

		EList<Location> ptrContainedEntities = patternEntity.getContainedEntities();
		EList<Location> incContainedEntities = incidentEntity.getContainedEntities();

		// check if the knowledge is exact then both should have the same number
		// of entities
		if (ptrCotnainedEntitiesKnowledge.equals(Knowledge.EXACT)) {
			if (ptrContainedEntities.size() != incContainedEntities.size()) {
				return false;
			}
		}

		// check for the case when the pattern has more cotnained entities
		// what should be done!? Allow it? return false?

		// check the types of both (pattern types should be same class or
		// superclass of the incident ones)
		if (!checkContainedEntities(ptrContainedEntities, incContainedEntities)) {
			return false;
		}

		// 4-Connections: check number & types
		// if knowledge is complete in pattern entity then both should have the
		// same number,
		// and type of pattern contained entities should be same class or
		// superclass
		Knowledge ptrConnectionsKnowledge = patternEntity.getConnectionsKnowledge();
		// Knowledge incContainedEntitiesKnowledge =
		// patternEntity.getContainedAssetsKnowledge();

		EList<Connection> ptrConnections = patternEntity.getConnections();
		EList<Connection> incConnections = incidentEntity.getConnections();

		// check if the knowledge is exact then both should have the same number
		// of entities
		if (ptrConnectionsKnowledge.equals(Knowledge.EXACT)) {
			if (ptrConnections.size() != incConnections.size()) {
				return false;
			}
		}

		// check for the case when the pattern has more cotnained entities
		// what should be done!? Allow it? return false?

		// check the types of both (pattern types should be same class or
		// superclass of the incident ones)
		if (!checkConnections(ptrConnections, incConnections)) {
			return false;
		}

		return true;
	}

	protected boolean compareAssets(Asset patternAsset, Asset incidentAsset) {

		if (patternAsset != null && incidentAsset == null) {
			return false;
		}

		if (patternAsset == null) {
			return true;
		}

		boolean isApplicable = compareIncidentEntities(patternAsset, incidentAsset);

		if (!isApplicable) {
			return false;
		}

		EList<Vulnerability> ptrVuls = patternAsset.getVulnerability();
		EList<Vulnerability> incVuls = incidentAsset.getVulnerability();

		// compare vulnerabilities
		List<Integer> matchedConnections = new LinkedList<Integer>();

		for (Vulnerability ptrVul : ptrVuls) {

			if (ptrVul.getName() == null || ptrVul.getName().isEmpty()) {
				continue;// ignored
			}

			boolean isVulMatched = false;

			Vulnerability incVul = null;

			for (int i = 0; i < incVuls.size(); i++) {

				if (matchedConnections.contains(i)) {
					continue;
				}

				incVul = incVuls.get(i);

				if (ptrVul.equals(incVul)) {
					matchedConnections.add(i);
					isVulMatched = true;
					break;
				}
			}

			// if none of the incident vulnerabilities match the pattern
			// vulnerabilities then it is a mismatch
			if (!isVulMatched) {
				return false;
			}

			isVulMatched = false;

		}

		return true;
	}

	protected boolean compareActors(Actor patternActor, Actor incidentActor) {

		if (patternActor != null && incidentActor == null) {
			return false;
		}

		if (patternActor == null) {
			return true;
		}

		boolean canBeApplied = compareIncidentEntities(patternActor, incidentActor);

		if (!canBeApplied) {
			return false;
		}

		// Attributes to compare:
		// 1-Role (e.g., offender, vicitm)
		// 2-Level (e.g., individual, group)

		// 1-Role
		ActorRole ptrRole = patternActor.getRole();
		ActorRole incRole = incidentActor.getRole();

		if (!ptrRole.equals(incRole)) {
			return false;
		}

		// 2-Level
		ActorLevel ptrLevel = patternActor.getLevel();
		ActorLevel incLevel = incidentActor.getLevel();

		if (!ptrLevel.equals(incLevel)) {
			return false;
		}

		return true;
	}

	protected boolean compareResources(Resource patternResource, Resource incidentResource) {

		if (patternResource != null && incidentResource == null) {
			return false;
		}

		if (patternResource == null) {
			return true;
		}

		boolean canBeApplied = compareIncidentEntities(patternResource, incidentResource);

		if (!canBeApplied) {
			return false;
		}

		// specific comparison criteria for resources can be defined here

		return true;
	}

	protected boolean compareLocations(Location patternLocation, Location incidentLocation) {

		if (patternLocation != null && incidentLocation == null) {
			return false;
		}

		if (patternLocation == null) {
			return true;
		}

		boolean canBeApplied = compareIncidentEntities((IncidentEntity) patternLocation,
				(IncidentEntity) incidentLocation);

		if (!canBeApplied) {
			return false;
		}

		// more specifc criteria to locations can be defined here
		// for example, connection ends can be further explored here

		return true;

	}

	/**
	 * Finds if the given patternType parameter is of the same class or super
	 * class of the given incidentType parameter
	 * 
	 * @param patternType
	 * @param incidentType
	 * @return
	 */
	protected boolean isSameClassOrSuperClass(String patternType, String incidentType) {

		// depends on the classes of the cyber physical system

		if (patternType != null && incidentType == null) {
			return false;
		}

		if (patternType == null && incidentType == null) {
			return true;
		}

		try {
			String potentialPatternClassName = "environment.impl." + patternType;

			if (!patternType.endsWith("Impl")) {
				potentialPatternClassName += "Impl";
			}

			Class<?> patternClass = Class.forName(potentialPatternClassName);

			String potentialIncidentClassName = "environment.impl." + incidentType;

			if (!incidentType.endsWith("Impl")) {
				potentialIncidentClassName += "Impl";
			}

			Class<?> incidentClass = Class.forName(potentialIncidentClassName);

			if (!patternClass.isAssignableFrom(incidentClass)) {
				return false;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			return false;
		}

		return true;
	}

	protected boolean checkContainedEntities(EList<Location> patternContainedEntities,
			EList<Location> incidentContainedEntities) {

		// compare type
		LinkedList<Integer> matchedcontainedAssets = new LinkedList<Integer>();

		for (Location ent : patternContainedEntities) {

			IncidentEntity ptrcontainedEntity = (IncidentEntity) ent;

			if (ptrcontainedEntity.getType() == null) {
				continue;// ignored
			}

			String ptrTypeName = ptrcontainedEntity.getType().getName();

			boolean iscontainedEntityMatched = false;

			IncidentEntity incContainedEntity = null;

			for (int i = 0; i < incidentContainedEntities.size(); i++) {

				if (matchedcontainedAssets.contains(i)) {
					continue;
				}

				incContainedEntity = (IncidentEntity) incidentContainedEntities.get(i);

				String incTypeName = incContainedEntity.getType() != null ? incContainedEntity.getType().getName()
						: null;

				if (isSameClassOrSuperClass(ptrTypeName, incTypeName)) {
					matchedcontainedAssets.add(i);
					iscontainedEntityMatched = true;
					break;
				}
			}

			// if none of the incident contained entities match the pattern
			// contained entities then it is a mismatch
			if (!iscontainedEntityMatched) {
				return false;
			}

			iscontainedEntityMatched = false;

		}

		return true;
	}

	protected boolean checkConnections(EList<Connection> patternConnections, EList<Connection> incidentConnections) {

		// compare type
		LinkedList<Integer> matchedConnections = new LinkedList<Integer>();

		for (Connection ptrConnection : patternConnections) {

			if (ptrConnection.getType() == null) {
				continue;// ignored
			}

			String ptrTypeName = ptrConnection.getType().getName();

			boolean isConnectionMatched = false;

			Connection incConnection = null;

			for (int i = 0; i < incidentConnections.size(); i++) {

				if (matchedConnections.contains(i)) {
					continue;
				}

				incConnection = incidentConnections.get(i);

				String incTypeName = incConnection.getType() != null ? incConnection.getType().getName() : null;

				if (isSameClassOrSuperClass(ptrTypeName, incTypeName)) {
					matchedConnections.add(i);
					isConnectionMatched = true;
					break;
				}
			}

			// if none of the incident contained entities match the pattern
			// contained entities then it is a mismatch
			if (!isConnectionMatched) {
				return false;
			}

			isConnectionMatched = false;

		}

		return true;
	}

	protected boolean compareConditions(BigraphExpression patternCondition, BigraphExpression incidentCondition) {

		if (patternCondition == null) {
			return true;
		}

		if (patternCondition != null && (incidentCondition == null || incidentCondition.isEmpty())) {
			return false;
		}

		boolean isGround = true;
		Bigraph incBigraph = incidentCondition.createBigraph(isGround);
		Matcher matcher = new Matcher();

		if (incBigraph != null) {
			// update entities names in the pattern precondition by mapping
			// names to the incident conditions
			BigraphExpression ptrcondCopy = patternCondition.clone();

			boolean isAllMapped = updateEntityNames(ptrcondCopy);

			if (isAllMapped) {
				// create a bigraph of the pattern precondition
				Bigraph ptrBigraph = ptrcondCopy.createBigraph(!isGround);

				if (matcher.match(incBigraph, ptrBigraph).iterator().hasNext()) {
					return true;
				}
			}
		}

		return false;
	}

	protected boolean updateEntityNames(BigraphExpression patternCondition) {

		// System.out.println(entityMap);
		List<String> notFoundNames = new LinkedList<String>();
		LinkedList<Entity> visitedEntities = new LinkedList<Entity>();

		visitedEntities.addAll(patternCondition.getEntity());

		while (!visitedEntities.isEmpty()) {
			Entity entity = visitedEntities.pop();
			if (entityMap.containsKey(entity.getName())) {
				entity.setName(entityMap.get(entity.getName()));
			} else {

				notFoundNames.add(entity.getName());
			}

			visitedEntities.addAll(entity.getEntity());
		}

		// if some entities cannot be mapped then try to find a [similar] entity
		// in the incident activity
		// condition that has not yet been mapped, otherwise the conditions do
		// NOT match
		// currently if there are unmapped entities in the pattern condition
		// then it is a NO match
		if (notFoundNames.size() != 0) {
			// System.out.println(notFoundNames);
			return false;
		}

		// System.out.println("True");
		return true;
	}

}
