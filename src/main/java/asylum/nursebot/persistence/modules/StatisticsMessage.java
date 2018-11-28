package asylum.nursebot.persistence.modules;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.javalite.activejdbc.Model;

import asylum.nursebot.persistence.selfbuilding.Column;
import asylum.nursebot.persistence.selfbuilding.DataType;
import asylum.nursebot.persistence.selfbuilding.Key;
import asylum.nursebot.persistence.selfbuilding.Selfbuilding;
import asylum.nursebot.persistence.selfbuilding.Type;

public class StatisticsMessage extends Model implements Selfbuilding {
	public StatisticsMessage setChatId(long v) {
		set("chatid", v);
		return this;
	}
	public StatisticsMessage setLength(int i) {
		set("length", i);
		return this;
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
		new Column("chatid", new Type(DataType.BIGINT))
			.setNotNullFlag(true),
		new Column("length", new Type(DataType.INT))
			.setNotNullFlag(true),
		new Column("time", new Type(DataType.TIMESTAMP))
			.setDefaultValue("CURRENT_TIMESTAMP")
			.setNotNullFlag(true));
	}
	public Timestamp getTime() {
		return getTimestamp("time");
	}
	public int getLength() {
		return getInteger("length");
	}
}
