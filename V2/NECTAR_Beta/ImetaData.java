package NECTAR_Beta;

import java.util.Map;
import java.util.Set;

// Graph meta data.
// Used to react with the community structure.
public interface ImetaData {
	
	// Getter for the set of communities IDs to which the node belongs.
	public Set<Integer> getComsOfNode(Integer node);	        
	
	// Removes the node from all communities.
	public void ClearCommsOfNode(Integer node);
	
	// Place the node in the given communities.
	// Returns: for each relevant couple of communities - the intersection ratio of them.
	// Map key is the 2-sized array holding the communities IDs, sorted.
	// Only communities which the node joined are considered.
	public Map<Integer[],Double> SetCommsForNode(Integer node, Set<Integer> comms, boolean shouldMergeComms );
	
	// Getter for the full com2nodes dictionary.
	public Map<Integer, Set<Integer>> getCom2nodes();
	
	// Getter for the set of nodes IDs which belongs to the community.
	public Set<Integer> getNodesOfComm(Integer comm);	
	
	// Removes the node from the specific community.
	public void RemoveCommForNode(Integer node,Integer comm);
	
	// Adds the node to the specific community.
	public void AddCommForNode(Integer node,Integer comm);
	
	// Calculates the improvement gained from adding the node to the community.	
	public double CalcMetricImprovemant(Integer comm, Integer node);

}
