package NECTAR_Beta;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

// Meta data to be used when maximizing WOCC.
public class WOCCMetaData implements ImetaData{

	//================================================================================
    // Properties
    //================================================================================		
	
	UndirectedUnweightedGraph g;    
    
	// A mapping between a node and the amount of triangles in which he participants.
	public Map<Integer, Long> T;
	
	// A mapping between a node and all nodes which closes a triangle with him.
    public Map<Integer, Set<Integer>> VT;  
    
    // A mapping of communities IDs (sorted!) to the intersection size between them.   
    public Map<Integer, Map<Integer, Integer>> Intersection_c1_c2;        

    // Mapping of community ID to nodes member in the community.
    public Map<Integer, Set<Integer>> com2nodes;
    
    // Mapping of a node to the communities to which he belongs.
    public Map<Integer, Set<Integer>> node2coms;
    
    //================================================================================
    // Constructors
    //================================================================================

    public WOCCMetaData(){
    	Intersection_c1_c2 = new HashMap<Integer, Map<Integer, Integer>>();
    	T = new HashMap<Integer, Long>();
    	VT = new HashMap<Integer, Set<Integer>>();
    	com2nodes = new HashMap<Integer, Set<Integer>>();
    	node2coms = new HashMap<Integer, Set<Integer>>();
    }
    
    public WOCCMetaData(UndirectedUnweightedGraph graph){        
        //VerifyNodesNumbers(graph);
    	this(); 
    	g = graph;
    	T = graph.Triangles();
    	VT = graph.VTriangles();
    	Integer count = 0;
    
        for (Integer node : graph.nodes()){
            Intersection_c1_c2.put(node, new HashMap<Integer, Integer>());
            Set<Integer> comm = new HashSet<Integer>();
            comm.add(node);
            com2nodes.put(node, comm);
            Set<Integer> commId = new HashSet<Integer>();
            commId.add(node);
            node2coms.put(node, commId);          
            count++;
        }

    }

    /*public WOCCMetaData(UndirectedUnweightedGraph graph, Map<Integer,Set<Integer>> comms){
    	this(); 
    	g = graph;
    	T = graph.Triangles();   
    	VT = graph.VTriangles(); 	
        for (Entry<Integer, Set<Integer>> comm :comms.entrySet()){
        	int commID =comm.getKey();
        	Set<Integer> nodes =comm.getValue();        
            com2nodes.put(commID,nodes);
            Intersection_c1_c2.put(commID,new HashMap<>());
            for (int node : nodes){
            		Set<Integer> commInSet = new HashSet<>();
            		commInSet.add(commID);
	                node2coms.put(node,commInSet);
            }
        }
    }*/
    
    public WOCCMetaData(UndirectedUnweightedGraph graph, Map<Integer,Set<Integer>> comms, boolean partitionIsFromFile){
    	this(); 
    	g = graph;
    	T = graph.Triangles();   
    	VT = graph.VTriangles(); 
    	int maxCommIDSeen = 0;
    	int commID = 0;
        for (Entry<Integer, Set<Integer>> comm :comms.entrySet()){
        	commID =comm.getKey();
        	maxCommIDSeen = Math.max(maxCommIDSeen, commID);
        	Set<Integer> nodes =comm.getValue();        
            com2nodes.put(commID,nodes);
            Intersection_c1_c2.put(commID,new HashMap<>());
            for (int node : nodes){
            		Set<Integer> commsNodeIsIn = node2coms.get(node);
            		if(commsNodeIsIn == null){
	            		commsNodeIsIn = new HashSet<>();	            		
		                node2coms.put(node,commsNodeIsIn);
            		}
        			commsNodeIsIn.add(commID);            		
            }
        }
        
        if(partitionIsFromFile){
        	for( Entry<Integer, Set<Integer>> AcommIdAndNodes: com2nodes.entrySet()){
        		for( Entry<Integer, Set<Integer>> BcommIdAndNodes: com2nodes.entrySet()){
        			int AcommId = AcommIdAndNodes.getKey();
        			int BcommId = BcommIdAndNodes.getKey();
        			if (AcommId < BcommId){
        				int intersectionSize = Utills.IntersectionSize(AcommIdAndNodes.getValue(), BcommIdAndNodes.getValue());
        				Map<Integer, Integer> AcommDictionary = Intersection_c1_c2.get(AcommId);
        				AcommDictionary.put(BcommId, intersectionSize);
        			}
        		}
        	}
        	commID = maxCommIDSeen +1;
        	for(int node : g.nodes()){
        		Set<Integer> nodesComms = node2coms.get(node);
        		if(nodesComms == null){
        			nodesComms = new HashSet<>();
        			nodesComms.add(commID);
        			node2coms.put(node,nodesComms);
        			Set<Integer> nodeInSet = new HashSet<>();
        			nodeInSet.add(node);
        			com2nodes.put(commID, nodeInSet);
        			commID++;
        		}
        	}
        }
    }
    
