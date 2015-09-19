package rdfgen.data;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Rappresenta un concetto di una tassonomia, estratto da un file di testo.
 * 
 * Il concetto è definito da una stringa di caratteri con tre campi separati
 * dal carattere cancelletto (#):
 * - label del concetto genitore (prefLabel)
 * - label del concetto corrente (prefLabel)
 * - lista di label (hiddenLabel) seaparate da punto e virgola.
 * A partire dalle prefLabel si genera l'id del concetto
 */
public class TaxonomyConcept implements Data {
	
	private String id;
	private String prefLabel;
	private List<String> hiddenLabels;
	private String parentId;

	private TaxonomyConcept(){}
	
	public static TaxonomyConcept build(String line){
		String labelsListStr;
		Pattern linePattern = Pattern.compile(LINE_PATTERN);
		Matcher matcher = linePattern.matcher(line);
		TaxonomyConcept concept = null;
		if (matcher.matches()) {
			concept = new TaxonomyConcept();
			concept.setParentId(generateIdByLabel(matcher.group(1)));
			concept.setId(generateIdByLabel(matcher.group(2)));
			concept.setPrefLabel(StringUtils.capitalize(matcher.group(2)));
			
			labelsListStr = matcher.group(3);
			if(!labelsListStr.isEmpty()){
				concept.setHiddenLabels(Arrays.asList(labelsListStr.split(";")));
			} else {
				concept.setHiddenLabels(new ArrayList<String>());
			}
		}
		return concept;
	}
	
	public static String generateIdByLabel(String label){
		return Normalizer.normalize(WordUtils.capitalizeFully(label), Normalizer.Form.NFD)
				.replaceAll("[^A-Za-z0-9]", "");
	}
	
	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

	public String getPrefLabel() {
		return prefLabel;
	}

	private void setPrefLabel(String prefLabel) {
		this.prefLabel = prefLabel;
	}

	public List<String> getHiddenLabels() {
		return hiddenLabels;
	}

	private void setHiddenLabels(List<String> hiddenLabels) {
		this.hiddenLabels = hiddenLabels;
	}

	public String getParentId() {
		return parentId;
	}

	private void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	private static String LINE_PATTERN = "^(.*)#(.*)#(.*)$";
}
