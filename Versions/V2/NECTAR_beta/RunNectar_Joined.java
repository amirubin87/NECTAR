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
					+ "pathToGraph  MANDATORY"
					+ "outputPath  MANDATORY"
					+ "betas=1 (possibly a list: 1.1,1.2,2.0,3.0) "
					+ "alpha=0.8  "
					+ "iteratioNumToStartMerge=6  "
					+ "maxIterationsToRun=20 "
					+ "firstPartMode=0(0=CC, 3=clique 3, 4=clique 4) "
					+ "percentageOfStableNodes=95 "
					+ "dynamicChoose=false "
					+ "useModularity=false "
					+ "useWOCC=false "					
					+ "useConductance=true "
					+ "verboseLevel=2 (0=no output 1=minimal 2=full)"
					);
		}
		else{
			int index = 0;
			String pathToGraph = (args.length > index) ? args[index] : "C:/Temp/test.txt";
			index++;
			String outputPath = (args.length > index) ? args[index] : "C:/Temp/";//"C:/Temp/WD/Chrome1_merge/";
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
			index++;
			boolean dynamicChoose =  (args.length > index) ? Boolean.parseBoolean(args[index]) : false;
			index++;
			boolean useModularity =  (args.length > index) ? Boolean.parseBoolean(args[index]) : false;
			index++;
			boolean useWOCC =  (args.length > index) ? Boolean.parseBoolean(args[index]) : true;
			index++;
			boolean useConductance =  (args.length > index) ? Boolean.parseBoolean(args[index]) : false;
			index++;
			int verboseLevel =  (args.length > index) ? Integer.parseInt(args[index]) : 2;						
			
			if(!dynamicChoose & ((useModularity & useWOCC) | (useModularity & useConductance) | (useWOCC & useConductance)) ){
				throw new RuntimeException("Only one of @useModularity, @useConductance, @useWOCC can be true if @dynamicChoose is false.");
			}
			
			if(dynamicChoose){
				useConductance = false;
				useModularity = ShouldUseModularity(pathToGraph);
				useWOCC = !useModularity;
			}
			
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
					System.out.println("useModularity:           "+useModularity);				
					System.out.println("useWOCC:                 "+useWOCC);
					System.out.println("useConductance:          "+useConductance);
					System.out.println("");
			}
		    //File outputDirectory = new File(String.valueOf(outputPath));
		    //if (! outputDirectory.exists()){
		    //	outputDirectory.mkdirs();
		    //}
			
			Nectar nectar = new Nectar(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun,percentageOfStableNodes,firstPartMode,dynamicChoose,useModularity,useWOCC,useConductance,verboseLevel);		
			
			nectar.FindCommunities();
			}
		}
	
}