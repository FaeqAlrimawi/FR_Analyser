package ie.lero.spare.franalyser.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import org.json.JSONObject;
import org.json.XML;

import com.saxonica.xqj.SaxonXQDataSource;

public class XqueryExecuter {

	public static String NS_DECELERATION = "declare namespace cyberPhysical_Incident = \"http://www.example.org/cyberPhysical_Incident\"; "
			+ "declare namespace environment = \"http://www.example.org/environment\";";
	public static String INCIDENT_DOC = "incident.cpi";
	public static String SPACE_DOC = "space.xml";
	public static String INCIDENT_ROOT_ELEMENT = "cyberPhysical_Incident:IncidentDiagram";
	public static String SPACE_ROOT_ELEMENT = "";
	
	public XqueryExecuter(){
		
	}
	
	public static String executeQueryFromFile(String xqueryFilePath) throws FileNotFoundException, XQException{
		StringBuilder res=new StringBuilder();
		InputStream inputStream = new FileInputStream(new File(xqueryFilePath));
		 XQDataSource ds = new SaxonXQDataSource();
		 
		 XQConnection conn = ds.getConnection();
		 XQPreparedExpression exp = conn.prepareExpression(inputStream);
		 XQResultSequence result = exp.executeQuery();
		 
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

	public static String[] returnConditions(String activityName, PredicateType type) throws FileNotFoundException, XQException{
		String [] conditions = null;
		String res=null; 
		String query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity[@name=\""+activityName+"\"]/"
				+ "condition[@xsi:type=\"cyberPhysical_Incident:"+type.toString()+"\"]/expression/exp/concat(../../@name,\"##\", ./text(), \"%%\")";
		
		res = executeQuery(query);
		
		if(res != null) {
			conditions = res.split("%%");
		}
		
		return conditions;
	}
	
	public static JSONObject getBigraphConditions(String activityName, PredicateType type) throws FileNotFoundException, XQException{

		String res=null; 
		String query = NS_DECELERATION+"doc(\""+INCIDENT_DOC+"\")//"+INCIDENT_ROOT_ELEMENT+"/activity[@name=\""+activityName+"\"]/"
				+ "condition[@xsi:type=\"cyberPhysical_Incident:"+type.toString()+"\"]/expression/entity";
		
		res = executeQuery(query);
		
		JSONObject conditions = XML.toJSONObject(res);
		System.out.println(conditions.toString(4));
		
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
		getBigraphConditions("activity1", PredicateType.Precondition);
		} catch (FileNotFoundException | XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
		 
}
