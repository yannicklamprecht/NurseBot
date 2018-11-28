package asylum.nursebot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigHolder {
	private static ConfigHolder holder;
	
	public static ConfigHolder getInstance() throws IOException {
		if (holder == null)
			holder = new ConfigHolder();
		
		return holder;
	}
	
	private static final String FILE = "config.properties";
	
	private Properties properties;

	public String getTelegramToken() {
		return properties.getProperty("telegram.token");
	}

	public String getDatabaseHost() {
		return properties.getProperty("database.host");
	}
	
	public String getDatabaseSchema() {
		return properties.getProperty("database.schema");
	}
	
	public String getDatabaseUser() {
		return properties.getProperty("database.user");
	}
	
	public String getDatabasePassword() {
		return properties.getProperty("database.password");
	}
	
	private ConfigHolder() throws IOException {
		properties = new Properties();
		
		InputStream input = new FileInputStream(FILE);
		
		properties.load(input);
	}

	public String getTelegramUser() {
		return properties.getProperty("telegram.username");
	}
}
