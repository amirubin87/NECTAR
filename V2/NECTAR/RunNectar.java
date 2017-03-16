package NECTAR;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RunNectar {

	public static void main(String[] args) throws Exception {		
		
		long startTime = System.currentTimeMillis();		
		if (args.length <2){
			System.out.println("Input parameteres for NECTAR: "
					+ "pathToGraph  "
					+ "outputPath  "
					+ "betas=1.1,1.2,2.0,3.0 "
					+ "alpha=0.8  "
					+ "iteratioNumToStartMerge=6  "
					+ "maxIterationsToRun=20 "
					+ "firstPartMode=0(0=CC, 3=clique 3, 4=clique 4) "
					+ "percentageOfStableNodes=95 "
					+ "runMultyThreaded=true "
					+ "numOfThreads=8 "
					+ "dynamicChoose=true "
					+ "useModularity=false "
					+ "debug=false");
		}
		else{
			boolean useModularity = true;
			String pathToGraph = "C:/EclipseWorkspace/NECTAR/network.dat";
			String outputPath = "C:/EclipseWorkspace/NECTAR/Regular/useModularity_" + useModularity +"/";
			double[] betas = {1.2};
			int firstPartMode = 0;
			double alpha = 0.8;
			int iteratioNumToStartMerge = 6;
			int maxIterationsToRun = 20;
			int percentageOfStableNodes = 95;
			boolean runMultyThreaded = false;
			int numOfThreads = 1;
			boolean dynamicChoose = false;
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
			
			if(args.length > 9){
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
			System.out.println("debug mode: "+debug);
			

			System.out.println("");
			
		    File outputDirectory = new File(String.valueOf(outputPath));
		    if (! outputDirectory.exists()){
		    	outputDirectory.mkdirs();
		    }
		    
			if(dynamicChoose){
				double[] graphFeatures = CalcFeatures.processGraph(pathToGraph);
			
				useModularity = DynamicFunctionChoose.shouldUseModularity(graphFeatures);
			}
			
			 
			if(!useModularity){
				System.out.println("                         Using WOCC.");
				NectarW nectarW = new NectarW(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun,percentageOfStableNodes,firstPartMode, debug);				
				nectarW.FindCommunities(runMultyThreaded, numOfThreads);
			}
			else{
				System.out.println("                         Using Modularity.");
				NectarQ nectarQ= new NectarQ(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun, percentageOfStableNodes, debug);								
				nectarQ.FindCommunities(runMultyThreaded, numOfThreads);			
			}
		}
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println(totalTime/1000);
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