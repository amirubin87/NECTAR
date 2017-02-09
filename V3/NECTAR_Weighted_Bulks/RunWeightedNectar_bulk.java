package NECTAR_Weighted_Bulks;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import NECTAR_Weighted_Bulks.*;
public class RunWeightedNectar_bulk {

/*
 * Notes.
 * Bulk size is getting bigger in every step
 * we only output communities larger than 10
 * 
 */
	public static void main(String[] args) throws Exception {		
		
		long startTime = System.currentTimeMillis();
		
		if (false & args.length <2){
			System.out.println("Input parameteres for NECTAR weighted: "
					+ "pathToGraph  "
					+ "outputPath  "
					+ "betas=1.1,1.2,2.0,3.0  "
					+ "alpha=0.8  "
					+ "iteratioNumToStartMerge=6  "
					+ "maxIterationsToRun=20 "
					+ "firstPartMode=0(0=CC, 3=clique 3, 4=clique 4) "
					+ "percentageOfStableNodes=95 "
					+ "runMultyThreaded=true "
					+ "numOfThreads=8 "
					+ "dynamicChoose=true "
					+ "useModularity=false "
					+ "debug=false"
					+ "bulkSize=50");
		}
		else{
			String th = "0.18";
			String pathToGraph = "C:/Users/t-amirub/OneDrive/PhD/Data/Genes/" + th +"edgeList.txt";
			String outputPath = "C:/Users/t-amirub/OneDrive/PhD/Data/Genes/output/NECTAR_W_BULK/" + th +"/";
			
			
			double[] betas = {1.3};				
			double alpha = 0.5;
			int iteratioNumToStartMerge = 3;
			int maxIterationsToRun = 20;
			int firstPartMode = 1;
			int bulkSize = 150;
			int bulkSizeIncreaseSize = 5;
			int percentageOfStableNodes = 95;
			boolean runMultyThreaded = false;
			int numOfThreads = 1;
			boolean dynamicChoose = false;   
			boolean useModularity = false;
			boolean debug = false;
			
			if (args.length > 0)
				pathToGraph = args[0];		
			
			if (args.length > 1)
				outputPath = args[1];		
			
			if (args.length > 2)
				betas = ParseDoubleArray(args[2]);		
				
			if (args.length > 3)
				 alpha = Double.parseDouble(args[3]);
			
			if ( args.length > 4)
				 iteratioNumToStartMerge = Integer.parseInt(args[4]);
			
			if ( args.length > 5)
				maxIterationsToRun = Integer.parseInt(args[5]);
			
			if ( args.length > 6)
				firstPartMode = Integer.parseInt(args[6]);
			
			if(args.length > 7){
				percentageOfStableNodes = Integer.parseInt(args[7]);
				if(percentageOfStableNodes<1 || percentageOfStableNodes>100){
					throw(new RuntimeException("param at location 7 is percentageOfStableNodes. You gave: " + percentageOfStableNodes +"  which is not <1 or >100."));
				}
			}
			
			if(args.length > 8){
				runMultyThreaded = Boolean.parseBoolean(args[8]);				
			}
			
			if(runMultyThreaded & args.length > 9){
				numOfThreads = Integer.parseInt(args[9]);				
			}
			
			
			if(args.length > 10){
				dynamicChoose = Boolean.parseBoolean(args[10]);				
			}
			
			if(args.length > 11){
				useModularity = Boolean.parseBoolean(args[11]);				
			}
			
			if(args.length > 12){
				debug = Boolean.parseBoolean(args[12]);				
			}
			
			if(args.length > 13){
				bulkSize = Integer.parseInt(args[13]);				
			}
			
			
			
			String betasString = "";
			for (double d: betas){
				betasString = betasString + d + " ";
			}
			
			System.out.println("pathToGraph:              "+pathToGraph);      
			System.out.println("outputPath:               "+outputPath);			
			System.out.println("betas:                    "+betasString);
			System.out.println("alpha:                    "+alpha);
			System.out.println("iteratioNumToStartMerge:  "+iteratioNumToStartMerge);
			System.out.println("first Part mode:          "+firstPartMode);
			System.out.println("maxIterationsToRun:       "+maxIterationsToRun);
			System.out.println("percentageOfStableNodes:  "+percentageOfStableNodes);
			System.out.println("runMultyThreaded:         "+runMultyThreaded);
			System.out.println("numOfThreads:             "+numOfThreads);
			System.out.println("dynamicChoose:            "+dynamicChoose);
			System.out.println("useModularity:            "+useModularity);
			System.out.println("debug mode:               "+debug);
			System.out.println("bulkSize:                 "+bulkSize);
			

			System.out.println("");
						
			if(dynamicChoose){
				double[] graphFeatures = CalcFeatures.processGraph(pathToGraph);
			
				useModularity = DynamicFunctionChoose.shouldUseModularity(graphFeatures);
			}
			
			for ( double betta : betas){
				TreeSet<Edge> EdgesForNectar = new TreeSet<Edge>();
				Map<Integer, Set<Integer>> CommunitesFromNectar = null;
				Set<Integer> relevantNodes = new HashSet<Integer>();
				Set<Integer> nodesSeen = new HashSet<Integer>();
				// Read all edegs, sort from the srongest down.
				TreeSet<Edge> AllEdgesSorted = GetSortedListOfEdges(pathToGraph);
				int bulkNumber = 0;
				
				while ((nodesSeen.size()< 550 
						| (nodesSeen.size()== 550 & (CommunitesFromNectar == null || CommunitesFromNectar.size()>3)))
										& AllEdgesSorted.size() >0){
					bulkNumber++;	
					relevantNodes = new HashSet<Integer>();
					// Get the edges to add for the current bulk.
					// (In the first run, use bulkSize*3 number of edges)
					TreeSet<Edge> EdgesToAddForNectar = GetEdgesToAddForNectar(EdgesForNectar.size() == 0,AllEdgesSorted, bulkSize); 
					bulkSize = bulkSize + bulkSizeIncreaseSize;
					EdgesForNectar.addAll(EdgesToAddForNectar);
					
					// Get the nodes relavant to the new edges
					for (Edge e : EdgesToAddForNectar){
						relevantNodes.add(e.getFrom());
						relevantNodes.add(e.getTo());
					}
					nodesSeen.addAll(relevantNodes);
					
					// The new nodes are to be added, but may belong to different comms, so we partition them.	
					// We do it based on all the edges between them.
					Map<Integer, Set<Integer>> partitionOfNewNodes =  GetPartitionOfNewNodes(EdgesForNectar,relevantNodes,betta);
					
					// Use the partition NECTAR found so far. Add all nodes relevant to the new edges in a new community.
					Map<Integer, Set<Integer>> partitionForNectar = GetPartitionForNectar(CommunitesFromNectar, partitionOfNewNodes);
					
					
					// Run NECTAR, and keep the communities it finds.
					
					System.out.println("Bulk number                " + bulkNumber);
					System.out.println("relevantNodes to this bulk " + relevantNodes.size());
					System.out.println("EdgesForNectar in total    " + EdgesForNectar.size());
					System.out.println("nodesSeen in total         " + nodesSeen.size());					
					System.out.println("CommunitesFromNectar       " + (CommunitesFromNectar!=null ? CommunitesFromNectar.size() : 0));					
					// TODO:
					// a. support partitionForNectar in Nectar
					// b. deal with merging comms..
					if(!useModularity){
						System.out.println("                         Using WOCC.");
						WeightedNectarWOCC_bulk nectarW = new WeightedNectarWOCC_bulk(pathToGraph, partitionForNectar, EdgesForNectar,betas,alpha,outputPath + "BULK_" + bulkNumber + "_", iteratioNumToStartMerge, maxIterationsToRun,percentageOfStableNodes,firstPartMode, debug);			
						
						CommunitesFromNectar = nectarW.FindCommunities(runMultyThreaded, numOfThreads,betta);					
					}
					else{
						System.out.println("                         Using Modularity. TODO, not supported yet");
						//WeightedNectarQ nectarQ= new WeightedNectarQ(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun, percentageOfStableNodes, debug);
						//CommunitesFromNectar = nectarQ.FindCommunities(runMultyThreaded, numOfThreads);			
					}
				}
			
			}
		}
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println(totalTime/1000);
	}

