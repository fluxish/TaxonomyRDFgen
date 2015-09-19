package rdfgen.generator;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.jena.riot.RDFDataMgr;

import rdfgen.data.Edge;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Generatore della tassonomia, a partire da una struttura
 */
public class RDFNetworkGenerator {

	private Model model;
	private String foafNS = "http://xmlns.com/foaf/0.1/";
	private Resource foafPerson;
	private Property foafKnows;
	private List<Statement> existsEdges;
	
	public RDFNetworkGenerator(String fileName) {
		model = RDFDataMgr.loadModel(fileName);
		model.setNsPrefix("foaf", foafNS);
		
		foafKnows = model.createProperty(foafNS + "knows");
		foafPerson = model.createResource(foafNS + "Person");
		
		Resource foafNull = null;
		
		StmtIterator iter = model.listStatements(foafNull, foafKnows, foafNull);
		existsEdges = new ArrayList<Statement>();
		while(iter.hasNext()){
			existsEdges.add(iter.next());
		}
	}
	
	/**
	 * Salva la tassonomia in un file in formato RDF/XML
	 * @param fileName nome del file in cui salvare la tassonomia
	 */
	public void write(String fileName){
		if(fileName == null){ 
			model.write(System.out, "RDF/XML");
		}
		
		try {
			model.write(new PrintStream(fileName), "RDF/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void addRandomKnows(int numClusters, int numEdgesClusters, List<Edge> edges){
		int i, j, selectedIndex;
		int clusterSize = (int)Math.ceil(edges.size() / numClusters);
		int size = edges.size();
		boolean added = false;
		Edge edge;
		Random rand = new Random();
		Set<Integer> generatedNumbers = new HashSet<Integer>();
		
		for(i=0; i<numClusters; i++){
			j = 0;
			while(j<numEdgesClusters){
				while(true){
					selectedIndex = rand.nextInt(clusterSize) + (i*clusterSize); //seleziona a caso un indice corretto nel cluster
					if(!generatedNumbers.contains(selectedIndex) && selectedIndex < size){
						generatedNumbers.add(selectedIndex);
						break;
					}
				}
				edge = edges.get(selectedIndex);
				added = addKnows(edge.getFromNode(), edge.getToNode());
				if(added) j++;
			}
		}
	}
	
	public void removeRandomKnows(int numClusters, int numEdgesClusters){
		int i, j, selectedIndex;
		int clusterSize = (int)Math.ceil(existsEdges.size() / numClusters);
		int size = existsEdges.size();
		Statement stmt;
		Random rand = new Random();
		Set<Integer> generatedNumbers = new HashSet<Integer>();
		
		for(i=0; i<numClusters; i++){
			j = 0;
			while(j<numEdgesClusters){
				while(true){
					selectedIndex = rand.nextInt(clusterSize) + (i*clusterSize); //seleziona a caso un indice corretto nel cluster
					if(!generatedNumbers.contains(selectedIndex) && selectedIndex < size){
						generatedNumbers.add(selectedIndex);
						break;
					}
				}
				stmt = existsEdges.get(selectedIndex);
				model.remove(stmt);
				j++;
			}
		}
	}
	
	public boolean addKnows(String idFrom, String idTo) {
		Resource nodeFrom = model.getResource(idFrom);
		Resource nodeTo = model.getResource(idTo);
		
		if(model.contains(nodeFrom, foafKnows, nodeTo)) return false;
		
		nodeFrom.addProperty(foafKnows, nodeTo);
		//System.out.printf("%s -> %s\n", idFrom, idTo);
		return true;
	}
}