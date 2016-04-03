import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public class OmegaIndex {
	static long M=0;

	public static void main(String[] args) throws IOException {
		if (args.length <3){
			System.out.println("Input parameteres for OmegaIndex: pathToFirstPartitioning  pathToSecondPartitioning outputPath");			
		}
		else{
			CalcOmegaIndex(args[0],args[1],args[2]);
		}
	}
	
    /*Goes over all pairs of nodes.
    Counts shared comms in c1, and in c2.
    Increments by 1 the count for these values.
    :param Node2Comm1: mapping of nodes to comms.
    :param Node2Comm2: mapping of nodes to comms.
    :return: A dictionary - keys are amounts of couples of nodes.
            Values are an array of three: amount in C1, amount in C2, amount of shared couples.
            So if C1 has only 1 couple of nodes and they share 2 comms, and another 2 couples with 0 shared comms,
            and C2 has 2 couples of nodes with 2 shared comms and another single couple with 0 shared comms,
            but only one couple got in both 0 shared comms, we will have:
            0:[2,2,1], 1:[0,0,0], 2:[1,2,0]
    */
	public static Map<Integer, long[]> AmountOfSharedCommsToAmountOfPairsOfNodesV2
		(Map<Integer,ArrayList<Integer>> Node2Comm1, Map<Integer,ArrayList<Integer>> Node2Comm2, Map<Integer,ArrayList<Integer>> comm2Nodes1, Map<Integer,ArrayList<Integer>> comm2Nodes2, Set<Integer> nodes){	    

		Map<Integer,long[]> ans = new HashMap<Integer,long[]>();
				
		long totalCouplesCovered=0;		
		ArrayList<Integer> vComms1;
		ArrayList<Integer> vComms2;
		Set<Integer> nodesReleventToV;
		Set<Integer> FilteredNodesReleventToV;
		ArrayList<Integer> uComms1;
		ArrayList<Integer> uComms2;
		
		for (int v :nodes){
			//get comms of v in both C1 and C2
			vComms1 = Node2Comm1.get(v);
			if (vComms1 == null){
				vComms1 =new ArrayList<Integer>();
			}
			vComms2 = Node2Comm2.get(v);
			if (vComms2 == null){
				vComms2 =new ArrayList<Integer>();
			}		        
		
			nodesReleventToV = new HashSet<Integer>();
	
	        for (int comm :vComms1){
	            nodesReleventToV.addAll(new HashSet<Integer>(comm2Nodes1.get(comm)));
	        }	        
	        	             
	        for (int comm :vComms2){
	            nodesReleventToV.addAll(new HashSet<Integer>(comm2Nodes2.get(comm)));
	        }
	        
	        FilteredNodesReleventToV = new HashSet<Integer>();
	        for (int u :nodesReleventToV){
	        	if(u>v){
	        		FilteredNodesReleventToV.add(u);
	        	}
	        }
	        
	        totalCouplesCovered = totalCouplesCovered + FilteredNodesReleventToV.size();
	        
	        for (int u : FilteredNodesReleventToV){
	            // get comms of u in both C1 and C2
	            uComms1 = Node2Comm1.get(u);
	            if (uComms1 == null){
	            	uComms1 =new ArrayList<Integer>();
				}
	            uComms2 = Node2Comm2.get(u);
	            if (uComms2 == null){
	            	uComms2 =new ArrayList<Integer>();
				}
	            // check how many shared comms v and u have in C1, increment by 1 the count of it
	            int amountOfSharedComms1 = IntersectionSize(vComms1,uComms1);	            
	            if (ans.get(amountOfSharedComms1) == null){
	            	long[] zeroes= { 0L,0L,0L};            	            
	                ans.put(amountOfSharedComms1, zeroes);
	            }            	            
	            ans.get(amountOfSharedComms1)[0] = ans.get(amountOfSharedComms1)[0] + 1;   
	            
	            // check how many shared comms v and u have in C2, increment by 1 the count of it
	            int amountOfSharedComms2 = IntersectionSize(vComms2,uComms2);
	            if (ans.get(amountOfSharedComms2) == null){
	            	long[] zeroes= { 0L,0L,0L};            	            
	                ans.put(amountOfSharedComms2, zeroes);
	            }            	            
	            ans.get(amountOfSharedComms2)[1] = ans.get(amountOfSharedComms2)[1] + 1;	            
	            
	            // if they share the same amount of nodes in C1 and C2, increment by 1 the count of it.
	            if (amountOfSharedComms1 == amountOfSharedComms2){
	            	ans.get(amountOfSharedComms2)[2] = ans.get(amountOfSharedComms2)[2]+ 1;	            	
	            	}
	        }
		}
	    
		// 	deal with the nodes which doesnt have any shared comms with v
	    if (ans.get(0) == null){
	    	long[] zeroes= { 0L,0L,0L};             	            
            ans.put(0, zeroes);
	    }    
		long couplesNotCovered = M - totalCouplesCovered;		
		ans.get(0)[0] = ans.get(0)[0]+ couplesNotCovered;
		ans.get(0)[1] = ans.get(0)[1]+ couplesNotCovered;
		ans.get(0)[2] = ans.get(0)[2]+ couplesNotCovered;
		
        return ans;
	}

	/*counts the amount of elements in the intersection of two collections
	param collection1: first collection
	param collection2: second collection
	return: the amount of elements in the intersection of the two collections*/
	public static <T> int IntersectionSize(Collection<T> collection1,Collection<T> collection2){
		int ans = 0;
		Collection<T> small = collection1;
		Collection<T> big = collection2;		
		if (collection1.size() > collection2.size()){
			small = collection2;
			big = collection1;
		}		
		for (T comm : small){
			if (big.contains(comm)){
				ans ++;
			}
		}
		return ans;
	}
	
	public static void ListOfNodesToMappings
	(String listsOfNodesPath, Map<Integer,ArrayList<Integer>> Node2Comms,  Map<Integer,ArrayList<Integer>> Comm2Nodes) throws IOException{
		String[] listsOfNodes = (Files.readAllLines(Paths.get(listsOfNodesPath))).toArray(new String[0]);
        int commId = -1;
        String comm = "";
        String[] nodes;
        ArrayList<Integer> nodesInt;
        ArrayList<Integer> nodeComms;
        for(int i = 0; i<listsOfNodes.length ; i++){
            commId ++;
            comm = listsOfNodes[i];
            nodes = comm.replace("\t", " ").split(" ");
            nodesInt = new ArrayList<Integer>();
            ConvertListOfStringToListOfInt(nodes, nodesInt);            		
            Comm2Nodes.put(commId, nodesInt);            
            for (int node : nodesInt){
            	nodeComms = Node2Comms.get(node);
            	if ( nodeComms == null){
            		nodeComms = new ArrayList<Integer>();
            		Node2Comms.put(node, nodeComms);
            	}
            	nodeComms.add(commId);
            }
        }
	}
        
    private static void ConvertListOfStringToListOfInt(String[] nodes, ArrayList<Integer> nodesInt) {
		for (String node: nodes){
			if(node.length()==0){
				continue;
			}
			nodesInt.add(Integer.parseInt(node));
		}
	}

	/// Calcs Omega index as in "Overlapping Community Detection in Networks: The State-of-the-Art and Comparative Study"    	
	public static void CalcOmegaIndex(String listOfNodes1Path, String listOfNodes2Path, String outputPath) throws IOException{    
		
		Map<Integer,ArrayList<Integer>> Node2Comm1 = new HashMap<Integer,ArrayList<Integer>>();
		Map<Integer,ArrayList<Integer>> Comm2Nodes1 = new HashMap<Integer,ArrayList<Integer>>();			
		ListOfNodesToMappings(listOfNodes1Path, Node2Comm1, Comm2Nodes1);

		Map<Integer,ArrayList<Integer>> Node2Comm2 = new HashMap<Integer,ArrayList<Integer>>();
		Map<Integer,ArrayList<Integer>> Comm2Nodes2 = new HashMap<Integer,ArrayList<Integer>>();	
		ListOfNodesToMappings(listOfNodes2Path, Node2Comm2, Comm2Nodes2);

		
		//amount of nodes pairs
		Set<Integer> nodes = new HashSet<Integer>();
		for(ArrayList<Integer> cNodes:Comm2Nodes1.values()){
			nodes.addAll(new HashSet<>(cNodes));
		}
		for(ArrayList<Integer> cNodes:Comm2Nodes2.values()){
			nodes.addAll(new HashSet<>(cNodes));
		}
		long n = nodes.size();
		
		System.out.println("n: "+n);
		if( n >= new Long("3037000499")){
			throw new RuntimeException("Networks with more than 3037000499 nodes are not supported.");
		}
		
		M=n*(n-1)/2; 
		
		Map<Integer, long[]> amountsOfNodesWithSameAmountOfComms =  
				AmountOfSharedCommsToAmountOfPairsOfNodesV2(Node2Comm1,Node2Comm2, Comm2Nodes1, Comm2Nodes2, nodes);
		//We and Wu
		long sumUnadjustedOmegaIndex = 0L;
		BigDecimal Wu = new BigDecimal("0");
		BigDecimal sumExpectedOmegaIndex = new BigDecimal ("0");
		BigDecimal We = new BigDecimal("0");
		for (long[]val : amountsOfNodesWithSameAmountOfComms.values()){
		  	sumUnadjustedOmegaIndex = sumUnadjustedOmegaIndex + val[2];
		  	sumExpectedOmegaIndex = sumExpectedOmegaIndex.add(new BigDecimal(val[0]).multiply(new BigDecimal( val[1])));
		}
		
		Wu = ((new BigDecimal(sumUnadjustedOmegaIndex)).divide(new BigDecimal(M),20, BigDecimal.ROUND_DOWN));
		
		We =  (sumExpectedOmegaIndex.divide((new BigDecimal(M).pow(2)),20, BigDecimal.ROUND_DOWN));
		PrintWriter writer = new PrintWriter(outputPath, "UTF-8");		
		BigDecimal ans = new BigDecimal("0");	
		
		if(!We.equals(BigDecimal.ONE)){
			ans = ((Wu.subtract(We)).divide((BigDecimal.ONE.subtract(We)),20,BigDecimal.ROUND_UP));
		}	
		writer.println("OmegaIndex:\t" + ans);
		writer.close();		
		System.out.println(ans);
     }
}
