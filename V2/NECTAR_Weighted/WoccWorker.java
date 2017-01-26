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
                double inc= Calc_WOCC_Weighted(neighborComm, node);
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
	    
	    public double Calc_WOCC_Weighted(int comm, Integer x){	    
	    	// The sum of the weights of the triangles which the node is in.
			double TxV = metaData.g.TrianglesWeight().get(x);	    
		    if (TxV==0){
		        return 0;
		    }
	    			    
	    	Set<Integer> commMembers = metaData.com2nodes.get(comm);			
		    
	    	// The sum of the weights of the triangles which the node has with
			// nodes in the comm.
			double TxC = calcTWeights(commMembers, x);	    
			if(TxC == 0){
				return 0;
			}
			BigDecimal partA = new BigDecimal(TxC).divide(new BigDecimal(TxV),10, BigDecimal.ROUND_DOWN); 
		    			
			// The sum of weights of edges between x and nodes which share 
			// a triangle with x
		    double VTxV = metaData.g.GetWeightOfEdgesWithTriangleInNode(x);
		    if(VTxV == 0){
				return 0;
			}
		    // The sum of weights of edges between x and nodes which share 
 			// a triangle with x, except those in the community
		    double VTxVnoC = calcVTWithoutComm(commMembers, x);	 
		    
		    double averageEdgeWeight = metaData.g.GetAverageEdgeWeight();
		    double divesor = (double)(commMembers.size()*averageEdgeWeight +(VTxVnoC));	    
		    if (divesor==0){
		        return 0;
		    }	    
		    BigDecimal partB = new BigDecimal(VTxV).divide(new BigDecimal(divesor),10, BigDecimal.ROUND_DOWN);	
		    double ans = (partA.multiply(partB)).doubleValue();
		    
		    return ans;
		    
		}
	    
		// Calc the sum of weights of edges with nodes which v is in trianlges with, 
	    // without the nodes of the community 
	    private double calcVTWithoutComm(Set<Integer> commMembers, int node) {		
			double weight = 0.0;
			Set<Integer> nodesWithTriangle = metaData.g.VTriangles().get(node);
			Set<Integer> nodesWithTriangleNotInComm = UtillsWOCC.RemoveElements(nodesWithTriangle, commMembers);
			// Go over the candidates, verify they close triangles, if so - take the weigths.
			Integer[] arrCandidates = nodesWithTriangleNotInComm.toArray(new Integer[nodesWithTriangleNotInComm.size()]);        	
	    	
			Set<Integer> alreadyAddedWeights = new HashSet<Integer>(); 
			for(int i1 = 0 ; i1 < arrCandidates.length ; i1++){
	    		Integer v1 = arrCandidates[i1];
	    		boolean v1WasAdded = alreadyAddedWeights.contains(v1);
	    		for(int i2 = i1+1 ; i2 < arrCandidates.length ; i2++){        			
					Integer v2 = arrCandidates[i2];
					boolean v2WasAdded = alreadyAddedWeights.contains(v2);
					// If they are neighbors - we have a triangle - and so we add the weights of their edges.
					if (metaData.g.AreNeighbors(v1,v2)){
						if (!v1WasAdded){
							double WeightV1V = metaData.g.GetEdgeWeight(node,v1);
							weight = weight + WeightV1V;
							v1WasAdded = true;
							alreadyAddedWeights.add(v1);
						}
						if (!v2WasAdded){
							double WeightV2V = metaData.g.GetEdgeWeight(node,v2);
							weight = weight + WeightV2V;
							v2WasAdded = true;
							alreadyAddedWeights.add(v2);
						}
					}
	    		}
	    	}
	    	return weight;
		}

		// For the given node and comm, we sum the weights of the triangles which the node has with
		// nodes in the comm.
		private double calcTWeights(Set<Integer> commMembers, int node) {
			double t=0;
		    Set<Integer> neighbours = metaData.g.neighbors(node);
		    Set<Integer> neighInComm = UtillsWOCC.Intersection(commMembers, neighbours);
		    Integer[] arrNeighInComm = neighInComm.toArray(new Integer[neighInComm.size()]);
        	// Go over the neighbors
        	for(int i1 = 0 ; i1 < arrNeighInComm.length ; i1++){
        		Integer v1 = arrNeighInComm[i1];
        		double WeightV1V = metaData.g.GetEdgeWeight(v1,node);
        		for(int i2 = i1+1 ; i2 < arrNeighInComm.length ; i2++){        			
					Integer v2 = arrNeighInComm[i2];
					if (metaData.g.AreNeighbors(v1,v2)){
						double WeightV2V = metaData.g.GetEdgeWeight(v2,node);
						double WeightV1V2 = metaData.g.GetEdgeWeight(v2,v1);						
						t= t + Math.min(
								WeightV1V, Math.min(
								WeightV2V, 
								WeightV1V2)); 
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


