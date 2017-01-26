package NECTAR_Weighted;

public class Edge {
		private final Object from;
		private final Object to;
		
		public Edge(Object from, Object to){
			this.from = from;
			this.to = to;
		}
		
		public Object getFrom(){return from;}
		public Object getTo(){return to;}
		
		@Override
		public String toString(){
			return "(" + ((from!=null) ? from.toString() : null) + "," + ((to!=null) ? to.toString() : null) + ")";
		}
	}