    // Copy - constructor
	public WOCCMetaData(WOCCMetaData ORIGINALmetaData) {
    	g=ORIGINALmetaData.g;
    	T=Utills.CopyMapIntLong(ORIGINALmetaData.T);
    	VT=Utills.CopyMapIntSet(ORIGINALmetaData.VT);
    	Intersection_c1_c2 = Utills.CopyMapIntMapIntInt(ORIGINALmetaData.Intersection_c1_c2);
    	com2nodes = Utills.CopyMapIntSet(ORIGINALmetaData.com2nodes);
    	node2coms = Utills.CopyMapIntSet(ORIGINALmetaData.node2coms);
	}

	//================================================================================
    // Methods 
    //================================================================================
	
	public void ClearCommsOfNode(Integer node){
    	Set<Integer> commsSet = node2coms.get(node); 
    	
    	//update intersection ratio
    	UpdateIntersectionRatioRemove(commsSet);   
    	
    	//Symbolic change
        for(Integer comm : commsSet){
            com2nodes.get(comm).remove(node);
        }

        node2coms.put(node, new HashSet<Integer>());
    }
 	
	public Map<Integer[],Double> SetCommsForNode(Integer node, Set<Integer> comms, boolean shouldMergeComms){		
    	
		UpdateIntersectionRatioAdd(comms);
		
		Map<Integer[],Double> commsCouplesIntersectionRatio = new HashMap<Integer[],Double>();
	    
		// Find intersection ration for merge
        Integer[] commsArray = new Integer[comms.size()];
        int k = 0;
        for(Integer comm : comms){
        	commsArray[k] = comm;
        	k++;      	
        }        
        
		// Symbolic add
	    for (Integer comm : comms){	    	
	        com2nodes.get(comm).add(node);	        
	    }
	    
	    node2coms.put(node, comms);
        if(shouldMergeComms){
		    for (int i = 0; i <commsArray.length ; i ++){
		    	for (int j = i+1; j < commsArray.length ; j++){
		    		int x = commsArray[i];
		    		int y = commsArray[j];
		    		Integer lowComm = Math.min(x, y);
		    		Integer highComm = Math.max(x, y);
			        double intersectionRatio = (double)Intersection_c1_c2.get(lowComm).get(highComm)/(double)Math.min(com2nodes.get(lowComm).size(), com2nodes.get(highComm).size());
			        Integer[] sortedComms= new Integer[]{lowComm,highComm};
			        commsCouplesIntersectionRatio.put(sortedComms, intersectionRatio);
		    	}
		    }
        }
	    

	    return commsCouplesIntersectionRatio;
    }
	
	public void AddCommForNode(Integer node, Integer comm) {
		Set<Integer> comms = node2coms.get(node);
        if(comms.contains(comm)){
        	return;
        }
        
        for (int c : comms){
            int lowComm = Math.min(c, comm);
            int highComm = Math.max(c, comm);

            Map<Integer,Integer> lowCommDic = Intersection_c1_c2.get(lowComm);
		    if ( lowCommDic == null){
		    	lowCommDic = new HashMap<Integer, Integer>();
	            Intersection_c1_c2.put(lowComm, lowCommDic);
		    }	    
		    
		    Integer intersectionSize = lowCommDic.get(highComm);
	        if ( intersectionSize == null){
	        	intersectionSize = 0;
	        }   
	        
	        intersectionSize++;
	        lowCommDic.put(highComm,intersectionSize);
        }
        node2coms.get(node).add(comm);
        com2nodes.get(comm).add(node);
	}
	
	public void RemoveCommForNode(Integer node, Integer comm) {
        if(!node2coms.get(node).contains(comm)){
        	return;
        }        
        
        node2coms.get(node).remove(comm);
        com2nodes.get(comm).remove(node);
        Set<Integer> comms = node2coms.get(node);
		
        for (int c : comms){
            int lowComm = Math.min(c, comm);
            int highComm = Math.max(c, comm);

            Map<Integer,Integer> lowCommDic = Intersection_c1_c2.get(lowComm);
		    if ( lowCommDic != null){
		    	Integer intersectionSize = lowCommDic.get(highComm);
		    	if ( intersectionSize != null){
		    		intersectionSize--;
		    		lowCommDic.put(highComm,intersectionSize);
		    		}
		    }
        }    
        
	}
                    
