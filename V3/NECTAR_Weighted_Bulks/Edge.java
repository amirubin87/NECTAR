package NECTAR_Weighted_Bulks;

public class Edge implements Comparable<Edge>{
		private final Integer from;
		private final Integer to;
		private final Double weight;
		
		public Edge(Integer from, Integer to){
			this.from = from;
			this.to = to;
			weight = 1.0;
		}
		
		public Edge(Integer from, Integer to, double w){
			this.from = from;
			this.to = to;
			weight = w;
		}
		
		public Integer getFrom(){return from;}
		public Integer getTo(){return to;}
		public double getW(){return weight;}
		
		@Override
		public String toString(){
			return "(" + ((from!=null) ? from.toString() : null) + "," + ((to!=null) ? to.toString() : null) + 
					"," + weight + ")";
		}

		@Override
		public int compareTo(Edge o) {			
			return weight > o.weight ? 1 : weight < o.weight ? -1 : 0;
		}
	}