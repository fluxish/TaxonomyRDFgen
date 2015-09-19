package rdfgen.generator;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import rdfgen.data.TaxonomyConcept;
import rdfgen.data.TaxonomyStructure;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Generatore della tassonomia, a partire da una struttura
 */
public class OntTaxonomyGenerator {

	private OntModel model;
	private String rootNS;
	private String skosNS = "http://www.w3.org/2004/02/skos/core#";

	private OntProperty prefLabel;
	private OntProperty hiddenLabel;
	private OntProperty broaderTransitive;
	private OntProperty narrowerTransitive;
	private OntClass skosConcept;

	private TaxonomyStructure structure;
	
	/**
	 * Genera una nuova tassonomia
	 * @param fileName il nome del file in cui è salvata la struttura
	 * @param rootNS il namespace di root
	 */
	public OntTaxonomyGenerator(String fileName, String rootNS) {
		this.rootNS = rootNS;
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.setNsPrefix("skos", skosNS);
		model.setNsPrefix("geo", rootNS);

		prefLabel = model.createOntProperty(skosNS + "prefLabel");
		hiddenLabel = model.createOntProperty(skosNS + "hiddenLabel");
		broaderTransitive = model.createOntProperty(skosNS + "broaderTransitive");
		narrowerTransitive = model.createOntProperty(skosNS + "narrowerTransitive");
		skosConcept = model.createClass(skosNS + "Concept");

		structure = new TaxonomyStructure(fileName);
	}
	
	/**
	 * Genera la tassonomia
	 */
	public void generate() {
		TaxonomyConcept conceptData;
		structure.load();

		while ((conceptData = structure.getNextData()) != null) {
			createConcept(conceptData);
		}

		structure.close();
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
			model.write(new PrintStream(fileName), "OWL/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void createConcept(TaxonomyConcept currConceptData) {
		Individual currConcept, parentConcept;
		Literal prefLabelLiteral, hiddenLabelLiteral;
		
		// Crea il concetto
		currConcept = skosConcept.createIndividual(rootNS + currConceptData.getId());
		currConcept.addProperty(RDF.type, skosConcept);
		
		// crea una prefLabel
		prefLabelLiteral = model.createLiteral(currConceptData.getPrefLabel(), "it");
		currConcept.addLiteral(prefLabel, prefLabelLiteral);
		
		// crea una hiddenLabel dalla prefLabel
		hiddenLabelLiteral = model.createLiteral(currConceptData.getPrefLabel(), "it");
		currConcept.addLiteral(hiddenLabel, hiddenLabelLiteral);
		
		// aggiunge le altre hiddenLabel
		for (String hl : currConceptData.getHiddenLabels()) {
			hiddenLabelLiteral = model.createLiteral(hl, "it");
			currConcept.addLiteral(hiddenLabel, hiddenLabelLiteral);
		}
		
		if(!currConceptData.getParentId().isEmpty()){
			parentConcept = skosConcept.createIndividual(rootNS + currConceptData.getParentId());
			parentConcept.addProperty(narrowerTransitive, currConcept);
			currConcept.addProperty(broaderTransitive, parentConcept);
		}
	}
}