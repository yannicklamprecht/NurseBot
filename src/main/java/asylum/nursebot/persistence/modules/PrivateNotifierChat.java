package asylum.nursebot.persistence.modules;

import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;

import asylum.nursebot.persistence.selfbuilding.Column;
import asylum.nursebot.persistence.selfbuilding.DataType;
import asylum.nursebot.persistence.selfbuilding.Key;
import asylum.nursebot.persistence.selfbuilding.Selfbuilding;
import asylum.nursebot.persistence.selfbuilding.Type;

public class PrivateNotifierChat extends Model implements Selfbuilding {
	
	public PrivateNotifierChat() {
	}
	
	public PrivateNotifierChat(int userid) {
		setLong("userid", userid);
	}

	public static PrivateNotifierChat find(long userid) {
		List<PrivateNotifierChat> list = NurseModule.where("userid = ?", userid);
		if (list == null || list.size() == 0)
			return null;
		return list.get(0);
	}
	
	@Override
	public String getSelfbuildingName() {
		return getTableName();
	}
	
	@Override
	public List<Column> getSelfbuildingColumns() {
		return Arrays.asList(new Column("userid", new Type(DataType.INT))
			.setKey(Key.PRIMARY_KEY));
	}

	public Integer getUserId() {
		return getInteger("userid");
	}
}