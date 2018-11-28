package asylum.nursebot.persistence.modules;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Many2Many;

import asylum.nursebot.persistence.selfbuilding.Column;
import asylum.nursebot.persistence.selfbuilding.DataType;
import asylum.nursebot.persistence.selfbuilding.Key;
import asylum.nursebot.persistence.selfbuilding.Selfbuilding;
import asylum.nursebot.persistence.selfbuilding.Type;

@IdName("id")
@Many2Many(other = BusinessCardsField.class, join = "business_cards_entries", sourceFKName = "business_cards_card_id", targetFKName = "business_cards_field_id")
public class BusinessCardsCard extends Model implements Selfbuilding {

	public static List<BusinessCardsCard> getByName(String name, int userid) {
		return find("name = ? AND userid = ?", name, userid);
	}
	
	public static List<BusinessCardsCard> getByUserid(int userid) {
		return find("userid = ?", userid);
	}
	
	public BusinessCardsCard setUserid(int id) {
		set("userid", id);
		return this;
	}
	
	public BusinessCardsCard setName(String name) {
		set("name", name);
		return this;
	}
	
	public BusinessCardsCard setPublic(boolean pub) {
		set("public", pub ? 1 : 0);
		return this;
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
		new Column("userid", new Type(DataType.INT))
			.setNotNullFlag(true),
		new Column("name", new Type(DataType.TEXT))
			.setNotNullFlag(true),
		new Column("public", new Type(DataType.TINYINT))
			.setNotNullFlag(true)
			.setDefaultValue("0"));
	}

	public String getName() {
		return getString("name");
	}

	public boolean isPublic() {
		return getBoolean("public");
	}
}
