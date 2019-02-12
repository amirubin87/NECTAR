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
		@Override
		public boolean equals(Object other){
			if (!( other  instanceof Edge)){
				return false;
			}
			return from.equals(((Edge)other).getFrom()) & to.equals(((Edge)other).getTo());
		}
		
		@Override
	    public int hashCode() {
	        int hash = 17;
	        hash = hash*37 + from.hashCode();
	        hash = hash*37 + to.hashCode();
	        return hash;
	    }
	}