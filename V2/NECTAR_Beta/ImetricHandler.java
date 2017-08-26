package NECTAR_Beta;

import java.util.Map;
import java.util.Set;

public interface ImetricHandler {
	
	// Used for copy constructures
	ImetricHandler deepCopy();
	
	// Initiate the data structures you need
	void Init();
	
	// Initiate the data structures you need based on the @graph. 
	// NOTE - may be called by the used after a call to Init().
	void Init(UndirectedUnweightedGraph graph);
	
	// Initiate the data structures you need based on the @graph and the @firstPart - a mapping of community id and list of nodes.
	// There is no optino for overlapping in the firstPart!
	// NOTE - may be called by the used after a call to Init().
	void Init(UndirectedUnweightedGraph graph, Map<Integer,Set<Integer>> firstPart);
	
	// Update the data strcutures with respect to removing the node @node from the comm @comm.
	void UpdateRemoveNodeFromComm(Integer node, Integer comm);

	// Update the data strcutures with respect to adding the node @node to the comm @comm.
	void UpdateAddNodeToComm(Integer node, Integer comm);

	// What will be the gain from adding the node @ndoe to the comm @comm.
	double CalcMetricImprovemant(Integer comm, Integer node);

}
