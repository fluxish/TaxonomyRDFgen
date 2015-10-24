package rdfgen;

import rdfgen.generator.OntTaxonomyGenerator;
import rdfgen.generator.RDFTaxonomyGenerator;
import rdfgen.generator.TaxonomyGenerator;
import rdfgen.util.Config;

public class Main {

	public static void main (String[] args){
		
		Config config = Config.getInstance();
		
		String rootNS; 
		String inputPath = config.getProperty("input.path");
		String outputPath = config.getProperty("output.path");
		String outputFileNamePrefix = config.getProperty("output.fileNamePrefix");
		String[] fileNames = config.getProperty("input.filenames").split(",");
		
		String inputFileEstension = config.getProperty("input.fileExtension");
		String outputFileEstension = config.getProperty("output.fileExtension");
		
		for(String fileName : fileNames){
			rootNS = config.getProperty("namespace.root") + fileName + "#";
			TaxonomyGenerator generator;
			if(outputFileEstension.equals(".ttl")){
				generator = new OntTaxonomyGenerator(inputPath + fileName + inputFileEstension, rootNS);
			}
			else {
				generator = new RDFTaxonomyGenerator(inputPath + fileName + inputFileEstension, rootNS);
			}
			generator.generate();
			generator.write(outputPath + outputFileNamePrefix + fileName + outputFileEstension);
		}
	}

}
