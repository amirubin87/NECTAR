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
			String outputPath = "C:/Users/t-amirub/OneDrive/PhD/Data/Genes/output/NECTAR_W/" + th +"_";
			
			
			double[] betas = {1.1,1.2};				
			double alpha = 0.5;
			int iteratioNumToStartMerge = 3;
			int maxIterationsToRun = 20;
			int firstPartMode = 1;
			int bulkSize = 50;
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
				Set<Integer> newNodes = new HashSet<Integer>();
				Set<Integer> nodesSeen = new HashSet<Integer>();
				// Read all edegs, sort from the srongest down.
				TreeSet<Edge> AllEdgesSorted = GetSortedListOfEdges(pathToGraph);
				int bulkNumber = 0;
				//TODO remove first cond
				while (bulkNumber < 5 & AllEdgesSorted.size() >0){
					bulkNumber++;					
					// In the first run, use bulkSize*3 number of edges
					TreeSet<Edge> EdgesToAddForNectar = GetEdgesToAddForNectar(EdgesForNectar.size() == 0,AllEdgesSorted, bulkSize); 
					EdgesForNectar.addAll(EdgesToAddForNectar);
					for (Edge e : EdgesToAddForNectar){
						newNodes.add(e.getFrom());
						newNodes.add(e.getTo());
					}
					
					Map<Integer, Set<Integer>> partitionForNectar = GetPartitionForNectar(CommunitesFromNectar, newNodes);
					nodesSeen.addAll(newNodes);
					// Run NECTAR, and keep the communities it finds.
					
					System.out.println("Bulk number " + bulkNumber);
					System.out.println("newNodes " + newNodes.size());
					System.out.println("EdgesForNectar " + EdgesForNectar.size());
					System.out.println("nodesSeen " + nodesSeen.size());
					System.out.println("partitionForNectar " + partitionForNectar);
					// TODO:
					// a. support partitionForNectar in Nectar
					// b. deal with merging comms..
					if(!useModularity){
						System.out.println("                         Using WOCC.");
						WeightedNectarWOCC nectarW = new WeightedNectarWOCC(pathToGraph, EdgesForNectar,betas,alpha,outputPath + "BULK_" + bulkNumber + "_", iteratioNumToStartMerge, maxIterationsToRun,percentageOfStableNodes,firstPartMode, debug);			
						
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

	private static Map<Integer, Set<Integer>> GetPartitionForNectar(Map<Integer, Set<Integer>> communitesFromNectar,
			Set<Integer> newNodes) {
		
		//first run - Nectar will do this on his own.
		if(communitesFromNectar == null){ return null;}
		else{
			Map<Integer, Set<Integer>> partitionForNectar = communitesFromNectar;
			// TODO - is there a smarter way? maybe we can control what commID nectar returns?
			for (int i = 0 ; i<partitionForNectar.size()+1 ; i++){
				if(partitionForNectar.get(new Integer(i)) == null){
					partitionForNectar.put(new Integer(i),newNodes);
					break;
				}
			}
			return partitionForNectar;
		}		
	}

	private static TreeSet<Edge> GetEdgesToAddForNectar(boolean isFirstBulk, TreeSet<Edge> allEdgesSorted, int bulkSize) {
		TreeSet<Edge> newEdges = null;
		//First run
		if(isFirstBulk){
			newEdges = PullBulkLastElements(allEdgesSorted, bulkSize*3);			
		}
		else{
			newEdges = PullBulkLastElements(allEdgesSorted, bulkSize);
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