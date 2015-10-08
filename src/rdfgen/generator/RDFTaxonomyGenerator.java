package rdfgen.generator;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import rdfgen.data.TaxonomyConcept;
import rdfgen.data.TaxonomyStructure;
import rdfgen.util.Config;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Generatore della tassonomia, a partire da una struttura
 */
public class RDFTaxonomyGenerator {

	private Model model;
	private String rootNS;
	private String skosNS = "http://www.w3.org/2004/02/skos/core#";
	private String rdfsNS = "http://www.w3.org/2000/01/rdf-schema#";

	private Property prefLabel;
	private Property altLabel;
	private Property hiddenLabel;
	private Property broader;
	private Property narrower;
	private Resource skosConcept;
	private Resource rootClass;

	private TaxonomyStructure structure;
	
	private String lang;
	
	/**
	 * Genera una nuova tassonomia
	 * @param fileName il nome del file in cui è salvata la struttura
	 * @param rootNS il namespace di root
	 */
	public RDFTaxonomyGenerator(String fileName, String rootNS) {
		this.rootNS = rootNS;
		model = ModelFactory.createDefaultModel();
		model.setNsPrefix("skos", skosNS);
		model.setNsPrefix("rdfs", rdfsNS);

		prefLabel = model.createProperty(skosNS + "prefLabel");
		altLabel = model.createProperty(skosNS + "altLabel");
		hiddenLabel = model.createProperty(skosNS + "hiddenLabel");
		broader = model.createProperty(skosNS + "broader");
		narrower = model.createProperty(skosNS + "narrower");
		skosConcept = model.createResource(skosNS + "Concept");

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
	
	public Resource createRootConcept(TaxonomyConcept rootConcept){
		rootClass = model.createResource(rootNS + rootConcept.getId());
		Property subClassOf = model.createProperty(rdfsNS + "subClassOf");
		rootClass.addProperty(subClassOf, skosConcept);
		return rootClass;
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
	
	private void createConcept(TaxonomyConcept currConceptData) {
		Resource currConcept, parentConcept;
		Literal prefLabelLiteral, altLabelLiteral, hiddenLabelLiteral;
		String[] labelParts;
		String labelLang;
		
		currConcept = model.createResource(rootNS + currConceptData.getId());
		currConcept.addProperty(RDF.type, rootClass);
		
		for (String pl : currConceptData.getPrefLabels()) {
			labelParts = pl.split("@");
			if(labelParts.length > 1) labelLang = labelParts[1];
			else labelLang = lang;
			
			prefLabelLiteral = model.createLiteral(labelParts[0], labelLang);
			currConcept.addLiteral(prefLabel, prefLabelLiteral);
		}
		
		for (String al : currConceptData.getAltLabels()) {
			labelParts = al.split("@");
			if(labelParts.length > 1) labelLang = labelParts[1];
			else labelLang = lang;
			
			altLabelLiteral = model.createLiteral(labelParts[0], labelLang);
			currConcept.addLiteral(altLabel, altLabelLiteral);
		}
		
		for (String hl : currConceptData.getHiddenLabels()) {
			labelParts = hl.split("@");
			if(labelParts.length > 1) labelLang = labelParts[1];
			else labelLang = lang;
			
			hiddenLabelLiteral = model.createLiteral(labelParts[0], labelLang);
			currConcept.addLiteral(hiddenLabel, hiddenLabelLiteral);
		}
		
		parentConcept = model.getResource(rootNS + currConceptData.getParentId());
		if(!parentConcept.equals(rootClass)){
			parentConcept.addProperty(narrower, currConcept);
			currConcept.addProperty(broader, parentConcept);
		}
	}
}