package NECTAR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalcFeatures {
	
	public static double[] processGraph(String pathToGraph) throws IOException {		
				
		Set<Edge> edges = GetEdgesList(pathToGraph);
		
		Map<Integer,Set<Integer>> adjacencyMap = buildAdjacencyMap(edges);
		
		Set<Integer> nodes = adjacencyMap.keySet();
		
		int NumOfNodes = nodes.size();
		
		
		Map<Integer,Integer> node2triangles = new HashMap<>();					
		double[] triCount = FindTrianglesAndTriplets(adjacencyMap, node2triangles);
		
		double numOfConnectedTriplets = triCount[1];
		
		double numOfNodes = NumOfNodes;		 
		double numOfEdges = edges.size();
		double averageDegree = (2*numOfEdges)/numOfNodes;
		double numOfNodesInTriangles = triCount[2];				
		double numOfTriangles = triCount[0];
		double avergaeTrianglesRate = numOfTriangles/numOfNodes;
		//double avergaeTrianglesParticipationRate = 3*numOfTriangles/numOfNodes;
		double ratioOfNodesInTriangles = numOfNodesInTriangles/numOfNodes;
		
		double GCC = 3*numOfTriangles/(double)numOfConnectedTriplets;
		System.currentTimeMillis();
		double ACC = calcACC(adjacencyMap, node2triangles, NumOfNodes);		
		
		return new double[] {
				averageDegree,
				avergaeTrianglesRate,
				ratioOfNodesInTriangles,
				GCC,
				ACC,
				//Place holder for label
				0.0
				};
	}	
	
	private static double calcACC(Map<Integer, Set<Integer>> adjacencyMap, 
			Map<Integer,Integer> node2triangles, int NumOfNodes) {
		double sum = 0;
		
		for (Integer v : adjacencyMap.keySet()){
			Integer numOfTrianglesI = node2triangles.get(v);
			Double numOfTriangles = numOfTrianglesI == null ? 0.0 : 1.0*numOfTrianglesI;
			double deg = adjacencyMap.get(v).size();
			if (deg>1)
				sum = sum + 2*numOfTriangles / (deg *(deg-1));
		}
		
		return sum / NumOfNodes;
	}


	private static double[] FindTrianglesAndTriplets(Map<Integer, Set<Integer>> adjacencyMap, Map<Integer,Integer> node2triangles) {		
		int triangles = 0;
		int ONLYtriplets = 0;
		int numOfNodesInTriangles=0;
		
		for ( Integer v1 : adjacencyMap.keySet()){		
			Set<Integer> neighbors = adjacencyMap.get(v1);			
			for (Integer v2 : neighbors){
				int v2val = v2.intValue();
				for (Integer v3 : neighbors){
					int v3val = v3.intValue();
					if (v2val < v3val){							 
						 if (adjacencyMap.get(v2).contains(v3)){
							 //v1-v2-v3
							 triangles++;	
							 Integer val = node2triangles.get(v1);
							 if(val == null) {
								 numOfNodesInTriangles++;
								 val = 0;
							 }
							 node2triangles.put(v1, val+1);
						 }
						 else{
							// v1-v2-v3	
							ONLYtriplets++;
						 }
					}
				}
				
			}
		}	
		// any triangle has 3 connected triplets in it.
		return  new double[] {triangles/3, ONLYtriplets + triangles, numOfNodesInTriangles};
	}
		
	private static Set<Edge> GetEdgesList(String pathToGraph) throws IOException {
		List<String> lines= Files.readAllLines(Paths.get(pathToGraph));
		Set<Edge> edges= new HashSet<>();
		
		for (String line : lines){
			String[] parts = line.split(" |\t");
			Integer v = Integer.parseInt(parts[0].trim());
			Integer u = Integer.parseInt(parts[1].trim());
			if(v.intValue() == u.intValue()) continue;
			if(v.intValue() > u.intValue()){
				Integer t = u;
				u = v;
				v = t;				
			}				
			edges.add(new Edge(v, u));
		}
		return edges;
	}
		
	public static Map<Integer,Set<Integer>> buildAdjacencyMap(Set<Edge> edges){
		if ((edges==null) || (edges.isEmpty())){
			return Collections.<Integer,Set<Integer>>emptyMap();
		}
		
		Map<Integer,Set<Integer>> AdjacencyMap = new HashMap<>();
		for (Edge e : edges){
			if (!AdjacencyMap.containsKey(e.getFrom())){
				AdjacencyMap.put((Integer)e.getFrom(), new HashSet<Integer>());
			}
			if (!AdjacencyMap.containsKey(e.getTo())){
				AdjacencyMap.put((Integer)e.getTo(), new HashSet<Integer>());
			}
			AdjacencyMap.get(e.getFrom()).add((Integer)e.getTo());
			AdjacencyMap.get(e.getTo()).add((Integer)e.getFrom());
		}
		
		return AdjacencyMap;
	}
		
	
}
