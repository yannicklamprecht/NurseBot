package asylum.nursebot.persistence.modules;

import java.util.Arrays;
import java.util.List;

import asylum.nursebot.persistence.selfbuilding.Column;
import asylum.nursebot.persistence.selfbuilding.DataType;
import asylum.nursebot.persistence.selfbuilding.Key;
import asylum.nursebot.persistence.selfbuilding.Selfbuilding;
import asylum.nursebot.persistence.selfbuilding.Type;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;

@IdName("id")
public class BusinessCardsField extends Model implements Selfbuilding {

	public static BusinessCardsField getByName(String name) {
		List<BusinessCardsField> list = find("name = ?", name);
		if (list == null || list.isEmpty())
			return null;
		return list.get(0);
	}
	
	@Override
	public String getSelfbuildingName() {
		return getTableName();
	}

	@Override
	public List<Column> getSelfbuildingColumns() {
		return Arrays.asList(new Column("id", new Type(DataType.INT))
			.setKey(Key.PRIMARY_KEY)
			.setAutoincrement(true),
		new Column("name", new Type(DataType.TEXT))
			.setNotNullFlag(true),
		new Column("label", new Type(DataType.TEXT))
			.setNotNullFlag(true),
		new Column("approved", new Type(DataType.TINYINT))
			.setNotNullFlag(true)
			.setDefaultValue("0"));
	}

	public String getName() {
		return getString("name");
	}

	public String getLabel() {
		return getString("label");
	}

	public boolean isApproved() {
		return getBoolean("approved");
	}

	public void setName(String name) {
		set("name", name);
	}

	public void setLabel(String label) {
		set("label", label);
	}

	public void setApproved(boolean b) {
		set("approved", b ? 1 : 0);
	}

}