	public Set<Integer> getComsOfNode(Integer node) {
		return node2coms.get(node);				
	}

	public Map<Integer, Set<Integer>> getCom2nodes() {
		return com2nodes;
	}
	
	public Set<Integer> getNodesOfComm(Integer comm) {
		return com2nodes.get(comm);		
	}
	
	public double CalcMetricImprovemant(Integer comm, Integer node) {	    
		Set<Integer> commMembers = com2nodes.get(comm);
		long TxV = T.get(node);	    
	    if (TxV==0){
	        return 0;
	    }
	    
		long TxC = calcT(commMembers, node);	    
		if(TxC == 0){
			return 0;
		}
		BigDecimal partA = new BigDecimal(TxC).divide(new BigDecimal(TxV),10, BigDecimal.ROUND_DOWN); 
	    
	    int VTxV = VT.get(node).size();
	    if(VTxV == 0){
			return 0;
		}
	    int VTxVnoC = calcVTWithoutComm(commMembers, node);	    
	    double divesor = (double)(commMembers.size() +(VTxVnoC));	    
	    if (divesor==0){
	        return 0;
	    }	    
	    BigDecimal partB = new BigDecimal(VTxV).divide(new BigDecimal(divesor),10, BigDecimal.ROUND_DOWN);	
	    double ans = (partA.multiply(partB)).doubleValue();
	    
	    return ans;    
	}

	private int calcVTWithoutComm(Set<Integer> commMembers, int node) {		
		Set<Integer> nodesWithTriangle = VT.get(node);
		return nodesWithTriangle.size() - Utills.IntersectionSize(nodesWithTriangle, commMembers);
	}

	private long calcT(Set<Integer> commMembers, int node) {
		long t=0;
	    Set<Integer> neighbours = g.neighbors(node);
	    Set<Integer> neighInComm = Utills.Intersection(commMembers, neighbours);
	    for (int v : neighInComm){
	        for (int u : neighInComm){
	            if (u > v && g.get_edge_weight(u,v)>0){
	                t++;
	            }
	        }
	    }
	    return t;
	}

	private void UpdateIntersectionRatioRemove(Set<Integer> c_v) {
		// Intersection size update
        Integer[] comms = new Integer[c_v.size()];
        int k = 0;
        for(Integer comm : c_v){
        	comms[k] = comm;
        	k++;      	
        }           
        
        for (int i = 0; i <comms.length ; i++){
        	for (int j = i+1; j < comms.length ; j++){
        		int x = comms[i];
        		int y = comms[j];
        		
        		Integer min = Math.min(x, y);
        		Integer max = Math.max(x, y);
        		Intersection_c1_c2.get(min).put(max, Intersection_c1_c2.get(min).get(max)-1);
        	}
        }		
	}
	
	private void UpdateIntersectionRatioAdd(Set<Integer> c_v) {
		// Update Intersection_c1_c2
        Integer[] commsArray = new Integer[c_v.size()];
        int k = 0;
        for(Integer comm : c_v){
        	commsArray[k] = comm;
        	k++;      	
        }
        
	    for (int i = 0; i <commsArray.length ; i++){
	    	for (int j = i+1; j < commsArray.length ; j++){
	    		int x = commsArray[i];
	    		int y = commsArray[j];
	    		Integer lowComm = Math.min(x, y);
	    		Integer highComm = Math.max(x, y);	
		        Map<Integer,Integer> lowCommDic = Intersection_c1_c2.get(lowComm);
			    if ( lowCommDic == null){
			    	lowCommDic = new HashMap<Integer, Integer>();
		            Intersection_c1_c2.put(lowComm, lowCommDic);
			    }	    
			    
			    Integer intersectionSize = lowCommDic.get(highComm);
		        if ( intersectionSize == null){
		        	intersectionSize = 0;
		        }   
		        
		        intersectionSize++;
		    
		        lowCommDic.put(highComm, intersectionSize);
	    	}
	    }
	}

	@Override
	public ImetaData deepCopy() {
		// TODO Auto-generated method stub
		return new WOCCMetaData(this);
	}
}
