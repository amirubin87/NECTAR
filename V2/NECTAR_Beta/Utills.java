package NECTAR_Beta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

// Utilities methods. 
public class Utills {
	
	public static double[] ParseDoubleArray(String string) {
		String[] parts = string.split(",");
		double[] ans= new double[parts.length];
	    int i=0;
	    for(String str:parts){
	    	ans[i]=Double.parseDouble(str);
	        i++;
	    }
		return ans;
	}
	
    public static Set<Integer> Intersection(Set<Integer> set1, Set<Integer> set2) {
    	Set<Integer> ans = new HashSet<>();
		Set<Integer> small = set1;
		Set<Integer> big = set2;
		if(set2.size() < set1.size()){
			small = set2;
			big = set1;
		}
		for(Integer i : small){			
				if(big.contains(i)){
					ans.add(i);
				}
			}		
		return ans;
	}

    public static int IntersectionSize(Set<Integer> set1, Set<Integer> set2) {
		return Intersection(set1, set2).size();    
	}
    
	public Map<Integer,List<Integer>> RenumberComms(Map<Integer,List<Integer>> map){
	    int count = 0;
		Map<Integer,List<Integer>> ans = new HashMap<Integer, List<Integer>>();
		Map<Integer,Integer> new_values = new HashMap<Integer,Integer>();
	    for (Entry<Integer,List<Integer>>entry : map.entrySet()){
	    	List<Integer> newSet = new ArrayList<Integer>();	        
	        for (int comm : entry.getValue()){
	            Integer new_value = new_values.get((Integer)comm);
	            if (new_value == null){
	                new_values.put((Integer)comm, count);
	                new_value = count;
	                count = count + 1;
	            }
	            newSet.add(new_value);
	        }
	        ans.put(entry.getKey(), newSet);
	    }
	    return ans;
	}

	public static Map<Integer, Double> CopyMapIntDouble(Map<Integer, Double> source) {
		Map<Integer, Double> copy = new HashMap<Integer, Double>();
		for( Entry<Integer, Double> entry: source.entrySet()){
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	public static Map<Integer, Integer> CopyMapIntInt(Map<Integer, Integer> source) {
		Map<Integer, Integer> copy = new HashMap<Integer, Integer>();
		for( Entry<Integer, Integer> entry: source.entrySet()){
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	public static Map<Integer, Long> CopyMapIntLong(Map<Integer, Long> source) {
		Map<Integer, Long> copy = new HashMap<Integer, Long>();
		for( Entry<Integer, Long> entry: source.entrySet()){
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	public static Map<Integer, Map<Integer, Double>> CopyMapIntMapIntDouble(Map<Integer, Map<Integer, Double>> source) {
		Map<Integer, Map<Integer, Double>> copy = new HashMap<Integer, Map<Integer, Double>>();
		for( Entry<Integer, Map<Integer, Double>> entry: source.entrySet()){
			copy.put(entry.getKey(), CopyMapIntDouble(entry.getValue()));
		}
		return copy;
	}

	public static Map<Integer, Set<Integer>> CopyMapIntSet(Map<Integer, Set<Integer>> source) {
		Map<Integer, Set<Integer>> copy = new HashMap<Integer, Set<Integer>>();
		for( Entry<Integer, Set<Integer>> entry: source.entrySet()){
			copy.put(entry.getKey(), CloneSet(entry.getValue()));
		}
		return copy;
	}

	public static Map<Integer, Map<Integer, Integer>> CopyMapIntMapIntInt(Map<Integer, Map<Integer, Integer>> source) {
		Map<Integer, Map<Integer, Integer>> copy = new HashMap<Integer, Map<Integer, Integer>>();
		for( Entry<Integer, Map<Integer, Integer>> entry: source.entrySet()){
			copy.put(entry.getKey(), CopyMapIntInt(entry.getValue()));
		}
		return copy;
	}
	
	public static <T> Set<T> CloneSet(Set<T> source) {
		Set<T> copy = new HashSet<T>(source.size()); 
		Iterator<T> iterator = source.iterator();
		while(iterator.hasNext()){ 
			copy.add((T) iterator.next()); 
			}
		return copy;
	}
	
	// Sort a map on the base of its values.
	// Based on the code from :
	// 	http://stackoverflow.com/questions/6290406/generic-method-to-sort-a-map-on-values
    public static <K, V extends Comparable<? super V>> Map<K, V> 
        sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
            new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return -1*(o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

}
