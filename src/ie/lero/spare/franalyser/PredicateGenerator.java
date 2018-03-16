package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.json.JSONArray;
import org.json.JSONObject;

import ie.lero.spare.franalyser.utility.BigraphNode;
import ie.lero.spare.franalyser.utility.PredicateType;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class PredicateGenerator {

	private AssetMap assetMap;
	private PredicateHandler predHandler;
	private String [] spaceAssetSet;
	private String [] incidentAssetNames;
	private boolean isDebugging = true;
	private String [] systemAssetControls;
	
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
			
			predHandler.updateNextPreviousActivities();
			
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return predHandler.getIncidentActivities();
	}
	
	//might go absolute because of the new one
/*	public PredicateHandler generatePredicates2() {
		
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
	*/
	public PredicateHandler generatePredicates() {

		PredicateType[] types = { PredicateType.Precondition, PredicateType.Postcondition };

		try {
			
			// create activties of the incident
			HashMap<String, IncidentActivity> activities = createIncidentActivities();

			//get controls for the asset set from the system file
			systemAssetControls = XqueryExecuter.getSystemAssetControls(spaceAssetSet);

			//create the Bigraph representation (from LibBig library) for the pre/postconditions of the activities
			for (String activity : activities.keySet()) {
				for (PredicateType type : types) {
					JSONObject condition = XqueryExecuter.getBigraphConditions(activity, type);
					Predicate p = new Predicate();
					p.setIncidentActivity(activities.get(activity));
					p.setPredicateType(type);
					p.setName(activity + "_pre");
					convertToMatchedAssets(condition);
					p.setBigraphPredicate(condition);
					if (p.getBigraphPredicate() != null)
						predHandler.addActivityPredicate(activity, p);
				}
			}

		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return predHandler;
	}

	/*private JSONObject convertToMatchedAssets2(JSONObject obj) {
		
		try {
			
			if(obj.isNull("entity")) {
				return obj;
			}
			
			systemAssetControls = XqueryExecuter.getSystemAssetControls(spaceAssetSet);
			
			getChildren(obj);
			
			} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return obj;
	}
	*/
	private void convertToMatchedAssets(JSONObject obj) {

		JSONArray tmpAry;

		if (obj.isNull("entity")) {
			return;
		}

		if (JSONArray.class.isAssignableFrom(obj.get("entity").getClass())) {
			tmpAry = (JSONArray) obj.get("entity");
		} else {
			tmpAry = new JSONArray();
			tmpAry.put((JSONObject) obj.get("entity"));
		}
		for (int i = 0; i < tmpAry.length(); i++) {
			JSONObject tmpObj = tmpAry.getJSONObject(i); // gets hold of node
															// info
			String name = tmpObj.get("name").toString();
			for (int j = 0; j < incidentAssetNames.length; j++) {
				if (incidentAssetNames[j].equals(name)) {
					tmpObj.put("name", spaceAssetSet[j]);
					tmpObj.put("control", systemAssetControls[j]);
					break;
				}
			}

			// iterate over other children
			if (!tmpObj.isNull("entity")) {
				convertToMatchedAssets(tmpObj);
			}

		}
	}

/*	public String matchConditionAssetsToSpaceAssets(String condition) {
		String result = condition;

		//assuming a well formatted Bigraph statement
		for (int i=0;i<incidentAssetNames.length;i++) {
			//if the condition contains the name of an incident asset then replace with 
			if(condition.contains(incidentAssetNames[i])) {
		
				result=result.replaceAll(incidentAssetNames[i], spaceAssetSet[i]);
			}
		}

		return result;
	}*/

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
	
	private void print(String msg) {
		if(isDebugging) {
			System.out.println("PredicateGenerator"+msg);
		}
	}
	
	
	public static void main(String [] args) {
		PredicateGenerator pred = new PredicateGenerator();
		
		try {
			pred.convertToMatchedAssets(XqueryExecuter.getBigraphConditions("activity1", PredicateType.Precondition));
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
