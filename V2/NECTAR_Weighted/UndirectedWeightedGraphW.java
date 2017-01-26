package NECTAR_Weighted;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UndirectedWeightedGraphW {	
	private Set<Integer> nodes;
	// For node v, neighbors[v] is the set of v's neighbors
	private Map<Integer,Set<Integer>> neighbors;
	// For nodes v,u (v<u) weights[v][u] = w(u,v) 
	private Map<Integer,Map<Integer,Double>> weights;
	
	// For each node v T[v] is the number of triangles v takes part in.
	private Map<Integer, Long> T;
	// For each node v TW[v] is the sum of weights of triangles v takes part in.
	private Map<Integer, Double> VTW;
	// For each node v VT[v] is the set of nodes which closes a triangle with v.
	private Map<Integer, Set<Integer>> VT;
	private int MaxNodeId=0;
	// For each node v TripletsW[v] is the sum of weights of triplets with v in the center(vu, vw, regardless of uw).
	private Map<Integer, Double> TripletsW;
	
	// For each node v WeightOfEdgesInNode[v] is the sum of weights of edges with v in them.
	private Map<Integer, Double> WeightOfEdgesInNode;
	
	// For each node v WeightOfEdgesWithTrianglesInNode[v] is 
	// the sum of weights of edges with nodes which close a triangle with v with v in them.
	private Map<Integer, Double> WeightOfEdgesWithTrianglesInNode;
	
	private int number_of_edges;
	private double sum_of_weights;
	
	
	public UndirectedWeightedGraphW(Path p) throws IOException{
		List<String> lines= Files.readAllLines(p,StandardCharsets.UTF_8);
		System.out.println("All graph lines read.");
		nodes = Collections.synchronizedSet(new HashSet<Integer>());
		neighbors = new ConcurrentHashMap<Integer,Set<Integer>>();
		weights = new ConcurrentHashMap<Integer,Map<Integer,Double>>();
		sum_of_weights = 0.0;
		number_of_edges = 0;
		
		for (String line : lines){
			String[] parts = line.split(" |\t");
			Integer v = Integer.parseInt(parts[0].trim());
			Integer u = Integer.parseInt(parts[1].trim());
			Double w = 1.0;
			if(parts.length>2) w = Double.parseDouble(parts[2].trim());
			int max = Math.max(u, v);
			if(max>MaxNodeId){
				MaxNodeId=max;
			}
			
			Set<Integer> vNeig= neighbors.get(v);
			if(vNeig == null){
				vNeig = Collections.synchronizedSet(new HashSet<Integer>());
				neighbors.put(v, vNeig);
			}
			Set<Integer> uNeig= neighbors.get(u);
			if(uNeig == null){
				uNeig = Collections.synchronizedSet(new HashSet<Integer>());
				neighbors.put(u, uNeig);
			}
			// New edge seen, add u to v's neighbors and u to v's.
			if(v!=u && !vNeig.contains(u)){
				vNeig.add(u);
				uNeig.add(v);
				number_of_edges++;
				nodes.add(v);
				nodes.add(u);
				// Add the weight
				sum_of_weights = sum_of_weights + w;
				int minOfVU = Math.min(v, u);
				int maxOfVU = Math.max(v, u);
				Map<Integer,Double> minWeights= weights.get(minOfVU);
				if(minWeights == null){
					minWeights = new ConcurrentHashMap<Integer,Double>();
					weights.put(minOfVU, minWeights);
				}
				minWeights.put(maxOfVU,w);
			}			
		}
		System.out.println("Start calc triangles");
		CalcTrianglesAndVT();
		CalcTripletsWeightAndWeightsInNode();
		CalcWeightOfEdgesWithTrianglesInNode();
	}
	
	private void CalcWeightOfEdgesWithTrianglesInNode() {
		WeightOfEdgesWithTrianglesInNode = new ConcurrentHashMap<Integer, Double>();
		for (Map.Entry<Integer, Set<Integer>> entry : VT.entrySet()){
			Integer v = entry.getKey();
			double sumOfWeights = 0.0;
			for (Integer u : entry.getValue()){
				sumOfWeights = sumOfWeights + GetEdgeWeight(v, u);
			}
			WeightOfEdgesWithTrianglesInNode.put(v, sumOfWeights);			
		}
		
	}

	public String toString() {
		return "Num of nodes: " + nodes.size()
		+ " .\n Num of edges: " + number_of_edges
		+ " .\n Sum of weights: " + sum_of_weights
		+ " .\n T: " + T
		+ " .\n weights: " + weights
		+ " .\n TW: " + VTW
		+ " .\n VT: " + VT
		+ " .\n TripletsW: " + TripletsW
		+ " .\n WeightOfEdgesInNode: " + WeightOfEdgesInNode	
		+ " .\n WeightOfEdgesWithTrianglesInNode: " + WeightOfEdgesWithTrianglesInNode		
		+ " .\n\n";
	}
	
	private double WeightedClustringPerNode(Integer node) {
		int d = (int) degree(node);
		if (d <=1){
			return 0;
		}	
		// CC of a node is the amount of triangles the node is in divided by the number of triplets he is center in (vu,vw)
		// Weighted-CC can use the average weights, the min, the max, etc.
		// We will use the min weight.
		return (double)(2*VTW.get(node))/(TripletsW.get(node));
	}
	
	public Map<Integer,Double> WeightedClustring() {
		Map<Integer,Double> ans = new ConcurrentHashMap<>();
		for(int node:nodes){
			ans.put(node, WeightedClustringPerNode(node));
		}
		return ans;
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

	public boolean AreNeighbors(Integer node, Integer neighbor) {		
		if (neighbors(node).contains(neighbor)){
			return true;
		}
		return false;
	}

	public int size() {		
		return number_of_edges;
	}

	public double degree(Integer node) {		
		return neighbors(node).size();
	}

	public Map<Integer, Long> Triangles() {
		return T;
	}
	
	public Map<Integer, Double> TrianglesWeight() {
		return VTW;
	}
		
	public Map<Integer, Set<Integer>> VTriangles() {
		return VT;
	}
	
	public int maxNodeId() {		
		return MaxNodeId;
	}
	
	public double GetEdgeWeight(int v, int u){
		double w = 0.0;
		try{
			w = weights.get(Math.min(v, u)).get(Math.max(v, u));
		}catch(Exception e){
			return 0.0;
		}
		return w;
	}
	
	// Calculate per node the triplets weight. 
	private void CalcTripletsWeightAndWeightsInNode() {
		TripletsW = new ConcurrentHashMap<Integer, Double>();
		WeightOfEdgesInNode = new ConcurrentHashMap<Integer, Double>();
    	for(int v : nodes){    		
    		double vTripletsW = 0.0;    		
    		double weightInV = 0.0;
        	Set<Integer> vNeighbors = neighbors(v);
        	Integer[] arrVNeighbors = vNeighbors.toArray(new Integer[vNeighbors.size()]);
        	// Go over the neighbors
        	for(int i1 = 0 ; i1 < arrVNeighbors.length ; i1++){
        		Integer v1 = arrVNeighbors[i1];
        		double WeightV1V = weights.get(Math.min(v1, v)).get(Math.max(v1, v));
        		weightInV = weightInV+WeightV1V;
        		for(int i2 = i1+1 ; i2 < arrVNeighbors.length ; i2++){        			
					Integer v2 = arrVNeighbors[i2];
					double WeightV2V = weights.get(Math.min(v2, v)).get(Math.max(v2, v));
					// Add the minimum out of the two edges.
					vTripletsW = vTripletsW + Math.min(WeightV1V, WeightV2V);
        		}
        	}
        	TripletsW.put(v,vTripletsW);
        	WeightOfEdgesInNode.put(v, weightInV);        	
    	}
	}
	
    private Map<Integer, Long> CalcTrianglesAndVT() {
    	T = new ConcurrentHashMap<Integer, Long>();    	 
    	VT = new ConcurrentHashMap<Integer, Set<Integer>>();
    	VTW = new ConcurrentHashMap<Integer, Double>();    	
    	
    	// Initiate values for all nodes.
    	for(int v : nodes){    		
    		T.put(v,(long) 0);    		
    		VT.put(v,Collections.synchronizedSet(new HashSet<>()));
    		VTW.put(v,(double) 0);    		    		    		
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
    						double vu = weights.get(v).get(u);
    						double vw = weights.get(v).get(w);
    						double uw = weights.get(u).get(w);
    						// The weight of the trianlge is the minimum weight of the edges in it.
    						double triangleWeight = Math.min(
    												vu, Math.min(
    												vw, 
    												uw)); 
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
    						VTW.put(v, VTW.get(v)+triangleWeight);
    						VTW.put(u, VTW.get(u)+triangleWeight);
    						VTW.put(w, VTW.get(w)+triangleWeight);    						  						
    					}
    				}
    			}
    		}
    	}
		return T;
	}

	public double GetWeightOfEdgesInNode(Integer x) { 
		return WeightOfEdgesInNode.get(x);
	}

	public double GetWeightOfEdgesWithTriangleInNode(Integer x) {
		return WeightOfEdgesWithTrianglesInNode.get(x);
	}

	public double GetAverageEdgeWeight() {		
		return sum_of_weights/number_of_edges;
	}

	
	
}
