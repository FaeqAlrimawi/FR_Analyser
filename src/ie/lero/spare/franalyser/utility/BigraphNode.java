package ie.lero.spare.franalyser.utility;

import java.util.LinkedList;

public class BigraphNode {
	
	private String id;
	private LinkedList<String> outerNames;
	private LinkedList<String> innerNames;
	private boolean site;
	private LinkedList<BigraphNode> nodes;
	private String control;
	private BigraphNode parent;
	private int parentRoot;
	
	public BigraphNode() {
		outerNames = new LinkedList<String>();
		innerNames = new LinkedList<String>();
		site = false;
		nodes = new LinkedList<BigraphNode>();
		parentRoot = -1;
		parent = null;
	}

	public void addOuterName(String name) {
		if(!outerNames.contains(name)) {
			outerNames.add(name);
		}
		
	}
	
	public void addOuterNames(LinkedList<String> names) {
		if(names != null && !names.isEmpty()){
			for(String n : names) {
				addOuterName(n);
			}
		}
		
	}
	
	public void addInnerName(String name) {
		if(!innerNames.contains(name)) {
			innerNames.add(name);
		}
	}
	
	public void addInnerNames(LinkedList<String> names) {
		if(names != null && !names.isEmpty()){
			for(String n : names) {
				addInnerName(n);
			}
		}
		
	}
	
	public void addBigraphNode(BigraphNode node) {
		if(!nodes.contains(node)) {
			nodes.add(node);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!BigraphNode.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    final BigraphNode other = (BigraphNode) obj;
	    
	    //if both nodes have the same id they are equal
	    if(other.getId().equals(this.id)) {
	    	return true;
	    }
	    
	    return false;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LinkedList<String> getOuterNames() {
		return outerNames;
	}

	public void setOuterNames(LinkedList<String> outerNames) {
		this.outerNames = outerNames;
	}

	public LinkedList<String> getInnerNames() {
		return innerNames;
	}

	public void setInnerNames(LinkedList<String> innerNames) {
		this.innerNames = innerNames;
	}

	public boolean getSite() {
		return site;
	}

	public void setSite(boolean site) {
		this.site = site;
	}

	public LinkedList<BigraphNode> getNodes() {
		return nodes;
	}

	public void setNodes(LinkedList<BigraphNode> nodes) {
		this.nodes = nodes;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String control) {
		this.control = control;
	}

	public BigraphNode getParent() {
		return parent;
	}

	public void setParent(BigraphNode parent) {
		this.parent = parent;
	}

	public int getParentRoot() {
		return parentRoot;
	}

	public void setParentRoot(int parentRoot) {
		this.parentRoot = parentRoot;
	}
	
	public String toString() {
		StringBuilder res = new StringBuilder();
		
		res.append("id:").append(getId()).append(",")
			.append("control:").append(getControl()).append(",");
		
		if(parent == null) {
			res.append("parentRoot:").append(getParentRoot());
		} else {
			res.append("parent:").append(getParent().getId());
		}
		
		//append outer names
		if(outerNames != null && outerNames.size()>0) {
		res.append(",outerNames:{");
		for(int i=0;i<outerNames.size();i++) {
			res.append(outerNames.get(i));
			if(i != outerNames.size()-1) {
				res.append(",");
			}	
		}
		res.append("}");
		}
		
		//append inner names
				if(innerNames != null && innerNames.size()>0) {
				res.append(",innerNames:{");
				for(int i=0;i<innerNames.size();i++) {
					res.append(innerNames.get(i));
					if(i != innerNames.size()-1) {
						res.append(",");
					}	
				}
				res.append("}");
				}
		
		//append site
		res.append("site:"+site);
		
		res.append("\n");
		
		return res.toString();
	}
	

}
