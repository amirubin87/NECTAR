package NECTAR;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunNectar {

	public static void main(String[] args) throws IOException, InterruptedException {		
		if (args.length <2){
			System.out.println("Input parameteres for NECTAR: pathToGraph  outputPath  betas={1.1,1.2,2.0,3.0}  alpha=0.8  iteratioNumToStartMerge=6  maxIterationsToRun=20 firstPartMode=0(0=CC, 3=clique 3, 4=clique 4) percentageOfStableNodes=95 runMultyThreaded=false numOfThreads=5 dynamicChoose=true useModularity=false");
		}
		else{
			String pathToGraph = "";
			String outputPath = "";
			double[] betas = {1.1,1.2,2.0,3.0};				
			double alpha = 0.8;
			int iteratioNumToStartMerge = 6;
			int maxIterationsToRun = 20;
			int firstPartMode = 0;
			int percentageOfStableNodes = 95;
			boolean runMultyThreaded = false;
			int numOfThreads = 5;
			boolean dynamicChoose = true;   
			boolean useModularity = false;
			
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
			
			if(args.length > 9){
				numOfThreads = Integer.parseInt(args[9]);				
			}
			
			
			if(args.length > 10){
				dynamicChoose = Boolean.parseBoolean(args[10]);				
			}
			
			if(args.length > 11){
				useModularity = Boolean.parseBoolean(args[11]);				
			}
			
			String betasString = "";
			for (double d: betas){
				betasString = betasString + d + " ";
			}
			
			System.out.println("pathToGraph:             "+pathToGraph);      
			System.out.println("outputPath:              "+outputPath);
			System.out.println("betas:                   "+betasString);
			System.out.println("alpha:                   "+alpha);
			System.out.println("iteratioNumToStartMerge: "+iteratioNumToStartMerge);
			System.out.println("first Part mode:      "+firstPartMode);
			System.out.println("maxIterationsToRun:      "+maxIterationsToRun);
			System.out.println("percentageOfStableNodes: "+percentageOfStableNodes);
			System.out.println("runMultyThreaded: "+runMultyThreaded);
			System.out.println("numOfThreads: "+numOfThreads);
			System.out.println("dynamicChoose: "+dynamicChoose);
			System.out.println("useModularity: "+useModularity);

			System.out.println("");
			List<Edge> edges = GetEdgesList(pathToGraph);
			double trianglesRate = TrianglesRate(edges);
			
			WriteToFile("./Triangles.txt", pathToGraph + " " + trianglesRate);
			
			if(!dynamicChoose & useModularity){
				trianglesRate = 1;
			}
			
			if(!dynamicChoose & !useModularity){
				trianglesRate = 10;
			}
			 
			if(trianglesRate > 5){
				System.out.println("                         Using WOCC.");
				NectarW nectarW = new NectarW(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun,percentageOfStableNodes,firstPartMode);				
				nectarW.FindCommunities(runMultyThreaded);
			}
			else{
				System.out.println("                         Using Modularity.");
				NectarQ nectarQ= new NectarQ(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun);
				nectarQ.FindCommunities(runMultyThreaded, numOfThreads);			
			}
		}
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
	
	private static List<Edge> GetEdgesList(String pathToGraph) throws IOException {
		List<String> lines= Files.readAllLines(Paths.get(pathToGraph));
		List<Edge> edges= new ArrayList<>();
		
		for (String line : lines){
			String[] parts = line.split(" |\t");
			Integer v = Integer.parseInt(parts[0].trim());
			Integer u = Integer.parseInt(parts[1].trim());
			edges.add(new Edge(v, u));
		}
		return edges;
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
	
	private static double TrianglesRate(List<Edge> edges){
		Map<Object,Set<Object>> graph = buildAdjacencyMap(edges);
		
		int triangles = 0;
		int nodes = 0;
		for (Set<Object> neighbors : graph.values()){
			nodes++;
			for (Object v2 : neighbors){
				for (Object v3 : neighbors){
					if ((!v2.equals(v3))){						
						 if (graph.get(v2).contains(v3)){
							 triangles++;
						 }
					}
				}
			}
		}		
		return (double)(triangles/6)/(double)nodes;
	}
	
	private static void WriteToFile(String path, String msg) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));				
		out.println(msg);
		out.close();	
	}
}