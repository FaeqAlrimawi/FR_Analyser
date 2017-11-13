package ie.lero.spare.franalyser;


import java.io.FileNotFoundException;
import javax.xml.xquery.XQException;
import ie.lero.spare.franalyser.utility.XqueryExecuter;

public class Mapper {

	private String xqueryFilePath;

	public Mapper() {

	}

	public Mapper(String xqueryFilePath) {
		this.xqueryFilePath = xqueryFilePath;
	}

	public AssetMap findMatches() throws FileNotFoundException, XQException {
		String res = null;
		
		res = XqueryExecuter.executeQueryFromFile(xqueryFilePath);
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

		map.setIncidentAssetNames(incidentAssetNames);
		map.setSpaceAssetMatches(matches);

		return map;
	}


	public String getXquery() {
		return xqueryFilePath;
	}

	public void setXquery(String xquery) {
		this.xqueryFilePath = xquery;
	}

}
