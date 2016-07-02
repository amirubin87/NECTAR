package NECTAR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class FindBestCommForNodeWorker implements Runnable {

	    private Integer node;
	    private double betta;
	    private ModularityMetaData metaData;
	    private Map<Integer, Set<Integer>> OLDnode2comms;
	    private Map<Integer, Set<Integer>> NEWnode2comms;

	    public FindBestCommForNodeWorker(Integer n, double betta, ModularityMetaData metaData, Map<Integer, Set<Integer>> OLDnode2comms, Map<Integer, Set<Integer>> NEWnode2comms){
	        this.node=n;
	        this.betta = betta;
	        this.metaData = metaData;
	        this.OLDnode2comms = OLDnode2comms;
	        this.NEWnode2comms = NEWnode2comms;
	    }

	    @Override
	    public void run() {
	        //System.out.println(Thread.currentThread().getName()+" Start. node = "+node);
	        findBestCommForNode();
	        //System.out.println(Thread.currentThread().getName()+" End.");
	    }

	    private void findBestCommForNode() {
            Set<Integer> c_v_original = metaData.node2coms.get(node);

            // Remove from all comms
            // TODO_ make thread safe! metaData.ClearCommsOfNode(node);
            Map<Integer, Double> comms_inc = new HashMap<Integer, Double>();
            Set<Integer> neighborComms = Find_Neighbor_Comms(node);
            for (Integer neighborComm : neighborComms){
                double inc= Calc_Modularity_Improvement(neighborComm, node);
                comms_inc.put(neighborComm, inc);
            }
            Set<Integer> c_v_new =Keep_Best_Communities(comms_inc, betta);
            
            // Store old comms.
            OLDnode2comms.put(node, c_v_original);
            
            // Store new comms.
            NEWnode2comms.put(node, c_v_new);
            
            //TODO_ make thread safe! metaData.SetCommsForNode((Integer)node, c_v_original,false);
	    }
	    
	    private Set<Integer> Find_Neighbor_Comms(Integer node){
		    Set<Integer>neighborComms = new HashSet<Integer>();
		    for (Integer neighbor : metaData.g.neighbors(node)){
		        neighborComms.addAll(metaData.node2coms.get(neighbor));
		    }
	    return neighborComms;
	    }
	    
	    private double Calc_Modularity_Improvement(Integer comm, Integer node){
	    	//As Sigma_c is the sum of the weights of the edges incident in nodes in c,
	    	// and we havent took node out, we should reduce it.
	    	Double edgesWeightFromNodeToC = metaData.K_v_c.get(node).get(comm);
        	if(edgesWeightFromNodeToC == null){
        		edgesWeightFromNodeToC = 0D;
        	}        	
	    	double trueSigma = metaData.Sigma_c.get(comm) - metaData.K_v.get(node) + edgesWeightFromNodeToC;
		    
	    	return metaData.K_v_c.get(node).get(comm)-trueSigma*metaData.K_v.get(node)/(2*metaData.m);
	    }
	    
	    private static Set<Integer> Keep_Best_Communities(Map<Integer, Double>comms_imps,double betta){
		    double bestImp = 0;
		    for( double imp : comms_imps.values()){
		    	bestImp = Math.max(bestImp, imp);
		    }
		    
		    Set<Integer> bestComs = new HashSet<Integer>();
			    for(Entry<Integer, Double> entry: comms_imps.entrySet()){
			    		 if (entry.getValue()*betta >= bestImp){
			    				 bestComs.add(entry.getKey());
			    		 }
			    }
		    return bestComs;
		}

	    @Override
	    public String toString(){
	        return "" + this.node;
	    }
	}