	private static Map<Integer, Set<Integer>> GetPartitionOfNewNodes(TreeSet<Edge> edgesForNectar,
			Set<Integer> relevantNodes, double betta) throws IOException {		
		// Find relevant edges
		TreeSet<Edge> egdesInNodes = new TreeSet<Edge>();
		for ( Edge e : edgesForNectar){
			if (relevantNodes.contains(e.getFrom()) && relevantNodes.contains(e.getTo())){
				egdesInNodes.add(e);
			}
		}
		// Build graph
		UndirectedWeightedGraphWOCC graphWithNodes = new UndirectedWeightedGraphWOCC(egdesInNodes);
		// TODO refactor and move this method to the right place.		
		return WeightedNectarWOCC_bulk.GetFirstPartitionDenseGraph(graphWithNodes,betta);
	}

	private static Map<Integer, Set<Integer>> GetPartitionForNectar(Map<Integer, Set<Integer>> communitesFromNectar,
			Map<Integer, Set<Integer>> partitionOfNewNodes) {
		
		//first run - Nectar will do this on his own.
		if(communitesFromNectar == null){ return null;}
		else{
			boolean havePlacedNewNodes = false;
			Integer maxCommId = new Integer(-1);
			Map<Integer, Set<Integer>> partitionForNectar = new HashMap<Integer, Set<Integer>>();
			// Go over all comms. Take only non empty
			for (Integer i : communitesFromNectar.keySet()){
				Set<Integer> comm = communitesFromNectar.get(i);
				if(comm != null && comm.size()!=0){					
					partitionForNectar.put(i,comm);
					maxCommId=Math.max(i, maxCommId);
				}
			}	
			// We place the new nodes in maxCommId+1,maxCommId+2 ... 
			//TODO there is probably a better way to find a free place..
			Integer c = new Integer(maxCommId+1);
			for (Set<Integer> newComm : partitionOfNewNodes.values()){			
				partitionForNectar.put(c,newComm);
				c = new Integer(c+1);
			}
			
			return partitionForNectar;
		}		
	}

