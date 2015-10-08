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
	private List<String> prefLabels;
	private List<String> altLabels;
	private List<String> hiddenLabels;
	private String parentId;

	private TaxonomyConcept(){}
	
	public static TaxonomyConcept build(String line){
		String prefLabelsListStr, altLabelsListStr, hiddenLabelsListStr;
		List<String> prefLabels = new ArrayList<String>();
		List<String> altLabels = new ArrayList<String>();
		
		Pattern linePattern = Pattern.compile(LINE_PATTERN);
		Matcher matcher = linePattern.matcher(line);
		TaxonomyConcept concept = null;
		if(matcher.matches()) {
			concept = new TaxonomyConcept();
			concept.setParentId(generateIdByLabel(matcher.group(1)));
			concept.setId(generateIdByLabel(matcher.group(2)));
			
			prefLabelsListStr = matcher.group(2);
			for(String prefLabelStr : prefLabelsListStr.split(";")){
				prefLabels.add(StringUtils.capitalize(prefLabelStr.replaceAll("[^\\p{L}0-9\\@\\ ]", "").trim()));
			}
			concept.setPrefLabels(prefLabels);
			concept.setId(generateIdByLabel(prefLabels.get(0)));
			
			altLabelsListStr = matcher.group(3);
			if(!altLabelsListStr.isEmpty()){
				for(String altLabelStr : altLabelsListStr.split(";")){
					altLabels.add(StringUtils.capitalize(altLabelStr.replaceAll("[^\\p{L}0-9\\@\\ ]", "").trim()));
				}
			}
			concept.setAltLabels(altLabels);
			
			hiddenLabelsListStr = matcher.group(4);
			if(!hiddenLabelsListStr.isEmpty()){
				concept.setHiddenLabels(Arrays.asList(hiddenLabelsListStr.split(";")));
			} else {
				concept.setHiddenLabels(new ArrayList<String>());
			}
		}
		return concept;
	}
	
	public static String generateIdByLabel(String label){
		return Normalizer.normalize(WordUtils.capitalizeFully(label), Normalizer.Form.NFD)
				.replaceAll("[^A-Za-z0-9\\_]", "");
	}
	
	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

	public List<String> getPrefLabels() {
		return prefLabels;
	}

	public List<String> getAltLabels() {
		return altLabels;
	}

	public void setAltLabels(List<String> altLabels) {
		this.altLabels = altLabels;
	}

	private void setPrefLabels(List<String> prefLabel) {
		this.prefLabels = prefLabel;
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
	
	private static String LINE_PATTERN = "^(.*)#(.*)#(.*)#(.*)$";
}
