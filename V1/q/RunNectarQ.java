package q;
import java.io.IOException;

public class RunNectarQ {
	

	public static void main(String[] args) throws IOException {		
		if (false&& args.length <2){
			System.out.println("Input parameteres for NECTAR: pathToGraph  outputPath  betas={1.1,1.2}  alpha=0.8  iteratioNumToStartMerge=6  maxIterationsToRun=20");
		}
		else{
			String pathToGraph = "C:/Users/t-amirub/Desktop/net/b-om2-10-1/net.txt";
			String outputPath = "C:/Users/t-amirub/Desktop/net/b-om2-10-1/Qnet.txt";
			double[] betas = {1.1,1.2,2.0,3.0};
			double alpha = 0.8;
			int iteratioNumToStartMerge = 6;
			int maxIterationsToRun = 20;
			
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
			
			String betasString = "";
			for (double d: betas){
				betasString = betasString + d + " ";
			}
			
			System.out.println("pathToGraph:             "+pathToGraph);      
			System.out.println("outputPath:              "+outputPath);
			System.out.println("betas:                   "+betasString);
			System.out.println("alpha:                   "+alpha);
			System.out.println("iteratioNumToStartMerge: "+iteratioNumToStartMerge);
			System.out.println("maxIterationsToRun:      "+maxIterationsToRun);
			System.out.println("");
			NectarQ nectar= new NectarQ(pathToGraph,betas,alpha,outputPath, iteratioNumToStartMerge, maxIterationsToRun);
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
