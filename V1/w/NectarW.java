package w;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class NectarW {
	UndirectedUnweightedGraphW g;
	public double[] betas;
	public double alpha;
	public String outputPath;
	public int iteratioNumToStartMerge;
	public int maxIterationsToRun;
	public int percentageOfStableNodes;
	public String pathToGraph;
	public WoccMetaData ORIGINALmetaData;
	public WoccMetaData metaData;
	
	public NectarW(String pathToGraph, double[]betas, double alpha, String outputPath, int iteratioNumToStartMerge, int maxIterationsToRun, int percentageOfStableNodes, int firstPartMode) throws IOException{
		
		this.percentageOfStableNodes= percentageOfStableNodes;
		this.betas= betas;
		this.alpha = alpha;
		this.outputPath =outputPath;
		this.iteratioNumToStartMerge = iteratioNumToStartMerge;
		this.maxIterationsToRun = maxIterationsToRun;
		this.pathToGraph = pathToGraph;
		this.g = new UndirectedUnweightedGraphW(Paths.get(pathToGraph));		
		Map<Integer, Set<Integer>> firstPart;
		System.out.println("Get first part");
		if (firstPartMode == 0){
			firstPart = GetFirstPartition(g);
		}
		else if (firstPartMode == 3){
			firstPart = GetFirstPartitionCliques3(g);
		}
		else if (firstPartMode == 4){
			firstPart = GetFirstPartitionCliques4(g);
		}
		else{
			throw new RuntimeException("param firstPartMode must be on of 0=CC, 3=clique 3, 4=clique 4");
		}		
		System.out.println("Get meta data");
		this.ORIGINALmetaData = new WoccMetaData(g,firstPart,true);
		this.metaData = this.ORIGINALmetaData;
		
	}
	
	public NectarW(String pathToGraph, String pathToPartition, double[]betas, double alpha, String outputPath, int iteratioNumToStartMerge, int maxIterationsToRun, int percentageOfStableNodes) throws IOException{
		this.percentageOfStableNodes= percentageOfStableNodes;
		this.betas= betas;
		this.alpha = alpha;
		this.outputPath =outputPath;
		this.iteratioNumToStartMerge = iteratioNumToStartMerge;
		this.maxIterationsToRun = maxIterationsToRun;
		this.pathToGraph = pathToGraph;
		this.g = new UndirectedUnweightedGraphW(Paths.get(pathToGraph));
		Map<Integer, Set<Integer>> firstPart = GetPartitionFromFile(pathToPartition);	
		System.out.println(firstPart.entrySet());
		this.ORIGINALmetaData = new WoccMetaData(g,firstPart,true);
		this.metaData = this.ORIGINALmetaData; 		
	}
	
	public void FindCommunities() throws IOException{
		for (double betta : betas){
			System.out.println("");
			System.out.println("                       Input: " + pathToGraph);
			System.out.println("                       betta: " + betta);
			// Create a copy of the original meta data
			metaData = new WoccMetaData(ORIGINALmetaData);
			Map<Integer,Set<Integer>> comms = FindCommunities(betta);
			WriteToFile(comms, betta);
		}
	}
	
	private Map<Integer,Set<Integer>> FindCommunities(double betta) throws FileNotFoundException, UnsupportedEncodingException {
	    int numOfStableNodes = 0;
	    int amountOfScans = 0;
	    int n = g.number_of_nodes();
	    int numOfStableNodesToReach = n*percentageOfStableNodes/100;
	    while (amountOfScans <5 || (numOfStableNodes < numOfStableNodesToReach && amountOfScans < maxIterationsToRun)){	    	
	    	System.out.println("Input: " +pathToGraph + " betta: " + betta + "            Num of iter: " + amountOfScans);
	    	System.out.println(numOfStableNodes);
	    	numOfStableNodes=0;
	    	amountOfScans++;
	    	for (Integer node : g.nodes()){
	    		
	            Set<Integer> c_v_original = metaData.node2coms.get(node);	            
	            metaData.ClearCommsOfNode(node);
	            Map<Integer, Double> comms_inc = new HashMap<Integer, Double>();
	            Set<Integer> neighborComms = Find_Neighbor_Comms(node);
	            for (Integer neighborComm : neighborComms){
	                double inc= Calc_WCC(neighborComm, node);
	                comms_inc.put(neighborComm, inc);
	            }	            
	            Set<Integer> c_v_new =Keep_Best_Communities(comms_inc, betta);
	            
	            boolean shouldMergeComms = amountOfScans>iteratioNumToStartMerge;
				Map<Integer[],Double> commsCouplesIntersectionRatio = metaData.SetCommsForNode(node, c_v_new, shouldMergeComms );
	            boolean haveMergedComms = false;
	            if(shouldMergeComms){
	            	haveMergedComms = FindAndMergeComms(commsCouplesIntersectionRatio,amountOfScans);
	            }	            
	            
	            if (!haveMergedComms && c_v_new.equals(c_v_original)){
	            	numOfStableNodes++;
	            }
	        }
        }    
	    if (amountOfScans >= maxIterationsToRun){
	        System.out.println(String.format("NOTICE - THE ALGORITHM HASNT STABLED. IT STOPPED AFTER SCANNING ALL NODES FOR %1$d TIMES.",maxIterationsToRun));
	    }
	    return metaData.com2nodes;
	}	  
	
	private boolean FindAndMergeComms (Map<Integer[],Double> commsCouplesIntersectionRatio, int amountOfScans){
	    boolean haveMergedComms = false;
	    for (Entry<Integer[],Double > c1c2intersectionRate : commsCouplesIntersectionRatio.entrySet()){	    	
	    	if(c1c2intersectionRate.getValue()>alpha){
	        	Integer[] c1c2 = c1c2intersectionRate.getKey();
	        	MergeComms(c1c2);
	        	haveMergedComms = true;
	        }
	    }
	    return haveMergedComms;
	}

	private void MergeComms(Integer[] commsToMerge){
		Integer c1 = commsToMerge[0];
		Integer c2 = commsToMerge[1];
		List<Integer> copyOfC1= new ArrayList<>(metaData.com2nodes.get(c1));
		List<Integer> copyOfC2= new ArrayList<>(metaData.com2nodes.get(c2));
	    for (Integer node : copyOfC1){	 
	    	metaData.RemoveCommForNode(node,c1);
	        if(!copyOfC2.contains(node)){	        	
	        	metaData.AddCommForNode(node,c2);
	        }	        
	    }
	}
	
	private Set<Integer> Keep_Best_Communities(Map<Integer, Double>comms_imps,double betta){
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

	private Set<Integer> Find_Neighbor_Comms(Integer node){
	    Set<Integer>neighborComms = new HashSet<Integer>();
	    for (Integer neighbor : g.neighbors(node)){
	        neighborComms.addAll(metaData.node2coms.get(neighbor));
	    }
    return neighborComms;
    }
	
	private void WriteToFile(Map<Integer, Set<Integer>> comms, double betta) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter(outputPath + betta + ".txt", "UTF-8");
		for ( Set<Integer> listOfNodes : comms.values()){
			if(listOfNodes.size()>2){
				for(int node : listOfNodes){
					writer.print(node + " ");
				}
				writer.println("");
			}
		}		
		writer.close();	
	}

	public static Map<Integer,Set<Integer>> GetFirstPartition(UndirectedUnweightedGraphW G){
		Map<Integer,Set<Integer>> result = new HashMap<>();
		Map<Integer, Double> CC = G.Clustring();		
	    Map<Integer, Double> sorted_CC = MapUtil.sortByValue(CC);
	    double maxSeenSoFar=1.0;    
	    boolean[] isVisited = new boolean[G.maxNodeId()+1];	    
	    int commID=0;	    
	    for (int v : sorted_CC.keySet()){
	    	if(maxSeenSoFar<CC.get(v)){
	    		throw(new RuntimeException(String.format("sortedCC was not sorted. node: %1$d.", v)));
	    	}	    	
	        if (!isVisited[v]){
	            isVisited[v]= true;
	            Set<Integer> vSet = new HashSet<>();
	            vSet.add(v);
	            result.put(commID, vSet);
	            for(int  neigh : G.neighbors(v)){
	            	if (!isVisited[neigh]){
	            		isVisited[neigh]= true;
	                    result.get(commID).add(neigh);
	                }
	            }
	            commID+=1;
	        }
	    }
	    
	    return result;
	}
	
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
	    Set<Integer> neighbours = g.neighbors(node);
	    Set<Integer> neighInComm = UtillsW.Intersection(commMembers, neighbours);
	    for (int v : neighInComm){
	        for (int u : neighInComm){
	            if (u > v && g.get_edge_weight(u,v)>0){
	                t++;
	            }
	        }
	    }
	    return t;
	}
	
	public Map<Integer,Set<Integer>> GetPartitionFromFile(String partFile) throws IOException{		
		Map<Integer,Set<Integer>> comm2Nodes= new HashMap<Integer,Set<Integer>>();
		List<String> lines= Files.readAllLines(Paths.get(partFile));		
	    int commID=0;
	    for (String line : lines){
	        String[] nodes = line.split(" |\t");	        		 
	    	if (nodes.length >2){
	    		Set<Integer> comm = new HashSet<>();
	    		for (String node : nodes){
	    			comm.add(Integer.parseInt(node.trim()));
	    		}
	            comm2Nodes.put(commID, comm);
	            commID ++;
	    	}
	    }
	    return comm2Nodes;
	}
	
	public static Map<Integer,Set<Integer>> GetFirstPartitionCliques4(UndirectedUnweightedGraphW G){
		Set<Integer> hasComm = new HashSet<>();
		boolean vHasComm=false;
		Map<Integer,Set<Integer>> result = new HashMap<>();
			    
	    int commID=0;
	    Set<Integer> nodes = G.nodes();
	    for (int v :nodes){
	    	if(hasComm.contains(v)){
	    		continue;
	    	}
	    	vHasComm = false;
	    	Set<Integer> vNeigh = G.neighbors(v);
	    	for(int u:vNeigh){
	    		if(vHasComm || u<v){
	    			break;
	    		}
	    		if(!hasComm.contains(u)){
	    			Set<Integer> UVNeigh= UtillsW.Intersection(vNeigh, G.neighbors(u));
	    			for(int w:UVNeigh){
	    				if(vHasComm || w<u){
	    	    			break;
	    	    		}
	    				if(!hasComm.contains(w)){	
	    					for(int z : UtillsW.Intersection(UVNeigh, G.neighbors(w))){
	    						if(vHasComm  || z<w){
	    			    			break;
	    			    		}
	    						if(!hasComm.contains(z)){	    					
			    					Set<Integer> comm = new HashSet<>();
			    					comm.add(v);
			    					comm.add(u);
			    					comm.add(w);
			    					comm.add(z);
			    					result.put(commID, comm);
			    					commID+=1;
			    					break;
			    				}	    						
	    					}
	    				}
	    			}
	    		}
	    	}
	    	if(!vHasComm){
	    		Set<Integer> comm = new HashSet<>();
				comm.add(v);
	    		result.put(commID, comm);
	    		commID+=1;
	    	}
	    }
	    return result;
	}
	
	
	public static Map<Integer,Set<Integer>> GetFirstPartitionCliques3(UndirectedUnweightedGraphW G){
		Set<Integer> hasComm = new HashSet<>();
		boolean vHasComm=false;
		Map<Integer,Set<Integer>> result = new HashMap<>();
			    
	    int commID=0;
	    Set<Integer> nodes = G.nodes();
	    for (int v :nodes){
	    	if(hasComm.contains(v)){
	    		continue;
	    	}
	    	vHasComm = false;
	    	Set<Integer> vNeigh = G.neighbors(v);
	    	for(int u:vNeigh){
	    		if(vHasComm){
	    			break;
	    		}
	    		if(!hasComm.contains(u)){
	    			Set<Integer> UVNeigh= UtillsW.Intersection(vNeigh, G.neighbors(u));
	    			for(int w:UVNeigh){
	    				if(vHasComm){
	    	    			break;
	    	    		}
	    				if(!hasComm.contains(w)){		    					   					
	    					Set<Integer> comm = new HashSet<>();
	    					comm.add(v);
	    					comm.add(u);
	    					comm.add(w);
	    					result.put(commID, comm);
	    					commID+=1;
	    					hasComm.add(v);
	    					hasComm.add(u);
	    					hasComm.add(w);
	    					vHasComm = true;
	    					break;
	    				}
	    			}
	    		}
	    	}
	    	if(!vHasComm){
	    		Set<Integer> comm = new HashSet<>();
				comm.add(v);
	    		result.put(commID, comm);
	    		commID+=1;
	    		hasComm.add(v);
	    	}
	    }
	    return result;
	}
}





