package ie.lero.spare.franalyser.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import org.json.JSONObject;
import org.json.XML;

import com.saxonica.xqj.SaxonXQDataSource;


public class XqueryExecuter {

	public static final String NS_DECELERATION = "declare namespace cyberPhysical_Incident = \"http://www.example.org/cyberPhysical_Incident\"; "
			+ "declare namespace environment = \"http://www.example.org/environment\";";
	public static String INCIDENT_DOC = "etc/example/eavesdropping_incident-pattern.cpi";
	public static String SPACE_DOC = "etc/example/research_centre_model.environment";
	public static final String INCIDENT_ROOT_ELEMENT = "cyberPhysical_Incident:IncidentDiagram";
	public static final String SPACE_ROOT_ELEMENT = "";
	
	public XqueryExecuter(){
		
	}
	
	public static String executeQueryFromFile(String xqueryFilePath) throws FileNotFoundException, XQException{
		
		StringBuilder res=new StringBuilder();
		XQResultSequence result;
		
		InputStream inputStream = new FileInputStream(new File(xqueryFilePath));
		 XQDataSource ds = new SaxonXQDataSource();
		 
		 XQConnection conn = ds.getConnection();
		 XQPreparedExpression exp = conn.prepareExpression(inputStream);
		 XQItemType xsUntyped = conn.createAtomicType(XQItemType.XQBASETYPE_STRING);
		 
		 //bind extranl variables to ones in the xquery file
		 final QName incidentDoc = new QName("incidentDoc");
		 final QName spaceDoc = new QName("spaceDoc");
		 exp.bindString(incidentDoc, INCIDENT_DOC, xsUntyped);
		 exp.bindString(spaceDoc, SPACE_DOC, xsUntyped);
		 
		 result = exp.executeQuery();
		 
		 while (result.next()) {
		        res.append(result.getItemAsString(null));
			 }
			 
			 return res.toString(); 
	}
	
	public static String executeQuery(String xquery) throws FileNotFoundException, XQException{
		StringBuilder res = new StringBuilder("");
		
		 XQDataSource ds = new SaxonXQDataSource();
		 
		 XQConnection conn = ds.getConnection();
		 XQPreparedExpression exp = conn.prepareExpression(xquery);
		 XQResultSequence result = exp.executeQuery();
		 
		 while (result.next()) {
	        res.append(result.getItemAsString(null));
		 }
		 
		 return res.toString(); 
	}
	
