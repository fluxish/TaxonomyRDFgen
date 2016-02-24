package rdfgen.generator;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import rdfgen.data.TaxonomyConcept;
import rdfgen.data.TaxonomyStructure;
import rdfgen.util.Config;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Generatore della tassonomia, a partire da una struttura
 */
public class OntTaxonomyGenerator implements TaxonomyGenerator {

	private OntModel model;
	private String rootNS;
	private String skosNS = "http://www.w3.org/2004/02/skos/core#";
	private String rdfsNS = "http://www.w3.org/2000/01/rdf-schema#";

	private OntProperty prefLabel;
	private OntProperty altLabel;
	private OntProperty hiddenLabel;
	private OntProperty broader;
	private OntProperty narrower;
	private OntProperty related;
	private OntClass skosConcept;
	private OntClass rootClass;

	private TaxonomyStructure structure;
	
	private String lang;
	
	/**
	 * Genera una nuova tassonomia
	 * @param fileName il nome del file in cui è salvata la struttura
	 * @param rootNS il namespace di root
	 */
	public OntTaxonomyGenerator(String fileName, String rootNS) {
		this.rootNS = rootNS;
		model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.setNsPrefix("skos", skosNS);
		model.setNsPrefix("rdfs", rdfsNS);

		prefLabel = model.createDatatypeProperty(skosNS + "prefLabel");
		altLabel = model.createDatatypeProperty(skosNS + "altLabel");
		hiddenLabel = model.createDatatypeProperty(skosNS + "hiddenLabel");
		broader = model.createObjectProperty(skosNS + "broader");
		narrower = model.createObjectProperty(skosNS + "narrower");
		related = model.createObjectProperty(skosNS + "related");
		skosConcept = model.createClass(skosNS + "Concept");

		structure = new TaxonomyStructure(fileName);
		
		lang = Config.getInstance().getProperty("label.lang");
	}
	
	/**
	 * Genera la tassonomia
	 */
	public void generate() {
		TaxonomyConcept conceptData;
		structure.load();

		while ((conceptData = structure.getNextData()) != null) {
			if(conceptData.getParentId().isEmpty()){
				//La root diventa tipo della tassonomia (sottoclasse di skos:Concept)
				createRootConcept(conceptData);
			} else {
				createConcept(conceptData);
			}
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
			model.write(new PrintStream(fileName), "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Resource createRootConcept(TaxonomyConcept rootConcept){
		rootClass = model.createClass(rootNS + rootConcept.getId());
		Property subClassOf = model.createProperty(rdfsNS + "subClassOf");
		rootClass.addProperty(subClassOf, skosConcept);
		return rootClass;
	}
	
	private void createConcept(TaxonomyConcept currConceptData) {
		Individual currConcept, parentConcept;
		Literal prefLabelLiteral, altLabelLiteral, hiddenLabelLiteral;
		String[] labelParts;
		String labelLang;
		System.out.println(currConceptData.getParentId() + "." + currConceptData.getId());
		// Crea il concetto
		currConcept = skosConcept.createIndividual(rootNS + currConceptData.getId());
		currConcept.addProperty(RDF.type, rootClass);
		
		// crea una prefLabel
		for (String pl : currConceptData.getPrefLabels()) {
			labelParts = pl.split("@");
			if(labelParts.length > 1) labelLang = labelParts[1];
			else labelLang = lang;
			
			prefLabelLiteral = model.createLiteral(labelParts[0], labelLang);
			currConcept.addLiteral(prefLabel, prefLabelLiteral);
		}
		
		// crea le altlabel
		for (String al : currConceptData.getAltLabels()) {
			labelParts = al.split("@");
			if(labelParts.length > 1) labelLang = labelParts[1];
			else labelLang = lang;
			
			altLabelLiteral = model.createLiteral(labelParts[0], labelLang);
			currConcept.addLiteral(altLabel, altLabelLiteral);
		}
		
		
		//crea le hidden label
		for (String hl : currConceptData.getHiddenLabels()) {
			labelParts = hl.split("@");
			if(labelParts.length > 1) labelLang = labelParts[1];
			else labelLang = lang;
			
			hiddenLabelLiteral = model.createLiteral(labelParts[0], labelLang);
			currConcept.addLiteral(hiddenLabel, hiddenLabelLiteral);
		}
		
		
		Resource res;
		String namespaceRoot = Config.getInstance().getProperty("namespace.root");
		String resURI;
		for (String rel : currConceptData.getRelateds()) {
			resURI = namespaceRoot + rel;
			res = ResourceFactory.createResource(resURI);
			currConcept.addProperty(related, res);
		}
		
		parentConcept = model.getIndividual(rootNS + currConceptData.getParentId());
		if(!parentConcept.equals(rootClass)){
			parentConcept.addProperty(narrower, currConcept);
			currConcept.addProperty(broader, parentConcept);
		}
	}
}