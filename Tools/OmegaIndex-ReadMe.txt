A java implementation to calculate the Omega-Index of two given networks partitioning into communities, 
as presented in "Overlapping Community Detection in Networks: The State-of-the-Art and Comparative Study".

Input is two files containing lists of nodes. Each line describes a community.
	Example:
	1 2 3 4
	2 3 4
	
Notice that the highest amount of nodes we support is 3037000499.

In order to use our implementation ,first compile using: 
	"javac OmegaIndex.java"
To run use:
	"java OmegaIndex <pathToFirstPartitioning> <pathToSecondPartitioning> <outputPath>"