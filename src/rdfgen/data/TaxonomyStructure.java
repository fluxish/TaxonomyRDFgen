package rdfgen.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Interprete della struttura della tassonomia rappresentata in un file di testo.
 * Ciascuna linea del file rappresenta un concetto della tassonomia
 * 
 * @see TaxonomyConcept
 */
public class TaxonomyStructure implements Dataset<TaxonomyConcept> {

	private String fileName;
	private BufferedReader br;
	private TaxonomyConcept currentData;
	
	/**
	 * Crea una nuovo interprete partire da un file
	 * @param fileName nome del file
	 */
	public TaxonomyStructure(String fileName) {
		this.fileName= fileName; 
	}
	
	/**
	 * Carica il file con la struttura
	 */
	public void load() {
		if (br == null) {
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), Charset.forName("UTF-8")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Chiude il file con la struttura
	 */
	public void close() {
		try {
			br.close();
			br = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Restituisce il prossimo concetto della struttura
	 * @return concetto della struttura
	 */
	public TaxonomyConcept getNextData() {
		try {
			String line = br.readLine();
			if (line != null) {
				currentData = TaxonomyConcept.build(line);
				return currentData;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
