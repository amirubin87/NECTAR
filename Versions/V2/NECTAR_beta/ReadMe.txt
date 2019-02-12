Input:
	A list of undirected edges, tab or space delimited.
	
	Example:
	2	1
	3	1
	3	2
	4	1
	4	2
	4	3
	
	Notice that in case the input contains both '1 2' and '2 1' the edge will be taken into account only once. 

Output:
	A list of nodes per community, space delimited.
	Each line stands for a different community.
	Example:
	1 2 3 4
	2 3 4

Parameters:
	Parameters must be supplied in the exact same order as described here.
	Default values are in parenthesis.

		pathToGraph - the path to the input network file. 
		outputPath - an existing directory to which the output files will be written. 
		betas (NECTAR_Q : 1.1,1.2 NECTAR_W : 2.0,3.0) - a list of comma delimited double values, which should be larger than 1.0.
			For each the algorithm will generate an output. Used by the algorithm to choose for a given node to which communities he shell belong. 
		alpha (0.8) - a double value between 0.0 and 1.0. Two communities which share more than alpha of their nodes (divided by the size of the smaller community between the two) will be merged to one. 
		iteratioNumToStartMerge (6) - an integer indicating after how many iteration the algorithm will merge similar communities. 
			Notice that NECTAR_W starts with an initial partition to communities and for small network it is recommended to set this value to a low number. 
		maxIterationsToRun (20) - an integer indicating the maximum amount of iteration of the algorithm.
		firstPartMode (0) - an integer, one of {0,3,4}. Indicating how to build the first partitioning of the network to communities.
			Choose one of the following three three:
				0: First we sort the nodes by their clustering coefficient, then we iterate them by descending order. 
				   For each node, if not placed in a community, we take all neighbors which haven't been placed in a community and join them all to a new community. 
				3: 3-sized non-overlapping cliques are used. 
				4: 4-sized non-overlapping cliques are used.
			Notice that using '3' or '4' may cost with a long initialization time.			
		percentageOfStableNodes (95) - the algorithm will stop when this percentage of nodes have stabled (e.g. haven't changed any community in the last iteration).
		dynamicChoose (true) - when set to true, the algorithm will choose which objective function to optimize, based on the graph at hand.
		UseWOCC (false) - if dynamicChoose is set to false, this parameter controls which objective function to optimize (when false, Modularity is used).
					
Usage examples:
	
	Minimum parameters needed.
		java -jar  NECTAR_refactored ./net.txt ./
			
	NECTAR runs with 10 different beta values. After the third iteration communities which share 70% of their nodes will be merged.
	The algorithm will stop after 30 iterations.
		java -jar NECTAR_refactored ./net.txt ./ 1.1,1.2,1.3,1.4,1.5,2.0,2.5,3.0,3.5,4.0 0.8 3 30
	
	NECTAR runs with 4 different beta values. The first partitioning is all 3-sized cliques. After the forth iteration communities which share 70% of their nodes will be merged.
	The algorithm will stop after 30 iterations or when 99% of the nodes have stabled. 
		java -jar NECTAR_refactored ./net.txt ./ 1.5,3.5,5.0,10.0 0.7 4 30 3 99	
		