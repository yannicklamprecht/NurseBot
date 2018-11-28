package asylum.nursebot.persistence.selfbuilding;

import java.util.List;

import org.javalite.activejdbc.Base;

public interface Selfbuilding {
	
	String getSelfbuildingName();
	List<Column> getSelfbuildingColumns();
	
	default boolean selfbuild() {
		List<Column> list = getSelfbuildingColumns();
		
		System.out.println("Checking for table " + getSelfbuildingName() + "...");
		boolean present = Base.firstCell("SHOW TABLES LIKE '" + getSelfbuildingName() + "'") != null;
		
		if (present) {
			System.out.println("... present"); 
		} else {
			System.out.println("... missing"); 
			StringBuilder builder = new StringBuilder();
			builder.append("CREATE TABLE IF NOT EXISTS `").append(getSelfbuildingName()).append("`\n(\n");
			boolean first = true;
			for(Column column : list) {
				if (first) {
					first = false;
				} else {
					builder.append(", \n");
				}
				builder.append(column.toString());
			}
			builder.append("\n)");
			
			System.out.println("Creating table " + getSelfbuildingName() + "...");
			try {
				Base.exec(builder.toString());
			} catch (Exception e) {
				System.out.println("... failed.");
				throw e;
			}
			System.out.println("... success");
		}
		
		return !present;
	}
}
