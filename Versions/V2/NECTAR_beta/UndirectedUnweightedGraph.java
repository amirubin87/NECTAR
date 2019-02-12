package NECTAR_Beta;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Holds data regarding an undirected, unweighed graph.
public class UndirectedUnweightedGraph {
	
	//================================================================================
    // Properties
    //================================================================================
	
	public Set<Integer> nodes;
	public Map<Integer,Set<Integer>> neighbors;
	public double number_of_edges;
	public int MaxNodeId=0;
	
	// Only in used when working with WOCC.
	// See WOCCMetaData for documentation.
	public Map<Integer, Long> T;
	private Map<Integer, Set<Integer>> VT;
	
	//================================================================================
    // Constructors
    //================================================================================

	public UndirectedUnweightedGraph(Path p) throws IOException{
		List<String> lines= Files.readAllLines(p);
		nodes = new HashSet<Integer>();
		neighbors = new HashMap<Integer,Set<Integer>>();
		for (String line : lines){
			String[] parts = line.split(" |\t");
			Integer v = Integer.parseInt(parts[0].trim());
			Integer u = Integer.parseInt(parts[1].trim());
			int max = Math.max(u, v);
			if(max>MaxNodeId){
				MaxNodeId=max;
			}
			Set<Integer> vNeig= neighbors.get(v);
			if(vNeig == null){
				vNeig = new HashSet<Integer>();
				neighbors.put(v, vNeig);
			}
			Set<Integer> uNeig= neighbors.get(u);
			if(uNeig == null){
				uNeig = new HashSet<Integer>();
				neighbors.put(u, uNeig);
			}
			if(v!=u && !vNeig.contains(u)){
				vNeig.add(u);
				uNeig.add(v);
				number_of_edges++;
				nodes.add(v);
				nodes.add(u);				
			}
			
		}
	}

	//================================================================================
    // Methods 
    //================================================================================
	
	public String toString() {		
		return "Num of nodes: " + nodes.size() + " . Num of edges: " + number_of_edges;
	}

	
	public Map<Integer, Long> Triangles() {
		return T;
	}
	
	public Map<Integer, Set<Integer>> VTriangles() {
		return VT;
	}
	
	public int number_of_nodes() {		
		return nodes.size();
	}

	public Set<Integer> nodes() {		
		return nodes;
	}

	public Set<Integer> neighbors(int node) {		
		return neighbors.get(node);
	}

	public double get_edge_weight(Integer node, Integer neighbor) {		
		if (neighbors(node).contains(neighbor)){
			return 1;
		}
		return 0;
	}

	public double size() {		
		return number_of_edges;
	}

	public double degree(Integer node) {		
		return neighbors(node).size();
	}
	
	public int maxNodeId() {		
		return MaxNodeId;
	}
	
	private double ClustringPerNode(Integer node) {
		int d = (int) degree(node);
		if (d <=1){
			return 0;
		}		
		return (double)(2*T.get(node))/(double)(d*(d-1));
	}
	
	// Calc the clustering coefficient of all nodes.
	public Map<Integer,Double> Clustring() {
		Map<Integer,Double> ans = new HashMap<>();
		for(int node:nodes){
			ans.put(node, ClustringPerNode(node));
		}
		return ans;
	}
	
    public Map<Integer, Long> CalcTrianglesAndVT() {
    	T = new HashMap<Integer, Long>();    	 
    	VT = new HashMap<Integer, Set<Integer>>();
    	
    	for(int v : nodes){    		
    		T.put(v,(long) 0);    		
    		VT.put(v,new HashSet<>());
    	}
    	Set<Integer> vTriangle, uTriangle, wTriangle;    	
    	for(int v : nodes){
    		Set<Integer> vNeighbors = neighbors(v);
    		vTriangle = VT.get(v);
    		for( int u : vNeighbors){
    			if(u > v){
    				uTriangle = VT.get(u);
    				for(int w : neighbors(u)){
    					if (w > u && vNeighbors.contains(w)){
    						wTriangle = VT.get(w);
    						vTriangle.add(u);
    						vTriangle.add(w);
    						uTriangle.add(v);
    						uTriangle.add(w);
    						wTriangle.add(v);
    						wTriangle.add(u);
    						T.put(v, T.get(v)+1);
    						T.put(u, T.get(u)+1);
    						T.put(w, T.get(w)+1);
    						
    					}
    				}
    			}
    		}
    	}
		return T;
	}

}
