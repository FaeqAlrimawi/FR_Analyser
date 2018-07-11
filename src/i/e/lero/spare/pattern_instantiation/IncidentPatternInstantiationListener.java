package i.e.lero.spare.pattern_instantiation;

import java.util.LinkedList;

public interface IncidentPatternInstantiationListener {
	
	public void updateProgress(int progress);
	public void updateLogger(String msg);
	public void updateAssetMapInfo(String msg);
	public void updateAssetSetInfo(LinkedList<String[]> assetSets);

}
