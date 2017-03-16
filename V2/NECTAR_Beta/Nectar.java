package NECTAR_Beta;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

// NECTAR algorithm implementation.
public class Nectar {
	
	//================================================================================
    // Properties
    //================================================================================
	
	public boolean UseWOCC = false;
	UndirectedUnweightedGraph g;
	public double[] betas;
	public double alpha;
	public String outputPath;
	public int iteratioNumToStartMerge;
	public int maxIterationsToRun;
	public int percentageOfStableNodes;
	public String pathToGraph;
	public ImetaData OriginalMetaData;
	public ImetaData metaData;
	
	//================================================================================
    // Constructors
    //================================================================================
	
	private Nectar(String pathToGraph, double[]betas, double alpha, String outputPath, int iteratioNumToStartMerge, int maxIterationsToRun, int percentageOfStableNodes, boolean dynamicChoose, boolean givenUseWOCC) throws IOException{
		this.percentageOfStableNodes= percentageOfStableNodes;
		this.betas= betas;
		this.alpha = alpha;
		this.outputPath =outputPath;
		this.iteratioNumToStartMerge = iteratioNumToStartMerge;
		this.maxIterationsToRun = maxIterationsToRun;
		this.pathToGraph = pathToGraph;
		this.g = new UndirectedUnweightedGraph(Paths.get(pathToGraph));
		
		// If we are to dynamicly choose objective function, we check if it should be used.
		this.UseWOCC = dynamicChoose ? CheckIfShouldUseWOCC() : givenUseWOCC;		
		
		if(UseWOCC) {
			System.out.println("                  Using WOCC");
		}
		else{
			System.out.println("                  Using Modularity");
		}
		
		if ( UseWOCC ){
			this.g.CalcTrianglesAndVT();
		}
	}

	public Nectar(String pathToGraph, double[]betas, double alpha, String outputPath, int iteratioNumToStartMerge, int maxIterationsToRun, int percentageOfStableNodes, int firstPartMode, boolean dynamicChoose, boolean givenUseWOCC) throws IOException{
		this(pathToGraph,betas,alpha,outputPath,iteratioNumToStartMerge,maxIterationsToRun, percentageOfStableNodes, dynamicChoose, givenUseWOCC);
		
		if(!UseWOCC){	
			this.OriginalMetaData = new MODMetaData(g);			
		}
		
		else{
			Map<Integer, Set<Integer>> firstPart;
			System.out.println("Get first part");
			if (firstPartMode == 0){
				firstPart = GetFirstPartition(g);
			}
			else if (firstPartMode == 3){
				firstPart = GetFirstPartitionCliques(g, true);
			}
			else if (firstPartMode == 4){
				firstPart = GetFirstPartitionCliques(g, false);
			}
			else{
				throw new RuntimeException("param firstPartMode must be on of 0=CC, 3=clique 3, 4=clique 4");
			}
			
			System.out.println("Get meta data");
			this.OriginalMetaData = new WOCCMetaData(g,firstPart,true);			
		}
		
	}

	//================================================================================
    // Methods 
    //================================================================================
	
	public void FindCommunities() throws IOException{
		for (double betta : betas){
			System.out.println("");
			System.out.println("                       Input: " + pathToGraph);
			System.out.println("                       betta: " + betta);
			// Create a copy of the original meta data
			if(UseWOCC)	{metaData = new WOCCMetaData((WOCCMetaData)OriginalMetaData);}
			else {metaData= new MODMetaData((MODMetaData)OriginalMetaData);}
			
			Map<Integer,Set<Integer>> comms = FindCommunities(betta);
			WriteToFile(comms, betta);
		}
	}
	
