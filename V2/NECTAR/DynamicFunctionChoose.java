package NECTAR;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class DynamicFunctionChoose {
	
	public static boolean shouldUseModularity(double[] features) throws Exception {
		Classifier cls = 
				(Classifier) weka.core.SerializationHelper.read((InputStream) Classifier.class.getResourceAsStream("/NECTAR/NECTARRandomTree.model"));		 		 
		 ArrayList<Attribute> atts = new ArrayList<Attribute>(7);
		 	
		 ArrayList<String> classVal = new ArrayList<String>();
		        classVal.add("0");
		        classVal.add("1");
	        
	        atts.add(new Attribute("averageDegree"));
	        atts.add(new Attribute("density"));
	        atts.add(new Attribute("avergaeTrianglesRate"));
	        atts.add(new Attribute("ratioOfNodesInTriangles"));
	        atts.add(new Attribute("GCC"));
	        atts.add(new Attribute("ACC"));
	        atts.add(new Attribute("@@class@@",classVal));
	        
	        Instances dataRaw = new Instances("TestInstances",atts,0);
	        
	        dataRaw.add(new DenseInstance(1.0, features));
	        dataRaw.setClassIndex(dataRaw.numAttributes() - 1);
	        //System.out.println(dataRaw);
	        
	        
	     // Make the prediction here.
	        double predictionIndex = 
	            cls.classifyInstance(dataRaw.instance(0)); 

	        // Get the predicted class label from the predictionIndex.
	        String predictedClassLabel =
	        		dataRaw.classAttribute().value((int) predictionIndex);
	        return predictedClassLabel=="0";
	}

}