	private static TreeSet<Edge> GetEdgesToAddForNectar(boolean isFirstBulk, TreeSet<Edge> allEdgesSorted, int bulkSize) {
		TreeSet<Edge> newEdges = null;
		//First run
		if(isFirstBulk){
			newEdges = PullBulkLastElements(allEdgesSorted, Math.min(bulkSize*3,allEdgesSorted.size()));			
		}
		else{
			newEdges = PullBulkLastElements(allEdgesSorted, Math.min(bulkSize,allEdgesSorted.size()));
		}
		
		return newEdges;
	}

	private static TreeSet<Edge> PullBulkLastElements(TreeSet<Edge> allEdgesSorted, int bulkSize) {
		TreeSet<Edge> ans = new TreeSet<Edge>();
		for ( int i = 0 ; i < bulkSize ; i++){
			ans.add(allEdgesSorted.pollLast());
		}
		
		return ans;
	}

	private static TreeSet<Edge> GetSortedListOfEdges(String pathToGraph) throws IOException {
		TreeSet<Edge> ans = new TreeSet<Edge>();
		Path path = Paths.get(pathToGraph);
		List<String> lines= Files.readAllLines(path,StandardCharsets.UTF_8);
		System.out.println("All graph lines read.");
		
		for (String line : lines){
			
			String[] parts = line.split(" |\t");
			if(parts.length >1){
				Integer v = Integer.parseInt(parts[0].trim());
				Integer u = Integer.parseInt(parts[1].trim());
				Double w = 1.0;			
				if(parts.length>2) w = Double.parseDouble(parts[2].trim());
				Edge e = new Edge(v,u,w);
				ans.add(e);
			}
		}
		return ans;
	}

	private static double[] ParseDoubleArray(String string) {
		String[] parts = string.split(",");
		double[] ans= new double[parts.length];
	    int i=0;
	    for(String str:parts){
	    	ans[i]=Double.parseDouble(str);
	        i++;
	    }
		return ans;
	}
	
	
	public static Map<Object,Set<Object>> buildAdjacencyMap(List<Edge> edges){
		if ((edges==null) || (edges.isEmpty())){
			return Collections.<Object,Set<Object>>emptyMap();
		}
		
		Map<Object,Set<Object>> graph = new HashMap<>();
		for (Edge e : edges){
			if (!graph.containsKey(e.getFrom())){
				graph.put(e.getFrom(), new HashSet<Object>());
			}
			if (!graph.containsKey(e.getTo())){
				graph.put(e.getTo(), new HashSet<Object>());
			}
			graph.get(e.getFrom()).add(e.getTo());
			graph.get(e.getTo()).add(e.getFrom());
		}
		
		return graph;
	}
	
}