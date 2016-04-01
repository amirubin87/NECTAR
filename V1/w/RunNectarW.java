package w;
import java.io.IOException;

public class RunNectarW {

	public static void main(String[] args) throws IOException {		
		if (args.length <2){
			System.out.println("Input parameteres for NECTAR: pathToGraph  outputPath  betas={2.0,3.0}  alpha=0.8  iteratioNumToStartMerge=6  maxIterationsToRun=20 firstPartMode=0(0=CC, 3=clique 3, 4=clique 4) percentageOfStableNodes=95 ");
		}
		else{
			String pathToGraph = "";
			String outputPath = "";
			double[] betas = {2.0,3.0};
			int firstPartMode = 0;
			double alpha = 0.8;
			int iteratioNumToStartMerge = 6;
			int maxIterationsToRun = 20;
			int percentageOfStableNodes = 95;
			String partitionFile = "";
			boolean partitionIsFromFile = false;
			
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
				partitionFile = args[8];
				partitionIsFromFile = true;
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
			if(partitionIsFromFile){
				System.out.println("partitionFile:           "+ partitionFile);
			}
			System.out.println("");
			
			NectarW nectar = null;
			if (partitionIsFromFile){
				nectar = new NectarW(pathToGraph, partitionFile, betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun, percentageOfStableNodes);
			}
			else{
				nectar = new NectarW(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun,percentageOfStableNodes,firstPartMode);
			}
			
			nectar.FindCommunities();
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
}