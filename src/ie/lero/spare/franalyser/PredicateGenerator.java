package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.json.JSONObject;

import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class PredicateGenerator {

	private AssetMap assetMap;
	private PredicateHandler predHandler;
	private String [] spaceAssetSet;
	private String [] incidentAssetNames;
	
	public PredicateGenerator() {
		predHandler = new PredicateHandler();

	}

	public PredicateGenerator(AssetMap map) {
		this();
		assetMap = map;
		incidentAssetNames = map.getIncidentAssetNames();
	}
	
	public PredicateGenerator(String[] systemAsset, String[] incidentAssetName) {
		this();
		spaceAssetSet = systemAsset;
		this.incidentAssetNames = incidentAssetName;
	}

	public HashMap<String, IncidentActivity> createIncidentActivities() {
		String[] tmp;
		String [] nextPreviousActivities;
		IncidentActivity activity;

		try {
			nextPreviousActivities = XqueryExecuter.returnNextPreviousActivities();
			
			if (nextPreviousActivities != null) {
				for(String res : nextPreviousActivities) {
					tmp = res.split("##"); //first is activity name, 2nd is next activities, 3rd previous activities
					activity = new IncidentActivity(tmp[0]);
					predHandler.addIncidentActivity(activity);
				}
			}
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return predHandler.getIncidentActivities();
	}
	
	//might go absolute because of the new one
	public PredicateHandler generatePredicates2() {
		
		String[] preconditions = null;
		String[] postconditions = null;
		
		try {
				HashMap<String, IncidentActivity> activities = createIncidentActivities();
				
				for (String activity : activities.keySet()) {
					preconditions = XqueryExecuter.returnConditions(activity, PredicateType.Precondition);
					for (String prec : preconditions) {
						if(prec.isEmpty()) continue; //if it has no preconditions
						Predicate p = new Predicate();
						//p.setActivityName(activity);//to be deleted
						p.setIncidentActivity(activities.get(activity));
						p.setPredicateType(PredicateType.Precondition);
						p.setName(prec.split("##")[0]);
						p.setPredicate(matchConditionAssetsToSpaceAssets(prec.split("##")[1]));
						//predHandler.addPredicate(p);
						predHandler.addActivityPredicate(activity, p);
					}
					postconditions = XqueryExecuter.returnConditions(activity, PredicateType.Postcondition);
					for (String prec : postconditions) {
						if(prec.isEmpty()) continue; //if it has no preconditions
						Predicate p = new Predicate();
					//	p.setActivityName(activity);//to be deleted
						p.setIncidentActivity(activities.get(activity));
						p.setPredicateType(PredicateType.Postcondition);
						p.setName(prec.split("##")[0]);
						p.setPredicate(matchConditionAssetsToSpaceAssets(prec.split("##")[1]));
						//predHandler.addPredicate(p);
						predHandler.addActivityPredicate(activity, p);
					}
				}
				

		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return predHandler;
	}
	
	public PredicateHandler generatePredicates() {

		PredicateType [] types = {PredicateType.Precondition, PredicateType.Postcondition};
		
		try {
				HashMap<String, IncidentActivity> activities = createIncidentActivities();
				
				for (String activity : activities.keySet()) {
					for(PredicateType type : types) {
						JSONObject condition = XqueryExecuter.getBigraphConditions(activity, type);
						Predicate p = new Predicate();
						p.setIncidentActivity(activities.get(activity));
						p.setPredicateType(PredicateType.Precondition);
						p.setName(activity+"_pre");
						condition = convertToMatchedAssets(condition);
						p.setBigraphPredicate(Predicate.convertJSONtoBigraph(condition));
						predHandler.addActivityPredicate(activity, p);
					}
				}
					
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return predHandler;
	}

	private JSONObject convertToMatchedAssets(JSONObject obj) {
		
		/**
		 * it should replace each entity/child in the condition with the matched asset
		 * so the name of the entity/child changes to the corresponding asset name and the control 
		 * changes to the type of the that asset
		 */
		return obj;
	}
	
	public String matchConditionAssetsToSpaceAssets(String condition) {
		String result = condition;

		//assuming a well formatted Bigraph statement
		for (int i=0;i<incidentAssetNames.length;i++) {
			//if the condition contains the name of an incident asset then replace with 
			if(condition.contains(incidentAssetNames[i])) {
		
				result=result.replaceAll(incidentAssetNames[i], spaceAssetSet[i]);
			}
		}

		return result;
	}

	/*public AssetMap getAssetMap() {
		return assetMap;
	}

	public void setAssetMap(AssetMap assetMap) {
		this.assetMap = assetMap;
	}
*/
	public PredicateHandler getPredHandler() {
		return predHandler;
	}

	public void setPredHandler(PredicateHandler predHandler) {
		this.predHandler = predHandler;
	}
	

}
