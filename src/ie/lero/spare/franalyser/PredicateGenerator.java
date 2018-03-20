package ie.lero.spare.franalyser;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
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
	

	public PredicateHandler generatePredicates() {

		PredicateType[] types = { PredicateType.Precondition, PredicateType.Postcondition };

		try {

			// create activties of the incident
			HashMap<String, IncidentActivity> activities = createIncidentActivities();

			// get controls for the asset set from the system file
			systemAssetControls = XqueryExecuter.getSystemAssetControls(spaceAssetSet);

			// create the Bigraph representation (from LibBig library) for the
			// pre/postconditions of the activities
			// assumption: esach activity has ONE precondition and ONE
			// postcondition
			for (String activity : activities.keySet()) {
				for (PredicateType type : types) {
					JSONObject condition = XqueryExecuter.getBigraphConditions(activity, type);

					// if there is no condition returend then skip creating a
					// predicate for it
					if (condition == null || condition.isNull(JSONTerms.ENTITY)) {
						continue;
					}

					Predicate p = new Predicate();
					p.setIncidentActivity(activities.get(activity));
					p.setPredicateType(type);
					p.setName(activity + "_" + type.toString()); // e.g., name =
																	// activity1_pre1
					// updates entity names and controls from incident pattern
					// to that from the system model
					if (convertToMatchedAssets(condition)) {
						p.setBigraphPredicate(condition);
						if (p.getBigraphPredicate() != null)
							predHandler.addActivityPredicate(activity, p);
					}
				}

			}

		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return predHandler;
	}

	private boolean convertToMatchedAssets(JSONObject obj) {

		JSONArray tmpAry;
		JSONObject tmpObject;
		LinkedList<JSONObject> objs = new LinkedList<JSONObject>();

		if (obj.isNull(JSONTerms.ENTITY)) {
			return false;
		}
		
		objs.add(obj);

		while (!objs.isEmpty()) {
			tmpObject = objs.pop();

			if (JSONArray.class.isAssignableFrom(tmpObject.get(JSONTerms.ENTITY).getClass())) {
				tmpAry = (JSONArray) tmpObject.get(JSONTerms.ENTITY);
			} else {
				tmpAry = new JSONArray();
				tmpAry.put((JSONObject) tmpObject.get(JSONTerms.ENTITY));
			}
			for (int i = 0; i < tmpAry.length(); i++) {
				JSONObject tmpObj = tmpAry.getJSONObject(i);
				
				String name = tmpObj.get(JSONTerms.NAME).toString();
				for (int j = 0; j < incidentAssetNames.length; j++) {
					if (incidentAssetNames[j].equals(name)) {
						tmpObj.put(JSONTerms.NAME, spaceAssetSet[j]);
						tmpObj.put(JSONTerms.CONTROL, systemAssetControls[j]);
						tmpObj.put(JSONTerms.INCIDENT_ASSET_NAME, incidentAssetNames[j]);
						break;
					}
				}

				//add contained entities
				if (!tmpObj.isNull(JSONTerms.ENTITY)) {
					objs.add(tmpObj);
				}
				
			}

		}

		return true;
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
