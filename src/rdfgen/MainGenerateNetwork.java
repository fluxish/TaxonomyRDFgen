package rdfgen;

import java.util.ArrayList;
import java.util.List;

import rdfgen.data.Edge;
import rdfgen.data.EdgeDataset;
import rdfgen.generator.RDFNetworkGenerator;

public class MainGenerateNetwork {

	public static void main (String[] args){
		
		String networkFileNameIn = "G:/corese_test/networks/boards_779_8.rdf";
		String networkFileNameOut = "G:/corese_test/networks/boards_b.rdf";
		
		RDFNetworkGenerator rdfNetworkGenerator = new RDFNetworkGenerator(networkFileNameIn);
		
		String edgesFileName = "G:/corese_test/networks/notexists_edges.txt";
		EdgeDataset edgeDataset = new EdgeDataset(edgesFileName);
		edgeDataset.load();
		List<Edge> notexists_edges = new ArrayList<Edge>();
		Edge edge;
		while((edge = edgeDataset.getNextData()) != null){
			notexists_edges.add(edge);
		}
		
		rdfNetworkGenerator.addRandomKnows(10, 10, notexists_edges);
		//rdfNetworkGenerator.removeRandomKnows(1, 50);
		rdfNetworkGenerator.write(networkFileNameOut);
		System.out.println("Fatto");
	}
}