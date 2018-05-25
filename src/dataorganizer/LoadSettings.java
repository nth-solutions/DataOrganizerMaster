package dataorganizer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class LoadSettings {
	Properties prop = new Properties();
	
	
	public void loadDefaultConfig() {
		this.prop.setProperty("CSVSaveLocation","");
		this.prop.setProperty("DefaultProfile", "");
		this.prop.setProperty("TemplateDirectory", "");
	}
	
	public void loadConfigFile() throws FileNotFoundException, IOException {
		this.prop.load(new FileInputStream("DataOrganizer.prop"));
	}
	
	public void setProp(String key, String val) {
		this.prop.setProperty(key, val);
		this.saveConfig();
	}
	
	public void saveConfig() {
		try {
			this.prop.store(new FileOutputStream("DataOrganizer.prop"), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getKeyVal(String Key) {
		return this.prop.getProperty(Key);
	}
}
