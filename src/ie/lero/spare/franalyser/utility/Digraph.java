package ie.lero.spare.franalyser.utility;

import java.util.*;

import i.e.lero.spare.pattern_instantiation.GraphPath;
import i.e.lero.spare.pattern_instantiation.Predicate;

public class Digraph<V> {

    public static class Edge<V>{
        private V vertex;
        private float probability;
        private String label;
        
        public Edge(V v, float prob, String lbl){
            vertex = v; 
            probability = prob;
            label = lbl;
        }
        
        public Edge(V v, float prob){
            vertex = v; 
            probability = prob;
            label = null;
        }

        public V getVertex() {
            return vertex;
        }

        public float getProbability() {
            return probability;
        }

        public String getLabel() {
        	return label;
        }
        
        @Override
        public String toString() {
          
            if(label != null && !label.isEmpty()) {
            	  return "Edge [state=" + vertex + ", probability=" + probability + ", label = "+ label +"]";
            } else {
            	  return "Edge [state=" + vertex + ", probability=" + probability + "]";
            }
        }

    }

    /**
     * A Map is used to map each vertex to its list of adjacent vertices.
     */

    private Map<V, List<Edge<V>>> neighbors = new HashMap<V, List<Edge<V>>>();

    private int nr_edges;

    /**
     * String representation of graph.
     */
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (V v : neighbors.keySet())
            s.append("\n    " + v + " -> " + neighbors.get(v));
        return s.toString();
    }

    /**
     * Add a vertex to the graph. Nothing happens if vertex is already in graph.
     */
    public void add(V vertex) {
        if (neighbors.containsKey(vertex))
            return;
        neighbors.put(vertex, new ArrayList<Edge<V>>());
    }

    public int getNumberOfEdges(){
        int sum = 0;
        for(List<Edge<V>> outBounds : neighbors.values()){
            sum += outBounds.size();
        }
        return sum;
    }

    /**
     * True iff graph contains vertex.
     */
    public boolean contains(V vertex) {
        return neighbors.containsKey(vertex);
    }

    /**
     * Add an edge to the graph; if either vertex does not exist, it's added.
     * This implementation allows the creation of multi-edges and self-loops.
     */
    public void add(V from, V to, float probability) {
        this.add(from);
        this.add(to);

        neighbors.get(from).add(new Edge<V>(to, probability));
    }
    
    public void add(V from, V to, float probability, String label) {
        this.add(from);
        this.add(to);
        neighbors.get(from).add(new Edge<V>(to, probability, label));
    }

    public int outDegree(int vertex) {
        return neighbors.get(vertex).size();
    }

    public int inDegree(V vertex) {
       return inboundNeighbors(vertex).size();
    }

    public List<V> outboundNeighbors(V vertex) {
        List<V> list = new ArrayList<V>();
        for(Edge<V> e: neighbors.get(vertex))
            list.add(e.vertex);
        return list;
    }

    public List<V> inboundNeighbors(V inboundVertex) {
        List<V> inList = new ArrayList<V>();
        for (V to : neighbors.keySet()) {
            for (Edge<V> e : neighbors.get(to))
                if (e.vertex.equals(inboundVertex))
                    inList.add(to);
        }
        return inList;
    }

    public boolean isEdge(V from, V to) {
      for(Edge<V> e :  neighbors.get(from)){
          if(e.vertex.equals(to))
              return true;
      }
      return false;
    }

    public float getProbability(V from, V to) {
        for(Edge<V> e :  neighbors.get(from)){
            if(e.vertex.equals(to))
                return e.probability;
        }
        return -1;
    }
    
    public List<V> getNodes() {
    	List<V> inList = new ArrayList<V>();
        for (V to : neighbors.keySet()) {
                   inList.add(to);
        }
        return inList;
    }
    
    public String getLabel(V from, V to) {
        for(Edge<V> e :  neighbors.get(from)){
            if(e.vertex.equals(to))
                return e.label;
        }
        return null;
    }
    
    public void setLabel(V from, V to, String label) {
        for(Edge<V> e :  neighbors.get(from)){
            if(e.vertex.equals(to))
                e.label = label;
        }
    }
    
}
