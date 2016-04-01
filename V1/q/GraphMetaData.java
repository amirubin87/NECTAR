package q;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GraphMetaData {
	UndirectedUnweightedGraph g;
    public double m;
    public Map<Integer, Double> K_v;
    public Map<Integer, Map<Integer, Double>> K_v_c;
    public Map<Integer, Map<Integer, Integer>> Intersection_c1_c2;
    public Map<Integer, Double> Sigma_c;    
    public Map<Integer, Set<Integer>> com2nodes;
    public Map<Integer, Set<Integer>> node2coms;
    
    //cons
    public GraphMetaData(){
    	m=0;
    	K_v = new HashMap<Integer, Double>();
    	K_v_c = new HashMap<Integer, Map<Integer,Double>>();
    	Intersection_c1_c2 = new HashMap<Integer, Map<Integer,Integer>>();
    	Sigma_c = new HashMap<Integer, Double>();
    	com2nodes = new HashMap<Integer, Set<Integer>>();
    	node2coms = new HashMap<Integer, Set<Integer>>();
    }
    
    public GraphMetaData(UndirectedUnweightedGraph graph){        
        //VerifyNodesNumbers(graph);
    	this(); 
    	g=graph;
    	Integer count = 0;        
        m = graph.size();
    
        for (Integer node : graph.nodes()){
            Intersection_c1_c2.put(count, new HashMap<Integer, Integer>());
            Set<Integer> comm = new HashSet<Integer>();
            comm.add(node);
            com2nodes.put(count, comm);
            Set<Integer> commId = new HashSet<Integer>();
            commId.add(count);
            node2coms.put(node, commId);
            double nodeDegree = graph.degree(node);
            K_v.put(node, nodeDegree);
            Sigma_c.put(count, nodeDegree);
            //Size_c.put(count, 1);
            count++;
        }

        for (Integer node : graph.nodes()){
            if (K_v_c.get(node) == null){
                K_v_c.put(node, new HashMap<Integer, Double>());
            }
            for (Integer neighbour : graph.neighbors(node)){
            	Integer comm = node2coms.get(neighbour).iterator().next();
                double weight = graph.get_edge_weight(node, neighbour);
                K_v_c.get(node).put(comm, weight);
            }
        }
    }
        
    public GraphMetaData(GraphMetaData ORIGINALmetaData) {
    	g=ORIGINALmetaData.g;
    	m= ORIGINALmetaData.m;
    	K_v = Utills.CopyMapIntDouble(ORIGINALmetaData.K_v);
    	K_v_c = Utills.CopyMapIntMapIntDouble(ORIGINALmetaData.K_v_c);
    	Intersection_c1_c2 = Utills.CopyMapIntMapIntInt(ORIGINALmetaData.Intersection_c1_c2);
    	Sigma_c = Utills.CopyMapIntDouble(ORIGINALmetaData.Sigma_c);
    	com2nodes = Utills.CopyMapIntSet(ORIGINALmetaData.com2nodes);
    	node2coms = Utills.CopyMapIntSet(ORIGINALmetaData.node2coms);
	}

	public void ClearCommsOfNode(Integer node){
    	Set<Integer> commsSet = node2coms.get(node);
    	
    	// Update weights
    	Update_Weights_Remove(commsSet, node, false);    	        
        
    	//Symbolic change
        for(Integer comm : commsSet){
            com2nodes.get(comm).remove(node);
        }

        node2coms.put(node, new HashSet<Integer>());
    }
   
	public Map<Integer[],Double> SetCommsForNode(Integer node, Set<Integer> comms){
		Update_Weights_Add(comms,node, false);
    	
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
	    

	    return commsCouplesIntersectionRatio;
    }

    public void SymbolicClearComm(Integer comm){    	
    	Set<Integer> nodes = new HashSet<Integer>(com2nodes.get(comm));
    	for (Integer node: nodes){
    		SymbolicRemoveNodeFromComm(node, comm);
    	}
    }
    
    public void SymbolicRemoveNodeFromComm(Integer node,Integer comm){
        node2coms.get(node).remove(comm);
        com2nodes.get(comm).remove(node);        
    }
    
	public void SymbolicAddNodeToComm(Integer node, Integer comm) {
		node2coms.get(node).add(comm);
		com2nodes.get(comm).add(node);		
	}
	
    private void Update_Weights_Remove(Set<Integer> c_v, Integer node, boolean shouldCalcIntersictionChange){
        Set<Integer> neighbors = g.neighbors(node);
        
        for (Integer c : c_v){
        	Double edgesWeightFromNodeToC = K_v_c.get(node).get(c);
        	if(edgesWeightFromNodeToC == null){
        		edgesWeightFromNodeToC = 0D;
        	}
            Sigma_c.put(c, Sigma_c.get(c) - K_v.get(node) + edgesWeightFromNodeToC);
            
            for ( Integer neighbour : neighbors){
                double weight = g.get_edge_weight(node, neighbour);
                Double edgesWeightFromNeighorToC = K_v_c.get(neighbour).get(c);
                if(edgesWeightFromNeighorToC == null){
                	edgesWeightFromNeighorToC = 0D;
                }
                K_v_c.get(neighbour).put(c, edgesWeightFromNeighorToC -  weight);

            }
        }        
        
    	// Intersection size update
        Integer[] comms = new Integer[c_v.size()];
        int k = 0;
        for(Integer comm : c_v){
        	comms[k] = comm;
        	k++;      	
        }           
        
        for (int i = 0; i <comms.length ; i ++){
        	for (int j = i+1; j < comms.length ; j++){
        		int x = comms[i];
        		int y = comms[j];
        		Integer min = Math.min(x, y);
        		Integer max = Math.max(x, y);
        		Intersection_c1_c2.get(min).put(max, Intersection_c1_c2.get(min).get(max) -1);
        	}
        }
        
        if(shouldCalcIntersictionChange){
        	Update_Intersection_OneComm_Remove(c_v.iterator().next(), node);
        }
        
        m = m - K_v.get(node)*(c_v.size() - 1);
    }
    
    private void Update_Weights_Add(Set<Integer> c_v, Integer node, boolean shouldCalcIntersictionChange){
        Set<Integer> neighbors = g.neighbors(node);
        double rank = K_v.get(node);
        for (Integer c : c_v){
        	Double edgesWeightFromNodeToC = K_v_c.get(node).get(c);
        	if(edgesWeightFromNodeToC == null){
        		edgesWeightFromNodeToC = 0D;
        	}
            Sigma_c.put(c, Sigma_c.get(c) + rank - edgesWeightFromNodeToC);
        
            for ( Integer neighbour : neighbors){
                double weight = g.get_edge_weight(node, neighbour);
                Double edgesWeightFromNeighorToC = K_v_c.get(neighbour).get(c);
                if(edgesWeightFromNeighorToC == null){
                	edgesWeightFromNeighorToC= 0D;
                }
                K_v_c.get(neighbour).put(c, edgesWeightFromNeighorToC +  weight);
            }
        }
        
        // Update Intersection_c1_c2
        Integer[] commsArray = new Integer[c_v.size()];
        int k = 0;
        for(Integer comm : c_v){
        	commsArray[k] = comm;
        	k++;      	
        }
        
	    for (int i = 0; i <commsArray.length ; i ++){
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
	    
	    if(shouldCalcIntersictionChange){
	    	Update_Intersection_OneComm_Add(c_v.iterator().next(), node);
	    }
	    
        
        m = m + K_v.get(node)*(c_v.size() - 1);
    }
    
    public void Update_Weights_Add(Integer c, Integer node){
    	if(com2nodes.get(c).contains(node)){
    		return;
    	}
    	Set<Integer> c_v = new HashSet<Integer>();
    	c_v.add(c);
    	Update_Weights_Add(c_v,node, true);
    }
    
    public void Update_Weights_Remove(Integer c, Integer node){
    	if(!com2nodes.get(c).contains(node)){
    		return;
    	}
    	
    	Set<Integer> c_v = new HashSet<Integer>();
    	c_v.add(c);
    	Update_Weights_Remove(c_v,node,true);    	
    }

	private void Update_Intersection_OneComm_Remove(Integer c, Integer node) {
		Set<Integer> comms = node2coms.get(node);
		for(Integer comm : comms){
			if(!comm.equals(c)){
				int small = Math.min(c, comm);
				int big = Math.max(c, comm);
				Intersection_c1_c2.get(small).put(big, Intersection_c1_c2.get(small).get(big) -1 );
			}
		}
	}
	
	private void Update_Intersection_OneComm_Add(Integer c, Integer node) {
		Set<Integer> comms = node2coms.get(node);
		for(Integer comm : comms){
			if(!comm.equals(c)){
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
		    
		        lowCommDic.put(highComm, intersectionSize);
			}
		}
	}
    
}
    

    