	public static String[] returnActivityNames() throws FileNotFoundException, XQException{
		String [] names = null;
		String res=null; 
		String query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity/concat(data(@name), \"%%\")";
		
		res = executeQuery(query);
		
		if(res != null) {
			names = res.split("%%");
		}
		
		return names;
	}
	
	public static String[] getSystemAssetControls(String asset) throws FileNotFoundException, XQException{
		String [] names = null;
		String res=null; 
	
		String query = NS_DECELERATION+"doc(\""+SPACE_DOC+"\")//asset[@name=(\""+asset+"\")]/concat(data(@control), \"%%\")";
		
		res = executeQuery(query);
		
		if(res != null) {
			names = res.split("%%");
		}
		
		return names;
	}

/*	public static String[] getSystemAssetControls(String [] assets) throws FileNotFoundException, XQException{
		String [] names = null;
		String res=null; 
		StringBuilder asts = new StringBuilder();
		for(int i=0;i<assets.length;i++) {
			if(i<assets.length-1) {
				asts.append("\""+assets[i]+"\",");	
			} else {
				asts.append("\""+assets[i]+"\"");	
			}
			
		}
		System.out.println("Xq:" + asts.toString());
		String query = NS_DECELERATION+"doc(\""+SPACE_DOC+"\")//asset[@name=("+asts.toString()+")]/concat(data(@control), \"%%\")";
		
		res = executeQuery(query);
		
		if(res != null) {
			names = res.split("%%");
		}
		
		return names;
	}
	*/
	
	public static String[] getSystemAssetControls(String [] assets) throws FileNotFoundException, XQException{
		String[] names = new String[assets.length];
		
		for(int i=0;i<assets.length;i++) {
			names[i] = executeQuery(NS_DECELERATION+"doc(\""+SPACE_DOC+"\")//asset[@name=\""+assets[i]+"\"]/data(@control)");
		}

		return names;
	}
	
	public static String[] getSystemAssetControls() throws FileNotFoundException, XQException{
		String [] names = null;
		String res=null; 
		String query = NS_DECELERATION+"doc(\""+SPACE_DOC+"\")//asset/concat(data(@control), \"%%\")";
		
		res = executeQuery(query);
		
		if(res != null) {
			names = res.split("%%");
		}
		
		return names;
	}

	public static String[] returnConditions(String activityName, PredicateType type) throws FileNotFoundException, XQException{
		String [] conditions = null;
		String res=null; 
		String query = null;
		
		if(type == PredicateType.Precondition) {
			query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity[@name=\""+activityName+"\"]/"
					+ "precondition/expression/exp/concat(../../@name,\"##\", ./text(), \"%%\")";	
		} else if (type == PredicateType.Postcondition) {
			query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity[@name=\""+activityName+"\"]/"
					+ "postcondition/expression/exp/concat(../../@name,\"##\", ./text(), \"%%\")";	
		}
		
		res = executeQuery(query);
		
		if(res != null) {
			conditions = res.split("%%");
		}
		
		return conditions;
	}
	
	public static JSONObject getBigraphConditions(String activityName, PredicateType type) throws FileNotFoundException, XQException{

		String res=null; 
		String query = null;
		
		if(type == PredicateType.Precondition) {
			query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity[@name=\""+activityName+"\"]/"
					+ "precondition/expression/entity";
				
		} else if (type == PredicateType.Postcondition) {
			query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity[@name=\""+activityName+"\"]/"
					+ "postcondition/expression/entity";
			
		}
		
		res = executeQuery(query);
		
		JSONObject conditions = XML.toJSONObject(res);
		
		return conditions;
	}
	
	
	//result format: activityName##NextActivities_[space separated]!!PreviousActivities_[comma separated], e.g., activity5##activity6 activity7
	public static String[] returnNextPreviousActivities() throws FileNotFoundException, XQException{
		String [] nextActivities = null;
		String res=null; 
		String query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity"
				+ "/concat(@name,\"##\", data(@nextActivities),\"!!\", data(@previousActivities), \"%%\")";
		
		res = executeQuery(query);
		
		if(res != null) {
			nextActivities = res.split("%%");
		}
		
		return nextActivities;
	}
	
	//result format: activityName#PreviousActivities_space separated, e.g., activity5##activity4 activity3
	public static String[] returnPreviousActivities() throws FileNotFoundException, XQException{
		String [] previousActivities = null;
		String res=null; 
		String query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity"
				+ "/concat(@name,\"##\", data(@previousActivities), \"%%\")";
		
		res = executeQuery(query);
		
		if(res != null) {
			previousActivities = res.split("%%");
		}
		
		return previousActivities;
	}
	
	
	public static void main(String[] args) {
		
		try {
			String [] ast = {"toilet", "light1"};
			
		//System.out.println(Arrays.toString(getSystemAssetControls("a")));
		//isKnowledgePartial("smartDevice1");
		getBigraphConditions("activity1", PredicateType.Precondition);
		
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static boolean isKnowledgePartial(String entityName) throws FileNotFoundException, XQException {
		
		String query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//asset[@name=\""+entityName+"\"]/data(@knowledge)";
		
		String res = executeQuery(query);
		
		//if knowldege is EXACT then it returns false
		//default is to be partial (if it does not exist as an attribute in the pattern xml)
		if(res != null && !res.isEmpty() && res.equals("EXACT")) {
			return false;
		}
		
		return true;
	}
	
		 
}
