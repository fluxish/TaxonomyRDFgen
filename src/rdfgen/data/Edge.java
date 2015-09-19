package rdfgen.data;

public class Edge implements Data {
	
	private String fromNode;
	private String toNode;

	private Edge(){}
	
	public static Edge build(String line){
		String[] edgeList = line.split(",");
		Edge edge = null;
		if (edgeList.length > 0) {
			edge = new Edge();
			edge.fromNode = edgeList[0];
			edge.toNode = edgeList[1];
		}
		return edge;
	}

	public String getFromNode() {
		return fromNode;
	}

	public String getToNode() {
		return toNode;
	}
}
