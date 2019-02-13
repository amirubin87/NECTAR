import java.io.FileInputStream;
import java.util.ArrayList;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class DynamicFunctionChoose {
	
	public static boolean shouldUseModularity(double[] features) throws Exception {		
		Classifier cls = 
				(Classifier)(SerializationHelper.read(new FileInputStream("./lib/NECTARRandomTree.model")));
		/*
		    FYI, this is the model:
		    RandomTree
			==========
			
			avergaeTrianglesRate < 4.79
			|   ratioOfNodesInTriangles < 0.96
			|   |   ratioOfNodesInTriangles < 0.95
			|   |   |   ACC < 0.35 : 0 (71.83/0.02)
			|   |   |   ACC >= 0.35 : 0 (6.46/0.29)
			|   |   ratioOfNodesInTriangles >= 0.95
			|   |   |   ACC < 0.29 : 0 (2.7/0.03)
			|   |   |   ACC >= 0.29 : 1 (0.79/0.05)
			|   ratioOfNodesInTriangles >= 0.96
			|   |   ratioOfNodesInTriangles < 1
			|   |   |   GCC < 0.23 : 1 (11.26/1.13)
			|   |   |   GCC >= 0.23 : 0 (1.1/0.53)
			|   |   ratioOfNodesInTriangles >= 1 : 1 (3.71/-0)
			avergaeTrianglesRate >= 4.79
			|   ACC < 0.45
			|   |   avergaeTrianglesRate < 7.57
			|   |   |   averageDegree < 17.54 : 0 (0.26/0.05)
			|   |   |   averageDegree >= 17.54 : 1 (6.53/0.6)
			|   |   avergaeTrianglesRate >= 7.57
			|   |   |   ACC < 0.29 : 1 (22.1/0.94)
			|   |   |   ACC >= 0.29 : 1 (50.18/0.03)
			|   ACC >= 0.45
			|   |   ratioOfNodesInTriangles < 1
			|   |   |   avergaeTrianglesRate < 6.92 : 1 (2.96/0.04)
			|   |   |   avergaeTrianglesRate >= 6.92 : 1 (10.56/0)
			|   |   ratioOfNodesInTriangles >= 1 : 1 (47.14/0)
			
			Size of the tree : 27
			Max depth of tree: 4
	 	*/
		 ArrayList<Attribute> atts = new ArrayList<Attribute>(6);
		 	
		 ArrayList<String> classVal = new ArrayList<String>();
		        classVal.add("0");
		        classVal.add("1");
	        
	        atts.add(new Attribute("averageDegree"));
	        //atts.add(new Attribute("density"));
	        atts.add(new Attribute("avergaeTrianglesRate"));
	        atts.add(new Attribute("ratioOfNodesInTriangles"));
	        atts.add(new Attribute("GCC"));
	        atts.add(new Attribute("ACC"));
	        atts.add(new Attribute("@@class@@",classVal));
	        
	        Instances dataRaw = new Instances("TestInstances",atts,0);
	        
	        dataRaw.add(new DenseInstance(1.0, features));
	        dataRaw.setClassIndex(dataRaw.numAttributes() - 1);	        
	        
	        
	     // Make the prediction here.
	        double predictionIndex = 
	            cls.classifyInstance(dataRaw.instance(0)); 

	        // Get the predicted class label from the predictionIndex.
	        String predictedClassLabel =
	        		dataRaw.classAttribute().value((int) predictionIndex);
	        return predictedClassLabel=="0";
	}

}
