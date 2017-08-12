package NECTAR_Beta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MetaData_Base implements ImetaData{

	//================================================================================
    // Properties
    //================================================================================		
	
	UndirectedUnweightedGraph g;    
    
	public ImetricHandler metricHandler;
	
    // A mapping of communities IDs (sorted!) to the intersection size between them.   
    public Map<Integer, Map<Integer, Integer>> Intersection_c1_c2;        

    // Mapping of community ID to nodes member in the community.
    public Map<Integer, Set<Integer>> com2nodes;
    
    // Mapping of a node to the communities to which he belongs.
    public Map<Integer, Set<Integer>> node2coms;
    
    //================================================================================
    // Constructors
    //================================================================================

    public MetaData_Base(ImetricHandler metricHandler){
    	this.metricHandler = metricHandler;
    	Intersection_c1_c2 = new HashMap<Integer, Map<Integer, Integer>>();
    	metricHandler.Init();
    	com2nodes = new HashMap<Integer, Set<Integer>>();
    	node2coms = new HashMap<Integer, Set<Integer>>();
    }
    
    public MetaData_Base(UndirectedUnweightedGraph graph, ImetricHandler metricHandler){
    	this(metricHandler); 
    	metricHandler.Init(graph);
    	g = graph;
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
       
    // Copy - constructor
	public MetaData_Base(MetaData_Base ORIGINALmetaData) {
    	g=ORIGINALmetaData.g;
    	metricHandler= metricHandler.deepCopy();
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
    	UpdateIntersectionRatioRemoveSingleNode(commsSet);   
    	
    	//Symbolic change
        for(Integer comm : commsSet){
        	metricHandler.UpdateRemoveNodeFromComm(node,comm);
            com2nodes.get(comm).remove(node);
        }

        node2coms.put(node, new HashSet<Integer>());
    }
 	
	public Map<Integer[],Double> SetCommsForNode(Integer node, Set<Integer> comms, boolean shouldMergeComms){		
    	
		UpdateIntersectionRatioAddSingleNode(comms);
		
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
	    	metricHandler.UpdateAddNodeToComm(node,comm);
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
        metricHandler.UpdateAddNodeToComm(node,comm);
        node2coms.get(node).add(comm);
        com2nodes.get(comm).add(node);
	}
	
	public void RemoveCommForNode(Integer node, Integer comm) {
        if(!node2coms.get(node).contains(comm)){
        	return;
        }        
        metricHandler.UpdateRemoveNodeFromComm(node,comm);
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
		return metricHandler.CalcMetricImprovemant(comm,node);
	}
	
	private void UpdateIntersectionRatioRemoveSingleNode(Set<Integer> c_v) {
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
	
	private void UpdateIntersectionRatioAddSingleNode(Set<Integer> c_v) {
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
		return new MetaData_Base(this);
	}	
}
