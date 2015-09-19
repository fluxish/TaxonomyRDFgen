package rdfgen.util;

import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

public class Config {

	private String fileName = "config.properties";
	private Properties prop;
	private static Config instance = null;

	private Config() {
		prop = new Properties();
		try {
			prop.load(getClass().getClassLoader().getResourceAsStream(fileName));
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getProperty(String key){
		return prop.getProperty(key);
	}
	
	public static Config getInstance(){
		if(instance == null) instance = new Config();
		return instance;
	}
}