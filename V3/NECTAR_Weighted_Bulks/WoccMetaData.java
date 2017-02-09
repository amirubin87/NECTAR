package NECTAR_Weighted_Bulks;

import java.util.Collections;
import java.util.HashSet;
//import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class WoccMetaData {
	UndirectedWeightedGraphWOCC g;    
    public Map<Integer, Map<Integer, Integer>> Intersection_c1_c2;        
    public Map<Integer, Set<Integer>> com2nodes;
    public Map<Integer, Set<Integer>> node2coms;
    
    //cons
    public WoccMetaData(){
    	Intersection_c1_c2 = new ConcurrentHashMap<Integer, Map<Integer, Integer>>();
    	com2nodes = new ConcurrentHashMap<Integer, Set<Integer>>();
    	node2coms = new ConcurrentHashMap<Integer, Set<Integer>>();
    }
    
    public WoccMetaData(UndirectedWeightedGraphWOCC graph){        
        //VerifyNodesNumbers(graph);
    	this(); 
    	g = graph;
    	Integer count = 0;
    
        for (Integer node : graph.nodes()){
            Intersection_c1_c2.put(node, new ConcurrentHashMap<Integer, Integer>());
            Set<Integer> comm = Collections.synchronizedSet(new HashSet<Integer>());
            comm.add(node);
            com2nodes.put(node, comm);
            Set<Integer> commId = Collections.synchronizedSet(new HashSet<Integer>());
            commId.add(node);
            node2coms.put(node, commId);          
            count++;
        }

    }

    /*public WoccMetaData(UndirectedUnweightedGraphW graph, Map<Integer,Set<Integer>> comms){
    	this(); 
    	g = graph;	
        for (Entry<Integer, Set<Integer>> comm :comms.entrySet()){
        	int commID =comm.getKey();
        	Set<Integer> nodes =comm.getValue();        
            com2nodes.put(commID,nodes);
            Intersection_c1_c2.put(commID,new ConcurrentHashMap<>());
            for (int node : nodes){
            		Set<Integer> commInSet = Collections.synchronizedSet(new HashSet<>();
            		commInSet.add(commID);
	                node2coms.put(node,commInSet);
            }
        }
    }*/
    
    public WoccMetaData(UndirectedWeightedGraphWOCC graph, Map<Integer,Set<Integer>> comms, boolean commsMayOverlap){
    	this(); 
    	g = graph;
    	int maxCommIDSeen = 0;
    	int commID = 0;
        for (Entry<Integer, Set<Integer>> comm :comms.entrySet()){
        	commID =comm.getKey();
        	maxCommIDSeen = Math.max(maxCommIDSeen, commID);
        	Set<Integer> nodes =comm.getValue();        
            com2nodes.put(commID,nodes);
            Intersection_c1_c2.put(commID,new ConcurrentHashMap<>());
            for (int node : nodes){
            		Set<Integer> commsNodeIsIn = node2coms.get(node);
            		if(commsNodeIsIn == null){
	            		commsNodeIsIn = Collections.synchronizedSet(new HashSet<>());	            		
		                node2coms.put(node,commsNodeIsIn);
            		}
        			commsNodeIsIn.add(commID);            		
            }
        }
        if(commsMayOverlap){
        	for( Entry<Integer, Set<Integer>> AcommIdAndNodes: com2nodes.entrySet()){
        		for( Entry<Integer, Set<Integer>> BcommIdAndNodes: com2nodes.entrySet()){
        			int AcommId = AcommIdAndNodes.getKey();
        			int BcommId = BcommIdAndNodes.getKey();
        			if (AcommId < BcommId){

        				int intersectionSize = UtillsWOCC.IntersectionSize(AcommIdAndNodes.getValue(), BcommIdAndNodes.getValue());
        				
        				Map<Integer, Integer> AcommDictionary = Intersection_c1_c2.get(AcommId);
        				AcommDictionary.put(BcommId, intersectionSize);
        			}
        		}
        	}
        	commID = maxCommIDSeen +1;
        	for(int node : g.nodes()){
        		Set<Integer> nodesComms = node2coms.get(node);
        		if(nodesComms == null){
        			nodesComms = Collections.synchronizedSet(new HashSet<>());
        			nodesComms.add(commID);
        			node2coms.put(node,nodesComms);
        			Set<Integer> nodeInSet = Collections.synchronizedSet(new HashSet<>());
        			nodeInSet.add(node);
        			com2nodes.put(commID, nodeInSet);
        			commID++;
        		}
        	}
        }
        
    }
    
	public WoccMetaData(WoccMetaData ORIGINALmetaData) {
    	g=ORIGINALmetaData.g;
    	Intersection_c1_c2 = UtillsWOCC.CopyMapIntMapIntInt(ORIGINALmetaData.Intersection_c1_c2);
    	com2nodes = UtillsWOCC.CopyMapIntSet(ORIGINALmetaData.com2nodes);
    	node2coms = UtillsWOCC.CopyMapIntSet(ORIGINALmetaData.node2coms);
	}

	public void ClearCommsOfNode(Integer node){
    	Set<Integer> commsSet = node2coms.get(node); 
    	
    	//update intersection ratio
    	UpdateIntersectionSizeRemove(commsSet);   
    	
    	//Symbolic change
        for(Integer comm : commsSet){
            com2nodes.get(comm).remove(node);
        }

        node2coms.put(node, Collections.synchronizedSet(new HashSet<Integer>()));
    }
   
	private void UpdateIntersectionSizeRemove(Set<Integer> c_v) {
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
        		Integer intersection = Intersection_c1_c2.get(min).get(max);
        		if (intersection == null){
        			intersection =1;
        		}
        		intersection--;
        		Intersection_c1_c2.get(min).put(max, intersection);
        	}
        }		
	}
	
	private void UpdateIntersectionAdd(Set<Integer> c_v) {
		// Update Intersection_c1_c2
        Integer[] commsArray = c_v.toArray( new Integer[c_v.size()]);        
        
	    for (int i = 0; i <commsArray.length ; i++){
	    	for (int j = i+1; j < commsArray.length ; j++){
	    		int x = commsArray[i];
	    		int y = commsArray[j];
	    		Integer lowComm = Math.min(x, y);
	    		Integer highComm = Math.max(x, y);	
		        Map<Integer,Integer> lowCommDic = Intersection_c1_c2.get(lowComm);
			    if ( lowCommDic == null){
			    	lowCommDic = new ConcurrentHashMap<Integer, Integer>();
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

	public Map<Integer[],Double> SetCommsForNode(Integer node, Set<Integer> comms, boolean shouldMergeComms, boolean calcOutput){		
    	// When going to Multy thread todo - arrange this method!
		UpdateIntersectionAdd(comms);
		
		Map<Integer[],Double> commsCouplesIntersectionRatio = new ConcurrentHashMap<Integer[],Double>();
	    
		// Find intersection ratio for merge
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
			        double intersectionRatio = (double)Intersection_c1_c2.get(lowComm).get(highComm)/(double)Math.max(com2nodes.get(lowComm).size(), com2nodes.get(highComm).size());
			        Integer[] sortedComms= new Integer[]{lowComm,highComm};
			        commsCouplesIntersectionRatio.put(sortedComms, intersectionRatio);
		    	}
		    }
        }	    

	    return commsCouplesIntersectionRatio;
    }
	
	public Map<Integer[],Double> GetIntersectionBetweenAllComms(){
		Map<Integer[],Double> commsCouplesIntersectionRatio = new ConcurrentHashMap<Integer[],Double>();
		
		for (Integer lowComm : Intersection_c1_c2.keySet()){
			Map<Integer,Integer> highCommIntersection = Intersection_c1_c2.get(lowComm);
			Integer highCommSize = com2nodes.get(lowComm).size();
	    	for (Entry<Integer, Integer> entry: highCommIntersection.entrySet()){
	    		Integer highComm = entry.getKey();
	    		// This is important - the intersection ration is devide by the MAX!
		        double intersectionRatio = (double)entry.getValue()/(double)Math.max(highCommSize, com2nodes.get(highComm).size());
		        Integer[] sortedComms= new Integer[]{lowComm,highComm};
		        if (intersectionRatio > 0){
		        	commsCouplesIntersectionRatio.put(sortedComms, intersectionRatio);
		        }
			}
		}
		
		return commsCouplesIntersectionRatio;
	}
	
	public void SetCommsForNodeNoMergeForWorker(Integer node, Set<Integer> comms){
		UpdateIntersectionAdd(comms);
		
		// Symbolic add
	    for (Integer comm : comms){
	        com2nodes.get(comm).add(node);	        
	    }
	    node2coms.put(node, comms);
    }

	public void AddCommForNode(int node, int c1){
        
		Set<Integer> comms = node2coms.get(node);
        if(comms.contains(c1)){
        	return;
        }
        
        for (int comm : comms){
            int lowComm = Math.min(comm, c1);
            int highComm = Math.max(comm, c1);

            Map<Integer,Integer> lowCommDic = Intersection_c1_c2.get(lowComm);
		    if ( lowCommDic == null){
		    	lowCommDic = new ConcurrentHashMap<Integer, Integer>();
	            Intersection_c1_c2.put(lowComm, lowCommDic);
		    }	    
		    
		    Integer intersectionSize = lowCommDic.get(highComm);
	        if ( intersectionSize == null){
	        	intersectionSize = 0;
	        }   
	        
	        intersectionSize++;
	        lowCommDic.put(highComm,intersectionSize);
        }
        node2coms.get(node).add(c1);
        com2nodes.get(c1).add(node);
	}
	
	public void RemoveCommForNode(int node, int c1){        
        if(!node2coms.get(node).contains(c1)){
        	return;
        }        
        
        node2coms.get(node).remove(c1);
        com2nodes.get(c1).remove(node);
        Set<Integer> comms = node2coms.get(node);
		
        for (int comm : comms){
            int lowComm = Math.min(comm, c1);
            int highComm = Math.max(comm, c1);

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
                    
	/*private void SymbolicClearComm(Integer comm){    	
    	Set<Integer> nodes = Collections.synchronizedSet(new HashSet<Integer>(com2nodes.get(comm)));
    	for (Integer node: nodes){
    		SymbolicRemoveNodeFromComm(node, comm);
    	}
    }
    
	private void SymbolicRemoveNodeFromComm(Integer node,Integer comm){
        node2coms.get(node).remove(comm);
        com2nodes.get(comm).remove(node);        
    }
    
	private void SymbolicAddNodeToComm(Integer node, Integer comm) {
		node2coms.get(node).add(comm);
		com2nodes.get(comm).add(node);		
	}*/

}
