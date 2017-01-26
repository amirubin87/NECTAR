package NECTAR_Weighted;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class WoccWorker implements Runnable {

	    private Integer node;
	    private double betta;
	    private WoccMetaData metaData;
	    private Set<Set<Integer>> changedComms;
	    private AtomicInteger numOfStableNodes;
	    
	    public WoccWorker(Integer n, double betta, WoccMetaData metaData, Set<Set<Integer>> changedComms,
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
                double inc= Calc_WCC(neighborComm, node);
                comms_inc.put(neighborComm, inc);
            }
            Set<Integer> c_v_new =Keep_Best_Communities(comms_inc, betta);
            changedComms.add(c_v_new);
            
            metaData.SetCommsForNodeNoMergeForWorker(node, c_v_new);
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
	    
	 // TODO support weight
	    public double Calc_WCC(int comm, int  x){	    
			Set<Integer> commMembers = metaData.com2nodes.get(comm);
			long TxV = metaData.T.get(x);	    
		    if (TxV==0){
		        return 0;
		    }
		    
			long TxC = calcT(commMembers, x);	    
			if(TxC == 0){
				return 0;
			}
			BigDecimal partA = new BigDecimal(TxC).divide(new BigDecimal(TxV),10, BigDecimal.ROUND_DOWN); 
		    
		    int VTxV = metaData.VT.get(x).size();
		    if(VTxV == 0){
				return 0;
			}
		    int VTxVnoC = calcVTWithoutComm(commMembers, x);	    
		    double divesor = (double)(commMembers.size() +(VTxVnoC));	    
		    if (divesor==0){
		        return 0;
		    }	    
		    BigDecimal partB = new BigDecimal(VTxV).divide(new BigDecimal(divesor),10, BigDecimal.ROUND_DOWN);	
		    double ans = (partA.multiply(partB)).doubleValue();
		    
		    return ans;
		    
		}

		private int calcVTWithoutComm(Set<Integer> commMembers, int node) {		
			Set<Integer> nodesWithTriangle = metaData.VT.get(node);
			return nodesWithTriangle.size() - UtillsW.IntersectionSize(nodesWithTriangle, commMembers);
		}

		private long calcT(Set<Integer> commMembers, int node) {
			long t=0;
		    Set<Integer> neighbours = metaData.g.neighbors(node);
		    Set<Integer> neighInComm = UtillsW.Intersection(commMembers, neighbours);
		    for (int v : neighInComm){
		        for (int u : neighInComm){
		            if (u > v && metaData.g.get_edge_weight(u,v)>0){
		                t++;
		            }
		        }
		    }
		    return t;
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


