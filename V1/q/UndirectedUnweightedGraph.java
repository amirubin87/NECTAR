package q;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UndirectedUnweightedGraph {	
	private Set<Integer> nodes;
	private Map<Integer,Set<Integer>> neighbors;
	private double number_of_edges;
	
	public UndirectedUnweightedGraph(Path p) throws IOException{
		List<String> lines= Files.readAllLines(p);
		nodes = new HashSet<Integer>();
		neighbors = new HashMap<Integer,Set<Integer>>();
		for (String line : lines){
			String[] parts = line.split(" |\t");
			Integer v = Integer.parseInt(parts[0].trim());
			Integer u = Integer.parseInt(parts[1].trim());
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
	
	public String toString() {		
		return "Num of nodes: " + nodes.size() + " . Num of edges: " + number_of_edges;
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

}
