package socialViz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppProperties {

	private Properties prop = new Properties();
	private static String filename = "config.properties";
	private static AppProperties instance = null;
	
	private AppProperties() throws IOException {
		InputStream input = getClass().getClassLoader().getResourceAsStream(filename);
		prop.load(input);
		input.close();
	}

	public static AppProperties getInstance() throws IOException {
		if(instance == null)
			instance = new AppProperties();
		return instance;
	}
	
	public String readProperty(String name) {
		return prop.getProperty(name);
	}
	
	public String getServer() {
		return readProperty("server");
	}
	
	public String getPort() {
		return readProperty("port");
	}
	
	public String getDatabase() {
		return readProperty("database");
	}
	
	public String getTablename() {
		return readProperty("tablename");
	}
	
	public String getUsername() {
		return readProperty("user");
	}
	
	public String getPassword() {
		return readProperty("password");
	}
	
	public String getHashtagFile() {
		return readProperty("hashtagFile");
	}
	
	public String getCreationDateColumn() {
		return readProperty("column_creationDate");
	}
	
	public String getHashtagsColumn() {
		return readProperty("column_hashtags");
	}
	
	public String getJsonOutputFolder() {
		return readProperty("jsonOutputFolder");
	}
	
	public String getOutputFolder() {
		return readProperty("outputFolder");
	}
	
	public String getNodeCommand() {
		return readProperty("nodeCommand");
	}
	
	public String getNodeScript() {
		return readProperty("nodeScript");
	}
	
	public String getffmegCommand() {
		return readProperty("ffmegCommand");
	}
	
	public String getffmegInputOptions() {
		return readProperty("ffmegInputOptions");
	}
	
	public String getffmegOutputOptions() {
		return readProperty("ffmegOutputOptions");
	}
	
	public int getNumThreads() {
		return Integer.parseInt(readProperty("numThreads"));
	}
}
