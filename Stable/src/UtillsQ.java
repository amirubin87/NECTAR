import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;

public class UtillsQ {
	
	 
    public static int IntersectionSize(Set<Integer> set1, Set<Integer> set2) {
		int ans = 0;
		Set<Integer> small = set1;
		Set<Integer> big = set2;
		if(set2.size() < set1.size()){
			small = set2;
			big = set1;
		}
		for(Integer i : small){			
				if(big.contains(i)){
					ans++;
				}
			}		
		return ans;
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
		Map<Integer, Double> copy = new ConcurrentHashMap<Integer, Double>();
		for( Entry<Integer, Double> entry: source.entrySet()){
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	public static Map<Integer, Integer> CopyMapIntInt(Map<Integer, Integer> source) {
		Map<Integer, Integer> copy = new ConcurrentHashMap<Integer, Integer>();
		for( Entry<Integer, Integer> entry: source.entrySet()){
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	public static Map<Integer, Map<Integer, Double>> CopyMapIntMapIntDouble(Map<Integer, Map<Integer, Double>> source) {
		Map<Integer, Map<Integer, Double>> copy = new ConcurrentHashMap<Integer, Map<Integer, Double>>();
		for( Entry<Integer, Map<Integer, Double>> entry: source.entrySet()){
			copy.put(entry.getKey(), CopyMapIntDouble(entry.getValue()));
		}
		return copy;
	}

	public static Map<Integer, Set<Integer>> CopyMapIntSet(Map<Integer, Set<Integer>> source) {
		Map<Integer, Set<Integer>> copy = new ConcurrentHashMap<Integer, Set<Integer>>();
		for( Entry<Integer, Set<Integer>> entry: source.entrySet()){
			copy.put(entry.getKey(), CloneSet(entry.getValue()));
		}
		return copy;
	}

	public static Map<Integer, Map<Integer, Integer>> CopyMapIntMapIntInt(Map<Integer, Map<Integer, Integer>> source) {
		Map<Integer, Map<Integer, Integer>> copy = new ConcurrentHashMap<Integer, Map<Integer, Integer>>();
		for( Entry<Integer, Map<Integer, Integer>> entry: source.entrySet()){
			copy.put(entry.getKey(), CopyMapIntInt(entry.getValue()));
		}
		return copy;
	}
	
	public static <T> Set<T> CloneSet(Set<T> source) {
		Set<T> copy = Collections.synchronizedSet(new HashSet<T>(source.size())); 
		Iterator<T> iterator = source.iterator();
		while(iterator.hasNext()){ 
			copy.add((T) iterator.next()); 
			}
		return copy;
	}
	

	

}
