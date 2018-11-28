package asylum.nursebot.persistence.modules;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;

import asylum.nursebot.persistence.selfbuilding.Column;
import asylum.nursebot.persistence.selfbuilding.DataType;
import asylum.nursebot.persistence.selfbuilding.Key;
import asylum.nursebot.persistence.selfbuilding.Selfbuilding;
import asylum.nursebot.persistence.selfbuilding.Type;

public class NurseModule extends Model implements Selfbuilding {
	public String getName() {
		return getString("module");
	}
	
	public boolean isActive() {
		return getBoolean("active");
	}
	
	static public NurseModule byName(String name) {
		List<NurseModule> list = NurseModule.where("module = ?", name);
		if (list == null || list.size() == 0)
			return null;
		return list.get(0);
	}
	
	public NurseModule setName(String name) {
		return set("module", name);
	}
	
	public NurseModule setActive(boolean b) {
		return set("active", b);
	}
	
	public NurseModule setActive() {
		return setActive(true);
	}
	
	public NurseModule setInactive() {
		return setActive(false);
	}
	
	@Override
	public String getSelfbuildingName() {
		return getTableName();
	}
	
	@Override
	public List<Column> getSelfbuildingColumns() {
		return Arrays.asList(new Column("id", new Type(DataType.BIGINT))
			.setKey(Key.PRIMARY_KEY)
			.setAutoincrement(true),
		new Column("module", new Type(DataType.TEXT))
			.setNotNullFlag(true),
		new Column("active", new Type(DataType.TINYINT))
			.setNotNullFlag(true));
	}
}
