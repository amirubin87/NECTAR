import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class ModularityWorker implements Runnable {

	    private Integer node;
	    private double betta;
	    private ModularityMetaData metaData;
	    private Set<Set<Integer>> changedComms;
	    private AtomicInteger numOfStableNodes;
	    
	    public ModularityWorker(Integer n, double betta, ModularityMetaData metaData, Set<Set<Integer>> changedComms,
	    		AtomicInteger numOfStableNodes){
	        this.node=n;
	        this.betta = betta;
	        this.metaData = metaData;
	        this.changedComms = changedComms;
	        this.numOfStableNodes = numOfStableNodes;
	    }

	    @Override
	    public void run() {
	        //System.out.println(Thread.currentThread().getName()+" Start. node = "+node);
	        SetBestCommForNode();
	        //System.out.println(Thread.currentThread().getName()+" End.");
	    }

	    private void SetBestCommForNode() {
	    	
            Set<Integer> c_v_original = metaData.node2coms.get(node);

            // Remove from all comms
            metaData.ClearCommsOfNode(node);
            Map<Integer, Double> comms_inc = new HashMap<Integer, Double>();
            Set<Integer> neighborComms = Find_Neighbor_Comms(node);
            for (Integer neighborComm : neighborComms){
                double inc= Calc_Modularity_Improvement(neighborComm, node);
                comms_inc.put(neighborComm, inc);
            }
            Set<Integer> c_v_new =Keep_Best_Communities(comms_inc, betta);
            changedComms.add(c_v_new);
            metaData.SetCommsForNodeNoMerge(node, c_v_new);
            if (c_v_new.equals(c_v_original)){
            	numOfStableNodes.getAndIncrement();
            }
	    }
	    
	    private Set<Integer> Find_Neighbor_Comms(Integer node){
		    Set<Integer>neighborComms = new HashSet<Integer>();
		    for (Integer neighbor : metaData.g.neighbors(node)){
		        neighborComms.addAll(metaData.node2coms.get(neighbor));
		    }
	    return neighborComms;
	    }
	    
	    private double Calc_Modularity_Improvement(Integer comm, Integer node){
	    	double trueSigma = metaData.Sigma_c.get(comm);

	    	// In case the comm is no longer a neighbor comm:
		    Double k_v_c = metaData.K_v_c.get(node).get(comm);
		    if (k_v_c == null) return 0;
	    	return k_v_c-trueSigma*metaData.K_v.get(node)/(2*metaData.m);
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


