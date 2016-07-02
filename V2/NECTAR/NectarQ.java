package NECTAR;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import java.util.concurrent.Executors;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NectarQ {
	public ModularityMetaData ORIGINALmetaData;
	public ModularityMetaData metaData;
	public UndirectedUnweightedGraphQ g;
	public double[] betas;
	public double alpha;
	public String outputPath;
	public int iteratioNumToStartMerge;
	public int maxIterationsToRun;
	public String pathToGraph;
	public PrintWriter runTimeLog;
	public long startTime;
	
	public NectarQ(String pathToGraph, double[]betas, double alpha, String outputPath, int iteratioNumToStartMerge, int maxIterationsToRun) throws IOException{
		this.runTimeLog = new PrintWriter(new BufferedWriter(new FileWriter("./NectarQ-runTime.log", true)));
		this.startTime = System.currentTimeMillis();
		this.betas= betas;
		this.alpha = alpha;
		this.outputPath =outputPath;
		this.iteratioNumToStartMerge = iteratioNumToStartMerge;
		this.maxIterationsToRun = maxIterationsToRun;
		this.pathToGraph = pathToGraph;
		this.g = new UndirectedUnweightedGraphQ(Paths.get(pathToGraph));
		TakeTime();
		TakeTime();
		this.ORIGINALmetaData = new ModularityMetaData(g);
		TakeTime();		
	}
	
	private void TakeTime() {
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		runTimeLog.println(totalTime/1000);
		startTime = endTime;
	}
	
	public void FindCommunities(boolean runMultyThreaded, int numOfThreads) throws FileNotFoundException, UnsupportedEncodingException, InterruptedException{
		for (double betta : betas){
			System.out.println("");
			System.out.println("                       Input: " + pathToGraph);
			System.out.println("                       betta: " + betta);
			// Create a copy of the original meta data
			metaData = new ModularityMetaData(ORIGINALmetaData);
			Map<Integer,Set<Integer>> comms;
			if(runMultyThreaded){
				//1
				TakeTime();
				comms = FindCommunitiesMultyThreaded(betta, numOfThreads);
			}
			else{
				//1
				TakeTime();
				comms = FindCommunities(betta);
			}
			//5
			TakeTime();
			WriteToFile(comms, betta);
			//6
			TakeTime();
			runTimeLog.println("DONE Beta");
		}
		runTimeLog.println("DONE");
		runTimeLog.close();
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

	private Map<Integer,Set<Integer>> FindCommunitiesMultyThreaded(double betta, int numOfThreads) throws FileNotFoundException, UnsupportedEncodingException, InterruptedException{
	    int numOfStableNodes = 0;
	    int amountOfScans = 0;
	    int n = g.number_of_nodes();
	    while (numOfStableNodes < n && amountOfScans < maxIterationsToRun){
	    	System.out.println("Input: " +pathToGraph + " betta: " + betta + "            Num of iterations: " + amountOfScans);
	    	System.out.println("Number of stable nodes: " + numOfStableNodes);
            numOfStableNodes = 0;
            amountOfScans++;
            Map<Integer, Set<Integer>> OLDnode2comms = new ConcurrentHashMap <Integer, Set<Integer>>();
            Map<Integer, Set<Integer>> NEWnode2comms = new ConcurrentHashMap <Integer, Set<Integer>>();
            
            
            
            // Find new comms for each node
            FindCommsForNodesMultyThreaded(numOfThreads, betta, OLDnode2comms, NEWnode2comms);
            
            
	        Set<Set<Integer>> changedComms = new HashSet<>();
            // Move nodes to new comms
	        for (Integer node : g.nodes()){       	
	        	
	        	// Remove from all comms
	            metaData.ClearCommsOfNode(node);
	            Set<Integer> c_v_original = OLDnode2comms.get((Integer)node);
	        	Set<Integer> c_v_new = NEWnode2comms.get((Integer)node);
	        	
	        	changedComms.add(c_v_new);
    			metaData.SetCommsForNodeNoMerge(node, c_v_new);
    			if (c_v_new.equals(c_v_original)){
	            	numOfStableNodes++;
	            }
	        }
	        
	        // Merge comms.
            Map<Integer[],Double> commsCouplesIntersectionRatio = scanChangedComms(changedComms);
            
            boolean haveMergedComms = false;
            if(amountOfScans>iteratioNumToStartMerge){
        	   haveMergedComms = FindAndMergeComms(commsCouplesIntersectionRatio);
            }
            
            if (haveMergedComms){
        		numOfStableNodes--;
            }            
	    } 
        	   
	    if (amountOfScans >= maxIterationsToRun){
	        System.out.println(String.format("NOTICE - THE ALGORITHM HASNT STABLED. IT STOPPED AFTER SCANNING ALL NODES FOR  %1$d TIMES.",maxIterationsToRun));
	    }
	    
	    return metaData.com2nodes;
	}

	private void FindCommsForNodesMultyThreaded(int threads, double betta, Map<Integer, Set<Integer>> OLDnode2comms,
			Map<Integer, Set<Integer>> NEWnode2comms) {
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		for (Integer node : g.nodes()){            
		    Runnable worker = new FindBestCommForNodeWorker(node, betta, metaData,  OLDnode2comms, NEWnode2comms);
		    executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {            	
		}
		System.out.println("			Finished all nodes");
	}
	
	private Map<Integer,Set<Integer>> FindCommunities(double betta) throws FileNotFoundException, UnsupportedEncodingException{
	    int numOfStableNodes = 0;
	    int amountOfScans = 0;
	    int n = g.number_of_nodes();
	    
	    long Sec1Time = 0;
	    long Sec2Time = 0;
	    long Sec3Time = 0;	    
	    
	    while (numOfStableNodes < n && amountOfScans < maxIterationsToRun){
	    	System.out.println("Input: " +pathToGraph + " betta: " + betta + "            Num of iterations: " + amountOfScans);
	    	System.out.println("Number of stable nodes: " + numOfStableNodes);
            numOfStableNodes = 0;
            amountOfScans++;
            
            // Find new comms for each node
	        for (Integer node : g.nodes()){
				////////////////////////////////////Section 1
				startTime = System.currentTimeMillis();
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
            	Sec1Time += (System.currentTimeMillis() - startTime);
	            
	            /////////////////////////////////////////    Section 2
            	startTime = System.currentTimeMillis();
	             
	            Map<Integer[],Double> commsCouplesIntersectionRatio = metaData.SetCommsForNode(node, c_v_new,true);
            	Sec2Time += (System.currentTimeMillis() - startTime);
	            
        		///////////////////////////////////////    Section 3
            	startTime = System.currentTimeMillis();
	            boolean haveMergedComms = false;
	            if(amountOfScans>iteratioNumToStartMerge){
	            	haveMergedComms = FindAndMergeComms(commsCouplesIntersectionRatio);
	            }
	            if (!haveMergedComms && c_v_new.equals(c_v_original)){
	            	numOfStableNodes++;
	            }
	            Sec3Time += (System.currentTimeMillis() - startTime);
	        }
	    }
	        
            
	    if (amountOfScans >= maxIterationsToRun){
	        System.out.println(String.format("NOTICE - THE ALGORITHM HASNT STABLED. IT STOPPED AFTER SCANNING ALL NODES FOR  %1$d TIMES.",maxIterationsToRun));
	    }
	    //2
	    runTimeLog.println(Sec1Time/(1000));
	    //3
	    runTimeLog.println(Sec2Time/(1000));
	    //4
	    runTimeLog.println(Sec3Time/(1000));
	    
	    return metaData.com2nodes;
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
				        double intersectionRatio = (double)metaData.Intersection_c1_c2.get(lowComm).get(highComm)/(double)Math.min(metaData.com2nodes.get(lowComm).size(), metaData.com2nodes.get(highComm).size());
				        Integer[] sortedComms= new Integer[]{lowComm,highComm};
				        commsCouplesIntersectionRatio.put(sortedComms, intersectionRatio);
			    	}
			    }
		    }    

		    return commsCouplesIntersectionRatio;
	    }

	private Set<Integer> Find_Neighbor_Comms(Integer node){
	    Set<Integer>neighborComms = new HashSet<Integer>();
	    for (Integer neighbor : g.neighbors(node)){
	        neighborComms.addAll(metaData.node2coms.get(neighbor));
	    }
    return neighborComms;
    }
	
	private double Calc_Modularity_Improvement(Integer comm, Integer node){
		    return metaData.K_v_c.get(node).get(comm)-metaData.Sigma_c.get(comm)*metaData.K_v.get(node)/(2*metaData.m);
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
	
	private boolean FindAndMergeComms (Map<Integer[],Double> commsCouplesIntersectionRatio){
	    boolean haveMergedComms = false;	    
	    for (Entry<Integer[],Double > c1c2intersectionRate : commsCouplesIntersectionRatio.entrySet()){	    	
	        if(c1c2intersectionRate.getValue()>alpha){
	        	Integer[] c1c2 = c1c2intersectionRate.getKey();
	        	//System.out.println("          MERGED" + c1c2intersectionRate.getValue());
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
	        metaData.Update_Weights_Remove(c1, node);
	        metaData.SymbolicRemoveNodeFromComm(node,c1);
	        if(!copyOfC2.contains(node)){
	        	metaData.Update_Weights_Add(c2, node);
	        	metaData.SymbolicAddNodeToComm(node,c2);
	        }	        
	    }
	}
}