	private Map<Integer,Set<Integer>> FindCommunities(double betta) throws FileNotFoundException, UnsupportedEncodingException {
	    int numOfStableNodes = 0;
	    int amountOfScans = 0;
	    int n = g.number_of_nodes();
	    int numOfStableNodesToReach = n*percentageOfStableNodes/100;

	    
	    while (numOfStableNodes < numOfStableNodesToReach && amountOfScans < maxIterationsToRun){	    	
	    	System.out.print("Input: " +pathToGraph + " betta: " + betta + "            Num of iter: " + amountOfScans);
	    	System.out.println(" Number of stable nodes: " + numOfStableNodes);
	    	numOfStableNodes=0;
	    	amountOfScans++;
	    	for (Integer node : g.nodes()){	    		
	            Set<Integer> c_v_original = metaData.getComsOfNode(node);	 
	            
	            metaData.ClearCommsOfNode(node);
	            Map<Integer, Double> comms_inc = new HashMap<Integer, Double>();
	            Set<Integer> neighborComms = Find_Neighbor_Comms(node);
	            for (Integer neighborComm : neighborComms){
	            	
	                double inc= metaData.CalcMetricImprovemant(neighborComm, node);
	                comms_inc.put(neighborComm, inc);
	                
	            }	          
	           
	            Set<Integer> c_v_new =Keep_Best_Communities(comms_inc, betta);
	            
	            boolean shouldMergeComms = amountOfScans > iteratioNumToStartMerge;
				Map<Integer[],Double> commsCouplesIntersectionRatio = metaData.SetCommsForNode(node, c_v_new, shouldMergeComms );
	            boolean haveMergedComms = false;
	            if(shouldMergeComms){
	            	haveMergedComms = FindAndMergeComms(commsCouplesIntersectionRatio);
	            }	            
	            
	            if (!haveMergedComms && c_v_new.equals(c_v_original)){
	            	numOfStableNodes++;
	            }
	        }
        }
	   
	    if (amountOfScans >= maxIterationsToRun){
	        System.out.println(String.format("NOTICE - THE ALGORITHM HASNT STABLED. IT STOPPED AFTER SCANNING ALL NODES FOR %1$d TIMES.",maxIterationsToRun));
	    }	  
	    return metaData.getCom2nodes();
	}	  
	
	private boolean FindAndMergeComms (Map<Integer[],Double> commsCouplesIntersectionRatio){
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
		List<Integer> copyOfC1= new ArrayList<>(metaData.getNodesOfComm(c1));
		List<Integer> copyOfC2= new ArrayList<>(metaData.getNodesOfComm(c2));
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
	        neighborComms.addAll(metaData.getComsOfNode(neighbor));
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

	private boolean CheckIfShouldUseWOCC() {
		return  getNumberOfTriangles() / (double)g.number_of_nodes() > 5.0;
	}
			
	private int getNumberOfTriangles(){		
		int triangles = 0;
		for (Integer v1 : g.nodes()){
			Set<Integer> neighbors = g.neighbors(v1);
			for (Integer v2 : neighbors){
				for (Integer v3 : neighbors){
					if ((!v2.equals(v3))){
						 if (g.neighbors(v2).contains(v3)){
							 triangles++;
						 }
					}
				}
			}
		}		
		return (triangles/6);
	}
	
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////
	private static Map<Integer,Set<Integer>> GetFirstPartition(UndirectedUnweightedGraph G){
		Map<Integer,Set<Integer>> result = new HashMap<>();
		Map<Integer, Double> CC = G.Clustring();		
	    Map<Integer, Double> sorted_CC = Utills.sortByValue(CC);
	    // double maxSeenSoFar=1.0;    
	    boolean[] isVisited = new boolean[G.maxNodeId()+1];	    
	    int commID=0;	    
	    for (int v : sorted_CC.keySet()){	    		    	
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
	
	private static Map<Integer,Set<Integer>> GetFirstPartitionCliques(UndirectedUnweightedGraph G, boolean cliqueSizeIs3){
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
	    			Set<Integer> UVNeigh= Utills.Intersection(vNeigh, G.neighbors(u));
	    			for(int w:UVNeigh){
	    				if(vHasComm || w<u){
	    	    			break;
	    	    		}
	    				if(!hasComm.contains(w)){
	    					if(cliqueSizeIs3){	
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
		    				else{	
		    					for(int z : Utills.Intersection(UVNeigh, G.neighbors(w))){
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
}





