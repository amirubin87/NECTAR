package NECTAR_Beta;


// Main class - running the NECTAR algorithm.
public class RunNectar_Joined {

	// For extarnal use - decide if Modularity or WOCC
	public static Boolean ShouldUseModularity(String pathToGraph) throws Exception{
		double[] graphFeatures = CalcFeatures.processGraph(pathToGraph);		
		return DynamicFunctionChoose.shouldUseModularity(graphFeatures);
	}
	
	public static void main(String[] args) throws Exception {			
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
					//+ "runMultyThreaded=false "
					//+ "numOfThreads=8 "
					+ "dynamicChoose=true "
					+ "useModularity=false "
					+ "verboseLevel=0 (0=no output 1=minimal 2=full)"
					);
		}
		else{
			int index = 0;
			String pathToGraph = (args.length > index) ? args[index] : "C:/Temp/WD/Chrome1_merge/Level_4_Entry_95_ParentEntry_49_ParentLine_1_TH_0.11000000000000001_Edges.txt";
			index++;
			String outputPath = (args.length > index) ? args[index] : "C:/Temp/WD/Chrome1_merge/";
			index++;			
			double[] betas =  (args.length > index) ? Utills.ParseDoubleArray(args[index]) : new double[]{1};		
			index++;
			double alpha =  (args.length > index) ? Double.parseDouble(args[index]) : 0.8;
			index++;
			int iteratioNumToStartMerge =  (args.length > index) ? Integer.parseInt(args[index]) : 6;
			index++;
			int maxIterationsToRun =  (args.length > index) ? Integer.parseInt(args[index]) : 20;
			index++;
			int firstPartMode =  (args.length > index) ? Integer.parseInt(args[index]) : 0;
			index++;
			int percentageOfStableNodes =  (args.length > index) ? Integer.parseInt(args[index]) : 95;
			if(percentageOfStableNodes<1 || percentageOfStableNodes>100){throw(new RuntimeException("param at location 7 is percentageOfStableNodes. You gave: " + percentageOfStableNodes +"  which is not <1 or >100."));}
			//index++;
			//boolean runMultyThreaded =  (args.length > index) ? Boolean.parseBoolean(args[index]) : false;
			//index++;
			//int numOfThreads =  (args.length > index) ? Integer.parseInt(args[index]) : 8;
			index++;
			boolean dynamicChoose =  (args.length > index) ? Boolean.parseBoolean(args[index]) : true;
			index++;
			boolean useModularity =  (args.length > index) ? Boolean.parseBoolean(args[index]) : false;
			index++;
			int verboseLevel =  (args.length > index) ? Integer.parseInt(args[index]) : 0;						
			
			if(dynamicChoose){
				useModularity = ShouldUseModularity(pathToGraph);
			}
			boolean useWocc = !useModularity;
			
			String betasString = "";
			for (double d: betas){
				betasString = betasString + d + " ";
			}

			if(verboseLevel>0){				
					System.out.println("pathToGraph:             "+pathToGraph);      
					System.out.println("outputPath:              "+outputPath);
					System.out.println("betas:                   "+betasString);
					System.out.println("alpha:                   "+alpha);
					System.out.println("iteratioNumToStartMerge: "+iteratioNumToStartMerge);
					System.out.println("first Part mode:         "+firstPartMode);
					System.out.println("maxIterationsToRun:      "+maxIterationsToRun);
					System.out.println("percentageOfStableNodes: "+percentageOfStableNodes);
					System.out.println("dynamicChoose:           "+dynamicChoose);
					System.out.println("UseWOCC      :           "+useWocc);				
					System.out.println("");
			}
		    //File outputDirectory = new File(String.valueOf(outputPath));
		    //if (! outputDirectory.exists()){
		    //	outputDirectory.mkdirs();
		    //}
			
			Nectar nectar = new Nectar(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun,percentageOfStableNodes,firstPartMode,dynamicChoose,useWocc,verboseLevel);		
			
			nectar.FindCommunities();
			}
		}
	
}