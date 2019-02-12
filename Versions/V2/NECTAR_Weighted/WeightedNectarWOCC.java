package NECTAR_Weighted;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WeightedNectarWOCC {
	UndirectedWeightedGraphWOCC g;
	public double[] betas;
	public double alpha;
	public String outputPath;
	public int iteratioNumToStartMerge;
	public int maxIterationsToRun;
	public int percentageOfStableNodes;
	public String pathToGraph;
	public WoccMetaData ORIGINALmetaData;
	public WoccMetaData metaData;
	public PrintWriter runTimeLog;
	public long startTime;
	private boolean debug;
	
	public WeightedNectarWOCC(String pathToGraph, double[]betas, double alpha, String outputPath, int iteratioNumToStartMerge, int maxIterationsToRun, int percentageOfStableNodes, int firstPartMode, boolean debug) throws IOException{
		this.runTimeLog = new PrintWriter(new BufferedWriter(new FileWriter("./NectarW-runTime.log", true)));		
		this.startTime = System.currentTimeMillis();
		this.percentageOfStableNodes= percentageOfStableNodes;
		this.betas= betas;
		this.alpha = alpha;
		this.outputPath =outputPath;
		this.iteratioNumToStartMerge = iteratioNumToStartMerge;
		this.maxIterationsToRun = maxIterationsToRun;
		this.pathToGraph = pathToGraph;		
		this.debug = debug;
		this.g = new UndirectedWeightedGraphWOCC(Paths.get(pathToGraph));		

		Map<Integer, Set<Integer>> firstPart;
		System.out.println("Get first part");
		if (firstPartMode == 0){
			firstPart = GetFirstPartition(g);			
		}
		
		else if (firstPartMode == 1){
			// Note that we use the beta given to us first in betas!
			firstPart = GetFirstPartitionDenseGraph(g,betas[0]);
			
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
		//TODO remove
		System.out.println(firstPart.size());
		System.out.println(firstPart);
		TakeTime();
		System.out.println("Get meta data");
		boolean commsMayOverlap = (firstPartMode >1);
		this.ORIGINALmetaData = new WoccMetaData(g,firstPart, commsMayOverlap);
		TakeTime();
		this.metaData = this.ORIGINALmetaData;		
	}

	private void TakeTime() {
		if(debug){
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			runTimeLog.println(totalTime/1000);
			startTime = endTime;
		}
	}
	
	public WeightedNectarWOCC(String pathToGraph, String pathToPartition, double[]betas, double alpha, String outputPath, int iteratioNumToStartMerge, int maxIterationsToRun, int percentageOfStableNodes) throws IOException{		
		if(debug)
			this.runTimeLog = new PrintWriter(new BufferedWriter(new FileWriter("./NectarW-runTime.log", true)));
		this.percentageOfStableNodes= percentageOfStableNodes;
		this.betas= betas;
		this.alpha = alpha;
		this.outputPath =outputPath;
		this.iteratioNumToStartMerge = iteratioNumToStartMerge;
		this.maxIterationsToRun = maxIterationsToRun;
		this.pathToGraph = pathToGraph;		
		this.g = new UndirectedWeightedGraphWOCC(Paths.get(pathToGraph));		
				
		TakeTime();
		Map<Integer, Set<Integer>> firstPart = GetPartitionFromFile(pathToPartition);
		TakeTime();		
		System.out.println(firstPart.entrySet());
		boolean commsMayOverlap = true;
		this.ORIGINALmetaData = new WoccMetaData(g,firstPart, commsMayOverlap);
		TakeTime();
		this.metaData = this.ORIGINALmetaData; 		
	}
	
	public void FindCommunities(boolean runMultyThreaded, int numOfThreads) throws IOException{
		for (double betta : betas){
			System.out.println("");
			System.out.println("                       Input: " + pathToGraph);
			System.out.println("                       betta: " + betta);
			// Create a copy of the original meta data
			metaData = new WoccMetaData(ORIGINALmetaData);
			Map<Integer,Set<Integer>> comms;
			if(runMultyThreaded){
				TakeTime();
				comms = FindCommunitiesMultyThreaded(betta, numOfThreads);
			}
			else{
				TakeTime();
				comms = FindCommunities(betta);
			}			
			TakeTime();
			WriteToFile(comms, betta);			
			TakeTime();
			if(debug)
				runTimeLog.println("DONE Beta");
		}
		if(debug){
			runTimeLog.println("DONE");
			runTimeLog.close();
		}
	}
	
	private Map<Integer,Set<Integer>> FindCommunitiesMultyThreaded(double betta, int numOfThreads) throws FileNotFoundException, UnsupportedEncodingException{
		AtomicInteger numOfStableNodes = new AtomicInteger(0);
	    int amountOfScans = 0;
	    int n = g.number_of_nodes();
	    int numOfStableNodesToReach = n*percentageOfStableNodes/100;	    
	    while (amountOfScans <5 || (numOfStableNodes.intValue() < numOfStableNodesToReach && amountOfScans < maxIterationsToRun)){
	    	System.out.print("Input: " +pathToGraph + " betta: " + betta + "  Num of iterations: " + amountOfScans);
	    	System.out.println("  Number of stable nodes: " + numOfStableNodes);
            numOfStableNodes = new AtomicInteger(0);
            amountOfScans++;
            Set<Set<Integer>> changedComms = Collections.synchronizedSet(new HashSet<Set<Integer>>());            
            
            // Find new comms for each node
            SetCommsForNodesMultyThreaded(numOfThreads, betta, changedComms, numOfStableNodes);
	        
	        // Merge comms.
            Map<Integer[],Double> commsCouplesIntersectionRatio = scanChangedComms(changedComms);
            
            boolean haveMergedComms = false;
            if(amountOfScans>iteratioNumToStartMerge){
        	   haveMergedComms = FindAndMergeComms(commsCouplesIntersectionRatio);
            }
            
            if (haveMergedComms){
        		numOfStableNodes.decrementAndGet();
            }              
	    } 
	    
	    // If we havent meged any communities- we should do so in the end.
	    if(amountOfScans<=iteratioNumToStartMerge){
	    	FindAndMergeComms(metaData.GetIntersectionBetweenAllComms());
	    }
	    	
	    System.out.println("Number of stable nodes: " + numOfStableNodes);	   
	    if (amountOfScans >= maxIterationsToRun){
	        System.out.println(String.format("NOTICE - THE ALGORITHM HASNT STABLED. IT STOPPED AFTER SCANNING ALL NODES FOR  %1$d TIMES.",maxIterationsToRun));
	    }
	    
	    return metaData.com2nodes;
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

	private Map<Integer[], Double> scanChangedComms(Set<Set<Integer>> changedComms) {		
		Map<Integer[],Double> commsCouplesIntersectionRatio = new HashMap<Integer[],Double>();
	    for( Set<Integer> comms : changedComms){
			
	    	// Find intersection ratio for merge
	        Integer[] commsArray = new Integer[comms.size()];
	        int k = 0;
	        for(Integer comm : comms){
	        	commsArray[k] = comm;
	        	k++;      	
	        }        		        
		    for (int i = 0; i <commsArray.length ; i ++){
		    	for (int j = i+1; j < commsArray.length ; j++){
		    		int x = commsArray[i];
		    		int y = commsArray[j];
		    		Integer lowComm = Math.min(x, y);
		    		Integer highComm = Math.max(x, y);
		    		Integer intersection = metaData.Intersection_c1_c2.get(lowComm).get(highComm); 
		    		double intersectionRatio = 0;
		    		if ( intersection !=null){
		    			intersectionRatio = (double)metaData.Intersection_c1_c2.get(lowComm).get(highComm)/(double)Math.min(metaData.com2nodes.get(lowComm).size(), metaData.com2nodes.get(highComm).size());
		    		}
			        Integer[] sortedComms= new Integer[]{lowComm,highComm};
			        commsCouplesIntersectionRatio.put(sortedComms, intersectionRatio);
		    	}
		    }
	    }    

	    return commsCouplesIntersectionRatio;
    }
	
	private void SetCommsForNodesMultyThreaded(int threads, double betta, Set<Set<Integer>> changedComms, AtomicInteger numOfStableNodes) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (Integer node : g.nodes()){            
		    Runnable worker = new WoccWorker(node, betta, metaData, changedComms, numOfStableNodes);
		    executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {            	
		}	
	}
	
	// The reason we keep this non-threaded version is performance, and our ability to measure the time it takes to perform the different parts.
	private Map<Integer,Set<Integer>> FindCommunities(double betta) throws FileNotFoundException, UnsupportedEncodingException {
	    int numOfStableNodes = 0;
	    int amountOfScans = 0;
	    int n = g.number_of_nodes();
	    int numOfStableNodesToReach = n*percentageOfStableNodes/100;
	    
	    long Sec1Time = 0;
	    long Sec2Time = 0;
	    long Sec3Time = 0;	    	    
	    
	    while (amountOfScans <3 || (numOfStableNodes < numOfStableNodesToReach && amountOfScans < maxIterationsToRun)){	    	
	    	System.out.print("Input: " +pathToGraph + " betta: " + betta + "  Num of iter: " + amountOfScans);
	    	System.out.println("  Number of stable nodes: " + numOfStableNodes);
	    	numOfStableNodes=0;
	    	amountOfScans++;
	    	for (Integer node : g.nodes()){	    		
	    		////////////////////////////////////   Section 1
	    		startTime = System.currentTimeMillis();
	            Set<Integer> c_v_original = metaData.node2coms.get(node);	            
	            metaData.ClearCommsOfNode(node);
	            Map<Integer, Double> comms_inc = new HashMap<Integer, Double>();
	            Set<Integer> neighborComms = Find_Neighbor_Comms(node);
	            for (Integer neighborComm : neighborComms){
	                double inc= Calc_WOCC_Weighted(neighborComm, node);
	                comms_inc.put(neighborComm, inc);	              
	            }	
	            // TODO - make true, or add a user param to control this.
	            boolean includeBestComm = true;//Math.random() < 0.5; 
	            Set<Integer> c_v_new =Keep_Best_Communities(comms_inc, betta, includeBestComm);
	            
	            Sec1Time += (System.currentTimeMillis() - startTime);
	            
	            /////////////////////////////////////////    Section 2
	            startTime = System.currentTimeMillis();
	            boolean shouldMergeComms = amountOfScans>iteratioNumToStartMerge;
				Map<Integer[],Double> commsCouplesIntersectionRatio = metaData.SetCommsForNode(node, c_v_new, shouldMergeComms,true );
	            boolean haveMergedComms = false;
	            Sec2Time += (System.currentTimeMillis() - startTime);
	            
        		///////////////////////////////////////    Section 3
	            startTime = System.currentTimeMillis();
	            if(shouldMergeComms){
	            	haveMergedComms = FindAndMergeComms(commsCouplesIntersectionRatio);
	            }	            
	            
	            if (!haveMergedComms && c_v_new.equals(c_v_original)){
	            	numOfStableNodes++;
	            }
	            
	            Sec3Time += (System.currentTimeMillis() - startTime);
	            
	        }  	
            
        }    
	    // If we havent meged any communities- we should do so in the end.
	    if(amountOfScans<=iteratioNumToStartMerge){
	    	FindAndMergeComms(metaData.GetIntersectionBetweenAllComms());
		}
	    
	    if (amountOfScans >= maxIterationsToRun){
	        System.out.println(String.format("NOTICE - THE ALGORITHM HASNT STABLED. IT STOPPED AFTER SCANNING ALL NODES FOR %1$d TIMES.",maxIterationsToRun));
	    }	   
	    if(debug){
		    runTimeLog.println(Sec1Time/(1000));
		    runTimeLog.println(Sec2Time/(1000));
		    runTimeLog.println(Sec3Time/(1000));
	    }
	    return metaData.com2nodes;
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
	
	private Set<Integer> Keep_Best_Communities(Map<Integer, Double>comms_imps,double betta, boolean includeBestComm){
		Set<Integer> bestComms = new HashSet<Integer>();
		// Single element
		if(comms_imps.values().size() == 1){
			bestComms.add(comms_imps.keySet().iterator().next());
			return bestComms;
		}
		// get the best improvement we can gain
		double bestImp = 0;
	    for( double imp : comms_imps.values()){
	    	bestImp = Math.max(bestImp, imp);
	    }	    
       	for(Entry<Integer, Double> entry: comms_imps.entrySet()){
	    		 if (entry.getValue()*betta >= bestImp){
	    			 // If we dont want to take the best comm, we only take those who are less than it.
	    			 if (includeBestComm | entry.getValue() < bestImp){
	    				 bestComms.add(entry.getKey());
	    			 }
	    		 }
	    }
	    
	    return bestComms;
	}	

	private Set<Integer> Find_Neighbor_Comms(Integer node){
	    Set<Integer>neighborComms = new HashSet<Integer>();
	    for (Integer neighbor : g.neighbors(node)){
	        neighborComms.addAll(metaData.node2coms.get(neighbor));
	    }
    return neighborComms;
    }
	
	private void WriteToFile(Map<Integer, Set<Integer>> comms, double betta) throws IOException {
		File files = new File(outputPath);
        if (!files.exists()) {
            if (!files.mkdirs()) {
            	throw new IOException ("Failed to create directories fot the given output path: " + outputPath);
            }
        }

		PrintWriter writer = new PrintWriter(outputPath + betta + ".txt", "UTF-8");
		for ( Set<Integer> listOfNodes : comms.values()){			
			if(listOfNodes.size()>2){
				List<Integer> list = new ArrayList<Integer>(listOfNodes);
				  Collections.sort(list);
				for(int node : list){
					writer.print(node + " ");
				}
				writer.println("");
			}
		}		
		writer.close();	
	}

	public static Map<Integer,Set<Integer>> GetFirstPartition(UndirectedWeightedGraphWOCC G){
		Map<Integer,Set<Integer>> result = new HashMap<>();
		Map<Integer, Double> CC = G.WeightedClustring();		
	    Map<Integer, Double> sorted_CC = MapUtil.sortByValue(CC);
	    double maxSeenSoFar=Double.MAX_VALUE;    
	    boolean[] isVisited = new boolean[G.maxNodeId()+1];	    
	    int commID=0;	    
	    for (int v : sorted_CC.keySet()){
	    	if(maxSeenSoFar<sorted_CC.get(v)){
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
	
	public static Map<Integer,Set<Integer>> GetFirstPartitionDenseGraph(UndirectedWeightedGraphWOCC G, double beta){
		Map<Integer,Set<Integer>> result = new HashMap<>();
		Map<Integer, Double> CC = G.WeightedClustring();		
	    Map<Integer, Double> sorted_CC = MapUtil.sortByValue(CC);	    
	    boolean[] isVisited = new boolean[G.maxNodeId()+1];	    
	    int commID=0;	    
	    for (int v : sorted_CC.keySet()){	    	
	        if (!isVisited[v]){
	            isVisited[v]= true;
	            Set<Integer> vSet = new HashSet<>();
	            vSet.add(v);
	            result.put(commID, vSet);
	            // Only take the best neighbors, which was not visited:
	            // First, take the neigh not visited and the edges weight.
	            Map<Integer, Double> nonVisitedNeighbors = new HashMap<Integer, Double>();
	            double maxW = 0.0;
	            for(int  neigh : G.neighbors(v)){
	            	if (!isVisited[neigh]){
	            		double w = G.GetEdgeWeight(v, neigh);
	            		nonVisitedNeighbors.put(neigh, w);	
	            		maxW = Math.max(maxW, w);
	                }
	            }
	            //Now take only those close enough to the best, according to beta	            
	            for( Entry<Integer, Double> entry : nonVisitedNeighbors.entrySet()){
	            	Double w = entry.getValue();
	            	if (w*beta >= maxW){
		            	Integer neigh = entry.getKey();
		            	isVisited[neigh]= true;
		            	result.get(commID).add(neigh);
	            	}	            	
	            }	            
	            commID+=1;
	        }
	    }
	    
	    return result;
	}
	
	// TODO - this is the same code as in the worker - should be refactored...
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
	
	public static Map<Integer,Set<Integer>> GetFirstPartitionCliques4(UndirectedWeightedGraphWOCC G){
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
	    			Set<Integer> UVNeigh= UtillsWOCC.Intersection(vNeigh, G.neighbors(u));
	    			for(int w:UVNeigh){
	    				if(vHasComm || w<u){
	    	    			break;
	    	    		}
	    				if(!hasComm.contains(w)){	
	    					for(int z : UtillsWOCC.Intersection(UVNeigh, G.neighbors(w))){
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
	
	public static Map<Integer,Set<Integer>> GetFirstPartitionCliques3(UndirectedWeightedGraphWOCC G){
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
	    			Set<Integer> UVNeigh= UtillsWOCC.Intersection(vNeigh, G.neighbors(u));
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





